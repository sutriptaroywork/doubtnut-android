package com.doubtnutapp.data.survey.repository

import com.doubtnutapp.data.survey.service.UserSurveyService
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.survey.entities.ApiCheckSurvey
import com.doubtnutapp.domain.survey.entities.ApiSurvey
import com.doubtnutapp.domain.survey.repository.SurveyRepository
import io.reactivex.Completable
import io.reactivex.Single
import javax.inject.Inject

class SurveyRepositoryImpl @Inject constructor(
    private val userSurveyService: UserSurveyService
) : SurveyRepository {

    override fun getSurveyDetails(surveyId: Long, page: String?, type: String?): Single<ApiSurvey> =
        userSurveyService.getSurveyDetails(surveyId, page, type).map {
            it.data
        }

    override fun storeSurveyFeedback(
        surveyId: Long,
        questionId: Long?,
        feedback: String?
    ): Completable {
        val requestBody = hashMapOf<String, Any>()
        requestBody["survey_id"] = surveyId
        questionId?.let { requestBody["question_id"] = it }
        feedback?.let { requestBody["feedback"] = it }

        return userSurveyService.storeSurveyFeedback(requestBody.toRequestBody())
    }

    override fun checkSurveyByUser(page: String?, type: String?): Single<ApiCheckSurvey> {
        return userSurveyService.checkSurveyByUser(page, type).map {
            it.data
        }
    }
}
