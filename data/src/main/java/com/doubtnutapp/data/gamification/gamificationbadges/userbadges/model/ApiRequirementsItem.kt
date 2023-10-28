package com.doubtnutapp.data.gamification.gamificationbadges.userbadges.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiRequirementsItem(
    @SerializedName("requirement_type") val requirementType: String = "",
    @SerializedName("fullfilled") val fullfilled: Int = 0,
    @SerializedName("fullfilled_percent") val fullfilledPercent: Int = 0,
    @SerializedName("requirement") val requirement: Int = 0
)
