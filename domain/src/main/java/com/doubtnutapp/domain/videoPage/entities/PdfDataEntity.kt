package com.doubtnutapp.domain.videoPage.entities

import androidx.annotation.Keep

@Keep
data class PdfListDataEntity(
    val list: List<PdfEntity>
) {
    data class PdfEntity(
        val display: String,
        val imageUrl: String,
        val pdfLink: String
    )
}
