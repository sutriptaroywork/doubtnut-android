package com.doubtnutapp.domain.payment.entities

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Created by Anand Gaurav on 2019-12-13.
 */
@Keep
data class Taxation(
    @SerializedName("razorpay_payment") val razorPayInfo: RazorPayInfo,
    @SerializedName("payment_link") val paymentLinkCreate: PaymentLinkCreate?,
    @SerializedName("upi_intent") val qrPayment: QrPayment?,
    @SerializedName("assortment_type") val assortmentType: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("batch_id") val batchId: String?,
    @SerializedName("is_wallet_payment") val isWalletPayment: Boolean?
)

@Keep
data class RazorPayInfo(
    @SerializedName("order_id") val taxationId: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("email") val email: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("currency") val currency: String?
)

@Keep
data class ServerResponsePayment(
    @SerializedName("status") val status: String?,
    @SerializedName("order_id") val orderId: String?,
    @SerializedName("payment_for") val paymentFor: String?,
    @SerializedName("assortment_type") val assortmentType: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("reason") val reason: String?
)

@Keep
@Parcelize
data class PaymentStartBody(
    @SerializedName("payment_for") var paymentFor: String?,
    @SerializedName("method") var method: String?,
    @SerializedName("payment_info") var paymentStartInfo: PaymentStartInfo?
) : Parcelable

@Keep
@Parcelize
data class PaymentStartInfo(
    @SerializedName("amount") var amount: String?,
    @SerializedName("coupon_code") var couponCode: String?,
    @SerializedName("variant_id") var variantId: String?,
    @SerializedName("payment_for_id") val paymentForId: String? = null,
    @SerializedName("use_wallet_cash") var useWalletCash: Boolean?,
    @SerializedName("selected_wallet") var selectedWallet: List<String>?,
    @SerializedName("use_wallet_reward") var useWalletReward: Boolean?,
    @SerializedName("switch_assortment") var switchAssortmentId: String?
) : Parcelable

@Keep
data class QrPayment(
    @SerializedName("payment_status") val paymentStatus: String,
    @SerializedName("header") val header: String,
    @SerializedName("ttl") val ttl: Long,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("footer_image_url") val footerImageUrl: String?,
    @SerializedName("order_id") val orderId: String,
    @SerializedName("upi_intent_link") val upiIntentLink: String?,
    @SerializedName("assortment_type") val assortmentType: String?,
    @SerializedName("assortment_id") val assortmentId: String?,
    @SerializedName("cta_text") val ctaText: String?,
    @SerializedName("share_message") val shareMessage: String?,
)

@Keep
@Parcelize
data class PaymentActivityBody(
    var paymentStartBody: PaymentStartBody,
    var cardDetails: CardDetails?,
    var method: String,
    var type: String,
    var deeplink: String?,
    var upi: String?,
    var upiPackage: String?
) : Parcelable

@Keep
@Parcelize
data class CardDetails(
    var cardNo: String = "",
    var name: String = "",
    var CVV: String = "",
    var expiry: String = ""
) : Parcelable
