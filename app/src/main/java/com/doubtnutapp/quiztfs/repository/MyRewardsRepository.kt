package com.doubtnutapp.quiztfs.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.quiztfs.MyRewardsData
import com.doubtnutapp.quiztfs.widgets.DialogData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 07-09-2021
 */
class MyRewardsRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun fetchRewards(page: Int, type: String): Flow<ApiResponse<MyRewardsData>> =
        flow { emit(networkService.getRewardsData(page, type)) }

    fun submitScratchStatus(couponCode: Int?): Flow<ApiResponse<DialogData>> =
        flow { emit(networkService.submitScratchStatus(couponCode)) }
}