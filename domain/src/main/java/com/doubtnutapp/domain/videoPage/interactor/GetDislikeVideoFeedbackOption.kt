package com.doubtnutapp.domain.videoPage.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.videoPage.entities.VideoDislikeFeedbackOptionEntity
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Single
import javax.inject.Inject

class GetDislikeVideoFeedbackOption @Inject constructor(private val videoPageRepository: VideoPageRepository) : SingleUseCase<List<VideoDislikeFeedbackOptionEntity>, GetDislikeVideoFeedbackOption.Param> {

    override fun execute(param: Param): Single<List<VideoDislikeFeedbackOptionEntity>> = videoPageRepository.getVideoDislikeOptions(param.source)

    @Keep
    class Param(val source: String)
}
