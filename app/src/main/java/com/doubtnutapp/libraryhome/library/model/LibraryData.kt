package com.doubtnutapp.libraryhome.library.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem
import kotlinx.android.parcel.RawValue

@Keep
data class LibraryData(
        val playlistData: @RawValue List<RecyclerViewItem>)
