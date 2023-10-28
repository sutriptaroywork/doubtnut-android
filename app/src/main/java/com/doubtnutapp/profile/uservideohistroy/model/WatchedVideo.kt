package com.doubtnutapp.profile.uservideohistroy.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class WatchedVideo(
        val questionId: String,
        val ocrText: String?,
        val thumbnailImage: String?,
        val bgColor: String,
        val duration: Int,
        var shareCount: Int,
        var likeCount: Int,
        val html: String?,
        var isLiked: Boolean,
        val sharingMessage: String,
        val views: String?,
        val resourceType: String
) : Parcelable {
    fun isHtmlPresent() = html != null && html.isNotBlank()
}