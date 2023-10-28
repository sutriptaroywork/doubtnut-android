package com.doubtnutapp.domain.similarVideo.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import io.reactivex.Completable
import javax.inject.Inject

class SubmitSimilarTopicBoosterQuestion @Inject constructor(private val similarVideoRepository: SimilarVideoRepository) : CompletableUseCase<SubmitSimilarTopicBoosterQuestion.Param> {

    override fun execute(param: Param): Completable =
        similarVideoRepository.submitQuestionResponse(param.response, param.questionId, param.submitUrlEndpoint, param.widgetName)

    @Keep
    class Param(val response: String, val questionId: String, val submitUrlEndpoint: String, val widgetName: String)
}
