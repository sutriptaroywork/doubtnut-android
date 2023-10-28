package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VoucherData(
    @SerializedName("toolbar_data") val toolbarData: DnrToolbarData?,
    @SerializedName("title") val title: String,
    @SerializedName("dnr") val dnr: Int,
    @SerializedName("dnr_image") val dnrImage: String?,
    @SerializedName("pending_voucher_deeplink") val pendingVoucherDeeplink: String?,
    @SerializedName("tabs") val tabs: List<Tab>?,
    @SerializedName("active_tab") val activeTabId: Int,
)
