package com.doubtnutapp.data.gamification.gamePoints.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiViewLevelInfoItem(
    @SerializedName("is_achieved")val isAchieved: Int = 0,
    @SerializedName("lvl") val level: Int = 0,
    @SerializedName("xp") val xp: Int = 0
)
