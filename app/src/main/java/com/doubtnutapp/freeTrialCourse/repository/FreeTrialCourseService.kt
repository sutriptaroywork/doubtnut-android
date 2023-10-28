package com.doubtnutapp.freeTrialCourse.repository

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.freeTrialCourse.model.FreeTrialCourseResponse
import com.doubtnutapp.freeTrialCourse.model.FreeTrialCourseActivationResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface FreeTrialCourseService {

    @GET("v1/course/recommended-courses-by-user-category")
    suspend fun getFreeTrialCourses(): FreeTrialCourseResponse

    @GET("v3/package/trial")
    suspend fun activateTrial(
        @Query(value = "assortment_id") assortmentId: String
    ): ApiResponse<FreeTrialCourseActivationResponse>
}