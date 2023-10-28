package com.doubtnutapp.data.newglobalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.newglobalsearch.model.YoutubeSearchItemData
import com.doubtnutapp.domain.newglobalsearch.entities.YTSearchItemEntity
import javax.inject.Inject

class YTSearchItemMapper @Inject constructor(
    private val itemIdMapper: YTItemIdMapper,
    private val snippetMapper: YTItemSnippetMapper
) :
    Mapper<YoutubeSearchItemData, YTSearchItemEntity> {

    override fun map(srcObject: YoutubeSearchItemData) =
        YTSearchItemEntity(
            srcObject.kind.orEmpty(),
            srcObject.etag.orEmpty(),
            itemIdMapper.map(srcObject.id),
            snippetMapper.map(srcObject.snippet)
        )
}
