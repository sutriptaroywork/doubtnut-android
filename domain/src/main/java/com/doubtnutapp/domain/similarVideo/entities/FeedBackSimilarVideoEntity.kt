package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep

@Keep
data class FeedBackSimilarVideoEntity(
    val feedbackText: String,
    val isShow: Int,
    val bgColor: String
)
