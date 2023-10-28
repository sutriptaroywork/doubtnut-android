package com.doubtnutapp.domain.videoPage.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Completable
import javax.inject.Inject

class UpdateVideoViewInteractor @Inject constructor(private val videoPageRepository: VideoPageRepository) :
    CompletableUseCase<UpdateVideoViewInteractor.Param> {

    override fun execute(param: Param): Completable = videoPageRepository.updateVideoView(
        param.viewId,
        param.isBack,
        param.maxSeekTime,
        param.engagementTime,
        param.scheduled,
        param.lockUnlockLogs,
        param.networkBytes
    )

    @Keep
    class Param(
        val viewId: String,
        val isBack: String,
        val maxSeekTime: String,
        val engagementTime: String,
        val lockUnlockLogs: String?,
        val scheduled: Boolean = false,
        val networkBytes: String = ""
    )
}
