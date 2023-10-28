package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class ConceptVideoListEntity(
    val questionId: String,
    val ocrText: String?,
    val thumbnailImage: String?,
    val bgColor: String,
    val duration: Int?,
    val shareCount: Int?,
    val likeCount: Int?,
    val isLiked: Boolean?,
    val sharingMessage: String?,
    val resourceType: String,
    val isLocked: Boolean = false,
    val subjectName: String
) : DoubtnutViewItem {
    companion object {
        const val resourceType: String = "video"
    }
}
