package com.doubtnutapp.data.profile.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiStudentClass(
    @SerializedName("class") val className: String,
    @SerializedName("class_display") val classDisplay: String
)
