package com.doubtnutapp.data.gamification.mybio.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.domain.gamification.mybio.entity.ApiUserBio
import com.doubtnutapp.domain.gamification.mybio.entity.StudentClassEntity
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserBioService {

    @GET("/v2/student/{userId}/profile")
    fun getUserBio(@Path("userId") userId: String): Single<ApiResponse<ApiUserBio>>

    @POST("/v2/student/{userId}/profile")
    fun postUserBio(@Path("userId") userId: String, @Body body: JsonObject): Single<ApiResponse<Any>>

    @GET("v4/class/get-list/{lng_code}")
    fun getClassesWithSSC(@Path("lng_code") languageCode: String): Single<ApiResponse<ArrayList<StudentClassEntity>>>
}
