package com.doubtnutapp.data.structuredCourse.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.mocktestLibrary.model.ApiMockTestDetails
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface LiveClassesService {

    @GET("v8/testseries/testData/{mock_test_id}")
    fun getLiveClassesMockData(
        @Path("mock_test_id") mockTestId: Int

    ): Single<ApiResponse<ApiMockTestDetails>>
}
