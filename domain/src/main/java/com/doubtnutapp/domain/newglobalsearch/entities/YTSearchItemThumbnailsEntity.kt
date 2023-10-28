package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class YTSearchItemThumbnailsEntity(
    val defaultQualityThumbnail: ThumbnailEntity,
    val mediumQualityThumbnail: ThumbnailEntity,
    val highQualityThumbnail: ThumbnailEntity
)

@Keep
data class ThumbnailEntity(
    val url: String,
    val width: Int,
    val height: Int
)
