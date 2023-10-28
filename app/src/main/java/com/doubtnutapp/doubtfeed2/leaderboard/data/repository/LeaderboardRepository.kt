package com.doubtnutapp.doubtfeed2.leaderboard.data.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.Leaderboard
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by devansh on 12/07/21.
 */

class LeaderboardRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun getLeaderboard(): Flow<ApiResponse<Leaderboard>> {
        return flow { emit(networkService.getDfLeaderboard()) }
    }

    fun getLeaderboardList(id: Int, page: Int): Flow<ApiResponse<Leaderboard>> {
        val requestBody = mapOf(
            "id" to id,
            "page" to page
        ).toRequestBody()
        return flow { emit(networkService.getDfLeaderboardList(requestBody)) }
    }
}
