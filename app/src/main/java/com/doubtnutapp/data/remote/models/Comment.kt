package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Comment(
    @SerializedName("_id") val id: String,
    @SerializedName("parent_id") val parentId: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("reported_by") val reportedBy: List<Int>,
    @SerializedName("liked_by") val likedBy: List<Int>,
    @SerializedName("is_deleted") val isDeleted: Boolean,
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("entity_type") val entityType: String,
    @SerializedName("entity_id") val entityId: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("student_username") val studentUsername: String,
    @SerializedName("student_avatar") val studentAvatar: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("__v") val v: Int,
    @SerializedName("is_liked") var isLiked: Int,
    @SerializedName("is_bookmarked") var isBookmarked: Boolean?,
    @SerializedName("is_reported") val isReported: Int,
    @SerializedName("audio") val audio: String?,
    @SerializedName("like_count") var likeCount: Int?,
    @SerializedName("replies_count") var replyCount: Int?,
    @SerializedName("is_doubt") var isDoubt: String?,
    @SerializedName("is_admin") val isAdmin: Boolean? = false,
    @SerializedName("user_tag") val userTag: String? = "",
    @SerializedName("video_obj") val videoObj: ApiVideoObj?,
    @SerializedName("is_answer") val isAnswer: Boolean?,
    @SerializedName("type") val type: String?,
    @SerializedName("resource_url") val resourceUrl: String?,

    @SerializedName("action_two_text") val actionTwoText: String?,
    @SerializedName("action_two_dl") val actionTwoDl: String?,
    @SerializedName("action_two_img") val actionTwoImg: String?,
    @SerializedName("items") var items: ArrayList<Comment>?,
    @SerializedName("is_expanded") var isExpanded: Boolean?,
    @SerializedName("assortment_id") var assortmentId: String?,
    var isMyComment: Boolean = false,
    @SerializedName("icon_url") var iconUrl: String?,
    @SerializedName("undo_text") var undoText: String?,
    @SerializedName("reply") var reply: String?,
    @SerializedName("allow_reply") val allowReply: Boolean?
) : Parcelable

@Parcelize
data class ApiVideoObj(
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("youtube_id") var youtubeId: String?,
    @SerializedName("autoplay") var autoPlay: Boolean?,
    @SerializedName("view_id") val viewId: String? = null,
    @SerializedName("thumbnail_image") val thumbnailImage: String? = null,
    @SerializedName("video_resources") val resources: List<ApiVideoResource>?,
    @SerializedName("show_full_screen") val showFullScreen: Boolean?,
    @SerializedName("page") var page: String?
) : Parcelable

@Keep
@Parcelize
data class ApiVideoResource(
    @SerializedName("resource") val resource: String,
    @SerializedName("drm_scheme") val drmScheme: String?,
    @SerializedName("drm_license_url") val drmLicenseUrl: String?,
    @SerializedName("media_type") val mediaType: String?,
    @SerializedName("drop_down_list") val dropDownList: List<ApiPlayBackData>?,
    @SerializedName("time_shift_resource") val timeShiftResource: ApiPlayBackData?,
    @SerializedName("offset") val offset: Long?,
    var isPlayed: Boolean = false
) : Parcelable {
    @Keep
    @Parcelize
    data class ApiPlayBackData(
        @SerializedName("resource") val resource: String,
        @SerializedName("drm_scheme") val drmScheme: String?,
        @SerializedName("drm_license_url") val drmLicenseUrl: String?,
        @SerializedName("media_type") val mediaType: String?,
        @SerializedName("display") val display: String?
    ) : Parcelable
}
