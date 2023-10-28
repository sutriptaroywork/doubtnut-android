package com.doubtnutapp.data.profile.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiUserProfile(
    @SerializedName("student_id") val studentId: String,
    @SerializedName("student_email") val studentEmail: String?,
    @SerializedName("locale") val studentLocale: String?,
    @SerializedName("img_url") val studentImageUrl: String?,
    @SerializedName("school_name") val schoolName: String?,
    @SerializedName("student_class") val studentClass: String,
    @SerializedName("total_video_view_duration") val studentTotalVideoViewDuration: String?,
    @SerializedName("student_username") val studentUserName: String?,
    @SerializedName("pincode") val StudentPinCode: String?,
    @SerializedName("coaching") val studentCoaching: String?,
    @SerializedName("dob") val studentDOB: String?,
    @SerializedName("student_course") val studentCourse: String?,
    @SerializedName("student_class_display") val studentClassDisplay: String?,
    @SerializedName("language") val studentLanguage: String?,
    @SerializedName("language_display") val studentLanguageDisplay: String?
)
