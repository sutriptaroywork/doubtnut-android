package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ZoomFeedItem(
    var id: String,
    var isLiked: Int,
    var likeCount: Int,
    val imageUrl: String?,
    val text: String?,
    var commentsCount: Int,
    val type: String
) : Parcelable
