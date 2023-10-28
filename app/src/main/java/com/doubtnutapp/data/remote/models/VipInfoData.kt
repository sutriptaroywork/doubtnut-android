package com.doubtnutapp.data.remote.models

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class VipInfoData(
    @SerializedName("isVip") val isVip: Boolean,
    @SerializedName("isTrial") val isTrial: Boolean,
    @SerializedName("isTrialExpired") val isTrialExpired: Boolean,
    @SerializedName("purchaseCount") val purchaseCount: Int,
    @SerializedName("purchased_assortments") val purchasedAssortmentIds: List<String>?,
    @SerializedName("active_course") val isCourseActive: Boolean?
)
