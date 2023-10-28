package com.doubtnutapp.newlibrary.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.newlibrary.entities.AnnouncementEntity

@Keep
data class LibrarySavedItemViewItem(
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
        val deeplink: String?,
        override val viewLayoutType: Int
) : LibraryViewItem {
    companion object {
        const val type = "LISTX2"
    }
}