package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class ChapterFlexEntity(
    val id: String,
    val title: String?,
    val subTitle: String?,
    val imageUrl: String?,
    val videoCount: String?,
    val isLocked: Boolean?,
    val isLast: String?,
    val description: String?,
    val pdfUrl: String?,
    val flexList: List<ChapterEntity>,
    val packageDetailsId: String?,
    val announcement: AnnouncementEntity
) : RecyclerDomainItem {
    companion object {
        const val type: String = "FLEX"
    }
}
