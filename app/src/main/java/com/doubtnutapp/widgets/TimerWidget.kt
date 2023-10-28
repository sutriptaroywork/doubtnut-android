package com.doubtnutapp.widgets

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.ui.CoreBindingWidget
import com.doubtnut.core.widgets.ui.CoreWidget
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnut.referral.widgets.ReferralClaimWidget
import com.doubtnutapp.databinding.WidgetTimerBinding
import com.doubtnutapp.invisible
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class TimerWidget
@JvmOverloads
constructor(
    context: Context,
    @Suppress("UNUSED_PARAMETER")
    attrs: AttributeSet? = null,
    @Suppress("UNUSED_PARAMETER")
    defStyle: Int = 0
) : CoreBindingWidget<TimerWidget.WidgetViewHolder,
        TimerWidget.Model, WidgetTimerBinding>(context, attrs, defStyle) {

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    var source: String? = null
    var adapter: WidgetLayoutAdapter? = null

    private var countDownTimer: CountDownTimer? = null

    companion object {
        const val TAG = "TimerWidget"
        const val EVENT_TAG = "timer_widget"
    }

    init {
        CoreApplication.INSTANCE.androidInjector().inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun getViewBinding(): WidgetTimerBinding {
        return WidgetTimerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: Model
    ): WidgetViewHolder {
        super.bindWidget(holder, model)
        val binding = holder.binding
        val data = model.data

        binding.containerTrialInfo.background =
            GradientUtils.getGradientBackground(
                data.bgColorOne,
                data.bgColorTwo,
                GradientDrawable.Orientation.LEFT_RIGHT
            )

        binding.containerTrialInfo.setOnClickListener {
            deeplinkAction.performAction(context, data.deeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${ReferralClaimWidget.EVENT_TAG}_${CoreEventConstants.CLICKED}",
                    hashMapOf<String, Any>(
                        CoreEventConstants.WIDGET to TAG,
                        CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                        CoreEventConstants.SOURCE to source.orEmpty()
                    ).apply {
                        putAll(model.extraParams.orEmpty())
                    }
                )
            )
        }

        if (data.trialTitle.isNullOrEmpty()) {
            binding.tvTrialInfo.visibility = View.GONE
        } else {
            binding.tvTrialInfo.visibility = View.VISIBLE
            binding.tvTrialInfo.text = data.trialTitle
            binding.tvTrialInfo.applyTextSize(data.trialTitleSize)
            binding.tvTrialInfo.applyTextColor(data.trialTitleColor)
        }

        if (data.imageUrl.isNullOrEmpty()) {
            binding.ivGif.visibility = View.GONE
        } else {
            binding.ivGif.visibility = View.VISIBLE
            Glide.with(context).load(data.imageUrl).into(binding.ivGif)
        }

        countDownTimer?.cancel()
        if (data.time == null || data.time <= 0L) {
            onTimerFinished(binding, model)
        } else {
            val actualTimeLeft =
                ((data.time.or(0)).minus(System.currentTimeMillis()))

            if (actualTimeLeft > 0) {
                binding.tvTimer.visibility = View.VISIBLE
                binding.tvTimer.applyTextColor(data.timeTextColor)
                binding.tvTimer.applyTextSize(data.timeTextSize)

                countDownTimer = object : CountDownTimer(
                    actualTimeLeft, 1000
                ) {
                    override fun onTick(millisUntilFinished: Long) {
                        binding.tvTimer.text =
                            DateTimeUtils.formatMilliSecondsToTime(
                                millisUntilFinished
                            )
                    }

                    override fun onFinish() {
                        onTimerFinished(binding, model)
                    }
                }
                countDownTimer?.start()
            } else {
                onTimerFinished(binding, model)
            }
        }

        return holder
    }

    fun onTimerFinished(binding: WidgetTimerBinding, model: Model) {
        if (model.data.removeOnCompletion == true) {
            binding.root.apply {
                if (model.data.invisibleOnCompletion == true) {
                    invisible()
                } else {
                    gone()
                }
            }
            (binding.root.parent as? View)?.apply {
                if (model.data.invisibleOnCompletion == true) {
                    invisible()
                } else {
                    gone()
                }
            }
            return
        }

        val data = model.data
        val cornerRadii = if (data.roundedTopCorners == true) {
            floatArrayOf(
                4.dpToPxFloat(),
                4.dpToPxFloat(),
                4.dpToPxFloat(),
                4.dpToPxFloat(),
                0f,
                0f,
                0f,
                0f
            )
        } else null

        val background = GradientUtils.getGradientBackground(
            startGradient = data.bgColorOneExpired,
            endGradient = data.bgColorTwoExpired,
            orientation = GradientDrawable.Orientation.LEFT_RIGHT,
            cornerRadii = cornerRadii
        )
        binding.containerTrialInfo.background = background

        binding.tvTrialInfo.text = data.trialTitleExpired
        binding.tvTrialInfo.isVisible = data.trialTitle.isNotNullAndNotEmpty()

        binding.tvTimer.visibility = View.GONE
        binding.ivGif.visibility = View.GONE

        data.trialTitle = data.trialTitleExpired
        data.bgColorOne = data.bgColorOneExpired
        data.bgColorTwo = data.bgColorTwoExpired
    }

    class WidgetViewHolder(binding: WidgetTimerBinding, widget: CoreWidget<*, *>) :
        WidgetBindingVH<WidgetTimerBinding>(binding, widget)

    class Model :
        WidgetEntityModel<Data, WidgetAction>()

    @Keep
    data class Data(
        @SerializedName("trial_title")
        var trialTitle: String?,
        @SerializedName("trial_title_expired")
        val trialTitleExpired: String?,
        @SerializedName("trial_title_size")
        val trialTitleSize: String?,
        @SerializedName("trial_title_color")
        val trialTitleColor: String?,

        @SerializedName("time")
        val time: Long?,

        @SerializedName("time_text_color")
        val timeTextColor: String?,
        @SerializedName("time_text_size")
        val timeTextSize: String?,

        @SerializedName("bg_color_one")
        var bgColorOne: String?,
        @SerializedName("bg_color_two")
        var bgColorTwo: String?,
        @SerializedName("bg_color_one_expired")
        val bgColorOneExpired: String?,
        @SerializedName("bg_color_two_expired")
        val bgColorTwoExpired: String?,

        @SerializedName("image_url")
        val imageUrl: String?,

        @SerializedName("deeplink")
        val deeplink: String?,

        @SerializedName("remove_on_completion")
        val removeOnCompletion: Boolean?,
        @SerializedName("invisible_on_completion")
        val invisibleOnCompletion: Boolean?,

        @SerializedName("rounded_top_corners")
        var roundedTopCorners: Boolean?,

        ) : WidgetData()
}