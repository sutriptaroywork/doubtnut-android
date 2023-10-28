package com.doubtnutapp.domain.gamification.dailystreakbadge.entity

/**
 * Created by shrreya on 28/6/19.
 */
import androidx.annotation.Keep

@Keep
data class DailyStreakEntity(
    val description: String,
    val id: Int,
    val imageUrl: String?,
    val isAchieved: Int,
    val name: String,
    val sharingMessage: String
)
