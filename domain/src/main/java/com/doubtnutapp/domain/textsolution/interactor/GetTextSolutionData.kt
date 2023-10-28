package com.doubtnutapp.domain.textsolution.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.textsolution.entities.TextSolutionDataEntity
import com.doubtnutapp.domain.textsolution.repository.TextSolutionRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-28.
 */
class GetTextSolutionData @Inject constructor(private val textSolutionRepository: TextSolutionRepository) :
    SingleUseCase<TextSolutionDataEntity, GetTextSolutionData.Param> {

    override fun execute(param: Param): Single<TextSolutionDataEntity> =
        textSolutionRepository.getVideoData(
            param.questionId, param.playListId, param.mcId,
            param.page, param.mcClass, param.referredStudentId, param.parentId, param.ocrText, param.html
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
        val ocrText: String?,
        val html: String?
    )
}
