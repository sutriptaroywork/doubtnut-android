package com.doubtnutapp.newlibrary.model

import androidx.annotation.Keep

@Keep
data class LibraryHorizontalBannerViewItem(
        val dataList: List<LibraryViewItem>,
        override val size: String?,
        override val viewLayoutType: Int
) : LibraryViewItem