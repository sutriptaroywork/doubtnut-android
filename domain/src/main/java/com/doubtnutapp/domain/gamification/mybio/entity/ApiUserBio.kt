package com.doubtnutapp.domain.gamification.mybio.entity

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiUserBio(
    @SerializedName("name") val name: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("gender") val gender: ApiUserBioList,
    @SerializedName("class") val userClass: ApiUserBioList,
    @SerializedName("board") val board: ApiUserBoardList,
    @SerializedName("exam") val exams: ApiUserBoardList,
    @SerializedName("geo") val location: ApiUserBioLocation,
    @SerializedName("school") val school: String?,
    @SerializedName("coaching") val coaching: ApiUserBioCoaching,
    @SerializedName("date_of_birth") val dob: String?,
    @SerializedName("language") val languages: List<Language>?,
)

@Keep
data class Language(
    @SerializedName("id") val id: Int,
    @SerializedName("code") val code: String,
    @SerializedName("title") val title: String,
    @SerializedName("is_selected") val isSelected: Int
)
