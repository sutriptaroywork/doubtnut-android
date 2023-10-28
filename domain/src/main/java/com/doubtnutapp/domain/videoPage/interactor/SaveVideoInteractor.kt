package com.doubtnutapp.domain.videoPage.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.videoPage.entities.VideoDataEntity
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Completable
import javax.inject.Inject

class SaveVideoInteractor @Inject constructor(private val videoPageRepository: VideoPageRepository) : CompletableUseCase<SaveVideoInteractor.Param> {

    override fun execute(param: Param): Completable = videoPageRepository.saveVideoData(param.videoDataEntity)

    @Keep
    class Param(val videoDataEntity: VideoDataEntity)
}
