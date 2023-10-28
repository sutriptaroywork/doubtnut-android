package com.doubtnutapp.data.remote.models.userstatus

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import java.io.Serializable

@Keep
data class StatusMetaDetail(
    @SerializedName("view") val viewedDetail: ArrayList<StatusMetaDetailItem>?,
    @SerializedName("like") val likedDetail: ArrayList<StatusMetaDetailItem>?
) : Serializable

data class StatusMetaDetailItem(
    @SerializedName("_id") val id: String,
    @SerializedName("story_id") val statusId: String,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("type") val type: String,
    @SerializedName("class") val userClass: String,
    @SerializedName("profile_image") val profileImage: String?,
    @SerializedName("username") val userName: String,
    @SerializedName("value") val value: Boolean
) : Serializable
