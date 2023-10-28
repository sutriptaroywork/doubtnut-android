package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.PublicUser
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StudentsService {

    @POST("v3/student/add-public-user-web")
    fun addPublicUser(@Body params: RequestBody): RetrofitLiveData<ApiResponse<PublicUser>>

    @POST("v4/student/update-profile")
    fun updateUserProfileFromUpdate(@Body params: RequestBody): Single<ApiResponse<ResponseBody>>

    @POST("v2/student/browse")
    fun updateClassCourse(@Body params: RequestBody): RetrofitLiveData<ApiResponse<ResponseBody>>

    @POST("v4/student/update-profile")
    fun updateUserProfile(@Body params: RequestBody): RetrofitLiveData<ApiResponse<ResponseBody>>

    @POST("v1/student/store-on-board-language")
    fun storeOnBoardLanguage(@Body params: RequestBody): Completable

    @GET("v2/student/logout")
    fun logout(): RetrofitLiveData<ApiResponse<ResponseBody>>

    @GET("v2/student/recreate-token/{student_id}")
    fun refreshtoken(
        @Path(value = "student_id") student_id: String
    ): RetrofitLiveData<ApiResponse<String>>

    @GET("v2/student/recreate-token/{student_id}")
    fun refreshTokenSingle(
        @Path(value = "student_id") student_id: String
    ): Single<ApiResponse<String>>

    @POST("v2/student/onboard")
    fun userDetailsData(@Body params: RequestBody): Completable

    @POST("v2/student/store-appdata")
    fun getAllInstalledApps(@Body params: RequestBody): Completable

    @POST("/v2/student/pre-login-onboard")
    fun sendPreLoginOnboardData(@Body params: RequestBody): Completable
}
