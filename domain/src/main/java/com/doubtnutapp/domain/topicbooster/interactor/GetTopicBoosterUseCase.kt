package com.doubtnutapp.domain.topicbooster.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.similarVideo.entities.SimilarVideoEntity
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by devansh on 04/10/20.
 */

class GetTopicBoosterUseCase @Inject constructor(
    private val similarVideoRepository: SimilarVideoRepository
) : SingleUseCase<SimilarVideoEntity, GetTopicBoosterUseCase.Param> {

    override fun execute(param: Param): Single<SimilarVideoEntity> =
        similarVideoRepository.getTopicBoosterData(param.questionId)

    @Keep
    data class Param(val questionId: String)
}
