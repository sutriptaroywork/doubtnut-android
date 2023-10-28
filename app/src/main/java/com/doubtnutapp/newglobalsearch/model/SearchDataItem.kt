package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep

@Keep
data class SearchDataItem (
        val tabsList: List<SearchTabsItem>,
        val trendingList: List<SearchListViewItem>,
        val isVipUser: Boolean
)