package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class SearchSuggestionsDataEntity(
    val suggestionsList: MutableList<SearchSuggestionEntity>
)
