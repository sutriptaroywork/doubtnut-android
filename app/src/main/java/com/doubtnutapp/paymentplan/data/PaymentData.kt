package com.doubtnutapp.paymentplan.data

import androidx.annotation.Keep
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.domain.payment.entities.CouponInfo
import com.doubtnutapp.domain.payment.entities.PaymentHelpData
import com.google.gson.annotations.SerializedName

/**
 * Created by Mehul Bisht on 08-10-2021
 */

@Keep
data class PaymentData(
    @SerializedName("title") val pageTitle: String?,
    @SerializedName("widgets") val widgets: List<WidgetEntityModel<*, *>>,
    @SerializedName("help") val paymentHelp: PaymentHelpData?,
    @SerializedName("event_info") val eventInfo: EventInfo?,
    @SerializedName("coupon_info")val couponInfo: CouponInfo?,
    @SerializedName("currency_symbol")val currencySymbol: String?
) {
    @Keep
    data class EventInfo(
        @SerializedName("variant_id") val variantId: String?,
        @SerializedName("assortment_type") val assortmentType: String?,
        @SerializedName("amount") val amount: String?,
        @SerializedName("package_description") val packageDescription: String?,
        @SerializedName("courseId") val courseId: String?,
        @SerializedName("package_id") val packageId: String?,
        @SerializedName("assortment_id") val assortmentId: String?,
        @SerializedName("duration") val duration: String?
    )
}


