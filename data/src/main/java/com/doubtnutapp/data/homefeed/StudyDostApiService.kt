package com.doubtnutapp.data.homefeed

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StudyDostApiService {

    @POST("v1/studydost/request")
    fun requestForStudyDost(): Single<ApiResponse<ApiOnBoardingStatus.ApiStudyDost>>

    @GET("/v1/studydost/block-room")
    fun areYouBlocked(@Query(value = "room_id") roomId: String): Single<ApiResponse<ApiOnBoardingStatus.ApiStudyDost>>

    @POST("/v1/studydost/block-room")
    fun blockUser(@Body params: RequestBody): Single<ApiResponse<ApiOnBoardingStatus.ApiStudyDost>>
}
