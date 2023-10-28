package com.doubtnutapp.domain.gamification.milestonesandactions.entity

import androidx.annotation.Keep

@Keep
data class NextAchievableBadgeEntity(
    val description: String,
    val id: Int,
    val imageUrl: String,
    val name: String,
    val nextText: String
)
