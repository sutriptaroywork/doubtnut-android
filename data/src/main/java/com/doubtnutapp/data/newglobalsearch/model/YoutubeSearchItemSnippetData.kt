package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class YoutubeSearchItemSnippetData(
    @SerializedName("publishedAt")
    val publishedAt: String?,
    @SerializedName("channelId")
    val channelId: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("thumbnails")
    val thumbnails: YoutubeSearchItemThumbnails,
    @SerializedName("channelTitle")
    val channelTitle: String?,
    @SerializedName("liveBroadcastContent")
    val liveBroadcastContent: String?,
    @SerializedName("publishTime")
    val publishTime: String?
)
