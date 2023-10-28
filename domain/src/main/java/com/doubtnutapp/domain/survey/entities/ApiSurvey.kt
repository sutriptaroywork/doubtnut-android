package com.doubtnutapp.domain.survey.entities

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiSurvey(
    @SerializedName("survey_starting_data") val surveyStartingData: SurveyStartingData,
    @SerializedName("survey_ending_data") val surveyEndingData: SurveyEndingData,
    @SerializedName("question_data") val questionData: List<QuestionData>
)

@Keep
data class SurveyStartingData(
    @SerializedName("starting_img") val startingImg: String,
    @SerializedName("starting_heading") val startingHeading: String,
    @SerializedName("starting_sub_heading") val startingSubHeading: String,
    @SerializedName("starting_button_text") val startingButtonText: String
)

@Keep
data class SurveyEndingData(
    @SerializedName("ending_img") val endingImg: String,
    @SerializedName("ending_heading") val endingHeading: String,
    @SerializedName("ending_sub_heading") val endingSubHeading: String,
    @SerializedName("ending_button_text") val endingButtonText: String
)

@Keep
data class QuestionData(
    @SerializedName("question_id") val questionId: Long,
    @SerializedName("question_text") val questionText: String,
    @SerializedName("options") val options: List<String>,
    @SerializedName("type") val type: String,
    @SerializedName("question_img") val questionImg: String,
    @SerializedName("skippable") val skippable: Boolean,
    @SerializedName("next_text") val nextText: String,
    @SerializedName("skip_text") val skipText: String,
    @SerializedName("alert_text") val alertText: String?,
    var feedback: String? = null
)
