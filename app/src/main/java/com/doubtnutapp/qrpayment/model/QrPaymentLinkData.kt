package com.doubtnutapp.qrpayment.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 11/11/20.
 */
@Keep
@Parcelize
data class QrPaymentLinkData(
        @SerializedName("amount") val amount: String?,
        @SerializedName("coupon_code") val coupon: String?,
        @SerializedName("variant_id") val variantId: String?,
        @SerializedName("payment_for") val paymentFor: String?,
        @SerializedName("payment_for_id") val paymentForId: String?,
        @SerializedName("total_amount") val totalAmount: String?,
        @SerializedName("discount") val discount: String?,
        @SerializedName("wallet_amount") val walletAmount: String,
        @SerializedName("use_wallet") val useWallet: Boolean?,
        @SerializedName("pay_using_wallet") val payUsingWallet: Boolean?,
        @SerializedName("final_amount_with_wallet") val finalAmountWithWallet: String?,
        @SerializedName("method") val method: String,
        @SerializedName("source") val source: String
) : Parcelable