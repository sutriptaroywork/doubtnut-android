package com.doubtnutapp.matchquestion.model

import androidx.annotation.Keep

@Keep
data class SignedUrlEntityData(
    val url: String,
    val questionId: String,
    val fileName: String)