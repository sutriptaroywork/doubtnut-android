package com.doubtnutapp.data.splashscreen.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiCameraScreenConfig constructor(
    @SerializedName("camera_text6") val headlineText: String,
    @SerializedName("camera_text2") val tryOutQueText: String,
    @SerializedName("camera_text5") val typeYouQuesButtonText: String,
    @SerializedName("camera_text4") val pickGalleryButtonText: String,
    @SerializedName("camera_text7") val dontHaveQueText: String
)
