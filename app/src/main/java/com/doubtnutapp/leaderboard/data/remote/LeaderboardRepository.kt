package com.doubtnutapp.leaderboard.data.remote

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.leaderboard.data.entity.LeaderboardData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LeaderboardRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun getTestLeaderboardData(
        source: String,
        assortmentId: String?,
        testId: String?,
        type: String?,
    )
            : Flow<LeaderboardData> =
        flow {
            emit(
                networkService.getTestLeaderboardData(source, assortmentId, testId, type).data
            )
        }

    fun getPaidUserChampionshipLeaderboard(
        source: String,
        assortmentId: String?,
        testId: String?,
        type: String?,
    )
            : Flow<LeaderboardData> =
        flow {
            emit(
                networkService.getPaidUserChampionshipLeaderboard(
                    source,
                    assortmentId,
                    testId,
                    type
                ).data
            )
        }

}
