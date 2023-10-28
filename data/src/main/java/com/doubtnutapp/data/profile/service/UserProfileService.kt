package com.doubtnutapp.data.profile.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.profile.model.ApiStudentClass
import com.doubtnutapp.data.profile.model.ApiUserProfile
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserProfileService {

    @GET("/v2/chapter/{class}/{course}/{chapter}/{subtopic}/get-details")
    fun getMicroConcepts(
        @Path("class") clazz: String,
        @Path("chapter") chapter: String,
        @Path("course") course: String,
        @Path("subtopic") subTopic: String
    ): Single<ApiResponse<List<ApiUserProfile>>>

    @POST("v4/student/update-profile")
    fun updateProfile(@Body params: RequestBody): Single<ApiResponse<ResponseBody>>

    @GET("v3/student/get-profile/{student_id}")
    fun getProfileDetails(
        @Path("student_id") student_id: String
    ): Single<ApiResponse<ApiUserProfile>>

    //
    @POST("v2/student/check-username")
    fun getUserName(@Body params: RequestBody): Observable<ApiResponse<RequestBody>>

    @GET("/v3/class/get-list/{lng_code}")
    fun getClassData(@Path("lng_code") languageCode: String): Single<ApiResponse<List<ApiStudentClass>>>
}
