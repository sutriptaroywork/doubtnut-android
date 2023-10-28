package com.doubtnutapp.data.textsolution.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.videoPage.entities.TabDataEntity
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
@Keep
data class ApiTextSolutionData(
    @SerializedName("answer_id") val answerId: String?,
    @SerializedName("expert_id") val expertId: String?,
    @SerializedName("question_id") val questionId: String,
    @SerializedName("question") val question: String,
    @SerializedName("doubt") val doubt: String,
    @SerializedName("video_name") val videoName: String,
    @SerializedName("ocr_text") val ocrText: String,
    @SerializedName("answer_video") val answerVideo: String,
    @SerializedName("fallback_answer_video") val fallBackVideoUrl: String,
    @SerializedName("pre_add") val preAdVideoUrl: String?,
    @SerializedName("post_add") val postAdVideoUrl: String?,
    @SerializedName("hls_timeout") val hlsTimeoutTime: Long,
    @SerializedName("is_approved") val isApproved: String?,
    @SerializedName("answer_rating") val answerRating: String,
    @SerializedName("answer_feedback") val answerFeedback: String,
    @SerializedName("thumbnail_image") val thumbnailImage: String,
    @SerializedName("isLiked") val isLiked: Boolean,
    @SerializedName("isDisliked") val isDisliked: Boolean,
    @SerializedName("isPlaylistAdded") val isPlaylistAdded: Boolean,
    @SerializedName("isBookmarked") val isBookmarked: Boolean,
    @SerializedName("next_microconcept") val nextMicroconcept: ApiMicroConcept?,
    @SerializedName("view_id") val viewId: String,
    @SerializedName("title") val title: String,
    @SerializedName("weburl") val webUrl: String,
    @SerializedName("description") val description: String,
    @SerializedName("type") val videoEntityType: String,
    @SerializedName("id") val videoEntityId: String,
    @SerializedName("likes_count") val likeCount: Int,
    @SerializedName("dislikes_count") val dislikesCount: Int,
    @SerializedName("share_count") val shareCount: Int,
    @SerializedName("resource_type") val resourceType: String,
    @SerializedName("resource_data") val resourceData: String?,
    @SerializedName("tab_list") val tabList: List<TabDataEntity>?,
    @SerializedName("share_message") val shareMessage: String?,
    @SerializedName("video_lock_unlock_logs_data") val lockUnlockLogs: String?,
    @SerializedName("banner_data") val bannerData: ApiBannerData?,
    @SerializedName("batch_id") val batchId: String?,
    @SerializedName("hide_bottom_nav") val hideBottomNav: Boolean?,
    @SerializedName("back_press_bottom_sheet_deeplink") val backPressBottomSheetDeeplink: String?
)

@Keep
data class ApiBannerData(
    @SerializedName("image") val image: String,
    @SerializedName("text") val text: String,
    @SerializedName("cta_text") val ctaText: String,
    @SerializedName("cta_clicked_image") val ctaClickedImage: String,
    @SerializedName("cta_clicked_text") val ctaClickedText: String
)
