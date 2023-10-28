package com.doubtnutapp.domain.videoPage.interactor

import com.doubtnutapp.domain.base.SingleUseCaseWithoutParam
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by devansh on 30/12/20.
 */

class GetVideoViewStackSizeUseCase @Inject constructor(
    private val videoPageRepository: VideoPageRepository
) : SingleUseCaseWithoutParam<Int> {

    override fun execute(): Single<Int> = videoPageRepository.getVideoViewStackSize()
}
