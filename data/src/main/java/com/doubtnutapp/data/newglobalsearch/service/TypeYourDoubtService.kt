package com.doubtnutapp.data.newglobalsearch.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.newglobalsearch.model.ApiSuggestionData
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface TypeYourDoubtService {

    @POST("/v5/search/autoSuggest")
    fun getTydSuggestions(@Body requestBody: RequestBody): Single<ApiResponse<ApiSuggestionData>>

    @POST("/v3/search/insertLog")
    fun postUserSearchData(@Body requestBody: RequestBody): Completable
}
