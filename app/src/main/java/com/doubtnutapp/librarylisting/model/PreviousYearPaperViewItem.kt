package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

@Keep
data class PreviousYearPaperViewItem(
        val title: String,
        val isLast: String?,
        override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val type = "previous_paper"
    }
}