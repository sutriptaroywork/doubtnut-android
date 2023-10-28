package com.doubtnutapp.domain.textsolution.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.textsolution.repository.TextSolutionRepository
import io.reactivex.Completable
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
class LikedDislikedTextSolutionInteractor @Inject constructor(private val textSolutionRepository: TextSolutionRepository) : CompletableUseCase<LikedDislikedTextSolutionInteractor.Param> {

    override fun execute(param: Param): Completable = textSolutionRepository.videoLikedDisliked(param.videoName, param.questionId, param.answerId, param.viewtime, param.screenName, param.isLiked, param.feedback)

    @Keep
    class Param(val videoName: String, val questionId: String, val answerId: String, val viewtime: String, val screenName: String, val isLiked: Boolean, val feedback: String)
}
