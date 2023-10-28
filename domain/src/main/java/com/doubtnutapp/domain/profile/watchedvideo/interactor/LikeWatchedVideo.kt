package com.doubtnutapp.domain.profile.watchedvideo.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.profile.watchedvideo.interactor.LikeWatchedVideo.Param
import com.doubtnutapp.domain.profile.watchedvideo.repository.WatchedVideoRepository
import io.reactivex.Completable
import javax.inject.Inject

class LikeWatchedVideo @Inject constructor(private val watchedVideoRepository: WatchedVideoRepository) : CompletableUseCase<Param> {

    override fun execute(param: Param): Completable = watchedVideoRepository.watchedVideosLiked(param.questionId, param.screenName, param.isLiked)

    @Keep
    class Param(val questionId: String, val screenName: String, val isLiked: Boolean)
}
