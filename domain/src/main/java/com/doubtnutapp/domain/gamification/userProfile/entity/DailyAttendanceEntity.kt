package com.doubtnutapp.domain.gamification.userProfile.entity

import androidx.annotation.Keep

@Keep
data class DailyAttendanceEntity(
    val isAchieved: Int = 0,
    val itemIcon: String = "",
    val itemTitle: String = "",
    val badgeType: String,
    val points: Int = 0
)
