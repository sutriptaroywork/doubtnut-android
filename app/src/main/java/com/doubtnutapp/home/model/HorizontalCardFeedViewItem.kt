package com.doubtnutapp.home.model

import androidx.annotation.Keep

@Keep
class HorizontalCardFeedViewItem(val scrollSize: String, val homeFeedItems: List<HomeFeedViewItem>, override val viewType: Int, val sharingMessage: String) : HomeFeedViewItem {
    companion object {
        const val type = "horizontal_carousel"
    }
}