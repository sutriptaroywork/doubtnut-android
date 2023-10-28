package com.doubtnutapp.data.gamification.gamificationmilestone.repository

import com.doubtnutapp.data.gamification.gamificationmilestone.mapper.MilestonesAndActionsEntityMapper
import com.doubtnutapp.domain.gamification.milestonesandactions.entity.MilestonesAndActionsEntity
import com.doubtnutapp.domain.gamification.milestonesandactions.repository.MilestoneAndActionRepository
import io.reactivex.Single
import javax.inject.Inject

class MilestoneAndActionRepositoryImpl @Inject constructor(
    private val milestoneAndActionService: MilestoneAndActionService,
    private val milestonesAndActionsEntityMapper: MilestonesAndActionsEntityMapper
) : MilestoneAndActionRepository {

    override fun getMilestoneAndAction(): Single<MilestonesAndActionsEntity> {
        return milestoneAndActionService.getUserMilestonesAndGameActions().map {
            milestonesAndActionsEntityMapper.map(it.data)
        }
    }
}
