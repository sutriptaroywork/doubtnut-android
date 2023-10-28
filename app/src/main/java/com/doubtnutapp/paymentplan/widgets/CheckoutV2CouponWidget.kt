package com.doubtnutapp.paymentplan.widgets

import android.content.Context
import android.view.LayoutInflater
import androidx.annotation.Keep
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.setTextFromHtml
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.CouponApplied
import com.doubtnutapp.base.OnAddCouponClick
import com.doubtnutapp.base.OnRemoveCouponClick
import com.doubtnutapp.databinding.WidgetCheckoutV2CouponBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 07-10-2021
 */

class CheckoutV2CouponWidget(context: Context) :
    BaseBindingWidget<CheckoutV2CouponWidget.WidgetViewHolder, CheckoutV2CouponWidgetModel, WidgetCheckoutV2CouponBinding>(
        context
    ) {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun getViewBinding(): WidgetCheckoutV2CouponBinding {
        return WidgetCheckoutV2CouponBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.forceUnWrap()?.inject(this)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = WidgetViewHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: WidgetViewHolder,
        model: CheckoutV2CouponWidgetModel
    ): WidgetViewHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = model.layoutConfig ?: WidgetLayoutConfig(22, 2, 16, 16)
        })
        val data = model.data

        holder.binding.apply {
            if (data.status) {
                image.setImageResource(R.drawable.ic_tick)
                title.text = data.code.orEmpty()
                title.setTextColor(ContextCompat.getColor(context, R.color.black))
                next.setVisibleState(false)
                couponStatusApplied.setVisibleState(true)
                couponStatus.setVisibleState(false)
                tvCouponAmount.setVisibleState(true)
                couponStatusApplied.text = data.couponStatusText
                tvCouponAmount.text = data.couponAmount
            } else {
                image.setImageResource(R.drawable.ic_wallet)
                title.text = data.title
                title.setTextColor(ContextCompat.getColor(context, R.color.black_50))
                next.setVisibleState(true)
                couponStatusApplied.setVisibleState(false)
                couponStatus.setVisibleState(true)
                tvCouponAmount.setVisibleState(false)
                couponStatus.text = data.couponStatusText
            }
            couponStatus.setOnClickListener {
                if (data.status) {
                    actionPerformer?.performAction(OnRemoveCouponClick)
                } else {
                    actionPerformer?.performAction(OnAddCouponClick)
                }
            }
            couponStatusApplied.setOnClickListener {
                if (data.status) {
                    actionPerformer?.performAction(OnRemoveCouponClick)
                } else {
                    actionPerformer?.performAction(OnAddCouponClick)
                }
            }

            clFooter.isVisible = data.footerTitle.isNotNullAndNotEmpty()
            tvFooter.setTextFromHtml(data.footerTitle.orEmpty())
            tvFooterCode.setTextFromHtml(data.footerCoupon.orEmpty())

            clFooter.setOnClickListener {
                actionPerformer?.performAction(CouponApplied(data.footerCoupon.orEmpty()))
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                        hashMapOf<String, Any>(
                            EventConstants.CTA_TEXT to data.footerTitle.orEmpty(),
                            EventConstants.STUDENT_ID to UserUtil.getStudentId()
                        ).apply {
                            putAll(model.extraParams.orEmpty())
                        }
                    )
                )
            }
        }



        return holder
    }

    class WidgetViewHolder(
        binding: WidgetCheckoutV2CouponBinding,
        widget: BaseWidget<*, *>
    ) : WidgetBindingVH<WidgetCheckoutV2CouponBinding>(binding, widget)

    companion object {
        private const val EVENT_TAG = "checkout_v2_coupon_widget"
    }
}

class CheckoutV2CouponWidgetModel : WidgetEntityModel<CheckoutV2CouponWidgetData, WidgetAction>()

@Keep
data class CheckoutV2CouponWidgetData(
    @SerializedName("title") val title: String,
    @SerializedName("text") val couponStatusText: String,
    @SerializedName("code") val code: String?,
    @SerializedName("status") val status: Boolean = false,
    @SerializedName("coupon_amount") val couponAmount: String?,
    @SerializedName("footer_title") val footerTitle: String?,
    @SerializedName("footer_coupon") val footerCoupon: String?
) : WidgetData()