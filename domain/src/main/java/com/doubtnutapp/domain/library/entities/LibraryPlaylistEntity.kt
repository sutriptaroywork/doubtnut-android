package com.doubtnutapp.domain.library.entities

import androidx.annotation.Keep

/**
 * Created by shrreya on 17/6/19.
 */
@Keep
data class LibraryPlaylistEntity(
    val playlistId: String,
    val playlistContentName: String?,
    val resourceType: String,
    val resourcePath: String?,
    val isLast: String?,
    val size: String?,
    val isLocked: Boolean,
    val isNew: Boolean,
    val announcement: Announcement?
)
