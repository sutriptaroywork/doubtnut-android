package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.EMIDialogData
import com.doubtnutapp.SuggestionData
import com.doubtnutapp.WidgetsResponseData
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.CourseService
import com.doubtnutapp.data.remote.api.services.MicroService
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.payment.entities.PurchasedCourseDetail
import com.doubtnutapp.liveclass.ui.dialog.CouponDialogData
import com.doubtnutapp.liveclass.ui.dialog.CourseChangeData
import com.doubtnutapp.model.FilterData
import com.doubtnutapp.model.NextVideoDialogData
import com.doubtnutapp.toHashMapOfStringVString
import com.doubtnutapp.toRequestBody
import com.doubtnutapp.wallet.BestSellerData
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody

class CourseRepository(val courseService: CourseService, val microService: MicroService) {
    fun getPopoularCourse(page: String): Single<ApiResponse<Widgets>> =
        courseService.getPopularCourses(page)

    fun getCourse(clazz: String): RetrofitLiveData<ApiResponse<ArrayList<Course>>> =
        courseService.getClasses(clazz)

    fun getChapters(
        clazz: String,
        course: String
    ): RetrofitLiveData<ApiResponse<ChapterResponse>> = courseService.getChapters(clazz, course)

    fun getChapterDetails(
        clazz: String,
        course: String,
        chapter: String
    ): RetrofitLiveData<ApiResponse<ChapterDetail>> =
        courseService.getChapterDetails(clazz, course, chapter)

    fun getLibraryCourse(
        page: Int,
        categoryId: String?,
        filtersList: List<String>? = null
    ): Single<ApiResponse<ApiCourseData>> =
        courseService.getLibraryCourse(
            endPoint = if (categoryId == "free_classes") "get-free-live-class-data" else "home",
            page = page,
            categoryId = categoryId,
            filtersList = filtersList
        )

    fun getVmcDetail(): Single<ApiResponse<ApiVmcDetailData>> = courseService.getVmcDetail()

    fun getLibraryCoursesList(
        page: Int,
        ecmId: Int?,
        subject: String?
    ): Single<ApiResponse<ApiCourseData>> =
        courseService.getLibraryCoursesList(page, ecmId, subject)

    fun submitLiveClassQuiz(params: HashMap<String, String>): Single<ApiResponse<LiveClassQuizSubmitResponse>> =
        courseService.submitLiveClassQuiz(params.toRequestBody())

    fun submitLiveClassPoll(params: HashMap<String, String>): Single<ApiResponse<LiveClassPollSubmitResponse>> =
        microService.submitLiveClassPoll(params.toRequestBody())

    fun submitLiveClassFeedback(params: HashMap<String, Any>): Single<ApiResponse<LiveClassFeedbackResponse>> =
        microService.submitLiveClassFeedback(params.toRequestBody())

    fun viewedFeedbackDialog(params: HashMap<String, Any>): Single<ApiResponse<LiveClassFeedbackResponse>> =
        microService.viewedLiveClassFeedback(params.toRequestBody())

    fun getLiveClassPoll(
        publishId: String,
        pollId: String
    ): Single<ApiResponse<List<LiveClassPollOptionsData>>> =
        microService.getPollResult(publishId, pollId)

    fun getLiveClassPopUpDetail(resourceId: String): Single<ApiResponse<ApiPopUpDetail>> =
        courseService.getLiveClassPopUpDetail(resourceId)

    fun getFeedbackData(detailId: String?): Single<ApiResponse<TagsData>> =
        courseService.getFeedbackData(detailId)

    fun getHomeworkData(
        questionId: String,
        tabId: String,
        page: String
    ): Single<ApiResponse<HomeWorkData>> =
        courseService.getHomeworkData(questionId = questionId, tabId = tabId, source = page)

    fun getHomeWorkQuestions(questionId: String): Single<ApiResponse<HomeWorkQuestionData>> =
        courseService.getHomeWorkQuestions(questionId = questionId)

    fun getHomeWorkList(page: Int): Single<ApiResponse<HomeWorkListResponse>> =
        courseService.getHomeWorkList(page)

    fun submitHomeWork(data: HomeWorkPostData): Single<ApiResponse<HomeWorkResponseData>> =
        courseService.submitHomeWork(data)

    fun getHomeWorkSolutions(
        questionId: String,
        isVideoPage: Boolean
    ): Single<ApiResponse<HomeWorkSolutionData>> =
        courseService.getHomeWorkSolutions(questionId = questionId, isVideoPage = isVideoPage)

    fun isFeedbackRequired(detailId: String): Single<ApiResponse<FeedbackStatusResponse>> =
        microService.isFeedbackRequired(detailId)

    fun getAllLiveSectionList(
        ecmId: Int?,
        subject: String?,
        promo: String?,
        courseId: String?
    ): Single<ApiResponse<ApiLiveSectionData>> =
        courseService.getAllLiveSectionList(ecmId, subject, promo, courseId)

    fun getResourceData(
        courseDetailId: String,
        recorded: Int
    ): Single<ApiResponse<ApiResourceData>> =
        courseService.getResourceData(courseDetailId, recorded)

    fun getAllCoursesData(
        assortmentId: String,
        subject: String,
        studentClass: String?,
        source: String?,
        page: Int,
        bottomSheet: Boolean = false,
        filtersMap: Map<String, String>?,
        tabId: String?
    ): Single<ApiResponse<ApiCourseDataV3>> =
        courseService.getAllCoursesData(
            assortmentId = assortmentId,
            subject = subject,
            studentClass = studentClass,
            source = source,
            page = page,
            bottomSheet = bottomSheet,
            filtersMap = filtersMap,
            tabId = tabId
        )

    fun getStories(): Single<ApiResponse<Widgets>> = courseService.getStories()

    fun getPurchasedCourses(page: String? = null): Single<ApiResponse<PurchasedCourseDetail>> =
        courseService.getPurchasedCourses(page)

    fun getSubjectDetailData(
        subject: String,
        assortmentId: String,
        page: Int
    ): Single<ApiResponse<ApiSubjectDetailData>> =
        courseService.getSubjectDetailData(subject, assortmentId, page)

    fun getCourseTabsData(
        pageNumber: Int,
        assortmentId: String,
        tabId: String,
        contentType: String?
    ): Single<ApiResponse<Widgets>> =
        courseService.getCourseTabsData(pageNumber, assortmentId, tabId, contentType)

    fun getRecordedCourseData(subject: String?): Single<ApiResponse<ApiRecordedCourseData>> =
        courseService.getRecordedCourseData(subject)

    fun markInterested(
        resourceId: String,
        isReminder: Boolean,
        assortmentId: String,
        liveAt: String?,
        reminderSet: Int?
    ): Completable =
        courseService.markInterested(
            resourceId,
            isReminder, assortmentId, liveAt, reminderSet ?: 0
        )

    fun activateTrial(assortmentId: String): Single<ApiResponse<ActivateTrialData>> =
        courseService.activateTrial(assortmentId)

    fun markNotesRead(resourceId: String, download: Int): Completable =
        courseService.markNotesRead(resourceId, download)

    fun getVideoStatus(id: String): Single<ApiResponse<VideoStatus>> =
        courseService.getVideoStatus(id)

    fun getTimetable(
        courseId: String,
        studentClass: String
    ): Single<ApiResponse<ApiTimetableData>> =
        courseService.getTimetable(courseId, studentClass)

    fun getScheduleData(previous: String?, next: String?): Single<ApiResponse<ApiScheduleData>> {
        val map = hashMapOf<String, String>()
        if (!previous.isNullOrBlank()) {
            map["previous"] = previous
        }
        if (!next.isNullOrBlank()) {
            map["next"] = next
        }
        return courseService.getSchedule(map)
    }

    fun getResourceList(
        page: Int,
        id: String,
        subject: String
    ): Single<ApiResponse<ResourceListData>> =
        courseService.getResourceList(page, id, subject)

    fun getNudgesData(id: Int): Single<ApiResponse<Widgets>> =
        courseService.getNudgesData(id)

    fun getEmiReminderData(assortmentId: Int): Single<ApiResponse<EMIDialogData>> =
        courseService.getEmiReminderData(assortmentId)

    fun getCategorySearchData(requestBody: RequestBody): Flow<ApiResponse<WidgetsResponseData>> =
        flow { emit(courseService.getCategorySearchData(requestBody)) }

    fun getSuggestionData(requestBody: RequestBody): Flow<ApiResponse<SuggestionData>> =
        flow { emit(courseService.getSuggestionData(requestBody)) }

    fun getBestSellerData(): Flow<ApiResponse<BestSellerData>> =
        flow { emit(courseService.getBestSellerData()) }

    fun getCourseDetail(
        isFilterV2Enabled: Boolean,
        queryParams: Map<String, String>
    ) = flow {
        emit(
            if (isFilterV2Enabled) {
                courseService.getCourseDetailNew(queryParams)
            } else {
                courseService.getCourseDetail(queryParams)
            }
        )
    }

    fun markNudgeClosed(widgetId: String?, nudgeType: String?): Completable =
        courseService.markNudgeClosed(widgetId, nudgeType)

    fun getCourseScheduleDetail(
        page: Int,
        assortmentId: String,
        month: String?,
        prevMonth: String?
    ) = flow { emit(courseService.getCourseScheduleDetail(page, assortmentId, month, prevMonth)) }

    fun postPaidContentEvent(paramsMap: java.util.HashMap<String, Any>): Single<ApiResponse<ActivateTrialData>> =
        courseService.postPaidContentData(paramsMap.toRequestBody())

    fun getRecommendedCourses(): Flow<ApiResponse<RecommendedCourses>> =
        flow { emit(courseService.getRecommendedCourses()) }

    fun getNudgeDetails(
        nudgeId: String?,
        page: String?,
        type: String?
    ): Single<ApiResponse<NudgeData>> =
        courseService.getNudgeDetails(nudgeId, page, type)

    fun getCourseChangData(
        popupType: String,
        selectedAssortment: String,
        assortmentId: String
    ): Single<ApiResponse<CourseChangeData>> =
        courseService.getCourseChangeData(popupType, selectedAssortment, assortmentId)

    fun getCourseSelectionData(): Single<ApiResponse<CourseSelectionData>> =
        courseService.getCourseSelectionData()

    fun getCourseListData(
        selectedClass: String,
        selectedExam: String,
        selectedExamYear: String,
        selectedMedium: String,
        assortmentId: String
    ): Single<ApiResponse<CourseListData>> =
        courseService.getCourseListData(
            selectedClass,
            selectedExam,
            selectedExamYear,
            selectedMedium,
            assortmentId
        )

    fun requestCallback(
        assortmentId: String,
        selectedAssortmentId: String,
        subscriptionId: String
    ): Single<ApiResponse<CallbackData>> =
        courseService.requestCallback(assortmentId, selectedAssortmentId, subscriptionId)

    fun getCouponData(hashMap: HashMap<String, Any>): Single<CouponDialogData> {
        return courseService.getCouponData(hashMap.toRequestBody())
            .map { it.data }
    }

    fun getNextVideos(qid: String?, type: String?): Flow<ApiResponse<NextVideoDialogData>> =
        flow { emit(courseService.getNextVideos(qid, type)) }

    fun bookmark(
        id: String?,
        assortmentId: String?,
        type: String? = ""
    ): Flow<ApiResponse<BookmarkData>> =
        flow { emit(courseService.bookmark(id, assortmentId, type)) }

    fun getVideoTabsData(id: String?, qid: String?): Flow<ApiResponse<Widgets>> =
        flow { emit(courseService.getVideoTabsData(id, qid)) }

    fun getFilters(source: String, assortmentId: String, filters: Map<String, List<String>>)
            : Flow<ApiResponse<FilterData>> {
        return flow {
            emit(
                courseService.getFilters(
                    source,
                    assortmentId,
                    filters.toHashMapOfStringVString()
                )
            )
        }
    }
}
