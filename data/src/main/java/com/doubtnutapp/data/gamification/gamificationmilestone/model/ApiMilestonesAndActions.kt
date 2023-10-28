package com.doubtnutapp.data.gamification.gamificationmilestone.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiMilestonesAndActions(
    @SerializedName("lvl_progress") val gameGameMilestones: List<ApiGameMilestone>,
    @SerializedName("lvl_action") val gameGamePoints: List<ApiGamePoint>,
    @SerializedName("next_badge") val nextAchievableBadge: ApiNextAchievableBadge
)
