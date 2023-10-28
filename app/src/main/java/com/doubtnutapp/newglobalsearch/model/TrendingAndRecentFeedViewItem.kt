package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep

@Keep
data class TrendingAndRecentFeedViewItem(
        val type: String,
        val display: String,
        val imageUrl: String,
        val isRecentSearchItem: Boolean,
        val liveTag : Boolean,
        val liveOrder : Boolean,
        val search : String,
        val deeplink: String,
        val eventType: String?,
        override val viewType: Int
) : TrendingSearchFeedViewItem