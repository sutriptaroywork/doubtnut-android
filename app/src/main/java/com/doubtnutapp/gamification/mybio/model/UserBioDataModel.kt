package com.doubtnutapp.gamification.mybio.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UserBioDataModel(
    val name: String,
    val image: String,
    val gender: UserBioListDataModel,
    val userClass: UserBioListDataModel,
    val board: UserBoardListDataModel,
    val exams: UserBoardListDataModel,
    val location: UserBioLocationDataModel,
    val school: String,
    val coaching: UserBioCoachingDataModel,
    val dob: String,
    val languages: List<Language>?
)

@Keep
data class Language(
    val id: Int,
    val code: String,
    val title: String,
    val isSelected: Int,
)

@Keep
data class PostUserBioDataModel(
    @SerializedName("name")
    val name: String,
    @SerializedName("class")
    val userClass: String?,
    @SerializedName("gender")
    val gender: Int?,
    @SerializedName("board")
    val board: Int?,
    @SerializedName("exam")
    val goal: List<Int>?,
    @SerializedName("geo")
    val geo: PostUserBioLocation?,
    @SerializedName("school")
    val school: String?,
    @SerializedName("coaching")
    val coaching: String?,
    @SerializedName("date_of_birth")
    val dob: String?,
    @SerializedName("img_url")
    val imageUrl: String?,
    @SerializedName("language")
    val language: String?,
    @SerializedName("stream")
    val stream: Int?,
)

@Keep
data class PostUserBioLocation(
    @SerializedName("location")
    var location: String?,
    @SerializedName("lat")
    var lat: String?,
    @SerializedName("lon")
    var long: String?,
    @SerializedName("state")
    var state: String?,
    @SerializedName("country")
    var country: String?
)