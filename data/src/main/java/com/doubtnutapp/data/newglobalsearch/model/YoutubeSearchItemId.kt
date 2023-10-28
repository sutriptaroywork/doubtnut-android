package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class YoutubeSearchItemId(
    @SerializedName("kind")
    val kind: String?,
    @SerializedName("videoId")
    val videoId: String?
)
