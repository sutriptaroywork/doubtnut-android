package com.doubtnutapp.domain.camerascreen.entity

import androidx.annotation.Keep

@Keep
data class CameraConfigEntity(
    val headerTitle: String,
    val dontHaveQueText: String,
    val typeYourDoubtButtonText: String,
    val pickGalleryButtonText: String,
    val trickyQuestionText: String
)
