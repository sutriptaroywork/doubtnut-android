package com.doubtnutapp.data.gamification.gamificationmilestone.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiGameMilestone(
    @SerializedName("xp") val points: String,
    @SerializedName("lvl") val level: String,
    @SerializedName("is_achieved") val isAchieved: Int
)
