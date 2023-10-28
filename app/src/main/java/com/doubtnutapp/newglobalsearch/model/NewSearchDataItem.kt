package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep
import com.doubtnutapp.domain.newglobalsearch.entities.FeedBackDataEntity

@Keep
data class NewSearchDataItem(
        val tabsList: List<SearchTabsItem>,
        val allDataList: List<SearchListViewItem>,
        val categorizedDataList: List<NewSearchCategorizedDataItem>,
        val isVipUser: Boolean,
        val landingFacet: String?,
        val feedBackDataEntity: FeedBackDataEntity?
)