package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.isNotNullAndNotEmpty2

import com.doubtnutapp.databinding.ItemTestimonialBinding
import com.doubtnutapp.databinding.WidgetTestimonialBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.DebouncedOnClickListener
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TestimonialWidget(context: Context) : BaseBindingWidget<TestimonialWidget.WidgetHolder,
    TestimonialWidgetModel, WidgetTestimonialBinding>(context) {

    companion object {
        const val TAG = "TestimonialWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetTestimonialBinding {
        return WidgetTestimonialBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: TestimonialWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data: TestimonialWidgetData = model.data
        val binding = holder.binding

        val snapHelper = PagerSnapHelper()
        binding.rvTestimonial.onFlingListener = null
        snapHelper.attachToRecyclerView(binding.rvTestimonial)

        binding.titleTv.text = data.title.orEmpty()
        binding.tvSubtitle.text = data.subtitle.orEmpty()
        binding.tvSubtitle.isVisible = data.subtitle.isNotNullAndNotEmpty2()

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
            deeplinkAction
        )
        binding.rvTestimonial.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val offset: Int = recyclerView.computeHorizontalScrollOffset()
                if (offset % recyclerView.getChildAt(0).measuredWidth == 0) {
                    val position: Int = offset / recyclerView.getChildAt(0).measuredWidth
                    if (position > 0) {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.REVIEW_CAROUSAL_SWIPE,
                                hashMapOf<String, Any>().apply {
                                    put(EventConstants.ITEM_POSITION, position - 1)
                                    putAll(
                                        model.data.items?.get(position - 1)?.extraParams
                                            ?: hashMapOf()
                                    )
                                },
                                ignoreSnowplow = true
                            )
                        )
                    }
                }
            }
        })
        binding.circleIndicator.attachToRecyclerView(
            binding.rvTestimonial
        )
        return holder
    }

    class Adapter(
        val items: List<TestimonialWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_testimonial, parent,
                    false
                )
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val data = items[position]
            val binding = holder.binding
            binding.imageView.loadImageEtx(data.imageUrl.orEmpty())
            binding.textViewName.text = data.name.orEmpty()
            binding.textViewDescription.text = data.description.orEmpty()
            holder.itemView.setOnClickListener(object : DebouncedOnClickListener(1000) {
                override fun onDebouncedClick(v: View?) {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.REVIEW_CAROUSAL_VIDEO_CLICK,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.ITEM_POSITION, position)
                                putAll(
                                    data.extraParams
                                        ?: hashMapOf()
                                )
                            },
                            ignoreSnowplow = true
                        )
                    )
                    deeplinkAction.performAction(holder.itemView.context, data.deeplink.orEmpty())
                }
            })
            val rating = data.rating?.toFloatOrNull()
            if (rating == null) {
                binding.ratingBar.hide()
            } else {
                binding.ratingBar.show()
                binding.ratingBar.rating = rating
            }
            if (data.deeplink.isNullOrEmpty()) {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.REVIEW_CAROUSAL_VIDEO_CLICK,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.CLICKED_ITEM_ID, data.id.orEmpty())
                            putAll(data.extraParams ?: hashMapOf())
                        },
                        ignoreSnowplow = true
                    )
                )
                binding.playCourse.hide()
            } else {
                binding.playCourse.show()
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemTestimonialBinding.bind(itemView)
        }
    }

    class WidgetHolder(binding: WidgetTestimonialBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTestimonialBinding>(binding, widget)
}

class TestimonialWidgetModel : WidgetEntityModel<TestimonialWidgetData, WidgetAction>()

@Keep
data class TestimonialWidgetData(
    @SerializedName("items") val items: List<TestimonialWidgetItem>?,
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?
) : WidgetData()

@Keep
data class TestimonialWidgetItem(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("imageUrl", alternate = ["image_url"]) val imageUrl: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("rating") val rating: String?,
    @SerializedName("extra_params") val extraParams: HashMap<String, Any>?
)
