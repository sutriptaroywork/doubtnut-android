package com.doubtnutapp.data.profile.watchedvideo.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.profile.watchedvideo.model.ApiWatchedVideo
import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchedVideoEntityMapper @Inject constructor() : Mapper<ApiWatchedVideo, WatchedVideoEntity> {

    override fun map(srcObject: ApiWatchedVideo): WatchedVideoEntity = with(srcObject) {
        WatchedVideoEntity(
            questionId,
            ocrText,
            thumbnailImage,
            bgColor,
            duration,
            shareCount,
            likeCount,
            html,
            isLiked,
            sharingMessage,
            views,
            resourceType
        )
    }
}
