package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep

@Keep
data class SearchSuggestionsDataItem(
        val suggestionsList: MutableList<SearchListViewItem>
)