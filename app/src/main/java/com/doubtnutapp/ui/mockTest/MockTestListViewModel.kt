package com.doubtnutapp.ui.mockTest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.models.mocktest.MockTestListData
import com.doubtnutapp.data.remote.repository.MockTestRepository
import com.doubtnutapp.toRequestBody
import com.doubtnutapp.ui.mockTest.event.MockTestEventManager
import com.doubtnutapp.utils.Utils
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class MockTestListViewModel @Inject constructor(private val analyticsPublisher: AnalyticsPublisher,
                                                private val mockTestEventManager: MockTestEventManager,
                                                private val mockTestRepository: MockTestRepository,
                                                compositeDisposable: CompositeDisposable)
    : BaseViewModel(compositeDisposable) {

    private val _testListData
            : MutableLiveData<Outcome<MockTestListData>> = MutableLiveData()

    val testListData: LiveData<Outcome<MockTestListData>>
        get() = _testListData

    val reviewQuestionList = arrayListOf<Int>()

    fun getMockTestDetails(): RetrofitLiveData<ApiResponse<ArrayList<MockTestData>>> {
        return mockTestRepository.getMockTestData()
    }

    fun getTestSubscribe(testId: Int): RetrofitLiveData<ApiResponse<TestSubscribe>> {
        return mockTestRepository.getMockTestSubscribe(testId)
    }

    fun getTestResponse(testId: Int,
                        actionType: String,
                        isReview: Int?,
                        optionCode: String,
                        sectionCode: String,
                        testSubcriptionId: String,
                        questionbankId: String,
                        questionType: String,
                        isEligible: String,
                        timeTake: Int?,
                        subjectCode: String?,
                        chapterCode: String?,
                        subtopicCode: String?,
                        classCode: String?,
                        mcCode: String?,
                        reviewStatus: String? = null
    ): RetrofitLiveData<ApiResponse<TestResponse>> {
        return mockTestRepository.getMockTestResponse(
            Utils.getMockTestResponseBody(
                testId, actionType, isReview, optionCode, sectionCode, testSubcriptionId,
                questionbankId, questionType, isEligible, timeTake, subjectCode,
                chapterCode, subtopicCode, classCode, mcCode, reviewStatus
            ).toRequestBody()
        )
    }

    fun getTestSubmit(testSubscriptionId: Int): RetrofitLiveData<ApiResponse<TestSubmit>> {
        return mockTestRepository.getMockTestSubmit(testSubscriptionId)
    }

    fun getSummary(testSubscriptionId: Int): RetrofitLiveData<ApiResponse<MockTestSummaryData>> {
        return mockTestRepository.getSummary(testSubscriptionId)
    }

    fun submitRevisionCornerStats(
        testId: Int,
        totalScore: Int,
        totalMarks: Int,
        examType: String
    ) {
        viewModelScope.launch {
            mockTestRepository.submitRevisionCornerStats(
                testId = testId,
                totalScore = totalScore,
                totalMarks = totalMarks,
                examType = examType
            ).catch { }.collect()
        }
    }

    fun publishMockTestTopicSelected(subject: String, testName: String) {
        mockTestEventManager.mockTestTopicSelected(subject, testName)
    }

    fun publishEvent(eventStructured: StructuredEvent) {
        analyticsPublisher.publishEvent(eventStructured)
    }

    fun getTestList(courseName: String) {
        startLoading()
        viewModelScope.launch {
            mockTestRepository.getTestList(courseName)
                    .map {
                        it.data
                    }.catch { e ->
                        _testListData.value = Outcome.Failure(e)
                        stopLoading()
                    }.collect {
                        _testListData.value = Outcome.success(it)
                        stopLoading()
                }
        }
    }

    fun startLoading() {
        _testListData.value = Outcome.loading(true)
    }

    fun stopLoading() {
        _testListData.value = Outcome.loading(false)
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}
