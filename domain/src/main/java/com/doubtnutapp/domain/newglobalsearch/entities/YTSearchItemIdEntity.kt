package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class YTSearchItemIdEntity(
    val kind: String,
    val videoId: String
)
