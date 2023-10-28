package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ItemTestimonialV2Binding
import com.doubtnutapp.databinding.WidgetTestimonialV2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.hide
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.show
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TestimonialWidgetV2(context: Context) :
    BaseBindingWidget<TestimonialWidgetV2.WidgetViewHolder,
        TestimonialWidgetModelV2, WidgetTestimonialV2Binding>(context) {

    companion object {
        const val TAG = "TestimonialWidgetV2"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetTestimonialV2Binding {
        return WidgetTestimonialV2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: TestimonialWidgetModelV2
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: TestimonialWidgetDataV2 = model.data

        binding.countOne.text = data.courseData?.get(0)?.countText.orEmpty()
        binding.countTwo.text = data.courseData?.get(1)?.countText.orEmpty()
        binding.countThree.text = data.courseData?.get(2)?.countText.orEmpty()

        binding.textOne.text = data.courseData?.get(0)?.benefitText.orEmpty()
        binding.textTwo.text = data.courseData?.get(1)?.benefitText.orEmpty()
        binding.textThree.text = data.courseData?.get(2)?.benefitText.orEmpty()

        binding.titleTv.text = data.title.orEmpty()

        val snapHelper = PagerSnapHelper()
        binding.rvTestimonial.onFlingListener = null
        snapHelper.attachToRecyclerView(binding.rvTestimonial)

        if (data.items.isNullOrEmpty() || data.items.size == 1) {
            binding.circleIndicator.hide()
        } else {
            binding.circleIndicator.show()
        }

        binding.rvTestimonial.layoutManager = LinearLayoutManager(
            context,
            RecyclerView.HORIZONTAL, false
        )
        binding.rvTestimonial.adapter = Adapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher,
            deeplinkAction,
            context,
            model.extraParams
        )

        binding.circleIndicator.attachToRecyclerView(
            binding.rvTestimonial
        )
        return holder
    }

    class Adapter(
        val items: List<TestimonialWidgetItemV2>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val context: Context,
        val extraParams: HashMap<String, Any>?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemTestimonialV2Binding
                    .inflate(LayoutInflater.from(context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val binding = holder.binding as ItemTestimonialV2Binding
            binding.ivTestimonial.loadImageEtx(item.imageUrl.orEmpty())
            binding.testimonialCard.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.TESTIMONIAL_WIDGET_CLICKED,
                        hashMapOf<String, Any>().apply {
                            putAll(extraParams ?: hashMapOf())
                        }
                    )
                )
                deeplinkAction.performAction(holder.itemView.context, item.deeplink.orEmpty())
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)
    }

    class WidgetViewHolder(binding: WidgetTestimonialV2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTestimonialV2Binding>(binding, widget)
}

class TestimonialWidgetModelV2 : WidgetEntityModel<TestimonialWidgetDataV2, WidgetAction>()

@Keep
data class TestimonialWidgetDataV2(
    @SerializedName("items") val items: List<TestimonialWidgetItemV2>?,
    @SerializedName("course_data") val courseData: List<CourseData>?,
    @SerializedName("title") val title: String?
) : WidgetData()

@Keep
data class CourseData(
    @SerializedName("count_text") val countText: String?,
    @SerializedName("benefit_text") val benefitText: String?
)

@Keep
data class TestimonialWidgetItemV2(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("deeplink") val deeplink: String?
)
