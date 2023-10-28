package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem

/**
 * Created by Anand Gaurav on 2020-01-10.
 */
@Keep
data class NextVideoEntity(
    val playlistData: PlaylistEntity?,
    val videoData: VideoDataEntity?
) : RecyclerDomainItem {
    companion object {
        const val type: String = "video"
    }
}

@Keep
data class PlaylistEntity(
    val title: String?,
    val playlistId: String?,
    val isLast: String?
)

@Keep
data class VideoDataEntity(
    val questionId: String?,
    val playlistId: String?,
    val studentClass: String?,
    val chapter: String?,
    val chapterOrder: String?,
    val ncertExerciseName: String?,
    val ocrText: String?,
    val subject: String?,
    val parentId: String?,
    val questionTag: String?,
    val thumbnailImgUrl: String?,
    val thumbnailImgUrlHindi: String?,
    val doubt: String?,
    val resourceType: String?,
    val duration: String?,
    val bgColor: String?,
    val share: String?,
    val like: String?,
    val views: String?,
    val shareMessage: String?,
    val isLiked: Boolean?
)
