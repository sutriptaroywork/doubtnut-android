package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetCourseInfoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseInfoWidget(context: Context) : BaseBindingWidget<CourseInfoWidget.WidgetHolder,
    CourseInfoWidget.CourseInfoWidgetModel, WidgetCourseInfoBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        private const val TAG = "CourseInfoWidget"
    }

    override fun getViewBinding(): WidgetCourseInfoBinding {
        return WidgetCourseInfoBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseInfoWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding
        binding.titleTv.text = data.title.orEmpty()
        binding.descriptionTv.text = data.description.orEmpty()
        binding.link.text = model.data.link.orEmpty()
        binding.buttonDemoVideo.text = data.demoButton?.text
        binding.buttonDemoVideo.setTextColor(Utils.parseColor(data.demoButton?.textColor))
        binding.buttonTimeTable.setTextColor(Utils.parseColor(data.timetableButton?.textColor))
        binding.buttonTimeTable.text = data.timetableButton?.text
        binding.demoVideoImage.loadImageEtx(data.demoButton?.icon.orEmpty())
        binding.timeTableImage.loadImageEtx(data.timetableButton?.icon.orEmpty())
        binding.link.setVisibleState(!data.link.isNullOrEmpty())
        binding.layoutDemoVideo.setVisibleState(data.demoButton != null)
        binding.layoutTimeTable.setVisibleState(data.timetableButton != null)
        binding.layoutTimeTable.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    TAG + EventConstants.EVENT_ITEM_CLICK + "TIMETABLE",
                    hashMapOf<String, Any>(
                        "deeplink" to data.timetableButton?.deeplink.orEmpty()
                    ).apply {
                        putAll(model.extraParams ?: HashMap())
                    },
                    ignoreSnowplow = true
                )
            )
            deeplinkAction.performAction(context, data.timetableButton?.deeplink)
        }
        binding.layoutDemoVideo.setOnClickListener {
            playDemoVideo(context, data.demoButton, model.extraParams ?: HashMap())
        }
        if (data.timetableButton == null && data.demoButton != null) {
            binding.separator.hide()
        }

        binding.layoutDemoVideo.background = Utils.getShape(
            "#ffffff",
            model.data.demoButton?.textColor.toString(),
            8f,
            1
        )
        return holder
    }

    private fun playDemoVideo(
        context: Context,
        data: CourseInfoWidgetData.DemoButton?,
        extraParams: HashMap<String, Any>
    ) {
        if (data == null) {
            showToast(context, context.getString(R.string.api_error))
            return
        }
        when (data.playerType.orEmpty()) {
            "video" -> {
                openVideoPage(context, data.id, data.page)
            }
            else -> {
                showToast(context, context.getString(R.string.api_error))
            }
        }

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                TAG + EventConstants.EVENT_ITEM_CLICK + "DEMO_VIDEO",
                hashMapOf<String, Any>(
                    EventConstants.EVENT_NAME_ID to data.id.orEmpty(),
                    EventConstants.PAGE to data.page.orEmpty(),
                    EventConstants.PLAYER_TYPE to data.playerType.orEmpty()
                ).apply {
                    putAll(extraParams)
                },
                ignoreSnowplow = true
            )
        )
    }

    private fun openVideoPage(context: Context, id: String?, page: String?) {
        var pageSource = page
        if (Constants.PAGE_SEARCH_SRP == source) {
            pageSource = source
        }
        context.startActivity(
            VideoPageActivity.startActivity(
                context = context,
                questionId = id.orEmpty(),
                page = pageSource.orEmpty()
            )
        )
    }

    class WidgetHolder(binding: WidgetCourseInfoBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseInfoBinding>(binding, widget)

    class CourseInfoWidgetModel : WidgetEntityModel<CourseInfoWidgetData, WidgetAction>()

    @Keep
    data class CourseInfoWidgetData(
        @SerializedName("description") val description: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("link") val link: String?,
        @SerializedName("demo_button") val demoButton: DemoButton?,
        @SerializedName("timetable_button") val timetableButton: TimeTableButton?

    ) : WidgetData() {
        @Keep
        data class DemoButton(
            @SerializedName("text") val text: String?,
            @SerializedName("player_type") val playerType: String?,
            @SerializedName("page") val page: String?,
            @SerializedName("id") val id: String?,
            @SerializedName("text_color") val textColor: String?,
            @SerializedName("icon") val icon: String?
        )

        @Keep
        data class TimeTableButton(
            @SerializedName("text") val text: String?,
            @SerializedName("icon") val icon: String?,
            @SerializedName("text_color") val textColor: String?,
            @SerializedName("bg_color") val bgColor: String?,
            @SerializedName("deeplink") val deeplink: String?
        )
    }
}
