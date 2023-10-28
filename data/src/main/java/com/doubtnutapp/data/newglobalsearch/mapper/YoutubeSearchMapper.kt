package com.doubtnutapp.data.newglobalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.newglobalsearch.model.YoutubeSearchApiData
import com.doubtnutapp.domain.newglobalsearch.entities.YTSearchDataEntity
import javax.inject.Inject

class YoutubeSearchMapper @Inject constructor(
    private val ytPageInfoMapper: YTPageInfoMapper,
    private val ytSearchItemMapper: YTSearchItemMapper
) :
    Mapper<YoutubeSearchApiData, YTSearchDataEntity> {

    override fun map(srcObject: YoutubeSearchApiData): YTSearchDataEntity =
        YTSearchDataEntity(
            srcObject.kind.orEmpty(),
            srcObject.eTag.orEmpty(),
            srcObject.nextPageToken.orEmpty(),
            srcObject.regionCode.orEmpty(),
            ytPageInfoMapper.map(srcObject.pageInfo),
            srcObject.youtubeSearchItems.map {
                ytSearchItemMapper.map(it)
            }

        )
}
