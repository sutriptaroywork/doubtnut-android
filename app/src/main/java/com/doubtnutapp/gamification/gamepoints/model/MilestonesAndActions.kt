package com.doubtnutapp.gamification.gamepoints.model

import androidx.annotation.Keep

@Keep
data class MilestonesAndActions(
        val milestone: List<GameMilestone>,
        val actions: List<GameAction>,
        val nextAchievableBadge: NextAchievableBadge
)