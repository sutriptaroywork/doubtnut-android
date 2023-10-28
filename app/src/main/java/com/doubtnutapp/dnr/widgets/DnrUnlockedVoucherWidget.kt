package com.doubtnutapp.dnr.widgets

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.copy
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.DnrOpenWebUrl
import com.doubtnutapp.databinding.WidgetDnrUnlockedVoucherBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 25/10/21
 */

class DnrUnlockedVoucherWidget(context: Context) :
    BaseBindingWidget<
        DnrUnlockedVoucherWidget.WidgetHolder,
        DnrUnlockedVoucherWidgetModel,
        WidgetDnrUnlockedVoucherBinding>(context) {

    var source: String? = null

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetDnrUnlockedVoucherBinding {
        return WidgetDnrUnlockedVoucherBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetHolder,
        model: DnrUnlockedVoucherWidgetModel
    ): WidgetHolder {
        super.bindWidget(
            holder,
            model.apply {
                layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(0, 0, 16, 16)
            }
        )

        val data = model.data

        holder.binding.apply {
            if (data.isActive) {
                backgroundLayout.setOnClickListener {
                    deeplinkAction.performAction(context, data.deeplink)
                }
            }
            title.apply {
                text = data.title
                setTextColor(Color.parseColor(data.titleColor))
            }
            subtitle.apply {
                text = data.subtitle
                setTextColor(Color.parseColor(data.subtitleColor))
            }
            expiryOnText.text = data.expiryOnText
            button.apply {
                text = data.buttonText
                if (data.isActive)
                    setOnClickListener {
                        val url = data.ctaDeeplink.orEmpty()
                        if (url.startsWith("https://") or url.startsWith("http://")) {
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(EventConstants.DNR_REDEEM_NOW_CLICKED, ignoreSnowplow = true)
                            )
                            actionPerformer?.performAction(DnrOpenWebUrl(url))
                        } else {
                            deeplinkAction.performAction(context, data.ctaDeeplink)
                        }
                    }
            }

            if (data.voucherBackgroundColor.isValidColorCode()) {
                voucherLayout.setBackgroundColor(Color.parseColor(data.voucherBackgroundColor))
            }

            if (data.backgroundColor.isValidColorCode()) {
                backgroundLayout.setBackgroundColor(Color.parseColor(data.backgroundColor))
            }

            if (data.couponCode != null) {
                layoutCoupon.setVisibleState(true)
                couponCode.text = data.couponCode
                tvCopyText.apply {
                    text = data.copyText
                    setOnClickListener {
                        if (data.isActive) {
                            context?.copy(
                                text = data.couponCode,
                                label = "dnr_redeem_voucher_code",
                                toastMessage = context.getString(R.string.coupon_code_copied)
                            )
                        }
                    }
                }
            } else {
                layoutCoupon.setVisibleState(false)
            }

            expiredFilter.setVisibleState(data.isActive.not())
            expiredText.apply {
                isVisible = data.statusText.isNotNullAndNotEmpty()
                text = data.statusText
                if (data.statusTextColor.isValidColorCode()) {
                    setTextColor(Color.parseColor(data.statusTextColor))
                }
            }
            image.loadImage(data.imageUrl)
        }

        return holder
    }

    class WidgetHolder(
        binding: WidgetDnrUnlockedVoucherBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetDnrUnlockedVoucherBinding>(binding, widget)
}

@Keep
class DnrUnlockedVoucherWidgetModel() :
    WidgetEntityModel<DnrUnlockedVoucherWidgetData, WidgetAction>()

@Keep
data class DnrUnlockedVoucherWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("title_color") val titleColor: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("subtitle_color") val subtitleColor: String,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("cta_deeplink") val ctaDeeplink: String?,
    @SerializedName("status_text") val statusText: String?,
    @SerializedName("status_text_color") val statusTextColor: String?,
    @SerializedName("expiry_on_text") val expiryOnText: String?,
    @SerializedName("coupon_code") val couponCode: String?,
    @SerializedName("copy_text") val copyText: String,
    @SerializedName("cta_text") val buttonText: String,
    @SerializedName("voucher_image_url") val imageUrl: String,
    @SerializedName("voucher_background_color") val voucherBackgroundColor: String,
    @SerializedName("background_color") val backgroundColor: String,
    @SerializedName("is_active") val isActive: Boolean
) : WidgetData()
