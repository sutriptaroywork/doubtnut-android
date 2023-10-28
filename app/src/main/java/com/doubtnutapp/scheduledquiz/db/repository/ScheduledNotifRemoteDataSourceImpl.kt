package com.doubtnutapp.scheduledquiz.db.repository

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.scheduledquiz.di.datasource.ScheduledNotificationRemoteDataSource
import com.doubtnutapp.scheduledquiz.di.model.QuizData
import com.doubtnutapp.scheduledquiz.di.remote.api.services.ScheduledQuizNotificationService
import okhttp3.RequestBody

class ScheduledNotifRemoteDataSourceImpl(private val scheduledQuizNotificationService: ScheduledQuizNotificationService) :
    ScheduledNotificationRemoteDataSource {
    override suspend fun getQuizNotifFromRemote(requestBody: RequestBody): ApiResponse<QuizData> {
        return scheduledQuizNotificationService.getLatestScheduledNotifications(requestBody)
    }
}