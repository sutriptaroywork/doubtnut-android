package com.doubtnutapp.newlibrary.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.newlibrary.entities.AnnouncementEntity

@Keep
data class LibrarySubjectViewItem(
        val id: String,
        val title: String,
        val viewType: String,
        val description: String,
        val imageUrl: String,
        val studentClass: String,
        val studentId: Int,
        val isLast: String?,
        override val size: String?,
        val announcement: AnnouncementEntity,
        override val viewLayoutType: Int
) : LibraryViewItem {
    companion object {
        const val type = "subject"
    }
}