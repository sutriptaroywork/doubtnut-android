package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MatchedQuestion(
    val _index: String,
    val _type: String,
    val _id: String,
    val _score: Float,
    @SerializedName("class") val clazz: String?,
    val difficulty_level: String?,
    val chapter: String?,
    val question_thumbnail: String,
    val _source: MatchedQuestionSource,
    @SerializedName("html") val html: String?
) : Parcelable {

    fun isHtmlPresent() = html != null && html.isNotBlank()

    @Parcelize
    data class MatchedQuestionSource(val ocr_text: String) : Parcelable
}
