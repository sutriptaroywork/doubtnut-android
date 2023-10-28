package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class UserDetails(
    @SerializedName("student_fname") val studentFname: String,
    @SerializedName("student_username") val studentUsername: String,
    @SerializedName("profile_image") val profileImage: String,
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("count") val count: Int?,
    @SerializedName("total_engagement_time") val totalEngagementTime: Int,
    @SerializedName("eligible") val eligible: Int,
    @SerializedName("text_string") val contextTextString: String?

)
