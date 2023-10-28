package com.doubtnutapp.domain.videoPage.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.videoPage.entities.ViewOnboardingEntity
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Single
import javax.inject.Inject

class PublishViewOnboarding @Inject constructor(private val videoPageRepository: VideoPageRepository) :
    SingleUseCase<ViewOnboardingEntity, PublishViewOnboarding.RequestValues> {

    override fun execute(param: RequestValues): Single<ViewOnboardingEntity> = videoPageRepository.publishViewOnboarding(
        param.page, param.videoTime, param.engagementTime, param.studentId, param.questionId
    )

    @Keep
    class RequestValues(
        val page: String,
        val videoTime: String,
        val engagementTime: String,
        val studentId: String,
        val questionId: String
    )
}
