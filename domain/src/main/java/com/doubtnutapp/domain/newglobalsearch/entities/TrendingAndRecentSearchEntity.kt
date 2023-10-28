package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class TrendingAndRecentSearchEntity(
    val type: String,
    val display: String,
    val imageUrl: String,
    val tabType: String,
    val searchKey: String,
    val liveTag: Boolean,
    val liveOrder: Boolean,
    val deeplink: String
) : SearchSuggestionsFeedItem
