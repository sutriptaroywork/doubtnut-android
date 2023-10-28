package com.doubtnutapp.data.pcmunlockpopup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiPCMUnlockData(
    @SerializedName("badge_required")
    val apiBadgeRequired: List<ApiBadgeRequired>,
    @SerializedName("footer_text")
    val footerText: String,
    @SerializedName("heading")
    val heading: String,
    @SerializedName("subheading")
    val subheading: String,
    @SerializedName("user_images")
    val userImages: List<String>
)
