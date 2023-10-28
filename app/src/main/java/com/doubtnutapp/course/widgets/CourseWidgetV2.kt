package com.doubtnutapp.course.widgets

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import androidx.annotation.Keep
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.OnCourseCarouselChildWidgetItmeClicked
import com.doubtnutapp.databinding.WidgetCourseV2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

class CourseWidgetV2(context: Context) : BaseBindingWidget<CourseWidgetV2.WidgetHolder,
        CourseWidgetV2.CourseWidgetModelV2, WidgetCourseV2Binding>(context) {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    var source: String? = null

    companion object {
        const val TAG = "CourseWidgetV2"
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    override fun getViewBinding(): WidgetCourseV2Binding {
        return WidgetCourseV2Binding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(holder: WidgetHolder, model: CourseWidgetModelV2): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = WidgetLayoutConfig(
                    model.layoutConfig?.marginTop ?: 0,
                    model.layoutConfig?.marginBottom ?: 0, model.layoutConfig?.marginLeft ?: 0,
                    model.layoutConfig?.marginRight ?: 0
                )
            }
        )
        val item: CourseWidgetDataV2 = model.data

        val binding = holder.binding

        if (item.setWidth == true) {
            Utils.setWidthBasedOnPercentage(
                binding.root.context,
                holder.itemView,
                "1.3",
                R.dimen.spacing
            )
        }

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.POPULAR_COURSE_VIEW,
                hashMapOf<String, Any>(
                    EventConstants.ASSORTMENT_ID to item.id.orEmpty(),
                    EventConstants.WIDGET to TAG
                ).apply {
                    putAll(model.extraParams ?: HashMap())
                }
            )
        )

        binding.tvTitle.text = item.title.orEmpty()
        binding.tvTitle.setTextColor(Utils.parseColor(item.titleTextColor))
        binding.tvTitle.textSize = item.titleTextSize?.toFloat() ?: 18f

        binding.tvPrice.text = item.amountToPay.orEmpty()
        binding.tvPrice.setTextColor(Utils.parseColor(item.amountToPayTextColor))
        binding.tvPrice.textSize = item.amountToPayTextSize?.toFloat() ?: 18f

        if (item.isBuyNowEnabled == true) {
            binding.buttonBuyNow.visibility = VISIBLE
            binding.buttonBuyNow.text = item.buyText.orEmpty()
        } else {
            binding.buttonBuyNow.visibility = GONE
        }

        binding.tvMedium.text = item.mediumText.orEmpty()
        binding.tvMedium.setTextColor(Utils.parseColor(item.mediumTextColor))
        binding.tvMedium.textSize = item.mediumTextSize?.toFloat() ?: 13f

        binding.subtitle.text = item.poweredByText.orEmpty()
        binding.subtitle.setTextColor(Utils.parseColor(item.poweredByTextColor))
        binding.subtitle.textSize = item.poweredByTextSize?.toFloat() ?: 13f

        binding.cardView.loadBackgroundImage(item.imageBg.orEmpty(), R.color.blue)
        binding.ivPlay.loadImage(item.videoIconUrl.orEmpty())

        binding.tvStartingAt.text = item.discount.orEmpty()
        binding.tvStartingAt.setTextColor(Utils.parseColor(item.discountTextColor))
        binding.tvStartingAt.textSize = item.discountTextSize?.toFloat() ?: 13f

        binding.tvRating.text = item.ratingText.orEmpty()
        binding.tvRating.setTextColor(Utils.parseColor(item.ratingTextColor))
        binding.tvRating.textSize = item.ratingTextSize?.toFloat() ?: 14f
        binding.ivRating.loadImage(item.ratingIconUrl.orEmpty())
        if (!item.ratingText.isNullOrEmpty()) {
            binding.ivRating.visibility = VISIBLE
            binding.tvRating.visibility = VISIBLE
            binding.ivDuration.setMargins(12, 0, 0, 0)
        } else {
            binding.ivRating.visibility = GONE
            binding.ivDuration.setMargins(0, 0, 0, 0)
        }

        binding.tvDuration.text = item.durationText.orEmpty()
        binding.tvDuration.setTextColor(Utils.parseColor(item.durationTextColor))
        binding.tvDuration.textSize = item.durationTextSize?.toFloat() ?: 14f
        binding.ivDuration.loadImage(item.durationIconUrl.orEmpty())

        if (!item.trialText.isNullOrEmpty()) {
            binding.layoutDurationLeft.setVisibleState(true)
            binding.layoutDurationLeft.setBackgroundColor(Color.parseColor(item.trialBackgroundColor.orEmpty()))
            binding.tvDurationLeft.text = item.trialText.orEmpty()
            binding.tvDurationLeft.setTextColor(Color.parseColor(item.trialTextColor.orEmpty()))
            binding.ivDurationLeft.loadImage(item.trialImageUrl.orEmpty())
        } else {
            binding.layoutDurationLeft.setVisibleState(false)
        }

        binding.root.setOnClickListener {
            actionPerformer?.performAction(
                OnCourseCarouselChildWidgetItmeClicked(
                    item.title.orEmpty(),
                    item.id.orEmpty(),
                    -1,
                    source
                )
            )
            deeplinkAction.performAction(binding.root.context, item.deeplink)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.POPULAR_COURSE_BANNER_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.ASSORTMENT_ID to item.id.orEmpty(),
                        EventConstants.WIDGET to TAG
                    ).apply {
                        putAll(model.extraParams ?: HashMap())
                    }
                )
            )
        }
        if (!item.amountStrikeThrough.isNullOrEmpty()) {
            binding.tvStartingAt.text = item.amountStrikeThrough
            binding.tvStartingAt.paintFlags =
                binding.tvStartingAt.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            binding.tvStartingAt.text = item.discount
            binding.tvStartingAt.paintFlags =
                binding.tvStartingAt.paintFlags or Paint.LINEAR_TEXT_FLAG
        }
        binding.buttonBuyNow.setOnClickListener {
            val event = AnalyticsEvent(
                EventConstants.POPULAR_COURSE_BUTTON_CLICK,
                hashMapOf<String, Any>(
                    EventConstants.ASSORTMENT_ID to item.id.orEmpty(),
                    EventConstants.WIDGET to TAG,
                    EventConstants.CLICKED_BUTTON_NAME to item.buyText.toString()
                ).apply {
                    putAll(model.extraParams ?: HashMap())
                }, ignoreMoengage = false
            )
            MoEngageUtils.setUserAttribute(context, "dn_bnb_clicked",true)

            analyticsPublisher.publishEvent(event)
            val countToSendEvent: Int = Utils.getCountToSend(
                RemoteConfigUtils.getEventInfo(),
                EventConstants.POPULAR_COURSE_BUTTON_CLICK
            )
            val eventCopy = event.copy()
            repeat((0 until countToSendEvent).count()) {
                analyticsPublisher.publishBranchIoEvent(eventCopy)
            }
            deeplinkAction.performAction(context, item.buyDeeplink)
        }

        if (!item.courseTag.isNullOrEmpty()) {
            binding.tvCourseTag.text = item.courseTag.orEmpty()
            binding.ivCourseTag.setVisibleState(true)
            binding.tvCourseTag.setVisibleState(true)
        } else {
            binding.ivCourseTag.setVisibleState(false)
            binding.tvCourseTag.setVisibleState(false)
        }

        return holder
    }

    class WidgetHolder(binding: WidgetCourseV2Binding, widget: BaseWidget<*, *>) :
        WidgetBindingVH<WidgetCourseV2Binding>(binding, widget)

    class CourseWidgetModelV2 : WidgetEntityModel<CourseWidgetDataV2, WidgetAction>()

    @Keep
    data class CourseWidgetDataV2(
        @SerializedName("id") val id: String?,
        @SerializedName("image_bg") val imageBg: String?,
        @SerializedName("video_icon_url") val videoIconUrl: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("title_color") val titleTextColor: String?,
        @SerializedName("title_text_size") val titleTextSize: Int?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("buy_deeplink") val buyDeeplink: String?,
        @SerializedName("multiple_package") val multiplePackage: Boolean?,
        @SerializedName("amount_to_pay") val amountToPay: String?,
        @SerializedName("amount_to_pay_text_color") val amountToPayTextColor: String?,
        @SerializedName("amount_to_pay_text_size") val amountToPayTextSize: Int?,
        @SerializedName("amount_strike_through") val amountStrikeThrough: String?,
        @SerializedName("discount") val discount: String?,
        @SerializedName("buy_text") val buyText: String?,
        @SerializedName("set_width") val setWidth: Boolean?,
        @SerializedName("discount_color") val discountTextColor: String?,
        @SerializedName("discount_text_size") val discountTextSize: Int?,
        @SerializedName("is_premium") val isPremium: Boolean?,
        @SerializedName("medium_text") val mediumText: String?,
        @SerializedName("medium_text_color") val mediumTextColor: String?,
        @SerializedName("medium_text_size") val mediumTextSize: String?,
        @SerializedName("powered_by_text") val poweredByText: String?,
        @SerializedName("powered_by_text_color") val poweredByTextColor: String?,
        @SerializedName("powered_by_text_size") val poweredByTextSize: String?,
        @SerializedName("is_buy_now_enable") val isBuyNowEnabled: Boolean?,
        @SerializedName("rating_text") val ratingText: String?,
        @SerializedName("rating_text_color") val ratingTextColor: String?,
        @SerializedName("rating_text_size") val ratingTextSize: Int?,
        @SerializedName("rating_icon_url") val ratingIconUrl: String?,
        @SerializedName("duration_text") val durationText: String?,
        @SerializedName("duration_text_color") val durationTextColor: String?,
        @SerializedName("duration_text_size") val durationTextSize: Int?,
        @SerializedName("duration_icon_url") val durationIconUrl: String?,
        @SerializedName("is_purchased") val isPurchased: String?,
        @SerializedName("trial_text") val trialText: String?,
        @SerializedName("trial_image") val trialImageUrl: String?,
        @SerializedName("trial_background_color") val trialBackgroundColor: String?,
        @SerializedName("trial_text_color") val trialTextColor: String?,
        @SerializedName("course_tag") val courseTag: String?
    ) : WidgetData()
}
