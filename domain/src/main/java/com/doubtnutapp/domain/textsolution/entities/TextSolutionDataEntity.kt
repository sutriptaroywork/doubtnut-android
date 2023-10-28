package com.doubtnutapp.domain.textsolution.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.videoPage.entities.TabDataEntity

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
@Keep
data class TextSolutionDataEntity(
    val answerId: String,
    val expertId: String?,
    val questionId: String,
    val question: String,
    val doubt: String,
    val videoName: String,
    val ocrText: String,
    val answerVideo: String,
    val fallBackVideoUrl: String,
    val preAdVideoUrl: String?,
    val postAdVideoUrl: String?,
    val hlsTimeoutTime: Long,
    val isApproved: String?,
    val answerRating: String,
    val answerFeedback: String,
    val thumbnailImage: String,
    var isLiked: Boolean,
    var isDisliked: Boolean,
    val isPlaylistAdded: Boolean,
    val isBookmarked: Boolean,
    val nextMicroconcept: MicroConceptEntity?,
    val viewId: String,
    val title: String,
    val webUrl: String,
    val description: String,
    val videoEntityType: String,
    val videoEntityId: String,
    var likeCount: Int,
    var dislikesCount: Int,
    var shareCount: Int,
    val resourceType: String,
    val resourceData: String?,
    val tabList: List<TabDataEntity>?,
    val shareMessage: String?,
    val lockUnlockLogs: String?,
    var bannerData: BannerDataEntity?,
    val batchId: String?,
    val hideBottomNav: Boolean?,
    val backPressBottomSheetDeeplink: String?
)

@Keep
data class BannerDataEntity(
    val image: String?,
    val text: String?,
    val ctaText: String?,
    val ctaClickedImage: String?,
    val ctaClickedText: String?
)
