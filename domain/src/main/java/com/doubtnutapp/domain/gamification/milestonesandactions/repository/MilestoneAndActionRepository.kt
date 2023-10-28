package com.doubtnutapp.domain.gamification.milestonesandactions.repository

import com.doubtnutapp.domain.gamification.milestonesandactions.entity.MilestonesAndActionsEntity
import io.reactivex.Single

interface MilestoneAndActionRepository {

    fun getMilestoneAndAction(): Single<MilestonesAndActionsEntity>
}
