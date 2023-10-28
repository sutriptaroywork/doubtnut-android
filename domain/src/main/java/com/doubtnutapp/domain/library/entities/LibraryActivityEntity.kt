package com.doubtnutapp.domain.library.entities

import androidx.annotation.Keep

/**
 * Created by shrreya on 18/6/19.
 */
@Keep
data class LibraryActivityEntity(
    val playlist: List<LibraryPlaylistEntity>?,
    val header: List<LibraryHeaderEntity>?,
    val metaInfo: List<LibraryMetaInfoEntity>?

) {
    @Keep
    data class LibraryMetaInfoEntity(
        val icon: String,
        val title: String,
        val description: String?,
        val suggestionButtonText: String?,
        val suggestionId: String?,
        val suggestionName: String?
    )

    @Keep
    data class LibraryHeaderEntity(
        val headerId: String,
        val headerTitle: String,
        val headerImageUrl: String?,
        val headerIsLast: Int,
        val headerDescription: String?,
        val announcement: Announcement?,
        val isLocked: Boolean = false,
        val subject: String = ""
    )
}
