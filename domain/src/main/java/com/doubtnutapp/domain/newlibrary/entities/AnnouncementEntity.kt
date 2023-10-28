package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep

@Keep
data class AnnouncementEntity(
    val type: String,
    val state: Boolean
)
