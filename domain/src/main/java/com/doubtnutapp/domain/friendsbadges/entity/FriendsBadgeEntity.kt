package com.doubtnutapp.domain.gamification.friendsbadges.entity

import androidx.annotation.Keep

@Keep
data class FriendsBadgeEntity(
    val imageUrl: String,
    val isAchieved: Int,
    val name: String
)
