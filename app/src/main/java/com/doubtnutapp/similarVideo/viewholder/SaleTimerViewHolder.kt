package com.doubtnutapp.similarVideo.viewholder

import android.net.Uri
import android.os.CountDownTimer
import android.view.View
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.course.widgets.SaleWidget
import com.doubtnutapp.databinding.ItemSaleTimerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.hide
import com.doubtnutapp.loadBackgroundImage
import com.doubtnutapp.similarVideo.model.SaleTimerItem
import javax.inject.Inject

class SaleTimerViewHolder(itemView: View) : BaseViewHolder<SaleTimerItem>(itemView) {

    val binding = ItemSaleTimerBinding.bind(itemView)

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    private var hoursText = "00"
    private var minutesText = "00"
    private var secondsText = "00"

    override fun bind(data: SaleTimerItem) {
        val deeplinkUri = Uri.parse(data.deeplink)
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NUDGE_VIEW,
                hashMapOf<String, Any>(
                    EventConstants.NUDGE_ID to data.id.orEmpty(),
                    EventConstants.ASSORTMENT_ID to deeplinkUri.getQueryParameter("assortment_id")
                        .orEmpty(),
                    EventConstants.WIDGET to SaleWidget.SaleWidgetAdapter.TAG
                )
            )
        )
        binding.titleTv.text = data.title.orEmpty()
        binding.subtitleTv.text = data.subtitle.orEmpty()
        binding.bottomText.text = data.bottomText.orEmpty()
        binding.saleLayout.loadBackgroundImage(data.imageUrl.orEmpty(), R.color.blue)
        binding.timerLayout.loadBackgroundImage(
            data.imageUrlSecond.orEmpty(),
            R.color.yellow_f4ac3e
        )
        binding.fullLayout.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EVENT_NUDGE_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.NUDGE_ID to data.id.orEmpty(),
                        EventConstants.ASSORTMENT_ID to deeplinkUri.getQueryParameter("assortment_id")
                            .orEmpty()
                    )
                )
            )
            deeplinkAction.performAction(itemView.context, data.deeplink)
        }
        val actualTimeLeft = data.endTime ?: 0 -
        (System.currentTimeMillis() - data.responseAtTimeInMillis)
        if (actualTimeLeft > 0) {
            itemView.visibility = View.VISIBLE
            val timer = object : CountDownTimer(
                actualTimeLeft,
                1000
            ) {
                override fun onTick(millisUntilFinished: Long) {
                    formatMilliSecondsToTime(millisUntilFinished)
                    binding.hoursTv.text = hoursText
                    binding.minutesTv.text = minutesText
                    binding.secondsTv.text = secondsText
                }

                override fun onFinish() {
                    itemView.hide()
                }
            }
            timer.start()
        } else {
            itemView.visibility = View.GONE
        }
    }

    private fun formatMilliSecondsToTime(milliseconds: Long) {
        val seconds: Int = ((milliseconds / 1000) % 60).toInt()
        val minutes: Int = ((milliseconds / (1000 * 60)) % 60).toInt()
        val hours: Int = ((milliseconds / (1000 * 60 * 60)) % 24).toInt()
        hoursText = twoDigitString(hours)
        minutesText = twoDigitString(minutes)
        secondsText = twoDigitString(seconds)
    }

    private fun twoDigitString(number: Int): String {
        if (number == 0) {
            return "00"
        }
        if (number / 10 == 0) {
            return "0$number"
        }
        return number.toString()
    }
}