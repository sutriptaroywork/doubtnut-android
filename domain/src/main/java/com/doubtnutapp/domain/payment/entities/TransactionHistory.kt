package com.doubtnutapp.domain.payment.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2020-01-20.
 */
@Keep
data class TransactionHistoryItem(
    @SerializedName("date") val date: String?,
    @SerializedName("amount") val amount: String?,
    @SerializedName("amount_color") val amountColor: String?,
    @SerializedName("payment_for") val paymentFor: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("order_id") val orderId: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("partner_txn_id") val partnerTxnId: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("btn1_text") val btn1Text: String?,
    @SerializedName("btn2_text") val btn2Text: String?,
    @SerializedName("btn1_deeplink") val btn1Deeplink: String?,
    @SerializedName("btn2_deeplink") val btn2Deeplink: String?,
    @SerializedName("invoice_url") val invoiceUrl: String?,
    @SerializedName("pdf_url") val pdfUrl: String?,
    @SerializedName("expire_text") val expireText: String?,
    @SerializedName("currency_symbol") val currencySymbol: String?
)
