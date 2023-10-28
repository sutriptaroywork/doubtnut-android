package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.graphics.Color
import android.os.CountDownTimer
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.databinding.WidgetCouponBannerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.invisible
import com.doubtnutapp.loadImage
import com.doubtnutapp.show
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Akshat Jindal 1/5/21
 */

class CouponBannerWidget(context: Context) : BaseBindingWidget<CouponBannerWidgetHolder,
        CouponBannerWidgetModel, WidgetCouponBannerBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
    }

    override fun getViewBinding(): WidgetCouponBannerBinding {
        return WidgetCouponBannerBinding.inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = CouponBannerWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        widgetHolder: CouponBannerWidgetHolder,
        widgetModel: CouponBannerWidgetModel
    )
            : CouponBannerWidgetHolder {
        super.bindWidget(widgetHolder, widgetModel)

        val data = widgetModel.data
        val binding = widgetHolder.binding

        binding.apply {
            ivBackground.loadImage(data.image_url)
            tvTitle.text = data.title
            tvSubtitle.text = data.subtitle
            tvHeading.text = data.heading
            tvDescription.text = data.description
            setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.TIMER_PROMO_WIDGET_CLICK,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.NUDGE_ID, data?.id ?: -1)
                        }, ignoreSnowplow = true)
                )
                deeplinkAction.performAction(widgetHolder.itemView.context, data.deeplink)
            }

            tvTitle.setTextColor(Color.parseColor(data.textColor))
            tvSubtitle.setTextColor(Color.parseColor(data.textColor))
            tvHeading.setTextColor(Color.parseColor(data.textColor))
            tvDescription.setTextColor(Color.parseColor(data.textColor))

            tvTime1.setTextColor(Color.parseColor(data.textColor))
            tvTime2.setTextColor(Color.parseColor(data.textColor))
            tvSeperator.setTextColor(Color.parseColor(data.textColor))
            tvTime3.setTextColor(Color.parseColor(data.textColor))
            tvTime4.setTextColor(Color.parseColor(data.textColor))
            tvTime5.setTextColor(Color.parseColor(data.textColor))
            tvTime6.setTextColor(Color.parseColor(data.textColor))

            getTimer(data.time ?: 0, binding).start()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.TIMER_PROMO_WIDGET_SHOWN,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.NUDGE_ID, data?.id ?: -1)
                    }, ignoreSnowplow = true)
            )
        }
        return widgetHolder
    }

    private fun getTimer(time: Long, binding: WidgetCouponBannerBinding): CountDownTimer {

        val timer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {

                val totalSeconds = millisUntilFinished / 1000
                val minutesLeft = totalSeconds / 60
                val secondsLeft = totalSeconds - (minutesLeft * 60)
                val hoursLeft = minutesLeft / 60
                val daysLeft = hoursLeft / 24

                when {
                    daysLeft > 0 -> {
                        binding.tvTime1.text = (daysLeft / 10).toString()
                        binding.tvTime2.text = (daysLeft % 10).toString()
                        val hoursLeftFinal = hoursLeft - (daysLeft * 24)
                        binding.tvTime3.text = (hoursLeftFinal / 10).toString()
                        binding.tvTime4.text = (hoursLeftFinal % 10).toString()
                        binding.tvTime5.text = "Days"
                        binding.tvTime6.text = "Hours"

                    }
                    hoursLeft > 0 -> {
                        binding.tvTime1.text = (hoursLeft / 10).toString()
                        binding.tvTime2.text = (hoursLeft % 10).toString()
                        val minutesLeftFinal = minutesLeft - (hoursLeft * 60)
                        binding.tvTime3.text = (minutesLeftFinal / 10).toString()
                        binding.tvTime4.text = (minutesLeftFinal % 10).toString()
                        binding.tvTime5.text = "Hours"
                        binding.tvTime6.text = "Mins"
                    }
                    else -> {
                        binding.tvTime1.text = (minutesLeft / 10).toString()
                        binding.tvTime2.text = (minutesLeft % 10).toString()
                        binding.tvTime3.text = (secondsLeft / 10).toString()
                        binding.tvTime4.text = (secondsLeft % 10).toString()
                        binding.tvTime5.text = "Mins"
                        binding.tvTime6.text = "Secs"
                    }
                }

                if (totalSeconds % 2 == 0L) {
                    binding.tvSeperator.show()
                } else {
                    binding.tvSeperator.invisible()
                }
            }

            override fun onFinish() {
                binding.tvTime1.text = "0"
                binding.tvTime2.text = "0"
                binding.tvTime3.text = "0"
                binding.tvTime4.text = "0"
            }
        }

        return timer
    }

}

class CouponBannerWidgetHolder(binding: WidgetCouponBannerBinding, widget: BaseWidget<*, *>) :
    WidgetBindingVH<WidgetCouponBannerBinding>(binding, widget)

class CouponBannerWidgetModel : WidgetEntityModel<CouponBannerWidgetData, WidgetAction>()

@Keep
data class CouponBannerWidgetData(

    @SerializedName("id") val id: Int?,
    @SerializedName("image_url") val image_url: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("heading") val heading: String?,
    @SerializedName("time") val time: Long?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("text_color") val textColor: String?
) : WidgetData()