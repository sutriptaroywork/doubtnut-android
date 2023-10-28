package com.doubtnutapp.domain.gamification.gamePoints.entity

import androidx.annotation.Keep

@Keep
data class ViewLevelInfoItemEntity(
    val isAchieved: Int = 0,
    val lvl: Int = 0,
    val xp: Int = 0
)
