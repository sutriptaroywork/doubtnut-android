package com.doubtnutapp.data.profile.watchedvideo.model

import com.google.gson.annotations.SerializedName

data class ApiWatchedVideo(
    @SerializedName("question_id") val questionId: String,
    @SerializedName("ocr_text") val ocrText: String,
    @SerializedName("thumbnail_image") val thumbnailImage: String?,
    @SerializedName("bg_color") val bgColor: String,
    @SerializedName("duration") val duration: Int,
    @SerializedName("share") val shareCount: Int,
    @SerializedName("like") val likeCount: Int,
    @SerializedName("isLiked") val isLiked: Boolean,
    @SerializedName("share_message") val sharingMessage: String,
    @SerializedName("html") val html: String?,
    @SerializedName("views") val views: String?,
    @SerializedName("resource_type") val resourceType: String
)
