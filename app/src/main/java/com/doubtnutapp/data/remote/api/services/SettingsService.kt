package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Settings
import retrofit2.http.GET

interface SettingsService {

    @GET("v2/settings/get-about-us")
    fun aboutus(): RetrofitLiveData<ApiResponse<Settings>>

    @GET("v2/settings/get-contact-us")
    fun contactus(): RetrofitLiveData<ApiResponse<Settings>>

    @GET("v2/settings/get-tnc")
    fun termsnconditions(): RetrofitLiveData<ApiResponse<Settings>>

    @GET("v2/settings/get-privacy")
    fun privacypolicy(): RetrofitLiveData<ApiResponse<Settings>>

    @GET("v2/settings/get-camera-guide")
    fun cameraGuide(): RetrofitLiveData<ApiResponse<Any>>
}
