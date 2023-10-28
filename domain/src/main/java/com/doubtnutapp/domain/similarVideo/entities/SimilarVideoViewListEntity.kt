package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class SimilarVideoViewListEntity(
    val questionIdSimilar: String,
    val youTubeIdSimilar: String?,
    val ocrTextSimilar: String,
    val thumbnailImageSimilar: String,
    val localeThumbnailImageSimilar: String?,
    val bgColorSimilar: String,
    val durationSimilar: Int,
    val shareCountSimilar: Int,
    val likeCountSimilar: Int,
    val html: String?,
    val isLikedSimilar: Boolean,
    val sharingMessage: String,
    val resourceType: String,
    val subjectName: String,
    val views: String?,
    val targetCourse: String,
    val meta: String,
    val isLocked: Boolean,
    val tagsList: List<String>,
    val ref: String?,
    val viewsText: String?,
    val isVip: Boolean,
    val assortmentId: String?,
    val variantId: String?,
    val paymentDetails: String?,
    val isPlayableInPip: Boolean?,
    val questionTag: String?,
) : DoubtnutViewItem {
    companion object {
        const val resourceType: String = "video"
    }
}
