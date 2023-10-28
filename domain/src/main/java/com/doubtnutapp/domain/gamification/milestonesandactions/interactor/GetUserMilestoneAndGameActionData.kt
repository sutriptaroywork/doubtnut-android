package com.doubtnutapp.domain.gamification.milestonesandactions.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.milestonesandactions.entity.MilestonesAndActionsEntity
import com.doubtnutapp.domain.gamification.milestonesandactions.repository.MilestoneAndActionRepository
import io.reactivex.Single
import javax.inject.Inject

class GetUserMilestoneAndGameActionData @Inject constructor(
    private val milestoneAndActionRepository: MilestoneAndActionRepository
) : SingleUseCase<MilestonesAndActionsEntity, Unit> {

    override fun execute(param: Unit): Single<MilestonesAndActionsEntity> = milestoneAndActionRepository.getMilestoneAndAction()
}
