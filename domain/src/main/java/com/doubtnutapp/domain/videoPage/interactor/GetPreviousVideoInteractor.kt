package com.doubtnutapp.domain.videoPage.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.videoPage.entities.VideoDataEntity
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Single
import javax.inject.Inject

class GetPreviousVideoInteractor @Inject constructor(private val videoPageRepository: VideoPageRepository) :
    SingleUseCase<VideoDataEntity, GetPreviousVideoInteractor.None> {

    override fun execute(param: None): Single<VideoDataEntity> =
        videoPageRepository.getPreviousVideo()

    @Keep
    class None
}
