package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

/**
 * Created by Anand Gaurav on 2020-01-10.
 */
@Keep
data class NextVideoViewItem(
        val playlistData: PlaylistViewItem,
        val videoData: VideoDataViewItem,
        override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val type: String = "next_video"
    }
}

@Keep
data class PlaylistViewItem(
        val title: String,
        val playlistId: String,
        val isLast: String
)

@Keep
data class VideoDataViewItem(
        val questionId: String,
        val playlistId: String,
        val studentClass: String,
        val chapter: String,
        val chapterOrder: String,
        val ncertExerciseName: String,
        val ocrText: String,
        val subject: String,
        val parentId: String,
        val questionTag: String,
        val thumbnailImgUrl: String,
        val thumbnailImgUrlHindi: String,
        val doubt: String,
        val resourceType: String,
        val duration: String,
        val bgColor: String,
        val share: String,
        val like: String,
        val views: String?,
        val shareMessage: String,
        val isLiked: Boolean
)