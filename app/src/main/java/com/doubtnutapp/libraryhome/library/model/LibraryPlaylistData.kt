package com.doubtnutapp.libraryhome.library.model

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

/**
 * Created by shrreya on 17/6/19.
 */
@Keep
@Parcelize
data class LibraryPlaylistData(
        val playlistId: String,
        val playlistContentName: String?,
        val resourceType: String,
        val resourcePath: String?,
        val isLast: String?,
        val isLocked: Boolean,
        val isNew: Boolean,
        val announcement: AnnouncementData?
) : Parcelable