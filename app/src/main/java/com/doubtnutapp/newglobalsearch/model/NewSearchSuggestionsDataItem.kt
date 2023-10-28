package com.doubtnutapp.newglobalsearch.model

import androidx.annotation.Keep

@Keep
data class NewSearchSuggestionsDataItem(
        val suggestionsList: MutableList<SearchListViewItem>
)