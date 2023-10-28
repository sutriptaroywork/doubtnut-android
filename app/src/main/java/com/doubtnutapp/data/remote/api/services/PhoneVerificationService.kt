package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.GetOTP
import com.doubtnutapp.data.remote.models.VerifyOTP
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST

interface PhoneVerificationService {

    @POST("v4/student/login")
    fun sendOTP(@Body params: RequestBody): RetrofitLiveData<ApiResponse<GetOTP>>

    @POST("v4/student/verify")
    fun verifyOTP(@Body params: RequestBody): RetrofitLiveData<ApiResponse<VerifyOTP>>
}
