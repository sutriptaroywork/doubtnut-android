package com.doubtnutapp.domain.survey.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.survey.entities.ApiSurvey
import com.doubtnutapp.domain.survey.repository.SurveyRepository
import io.reactivex.Single
import javax.inject.Inject

class GetSurveyDetailsUseCase @Inject constructor(
    private val surveyRepository: SurveyRepository
) : SingleUseCase<ApiSurvey, GetSurveyDetailsUseCase.Param> {

    override fun execute(param: Param): Single<ApiSurvey> =
        surveyRepository.getSurveyDetails(param.surveyId, param.page, param.type)

    @Keep
    data class Param(
        val surveyId: Long,
        val page: String?,
        val type: String?
    )
}
