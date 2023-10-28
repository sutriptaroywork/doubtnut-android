package com.doubtnutapp.data.profile.watchedvideo.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.profile.watchedvideo.model.ApiWatchedVideo
import com.doubtnutapp.data.profile.watchedvideo.model.ApiWatchedVideoDataList
import com.doubtnutapp.data.profile.watchedvideo.model.ApiWatchedVideoMetaInfo
import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoEntity
import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoListEntity
import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoMetaInfoEntity
import javax.inject.Inject

class WatchedVideoListEntityMapper @Inject constructor(
    private val watchedVideoEntityMapper: WatchedVideoEntityMapper,
    private val watchedVideoMetaInfoEntityMapper: WatchedVideoMetaInfoEntityMapper

) : Mapper<ApiWatchedVideoDataList, WatchedVideoListEntity> {

    override fun map(srcObject: ApiWatchedVideoDataList): WatchedVideoListEntity = with(srcObject) {
        WatchedVideoListEntity(
            watchedVideo?.let { getWatchedVideoData(it) },
            noWatchedData?.let { getNoVideoWatchedData(it) }
        )
    }

    private fun getWatchedVideoData(searchResultList: List<ApiWatchedVideo>): List<WatchedVideoEntity> =
        searchResultList.map {
            watchedVideoEntityMapper.map(it)
        }

    private fun getNoVideoWatchedData(searchTabs: List<ApiWatchedVideoMetaInfo>): List<WatchedVideoMetaInfoEntity> =
        searchTabs.map {
            watchedVideoMetaInfoEntityMapper.map(it)
        }
}
