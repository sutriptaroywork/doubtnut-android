package com.doubtnutapp.data.mocktestLibrary.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.mocktestLibrary.model.ApiMockTest
import io.reactivex.Single
import retrofit2.http.GET

interface MockTestService {

    @GET("v9/testseries/active_mock_test")
    fun getMockTestData(): Single<ApiResponse<List<ApiMockTest>>>
}
