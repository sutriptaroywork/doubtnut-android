package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class YTSearchItemEntity(
    val kind: String,
    val eTag: String,
    val id: YTSearchItemIdEntity,
    val snippet: YTSearchItemSnippetEntity
)
