package com.doubtnutapp.domain.gamification.milestonesandactions.entity

import androidx.annotation.Keep

@Keep
data class MilestonesAndActionsEntity(
    val gameMilestones: List<GameMilestoneEntity>,
    val gameActions: List<GameActionEntity>,
    val nextAchievableBadge: NextAchievableBadgeEntity
)
