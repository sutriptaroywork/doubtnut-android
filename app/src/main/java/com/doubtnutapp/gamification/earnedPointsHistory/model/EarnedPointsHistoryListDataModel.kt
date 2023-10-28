package com.doubtnutapp.gamification.earnedPointsHistory.model

import androidx.annotation.Keep

@Keep
data class EarnedPointsHistoryListDataModel(
        val isActive: Int = 0,
        val activity: String = "",
        val referId: String = "",
        val userId: String = "",
        val xp: Int = 0,
        val createdAt: String = "",
        val action: String = "",
        val id: Int = 0,
        val actionDisplay: String = "",
        override val viewType: Int
): EarnedPointsBaseFeedViewItem