package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class ChapterViewItem(
        val id: String,
        val title: String?,
        val subTitle: String?,
        var imageUrl: String?,
        val videoCount: String?,
        val isLocked: Boolean?,
        val isLast: String?,
        val pdfUrl: String?,
        val description: String,
        val packageDetailsId: String?,
        val announcement: AnnouncementEntity,
        val deeplink: String?,
        override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val type: String = "chapter"
    }
}