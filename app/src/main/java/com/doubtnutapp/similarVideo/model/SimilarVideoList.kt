package com.doubtnutapp.similarVideo.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.common.contentlock.ContentLock
import com.doubtnutapp.youtubeVideoPage.model.VideoTagViewItem
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class SimilarVideoList(
        val questionIdSimilar: String,
        val youTubeIdSimilar: String?,
        val ocrTextSimilar: String,
        val thumbnailImageSimilar: String,
        val localeThumbnailImageSimilar: String?,
        val bgColorSimilar: String,
        val durationSimilar: Int,
        var shareCountSimilar: Int,
        var likeCountSimilar: Int,
        val html: String?,
        var isLikedSimilar: Boolean,
        var sharingMessage: String,
        val contentLock: ContentLock,
        val resourceType: String,
        val views: String?,
        val targetCourse: String?,
        val meta: String?,
        val tagsList: List<VideoTagViewItem>,
        val ref: String?,
        val viewsText: String?,
        val isVip: Boolean,
        val assortmentId: String?,
        val variantId: String?,
        val paymentDetails: String?,
        val isPlayableInPip: Boolean,
        val questionTag: String?,
        override val viewType: Int

) : Parcelable, RecyclerViewItem {
    fun isHtmlPresent() = html != null && html.isNotBlank()
}
