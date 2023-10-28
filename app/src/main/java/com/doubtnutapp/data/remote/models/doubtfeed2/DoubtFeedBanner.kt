package com.doubtnutapp.data.remote.models.doubtfeed2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 09/06/21.
 */

@Keep
data class DoubtFeedBanner(
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("cta_text") val ctaText: String,
    @SerializedName("deeplink") val deeplink: String,
    @SerializedName("topic") val topic: String,
    @SerializedName("is_show") val isShow: Boolean,
)
