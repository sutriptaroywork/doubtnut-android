package com.doubtnutapp.data.newglobalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.newglobalsearch.model.ThumbnailData
import com.doubtnutapp.data.newglobalsearch.model.YoutubeSearchItemThumbnails
import com.doubtnutapp.domain.newglobalsearch.entities.ThumbnailEntity
import com.doubtnutapp.domain.newglobalsearch.entities.YTSearchItemThumbnailsEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YTThumbnailsMapper @Inject constructor() :
    Mapper<YoutubeSearchItemThumbnails, YTSearchItemThumbnailsEntity> {

    override fun map(srcObject: YoutubeSearchItemThumbnails) =
        YTSearchItemThumbnailsEntity(
            getThumbnailData(srcObject.defaultQualityThumbnail),
            getThumbnailData(srcObject.mediumQualityThumbnail),
            getThumbnailData(srcObject.highQualityThumbnail)
        )

    private fun getThumbnailData(srcThumbnail: ThumbnailData) =
        ThumbnailEntity(
            srcThumbnail.url.orEmpty(),
            srcThumbnail.width ?: 0,
            srcThumbnail.height ?: 0
        )
}
