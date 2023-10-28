package com.doubtnutapp.domain.gamification.earnedPointsHistory.repository

import com.doubtnutapp.domain.gamification.earnedPointsHistory.entity.EarnedPointsHistoryListEntity
import io.reactivex.Single

interface EarnedPointsHistoryRepository {
    fun getEarnedPointsHistory(): Single<List<EarnedPointsHistoryListEntity>>
}
