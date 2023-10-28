package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.api.services.TestService
import com.doubtnutapp.data.remote.models.*
import io.reactivex.Single
import okhttp3.RequestBody

/**
 * Created by akshaynandwana on
 * 17, December, 2018
 **/
class TestRepository(val testService: TestService) {

    fun getTestDetails(token: String): RetrofitLiveData<ApiResponse<ArrayList<TestDetails>>> =
        testService.getTestDetails()

    fun getTestSubscribe(token: String, testId: Int): Single<ApiResponse<TestSubscribe>> =
        testService.getTestSubscribe(testId)

    fun getTestRules(token: String, rulesId: Int): RetrofitLiveData<ApiResponse<TestRules>> =
        testService.getTestRules(rulesId)

    fun getTestQuestions(
        token: String,
        testId: Int
    ): RetrofitLiveData<ApiResponse<java.util.ArrayList<TestQuestionDataOptions>>> =
        testService.getTestQuestions(testId)

    fun getTestResponse(
        token: String,
        params: RequestBody
    ): RetrofitLiveData<ApiResponse<TestResponse>> = testService.getTestResponse(params)

    fun getTestSubmit(testSubscriptionId: Int): RetrofitLiveData<ApiResponse<TestSubmit>> =
        testService.getTestSubmit(testSubscriptionId)

    fun getTestResult(token: String, testSubscriptionId: Int): Single<ApiResponse<TestResult>> =
        testService.getTestResult(testSubscriptionId)

    fun getTestLeaderboard(
        token: String,
        testId: Int
    ): RetrofitLiveData<ApiResponse<ArrayList<TestLeaderboard>>> =
        testService.getTestLeaderboard(testId)
}
