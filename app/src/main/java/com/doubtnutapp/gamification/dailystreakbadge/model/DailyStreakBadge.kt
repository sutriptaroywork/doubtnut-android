package com.doubtnutapp.gamification.dailystreakbadge.model

import androidx.annotation.Keep

@Keep
data class DailyStreakBadge(
        val featureType:String,
        val description: String,
        val id: Int,
        val imageUrl: String?,
        val isAchieved: Boolean,
        val name: String,
        val sharingMessage: String

)