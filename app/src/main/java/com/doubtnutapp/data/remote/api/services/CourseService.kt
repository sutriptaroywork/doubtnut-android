package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.EMIDialogData
import com.doubtnutapp.SuggestionData
import com.doubtnutapp.WidgetsResponseData
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.domain.payment.entities.PurchasedCourseDetail
import com.doubtnutapp.liveclass.ui.dialog.CouponDialogData
import com.doubtnutapp.liveclass.ui.dialog.CourseChangeData
import com.doubtnutapp.model.FilterData
import com.doubtnutapp.model.NextVideoDialogData
import com.doubtnutapp.wallet.BestSellerData
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface CourseService {

    @GET("v2/course/{class}/get-list")
    fun getClasses(@Path(value = "class") clazz: String): RetrofitLiveData<ApiResponse<ArrayList<Course>>>

    @GET("v2/chapter/{class}/{course}/get-list-with-class")
    fun getChapters(
        @Path(value = "class") clazz: String,
        @Path(value = "course") course: String
    ): RetrofitLiveData<ApiResponse<ChapterResponse>>

    @GET("v2/chapter/{class}/{course}/{chapter}/get-details")
    fun getChapterDetails(
        @Path(value = "class") clazz: String,
        @Path(value = "course") course: String,
        @Path(value = "chapter") chapter: String
    ): RetrofitLiveData<ApiResponse<ChapterDetail>>

    @GET("v6/course/{endPoint}")
    fun getLibraryCourse(
        @Path(value = "endPoint") endPoint: String = "home",
        @Query(value = "page") page: Int,
        @Query(value = "category") categoryId: String?,
        @Query(value = "filters_list") filtersList: List<String>? = null
    ): Single<ApiResponse<ApiCourseData>>

    @GET("v4/course/get-detail")
    fun getVmcDetail(): Single<ApiResponse<ApiVmcDetailData>>

    @GET("v2/course/list")
    fun getLibraryCoursesList(
        @Query(value = "page") page: Int,
        @Query(value = "ecm_id") ecmId: Int? = null,
        @Query(value = "subject") subject: String? = null
    ): Single<ApiResponse<ApiCourseData>>

    @POST("/v2/liveclass/quiz-submit")
    fun submitLiveClassQuiz(
        @Body params: RequestBody
    ): Single<ApiResponse<LiveClassQuizSubmitResponse>>

    @GET("v2/liveclass/post-quiz-details")
    fun getLiveClassPopUpDetail(@Query(value = "resource_id") resourceId: String): Single<ApiResponse<ApiPopUpDetail>>

    @GET("v3/course/get-livesection")
    fun getAllLiveSectionList(
        @Query(value = "ecm_id") ecmId: Int?,
        @Query(value = "subject") subject: String?,
        @Query(value = "promo") promo: String?,
        @Query(value = "course_id") courseId: String?
    ): Single<ApiResponse<ApiLiveSectionData>>

    @GET("v5/course/get-resource")
    fun getResourceData(
        @Query("detail_id") courseDetailId: String,
        @Query("recorded") recorded: Int
    ): Single<ApiResponse<ApiResourceData>>

    @GET("v7/course/get-detail")
    fun getAllCoursesData(
        @Query("assortment_id") assortmentId: String,
        @Query("subject") subject: String,
        @Query("student_class") studentClass: String?,
        @Query("source") source: String?,
        @Query("page") page: Int,
        @Query("bottom_sheet") bottomSheet: Boolean = false,
        @Query("supported_media_type") supportedMediaTypes: String = listOf(
            "DASH",
            "HLS",
            "RTMP",
            "BLOB"
        ).joinToString(","),
        @QueryMap filtersMap: Map<String, String>?,
        @Query("tab_id") tabId: String?
    ): Single<ApiResponse<ApiCourseDataV3>>

    @GET("v1/stories")
    fun getStories(): Single<ApiResponse<Widgets>>

    @GET("v7/course/get-subject-detail")
    fun getSubjectDetailData(
        @Query("subject") subject: String,
        @Query("assortment_id") assortmentId: String,
        @Query("page") page: Int,
    ): Single<ApiResponse<ApiSubjectDetailData>>

    @GET("v7/course/get-pre-purchase-tab-detail")
    fun getCourseTabsData(
        @Query("page") page: Int,
        @Query("assortment_id") assortmentId: String,
        @Query("tab") tabId: String,
        @Query("filter_value") contentType: String?
    ): Single<ApiResponse<Widgets>>

    @GET("/v4/course/get-recorded")
    fun getRecordedCourseData(
        @Query(value = "subject") subject: String? = null
    ): Single<ApiResponse<ApiRecordedCourseData>>

    @GET("v2/course/widgets")
    fun getHomeworkData(
        @Query(value = "page") page: String? = "liveclass_video",
        @Query(value = "question_id") questionId: String?,
        @Query(value = "tab_id") tabId: String?,
        @Query(value = "source") source: String?
    ): Single<ApiResponse<HomeWorkData>>

    @GET("v1/course/widgets")
    fun getPopularCourses(
        @Query(value = "page") page: String?,
        @Query(value = "question_id") questionId: String? = "empty"
    ): Single<ApiResponse<Widgets>>

    @GET("v1/course/homework/get")
    fun getHomeWorkQuestions(
        @Query(value = "question_id") questionId: String?
    ): Single<ApiResponse<HomeWorkQuestionData>>

    @GET("v1/course/homework/list")
    fun getHomeWorkList(@Query(value = "page") page: Int?): Single<ApiResponse<HomeWorkListResponse>>

    @POST("v1/course/homework/submit")
    fun submitHomeWork(@Body data: HomeWorkPostData): Single<ApiResponse<HomeWorkResponseData>>

    @GET("v1/course/homework/review")
    fun getHomeWorkSolutions(
        @Query(value = "question_id") questionId: String?,
        @Query(value = "is_video_page") isVideoPage: Boolean?
    ): Single<ApiResponse<HomeWorkSolutionData>>

    @GET("v1/liveclass/mark-student-interested")
    fun markInterested(
        @Query(value = "resource_id") resourceId: String,
        @Query(value = "is_reminder") isReminder: Boolean,
        @Query(value = "assortment_id") assortmentId: String,
        @Query(value = "live_at") liveAt: String?,
        @Query(value = "reminder_set") reminderSet: Int?
    ): Completable

    @GET("v3/package/trial")
    fun activateTrial(
        @Query(value = "assortment_id") assortmentId: String
    ): Single<ApiResponse<ActivateTrialData>>

    @GET("v1/course/pdf-download")
    fun markNotesRead(
        @Query(value = "resource_id") resourceId: String,
        @Query(value = "download") download: Int
    ): Completable

    @GET("v1/nudge/close-banner")
    fun markNudgeClosed(
        @Query(value = "id") widgetId: String?,
        @Query(value = "type") nudgeType: String?
    ): Completable

    @GET("v2/liveclass/status")
    fun getVideoStatus(@Query(value = "id") id: String): Single<ApiResponse<VideoStatus>>

    @GET("v5/course/get-timetable")
    fun getTimetable(
        @Query(value = "course_id") courseId: String,
        @Query(value = "studentClass") studentClass: String
    ):
            Single<ApiResponse<ApiTimetableData>>

    @GET("v1/course/timetable")
    fun getSchedule(@QueryMap options: Map<String, String>): Single<ApiResponse<ApiScheduleData>>

    @GET("v6/course/get-list")
    fun getResourceList(
        @Query(value = "page") page: Int,
        @Query(value = "id") id: String,
        @Query(value = "subject") subject: String
    ): Single<ApiResponse<ResourceListData>>

    @GET("v1/nudge/get")
    fun getNudgesData(@Query("id") id: Int): Single<ApiResponse<Widgets>>

    @GET("v6/course/emi-reminder")
    fun getEmiReminderData(@Query("assortment_id") id: Int): Single<ApiResponse<EMIDialogData>>

    @POST("v6/course/liveclass-search")
    suspend fun getCategorySearchData(@Body requestBody: RequestBody): ApiResponse<WidgetsResponseData>

    @POST("v6/course/auto-suggest")
    suspend fun getSuggestionData(@Body requestBody: RequestBody): ApiResponse<SuggestionData>

    @GET("v1/course/best-seller")
    suspend fun getBestSellerData(): ApiResponse<BestSellerData>

    @GET("v1/vod/liveclass/meta")
    fun getFeedbackData(
        @Query(value = "detail_id") detailId: String?,
    ): Single<ApiResponse<TagsData>>

    @GET("v7/course/get-tab-detail")
    suspend fun getCourseDetail(
        @QueryMap queryParams: Map<String, String>
    ): ApiResponse<ApiCourseDetailData>

    @GET("v7/course/get-tab-detail-new")
    suspend fun getCourseDetailNew(
        @QueryMap queryParams: Map<String, String>
    ): ApiResponse<ApiCourseDetailData>

    @GET("v2/course/timetable")
    suspend fun getCourseScheduleDetail(
        @Query(value = "page") page: Int,
        @Query(value = "assortment_id") assortmentId: String,
        @Query(value = "month") month: String?,
        @Query(value = "prev_month") prevMonth: String?,
    ): ApiResponse<ApiCourseDetailData>

    @POST("/v3/search/insert-premium-content-block-view-log")
    fun postPaidContentData(@Body requestBody: RequestBody): Single<ApiResponse<ActivateTrialData>>

    @GET("/v1/wallet/recommended-resources")
    suspend fun getRecommendedCourses(): ApiResponse<RecommendedCourses>

    @GET("/v1/course/purchased-list")
    fun getPurchasedCourses(@Query(value = "page") page: String?): Single<ApiResponse<PurchasedCourseDetail>>

    @GET("v1/nudge/popup-banner")
    fun getNudgeDetails(
        @Query(value = "id") id: String?,
        @Query(value = "page") page: String?,
        @Query(value = "type") type: String?
    ): Single<ApiResponse<NudgeData>>

    @GET("v1/course/help-popup")
    fun getCourseChangeData(
        @Query(value = "type") popupType: String?,
        @Query(value = "selected_assortment") selectedAssortment: String?,
        @Query(value = "assortment_id") assortmentId: String
    ): Single<ApiResponse<CourseChangeData>>

    @GET("v1/course/switch-filter-details")
    fun getCourseSelectionData(): Single<ApiResponse<CourseSelectionData>>

    @GET("v1/course/switch-list")
    fun getCourseListData(
        @Query(value = "filter_class") selectedClass: String?,
        @Query(value = "filter_exam") selectedExam: String?,
        @Query(value = "filter_year") selectedYear: String?,
        @Query(value = "filter_medium") selectedMedium: String?,
        @Query(value = "assortment_id") assortmentId: String?
    ): Single<ApiResponse<CourseListData>>

    @GET("v1/course/callback-data")
    fun requestCallback(
        @Query(value = "assortment_id") assortmentId: String?,
        @Query(value = "selected_assortment") selectedAssortment: String?,
        @Query(value = "subscription_id") subscriptionId: String?
    ): Single<ApiResponse<CallbackData>>

    @POST("v1/coupon/applicable-coupon-codes")
    fun getCouponData(@Body params: RequestBody): Single<com.doubtnutapp.data.common.model.ApiResponse<CouponDialogData>>

    @GET("v2/course/bottom-sheet")
    suspend fun getNextVideos(
        @Query(value = "question_id") questionId: String?,
        @Query(value = "type") type: String?
    ): ApiResponse<NextVideoDialogData>

    @GET("v1/course/bookmark-resources")
    suspend fun bookmark(
        @Query(value = "resource_id") id: String?,
        @Query(value = "assortment_id") assortmentId: String?,
        @Query(value = "type") type: String?,
    ): ApiResponse<BookmarkData>

    @GET("v2/course/video-page-tabs")
    suspend fun getVideoTabsData(
        @Query(value = "tab_id") id: String?,
        @Query(value = "question_id") qid: String?,
    ): ApiResponse<Widgets>

    @GET("v1/clp/get-filter-data")
    suspend fun getFilters(
        @Query("source") source: String,
        @Query("assortment_id") assortmentId: String,
        @QueryMap filtersMap: Map<String, String>
    ): ApiResponse<FilterData>

}
