package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class Intro(
    @SerializedName("type") val type: String,
    @SerializedName("video") val video: String,
    @SerializedName("question_id") val questionId: String
)
