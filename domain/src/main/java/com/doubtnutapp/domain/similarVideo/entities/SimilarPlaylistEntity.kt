package com.doubtnutapp.domain.similarVideo.entities

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2020-02-07.
 */
@Keep
data class SimilarPlaylistEntity(
    val similarVideo: List<SimilarPlaylistVideoEntity>,
    val tabs: List<SimilarPlaylistTabEntity>?
)

@Keep
data class SimilarPlaylistTabEntity(
    val title: String,
    val type: String,
    var isSelected: Boolean
)

@Keep
data class SimilarPlaylistVideoEntity(
    val questionIdSimilar: String,
    val ocrTextSimilar: String,
    val thumbnailImageSimilar: String,
    val resourceType: String,
    val packageId: String,
    val bgColorSimilar: String,
    val durationSimilar: Int,
    val shareCountSimilar: Int,
    val likeCountSimilar: Int,
    val views: String?,
    val ref: String?,
    val sharingMessage: String,
    val isLikedSimilar: Boolean
)
