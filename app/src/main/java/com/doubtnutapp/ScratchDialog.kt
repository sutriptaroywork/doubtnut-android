package com.doubtnutapp

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.ViewGroup
import android.widget.LinearLayout
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.course.widgets.SaleWidget
import com.doubtnutapp.course.widgets.SaleWidgetItem
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.liveclass.ui.views.ScratchTextView
import kotlinx.android.synthetic.main.fragment_scratch_card.*

class ScratchDialog(
        context: Context,
        val scratchData: SaleWidgetItem,
        val deeplinkAction: DeeplinkAction,
        val analyticsPublisher: AnalyticsPublisher
) : AlertDialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_scratch_card)
        fullScreenMode()
        initUI()
    }

    private fun fullScreenMode() {
        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT
        super.getWindow()?.setLayout(width, height)
        super.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun initUI() {
        val deeplinkUri = Uri.parse(scratchData.deeplink)
        analyticsPublisher.publishEvent(
                AnalyticsEvent(EventConstants.EVENT_NUDGE_VIEW,
                        hashMapOf<String, Any>(
                                EventConstants.NUDGE_ID to scratchData.id.orEmpty(),
                                EventConstants.ASSORTMENT_ID to deeplinkUri.getQueryParameter("assortment_id").orEmpty(),
                                EventConstants.WIDGET to SaleWidget.SaleWidgetAdapter.TAG
                        )
                )
        )
        title.text = scratchData.title.orEmpty()
        subtitle.text = scratchData.subtitle.orEmpty()
        layoutScratchCard.loadBackgroundImage(scratchData.imageUrl.orEmpty(), R.color.purple)
        btnBuyNow.setOnClickListener {
            analyticsPublisher.publishEvent(
                    AnalyticsEvent(EventConstants.EVENT_NUDGE_CLICK,
                            hashMapOf<String, Any>(
                                    EventConstants.NUDGE_ID to scratchData.id.orEmpty(),
                                    EventConstants.ASSORTMENT_ID to deeplinkUri.getQueryParameter("assortment_id").orEmpty(),
                                    EventConstants.WIDGET to SaleWidget.SaleWidgetAdapter.TAG
                            )
                    )
            )
            dismiss()
            deeplinkAction.performAction(context, scratchData.deeplink.orEmpty())

        }

        val text = SpannableString(scratchData.priceText + "\n" + scratchData.scratchText + "\n\n" + scratchData.couponCode.orEmpty())
        text.setSpan(RelativeSizeSpan(1.5f), 0, scratchData.priceText?.length
                ?: 0,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        text.setSpan(StyleSpan(Typeface.BOLD), 0, scratchData.priceText?.length
                ?: 0,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        scratchIv.text = text
        val listener = object : ScratchTextView.IRevealListener {

            override fun onRevealed(tv: ScratchTextView?) {
                scratchData.isRevealed = true
                btnBuyNow.visibility = LinearLayout.VISIBLE
            }

            override fun onRevealPercentChangedListener(stv: ScratchTextView?, percent: Float) {
                if (percent >= .15) {
                    analyticsPublisher.publishEvent(
                            AnalyticsEvent(EventConstants.EVENT_NUDGE_SCRATCH,
                                    hashMapOf<String, Any>(
                                            EventConstants.NUDGE_ID to scratchData.id.orEmpty(),
                                            EventConstants.WIDGET to SaleWidget.SaleWidgetAdapter.TAG
                                    )
                            )
                    )
                    scratchIv.reveal()
                }
            }
        }
        scratchIv.setRevealListener(listener)
    }

}