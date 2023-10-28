package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class SearchHeaderEntity(
    val title: String,
    val imageUrl: String
) : SearchListItem
