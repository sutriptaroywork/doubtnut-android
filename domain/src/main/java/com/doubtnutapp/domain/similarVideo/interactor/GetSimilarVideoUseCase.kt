package com.doubtnutapp.domain.similarVideo.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.similarVideo.entities.SimilarVideoEntity
import com.doubtnutapp.domain.similarVideo.repository.SimilarVideoRepository
import io.reactivex.Single
import javax.inject.Inject

class GetSimilarVideoUseCase @Inject constructor(private val similarVideoRepository: SimilarVideoRepository) :
    SingleUseCase<SimilarVideoEntity, GetSimilarVideoUseCase.Param> {

    override fun execute(param: Param): Single<SimilarVideoEntity> =
        similarVideoRepository.getSimilarVideo(
            param.questionId,
            param.mcId,
            param.playlistId,
            param.page,
            param.parentId,
            param.ocr,
            param.isFilter
        )

    @Keep
    data class Param(
        val questionId: String,
        val mcId: String?,
        val playlistId: String?,
        val page: String?,
        val parentId: String?,
        val ocr: String?,
        val isFilter: Boolean
    )
}
