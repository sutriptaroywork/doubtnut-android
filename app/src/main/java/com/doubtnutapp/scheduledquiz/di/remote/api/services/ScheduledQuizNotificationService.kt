package com.doubtnutapp.scheduledquiz.di.remote.api.services

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.scheduledquiz.di.model.QuizData
import okhttp3.RequestBody
import retrofit2.http.*

interface ScheduledQuizNotificationService {
    @POST("/v1/quiz/get-upcoming-quizzes")
    suspend fun getLatestScheduledNotifications(@Body params: RequestBody): ApiResponse<QuizData>
}