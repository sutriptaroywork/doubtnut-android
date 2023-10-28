package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Created by akshaynandwana on
 * 09, October, 2018
 **/
data class QuizReportResponse(
    @SerializedName("questions") val questions: ArrayList<QuizReport>,
    @SerializedName("score") val score: Int
)
