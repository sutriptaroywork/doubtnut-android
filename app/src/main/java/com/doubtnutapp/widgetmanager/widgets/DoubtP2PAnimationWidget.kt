package com.doubtnutapp.widgetmanager.widgets

import android.content.Context
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.databinding.WidgetDoubtP2pAnimationBinding
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.hide
import com.google.gson.annotations.SerializedName
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class DoubtP2PAnimationWidget(context: Context): BaseBindingWidget<
        DoubtP2PAnimationWidget.WidgetHolder,
        DoubtP2PAnimationWidget.Model,WidgetDoubtP2pAnimationBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var compositeDisposable: CompositeDisposable

    private lateinit var countDownTimer: CountDownTimer

    private var totalTimerDuration: Long? = null
    private var counter: Long? = 0L

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetDoubtP2pAnimationBinding =
            WidgetDoubtP2pAnimationBinding.inflate(LayoutInflater.from(context),this,true)

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(),this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        countDownTimer?.cancel()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        totalTimerDuration?.minus(counter?:0L)?.let { setUpTimer(it) }
    }

    override fun bindWidget(holder: WidgetHolder, model: Model): WidgetHolder {
        val data = model.data
        val binding = holder.binding
        binding.apply {
            if (data.animationFileName.isEmpty().not()) {
                doubtP2PAnimation.setAnimation(data.animationFileName)
                doubtP2PAnimation.playAnimation()
            }
            tvAlertMessage.text = data.text
            totalTimerDuration = data.totalTimerDuration
        }

        return holder
    }

    private fun setUpTimer(timerDuration: Long) {
        countDownTimer = object : CountDownTimer(timerDuration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counter = counter?.plus(1000L)
                widgetViewHolder.binding.tvTimer.text = String.format(context.getString(R.string.p2p_animation_timer_text), millisUntilFinished/1000)
            }

            override fun onFinish() {
                widgetViewHolder.binding.tvAlertMessage.text = context.getString(R.string.no_helper_alert_message_2)
                widgetViewHolder.binding.tvTimer.hide()
            }
        }
        countDownTimer.start()
    }

    class WidgetHolder(binding: WidgetDoubtP2pAnimationBinding,widget: BaseWidget<*, *>) :
            WidgetBindingVH<WidgetDoubtP2pAnimationBinding>(binding,widget)

    class Model : WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("id") val id: String?,
        @SerializedName("text") var text: String?,
        @SerializedName("animation_file_name") val animationFileName: String,
        @SerializedName("timer_duration") val totalTimerDuration: Long?,
    ) : WidgetData()
}