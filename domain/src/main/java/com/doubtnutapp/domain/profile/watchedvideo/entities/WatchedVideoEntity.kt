package com.doubtnutapp.domain.profile.watchedvideo.entities

import androidx.annotation.Keep

@Keep
data class WatchedVideoEntity(
    val questionId: String,
    val ocrText: String,
    val thumbnailImage: String?,
    val bgColor: String,
    val duration: Int,
    val shareCount: Int,
    val likeCount: Int,
    val html: String?,
    val isLiked: Boolean,
    val sharingMessage: String,
    val views: String?,
    val resourceType: String
)
