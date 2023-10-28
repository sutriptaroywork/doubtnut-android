package com.doubtnutapp.data.gamification.earnedPointsHistory.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiEarnedPointsHistoryList(
    @SerializedName("is_active") val isActive: Int = 0,
    @SerializedName("activity") val activity: String = "",
    @SerializedName("refer_id") val referId: String = "",
    @SerializedName("user_id") val userId: String = "",
    @SerializedName("xp") val xp: Int = 0,
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("action") val action: String = "",
    @SerializedName("id") val id: Int = 0,
    @SerializedName("action_display") val actionDisplay: String = ""
)
