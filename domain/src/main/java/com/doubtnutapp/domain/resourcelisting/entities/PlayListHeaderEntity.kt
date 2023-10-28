package com.doubtnutapp.domain.resourcelisting.entities

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class PlayListHeaderEntity(
    val headerId: String,
    val headerTitle: String,
    val headerImageUrl: String?,
    val headerIsLast: Int,
    val headerDescription: String?,
    val announcement: AnnouncementEntity?,
    val isLock: Boolean?,
    val subject: String?
)
