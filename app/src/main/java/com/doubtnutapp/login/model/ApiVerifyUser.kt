package com.doubtnutapp.login.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Created by Anand Gaurav on 2019-08-13.
 */
@Keep
data class ApiVerifyUser(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("onboarding_video") val onboardingVideo: String,
    @SerializedName("intro") val intro: ArrayList<ApiIntro>,
    @SerializedName("student_username") val studentUsername: String?,
    @SerializedName("is_new_user") val isNewUser: Boolean,
    @SerializedName("guest_user") val guestUser: Boolean
)
