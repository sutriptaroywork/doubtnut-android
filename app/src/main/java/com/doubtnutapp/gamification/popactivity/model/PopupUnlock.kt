package com.doubtnutapp.gamification.popactivity.model

import androidx.annotation.Keep

@Keep
data class PopupUnlock(
        override val viewType: Int,
        override val gravity: Int,
        override val width: Int,
        override val height: Int,
        override val duration: Long,
        val description: String,
        val message: String,
        val imageUrl: String,
        val actionData: PopUnlockActionData
) : GamificationPopup

@Keep
data class PopUnlockActionData(
        val type: String,
        val id: String,
        val title: String,
        val button_text: String
)