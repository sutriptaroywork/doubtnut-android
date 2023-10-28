package com.doubtnutapp.librarylisting.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

@Keep
data class SuperSeriesViewItem(
        val title: String,
        val subTitle: String,
        val isLast: String?,
        override val viewType: Int
) : RecyclerViewItem {
    companion object {
        const val type = "super_series"
    }
}