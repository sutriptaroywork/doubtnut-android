package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.SnackBarData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SnackBarRepositoryImpl @Inject constructor(private val networkService: NetworkService) {

    fun getSnackBarData(
        source: String? = null,
        page: Int,
        assortmentId: String? = null,
        qid: String? = null
    ): Flow<ApiResponse<SnackBarData?>> {
        return flow {
            emit(
                networkService.getSnackBar(
                    source = source,
                    page = page,
                    assortmentId = assortmentId,
                    qid = qid
                )
            )
        }
    }
}
