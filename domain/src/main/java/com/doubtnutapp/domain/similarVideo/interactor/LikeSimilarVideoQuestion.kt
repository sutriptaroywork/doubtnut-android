package com.doubtnutapp.domain.similarVideo.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import io.reactivex.Completable
import javax.inject.Inject

class LikeSimilarVideoQuestion @Inject constructor(private val similarVideoRepository: SimilarVideoRepository) : CompletableUseCase<LikeSimilarVideoQuestion.Param> {

    override fun execute(param: Param): Completable = similarVideoRepository.similarVideoLiked(param.questionId, param.screenName, param.isLiked)

    @Keep
    class Param(val questionId: String, val screenName: String, val isLiked: Boolean)
}
