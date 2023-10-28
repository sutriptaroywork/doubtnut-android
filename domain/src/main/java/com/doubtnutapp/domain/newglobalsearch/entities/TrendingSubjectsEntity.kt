package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class TrendingSubjectsEntity(
    val display: String,
    val imageUrl: String,
    val tabType: String,
    val searchKey: String,
    val liveOrder: Boolean,
    val liveTag: Boolean
) : SearchSuggestionsFeedItem
