package com.doubtnutapp.domain.profile.watchedvideo.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.profile.watchedvideo.entities.WatchedVideoListEntity
import com.doubtnutapp.domain.profile.watchedvideo.repository.WatchedVideoRepository
import io.reactivex.Single
import javax.inject.Inject

class GetUsersWatchedVideo @Inject constructor(private val watchedVideoRepository: WatchedVideoRepository) : SingleUseCase<WatchedVideoListEntity, Int> {

    override fun execute(param: Int): Single<WatchedVideoListEntity> = watchedVideoRepository.getWatchedVideos(param)
}
