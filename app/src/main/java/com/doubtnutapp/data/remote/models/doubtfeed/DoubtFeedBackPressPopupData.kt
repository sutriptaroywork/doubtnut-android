package com.doubtnutapp.data.remote.models.doubtfeed

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 17/5/21.
 */

@Keep
data class DoubtFeedBackPressPopupData(
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("description") val description: String,
    @SerializedName("main_cta") val mainCta: String,
    @SerializedName("main_deeplink") val mainDeeplink: String,
    @SerializedName("secondary_cta") val secondaryCta: String,
)
