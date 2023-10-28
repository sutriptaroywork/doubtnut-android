package com.doubtnutapp.domain.similarVideo.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import io.reactivex.Completable
import javax.inject.Inject

class PostQuestionToCommunity @Inject constructor(
    private val similarVideoRepository: SimilarVideoRepository
) : CompletableUseCase<PostQuestionToCommunity.Param> {

    override fun execute(param: Param): Completable = similarVideoRepository.postQuestionToCommunity(param.questionId, param.chapter)

    @Keep
    class Param(val questionId: String, val chapter: String)
}
