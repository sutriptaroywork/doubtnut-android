package com.doubtnutapp.resultpage.repository

import com.doubtnut.core.data.remote.CoreResponse
import com.doubtnutapp.resultpage.model.ResultPageData
import retrofit2.http.GET
import retrofit2.http.Query

interface ResultPageService {

    @GET("v1/result/getresult")
    suspend fun getResultPageData(
        @Query("page")
        page: String?,
        @Query("type")
        type: String?,
        @Query("source")
        source: String?
    ): CoreResponse<ResultPageData>

}