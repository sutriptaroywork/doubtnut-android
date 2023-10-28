package com.doubtnutapp.scheduledquiz.di.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class QuizData(
        @SerializedName("quiz_data")
        var quizData: List<ScheduledQuizNotificationModel>
)
