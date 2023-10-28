package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class LastDayUser(
    @SerializedName("student_id") val studentId: Int,
    @SerializedName("video_count") val videoCount: Int,
    @SerializedName("total_engagement_time") val totalEngagementTime: Int,
    @SerializedName("student_fname") val studentFname: String,
    @SerializedName("student_username") val studentUsername: String,
    @SerializedName("profile_image") val profileImage: String,
    @SerializedName("amount") val amount: String,
    @SerializedName("position") val position: Int,
    @SerializedName("id") val id: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("type") val type: String?,
    @SerializedName("parameter") val parameter: String?,
    @SerializedName("contest_id") val contestId: String?,
    @SerializedName("total_referral") val totalReferral: Int?,
    @SerializedName("streak_count") val streakCount: Int?

)
