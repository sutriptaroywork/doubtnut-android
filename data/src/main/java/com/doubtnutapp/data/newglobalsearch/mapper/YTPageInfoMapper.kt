package com.doubtnutapp.data.newglobalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.newglobalsearch.model.YoutubeSearchResultsPageInfo
import com.doubtnutapp.domain.newglobalsearch.entities.YTSearchResultsPageEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YTPageInfoMapper @Inject constructor() :
    Mapper<YoutubeSearchResultsPageInfo, YTSearchResultsPageEntity> {

    override fun map(srcObject: YoutubeSearchResultsPageInfo) =
        YTSearchResultsPageEntity(
            srcObject.totalResults ?: 0,
            srcObject.resultsPerPage ?: 0
        )
}
