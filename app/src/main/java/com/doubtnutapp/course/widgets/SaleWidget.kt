package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.CountDownTimer
import android.text.Spannable
import android.text.SpannableString
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BuyNowClicked
import com.doubtnutapp.databinding.ItemSaleTimerBinding
import com.doubtnutapp.databinding.ItemScratchCardBinding
import com.doubtnutapp.databinding.WidgetScratchBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.liveclass.ui.views.ScratchTextView
import com.doubtnutapp.utils.BannerActionUtils.deeplinkAction
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class SaleWidget(context: Context) :
    BaseBindingWidget<SaleWidget.WidgetHolder,
        SaleWidgetModel, WidgetScratchBinding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun getViewBinding(): WidgetScratchBinding {
        return WidgetScratchBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        DoubtnutApp.INSTANCE.daggerAppComponent!!.inject(this)
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: SaleWidgetModel): WidgetHolder {
        super.bindWidget(holder, model)
        val data = model.data
        val binding = holder.binding
        if (data.scrollType == "horizontal") {
            binding.recyclerView.layoutManager = LinearLayoutManager(
                holder.itemView.context, RecyclerView.HORIZONTAL,
                false
            )
        } else {
            binding.recyclerView.layoutManager = LinearLayoutManager(
                holder.itemView.context, RecyclerView.VERTICAL,
                false
            )
        }
        binding.recyclerView.adapter = SaleWidgetAdapter(
            data.items.orEmpty(),
            actionPerformer,
            analyticsPublisher, deeplinkAction,
            model.extraParams ?: HashMap()
        )
        return holder
    }

    class SaleWidgetAdapter(
        val items: List<SaleWidgetItem>,
        val actionPerformer: ActionPerformer? = null,
        val analyticsPublisher: AnalyticsPublisher,
        val deeplinkAction: DeeplinkAction,
        val extraParams: HashMap<String, Any>
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        companion object {
            const val SCRATCH_CARD = 1
            const val TAG = "SaleWidget"
            const val TIMER = 2
            const val TYPE_SCRATCH_CARD = "scratch_card"
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if (viewType == SCRATCH_CARD) {
                ScratchCardViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_scratch_card, parent,
                        false
                    )
                )
            } else {
                SaleTimerViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.item_sale_timer, parent,
                        false
                    )
                )
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val data = items[position]
            if (data.type == TYPE_SCRATCH_CARD) {
                (holder as ScratchCardViewHolder).bindScratchCardData(
                    data, deeplinkAction,
                    actionPerformer, analyticsPublisher, extraParams
                )
            } else {
                (holder as SaleTimerViewHolder).bindSaleTimerData(data, deeplinkAction, analyticsPublisher, extraParams)
            }
        }

        override fun getItemViewType(position: Int): Int {
            if (items[position].type == TYPE_SCRATCH_CARD) {
                return SCRATCH_CARD
            }
            return TIMER
        }

        override fun getItemCount(): Int = items.size

        class ScratchCardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val binding = ItemScratchCardBinding.bind(itemView)

            fun bindScratchCardData(
                data: SaleWidgetItem,
                deeplinkAction: DeeplinkAction,
                actionPerformer: ActionPerformer?,
                analyticsPublisher: AnalyticsPublisher,
                extraParams: HashMap<String, Any>
            ) {
                val deeplinkUri = Uri.parse(data.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NUDGE_VIEW,
                        hashMapOf<String, Any>(
                            EventConstants.NUDGE_ID to data.id.orEmpty(),
                            EventConstants.ASSORTMENT_ID to deeplinkUri.getQueryParameter("assortment_id").orEmpty(),
                            EventConstants.WIDGET to TAG
                        ).apply {
                            putAll(extraParams)
                        }
                    )
                )
                binding.title.text = data.title.orEmpty()
                binding.subtitle.text = data.subtitle.orEmpty()
                binding.layoutScratchCard.loadBackgroundImage(data.imageUrl.orEmpty(), R.color.purple)
                binding.btnBuyNow.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_NUDGE_CLICK,
                            hashMapOf<String, Any>(
                                EventConstants.NUDGE_ID to data.id.orEmpty(),
                                EventConstants.ASSORTMENT_ID to deeplinkUri.getQueryParameter("assortment_id").orEmpty(),
                                EventConstants.WIDGET to TAG
                            ).apply {
                                putAll(extraParams)
                            }
                        )
                    )
                    actionPerformer?.performAction(BuyNowClicked())
                    deeplinkAction.performAction(itemView.context, data.deeplink.orEmpty())
                }
                if (data.isRevealed) {
                    binding.btnBuyNow.visibility = VISIBLE
                    binding.scratchedTv.visibility = VISIBLE
                    binding.scratchIv.visibility = INVISIBLE
                    val text = SpannableString(data.priceText + "\n" + data.scratchText + "\n\n" + data.couponCode.orEmpty())
                    text.setSpan(
                        RelativeSizeSpan(1.5f), 0,
                        data.priceText?.length
                            ?: 0,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    text.setSpan(
                        StyleSpan(Typeface.BOLD), 0,
                        data.priceText?.length
                            ?: 0,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    binding.scratchedTv.text = text
                } else {
                    binding.btnBuyNow.visibility = GONE
                    binding.scratchedTv.visibility = INVISIBLE
                    if (data.isDialogRequired == true) {
                        binding.scratchIv.visibility = VISIBLE
                        binding.scratchTv.visibility = GONE
                        binding.scratchIv.setOnClickListener {
                            showScratchDialog(data, analyticsPublisher)
                        }
                    } else {
                        binding.scratchIv.visibility = INVISIBLE
                        binding.scratchTv.visibility = VISIBLE
                        val listener = object : ScratchTextView.IRevealListener {
                            override fun onRevealed(tv: ScratchTextView?) {
                                data.isRevealed = true
                                binding.btnBuyNow.visibility = VISIBLE
                            }

                            override fun onRevealPercentChangedListener(stv: ScratchTextView?, percent: Float) {
                                if (percent >= .05) {
                                    analyticsPublisher.publishEvent(
                                        AnalyticsEvent(
                                            EventConstants.EVENT_NUDGE_SCRATCH,
                                            hashMapOf<String, Any>(
                                                EventConstants.NUDGE_ID to data.id.orEmpty(),
                                                EventConstants.WIDGET to TAG
                                            ).apply {
                                                putAll(extraParams)
                                            }
                                        )
                                    )
                                    binding.btnBuyNow.visibility = VISIBLE
                                    binding.scratchedTv.visibility = VISIBLE
                                    binding.scratchIv.visibility = INVISIBLE
                                    val text = SpannableString(data.priceText + "\n" + data.scratchText + "\n\n" + data.couponCode.orEmpty())
                                    text.setSpan(
                                        RelativeSizeSpan(1.5f), 0,
                                        data.priceText?.length
                                            ?: 0,
                                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                                    )
                                    text.setSpan(
                                        StyleSpan(Typeface.BOLD), 0,
                                        data.priceText?.length
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
            }

            private fun showScratchDialog(data: SaleWidgetItem, analyticsPublisher: AnalyticsPublisher) {
                val scratchDialog = ScratchDialog(itemView.context, data, deeplinkAction, analyticsPublisher)
                scratchDialog.show()
                scratchDialog.setOnDismissListener {
                    if (data.isRevealed) {
                        binding.btnBuyNow.visibility = VISIBLE
                        binding.scratchedTv.visibility = VISIBLE
                        binding.scratchIv.visibility = INVISIBLE
                        val text = SpannableString(data.priceText + "\n" + data.scratchText + "\n\n" + data.couponCode.orEmpty())
                        text.setSpan(
                            RelativeSizeSpan(1.5f), 0,
                            data.priceText?.length
                                ?: 0,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        text.setSpan(
                            StyleSpan(Typeface.BOLD), 0,
                            data.priceText?.length
                                ?: 0,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        binding.scratchedTv.text = text
                        it.dismiss()
                        binding.btnBuyNow.text = data.buyNowText
                    } else {
                        binding.btnBuyNow.visibility = GONE
                        binding.scratchedTv.visibility = INVISIBLE
                        binding.scratchIv.visibility = VISIBLE
                        it.dismiss()
                    }
                }
            }
        }

        class SaleTimerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = ItemSaleTimerBinding.bind(itemView)

            private var hoursText = "00"
            private var minutesText = "00"
            private var secondsText = "00"
            private var daysText = "00"
            private var hours: Int = 0
            private var minutes: Int = 0
            private var seconds: Int = 0
            private var daysLeft: Int = 0
            fun bindSaleTimerData(
                data: SaleWidgetItem,
                deeplinkAction: DeeplinkAction,
                analyticsPublisher: AnalyticsPublisher,
                extraParams: HashMap<String, Any>
            ) {
                val deeplinkUri = Uri.parse(data.deeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NUDGE_VIEW,
                        hashMapOf<String, Any>(
                            EventConstants.NUDGE_ID to data.id.orEmpty(),
                            EventConstants.ASSORTMENT_ID to deeplinkUri.getQueryParameter("assortment_id").orEmpty(),
                            EventConstants.WIDGET to TAG
                        ).apply {
                            putAll(extraParams)
                        }
                    )
                )
                binding.titleTv.text = data.title.orEmpty()
                binding.subtitleTv.text = data.subtitle.orEmpty()
                binding.bottomText.text = data.bottomText.orEmpty()
                binding.bottomText.setTextColor(Utils.parseColor(data.bottomTextColor.orEmpty()))
                binding.topText.text = data.priceText.orEmpty()
                binding.vipText.text = data.buyNowText.orEmpty()
                binding.topText.setTextColor(Utils.parseColor(data.topTextColor.orEmpty()))
                binding.vipText.setTextColor(Utils.parseColor(data.vipTextColor.orEmpty()))
                binding.vipText.text = data.buyNowText.orEmpty()
                binding.saleLayout.loadBackgroundImage(data.imageUrl.orEmpty(), R.color.blue)
                binding.timerLayout.loadBackgroundImage(data.imageUrlSecond.orEmpty(), R.color.yellow_f4ac3e)
                binding.fullLayout.setOnClickListener {
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_NUDGE_CLICK,
                            hashMapOf<String, Any>(
                                EventConstants.NUDGE_ID to data.id.orEmpty(),
                                EventConstants.ASSORTMENT_ID to deeplinkUri.getQueryParameter("assortment_id").orEmpty(),
                                EventConstants.WIDGET to TAG
                            ).apply {
                                putAll(extraParams)
                            }
                        )
                    )
                    deeplinkAction.performAction(itemView.context, data.deeplink)
                }
                val actualTimeLeft = (
                    data.endTime?.or(0)?.minus(
                        (
                            System.currentTimeMillis() - (
                                data.responseAtTimeInMillis
                                    ?: 0
                                )
                            )
                    )
                    ) ?: 0
                if (actualTimeLeft > 0) {
                    itemView.visibility = VISIBLE
                    val timer = object : CountDownTimer(
                        actualTimeLeft,
                        1000
                    ) {
                        override fun onTick(millisUntilFinished: Long) {
                            formatMilliSecondsToTime(millisUntilFinished)
                            when {
                                daysLeft > 0 -> {
                                    binding.hoursTv.text = daysText
                                    binding.minutesTv.text = hoursText
                                    binding.secondsTv.text = minutesText
                                    binding.hoursText.text = "days"
                                    binding.minsText.text = "hrs"
                                    binding.secondsText.text = "mins"
                                }
                                daysLeft == 0 -> {
                                    binding.hoursTv.text = hoursText
                                    binding.minutesTv.text = minutesText
                                    binding.secondsTv.text = secondsText
                                    binding.hoursText.text = "hrs"
                                    binding.minsText.text = "mins"
                                    binding.secondsText.text = "secs"
                                }
                            }
                        }

                        override fun onFinish() {
                            itemView.hide()
                        }
                    }
                    timer.start()
                } else {
                    itemView.visibility = GONE
                }
            }

            private fun formatMilliSecondsToTime(milliseconds: Long) {
                seconds = ((milliseconds / 1000) % 60).toInt()
                minutes = ((milliseconds / (1000 * 60)) % 60).toInt()
                hours = ((milliseconds / (1000 * 60 * 60)) % 24).toInt()
                daysLeft = ((milliseconds / (1000 * 60 * 60)) / 24).toInt()
                daysText = twoDigitString(daysLeft)
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

            private fun getMilliSeconds(timeText: String): Long {
                if (timeText.isEmpty())
                    return 0
                try {
                    val f = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
                    val d: Date? = f.parse(timeText)
                    return d?.time ?: 0
                } catch (e: Exception) {
                }
                return 0
            }
        }
    }

    class WidgetHolder(binding: WidgetScratchBinding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetScratchBinding>(binding, widget)
}

class SaleWidgetModel : WidgetEntityModel<SaleWidgetData, WidgetAction>()

@Keep
data class SaleWidgetData(
    @SerializedName("title") val title: String?,
    @SerializedName("scroll_type") val scrollType: String?,
    @SerializedName("items") val items: List<SaleWidgetItem>?
) : WidgetData()

@Keep
data class SaleWidgetItem(
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("image_url_second") val imageUrlSecond: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("subtitle") val subtitle: String?,
    @SerializedName("bottom_text") val bottomText: String?,
    @SerializedName("coupon_code") val couponCode: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("end_time") val endTime: Long?,
    var responseAtTimeInMillis: Long?,
    @SerializedName("scratch_text") val scratchText: String?,
    @SerializedName("price_text") val priceText: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("dialog") val isDialogRequired: Boolean?,
    @SerializedName("id") val id: String?,
    @SerializedName("buy_now_text") val buyNowText: String?,
    @SerializedName("nudge_id") val nudgeId: String?,
    @SerializedName("price_text_color") val topTextColor: String?,
    @SerializedName("buy_now_text_color") val vipTextColor: String?,
    @SerializedName("bottom_text_color") val bottomTextColor: String?,
    var isRevealed: Boolean = false

)
