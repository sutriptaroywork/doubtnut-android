package com.doubtnutapp.similarVideo.model

import androidx.annotation.Keep
import com.doubtnutapp.base.RecyclerViewItem

@Keep
class HorizontalCardViewItem(
        val conceptVideoItems: List<RecyclerViewItem>,
        override val viewType: Int) : RecyclerViewItem {

    companion object {
        const val type = "horizontal_carousel"
    }
}