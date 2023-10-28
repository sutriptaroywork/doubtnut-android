package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Created by akshaynandwana on
 * 26, September, 2018
 **/
data class QuizSubmitAnswer(
    @SerializedName("is_correct") val isCorrect: Int,
    @SerializedName("total_score") val totalScore: Int,
    @SerializedName("is_eligible") val isEligible: Int
)
