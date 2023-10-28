package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity

/**
 * Created by Anand Gaurav on 2020-01-10.
 */
@Keep
data class ChapterFlexViewItem(
        val id: String,
        val title: String?,
        val subTitle: String?,
        val imageUrl: String?,
        val videoCount: String?,
        val isLocked: Boolean?,
        val isLast: String?,
        val pdfUrl: String?,
        val description: String,
        val flexList: List<ChapterViewItem>,
        val packageDetailsId: String?,
        val announcement: AnnouncementEntity,
        override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val type: String = "chapter_flex"
    }
}