package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class InAppLastPlayedVideo(
    val qid: String,
    val page: String,
    val title: String,
    val image: String,
    @SerializedName("video_time") val videoTime: String,
    val duration: String
)
