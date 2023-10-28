package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class NewSearchSuggestionDataEntity(
    val suggestionsList: MutableList<SearchSuggestionEntity>,
    val searchResultEntity: NewSearchDataEntity
)
