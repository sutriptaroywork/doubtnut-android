package com.doubtnutapp.domain.videoPage.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Completable
import javax.inject.Inject

class UpdateAdVideoViewInteractor @Inject constructor(private val videoPageRepository: VideoPageRepository) :
    CompletableUseCase<UpdateAdVideoViewInteractor.Param> {

    override fun execute(param: Param): Completable = videoPageRepository.updateAdVideoView(
        param.adUuid,
        param.adId, param.engagementTime
    )

    @Keep
    class Param(
        val adUuid: String,
        val adId: String,
        val engagementTime: String
    )
}
