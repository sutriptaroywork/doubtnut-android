package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.newlibrary.model.ApiVideoObj
import com.doubtnutapp.databinding.WidgetTopDoubtAnswerVideoBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.model.Video
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.videoPage.model.VideoResource
import com.doubtnutapp.videoPage.ui.FullScreenVideoPageActivity
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TopDoubtAnswerVideoWidget(context: Context) :
    BaseBindingWidget<TopDoubtAnswerVideoWidget.WidgetHolder,
        TopDoubtAnswerVideoWidgetModel, WidgetTopDoubtAnswerVideoBinding>(context) {

    companion object {
        const val TAG = "TopDoubtAnswerVideoWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetTopDoubtAnswerVideoBinding {
        return WidgetTopDoubtAnswerVideoBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: TopDoubtAnswerVideoWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: TopDoubtAnswerVideoWidgetData = model.data
        binding.tvAnswer.text = "Answer " + (holder.adapterPosition + 1) + "."
        binding.tvTitle.text = data.title.orEmpty()
        binding.ivViewSolution.loadImageEtx(data.resourceUrl.orEmpty())
        binding.ivViewSolution.setOnClickListener {
            val videoObj = data.videoObj ?: return@setOnClickListener
            val video = Video(
                videoObj.questionId,
                true,
                videoObj.viewId,
                videoObj.resources?.map {
                    VideoResource(
                        resource = it.resource,
                        drmScheme = it.drmScheme,
                        drmLicenseUrl = it.drmLicenseUrl,
                        mediaType = it.mediaType,
                        isPlayed = false,
                        dropDownList = null,
                        timeShiftResource = null,
                        offset = it.offset
                    )
                },
                0,
                videoObj.page,
                false,
                videoObj.aspectRatio ?: VideoFragment.DEFAULT_ASPECT_RATIO
            )
            FullScreenVideoPageActivity.startActivity(
                widgetViewHolder.itemView.context,
                video,
                true
            )
                .apply {
                    widgetViewHolder.itemView.context.startActivity(this)
                }

            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.WIDGET_VIEW_SOLUTION_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET to TAG
                    ).apply {
                        putAll(model.extraParams ?: hashMapOf())
                    },
                    ignoreSnowplow = true
                )
            )
        }
        return holder
    }

    class WidgetHolder(binding: WidgetTopDoubtAnswerVideoBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTopDoubtAnswerVideoBinding>(binding, widget)
}

class TopDoubtAnswerVideoWidgetModel :
    WidgetEntityModel<TopDoubtAnswerVideoWidgetData, WidgetAction>()

@Keep
data class TopDoubtAnswerVideoWidgetData(
    @SerializedName("message") val title: String?,
    @SerializedName("resource_url") val resourceUrl: String?,
    @SerializedName("video_obj") val videoObj: ApiVideoObj?,
) : WidgetData()
