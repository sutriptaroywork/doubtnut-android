package com.doubtnutapp.newlibrary.model

import androidx.annotation.Keep

@Keep
interface LibraryViewItem {
    val viewLayoutType: Int
    val size: String?
}