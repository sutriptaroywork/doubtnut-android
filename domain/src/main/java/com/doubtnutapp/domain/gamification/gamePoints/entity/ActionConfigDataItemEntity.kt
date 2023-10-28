package com.doubtnutapp.domain.gamification.gamePoints.entity

import androidx.annotation.Keep

@Keep
data class ActionConfigDataItemEntity(
    val isActive: Int = 0,
    val xp: Int = 0,
    val action: String = "",
    val createdAt: String = "",
    val id: Int = 0,
    val actionDisplay: String = "",
    val actionPage: String = ""
)
