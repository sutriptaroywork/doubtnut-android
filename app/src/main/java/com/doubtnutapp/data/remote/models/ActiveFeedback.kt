package com.doubtnutapp.data.remote.models

data class ActiveFeedback(
    val type: String,
    val title: String,
    val question: String,
    val options: String,
    val submit: String,
    val id: String,
    val count: Int
)
