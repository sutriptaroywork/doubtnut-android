package com.doubtnutapp.dnr.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class DnrToolbarData(
    @SerializedName("title") val title: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("dnr") val dnr: String?,
    @SerializedName("dnr_image") val dnrImage: String? = null,
)
