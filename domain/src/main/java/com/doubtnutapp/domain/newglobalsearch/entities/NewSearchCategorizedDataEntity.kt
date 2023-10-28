package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class NewSearchCategorizedDataEntity(
    val title: String,
    val tabType: String,
    val size: Int,
    val seeAll: Boolean,
    val chapterDetails: ChapterDetails?,
    val dataList: List<SearchListItem>,
    val dataListWithFilter: List<SearchListItem>?,
    val titleWithFilter: String?,
    val descriptionWithOnlyFilter: String?,
    val titleWithOnlyFilter: String?,
    val text: String?,
    val secondaryText: String?,
    val description: String?,
    val secondaryDescription: String?,
    val secondaryList: List<SearchListItem>?,
    val filterList: List<SearchFilter>?,
    val secondaryFilterList: List<SearchFilter>?
)
