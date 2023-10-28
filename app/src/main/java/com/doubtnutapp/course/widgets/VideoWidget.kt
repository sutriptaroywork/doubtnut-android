package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetVideoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class VideoWidget(context: Context) : BaseBindingWidget<VideoWidget.WidgetHolder,
    VideoWidget.VideoWidgetModel, WidgetVideoBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun getViewBinding(): WidgetVideoBinding {
        return WidgetVideoBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: VideoWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: VideoWidgetData = model.data
        with(binding) {
            tvTitle.text = data.title.orEmpty()
            tvTitle.textSize = data.titleSize?.toFloat() ?: 48f
            tvTitle.setTextColor(Utils.parseColor(data.titleColor))
            ivTeacher.loadImageEtx(data.facultyImageUrl.orEmpty())
            imageView.loadImageEtx(data.bgImageUrl.orEmpty())
            binding.root.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.VIDEO_WIDGET_CLICKED,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: hashMapOf())
                        },
                        ignoreSnowplow = true
                    )
                )
                deeplinkAction.performAction(holder.itemView.context, data.deeplink.orEmpty())
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetVideoBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetVideoBinding>(binding, widget)

    class VideoWidgetModel : WidgetEntityModel<VideoWidgetData, WidgetAction>()

    @Keep
    data class VideoWidgetData(
        @SerializedName("title") val title: String?,
        @SerializedName("title_size") val titleSize: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("faculty_image_url") val facultyImageUrl: String?,
        @SerializedName("bg_image_url") val bgImageUrl: String?
    ) : WidgetData()
}
