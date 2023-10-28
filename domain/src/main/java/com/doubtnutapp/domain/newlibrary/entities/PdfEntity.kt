package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity

/**
 * Created by Anand Gaurav on 2019-10-06.
 */
@Keep
data class PdfEntity(
    val id: String,
    val title: String?,
    val description: String?,
    val waUrl: String?,
    val isLocked: Boolean?,
    val isLast: String?,
    val pdfUrl: String?,
    val announcement: AnnouncementEntity
) : RecyclerDomainItem {
    companion object {
        const val type: String = "PDF"
    }
}
