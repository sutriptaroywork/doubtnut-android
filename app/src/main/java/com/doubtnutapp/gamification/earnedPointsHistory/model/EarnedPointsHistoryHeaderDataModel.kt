package com.doubtnutapp.gamification.earnedPointsHistory.model

import androidx.annotation.Keep

@Keep
data class EarnedPointsHistoryHeaderDataModel(
       val title: String,
       override val viewType: Int
): EarnedPointsBaseFeedViewItem