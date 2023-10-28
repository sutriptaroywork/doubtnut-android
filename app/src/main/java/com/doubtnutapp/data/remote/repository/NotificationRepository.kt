package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.NotificationService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.AppEvents

class NotificationRepository(val notificationService: NotificationService) {

    fun getPendingEvents(): RetrofitLiveData<ApiResponse<ArrayList<AppEvents>>> =
        notificationService.getPendingNotifications()
}
