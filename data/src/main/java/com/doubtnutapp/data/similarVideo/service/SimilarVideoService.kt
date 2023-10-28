package com.doubtnutapp.data.similarVideo.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.similarVideo.model.ApiSimilarPlaylist
import com.doubtnutapp.data.similarVideo.model.ApiSimilarVideo
import com.doubtnutapp.data.similarVideo.model.ApiSimilarVideoList
import com.doubtnutapp.domain.videopageliveclass.model.ApiVideoPageSimilarLiveClass
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface SimilarVideoService {

    @POST("v3/feedback/video-add")
    fun similarVideoLiked(@Body requestBody: RequestBody): Completable

    @POST("v1/sharing/whatsapp")
    fun similarVideoShared(@Body requestBody: RequestBody): Completable

    @POST("v11/answers/view-similar-questions")
    fun getSimilarVideo(@Body params: RequestBody): Single<ApiResponse<ApiSimilarVideo>>

    @POST("v1/feedback/submit")
    fun submitSimilarQuestionFeedback(@Body requestBody: RequestBody): Completable

    @POST("v2/community/add-question")
    fun postQuestionToCommunity(@Body requestBody: RequestBody): Completable

    @GET("v1/answers/get-topic-video-by-questionid")
    fun getSimilarPlaylist(@Query("question_id") questionId: String): Single<ApiResponse<ApiSimilarPlaylist>>

    @FormUrlEncoded
    @POST("{submitUrlEndpoint}")
    fun submitQuestionResponse(
        @Path("submitUrlEndpoint", encoded = true) submitUrlEndpoint: String,
        @Field("response") response: String,
        @Field("question_id") questionId: String,
        @Field("widget_type") widgetName: String
    ): Completable

    @GET("v1/question/get-topic-data/{questionId}")
    fun getTopicBoosterData(@Path("questionId") questionId: String): Single<ApiResponse<List<ApiSimilarVideoList>>>

    @GET("v1/liveclass/video-page-data/{questionId}/{status}")
    fun getSimilarLiveClassData(
        @Path("questionId") questionId: String,
        @Path("status") status: String
    ): Single<ApiResponse<ApiVideoPageSimilarLiveClass>>

    @PUT("v1/answers/set-popular-widget-click")
    suspend fun clickOnWidget(): Any
}
