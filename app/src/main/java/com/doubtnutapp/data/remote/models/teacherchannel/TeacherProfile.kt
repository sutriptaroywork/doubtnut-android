package com.doubtnutapp.data.remote.models.teacherchannel

import android.os.Parcelable
import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class TeacherProfile(
    @SerializedName("is_profile_completed") val is_profile_completed: Boolean?,
    @SerializedName("teacher_meta") val profileData: TeacherProfileMeta?,
    @SerializedName("teaching_details") val teachingDetails: TeachingExperience?

) : Parcelable

@Keep
@Parcelize
data class TeacherProfileMeta(
    @SerializedName("fname") val fname: String?,
    @SerializedName("lname") val lname: String?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("img_url") val profileImage: String?,
    @SerializedName("college") val college: String?,
    @SerializedName("degree") val degree: String?,
    @SerializedName("mobile") val mobile: String?,
    @SerializedName("country_code") val country_code: String?,
    @SerializedName("pincode") val pincode: String?,
    @SerializedName("location") val location: String?,
    @SerializedName("about_me") val about: String?,
    @SerializedName("year_of_experience") val year_of_experience: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("dob") val dob: String?,
    @SerializedName("is_verified") val is_verified: Int?,
    @SerializedName("subscribers") val subscribers: String?,
) : Parcelable

@Keep
@Parcelize
data class TeachingExperience(
    @SerializedName("locale") val locale: String?,
    @SerializedName("class") val classTaught: String?,
    @SerializedName("board") val board: String?,
    @SerializedName("exam") val exam: String?,
    @SerializedName("subject") val subject: String?,
) : Parcelable
