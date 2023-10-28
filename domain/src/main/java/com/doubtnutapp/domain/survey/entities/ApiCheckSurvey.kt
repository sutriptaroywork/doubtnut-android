package com.doubtnutapp.domain.survey.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiCheckSurvey(
    @SerializedName("message") val message: String,
    @SerializedName("survey_id") val surveyId: Long?
)
