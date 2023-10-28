package com.doubtnutapp.newlibrary.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.newlibrary.entities.AnnouncementEntity

/**
 * Created by Anand Gaurav on 2019-11-15.
 */
@Keep
data class LibraryHomeBookViewItem(
        val id: String,
        val imageUrl: String,
        val title: String,
        val isLocked: Boolean,
        val subTitle: String?,
        val waUrl: String?,
        val isLast: String?,
        val startGradient: String?,
        val sharingMessage: String?,
        override val size: String?,
        val announcement: AnnouncementEntity,
        override val viewLayoutType: Int
) : LibraryViewItem {
    companion object {
        const val type: String = "books"
    }
}