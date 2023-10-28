package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep

@Keep
data class TrendingSubjectViewItem(
        val display: String,
        val imageUrl: String,
        val dataType: String,
        val tabType : String,
        val isRecentSearchItem: Boolean,
        val searchKey : String,
        val liveOrder : Boolean,
        val liveTag : Boolean,
        override val viewType: Int
) : TrendingSearchFeedViewItem