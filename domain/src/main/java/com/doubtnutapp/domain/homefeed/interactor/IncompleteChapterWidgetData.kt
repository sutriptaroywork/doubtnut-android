package com.doubtnutapp.domain.homefeed.interactor

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class IncompleteChapterWidgetData(
    @SerializedName("question_id") val questionId: String,
    @SerializedName("chapter") val chapter: String?,
    @SerializedName("book") val book: String?,
    @SerializedName("deeplink") val deeplink: String?,
    @SerializedName("title") val title: String
)
