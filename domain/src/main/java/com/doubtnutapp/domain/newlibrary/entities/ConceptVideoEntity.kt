package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.RecyclerDomainItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity

/**
 * Created by Anand Gaurav on 2019-10-06.
 */
@Keep
data class ConceptVideoEntity(
    val id: String,
    val title: String?,
    val subTitle: String?,
    val imageUrl: String?,
    val videoCount: String?,
    val isLocked: Boolean?,
    val isLast: String?,
    val announcement: AnnouncementEntity
) : RecyclerDomainItem {
    companion object {
        const val type: String = "concept_video"
    }
}
