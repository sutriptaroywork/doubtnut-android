package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity

/**
 * Created by Anand Gaurav on 2019-10-01.
 */
@Keep
data class BookViewItem(
        val id: String,
        val imageUrl: String,
        val title: String,
        val isLocked: Boolean,
        val subTitle: String?,
        val waUrl: String?,
        val isLast: String?,
        val startGradient: String?,
        val sharingMessage: String?,
        val resourceType : String?,
        val resourcePath : String?,
        val packageDetailsId: String?,
        val announcement: AnnouncementEntity,
        val deeplink: String?,
        override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val type: String = "books"
    }
}