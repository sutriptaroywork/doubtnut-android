package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import com.doubtnutapp.domain.common.entities.model.AnnouncementEntity

@Keep
data class ConceptVideosViewItem(
        val id: String,
        val title: String?,
        val subTitle: String?,
        val imageUrl: String?,
        val videoCount: String?,
        val isLocked: Boolean?,
        val isLast: String?,
        val announcement: AnnouncementEntity,
        override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val type: String = "concept_video"
    }
}