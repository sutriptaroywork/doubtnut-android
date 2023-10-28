package com.doubtnutapp.data.course.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.domain.course.entities.CourseDataEntity
import com.doubtnutapp.domain.course.entities.SchedulerListingEntity
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CourseService {

    @GET("/v2/course/get-lectures/{id}")
    fun getLectures(@Path(value = "id") id: String): Single<ApiResponse<CourseDataEntity>>

    @GET("v1/course/scheduler-listing")
    fun getSchedulerListing(
        @Query(value = "subjects") subjects: String?,
        @Query(value = "page") page: Int,
        @Query(value = "slot") slot: String?,
    ): Single<ApiResponse<SchedulerListingEntity>>
}
