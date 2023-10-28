package com.doubtnutapp.gamification.gamepoints.model

import androidx.annotation.Keep

@Keep
data class NextAchievableBadge(
        val description: String,
        val id: Int,
        val imageUrl: String,
        val name: String,
        val nextText: String
)