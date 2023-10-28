package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class SearchSuggestionEntity(
    val displayText: String,
    val variantId: Long,
    val id: String,
    val suggestionVersion: String ? = null
)
