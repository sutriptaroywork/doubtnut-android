package com.doubtnutapp.data.similarVideo.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2020-02-07.
 */
@Keep
data class ApiSimilarPlaylist(
    @SerializedName("list") val similarVideo: List<ApiSimilarPlaylistVideo>,
    @SerializedName("tab") val tabs: List<ApiSimilarPlaylistTab>?
)

@Keep
data class ApiSimilarPlaylistTab(
    @SerializedName("title") val title: String,
    @SerializedName("type") val type: String
)

@Keep
data class ApiSimilarPlaylistVideo(
    @SerializedName("question_id") val questionIdSimilar: String,
    @SerializedName("ocr_text") val ocrTextSimilar: String,
    @SerializedName("thumbnail_image") val thumbnailImageSimilar: String?,
    @SerializedName("resource_type") val resourceType: String,
    @SerializedName("package_id") val packageId: String?,
    @SerializedName("subject") val subjectName: String?,
    @SerializedName("bg_color") val bgColorSimilar: String,
    @SerializedName("duration") val durationSimilar: Int,
    @SerializedName("share") val shareCountSimilar: Int,
    @SerializedName("like") val likeCountSimilar: Int,
    @SerializedName("views") val views: String?,
    @SerializedName("ref") val ref: String?,
    @SerializedName("share_message") val sharingMessage: String,
    @SerializedName("isLiked") val isLikedSimilar: Boolean
)
