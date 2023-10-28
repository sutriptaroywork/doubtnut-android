package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.models.mocktest.MockTestAnalysisData
import com.doubtnutapp.data.remote.models.mocktest.MockTestCourseData
import com.doubtnutapp.data.remote.models.mocktest.MockTestListData
import com.doubtnutapp.data.toRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.RequestBody
import javax.inject.Inject

class MockTestRepository @Inject constructor(private val networkService: NetworkService) {

    fun getMockTestData(): RetrofitLiveData<ApiResponse<ArrayList<MockTestData>>> =
        networkService.getMockTestData()

    fun getMockTestSubscribe(testId: Int): RetrofitLiveData<ApiResponse<TestSubscribe>> =
        networkService.getMockTestSubscribe(testId)

    fun getMockTestRules(rulesId: Int): RetrofitLiveData<ApiResponse<TestRules>> =
        networkService.getMockTestRules(rulesId)

    fun getMockTestQuestions(testId: Int): RetrofitLiveData<ApiResponse<MockTestQuestionData>> =
        networkService.getMockTestQuestions(testId)

    fun getMockTestResponse(params: RequestBody): RetrofitLiveData<ApiResponse<TestResponse>> =
        networkService.getMockTestResponse(params)

    fun getMockTestSubmit(testSubscriptionId: Int): RetrofitLiveData<ApiResponse<TestSubmit>> =
        networkService.getMockTestSubmit(testSubscriptionId)

    fun getMockTestResult(testSubscriptionId: Int): RetrofitLiveData<ApiResponse<MockTestResult>> =
        networkService.getMockTestResult(testSubscriptionId)

    fun getMockTestLeaderboard(testId: Int): RetrofitLiveData<ApiResponse<ArrayList<TestLeaderboard>>> =
        networkService.getMockTestLeaderboard(testId)

    fun getSummary(testSubscriptionId: Int): RetrofitLiveData<ApiResponse<MockTestSummaryData>> =
        networkService.getSummary(testSubscriptionId)

    fun getAnalysisData(testId: String, subject: String?): Flow<ApiResponse<MockTestAnalysisData>> =
        flow { emit(networkService.fetchTestAnalysisData(testId, subject)) }

    fun getMockTestCourseData(): Flow<ApiResponse<MockTestCourseData>> =
        flow { emit(networkService.getMockTestCourseData()) }

    fun getTestList(courseName: String): Flow<ApiResponse<MockTestListData>> =
        flow { emit(networkService.fetchTestList(courseName)) }

    fun submitRevisionCornerStats(
        testId: Int,
        totalScore: Int,
        totalMarks: Int,
        examType: String
    ): Flow<ApiResponse<Unit>> =
        flow {
            val requestBody = mapOf(
                "test_id" to testId,
                "total_score" to totalScore,
                "total_marks" to totalMarks,
                "exam_type" to examType,
            ).toRequestBody()
            emit(networkService.submitRevisionCornerStats(requestBody))
        }

    fun startTest(testId: String): Flow<ApiResponse<StartTestData>> =
        flow { emit(networkService.startTest(testId)) }
}
