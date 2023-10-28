package com.doubtnutapp.data.newglobalsearch.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiSearchImageInfo(
    @SerializedName("start_gd")
    val startGrad: String?,
    @SerializedName("mid_gd")
    val midGrad: String?,
    @SerializedName("end_gd")
    val endGrad: String?,
    @SerializedName("faculty_title")
    val facultyName: String?,
    @SerializedName("faculty_image")
    val facultyImage: String?
)
