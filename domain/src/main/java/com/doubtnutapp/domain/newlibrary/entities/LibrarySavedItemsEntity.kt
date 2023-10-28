package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class LibrarySavedItemsEntity(
    val id: String,
    val title: String,
    val parentTitle: String,
    val viewType: String,
    val description: String,
    val imageUrl: String,
    val isLast: String?,
    val resourceType: String?,
    val resourcePath: String?,
    val announcement: AnnouncementEntity,
    val deeplink: String?
) : DoubtnutViewItem {
    companion object {
        const val type = "LISTX2"
    }
}
