package com.doubtnutapp.matchquestion.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.matchquestion.model.*
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface MatchQuestionService {

    @POST("v10/questions/ask")
    fun getMatches(@Body params: RequestBody): Single<ApiResponse<ApiAskQuestionResponse>>

    @POST("v1/question/generate-question-image-upload-url")
    fun getSignedUrl(@Body requestBody: RequestBody): Single<ApiResponse<ApiSignedUrl>>

    @GET("/v1/gamification/get-srp-banner")
    fun getMatchQuestionBanner(): Single<ApiResponse<ApiMatchQuestionBanner>>

    @GET("v1/feedback/match-failure-options")
    fun getMatchFailureOptions(@Query(value = "source") source: String): Single<ApiResponse<MatchFailureOption>>

    @PUT("v2/feedback/match-failure")
    fun postMatchFailureFeedback(@Body requestBody: RequestBody): Single<ApiResponse<ApiMatchFeedback>>

    @POST("v12/answers/advanced-search")
    fun filterMatches(@Body params: MatchFilterRequest): Single<ApiResponse<ApiAskQuestionResponse>>

    @POST("/v9/questions/advance-search-facets")
    fun getAdvancedSearchOptions(@Body params: RequestBody): Single<ApiResponse<ApiAdvancedSearchOptions>>

    @GET("v1/matchpage/get-carousels/{question_id}")
    fun getCarousals(@Path("question_id") questionId: String): Single<ApiResponse<MatchCarousalsData>>

    @POST("v1/feedback/get-popup-data")
    suspend fun getPopupData(@Body params: RequestBody): ApiResponse<ApiPopupData>

    @POST("v1/feedback/submit-popup-selections")
    suspend fun submitPopupSelection(@Body params: RequestBody): ApiResponse<ApiPopupData>

    @POST("v1/feedback/submit-feedback-prefrences")
    suspend fun submitFeedbackOption(@Body params: RequestBody): ApiResponse<ApiSubmitFeedbackPreference>

    @POST("v1/sharing/whatsapp")
    fun matchedQuestionShared(@Body requestBody: RequestBody): Completable

    @POST("v2/community/add-question")
    fun postQuestionToCommunity(@Body requestBody: RequestBody): Completable

    @POST("v2/questions/get-matches-by-filename")
    fun getMatchesByFileName(@Body params: RequestBody): Single<ApiResponse<ApiAskQuestionResponse>>

    @GET("/v2/camera/get-settings")
    fun getCameraSettingConfig(
        @Query(value = "openCount") openCount: String,
        @Query("studentClass") studentClass: String
    ): Single<ApiResponse<CameraSettingEntity>>

    @POST("v1/question/youtube-search")
    fun getYoutubeResults(@Body params: RequestBody): Single<ApiResponse<ApiYoutubeMatchResult>>

    @GET("v1/question/get-srp-widgets")
    fun getSrpNudges(@Query("question_id") questionId: String): Single<ApiResponse<MatchPageNudgesData>>
}