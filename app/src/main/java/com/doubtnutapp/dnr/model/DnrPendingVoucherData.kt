package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DnrPendingVoucherData(
    @SerializedName("description") val description: String?,
    @SerializedName("cta") val cta: String?,
    @SerializedName("image") val dnrImage: String?,
    @SerializedName("auto_hide_duration") val autoHideDuration: Long?,
    @SerializedName("deeplink") val deeplink: String?,
)
