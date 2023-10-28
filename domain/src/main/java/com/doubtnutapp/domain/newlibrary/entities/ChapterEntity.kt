package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class ChapterEntity(
    val id: String,
    val title: String?,
    val subTitle: String?,
    val imageUrl: String?,
    val videoCount: String?,
    val isLocked: Boolean?,
    val isLast: String?,
    val description: String?,
    val pdfUrl: String?,
    val packageDetailsId: String?,
    val announcement: AnnouncementEntity,
    val deeplink: String?
) : RecyclerDomainItem {
    companion object {
        const val type: String = "CHAPTER"
    }
}
