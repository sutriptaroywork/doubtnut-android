package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.ItemCourseSubjectBinding
import com.doubtnutapp.databinding.WidgetCourseSubjectBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.loadBackgroundImage
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseSubjectWidget(context: Context) : BaseBindingWidget<CourseSubjectWidget.WidgetHolder,
    CourseSubjectWidget.Model, WidgetCourseSubjectBinding>(context) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    var source: String? = null

    companion object {
        const val TAG = "CourseSubjectWidget"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetCourseSubjectBinding {
        return WidgetCourseSubjectBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(0, 0, 0, 0)
            }
        )
        val data: Data = model.data
        val binding = holder.binding
        binding.textViewTitleMain.text = data.title.orEmpty()

        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)

        val adapter = Adapter(
            data.items, analyticsPublisher, deeplinkAction,
            model.extraParams ?: HashMap(), data.title.orEmpty(),
            source
        )

        binding.recyclerView.adapter = adapter

        return holder
    }

    class Adapter(
        val items: List<Item>,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>,
        val parentTitle: String,
        val source: String?
    ) : RecyclerView.Adapter<Adapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                ItemCourseSubjectBinding
                    .inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val binding = holder.binding
            binding.cardContainer.loadBackgroundImage(item.bgImage, R.color.grey_808080)
            binding.textViewSubject.text = item.displayName.orEmpty()
            binding.textViewSubject.textSize = item.textFontSize?.toFloat() ?: 16f
            binding.textViewSubject.setTextColor(Utils.parseColor(item.displayNameColor))
            binding.root.setOnClickListener {
                deeplinkAction.performAction(
                    binding.root.context,
                    item.deeplink,
                    source.orEmpty()
                )
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.LC_COURSE_SUBJECT_CARD_CLICK,
                        hashMapOf<String, Any>(
                            EventConstants.SUBJECT to item.displayName.orEmpty()
                        ).apply {
                            putAll(extraParams)
                        }
                    )
                )
            }
        }

        override fun getItemCount(): Int = items.size

        class ViewHolder(val binding: ItemCourseSubjectBinding) :
            RecyclerView.ViewHolder(binding.root)
    }

    class WidgetHolder(binding: WidgetCourseSubjectBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseSubjectBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("items") val items: List<Item>,
    ) : WidgetData()

    @Keep
    data class Item(
        @SerializedName("bg_image") val bgImage: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("display_name") val displayName: String?,
        @SerializedName("display_name_color") val displayNameColor: String?,
        @SerializedName("text_font_size") val textFontSize: String?,
    )
}
