package com.doubtnutapp.doubtpecharcha.service

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.doubtpecharcha.model.*
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface DoubtPeCharchaApiService {

    @POST("v1/p2p/connect")
    fun connectToPeer(@Body params: RequestBody): Single<ApiResponse<DoubtP2PData>>

    @POST("v1/p2p/list-members")
    fun getListMembers(@Body params: RequestBody): Single<ApiResponse<P2pListMember>>

    @POST("v1/doubt/feedback")
    fun submitFeedback(@Query("feedback") feedback: String): Completable

    @POST("v1/p2p/feedback")
    fun submitFeedback(@Body params: RequestBody): Completable

    @POST("v1/p2p/add-member")
    fun addMember(@Body params: RequestBody): Single<ApiResponse<DoubtP2PAddMember>>

    @POST("v1/p2p/deactivate")
    fun disconnectFromRoom(@Body params: RequestBody): Single<ApiResponse<DoubtP2PDisconnect>>

    @GET("v1/p2p/get-question-thumbnail/{question_id}")
    fun getQuestionThumbnail(@Path("question_id") questionId: String): Single<ApiResponse<DoubtP2PQuestionThumbnail>>

    @POST("v2/p2p/home")
    fun getDoubtData(@Body params: RequestBody): Single<ApiResponse<P2PDoubtTypes>>

    @POST("v2/p2p/doubts")
    fun getDoubtsForPagination(@Body params: RequestBody): Single<ApiResponse<P2PDoubtTypes>>

    @POST("v1/p2p/helper-data")
    fun getHelperData(@Body params: RequestBody): Single<ApiResponse<P2pRoomData>>

    @POST("v1/p2p/similar-solved-doubts")
    fun getMatchPageDoubts(@Body params: RequestBody): Single<ApiResponse<P2PDoubtData>>

    @GET("https://run.mocky.io/v3/f0d18ebd-53a6-49b4-ac15-6d2006143e5a")
    suspend fun getDoubtPeCharchaRewards(): DoubtPeCharchaRewardsResponse

    @POST("v2/p2p/feedback-data")
    suspend fun getDoubtPeCharchaFeedbackData(@Body requestBody: RequestBody): ApiResponse<FeedbackUserListResponse>

    @POST("v1/p2p/feedback")
    suspend fun selectUsersForFeedback(@Body requestBody: RequestBody): ApiResponse<BaseResponse>

    @POST("v2/p2p/mark-solved")
    suspend fun markQuestionSolved(@Body requestBody: RequestBody): ApiResponse<BaseResponse>
}
