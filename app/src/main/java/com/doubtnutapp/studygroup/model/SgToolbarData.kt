package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class SgToolbarData(
        @SerializedName("title") val title: String?,
        @SerializedName("cta") val cta: TopCta?,
)

@Keep
data class TopCta(
        @SerializedName("title") val title: String?,
        @SerializedName("image") val image: String?,
        @SerializedName("deeplink") val deeplink: String?,
)