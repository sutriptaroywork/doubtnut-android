package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.MicroService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.NotificationCenterData
import com.doubtnutapp.notification.model.MaarkAsReadData
import com.doubtnutapp.notification.model.NotificationCountData
import io.reactivex.Single
import okhttp3.RequestBody

class NotificationCenterRepository(private val microService: MicroService) {

    fun getNotificationData(requestBody: RequestBody, pageNo: Int):
        Single<ApiResponse<List<NotificationCenterData>>> = microService
        .getAllNotifications(requestBody, pageNo)

    fun getUnreadNotifications():
        Single<ApiResponse<NotificationCountData>> = microService
        .getUnreadNotificationCount()

    fun updateSeenNotifications(params: RequestBody):
        Single<ApiResponse<MaarkAsReadData>> = microService
        .updateSeenNotifications(params)
}
