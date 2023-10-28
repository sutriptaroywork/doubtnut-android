package com.doubtnutapp.ui.likeuserlist

import com.google.gson.annotations.SerializedName

data class LikedUser(
        @SerializedName("created_at")
        val createdAt: String,
        @SerializedName("student_avatar")
        val studentAvatar: Any,
        @SerializedName("student_id")
        val studentId: Int,
        @SerializedName("student_username")
        val studentUsername: String
)