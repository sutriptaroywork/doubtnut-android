package com.doubtnutapp.newlibrary.model

import androidx.annotation.Keep

@Keep
data class LibraryHeaderViewItem(
        val title: String,
        override val size: String?,
        override val viewLayoutType: Int
) : LibraryViewItem {
    companion object {
        const val type = "header"
    }
}