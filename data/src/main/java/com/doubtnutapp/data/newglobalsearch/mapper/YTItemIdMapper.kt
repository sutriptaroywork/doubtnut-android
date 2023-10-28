package com.doubtnutapp.data.newglobalsearch.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.newglobalsearch.model.YoutubeSearchItemId
import com.doubtnutapp.domain.newglobalsearch.entities.YTSearchItemIdEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YTItemIdMapper @Inject constructor() : Mapper<YoutubeSearchItemId, YTSearchItemIdEntity> {

    override fun map(srcObject: YoutubeSearchItemId) =
        YTSearchItemIdEntity(
            srcObject.kind.orEmpty(),
            srcObject.videoId.orEmpty()
        )
}
