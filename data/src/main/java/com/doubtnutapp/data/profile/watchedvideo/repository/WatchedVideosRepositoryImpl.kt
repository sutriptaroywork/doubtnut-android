package com.doubtnutapp.data.profile.watchedvideo.repository

import com.doubtnutapp.data.profile.watchedvideo.mapper.WatchedVideoListEntityMapper
import com.doubtnutapp.data.profile.watchedvideo.service.WatchedVideoService
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoListEntity
import com.doubtnutapp.domain.profile.watchedvideo.repository.WatchedVideoRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class WatchedVideosRepositoryImpl @Inject constructor(
    private val watchedVideoVideoService: WatchedVideoService,
    private val watchedVideoListEntityMapper: WatchedVideoListEntityMapper
) : WatchedVideoRepository {

    override fun watchedVideosLiked(questionId: String, screen: String, isLiked: Boolean): Completable {
        val rating: String = if (isLiked) {
            "5"
        } else {
            "3"
        }

        val bodyParam = hashMapOf(
            "page" to screen,
            "question_id" to questionId,
            "rating" to rating,
            "feedback" to "",
            "view_time" to "",
            "answer_id" to "",
            "answer_video" to ""
        ).toRequestBody()

        return watchedVideoVideoService.watchedVideosLiked(bodyParam)
    }

    override fun watchedVideosShared(questionId: String): Completable {
        val bodyParam = hashMapOf(
            "entity_type" to "video",
            "entity_id" to questionId
        ).toRequestBody()

        return watchedVideoVideoService.watchedVideosShared(bodyParam)
    }

    private val PLAYLIST_NAME_HISTORY = "HISTORY"

    override fun getWatchedVideos(pageNumber: Int): Single<WatchedVideoListEntity> =
        watchedVideoVideoService.getWatchedVideosList(pageNumber, PLAYLIST_NAME_HISTORY).map { response ->
            watchedVideoListEntityMapper.map(response.data)
        }
}
