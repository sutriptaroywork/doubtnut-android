package com.doubtnutapp.domain.gamification.earnedPointsHistory.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.gamification.earnedPointsHistory.entity.EarnedPointsHistoryListEntity
import com.doubtnutapp.domain.gamification.earnedPointsHistory.repository.EarnedPointsHistoryRepository
import io.reactivex.Single
import javax.inject.Inject

class GetEarnedPointsHistoryList @Inject constructor(
    private val earnedPointsHistoryRepository: EarnedPointsHistoryRepository
) : SingleUseCase<List<EarnedPointsHistoryListEntity>, Unit> {

    override fun execute(param: Unit): Single<List<EarnedPointsHistoryListEntity>> = earnedPointsHistoryRepository.getEarnedPointsHistory()
}
