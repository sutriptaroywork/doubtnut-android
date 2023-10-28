package com.doubtnutapp.data.videoPage.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.videoPage.model.ApiVideoData
import com.doubtnutapp.data.videoPage.model.ApiVideoDislikeFeedbackOptions
import com.doubtnutapp.domain.similarVideo.models.ApiRecommendedClasses
import com.doubtnutapp.domain.videoPage.entities.ViewOnboardingEntity
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface VideoPageService {

    @POST("v13/answers/view-answer-by-question-id")
    fun getVideo(@Body params: RequestBody): Single<ApiResponse<ApiVideoData>>

    @POST("v4/feedback/video-add")
    fun videoLikedDisliked(@Body requestBody: RequestBody): Completable

    @POST("v1/sharing/whatsapp")
    fun videoShared(@Body requestBody: RequestBody): Completable

    @POST("/v10/answers/update-answer-view")
    fun updateVideoView(@Body bodyParam: RequestBody): Completable

    @GET("v1/feedback/video-dislike-options")
    fun getMatchFailureOptions(@Query("source") source: String): Single<ApiResponse<List<ApiVideoDislikeFeedbackOptions>>>

    @PUT("v1/feedback/video-dislike-options")
    fun postVideoDislikeFeedback(@Body feedback: RequestBody): Completable

    @POST("/v6/answers/view-onboarding")
    fun publishViewOnboarding(
        @Body params: RequestBody
    ): Single<ApiResponse<ViewOnboardingEntity>>

    @PUT("/v1/ads/update-engagetime")
    fun updateAdVideoView(@Body bodyParam: RequestBody): Completable

    @GET("v1/answers/get-video-top-widget/{questionId}")
    suspend fun getRecommendedOrPopularVideos(
        @Path("questionId")questionId: String
    ): ApiRecommendedClasses
}
