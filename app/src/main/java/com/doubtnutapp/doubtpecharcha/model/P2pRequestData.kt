package com.doubtnutapp.doubtpecharcha.model

import androidx.annotation.Keep

@Keep
data class P2pRequestData(
    val questionImageUrl: String?,
    val questionText: String?,
    val questionId: String,
    val thumbnailImages: List<String>
)
