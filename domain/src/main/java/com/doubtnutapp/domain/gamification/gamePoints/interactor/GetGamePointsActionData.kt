package com.doubtnutapp.domain.gamification.gamePoints.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.gamePoints.entity.GamePointsEntity
import com.doubtnutapp.domain.gamification.gamePoints.repository.GamePointsRepository
import io.reactivex.Single
import javax.inject.Inject

class GetGamePointsActionData @Inject constructor(
    private val gamePointsRepository: GamePointsRepository
) : SingleUseCase<GamePointsEntity, Unit> {

    override fun execute(param: Unit): Single<GamePointsEntity> = gamePointsRepository.getMilestoneAndAction()
}
