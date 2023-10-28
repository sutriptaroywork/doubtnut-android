package com.doubtnutapp.course.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetCourseExploreBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.loadImageEtx
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import java.util.*
import javax.inject.Inject

class CourseExploreWidget(context: Context) :
    BaseBindingWidget<CourseExploreWidget.WidgetHolder,
        CourseExploreWidget.Model, WidgetCourseExploreBinding>(context) {

    companion object {
        const val TAG = "CourseExploreWidget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    override fun getViewBinding(): WidgetCourseExploreBinding {
        return WidgetCourseExploreBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(2, 2, 4, 4)
            }
        )
        val data = model.data
        val binding = holder.binding
        binding.apply {
            tvTitle.text = data.title.orEmpty()
            imageView.loadImageEtx(data.courseDetails?.imageUrl.orEmpty())
            tvRightTitle.text = data.courseDetails?.rightTitle.orEmpty()
            tvLeftTitle.text = data.courseDetails?.leftTitle.orEmpty()
            tvSecondaryTitle.text = data.courseDetails?.secondaryTitle.orEmpty()
            tvNudgeTitle.text = data.nudgeCtaText.orEmpty()
            btnExplore.text = data.buttonTitle.orEmpty()
            btnExplore.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        TAG.toLowerCase(Locale.ROOT) + EventConstants.UNDERSCORE +
                            EventConstants.EVENT_ITEM_CLICK + EventConstants.UNDERSCORE + EventConstants.BUTTON,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: HashMap())
                        }
                    )
                )
                deeplinkAction.performAction(context, data.buttonDeeplink)
            }

            imageView.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        TAG.toLowerCase(Locale.ROOT) + EventConstants.UNDERSCORE + EventConstants.EVENT_ITEM_CLICK +
                            EventConstants.UNDERSCORE + EventConstants.IMAGE,
                        hashMapOf<String, Any>().apply {
                            putAll(model.extraParams ?: HashMap())
                        },
                        ignoreSnowplow = true
                    )
                )
                deeplinkAction.performAction(context, data.courseDetails?.deeplink)
            }
        }
        return holder
    }

    class WidgetHolder(binding: WidgetCourseExploreBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseExploreBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("title") val title: String?,
        @SerializedName("button_title") val buttonTitle: String?,
        @SerializedName("button_deeplink") val buttonDeeplink: String?,
        @SerializedName("nudge_cta_text") val nudgeCtaText: String?,
        @SerializedName("course_details") val courseDetails: CourseDetailData?
    ) : WidgetData()

    @Keep
    data class CourseDetailData(
        @SerializedName("image_url") val imageUrl: String?,
        @SerializedName("left_title") val leftTitle: String?,
        @SerializedName("secondary_title") val secondaryTitle: String?,
        @SerializedName("right_title") val rightTitle: String?,
        @SerializedName("deeplink") val deeplink: String?,
    )
}
