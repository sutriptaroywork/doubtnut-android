package com.doubtnutapp.newlibrary.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.newlibrary.entities.AnnouncementEntity

@Keep
data class LibraryExamViewItem(
        val id: String,
        val title: String,
        val parentTitle: String,
        val viewType: String,
        val description: String,
        var imageUrl: String,
        val isLast: String?,
        val resourceType: String?,
        val resourcePath: String?,
        override val size: String?,
        val announcement: AnnouncementEntity,
        override val viewLayoutType: Int
) : LibraryViewItem {
    companion object {
        const val type = "BIGX3"
    }
}