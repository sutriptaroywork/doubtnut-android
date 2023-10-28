package com.doubtnutapp.domain.profile.watchedvideo.entities

import androidx.annotation.Keep

@Keep
data class WatchedVideoListEntity(
    val watchedVideo: List<WatchedVideoEntity>?,
    val noWatchedData: List<WatchedVideoMetaInfoEntity>?
)
