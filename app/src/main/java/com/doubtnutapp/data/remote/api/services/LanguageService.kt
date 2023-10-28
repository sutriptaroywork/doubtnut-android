package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiLanguage
import com.doubtnutapp.data.remote.models.ApiResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface LanguageService {

    @GET("v2/language/get-list/{udid}")
    fun getLanguages(@Path("udid") udid: String): RetrofitLiveData<ApiResponse<ApiLanguage>>

    @POST("v2/language/update")
    fun setLanguage(@Body requestBody: RequestBody): RetrofitLiveData<ApiResponse<Any>>
}
