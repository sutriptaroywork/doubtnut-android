package com.doubtnutapp.data.gamification.gamePoints.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiActionConfigDataItem(
    @SerializedName("is_active") val isActive: Int = 0,
    @SerializedName("xp") val xp: Int = 0,
    @SerializedName("action") val action: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("id") val id: Int = 0,
    @SerializedName("action_display") val actionDisplay: String = "",
    @SerializedName("action_page") val actionPage: String = ""
)
