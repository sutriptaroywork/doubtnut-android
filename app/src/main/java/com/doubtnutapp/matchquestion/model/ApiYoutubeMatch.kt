package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiYoutubeMatchResult(
    @SerializedName("youtubeQid") val youtubeQid: Long,
    @SerializedName("youtubeMatches") val youtubeMatches: List<ApiYoutubeMatch>
)

@Keep
data class ApiYoutubeMatch(
    @SerializedName("kind") val kind: String,
    @SerializedName("etag") val etag: String,
    @SerializedName("id") val id: ApiYoutubeId,
    @SerializedName("snippet") val snippet: ApiSnippet
)

@Keep
data class ApiYoutubeId(
    @SerializedName("kind") val kind: String,
    @SerializedName("videoId") val videoId: String
)

@Keep
data class ApiSnippet(
    @SerializedName("publishedAt") val publishedAt: String,
    @SerializedName("channelId") val channelId: String,
    @SerializedName("title") val title: String,
    @SerializedName("description") val description: String,
    @SerializedName("thumbnails") val thumbnails: ApiThumbnails,
    @SerializedName("channelTitle") val channelTitle: String,
    @SerializedName("liveBroadcastContent") val liveBroadcastContent: String,
    @SerializedName("publishTime") val publishTime: String,
    @SerializedName("duration") val duration: String
)

@Keep
data class ApiThumbnails(
    @SerializedName("default") val default: ApiThumbnailDetails,
    @SerializedName("medium") val medium: ApiThumbnailDetails,
    @SerializedName("high") val high: ApiThumbnailDetails
)

@Keep
data class ApiThumbnailDetails(
    @SerializedName("url") val url: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int
)
