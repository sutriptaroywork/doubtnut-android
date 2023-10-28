package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Created by akshaynandwana on
 * 26, September, 2018
 **/
data class QuizTopWinnerList(
    @SerializedName("total_score") val totalScore: Int,
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("student_username") val studentUsername: String,
    @SerializedName("img_url") val imgUrl: String?,
    @SerializedName("student_fname") val studentFname: String,
    @SerializedName("quiz_id") val quizId: Int
)
