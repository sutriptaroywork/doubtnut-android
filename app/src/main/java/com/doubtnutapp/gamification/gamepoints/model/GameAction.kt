package com.doubtnutapp.gamification.gamepoints.model

import androidx.annotation.Keep

@Keep
data class GameAction(
        val point: String,
        val description: String)