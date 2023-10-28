package com.doubtnutapp.domain.videoPage.interactor

import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Completable
import javax.inject.Inject

class ShareVideoInteractor @Inject constructor(private val videoPageRepository: VideoPageRepository) : CompletableUseCase<String> {

    override fun execute(param: String): Completable = videoPageRepository.videoShared(param)
}
