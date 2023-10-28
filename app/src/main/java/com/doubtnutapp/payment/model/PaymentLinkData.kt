package com.doubtnutapp.payment.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by Anand Gaurav on 11/11/20.
 */
@Keep
@Parcelize
data class PaymentLinkData(
        val amount: String,
        val coupon: String,
        val variantId: String,
        val paymentFor: String,
        val paymentForId: String,
        val totalAmount: String?,
        val discount: String,
        val title: String?,
        val actionButtonText: String?,
        val description: List<String>?,
        val walletAmount: String,
        val useWallet: Boolean?,
        val payUsingWallet: Boolean?,
        val finalAmountWithWallet: String?
) : Parcelable