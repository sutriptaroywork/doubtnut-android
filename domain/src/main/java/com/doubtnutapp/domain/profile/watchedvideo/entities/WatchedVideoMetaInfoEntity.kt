package com.doubtnutapp.domain.profile.watchedvideo.entities

import androidx.annotation.Keep

@Keep
data class WatchedVideoMetaInfoEntity(
    val icon: String,
    val title: String,
    val description: String,
    val suggestionButtonText: String,
    val suggestionId: String,
    val suggestionName: String
)
