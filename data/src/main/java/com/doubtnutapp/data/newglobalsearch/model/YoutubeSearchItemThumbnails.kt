package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class YoutubeSearchItemThumbnails(
    @SerializedName("default")
    val defaultQualityThumbnail: ThumbnailData,
    @SerializedName("medium")
    val mediumQualityThumbnail: ThumbnailData,
    @SerializedName("high")
    val highQualityThumbnail: ThumbnailData
)

@Keep
data class ThumbnailData(
    @SerializedName("url")
    val url: String?,
    @SerializedName("width")
    val width: Int?,
    @SerializedName("height")
    val height: Int?
)
