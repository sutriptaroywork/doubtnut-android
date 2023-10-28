package com.doubtnutapp.gamification.popactivity.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

@Keep
interface GamificationPopup : RecyclerViewItem {
    val gravity: Int
    val width: Int
    val height: Int
    val duration: Long
}