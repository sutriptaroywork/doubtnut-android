package com.doubtnutapp.data.profile.watchedvideo.model

import com.google.gson.annotations.SerializedName

data class ApiWatchedVideoDataList(
    @SerializedName("playlist")val watchedVideo: List<ApiWatchedVideo>?,
    @SerializedName("meta_info")val noWatchedData: List<ApiWatchedVideoMetaInfo>?
)
