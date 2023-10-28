package com.doubtnutapp.domain.resourcelisting.entities

import androidx.annotation.Keep

/**
 * Created by Anand Gaurav on 2019-10-12.
 */
@Keep
data class PlayListMetaInfoEntity(
    val icon: String,
    val title: String,
    val description: String,
    val suggestionButtonText: String?,
    val suggestionId: String?,
    val suggestionName: String?
)
