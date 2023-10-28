package com.doubtnutapp.domain.profile.watchedvideo.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.profile.watchedvideo.repository.WatchedVideoRepository
import io.reactivex.Completable
import javax.inject.Inject

class ShareWatchedVideo @Inject constructor(private val watchedVideoRepository: WatchedVideoRepository) : CompletableUseCase<String> {

    override fun execute(param: String): Completable = watchedVideoRepository.watchedVideosShared(param)
}
