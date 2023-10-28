package com.doubtnut.scholarship.widget

import android.content.Context
import android.content.res.ColorStateList
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.scholarship.R
import com.doubtnut.scholarship.databinding.WidgetScholarshipProgressCardBinding
import com.doubtnut.scholarship.event.ChangeTest
import com.google.android.material.color.MaterialColors
import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ScholarshipProgressCardWidget(
    context: Context
) : CoreBindingWidget<ScholarshipProgressCardWidget.WidgetHolder, ScholarshipProgressCardWidgetModel, WidgetScholarshipProgressCardBinding>(
    context
) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    private var countDownTimer: CountDownTimer? = null

    override fun getViewBinding(): WidgetScholarshipProgressCardBinding {
        return WidgetScholarshipProgressCardBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        CoreApplication.INSTANCE.androidInjector().inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: ScholarshipProgressCardWidgetModel
    ): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding

        binding.root.applyBackgroundColor(model.data.bgColor)
        if (model.data.deeplink.isNullOrEmpty()) {
            binding.root.applyRippleColor("#00000000")
        } else {
            binding.root.rippleColor = ColorStateList.valueOf(
                MaterialColors.getColor(binding.root, R.attr.colorControlHighlight)
            )
        }
        binding.root.setOnClickListener {
            deeplinkAction.performAction(context, model.data.deeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.WIDGET_TITLE to model.data.title.orEmpty(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.STUDENT_ID to CoreUserUtils.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        binding.tvTitleOne.text = model.data.title
        binding.tvTitleOne.applyTextColor(model.data.titleTextColor)
        binding.tvTitleOne.applyTextSize(model.data.titleTextSize)

        binding.viewDivider.isVisible = model.data.showDivider == true
        binding.viewDivider.applyBackgroundColor(model.data.dividerColor)

        binding.tvTitleTwo.text = model.data.title2
        binding.tvTitleTwo.applyTextColor(model.data.title2TextColor)
        binding.tvTitleTwo.applyTextSize(model.data.title2TextSize)

        // Change Test
        binding.btnTitleThree.text = model.data.title3
        binding.btnTitleThree.applyTextColor(model.data.title3TextColor)
        binding.btnTitleThree.applyTextSize(model.data.title3TextSize)
        binding.btnTitleThree.isVisible = model.data.title3.isNullOrEmpty().not()
        binding.btnTitleThree.setOnClickListener {
            if (model.data.changeTest == true) {
                actionPerformer?.performAction(ChangeTest())
            } else {
                deeplinkAction.performAction(context, model.data.title3TextDeeplink)
            }
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.CTA_TEXT to model.data.title3.orEmpty(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.STUDENT_ID to CoreUserUtils.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        // Start Test
        binding.btnTitleFour.text = model.data.title4
        binding.btnTitleFour.applyTextColor(model.data.title4TextColor)
        binding.btnTitleFour.applyTextSize(model.data.title4TextSize)
        binding.btnTitleFour.applyBackgroundColor(model.data.title4TextBgColor)
        binding.btnTitleFour.isVisible = model.data.title4.isNullOrEmpty().not()
        binding.btnTitleFour.setOnClickListener {
            deeplinkAction.performAction(context, model.data.title4TextDeeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                    hashMapOf<String, Any>(
                        EventConstants.CTA_TEXT to model.data.title4.orEmpty(),
                        EventConstants.WIDGET to TAG,
                        EventConstants.STUDENT_ID to CoreUserUtils.getStudentId()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        val showTimer = model.data.startTimeInMillis != null
                && (model.data.startTimeInMillis ?: 0L) - (model.data.counter ?: 0L) > 0L

        binding.clDays.isVisible = showTimer
        binding.clHours.isVisible = showTimer
        binding.clMin.isVisible = showTimer
        binding.clSec.isVisible = showTimer

        countDownTimer?.cancel()
        if (showTimer) {
            countDownTimer = object : CountDownTimer(
                (model.data.startTimeInMillis ?: 0L) - (model.data.counter ?: 0L),
                1000
            ) {
                override fun onTick(millisUntilFinished: Long) {
                    model.data.counter = (model.data.counter ?: 0L) + 1000L

                    binding.tvDays.text =
                        TimeUnit.MILLISECONDS.toDays(millisUntilFinished).toString()
                    binding.tvHours.text = (
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished) - TimeUnit.DAYS.toHours(
                                TimeUnit.MILLISECONDS.toDays(millisUntilFinished)
                            )
                            ).toString()
                    binding.tvMin.text =
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) - TimeUnit.HOURS.toMinutes(
                            TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                        )).toString()
                    binding.tvSec.text =
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(
                            TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        )).toString()
                }

                override fun onFinish() {
                }
            }
            countDownTimer?.start()
        }

        return holder
    }

    class WidgetHolder(binding: WidgetScholarshipProgressCardBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetScholarshipProgressCardBinding>(binding, widget)

    companion object {
        const val TAG = "ScholarshipProgressCardWidget"
        const val EVENT_TAG = "scholarship_progress_card_widget"
    }
}

@Keep
class ScholarshipProgressCardWidgetModel :
    WidgetEntityModel<ScholarshipProgressCardWidgetData, WidgetAction>()

@Keep
data class ScholarshipProgressCardWidgetData(
    @SerializedName("bg_color")
    val bgColor: String?,
    @SerializedName("deeplink")
    val deeplink: String?,

    @SerializedName("change_test")
    val changeTest: Boolean?,

    @SerializedName("show_divider")
    val showDivider: Boolean?,
    @SerializedName("divider_color")
    val dividerColor: String?,

    @SerializedName("start_time_in_millis")
    val startTimeInMillis: Long?,
    var counter: Long?,

    @SerializedName("title")
    val title: String?,
    @SerializedName("title_text_color")
    val titleTextColor: String?,
    @SerializedName("title_text_size")
    val titleTextSize: String?,

    @SerializedName("title2")
    val title2: String?,
    @SerializedName("title2_text_color")
    val title2TextColor: String?,
    @SerializedName("title2_text_size")
    val title2TextSize: String?,

    @SerializedName("title3")
    val title3: String?,
    @SerializedName("title3_text_color")
    val title3TextColor: String?,
    @SerializedName("title3_text_size")
    val title3TextSize: String?,
    @SerializedName("title3_text_deeplink")
    val title3TextDeeplink: String?,

    @SerializedName("title4")
    val title4: String?,
    @SerializedName("title4_text_bg_color")
    val title4TextBgColor: String?,
    @SerializedName("title4_text_color")
    val title4TextColor: String?,
    @SerializedName("title4_text_deeplink")
    val title4TextDeeplink: String?,
    @SerializedName("title4_text_size")
    val title4TextSize: String?,

    @SerializedName("extra_params")
    var extraParams: HashMap<String, Any>? = null
) : WidgetData()

