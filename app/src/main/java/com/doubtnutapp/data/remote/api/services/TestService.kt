package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.*
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Created by akshaynandwana on
 * 17, December, 2018
 **/
interface TestService {

    @GET("v1/testseries/active")
    fun getTestDetails(): RetrofitLiveData<ApiResponse<ArrayList<TestDetails>>>

    @GET("v1/testseries/{test_id}/subscribe")
    fun getTestSubscribe(@Path(value = "test_id") testId: Int): Single<ApiResponse<TestSubscribe>>

    @GET("v1/testseries/rules/{rules_id}")
    fun getTestRules(@Path(value = "rules_id") rulesId: Int): RetrofitLiveData<ApiResponse<TestRules>>

    @GET("v1/testseries/{test_id}/questionsdata")
    fun getTestQuestions(@Path(value = "test_id") testId: Int): RetrofitLiveData<ApiResponse<ArrayList<TestQuestionDataOptions>>>

    @POST("v1/testseries/response")
    fun getTestResponse(@Body body: RequestBody): RetrofitLiveData<ApiResponse<TestResponse>>

    @GET("v9/testseries/{test_subscription_id}/submit")
    fun getTestSubmit(@Path(value = "test_subscription_id") testSubscriptionId: Int): RetrofitLiveData<ApiResponse<TestSubmit>>

    @GET("v1/testseries/{test_subscription_id}/result")
    fun getTestResult(@Path(value = "test_subscription_id") testSubscriptionId: Int): Single<ApiResponse<TestResult>>

    @GET("v1/testseries/{test_id}/leaderboard")
    fun getTestLeaderboard(@Path(value = "test_id") testId: Int): RetrofitLiveData<ApiResponse<ArrayList<TestLeaderboard>>>
}
