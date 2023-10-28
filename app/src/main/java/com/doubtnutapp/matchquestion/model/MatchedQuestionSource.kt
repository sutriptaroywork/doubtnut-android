package com.doubtnutapp.matchquestion.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class MatchedQuestionSource(
    val ocrText: String,
    val subject: String,
    var likeCount: Int,
    var shareCount: Int,
    var isLiked: Boolean,
    val duration: Int,
    val views: String?,
    val sharingMessage: String,
    val videoLanguage: String,
    val thumbnailLanguage: String,
    val exactMatch: Boolean?,
    val subjectIconLink: String?,
    val subjectTitle: String?,
    val subjectTitleColor: String?,
    val isExactMatch: Boolean?,
    val rating: String?,
    val ratingColor: String?,
    val ratingBackgroundColor: String?,
) : Parcelable

