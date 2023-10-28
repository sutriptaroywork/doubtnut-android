package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import okhttp3.RequestBody
import okhttp3.ResponseBody
import javax.inject.Inject

class ProfileRepository @Inject constructor(private val networkService: NetworkService) {

    fun updateProfile(params: RequestBody): RetrofitLiveData<ApiResponse<ResponseBody>> = networkService.updateProfile(params)

    fun getActiveSlots() = networkService.getActiveSlots()
}
