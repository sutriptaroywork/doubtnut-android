package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.WidgetD0AskQuestionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class D0QaWidget(context: Context) :
    BaseBindingWidget<D0QaWidget.WidgetHolder,
            D0QaWidget.Model, WidgetD0AskQuestionBinding>(context) {

    companion object {
        const val TAG = "D0QaWidget"
        const val EVENT_TAG = "qa_progress_widget"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var day0UserTimer: CountDownTimer? = null

    var source: String? = null

    override fun getViewBinding(): WidgetD0AskQuestionBinding {
        return WidgetD0AskQuestionBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun onDetachedFromWindow() {
        day0UserTimer?.cancel()
        super.onDetachedFromWindow()
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        super.bindWidget(holder, model)
        val data: D0QaWidget.Data = model.data
        holder.binding.apply {

            rootLayout.applyBackgroundColor(data.backgroundColor)

            tvTimer.apply {
                data.expirationTime?.let { expirationTime ->
                    visible()
                    day0UserTimer?.cancel()
                    startTimer(data)
                    applyTextColor(data.expirationTimeColor)
                    applyTextSize(data.expirationTimeSize)
                } ?: gone()
            }

            tvTitle.apply {
                data.title?.let {
                    visible()
                    TextViewUtils.setTextFromHtml(tvTitle, it)
                    applyTextColor(data.titleColor)
                    applyTextSize(data.titleSize)
                } ?: gone()
            }

            tvSubtitle.apply {
                data.subtitle?.let {
                    visible()
                    TextViewUtils.setTextFromHtml(tvSubtitle, it)
                    applyTextColor(data.subtitleColor)
                    applyTextSize(data.subtitleSize)
                } ?: gone()
            }

            qaProgress.apply {
                if (data.qaProgressBackgroundColor.isNotNullAndNotEmpty2() && data.qaProgressColor.isNotNullAndNotEmpty2()) {
                    visible()
                    val askedQuestion: Float = data.qaCount ?: 0F
                    val totalQuestion: Float = data.qaTotal ?: 5F
                    if (totalQuestion != 0F) { // must not be 0 to avoid ArithmeticException
                        val percentage = ((askedQuestion * 100) / totalQuestion)
                        progress = percentage
                    }
                    backgroundProgressBarColor =
                        Color.parseColor(data.qaProgressBackgroundColor ?: "#ffffff")
                    progressBarColor = Color.parseColor(data.qaProgressColor ?: "#ffc700")
                } else {
                    gone()
                }
            }

            qaRatio.apply {
                if (data.qaCount != null && data.qaTotal != null && data.qaTotal != 0F) {
                    visible()
                    text = String.format(
                        context.getString(R.string.d0_user_qa_ratio),
                        data.qaCount.toInt(),
                        data.qaTotal.toInt()
                    )
                    applyTextColor(data.qaRatioColor)
                    applyTextSize(data.qaRatioSize)
                } else {
                    gone()
                }
            }

            divider.apply {
                when (data.showDivider) {
                    true -> {
                        visible()
                        applyBackgroundColor(data.dividerColor)
                    }
                    else -> {
                        gone()
                    }
                }
            }

            root.setOnClickListener {
                deeplinkAction.performAction(context, data.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        name = "${EVENT_TAG}_${EventConstants.CLICKED}",
                        params = hashMapOf<String, Any>().apply {
                            put(EventConstants.SOURCE, source.orEmpty())
                        }.apply {
                            putAll(model.extraParams.orEmpty())
                        }
                    )
                )
            }

            trackingViewId = data.id.orEmpty()
        }
        return holder
    }

    private fun startTimer(data: D0QaWidget.Data) {
        val expirationTime = data.expirationTime ?: return
        val remainingTime = expirationTime - System.currentTimeMillis()
        day0UserTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                        TimeUnit.HOURS.toMinutes(hours)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours)
                val remaining = "${hours}h : ${minutes}m : ${seconds}s"
                widgetViewHolder.binding.tvTimer.text = remaining
            }

            override fun onFinish() {
                widgetViewHolder.binding.tvTimer.text =
                    widgetViewHolder.binding.root.context.getString(R.string.d0_user_end_time)
            }
        }
        day0UserTimer?.start()
    }

    class WidgetHolder(binding: WidgetD0AskQuestionBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetD0AskQuestionBinding>(binding, widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("background_color") val backgroundColor: String?,
        @SerializedName("expiration_time") val expirationTime: Long?,
        @SerializedName("expiration_time_color") val expirationTimeColor: String?,
        @SerializedName("expiration_time_size") val expirationTimeSize: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleColor: String?,
        @SerializedName("title_size") val titleSize: String?,
        @SerializedName("subtitle") val subtitle: String?,
        @SerializedName("subtitle_color") val subtitleColor: String?,
        @SerializedName("subtitle_size") val subtitleSize: String?,
        @SerializedName("qa_count") val qaCount: Float?,
        @SerializedName("qa_total") val qaTotal: Float?,
        @SerializedName("qa_ratio_color") val qaRatioColor: String?,
        @SerializedName("qa_ratio_size") val qaRatioSize: String?,
        @SerializedName("qa_progress_background_color") val qaProgressBackgroundColor: String?,
        @SerializedName("qa_progress_color") val qaProgressColor: String?,
        @SerializedName("show_divider") val showDivider: Boolean?,
        @SerializedName("divider_color") val dividerColor: String?,
        @SerializedName("deeplink") val deeplink: String?,
    ) : WidgetData()
}
