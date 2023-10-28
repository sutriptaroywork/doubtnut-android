package com.doubtnutapp.data.remote.models

import com.google.gson.annotations.SerializedName

data class StudentClass(
    @SerializedName("class") val name: Int,
    @SerializedName("class_display") val classDisplay: String?
)
