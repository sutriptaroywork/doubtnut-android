package com.doubtnutapp.profile.uservideohistroy.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoEntity
import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoListEntity
import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoMetaInfoEntity
import com.doubtnutapp.getAsViewsCountString
import com.doubtnutapp.profile.uservideohistroy.model.WatchedVideo
import com.doubtnutapp.profile.uservideohistroy.model.WatchedVideoDataList
import com.doubtnutapp.profile.uservideohistroy.model.WatchedVideoMetaInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchVideoMapper @Inject constructor() :
    Mapper<WatchedVideoListEntity, WatchedVideoDataList> {

    override fun map(srcObject: WatchedVideoListEntity) = with(srcObject) {
        WatchedVideoDataList(
            watchedVideo?.let { getWatchedVideo(it) },
            noWatchedData?.let { getNoWatchedVideoData(it) }

        )

    }

    private fun getNoWatchedVideoData(noWatchedData: List<WatchedVideoMetaInfoEntity>): List<WatchedVideoMetaInfo> =
        noWatchedData.map {
            mapNowatchedVideoData(it)
        }

    private fun mapNowatchedVideoData(watchedVideoMetaInfoEntity: WatchedVideoMetaInfoEntity): WatchedVideoMetaInfo =
        watchedVideoMetaInfoEntity.run {
            WatchedVideoMetaInfo(
                watchedVideoMetaInfoEntity.icon,
                watchedVideoMetaInfoEntity.title,
                watchedVideoMetaInfoEntity.description,
                watchedVideoMetaInfoEntity.suggestionButtonText,
                watchedVideoMetaInfoEntity.suggestionId,
                watchedVideoMetaInfoEntity.suggestionName
            )
        }

    private fun getWatchedVideo(watchedVideo: List<WatchedVideoEntity>): List<WatchedVideo> =
        watchedVideo.map {
            mapWatchedVideoData(it)
        }

    private fun mapWatchedVideoData(watchedVideoEntity: WatchedVideoEntity): WatchedVideo =
        watchedVideoEntity.run {
            WatchedVideo(
                watchedVideoEntity.questionId,
                watchedVideoEntity.ocrText,
                watchedVideoEntity.thumbnailImage,
                watchedVideoEntity.bgColor,
                watchedVideoEntity.duration,
                watchedVideoEntity.shareCount,
                watchedVideoEntity.likeCount,
                watchedVideoEntity.html,
                watchedVideoEntity.isLiked,
                watchedVideoEntity.sharingMessage,
                watchedVideoEntity.views.getAsViewsCountString(),
                watchedVideoEntity.resourceType
            )

        }
}