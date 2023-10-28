package com.doubtnutapp.domain.gamification.userProfile.entity

import androidx.annotation.Keep

@Keep
data class MyBadgesItemEntity(
    val imageUrl: String = "",
    val name: String = "",
    val description: String = "",
    val recentBadgesId: Int = 0,
    val isAchieved: Boolean = false,
    val blurImage: String
)
