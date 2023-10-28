package com.doubtnutapp.course.widgets

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.DateTimeUtils
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetTrialTimerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.forceUnWrap
import com.doubtnutapp.hide
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TrialTimerWidget(context: Context) : BaseBindingWidget<TrialTimerWidget.WidgetHolder,
    TrialTimerWidget.TrialTimerModel, WidgetTrialTimerBinding>(context) {

    companion object {
        const val TAG = "TimerTrialWidget"
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    var source: String? = null

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent.forceUnWrap().inject(this)
    }

    override fun getViewBinding(): WidgetTrialTimerBinding {
        return WidgetTrialTimerBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: TrialTimerModel): WidgetHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data: TrialTimerData = model.data
        binding.tvTitle.text = data.title.orEmpty()
        binding.btnPayNow.text = data.buttonText.orEmpty()
        binding.btnPayNow.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.COURSE_TRIAL_BUY_NOW_CLICK,
                    hashMapOf<String, Any>().apply {
                        putAll(model.extraParams ?: hashMapOf())
                    }
                )
            )
            deeplinkAction.performAction(holder.itemView.context, data.deeplink)
        }
        val actualTimeLeft = (data.endTime?.or(0)?.minus((System.currentTimeMillis()))) ?: 0
        if (actualTimeLeft > 0) {
            binding.tvTimer.visibility = VISIBLE
            binding.ivTime.visibility = VISIBLE
            val timer = object : CountDownTimer(
                actualTimeLeft,
                1000
            ) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.tvTimer.text = DateTimeUtils.formatMilliSecondsToTime(millisUntilFinished)
                }

                override fun onFinish() {
                    binding.btnPayNow.hide()
                }
            }
            timer.start()
        } else {
            binding.tvTimer.visibility = GONE
            binding.ivTime.visibility = GONE
        }
        return holder
    }

    class WidgetHolder(binding: WidgetTrialTimerBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetTrialTimerBinding>(binding, widget)

    class TrialTimerModel : WidgetEntityModel<TrialTimerData, WidgetAction>()

    @Keep
    data class TrialTimerData(
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("end_time") val endTime: Long?,
        @SerializedName("title") val title: String?,
        @SerializedName("button_text") val buttonText: String?,
        var responseAtTimeInMillis: Long?,
    ) : WidgetData()
}
