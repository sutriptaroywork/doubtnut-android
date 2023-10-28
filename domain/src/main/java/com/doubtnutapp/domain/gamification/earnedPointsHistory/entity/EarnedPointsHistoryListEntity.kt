package com.doubtnutapp.domain.gamification.earnedPointsHistory.entity

import androidx.annotation.Keep

@Keep
data class EarnedPointsHistoryListEntity(
    val isActive: Int = 0,
    val activity: String = "",
    val referId: String = "",
    val userId: String = "",
    val xp: Int = 0,
    val createdAt: String = "",
    val action: String = "",
    val id: Int = 0,
    val actionDisplay: String = ""
)
