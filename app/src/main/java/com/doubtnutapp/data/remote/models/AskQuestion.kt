package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class AskQuestion(
    val matched_questions: ArrayList<MatchedQuestion>,
    val question_id: String,
    val matched_count: Int,
    val question_image: String?,
    val is_subscribed: Int?,
    @SerializedName("handwritten") val isHandWritten: Int?,
    @SerializedName("ocr_text") val OcrText: String?,
    @SerializedName("notification") val notification: ArrayList<NotificationInMatch>
)
