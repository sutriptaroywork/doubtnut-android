package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import okhttp3.RequestBody
import retrofit2.Call
import javax.inject.Inject

class AnswerRepository @Inject constructor(private val networkService: NetworkService) {

    fun updateViewOnBoarding(params: RequestBody): Call<ApiResponse<Any>> = networkService.updateViewOnBoarding(params)
}
