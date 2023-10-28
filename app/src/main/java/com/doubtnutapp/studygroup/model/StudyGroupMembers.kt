package com.doubtnutapp.studygroup.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class StudyGroupMembers(
    @SerializedName("page") val page: Int,
    @SerializedName("members") val members: List<GroupMember>,
)

@Keep
data class GroupMember(
    @SerializedName("is_admin") var isAdmin: Int?,
    @SerializedName("is_blocked") val isBlocked: Int?,
    @SerializedName("is_active") val isActive: Int?,
    @SerializedName("name") val name: String?,
    @SerializedName("student_id") val studentId: String,
    @SerializedName("image") val image: String?,
)