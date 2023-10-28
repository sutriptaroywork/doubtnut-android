package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.PhoneVerificationService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.GetOTP
import com.doubtnutapp.data.remote.models.VerifyOTP
import okhttp3.RequestBody

class PhoneVerificationRepository(val phoneVerificationService: PhoneVerificationService) {

    fun getOTP(params: RequestBody): RetrofitLiveData<ApiResponse<GetOTP>> =
        phoneVerificationService.sendOTP(params)

    fun verifyOTP(params: RequestBody): RetrofitLiveData<ApiResponse<VerifyOTP>> =
        phoneVerificationService.verifyOTP(params)
}
