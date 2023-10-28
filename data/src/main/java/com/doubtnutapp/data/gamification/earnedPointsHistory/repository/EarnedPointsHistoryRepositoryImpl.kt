package com.doubtnutapp.data.gamification.earnedPointsHistory.repository

import com.doubtnutapp.data.gamification.earnedPointsHistory.mapper.EarnedPointsHistoryEntityMapper
import com.doubtnutapp.data.gamification.earnedPointsHistory.service.EarnedPointsHistoryService
import com.doubtnutapp.domain.gamification.earnedPointsHistory.entity.EarnedPointsHistoryListEntity
import com.doubtnutapp.domain.gamification.earnedPointsHistory.repository.EarnedPointsHistoryRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 *  Created by Pradip Awasthi on 2019-10-22.
 */

class EarnedPointsHistoryRepositoryImpl @Inject constructor(
    private val earnedPointsHistoryService: EarnedPointsHistoryService,
    private val earnedPointsHistoryEntityMapper: EarnedPointsHistoryEntityMapper
) : EarnedPointsHistoryRepository {

    override fun getEarnedPointsHistory(): Single<List<EarnedPointsHistoryListEntity>> {
        return earnedPointsHistoryService.getUserMilestonesAndGameActions().map {
            earnedPointsHistoryEntityMapper.map(it.data)
        }
    }
}
