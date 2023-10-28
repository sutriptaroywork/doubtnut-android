package com.doubtnutapp.data.remote.models
import com.google.gson.annotations.SerializedName

data class VerifyOTP(
    val student_id: String,
    val token: String,
    @SerializedName("onboarding_video") val onboardingVideo: String,
    val intro: ArrayList<Intro>,
    @SerializedName("student_username") val studentUsername: String
)
