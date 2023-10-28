package com.doubtnutapp.data.remote.models.topicboostergame2

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by devansh on 17/06/21.
 */

@Keep
data class LevelData(
    @SerializedName("levels") val levels: List<Level>,
    @SerializedName("completed_text") val completedText: String?,
)

@Keep
data class Level(
    @SerializedName("title") val title: String,
    @SerializedName("info") val info: String,
    @SerializedName("is_locked") val isLocked: Boolean,
    @SerializedName("coupon_code") val couponCode: String?,
    @SerializedName("cta_text") val ctaText: String?,
    @SerializedName("cta_deeplink") val ctaDeeplink: String?,
    var isInfoVisible: Boolean = false,
)
