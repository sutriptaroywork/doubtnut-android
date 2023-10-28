package com.doubtnutapp.domain.survey.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.survey.entities.ApiCheckSurvey
import com.doubtnutapp.domain.survey.repository.SurveyRepository
import io.reactivex.Single
import javax.inject.Inject

class CheckSurveyUseCase @Inject constructor(
    private val surveyRepository: SurveyRepository
) : SingleUseCase<ApiCheckSurvey, CheckSurveyUseCase.Param> {

    override fun execute(param: Param): Single<ApiCheckSurvey> =
        surveyRepository.checkSurveyByUser(param.page, param.type)

    @Keep
    data class Param(
        val page: String?,
        val type: String?
    )
}
