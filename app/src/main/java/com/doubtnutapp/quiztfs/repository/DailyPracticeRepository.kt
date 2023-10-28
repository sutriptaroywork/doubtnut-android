package com.doubtnutapp.quiztfs.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.quiztfs.DailyPracticeData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 02-09-2021
 */
class DailyPracticeRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun getDailyPracticeData(type: String): Flow<ApiResponse<DailyPracticeData>> =
        flow { emit(networkService.getDailyPracticeData(type)) }
}