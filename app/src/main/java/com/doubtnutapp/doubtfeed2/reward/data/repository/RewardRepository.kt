package com.doubtnutapp.doubtfeed2.reward.data.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.doubtfeed2.reward.data.model.RewardDetails
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RewardRepository @Inject constructor(private val networkService: NetworkService) {

    fun getRewardDetails(): Flow<ApiResponse<RewardDetails>> =
        flow {
            emit(networkService.getDfRewardDetails())
        }

    fun markRewardScratched(level: Int): Flow<ApiResponse<Unit>> =
        flow {
            emit(networkService.markDfRewardScratched(level))
        }
}
