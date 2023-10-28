package com.doubtnutapp.data.survey.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.domain.survey.entities.ApiCheckSurvey
import com.doubtnutapp.domain.survey.entities.ApiSurvey
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface UserSurveyService {

    @GET("v1/student/get-survey-details/{survey_id}")
    fun getSurveyDetails(
        @Path(value = "survey_id") surveyId: Long,
        @Query(value = "page") page: String?,
        @Query(value = "type") type: String?
    ): Single<ApiResponse<ApiSurvey>>

    @PUT("v1/student/store-survey-feedback")
    fun storeSurveyFeedback(@Body body: RequestBody): Completable

    @GET("v1/student/check-survey-by-user")
    fun checkSurveyByUser(
        @Query(value = "page") page: String?,
        @Query(value = "type") type: String?
    ): Single<ApiResponse<ApiCheckSurvey>>
}
