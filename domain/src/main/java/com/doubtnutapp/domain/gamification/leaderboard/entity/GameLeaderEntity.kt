package com.doubtnutapp.domain.gamification.leaderboard.entity

import androidx.annotation.Keep

@Keep
data class GameLeaderEntity(
    val profileImage: String?,
    val rank: Int,
    val userId: Int,
    val userName: String,
    val points: String,
    val isOwn: Boolean = false
)
