package com.doubtnutapp.data.newglobalsearch.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.newglobalsearch.model.ApiSuggestionData
import com.doubtnutapp.data.newglobalsearch.model.ApiTrendingSearchDataListItem
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface InAppSearchService {

    @POST("/v5/search/getSuggestions")
    fun getTrendingSearchResult(@Body requestBody: RequestBody): Single<ApiResponse<List<ApiTrendingSearchDataListItem>>>

    @POST("/v5/search/autoSuggest")
    fun getSearchSuggestions(@Body requestBody: RequestBody): Single<ApiResponse<ApiSuggestionData>>

    @POST("/v3/search/insertLog")
    fun postUserSearchData(@Body requestBody: RequestBody): Completable

    @POST("/v3/search/add-ias-suggestion-logs")
    fun postUserSearchSuggestionsData(@Body requestBody: RequestBody): Completable
}
