package com.doubtnutapp.scheduledquiz.di.datasource

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.scheduledquiz.di.model.QuizData
import okhttp3.RequestBody

interface ScheduledNotificationRemoteDataSource {
    suspend fun getQuizNotifFromRemote(requestBody: RequestBody): ApiResponse<QuizData>
}