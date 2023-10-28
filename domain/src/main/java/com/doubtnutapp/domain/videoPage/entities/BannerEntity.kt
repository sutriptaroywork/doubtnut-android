package com.doubtnutapp.domain.videoPage.entities

import androidx.annotation.Keep

@Keep
data class BannerEntity(
    val type: String,
    val link: String,
    val resourceType: String
)
