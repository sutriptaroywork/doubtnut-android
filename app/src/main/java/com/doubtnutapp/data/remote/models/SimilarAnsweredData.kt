package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class SimilarAnsweredData(
    val question_id: String,
    val ocr_text: String?,
    val question: String?,
    val thumbnail_image: String,
    @SerializedName("html") val html: String?
) {

    fun isHtmlPresent() = html != null && html.isNotBlank()
}
