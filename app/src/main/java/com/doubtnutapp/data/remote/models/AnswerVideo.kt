package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AnswerVideo(
    val answer_id: String,
    val expert_id: String,
    val question_id: String,
    val question: String,
    val doubt: String,
    val video_name: String,
    val ocr_text: String,
    val answer_video: String,
    @SerializedName("fallback_answer_video") val fallBackVideoUrl: String,
    @SerializedName("pre_add") val preAdVideoUrl: String?,
    @SerializedName("post_add") val postAdVideoUrl: String?,
    @SerializedName("hls_timeout") val hlsTimeoutTime: Long,
    val is_approved: String,
    val answer_rating: String,
    val answer_feedback: String,
    val thumbnail_image: String,
    @SerializedName("isLiked") val isLiked: Boolean,
    @SerializedName("isDisliked") val isDisliked: Boolean,
    @SerializedName("isPlaylistAdded") val isPlaylistAdded: Boolean,
    @SerializedName("isBookmarked") val isBookmarked: Boolean,
    val question_meta: QuestionMeta?,
    val next_microconcept: MicroConcept?,
    val view_id: String,
    @SerializedName("title") val title: String,
    @SerializedName("weburl") val webUrl: String,
    @SerializedName("description") val description: String,
    @SerializedName("type") val videoEntityType: String,
    @SerializedName("id") val videoEntityId: String
) : Parcelable
