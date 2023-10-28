package com.doubtnutapp.data.gamification.leaderboard.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiLeaderboardData(
    @SerializedName("all_leaderboard_data") val allLeaderboardData: List<ApiGameLeader>,
    @SerializedName("daily_leaderboard_data") val dailyLeaderboardData: List<ApiGameLeader>
)
