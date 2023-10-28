package com.doubtnutapp.data.camerascreen.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class ApiCropScreenConfig constructor(
    @SerializedName("demo_text1") val headlineTitleText: String,
    @SerializedName("demo_ocr_image") val sampleImageUrl: String,
    @SerializedName("demo_button_text") val findSolutionButtonText: String
)
