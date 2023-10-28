package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Widgets
import com.doubtnutapp.data.remote.models.teacherchannel.TeacherProfile
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TeacherChannelService {

    @GET("v1/teacher/channel")
    fun getTeacherChannel(
        @Query("teacher_id") teacherId: Int,
        @Query("type") teacherType: String?,
        @Query("page") page: Int,
        @Query("tab_filter") tabFilter: String?,
        @Query("sub_filter") subFilter: String?,
        @Query("content_filter") contentFilter: String?
    ): Single<ApiResponse<Widgets>>

    @GET("v1/teacher/profile-student")
    fun getTeacherProfile(
        @Query("teacher_id") teacherId: Int,
    ): Single<ApiResponse<TeacherProfile>>

    @POST("v1/teacher/subscribe")
    fun subscribeChannel(@Body requestBody: RequestBody): Completable
}
