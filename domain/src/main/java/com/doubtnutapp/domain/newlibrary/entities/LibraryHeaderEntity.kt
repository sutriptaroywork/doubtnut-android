package com.doubtnutapp.domain.newlibrary.entities

import androidx.annotation.Keep
import com.doubtnutapp.domain.common.entities.DoubtnutViewItem

@Keep
data class LibraryHeaderEntity(
    val text: String
) : DoubtnutViewItem {
    companion object {
        const val viewType = "header"
    }
}
