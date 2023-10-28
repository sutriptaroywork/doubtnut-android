package com.doubtnutapp.domain.globalsearch.entities

import androidx.annotation.Keep

@Keep
data class GlobalSearchEntity(
    val searchTabs: List<GlobalSearchTabEntity>,
    val searchResults: List<GlobalSearchResultEntity>
)
