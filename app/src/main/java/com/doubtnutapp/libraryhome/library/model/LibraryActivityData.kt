package com.doubtnutapp.libraryhome.library.model

import androidx.annotation.Keep

/**
 * Created by shrreya on 18/6/19.
 */
@Keep
data class LibraryActivityData(
        val playlistData: List<LibraryPlaylistData>?,
        val header: List<PlayListHeader>?,
        val metaInfo: List<PlayListMetaInfo>?,
        val selectedContentIndex: Int = 0
) {
    @Keep
    data class PlayListMetaInfo(
            val icon: String,
            val title: String,
            val description: String?,
            val suggestionButtonText: String?,
            val suggestionId: String?,
            val suggestionName: String?
    )

    @Keep
    data class PlayListHeader(
            val headerId: String,
            val headerTitle: String,
            val headerImageUrl: String?,
            val headerIsLast: Int,
            val headerDescription: String?,
            val isSelected: Boolean = false,
            val announcement: AnnouncementData?,
            val isLocked: Boolean,
            val subject: String
    )
}