package com.doubtnutapp.domain.gamification.milestonesandactions.entity

import androidx.annotation.Keep

@Keep
data class GameActionEntity(
    val point: String,
    val description: String
)
