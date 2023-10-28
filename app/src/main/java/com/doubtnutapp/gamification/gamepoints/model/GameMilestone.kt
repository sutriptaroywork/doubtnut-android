package com.doubtnutapp.gamification.gamepoints.model

import androidx.annotation.Keep

@Keep
data class GameMilestone(
        val isAchieved: Boolean,
        val point: String,
        val level: String,
        val isLast: Boolean = false)