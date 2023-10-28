package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep

@Keep
data class NewLibraryPlayListEntity(
    val playListId: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val isFirst: String,
    val isLast: String
)
