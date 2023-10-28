package com.doubtnutapp.data.remote.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TopComment(
    @SerializedName("parent_id") val parentId: Long,
    @SerializedName("image") val image: String?,
    @SerializedName("reported_by") val reportedBy: List<Number>?,
    @SerializedName("liked_by") val likedBy: List<Number>?,
    @SerializedName("is_deleted") val isDeleted: Boolean,
    @SerializedName("_id") val id: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("question_id") val questionId: String?,
    @SerializedName("entity_type") val entityType: String?,
    @SerializedName("entity_id") val entityId: String?,
    @SerializedName("student_id") val studentId: String?,
    @SerializedName("student_username") val studentUsername: String?,
    @SerializedName("student_avatar") val studentAvatar: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
    @SerializedName("__v") val v: Int?,
    @SerializedName("audio") val audio: String?
) : Parcelable
