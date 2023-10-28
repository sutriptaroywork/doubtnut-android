package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class YTSearchResultsPageEntity(
    val totalResults: Int,
    val resultsPerPage: Int
)
