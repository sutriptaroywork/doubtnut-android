package com.doubtnutapp.domain.likeDislike.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.likeDislike.repository.LikeDisLikeRepository
import io.reactivex.Completable
import javax.inject.Inject

class LikeDisLikeVideo @Inject constructor(private val likeDisLikeRepository: LikeDisLikeRepository) : CompletableUseCase<LikeDisLikeVideo.Param> {

    override fun execute(param: Param): Completable = likeDisLikeRepository.likeDisLike(param.questionId, param.screenName, param.isLiked)

    @Keep
    class Param(val questionId: String, val screenName: String, val isLiked: Boolean)
}
