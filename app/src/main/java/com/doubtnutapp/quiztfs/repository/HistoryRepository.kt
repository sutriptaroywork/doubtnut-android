package com.doubtnutapp.quiztfs.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.quiztfs.HistoryData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 08-09-2021
 */
class HistoryRepository @Inject constructor (
    private val networkService: NetworkService
) {

    fun getHistory(page: Int) : Flow<ApiResponse<HistoryData>> =
        flow { emit(networkService.getHistoryData(page)) }
}