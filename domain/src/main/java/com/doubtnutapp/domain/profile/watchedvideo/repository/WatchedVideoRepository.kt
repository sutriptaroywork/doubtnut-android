package com.doubtnutapp.domain.profile.watchedvideo.repository

import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoListEntity
import io.reactivex.Completable
import io.reactivex.Single

interface WatchedVideoRepository {

    fun getWatchedVideos(pageNumber: Int): Single<WatchedVideoListEntity>

    fun watchedVideosLiked(questionId: String, screen: String, isLiked: Boolean): Completable

    fun watchedVideosShared(questionId: String): Completable
}
