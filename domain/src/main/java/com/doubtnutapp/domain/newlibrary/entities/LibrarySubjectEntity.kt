package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class LibrarySubjectEntity(
    val id: String,
    val title: String,
    val viewType: String,
    val description: String,
    val imageUrl: String,
    val studentClass: String,
    val studentId: Int,
    val isLast: String?,
    val size: String?,
    val announcement: AnnouncementEntity
) : DoubtnutViewItem {
    companion object {
        const val type = "SUBJECTS"
    }
}
