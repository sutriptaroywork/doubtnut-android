package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.dictionary.DictionaryResponse
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.models.userstatus.StatusApiResponse
import com.doubtnutapp.data.remote.models.userstatus.StatusAttachment
import com.doubtnutapp.data.remote.models.userstatus.StatusMetaCount
import com.doubtnutapp.data.remote.models.userstatus.StatusMetaDetail
import com.doubtnutapp.login.model.OtpOverCall
import com.doubtnutapp.notification.model.MaarkAsReadData
import com.doubtnutapp.notification.model.NotificationCountData
import com.doubtnutapp.payment.ApbCashPaymentData
import com.doubtnutapp.payment.ApbLocationData
import com.doubtnutapp.studygroup.model.CreateStudyGroup
import com.doubtnutapp.studygroup.model.StudyGroupDashboardMessage
import com.doubtnutapp.studygroup.model.StudyGroupWrappedMessage
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*
import java.util.*

interface MicroService {

    @POST("api/liveclass/poll/submit")
    fun submitLiveClassPoll(
        @Body params: RequestBody
    ): Single<ApiResponse<LiveClassPollSubmitResponse>>

    @GET("/api/liveclass/poll/result/{pollId}/{publishId}")
    fun getPollResult(
        @Path(value = "pollId") pollId: String,
        @Path(value = "publishId") publishId: String
    ): Single<ApiResponse<List<LiveClassPollOptionsData>>>

    @POST("api/liveclass/feedback/submit")
    fun submitLiveClassFeedback(
        @Body params: RequestBody
    ): Single<ApiResponse<LiveClassFeedbackResponse>>

    @POST("api/liveclass/feedback/viewed")
    fun viewedLiveClassFeedback(
        @Body params: RequestBody
    ): Single<ApiResponse<LiveClassFeedbackResponse>>

    @GET("/api/liveclass/feedback/{detailId}/status")
    fun isFeedbackRequired(
        @Path(value = "detailId") detailId: String
    ): Single<ApiResponse<FeedbackStatusResponse>>

    @GET("/api/chatroom/messages/{roomId}/{roomType}")
    fun getMessages(
        @Path(value = "roomId") roomId: String,
        @Path(value = "roomType") roomType: String,
        @Query(value = "page") page: Int,
        @Query(value = "offset_cursor") offset: String? = null
    ): Single<ApiResponse<LiveClassChatResponse>>

    @GET("/api/chatroom/messages/{roomId}/liveclass_paid")
    fun getP2pMessages(
        @Path(value = "roomId") roomId: String,
        @Query(value = "page") page: Int,
        @Query(value = "offset_cursor") offset: String? = null
    ): Single<ApiResponse<StudyGroupWrappedMessage>>

    @POST("/api/chatroom_bans/ban")
    fun banUser(@Body params: RequestBody): Single<LiveClassChatResponse>

    @POST("api/chatroom_report/report")
    fun reportMessage(@Body params: RequestBody): Single<ReportUserResponse>

    // video download service region start

    @GET("api/video-download/validity")
    fun checkValidity(@Query("questionId") questionId: String): Call<Date>

    @GET("api/video-download/rental-license")
    fun getLicense(@Query("questionId") questionId: String): Observable<VideoLicenseResponse>

    @GET("api/video-download/options")
    fun getDownloadOptions(@Query("questionId") questionId: String): Observable<VideoDownloadResponse>

    // video download service region end

    // notification center service region start

    @POST("api/newton/sync/{pageNo}")
    fun getAllNotifications(@Body params: RequestBody, @Path("pageNo") pageNo: Int):
        Single<ApiResponse<List<NotificationCenterData>>>

    @POST("api/newton/markAsRead")
    fun updateSeenNotifications(@Body params: RequestBody):
        Single<ApiResponse<MaarkAsReadData>>

    @GET("api/newton/count/new")
    fun getUnreadNotificationCount(): Single<ApiResponse<NotificationCountData>>

    // notification center service region end

    // app config service region start

    @POST("api/app-config/flagr")
    fun getFeatureConfig(@Body requestedFeatures: HashMap<String, Any>): Single<HashMap<String, Any>>

    // Otp Over Call
    @PUT("otp/send-call")
    fun otpOverCall(@Body params: RequestBody): Single<OtpOverCall>

    // app config service region end

    // wallet service start
    @GET("api/v1/wallet/info")
    suspend fun fetchWalletData(): ApiResponse<WalletData>
    // wallet service end

    @GET("api/v1/stories/stories")
    fun getStories(
        @Query(value = "page") page: Int,
        @Query(value = "type") type: String,
        @Query("offsetCursor") offsetCursor: String
    ): RetrofitLiveData<ApiResponse<StatusApiResponse>>

    @GET("api/v1/stories/popular/stories")
    fun getPopularStories(
        @Query(value = "page") page: Int,
        @Query("offsetCursor") offsetCursor: String
    ): Single<ApiResponse<StatusApiResponse>>

    @GET("api/v1/stories/ads")
    fun getStatusAds(): Single<ApiResponse<StatusApiResponse>>

    @GET("api/v1/stories/storyMetaCount/{status_id}")
    fun getStoryMetaCount(@Path(value = "status_id") statusId: String): RetrofitLiveData<ApiResponse<ArrayList<StatusMetaCount>>>

    @GET("api/v1/stories/storyMeta/{status_id}")
    fun getStoryMetaDetail(
        @Path(value = "status_id") statusId: String,
        @Query(value = "type") type: String
    ): RetrofitLiveData<ApiResponse<StatusMetaDetail>>

    @POST("api/v1/stories/action")
    fun postStoryAction(@Body body: RequestBody): Single<ApiResponse<Any>>

    @POST("api/v1/stories/story")
    fun addStory(@Body body: RequestBody): Single<ApiResponse<StatusAttachment>>

    @POST("api/v1/stories/report/{status_id}")
    fun reportStory(@Path(value = "status_id") statusId: String): Single<ApiResponse<Any>>

    @GET("api/v1/stories/delete/{status_id}")
    fun deleteStory(@Path(value = "status_id") statusId: String): Single<ApiResponse<Any>>

    //region PDF Service

    @POST("api/pdf/question-ask")
    fun getPdfUrl(@Body requestBody: RequestBody): Single<PdfUrlData>

    //endregion

    // airtel payment bank
    @GET("api/apb/screen")
    fun getApbCashPaymentData(): Single<ApiResponse<ApbCashPaymentData>>

    @GET("api/apb/locationScreen")
    fun getApbLocationData(
        @Query(value = "lat") lat: String?,
        @Query(value = "long") long: String?
    ): Single<ApiResponse<ApbLocationData>>

    @GET("api/dictionary/meaning/{word}/{locale}")
    fun getWordMeaning(
        @Path(value = "word") word: String,
        @Path(value = "locale") locale: String
    ): Single<com.doubtnutapp.data.common.model.ApiResponse<DictionaryResponse?>>

    @POST("api/study-group/admin-dashboard")
    fun getSgAdminDashboard(
        @Body requestBody: RequestBody,
    ): Single<ApiResponse<StudyGroupDashboardMessage>>

    @POST("api/study-group/student-reported-messages")
    fun getStudentReportedMessages(
        @Body requestBody: RequestBody,
    ): Single<ApiResponse<StudyGroupDashboardMessage>>

    @POST("api/study-group/create")
    fun createGroup(@Body requestBody: RequestBody): Single<ApiResponse<CreateStudyGroup>>
}
