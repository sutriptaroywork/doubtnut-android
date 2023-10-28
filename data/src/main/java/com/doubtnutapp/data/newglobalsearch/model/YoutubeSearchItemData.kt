package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class YoutubeSearchItemData(
    @SerializedName("kind")
    val kind: String?,
    @SerializedName("etag")
    val etag: String?,
    @SerializedName("id")
    val id: YoutubeSearchItemId,
    @SerializedName("snippet")
    val snippet: YoutubeSearchItemSnippetData
)
