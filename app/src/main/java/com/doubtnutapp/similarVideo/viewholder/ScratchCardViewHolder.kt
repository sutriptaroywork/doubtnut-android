package com.doubtnutapp.similarVideo.viewholder

import android.graphics.Typeface
import android.net.Uri
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.LinearLayout
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewHolder
import com.doubtnutapp.course.widgets.SaleWidget
import com.doubtnutapp.databinding.ItemScratchCardBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.ui.views.ScratchTextView
import com.doubtnutapp.loadBackgroundImage
import com.doubtnutapp.similarVideo.model.ScratchCardItem
import javax.inject.Inject

class ScratchCardViewHolder(itemView: View) : BaseViewHolder<ScratchCardItem>(itemView) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val TAG = "ScratchCardViewHolder"

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    val binding = ItemScratchCardBinding.bind(itemView)

    override fun bind(data: ScratchCardItem) {
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
        binding.title.text = data.title.orEmpty()
        binding.subtitle.text = data.subtitle.orEmpty()
        binding.scratchTv.visibility = View.VISIBLE
        binding.layoutScratchCard.loadBackgroundImage(data.imageUrl.orEmpty(), R.color.purple)
        binding.btnBuyNow.setOnClickListener {
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
            deeplinkAction.performAction(itemView.context, data.deeplink.orEmpty())
        }
        if (data.isRevealed) {
            binding.btnBuyNow.visibility = LinearLayout.VISIBLE
            binding.scratchedTv.visibility = LinearLayout.VISIBLE
            binding.scratchIv.visibility = LinearLayout.INVISIBLE
            val text =
                SpannableString(data.priceText + "\n" + data.scratchText + "\n\n" + data.couponCode.orEmpty())
            text.setSpan(
                RelativeSizeSpan(1.5f), 0, data.priceText?.length
                    ?: 0,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text.setSpan(
                StyleSpan(Typeface.BOLD), 0, data.priceText?.length
                    ?: 0,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.scratchedTv.text = text
        } else {
            binding.btnBuyNow.visibility = LinearLayout.GONE
        }
        val listener = object : ScratchTextView.IRevealListener {
            override fun onRevealed(tv: ScratchTextView?) {
                data.isRevealed = true
                binding.btnBuyNow.visibility = LinearLayout.VISIBLE
            }

            override fun onRevealPercentChangedListener(stv: ScratchTextView?, percent: Float) {
                if (percent >= .05) {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_NUDGE_SCRATCH,
                            hashMapOf<String, Any>(
                                EventConstants.NUDGE_ID to data.id.orEmpty()
                            )
                        )
                    )
                    binding.btnBuyNow.visibility = LinearLayout.VISIBLE
                    binding.scratchedTv.visibility = LinearLayout.VISIBLE
                    binding.scratchIv.visibility = LinearLayout.INVISIBLE
                    val text =
                        SpannableString(data.priceText + "\n" + data.scratchText + "\n\n" + data.couponCode.orEmpty())
                    text.setSpan(
                        RelativeSizeSpan(1.5f), 0, data.priceText?.length
                            ?: 0,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    text.setSpan(
                        StyleSpan(Typeface.BOLD), 0, data.priceText?.length
                            ?: 0,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    binding.scratchedTv.text = text
                }
            }
        }
        binding.scratchTv.setRevealListener(listener)
    }

}