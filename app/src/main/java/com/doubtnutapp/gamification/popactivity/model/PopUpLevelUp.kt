package com.doubtnutapp.gamification.popactivity.model

import androidx.annotation.Keep

@Keep
data class PopUpLevelUp(
        override val viewType: Int,
        override val gravity: Int,
        override val width: Int,
        override val height: Int,
        override val duration: Long,
        val description: String,
        val message: String,
        val imageUrl: String
) : GamificationPopup