package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class YTSearchItemSnippetEntity(
    val publishedAt: String,
    val channelId: String,
    val title: String,
    val description: String,
    val thumbnails: YTSearchItemThumbnailsEntity,
    val channelTitle: String,
    val liveBroadcastContent: String,
    val publishTime: String
)
