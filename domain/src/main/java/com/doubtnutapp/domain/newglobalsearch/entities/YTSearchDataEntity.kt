package com.doubtnutapp.domain.newglobalsearch.entities

import androidx.annotation.Keep

@Keep
data class YTSearchDataEntity(
    val kind: String,
    val eTag: String,
    val nextPageToken: String,
    val regionCode: String,
    val pageInfo: YTSearchResultsPageEntity,
    val youtubeSearchItems: List<YTSearchItemEntity>
)
