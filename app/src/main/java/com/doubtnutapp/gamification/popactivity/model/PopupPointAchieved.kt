package com.doubtnutapp.gamification.popactivity.model

import androidx.annotation.Keep

@Keep
data class PopupPointAchieved(
        override val viewType: Int,
        override val gravity: Int,
        override val width: Int,
        override val height: Int,
        override val duration: Long,
        val message: String,
        val description: String
) : GamificationPopup