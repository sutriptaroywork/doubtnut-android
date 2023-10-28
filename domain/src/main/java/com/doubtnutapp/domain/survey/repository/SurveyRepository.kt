package com.doubtnutapp.domain.survey.repository

import com.doubtnutapp.domain.survey.entities.ApiCheckSurvey
import com.doubtnutapp.domain.survey.entities.ApiSurvey
import io.reactivex.Completable
import io.reactivex.Single

interface SurveyRepository {

    fun getSurveyDetails(surveyId: Long, page: String?, type: String?): Single<ApiSurvey>

    fun storeSurveyFeedback(surveyId: Long, questionId: Long?, feedback: String?): Completable

    fun checkSurveyByUser(page: String?, type: String?): Single<ApiCheckSurvey>
}
