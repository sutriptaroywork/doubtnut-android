package com.doubtnutapp.newlibrary.repository

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearFilterResponse
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearPapers
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearSelectionResponse
import com.doubtnutapp.newlibrary.service.PreviousPapersService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 26/11/21
 */

class LibraryPreviousYearPapersRepository @Inject constructor(
    private val service: PreviousPapersService
) {

    fun getPreviousPapersTabsAndFilterData(examId: String): Flow<ApiResponse<LibraryPreviousYearPapers>> =
        flow { emit(service.getPreviousPapersTabsAndFilterData(examId)) }

    fun getPreviousPapersYearData(
        examId: String,
        tabId: String,
        filterId: String,
        filterDataType: String,
        filterText: String?
    ): Flow<ApiResponse<LibraryPreviousYearFilterResponse>> =
        flow { emit(service.getYearData(
            examId = examId,
            tabId = tabId,
            filterId = filterId,
            filterDataType = filterDataType,
            filterText = filterText
        )) }

    fun getPreviousPapersSelectionData(
        examId: String,
        tabId: String,
        id: String?,
        filterId: String,
        filterDataType: String,
        filterText: String?
    ): Flow<ApiResponse<LibraryPreviousYearSelectionResponse>> =
        flow {
            emit(
                service.getSelectionData(
                    examId = examId,
                    tabId = tabId,
                    id = id,
                    filterId = filterId,
                    filterDataType = filterDataType,
                    filterText = filterText
                )
            )
        }
}