package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

/**
 * Created by Anand Gaurav on 2019-11-15.
 */
@Keep
data class LibraryHomeBookEntity(
    val id: String,
    val imageUrl: String?,
    val title: String?,
    val isLocked: Boolean,
    val subTitle: String?,
    val waUrl: String?,
    val isLast: String?,
    val viewType: String,
    val startGradient: String?,
    val sharingMessage: String?,
    val announcement: AnnouncementEntity
) : DoubtnutViewItem {
    companion object {
        const val type: String = "BOOK"
    }
}
