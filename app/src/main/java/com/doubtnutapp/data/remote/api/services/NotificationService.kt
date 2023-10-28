package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.AppEvents
import retrofit2.http.GET

interface NotificationService {

    @GET("v2/notification/get-pending")
    fun getPendingNotifications(): RetrofitLiveData<ApiResponse<ArrayList<AppEvents>>>
}
