package com.doubtnutapp.domain.library.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

/**
 * Created by shrreya on 14/6/19.
 */
@Keep
data class LibraryFragmentEntity(
    val playlistId: String,
    val playlistName: String?,
    val playlistDescription: String?,
    val playlistImageUrl: String?,
    val playlistIsFirst: String,
    val playlistIsLast: String,
    val playlistSize: Int,
    val announcement: Announcement?,
    val resourceType: String
) : DoubtnutViewItem {
    companion object {
        const val resourceType: String = "video"
    }
}
