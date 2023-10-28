package com.doubtnutapp.data.remote.models

data class LibraryQuestion(
    val classname: String?,
    val course: String?,
    val question_id: String,
    val ocr_text: String?,
    val question: String,
    val question_image: String?
)
