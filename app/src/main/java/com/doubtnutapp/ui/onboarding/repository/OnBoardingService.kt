package com.doubtnutapp.ui.onboarding.repository

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingSteps
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface OnBoardingService {

    @GET("v1/student/get-onboarding-status")
    fun getOnBoardingStatus(): Single<ApiResponse<ApiOnBoardingStatus>>

    @GET("v1/student/get-class-language")
    fun getClassAndLanguage(): Single<ApiResponse<ApiOnBoardingStatus>>

    @GET("v5/student/get-student-onboarding")
    fun getOnBoardingSteps(
        @Query("type") type: String?,
        @Query("code") code: String?,
        @Query("variant") variant: Int,
        @Query("langCode") languageCode: String
    ): Single<ApiResponse<ApiOnBoardingSteps>>

    @POST("v5/student/post-student-onboarding")
    fun postStudentOnBoardingSteps(@Body requestBody: RequestBody): Single<ApiResponse<ApiOnBoardingSteps>>
}
