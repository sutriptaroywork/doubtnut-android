package com.doubtnutapp.domain.videopageliveclass.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import com.doubtnutapp.domain.videopageliveclass.model.ApiVideoPageSimilarLiveClass
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by devansh on 26/10/20.
 */

class GetVideoPageSimilarLiveClass @Inject constructor(
    private val similarVideoRepository: SimilarVideoRepository
) : SingleUseCase<ApiVideoPageSimilarLiveClass, GetVideoPageSimilarLiveClass.Param> {

    override fun execute(param: Param): Single<ApiVideoPageSimilarLiveClass> =
        similarVideoRepository.getSimilarLiveClassData(param.questionId, param.status)

    @Keep
    data class Param(val questionId: String, val status: String)
}
