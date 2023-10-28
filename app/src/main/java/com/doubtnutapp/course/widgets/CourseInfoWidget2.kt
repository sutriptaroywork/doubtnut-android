package com.doubtnutapp.course.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.addRipple
import com.doubtnutapp.databinding.WidgetCourseInfoWidget2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseInfoWidget2 @JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : BaseBindingWidget<CourseInfoWidget2.WidgetHolder, CourseInfoWidget2Model,
    WidgetCourseInfoWidget2Binding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetCourseInfoWidget2Binding {
        return WidgetCourseInfoWidget2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: CourseInfoWidget2Model
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.tvTitleOne.text = model.data.titleOne
        binding.tvSubtitleOne.text = model.data.subtitleOne
        binding.ivTitleOne.isVisible = model.data.showIconOne == true

        binding.viewDividerOne.isVisible = model.data.titleTwo.isNullOrEmpty().not()

        binding.tvTitleTwo.text = model.data.titleTwo
        binding.tvSubtitleTwo.text = model.data.subtitleTwo
        binding.ivTitleTwo.isVisible = model.data.showIconTwo == true

        binding.viewDividerTwo.isVisible = model.data.titleThree.isNullOrEmpty().not()

        binding.tvTitleThree.text = model.data.titleThree
        binding.tvSubtitleThree.text = model.data.subtitleThree
        binding.ivTitleThree.isVisible = model.data.showIconThree == true

        if (model.data.dlOne.isNullOrEmpty()) {
            binding.viewClickOne.background = null
        } else {
            binding.viewClickOne.addRipple()
        }

        if (model.data.dlTwo.isNullOrEmpty()) {
            binding.viewClickTwo.background = null
        } else {
            binding.viewClickTwo.addRipple()
        }

        if (model.data.dlThree.isNullOrEmpty()) {
            binding.viewClickThree.background = null
        } else {
            binding.viewClickThree.addRipple()
        }

        binding.viewClickOne.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.NCP_COURSE_DURATION_TAPPED,
                    model.extraParams ?: hashMapOf()
                )
            )
            performAction(context, model.data.dlOne)
        }

        binding.viewClickTwo.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.NCP_COURSE_MEDIUM_TAPPED,
                    model.extraParams ?: hashMapOf()
                )
            )
            performAction(context, model.data.dlTwo)
        }

        binding.viewClickThree.setOnClickListener {
            performAction(context, model.data.dlThree)
        }

        return holder
    }

    fun performAction(context: Context, deepLink: String?) {
        deeplinkAction.performAction(context, deepLink)
    }

    class WidgetHolder(binding: WidgetCourseInfoWidget2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseInfoWidget2Binding>(binding, widget)
}

@Keep
class CourseInfoWidget2Model :
    WidgetEntityModel<CourseInfoWidget2Data, WidgetAction>()

@Keep
data class CourseInfoWidget2Data(
    @SerializedName("title_one")
    val titleOne: String?,
    @SerializedName("subtitle_one")
    val subtitleOne: String?,
    @SerializedName("show_icon_one")
    val showIconOne: Boolean?,
    @SerializedName("deeplink_one")
    val dlOne: String?,

    @SerializedName("title_two")
    val titleTwo: String?,
    @SerializedName("subtitle_two")
    val subtitleTwo: String?,
    @SerializedName("show_icon_two")
    val showIconTwo: Boolean?,
    @SerializedName("deeplink_two")
    val dlTwo: String?,

    @SerializedName("title_three")
    val titleThree: String?,
    @SerializedName("subtitle_three")
    val subtitleThree: String?,
    @SerializedName("show_icon_three")
    val showIconThree: Boolean?,
    @SerializedName("deeplink_three")
    val dlThree: String?

) : WidgetData()
