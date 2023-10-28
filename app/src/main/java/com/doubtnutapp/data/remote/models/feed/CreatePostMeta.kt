package com.doubtnutapp.data.remote.models.feed

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class CreatePostMeta(@SerializedName("topic") val topic: CreatePostTopic)

@Keep
data class CreatePostTopic(
    @SerializedName("image") val image: List<String>?,
    @SerializedName("link") val link: List<String>?,
    @SerializedName("pdf") val pdf: List<String>?,
    @SerializedName("video") val video: List<String>?,
    @SerializedName("message") val message: List<String>?,
    @SerializedName("live") val live: List<String>?
)
