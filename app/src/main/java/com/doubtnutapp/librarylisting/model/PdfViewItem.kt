package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity

@Keep
data class PdfViewItem(
        val id: String,
        val title: String?,
        val description: String?,
        val waUrl: String?,
        val isLocked: Boolean?,
        val isLast: String?,
        val pdfUrl: String?,
        val announcement: AnnouncementEntity,
        override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val type: String = "pdf"
    }
}