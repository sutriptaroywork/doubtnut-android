package com.doubtnutapp.domain.survey.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.CompletableUseCase
import com.doubtnutapp.domain.survey.repository.SurveyRepository
import io.reactivex.Completable
import javax.inject.Inject

class StoreSurveyFeedbackUseCase @Inject constructor(
    private val surveyRepository: SurveyRepository
) : CompletableUseCase<StoreSurveyFeedbackUseCase.Param> {

    override fun execute(param: Param): Completable =
        surveyRepository.storeSurveyFeedback(param.surveyId, param.questionId, param.feedback)

    @Keep
    data class Param(val surveyId: Long, val questionId: Long?, val feedback: String?)
}
