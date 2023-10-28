package com.doubtnutapp.data.newglobalsearch.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.newglobalsearch.model.ApiSearchRequest
import com.doubtnutapp.data.newglobalsearch.model.NewApiUserSearchData
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

interface InAppSearchMicroApiService {

    @POST("/api/search/matches")
    fun getNewGlobalSearchResult(@Body apiSearchRequest: ApiSearchRequest):
        Single<ApiResponse<NewApiUserSearchData>>
}
