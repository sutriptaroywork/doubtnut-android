package com.doubtnutapp.domain.liveclasseslibrary.entities

import androidx.annotation.Keep

@Keep
data class DetailLiveClassPdfEntity(
    val name: String,
    val imageLink: String,
    val pdfLink: String
)
