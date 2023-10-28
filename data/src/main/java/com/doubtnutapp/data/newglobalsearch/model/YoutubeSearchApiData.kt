package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class YoutubeSearchApiData(
    @SerializedName("kind")
    val kind: String?,
    @SerializedName("etag")
    val eTag: String?,
    @SerializedName("nextPageToken")
    val nextPageToken: String?,
    @SerializedName("regionCode")
    val regionCode: String?,
    @SerializedName("pageInfo")
    val pageInfo: YoutubeSearchResultsPageInfo,
    @SerializedName("items")
    val youtubeSearchItems: List<YoutubeSearchItemData>
)
