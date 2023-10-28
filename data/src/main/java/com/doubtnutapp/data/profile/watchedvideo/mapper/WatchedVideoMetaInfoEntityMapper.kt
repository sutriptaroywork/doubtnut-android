package com.doubtnutapp.data.profile.watchedvideo.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.profile.watchedvideo.model.ApiWatchedVideoMetaInfo
import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoMetaInfoEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchedVideoMetaInfoEntityMapper @Inject constructor() :
    Mapper<ApiWatchedVideoMetaInfo, WatchedVideoMetaInfoEntity> {

    override fun map(srcObject: ApiWatchedVideoMetaInfo): WatchedVideoMetaInfoEntity =
        with(srcObject) {
            WatchedVideoMetaInfoEntity(
                icon,
                title,
                description,
                suggestionButtonText,
                suggestionId,
                suggestionName
            )
        }
}
