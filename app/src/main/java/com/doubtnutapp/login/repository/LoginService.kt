package com.doubtnutapp.login.repository

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.login.model.ApiGetOTP
import com.doubtnutapp.login.model.ApiVerifyUser
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.login.model.LoginPinEntity
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/**
 * Created by Anand Gaurav on 2019-08-13.
 */
interface LoginService {

    @POST("v2/student/truecaller-login")
    fun verifyTrueCallerLogin(@Body params: RequestBody): Single<ApiResponse<ApiVerifyUser>>

    @POST("v4/student/login")
    fun requestOTP(@Body params: RequestBody): Single<ApiResponse<ApiGetOTP>>

    @POST("v4/student/verify")
    fun verifyOTP(@Body params: RequestBody): Single<ApiResponse<ApiVerifyUser>>

    @POST("v2/student/login-with-firebase")
    fun loginWithFirebase(@Body params: RequestBody): Single<ApiResponse<ApiVerifyUser>>

    @POST("v2/student/whatsapp-login-two")
    fun verifyWhatsAppLogin(@Body params: RequestBody): Single<ApiResponse<ApiVerifyUser>>

    @POST("v4/student/update-profile")
    fun updateFcmToken(@Body params: RequestBody): Completable

    @POST("/v2/student/pre-login-onboard")
    fun sendPreLoginOnboardData(@Body params: RequestBody): Completable

    @GET("v1/student/get-onboarding-status")
    fun getOnBoardingStatus(): Single<ApiResponse<ApiOnBoardingStatus>>

    @POST("/v1/student/store-pin")
    fun storePin(@Body params: RequestBody): Single<ApiResponse<LoginPinEntity>>

    @POST("v4/student/add-public-user-app")
    fun addPublicUserApp(@Body params: RequestBody): Single<ApiResponse<ApiVerifyUser>>
}
