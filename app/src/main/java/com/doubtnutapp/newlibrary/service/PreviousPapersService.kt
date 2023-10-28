package com.doubtnutapp.newlibrary.service

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearFilterResponse
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearPapers
import com.doubtnutapp.newlibrary.model.LibraryPreviousYearSelectionResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Created by Mehul Bisht on 26/11/21
 */

interface PreviousPapersService {

    @GET("v9/library/previous-years-papers")
    suspend fun getPreviousPapersTabsAndFilterData(@Query("exam_id") examId: String): ApiResponse<LibraryPreviousYearPapers>

    @GET("v9/library/previous-years-papers/filter-data")
    suspend fun getYearData(
        @Query("exam_id") examId: String,
        @Query("tab_id") tabId: String,
        @Query("filter_id") filterId: String,
        @Query("filter_data_type") filterDataType: String,
        @Query("filter_text") filterText: String?
    ): ApiResponse<LibraryPreviousYearFilterResponse>

    @GET("v9/library/previous-years-papers/selection-data")
    suspend fun getSelectionData(
        @Query("exam_id") examId: String,
        @Query("tab_id") tabId: String,
        @Query("id") id: String?,
        @Query("filter_id") filterId: String,
        @Query("filter_data_type") filterDataType: String,
        @Query("filter_text") filterText: String?
    ): ApiResponse<LibraryPreviousYearSelectionResponse>
}