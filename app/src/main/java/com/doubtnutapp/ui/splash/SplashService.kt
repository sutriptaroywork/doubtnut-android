package com.doubtnutapp.ui.splash

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.ui.onboarding.model.ApiLoginTimer
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SplashService {

    @GET("/v1/config/onboard")
    fun getCameraScreenConfig(
        @Query(value = "student_id") id: String?,
        @Query("gaid") gaid: String
    ): Single<ApiResponse<HashMap<String, Any>>>

    @GET("/v1/config/onboard")
    fun getCameraScreenConfig(@Query("gaid") gaid: String): Single<ApiResponse<HashMap<String, Any>>>

    @POST("/v2/gamification/updatedailystreak")
    fun registerDailyStreakEvent(): Completable

    @GET("/v1/student/get-onboarding-status")
    fun getOnBoardingStatus(): Single<ApiResponse<ApiOnBoardingStatus>>

    @GET("v1/student/get-login-timer")
    fun getLoginTimer(@Query(value = "udid") udid: String?): Single<ApiResponse<ApiLoginTimer>>
}
