package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 01/06/20.
 */
data class LiveClassQuizSubmitResponse(
    @SerializedName("is_correct") val isCorrect: String?,
    @SerializedName("answer") val answer: String
)
