package com.doubtnutapp.data.newglobalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.newglobalsearch.model.YoutubeSearchItemSnippetData
import com.doubtnutapp.domain.newglobalsearch.entities.YTSearchItemSnippetEntity
import javax.inject.Inject

class YTItemSnippetMapper @Inject constructor(private val ytThumbnailsMapper: YTThumbnailsMapper) : Mapper<YoutubeSearchItemSnippetData, YTSearchItemSnippetEntity> {

    override fun map(srcObject: YoutubeSearchItemSnippetData) =
        YTSearchItemSnippetEntity(
            srcObject.publishedAt.orEmpty(),
            srcObject.channelId.orEmpty(),
            srcObject.title.orEmpty(),
            srcObject.description.orEmpty(),
            ytThumbnailsMapper.map(srcObject.thumbnails),
            srcObject.channelTitle.orEmpty(),
            srcObject.liveBroadcastContent.orEmpty(),
            srcObject.publishTime.orEmpty()
        )
}
