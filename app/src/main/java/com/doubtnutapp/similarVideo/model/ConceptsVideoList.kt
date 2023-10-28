package com.doubtnutapp.similarVideo.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.common.contentlock.ContentLock
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class ConceptsVideoList(
        val questionId: String,
        val ocrText: String?,
        val thumbnailImage: String?,
        val bgColor: String,
        val duration: Int?,
        var shareCount: Int?,
        var likeCount: Int?,
        var isLiked: Boolean?,
        var sharingMessage: String?,
        val contentLock: ContentLock,
        val resourceType: String,
        override val viewType: Int
) : Parcelable, RecyclerViewItem

