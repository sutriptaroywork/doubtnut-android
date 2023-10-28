package com.doubtnutapp.gamification.popactivity.model

import androidx.annotation.Keep

@Keep
data class PopupAlert(
        override val viewType: Int,
        override val gravity: Int,
        override val width: Int,
        override val height: Int,
        override val duration: Long,
        val description: List<String>,
        val message: String,
        val imageUrl: String,
        val buttonText: String
) : GamificationPopup