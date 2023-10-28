package com.doubtnutapp.libraryhome.liveclasses.model

import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class PdfViewItem(
    val name: String,
    val imageLink: String,
    val pdfLink: String,
    override val viewType: Int
) : LiveClassesFeedViewItem() {
    companion object {
        const val type: String = "todays_pdf"
    }
}