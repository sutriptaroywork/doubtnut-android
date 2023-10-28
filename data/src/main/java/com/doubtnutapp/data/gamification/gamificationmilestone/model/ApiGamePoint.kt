package com.doubtnutapp.data.gamification.gamificationmilestone.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiGamePoint(
    @SerializedName("xp") val point: String,
    @SerializedName("action_display") val description: String
)
