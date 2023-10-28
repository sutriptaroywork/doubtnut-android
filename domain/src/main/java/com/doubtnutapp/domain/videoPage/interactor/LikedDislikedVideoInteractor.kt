package com.doubtnutapp.domain.videoPage.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Completable
import javax.inject.Inject

class LikedDislikedVideoInteractor @Inject constructor(private val videoPageRepository: VideoPageRepository) : CompletableUseCase<LikedDislikedVideoInteractor.Param> {

    override fun execute(param: Param): Completable = videoPageRepository.videoLikedDisliked(param.videoName, param.questionId, param.answerId, param.viewtime, param.screenName, param.isLiked, param.feedback, param.viewId)

    @Keep
    class Param(val videoName: String, val questionId: String, val answerId: String, val viewtime: String, val screenName: String, val isLiked: Boolean, val feedback: String, val viewId: String)
}
