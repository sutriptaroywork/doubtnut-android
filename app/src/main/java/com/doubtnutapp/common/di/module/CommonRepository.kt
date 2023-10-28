package com.doubtnutapp.common.di.module

import com.doubtnutapp.common.data.BookCallData
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.freeclasses.bottomsheets.FilterListBottomSheetDialogFragment
import com.doubtnutapp.freeclasses.bottomsheets.FilterListData
import com.doubtnutapp.toHashMapOfStringVString
import com.doubtnutapp.toRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CommonRepository @Inject constructor(
    private val networkService: NetworkService
) {
    fun getBookCallData(): Flow<BookCallData> =
        flow {
            emit(
                networkService.getBookCallData().data
            )
        }

    fun bookCall(
        dateId: String,
        timeId: String
    ): Flow<BaseResponse> =
        flow {
            emit(
                networkService.bookCall(dateId, timeId).data
            )
        }

    fun getBottomSheetData(
        source: String?
    ): Flow<BaseResponse> =
        flow {
            emit(
                networkService.getBottomSheetData(source).data
            )
        }

    fun getFilterBottomSheetData(
        type: String,
        assortmentId: String,
        filters: Map<String, List<String>>
    ): Flow<FilterListData> {
        return flow { emit(networkService.getFilterBottomSheetData(type,assortmentId,filters.toHashMapOfStringVString()).data) }
    }

}
