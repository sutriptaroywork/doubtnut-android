package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class TrendingSearchDataListEntity(
    val header: String,
    val dataType: String,
    val contentType: String,
    val imageUrl: String,
    val widgetType: String,
    val playlist: List<SearchSuggestionsFeedItem>,
    val itemImageUrl: String?,
    val eventType: String?
)
