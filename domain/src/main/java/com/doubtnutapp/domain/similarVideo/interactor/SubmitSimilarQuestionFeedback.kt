package com.doubtnutapp.domain.similarVideo.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import io.reactivex.Completable
import javax.inject.Inject

class SubmitSimilarQuestionFeedback @Inject constructor(private val similarVideoRepository: SimilarVideoRepository) : CompletableUseCase<Int> {

    override fun execute(param: Int): Completable = similarVideoRepository.submitSimilarQuestionFeedback(param)
}
