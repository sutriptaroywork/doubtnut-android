package com.doubtnutapp.domain.gamification.userProfile.entity

import androidx.annotation.Keep

@Keep
data class DailyLeaderboardItemEntity(
    val leaderBoardProfileImage: String = "",
    val leaderBoardUserId: Int = 0,
    val leaderBoardUserName: String = "",
    val leaderBoardRank: Int = 0,
    val leaderBoardPoints: String = "",
    val leaderBoardIsOwn: Int = 0
)
