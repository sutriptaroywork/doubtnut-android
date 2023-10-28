package com.doubtnutapp.domain.gamification.milestonesandactions.entity

import androidx.annotation.Keep

@Keep
data class GameMilestoneEntity(
    val points: String,
    val level: String,
    val isAchieved: Boolean
)
