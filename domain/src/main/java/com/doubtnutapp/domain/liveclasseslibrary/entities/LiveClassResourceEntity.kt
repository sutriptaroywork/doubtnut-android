package com.doubtnutapp.domain.liveclasseslibrary.entities

import androidx.annotation.Keep

@Keep
data class LiveClassResourceEntity(
    val type: String,
    val title: String,
    val qId: String,
    val imageUrl: String,
    val liveAt: String,
    val topicList: List<String>,
    val page: String?
)
