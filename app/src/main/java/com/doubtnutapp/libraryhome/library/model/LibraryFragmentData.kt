package com.doubtnutapp.libraryhome.library.model

import android.os.Parcelable
import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import kotlinx.android.parcel.Parcelize

/**
 * Created by shrreya on 14/6/19.
 */
@Keep
@Parcelize
data class LibraryFragmentData(
        val playlistId: String,
        val playlistName: String?,
        val playlistDescription: String?,
        val playlistImageUrl: String?,
        val playlistIsFirst: String,
        val playlistIsLast: String,
        val playlistSize: Int,
        val announcement: AnnouncementData?,
        override val viewType: Int
) : Parcelable, RecyclerViewItem, Comparable<LibraryFragmentData> {
    override fun compareTo(other: LibraryFragmentData): Int {
        return if (playlistSize <= other.playlistSize) {
            1
        } else
            -1
    }
}
