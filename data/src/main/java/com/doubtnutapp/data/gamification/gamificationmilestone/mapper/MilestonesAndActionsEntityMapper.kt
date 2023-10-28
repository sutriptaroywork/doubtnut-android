package com.doubtnutapp.data.gamification.gamificationmilestone.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.gamificationmilestone.model.ApiGameMilestone
import com.doubtnutapp.data.gamification.gamificationmilestone.model.ApiGamePoint
import com.doubtnutapp.data.gamification.gamificationmilestone.model.ApiMilestonesAndActions
import com.doubtnutapp.data.gamification.gamificationmilestone.model.ApiNextAchievableBadge
import com.doubtnutapp.domain.gamification.milestonesandactions.entity.GameActionEntity
import com.doubtnutapp.domain.gamification.milestonesandactions.entity.GameMilestoneEntity
import com.doubtnutapp.domain.gamification.milestonesandactions.entity.MilestonesAndActionsEntity
import com.doubtnutapp.domain.gamification.milestonesandactions.entity.NextAchievableBadgeEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MilestonesAndActionsEntityMapper @Inject constructor() :
    Mapper<ApiMilestonesAndActions, MilestonesAndActionsEntity> {

    override fun map(srcObject: ApiMilestonesAndActions) = with(srcObject) {
        MilestonesAndActionsEntity(
            getMilestoneEntitiesList(gameGameMilestones),
            getActionsEntitiesList(gameGamePoints),
            getNextAchievableBadge(nextAchievableBadge)
        )
    }

    private fun getNextAchievableBadge(nextAchievableBadge: ApiNextAchievableBadge): NextAchievableBadgeEntity {
        return NextAchievableBadgeEntity(
            nextAchievableBadge.description,
            nextAchievableBadge.id,
            nextAchievableBadge.imageUrl,
            nextAchievableBadge.name,
            nextAchievableBadge.nextText
        )
    }

    private fun getActionsEntitiesList(gameGamePoints: List<ApiGamePoint>): List<GameActionEntity> {
        return gameGamePoints.map {
            GameActionEntity(
                it.point,
                it.description
            )
        }
    }

    private fun getMilestoneEntitiesList(gameGameMilestones: List<ApiGameMilestone>): List<GameMilestoneEntity> {
        return gameGameMilestones.map {
            GameMilestoneEntity(
                it.points,
                it.level,
                it.isAchieved == LEVEL_ACHIEVED
            )
        }
    }

    companion object {
        private const val LEVEL_ACHIEVED = 1
    }
}
