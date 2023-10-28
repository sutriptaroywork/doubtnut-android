package com.doubtnutapp.domain.gamification.leaderboard.entity

import androidx.annotation.Keep

@Keep
data class LeaderboardEntity(
    val allLeaderboardData: List<GameLeaderEntity>,
    val dailyLeaderboardData: List<GameLeaderEntity>
)
