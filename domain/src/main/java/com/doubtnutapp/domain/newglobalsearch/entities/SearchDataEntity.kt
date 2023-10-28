package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class SearchDataEntity(
    val tabList: List<SearchTabsEntity>,
    val trendingList: List<SearchListItem>,
    val isVipUser: Boolean
)
