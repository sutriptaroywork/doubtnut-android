package com.doubtnutapp.domain.camerascreen.entity

import androidx.annotation.Keep

@Keep
data class CropScreenConfigEntity(
    val cropScreenTitle: String,
    val sampleImageUri: String,
    val findSolutionButtonText: String
)
