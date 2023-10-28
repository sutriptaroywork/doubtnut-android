package com.doubtnutapp.domain.videoPage.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.videoPage.entities.VideoDataEntity
import com.doubtnutapp.domain.videoPage.repository.VideoPageRepository
import io.reactivex.Single
import javax.inject.Inject

class GetVideoData @Inject constructor(private val videoPageRepository: VideoPageRepository) :
    SingleUseCase<VideoDataEntity, GetVideoData.Param> {

    override fun execute(param: Param): Single<VideoDataEntity> = videoPageRepository.getVideoData(
        param.questionId,
        param.playListId,
        param.mcId,
        param.page,
        param.mcClass,
        param.referredStudentId,
        param.parentId,
        param.isFromTopicBooster,
        param.token,
        param.youtube_id,
        param.ocr,
        param.isInPipMode,
        param.isNetworkSlow,
        param.isFilter,
        param.parentPage
    )

    @Keep
    data class Param(
        val questionId: String,
        val playListId: String?,
        val mcId: String?,
        val page: String,
        val mcClass: String?,
        val referredStudentId: String?,
        val parentId: String?,
        val isFromTopicBooster: Boolean,
        val token: String = "",
        val youtube_id: String? = null,
        val ocr: String? = null,
        val isInPipMode: Boolean = false,
        val isNetworkSlow: Boolean,
        val isFilter: Boolean,
        val parentPage: String? = null
    )
}
