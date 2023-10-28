package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class Student(
    @SerializedName("profile_image")
    val profileImage: String,
    @SerializedName("student_username")
    val studentUsername: String
)
