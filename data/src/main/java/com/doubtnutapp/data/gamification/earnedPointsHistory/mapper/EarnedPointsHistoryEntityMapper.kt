package com.doubtnutapp.data.gamification.earnedPointsHistory.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.gamification.earnedPointsHistory.model.ApiEarnedPointsHistoryList
import com.doubtnutapp.domain.gamification.earnedPointsHistory.entity.EarnedPointsHistoryListEntity
import javax.inject.Inject
import javax.inject.Singleton

/**
 *  Created by Pradip Awasthi on 2019-10-22.
 */
@Singleton
class EarnedPointsHistoryEntityMapper @Inject constructor() : Mapper<List<ApiEarnedPointsHistoryList>, List<EarnedPointsHistoryListEntity>> {

    override fun map(srcObject: List<ApiEarnedPointsHistoryList>): List<EarnedPointsHistoryListEntity> = srcObject.map {
        EarnedPointsHistoryListEntity(
            it.isActive,
            it.activity,
            it.referId,
            it.userId,
            it.xp,
            it.createdAt,
            it.action,
            it.id,
            it.actionDisplay
        )
    }
}
