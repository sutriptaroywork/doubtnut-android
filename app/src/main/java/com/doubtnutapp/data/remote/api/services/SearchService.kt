package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.InAppLastPlayedVideo
import com.doubtnutapp.newglobalsearch.model.ApiSearchAdvanceFilterResponse
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SearchService {

    @GET("v3/search/get-animation")
    fun getHintAnimationStrings(): Single<ApiResponse<Map<String, List<String>?>>>

    @GET("v3/search/dialogue-hints")
    fun getSearchHints(): Single<ApiResponse<Map<String, List<String>?>>>

    @GET("v3/search/last-watch-video")
    fun getLastPlayedVideoInfo(): RetrofitLiveData<ApiResponse<ArrayList<InAppLastPlayedVideo>>>

    @POST("/v5/search/get-ias-feedback")
    fun getAdvanceSearchFilters(@Body requestBody: RequestBody): Single<ApiResponse<ApiSearchAdvanceFilterResponse>>
}
