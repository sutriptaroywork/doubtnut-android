package com.doubtnutapp.data.remote.api.services

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.Constants
import com.doubtnutapp.bottomnavigation.model.BottomNavigationItemData
import com.doubtnutapp.bottomnavigation.model.BottomNavigationTabsData
import com.doubtnutapp.common.data.BookCallData
import com.doubtnutapp.course.widgets.VpaWidget
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.models.doubtfeed.*
import com.doubtnutapp.data.remote.models.doubtfeed2.DailyGoalSubmitResponse
import com.doubtnutapp.data.remote.models.feed.TopOptionsWidgetData
import com.doubtnutapp.data.remote.models.mocktest.MockTestAnalysisData
import com.doubtnutapp.data.remote.models.mocktest.MockTestCourseData
import com.doubtnutapp.data.remote.models.mocktest.MockTestListData
import com.doubtnutapp.data.remote.models.quiztfs.*
import com.doubtnutapp.data.remote.models.revisioncorner.*
import com.doubtnutapp.data.remote.models.reward.MarkAttendanceModel
import com.doubtnutapp.data.remote.models.reward.RewardDetails
import com.doubtnutapp.data.remote.models.topicboostergame.*
import com.doubtnutapp.data.remote.models.topicboostergame2.*
import com.doubtnutapp.data.remote.models.topicboostergame2.ChapterSelectionData
import com.doubtnutapp.data.videoPage.model.ApiVideoPagePlaylist
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.domain.payment.entities.PaymentStartBody
import com.doubtnutapp.domain.payment.entities.Taxation
import com.doubtnutapp.examcorner.model.ApiExamCornerBookmarkData
import com.doubtnutapp.examcorner.model.ApiExamCornerData
import com.doubtnutapp.feed.entity.TopIconsData
import com.doubtnutapp.freeclasses.bottomsheets.FilterListData
import com.doubtnutapp.icons.data.entity.IconsDetailResponse
import com.doubtnutapp.leaderboard.data.entity.LeaderboardData
import com.doubtnutapp.model.ActiveSlotApiModel
import com.doubtnutapp.model.InAppPopupResponse
import com.doubtnutapp.paymentplan.data.PaymentData
import com.doubtnutapp.quiztfs.widgets.DialogData
import com.doubtnutapp.sales.data.entity.CallingCardDismissRequest
import com.doubtnutapp.sales.data.entity.RequestCallbackRequest
import com.doubtnutapp.shorts.model.ShortsCategoryData
import com.doubtnutapp.shorts.model.ShortsListData
import com.doubtnutapp.teacher.model.TeacherListData
import com.doubtnutapp.ui.common.address.AddressFormData
import com.doubtnutapp.ui.games.GamesData
import com.doubtnutapp.ui.topperStudyPlan.ChapterDetailData
import com.doubtnutapp.ui.topperStudyPlan.StudyPlanData
import com.doubtnutapp.widgetmanager.widgets.DoubtFeedWidget
import com.doubtnutapp.widgets.data.entities.BaseWidgetData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Anand Gaurav on 24/11/20.
 */
interface NetworkService {

    // toppers service region start
    @POST("v1/personalize/get-details")
    fun getPersonalizeDetails(): RetrofitLiveData<ApiResponse<StudyPlanData>>

    @POST("v1/personalize/get-chapter-details")
    @FormUrlEncoded
    fun getChapterDetails(@Field("id") questionId: Long): RetrofitLiveData<ApiResponse<ChapterDetailData>>

    @POST("v1/personalize/get-next-book-chapter-details")
    @FormUrlEncoded
    fun getNextChapterDetails(@Field("question_id") questionId: Long): RetrofitLiveData<ApiResponse<ChapterDetailData>>
    // toppers service region end

    // game service region start
    @GET("/v1/games/list")
    fun fetchGamesList(@Query("order") source: String?): RetrofitLiveData<ApiResponse<GamesData>>

    @GET
    fun downloadGame(@Url gameUrl: String): Single<ResponseBody>
    // game service region start

    // mock text service region start
    @GET("v8/testseries/active_mock_test")
    fun getMockTestData(): RetrofitLiveData<ApiResponse<ArrayList<MockTestData>>>

    @GET("/v1/faq/get")
    fun getFaqData(
        @Query("bucket") bucket: String?,
        @Query("priority") priority: String?
    ): Single<ApiResponse<ApiFaqData>>

    @GET("v8/testseries/{test_id}/subscribe")
    fun getMockTestSubscribe(@Path(value = "test_id") testId: Int): RetrofitLiveData<ApiResponse<TestSubscribe>>

    @GET("v8/testseries/rules/{rules_id}")
    fun getMockTestRules(@Path(value = "rules_id") rulesId: Int): RetrofitLiveData<ApiResponse<TestRules>>

    @GET("v8/testseries/{test_id}/questionsdatabysections")
    fun getMockTestQuestions(@Path(value = "test_id") testId: Int): RetrofitLiveData<ApiResponse<MockTestQuestionData>>

    @POST("v8/testseries/response")
    fun getMockTestResponse(@Body body: RequestBody): RetrofitLiveData<ApiResponse<TestResponse>>

    @GET("v9/testseries/{test_subscription_id}/submit")
    fun getMockTestSubmit(@Path(value = "test_subscription_id") testSubscriptionId: Int): RetrofitLiveData<ApiResponse<TestSubmit>>

    @GET("v8/testseries/{testsubscriptionId}/mockresult")
    fun getMockTestResult(@Path(value = "testsubscriptionId") testSubscriptionId: Int): RetrofitLiveData<ApiResponse<MockTestResult>>

    @GET("v8/testseries/{test_id}/leaderboard")
    fun getMockTestLeaderboard(@Path(value = "test_id") testId: Int): RetrofitLiveData<ApiResponse<ArrayList<TestLeaderboard>>>

    @GET("v8/testseries/{testSubscriptionId}/responses")
    fun getSummary(@Path(value = "testSubscriptionId") testId: Int): RetrofitLiveData<ApiResponse<MockTestSummaryData>>
    // mock text service region end

    // formula sheet service region start
    @GET("v8/formulas/home")
    fun getFormulaHome(): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetSubjects>>>

    @GET("v1/formulas/subject/{subjectId}/chapters")
    fun getTopics(@Path(value = "subjectId") subjectId: String): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetSuperChapter>>>

    @GET("v1/formulas/chapter/{chapterId}/formulas")
    fun getFormulas(@Path(value = "chapterId") subjectId: String): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetFormulas>>>

    @GET("v1/formulas/subject/{subjectId}/chapters?")
    fun getTopicsSearch(
        @Path(value = "subjectId") subjectId: String,
        @Query(value = "search") searchText: String
    ): Observable<ApiResponse<ArrayList<FormulaSheetSuperChapter>>>

    @GET("v1/formulas/chapter/{chapterId}/formulas?")
    fun getFormulasSearch(
        @Path(value = "chapterId") subjectId: String,
        @Query(value = "search") searchText: String
    ): Observable<ApiResponse<ArrayList<FormulaSheetFormulas>>>

    @GET("v1/formulas/search/{searchText}")
    fun getGlobalSearch(@Path(value = "searchText") searchText: String): Observable<ApiResponse<ArrayList<FormulaSheetGlobalSearch>>>

    @GET("v1/formulas/searchpage/{searchType}/{chapterId}")
    fun getFormulasBySearch(
        @Path(value = "searchType") searchType: String,
        @Path(value = "chapterId") chapterId: String
    ): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetFormulas.FormulasList>>>

    @GET("v8/formulas/cheatsheets")
    fun getCheatSheet(): RetrofitLiveData<ApiResponse<ArrayList<CheatSheetData>>>

    @POST("v8/formulas/cheatsheet/add")
    fun addCheatsheet(@Body params: RequestBody): RetrofitLiveData<ApiResponse<Any>>

    @GET("v8/formulas/cheatsheet/get/{cheatSheetId}")
    fun getFormulasByCheatSheet(@Path(value = "cheatSheetId") cheatSheetId: String): RetrofitLiveData<ApiResponse<ArrayList<FormulaSheetFormulas.FormulasList>>>
    // formula sheet service region end

    // invite service region start
    @POST("/v4/student/add-referred-user")
    fun sendInvitationData(@Body params: RequestBody): Single<ApiResponse<Any?>>

    // invite service region end

    // vip info service region start

    @GET("v1/package/state")
    fun getVipPurchaseInfo(): Single<ApiResponse<VipInfoData>>

    // vip info service region end

    // profile service region start

    @POST("v4/student/update-profile")
    fun updateProfile(@Body params: RequestBody): RetrofitLiveData<ApiResponse<ResponseBody>>

    @POST("v1/personalize/get-active-slots")
    fun getActiveSlots(): Single<ActiveSlotApiModel>

    // profile service region end

    // group chat service region start

    @GET("v2/groupchat/get")
    fun getGroupList(): RetrofitLiveData<ApiResponse<ArrayList<GroupListData>>>

    @GET("v2/groupchat/{id}/messages?")
    fun getLiveChat(
        @Path(value = "id") id: String,
        @Query(value = "cursor") cursor: String
    ): Observable<ApiResponse<GroupChatMessages>>

    @GET("https://api.doubtnut.com/v2/comment/get-list-by-entity/invite/{id}")
    fun getLiveCommemt(@Path(value = "id") id: String): RetrofitLiveData<ApiResponse<ArrayList<Comment>>>

    @Multipart
    @POST("v2/groupchat/addmessage")
    fun addComment(
        @Part("entity_type") entityType: RequestBody?,
        @Part("entity_id") entityId: RequestBody?,
        @Part("message") message: RequestBody?,
        @Part imageFileMultiPartRequestBody: MultipartBody.Part?,
        @Part audioFileMultiPartRequestBody: MultipartBody.Part?
    ): RetrofitLiveData<ApiResponse<Comment>>

    @POST("v2/feed/{entityId}/report")
    fun reportComment(
        @Path("entityId") page: String,
        @Body params: RequestBody
    ): RetrofitLiveData<ApiResponse<Any>>

    // group chat service region end

    // download service region start
    @POST("v3/answers/pdf-download")
    fun pdfDownloads(@Body params: RequestBody): RetrofitLiveData<ApiResponse<DownloadPDFResponse>>

    // download service region end

    // answer service region start

    @POST("/v2/answers/view-onboarding")
    fun updateViewOnBoarding(
        @Body params: RequestBody
    ): Call<ApiResponse<Any>>

    // answer service region end

    // ask service region start
    @POST("v1/search/get-matches")
    fun autoCompleteQuestionText(@Body params: RequestBody): Observable<ApiResponse<AutoCompleteQuestion>>

    @POST("v2/questions/update-match-results")
    fun updateMatches(@Body params: RequestBody): Completable

    // ask service region end

    // question  service region start

    @FormUrlEncoded
    @POST("v2/questions/get-by-tag")
    fun getQuestionByTag(
        @Field("page") page: String,
        @Field("tag_data_obj") body: String
    ): RetrofitLiveData<ApiResponse<ArrayList<QuestionMeta>>>

    // question  service region end

    //region app exit service
    @GET("v1/homepage/get-nudge-pop-up-details")
    suspend fun getAppExitDialogData(
        @Query("show_popup") showPopup: Int,
        @Query("experiment") experiment: Int?,
        @Query("list_count") listCount: Int?
    ): ApiResponse<AppExitDialogData>
    //endregion

    //region user activity (core action) service
    @FormUrlEncoded
    @POST("v1/homepage/store-activity-data")
    suspend fun storeUserActivity(
        @Field("type") type: String,
        @Field("activity_name") activityName: String?,
        @Field("value") resetType: String?
    ): ApiResponse<StoreActivityResponse>
    //endregion

    //region top icon service (used to fetch top icons for various purposes)
    @GET("v1/icons/camera-screen/{icon_count}")
    suspend fun getCameraNavigationTopIcons(
        @Path("icon_count") iconCount: Int
    ): ApiResponse<WidgetEntityModel<TopOptionsWidgetData, *>>

    @GET("v2/camera/get-bottom-icons")
    suspend fun getCameraBottomIcons(
        @Query("is_doubt_feed_available") isDoubtFeedAvailable: Boolean
    ): ApiResponse<List<BottomNavigationItemData>>
    //endregion

    //region qr payment start
    @POST("v4/payment/start")
    suspend fun getQrPaymentInitialInfo(@Body paymentStartBody: PaymentStartBody): ApiResponse<Taxation>

    @FormUrlEncoded
    @POST("v1/payment/qr/status")
    suspend fun getQrPaymentInfo(@Field("order_id") orderId: String): ApiResponse<Taxation>
    //endregion

    //region topic booster game
    @GET("v1/dexter/{question_id}/get_widget")
    suspend fun getTopicBoosterGameBanner(
        @Path("question_id") questionId: String
    ): ApiResponse<TopicBoosterGameBannerData>

    @FormUrlEncoded
    @POST("v1/dexter/questions")
    suspend fun getTopicBoosterGameQuestionsList(
        @Field("test_uuid") testUuid: String,
        @Field("chapter_alias") chapterAlias: String,
        @Field("total_questions_quiz") totalQuestions: Int,
        @Field("expiry") expiry: Int,
    ): ApiResponse<List<TopicBoosterGameQuestion>>

    @POST("v1/dexter/save_response")
    suspend fun saveUserResponse(@Body userResponse: TopicBoosterGameUserResponse): ApiResponse<TopicBoosterGameSaveResponseResult>

    @FormUrlEncoded
    @POST("v1/dexter/result")
    suspend fun submitResult(
        @Field("game_result") gameResult: Int,
        @Field("is_wallet_reward") isWalletReward: Int,
    ): ApiResponse<TopicBoosterGameResult>
    //endregion

    //region top doubt
    @GET("v3/comment/get-list-by-entity/{entity_type}/{entity_id}")
    suspend fun fetchTopDoubtQuestions(
        @Path("entity_type") entityType: String,
        @Path("entity_id") entityId: String,
        @Query("page") page: String,
        @Query("filter") filter: String,
        @Query("batch_id") batchId: String?
    ): ApiResponse<List<TopDoubtQuestion>?>

    @GET("v3/comment/get-list-by-entity/comment/{entity_id}")
    suspend fun fetchTopDoubtAnswerData(
        @Path("entity_id") entityId: String,
        @Query("batch_id") batchId: String?,
        @Query("page") page: String = "1",
        @Query("filter") filter: String = "answers",
        @Query("supported_media_type") supportedMediaTypes: String = listOf(
            "DASH",
            "HLS",
            "RTMP",
            "BLOB",
            "YOUTUBE"
        ).joinToString(",")
    ): ApiResponse<List<WidgetEntityModel<*, *>>>
    //endregion

    //region daily attendance reward
    @POST("v1/rewards/attendance")
    suspend fun markAutoDailyAttendance(): ApiResponse<MarkAttendanceModel>

    @GET("v1/rewards/attendance")
    suspend fun markManualDailyAttendance(): ApiResponse<MarkAttendanceModel>

    @GET("v1/rewards/details")
    suspend fun getRewardDetails(): ApiResponse<RewardDetails>

    @FormUrlEncoded
    @POST("v1/rewards/subscribe")
    suspend fun subscribeRewardNotification(@Field("is_subscribed") isSubscribed: Int): ApiResponse<Unit>

    @FormUrlEncoded
    @POST("v1/rewards/scratch")
    suspend fun markRewardScratched(@Field("level") level: Int): ApiResponse<Unit>
    //endregion

    //region video page similar playlist
    @GET("v1/answers/similar-bottom-sheet/{question_id}")
    suspend fun getVideoPlaylist(@Path("question_id") questionId: String): ApiResponse<ApiVideoPagePlaylist>
    //endregion

    //region doubt feed
    @FormUrlEncoded
    @POST("v1/student/get-doubt-feed-details")
    suspend fun getDoubtFeed(@Field("topic_id") topicId: String?): ApiResponse<DoubtFeed>

    @FormUrlEncoded
    @POST("v1/student/get-doubt-feed-progress")
    suspend fun getDoubtFeedProgress(@Field("topic_id") topicId: String): ApiResponse<DoubtFeedProgress>

    @FormUrlEncoded
    @PUT("v1/student/submit-doubt-completion")
    suspend fun submitDoubtCompletion(@Field("type_id") goalId: Int): ApiResponse<DoubtFeedDailyGoalTaskCompletedPopupData>

    @GET("v1/student/get-doubt-feed-status")
    suspend fun getDoubtFeedStatus(): ApiResponse<DoubtFeedStatus>

    @POST("v1/student/get-doubtfeed-video-banner")
    suspend fun getDoubtFeedVideoBanner(@Body requestBody: RequestBody): ApiResponse<DoubtFeedBanner>
    //endregion

    //region pre purchase calling card
    @POST("v1/course/calling-card-dismiss")
    suspend fun dismissPrePurchaseCallingCard(@Body callingCardDismissRequest: CallingCardDismissRequest): ApiResponse<BaseResponse>

    @POST("v1/course/request-callback")
    suspend fun requestCallback(@Body requestCallbackRequest: RequestCallbackRequest): ApiResponse<BaseResponse>
    //endregion

    //region topic booster game 2 (Khelo Jeeto)
    @GET("v2/khelo-jeeto/home")
    suspend fun getTbgHomeData(): ApiResponse<TbgHomeData>

    @GET("v2/khelo-jeeto/levels")
    suspend fun getLevels(): ApiResponse<LevelData>

    @POST("v2/khelo-jeeto/topics")
    suspend fun getChapters(@Body requestBody: RequestBody): ApiResponse<ChapterSelectionData>

    @POST("v2/khelo-jeeto/questions")
    suspend fun getGameData(@Body requestBody: RequestBody): ApiResponse<TbgGameData>

    @FormUrlEncoded
    @POST("v2/khelo-jeeto/accept-invite")
    suspend fun acceptInvite(
        @Field("game_id") gameId: String,
        @Field("inviter_id") inviterId: String,
        @Field("is_inviter_online") isInviterOnline: Int,
        @Field("chapter_alias") chapterAlias: String,
    ): ApiResponse<TbgGameData>

    @POST("v2/khelo-jeeto/friends-tabs")
    suspend fun getTbgInviteData(@Body requestBody: RequestBody): ApiResponse<TbgInviteData>

    @POST("v2/khelo-jeeto/friends")
    suspend fun getFriendsList(@Body requestBody: RequestBody): ApiResponse<FriendsList>

    @POST("v2/khelo-jeeto/invite")
    suspend fun sendTbgInvitation(@Body requestBody: RequestBody): ApiResponse<Unit>

    @GET("v2/khelo-jeeto/leaderboard-tabs")
    suspend fun getLeaderboard(): ApiResponse<Leaderboard>

    @POST("v2/khelo-jeeto/leaderboard")
    suspend fun getLeaderboardList(@Body requestBody: RequestBody): ApiResponse<Leaderboard>

    @POST("v2/khelo-jeeto/number-invite")
    suspend fun sendNumberInvitation(@Body requestBody: RequestBody): ApiResponse<NumberInvite>

    @POST("v2/khelo-jeeto/result")
    suspend fun submitTbgResult(@Body requestBody: RequestBody): ApiResponse<TbgResult>

    @FormUrlEncoded
    @POST("v2/khelo-jeeto/previous-result")
    suspend fun getTbgPreviousResult(@Field("game_id") gameId: String): ApiResponse<TbgResult>

    @POST("v2/khelo-jeeto/quiz-history")
    suspend fun getQuizHistory(@Body requestBody: RequestBody): ApiResponse<QuizHistoryViewMore>
    //endregion

    //region leaderboard
    @GET("v4/gamification/test-leaderboard")
    suspend fun getTestLeaderboardData(
        @Query("source") source: String,
        @Query("assortment_id") assortmentId: String?,
        @Query("test_id") testId: String?,
        @Query("type") type: String?
    ): ApiResponse<LeaderboardData>

    @GET("v1/paid-user-championship/get-leaderboard")
    suspend fun getPaidUserChampionshipLeaderboard(
        @Query("source") source: String,
        @Query("assortment_id") assortmentId: String?,
        @Query("test_id") testId: String?,
        @Query("type") type: String?
    ): ApiResponse<LeaderboardData>
    //endregion

    //region course recommendation
    @POST("/v1/recommendation/chat")
    suspend fun fetchCourseRecommendation(
        @Body params: RequestBody
    ): ApiResponse<CourseRecommendationResponse>
    //endregion

    //region bottom sheet widget
    @GET("v1/course/bottom-sheet")
    suspend fun getBottomSheetWidgetData(
        @Query("widget_type") widgetType: String,
        @Query("assortment_id") assortmentId: String?,
        @Query("user_category") userCategory: String?,
        @Query("openCount") openCount: String?,
        @Query("questionAskCount") questionAskCount: String?,
    ): ApiResponse<BaseWidgetData>

    @GET("v1/common/paginated-bottom-sheet-widget")
    suspend fun getPaginatedBottomSheetWidgetData(
        @Query("id") id: String?,
        @Query("type") type: String,
        @Query("tab_id") tabId: String?,
        @Query("page") page: Int,
    ): ApiResponse<BaseWidgetData>

    @GET("v1/common/bottom-sheet")
    suspend fun getBottomSheetData(
        @Query("source") source: String?,
    ): ApiResponse<BaseResponse>

    @GET("v1/common/get-inapp-popup")
    fun getInAppPopup(
        @Query("page") page: String?,
        @Query("session_id") sessionId: Int?,
    ): Single<ApiResponse<InAppPopupResponse>>
    //endregion

    @GET("v1/common/dialog")
    suspend fun getDialogData(
        @Query("widget_type") widgetType: String,
        @Query("student_id") studentId: String?,
        @Query("assortment_id") assortmentId: String?,
        @Query("test_id") testId: String?,
        @Query("tab_number") tabNumber: String?
    ): ApiResponse<BaseWidgetData>

    //region Doubt Feed v2
    @FormUrlEncoded
    @POST("v2/daily-goal/get-doubt-feed-details")
    suspend fun getDoubtFeed2(@Field("topic_id") topicId: String?): ApiResponse<com.doubtnutapp.data.remote.models.doubtfeed2.DoubtFeed>

    @FormUrlEncoded
    @POST("v2/daily-goal/get-previous-doubts")
    suspend fun getPreviousDoubtFeed(@Field("topic_id") topicId: String?): ApiResponse<DoubtFeedWidget.Data>

    @FormUrlEncoded
    @PUT("v2/daily-goal/submit-completion")
    suspend fun submitDoubtCompletion2(@Field("type_id") goalId: Int): ApiResponse<DailyGoalSubmitResponse>

    @FormUrlEncoded
    @PUT("v2/daily-goal/submit-completion-for-previous")
    suspend fun submitDoubtCompletionForPrevious(@Field("type_id") goalId: Int)

    @GET("v2/daily-goal/leaderboard-tabs")
    suspend fun getDfLeaderboard(): ApiResponse<com.doubtnutapp.doubtfeed2.leaderboard.data.model.Leaderboard>

    @POST("v2/daily-goal/leaderboard")
    suspend fun getDfLeaderboardList(@Body requestBody: RequestBody): ApiResponse<com.doubtnutapp.doubtfeed2.leaderboard.data.model.Leaderboard>

    @POST("v2/daily-goal/streak")
    suspend fun markDfStreak(): ApiResponse<Unit>

    @GET("v2/daily-goal/reward-details")
    suspend fun getDfRewardDetails(): ApiResponse<com.doubtnutapp.doubtfeed2.reward.data.model.RewardDetails>

    @FormUrlEncoded
    @POST("v2/daily-goal/scratch")
    suspend fun markDfRewardScratched(@Field("level") level: Int): ApiResponse<Unit>

    //endregion

    //region study group
    @GET("/v1/study-group/signed-upload-url")
    fun getSignedUrl(
        @Query("content_type") contentType: String,
        @Query("file_name") fileName: String,
        @Query("file_ext") fileExt: String,
        @Query("mime_type") mimeType: String,
    ): Single<ApiResponse<SignedUrl>>

    //endregion

    //region Common Repository
    @GET("v1/book_call_data")
    suspend fun getBookCallData(): ApiResponse<BookCallData>

    @GET("v1/book_call")
    suspend fun bookCall(
        @Query("date_id") dateId: String,
        @Query("time_id") timeId: String
    ): ApiResponse<BaseResponse>

    //endregion

    //region exam corner widget
    @GET("v1/exam-corner/get-bookmarks")
    suspend fun getExamCornerBookmarkData(@Query("page") page: Int): ApiResponse<ApiExamCornerBookmarkData>

    @GET("v1/exam-corner")
    suspend fun getExamCornerData(
        @Query("filter_type") filterType: String,
        @Query("page") page: Int
    ): ApiResponse<ApiExamCornerData>

    @POST("/v1/exam-corner/set-bookmark")
    suspend fun setExamCornerBookmark(
        @Body params: RequestBody
    ): ApiResponse<Unit>
    //endregion

    // Test Analysis Data
    @GET("v9/testseries/suggestions")
    suspend fun fetchTestAnalysisData(
        @Query("testId") testId: String,
        @Query("subject") subject: String?
    ): ApiResponse<MockTestAnalysisData>
    // end

    // Test Test List Data
    @GET("v9/testseries/active_mock_test/{course}")
    suspend fun fetchTestList(@Path("course") courseName: String): ApiResponse<MockTestListData>
    // end

    // Mock Test Course Data
    @GET("v9/testseries/active_mock_test/")
    suspend fun getMockTestCourseData(): ApiResponse<MockTestCourseData>
    // end

    //region Revision Corner
    @GET("v1/practice-corner/home")
    suspend fun getRevisionCornerHomeData(): ApiResponse<RevisionCornerHomeData>

    @POST("v1/practice-corner/topics")
    suspend fun getRevisionCornerChapters(@Body requestBody: RequestBody): ApiResponse<com.doubtnutapp.data.remote.models.revisioncorner.ChapterSelectionData>

    @GET("v1/practice-corner/stats")
    suspend fun getPerformanceReport(): ApiResponse<PerformanceReport>

    @POST("/v1/practice-corner/rules")
    suspend fun getRevisionCornerRuleData(@Body requestBody: RequestBody): ApiResponse<RulesInfo>

    @POST("v1/practice-corner/subject-tabs")
    suspend fun getRevisionCornerResultData(@Body requestBody: RequestBody): ApiResponse<ResultInfo>

    @POST("v1/practice-corner/history")
    suspend fun getRevisionCornerResultListData(@Body requestBody: RequestBody): ApiResponse<ResultInfo>

    @POST("v1/practice-corner/questions")
    suspend fun getShortTestQuestions(@Body requestBody: RequestBody): ApiResponse<HomeWorkQuestionData>

    @POST("v1/practice-corner/submit")
    suspend fun submitShortTestResult(@Body requestBody: RequestBody): ApiResponse<HomeWorkSolutionData>

    @POST("v1/practice-corner/previous-result")
    suspend fun getPreviousShortTestResult(@Body requestBody: RequestBody): ApiResponse<HomeWorkSolutionData>

    @POST("v1/practice-corner/submit-stats")
    suspend fun submitRevisionCornerStats(@Body requestBody: RequestBody): ApiResponse<Unit>

    @POST("v2/practice-corner/get-full-test-history")
    suspend fun getRevisionCornerTestListData(@Body requestBody: RequestBody): ApiResponse<TestList>

    //endregion

    //region quiz tfs
    @GET("v1/quiztfs/get-question")
    suspend fun getQuizQuestion(
        @Query("isFirst") isFirst: Boolean,
        @Query("studentClass") studentClass: String,
        @Query("language") language: String,
        @Query("subject") subject: String
    ): ApiResponse<QuizQnaInfoApi>

    @GET("v1/quiztfs/question")
    suspend fun getQuizSolution(
        @Query("questionID") id: String,
        @Query("date") date: String
    ): ApiResponse<QuizTfsData>

    @POST("v1/quiztfs/submit-answer")
    suspend fun submitQuiz(@Body params: RequestBody): ApiResponse<QuizTfsSubmitResponse>

    @GET("/v1/quiztfs/quiztfs-start/")
    suspend fun getLiveQuestionsPracticeData(
        @Query("class") classCode: String,
        @Query("language") medium: String,
        @Query("subject") subject: String
    ): ApiResponse<LiveQuestionsData>

    @GET("/v1/quiztfs/quiztfs-start")
    suspend fun getLiveQuestionsPracticeInitialData():
            ApiResponse<LiveQuestionsData>

    @GET("/v1/quiztfs")
    suspend fun getDailyPracticeData(@Query("type") type: String):
            ApiResponse<DailyPracticeData>

    @GET("/v1/quiztfs/rewards")
    suspend fun getRewardsData(
        @Query("page") page: Int,
        @Query("type") type: String
    ): ApiResponse<MyRewardsData>

    @GET("v1/quiztfs/scratchcard")
    suspend fun submitScratchStatus(
        @Query("redeem_value") redeemValue: Int?
    ): ApiResponse<DialogData>

    @GET("/v1/quiztfs/past-sessions")
    suspend fun getHistoryData(
        @Query("page") page: Int
    ): ApiResponse<HistoryData>

    @GET("/v1/quiztfs/past-sessions-date")
    suspend fun getAnalysisData(
        @Query("page") page: Int,
        @Query("date") date: String?,
        @Query("filter") filter: String?
    ): ApiResponse<AnalysisData>

    @GET("v1/quiztfs/question-leader")
    suspend fun getQuizStatus(
        @Query("class") classCode: String,
        @Query("language") medium: String,
        @Query("subject") subject: String
    ): ApiResponse<QuizStatusData>
    //endregion

    // vpa service start
    @GET("/v1/wallet/show-vpa")
    suspend fun fetchVpaInfo(): ApiResponse<VpaWidget.Account>
    //endregion

    // test start
    @GET("/v9/testseries/{testSubscriptionId}/start")
    suspend fun startTest(@Path("testSubscriptionId") testSubscriptionId: String): ApiResponse<StartTestData>
    //endregion

    //region google auth
    @POST("v2/student/update-social-auth-verified-users")
    @FormUrlEncoded
    suspend fun verifyGoogleAuth(
        @Field("social_auth_identifier") socialAuthIdentifier: String = "google",
        @Field("firebase_token") token: String
    ): ApiResponse<Any>
    //endregion

    //region checkout_v2
    @POST("/v3/payment/checkout")
    suspend fun getCheckoutData(@Body params: RequestBody): ApiResponse<PaymentData>
    //endregion

    //region icons
    @POST("v1/icons/increase-icons-count")
    @FormUrlEncoded
    suspend fun increaseIconsCount(
        @Field("icon_id") id: String
    ): ApiResponse<BaseResponse>

    @GET("v1/icons/categories")
    suspend fun getCategories(
        @Query("type") type: String,
        @Query("id") id: String,
        @Query("page") page: Int,
        @Query("user_assortment") assortmentId: String = defaultPrefs().getString(
            Constants.SELECTED_ASSORTMENT_ID,
            ""
        ).orEmpty()
    ): ApiResponse<IconsDetailResponse>
    //endregion

    @POST("v1/student/auto-play-value")
    fun trackAutoPlayData(@Body params: RequestBody): Completable

    @GET("v1/course/purchase-popups")
    suspend fun getSnackBar(
        @Query("source") source: String?,
        @Query("page") page: Int,
        @Query("assortment_id") assortmentId: String?,
        @Query("qid") qid: String?
    ): ApiResponse<SnackBarData?>

    //region teachers list

    @GET("v1/teacher/get-teachers-tab")
    suspend fun getTeacherList(
        @Query("page") page: Int
    ): ApiResponse<TeacherListData>

    @GET("v1/clp/get-filter-data")
    suspend fun getFilterBottomSheetData(
        @Query("type") type: String,
        @Query("assortment_id") assortmentId: String,
        @QueryMap filtersMap: Map<String, String>
    ): ApiResponse<FilterListData>

    //endregion

    //region Address
    @GET("v1/common/address-form-data")
    suspend fun addressFormData(
        @Query("type") type: String,
        @Query("id") id: String?,
    ): ApiResponse<AddressFormData>

    @POST("v1/common/submit-address")
    @FormUrlEncoded
    suspend fun submitAddress(
        @Field("type") type: String,
        @Field("id") id: String?,
        @Field("link") link: String?,
        @Field("full_name") fullName: String?,
        @Field("country_code") countryCode: String?,
        @Field("mobile_number") mobileNumber: String?,
        @Field("pin_code") pinCode: String?,
        @Field("address_one") addressOne: String?,
        @Field("address_two") addressTwo: String?,
        @Field("landmark") landmark: String?,
        @Field("full_address") fullAddress: String?,
        @Field("city") city: String?,
        @Field("state_id") stateId: String?,
        @Field("size_id") sizeId: String?,
    ): ApiResponse<BaseResponse>
    //region Address

    //region Address
    @GET("v1/icons/get-all-icons/{screen}")
    suspend fun getAllHomeTopIcons(
        @Path(value = "screen") screen: String,
        @Query(value = "user_assortment") userAssortment: String,
        @Query(value = "screen_width") screenWidth: Int?,
    ): ApiResponse<TopIconsData>

    @GET("v1/icons/get-app-nav-icons")
    suspend fun getAppWideBottomNavIcons(): ApiResponse<BottomNavigationTabsData>

    //region shorts list
    @GET("v1/dn-shorts/get-shorts-videos")
    suspend fun fetchShortsList(
        @Query("last_id") lastId: String?,
        @Query("question_id") qid: String?,
        @Query("type") type: String?
    ): ApiResponse<ShortsListData>

    @POST("/v1/dn-shorts/bookmark-shorts")
    @FormUrlEncoded
    suspend fun bookmarkShortsVideo(
        @Field("question_id") questionId: String,
        @Field("is_bookmarked") isBookmarked: Boolean,
    ): ApiResponse<Any>

    @POST("/v1/dn-shorts/update-watch-footprint")
    @FormUrlEncoded
    suspend fun updateShortsWatchFootprint(
        @Field("question_id") questionId: String,
        @Field("engage_time") engageTime: Long
    ): ApiResponse<Any>

    //endregion

    @GET("v1/dn-shorts/get-dnshort-categories")
    suspend fun getCategoryBottomSheet(): ApiResponse<ShortsCategoryData>

    @POST("v1/dn-shorts/set-dnshort-categories")
    suspend fun sendCategoriesData(
        @Body
        requestBody: RequestBody
    ): ApiResponse<Any>
}
