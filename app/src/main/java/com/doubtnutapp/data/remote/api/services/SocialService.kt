package com.doubtnutapp.data.remote.api.services

import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.ProfileData
import com.doubtnutapp.data.remote.models.feed.ApiFollowerWidgetItems
import io.reactivex.Single
import retrofit2.http.*

interface SocialService {

    @POST("v1/social/{user_id}/report")
    fun reportUser(
        @Path(value = "user_id") userId: String
    ): Single<ApiResponse<Any>>

    @GET("v1/social/{user_id}/following")
    fun getUserFollowing(
        @Path(value = "user_id") userId: String,
        @Query("page") page: Int
    ): RetrofitLiveData<ApiResponse<List<ProfileData>>>

    @GET("v1/social/{user_id}/followers")
    fun getUserFollowers(
        @Path(value = "user_id") userId: String,
        @Query("page") page: Int
    ): RetrofitLiveData<ApiResponse<List<ProfileData>>>

    @GET("v1/ban/status")
    fun userBanStatus(): Single<ApiResponse<Any>>

    @GET("v1/unban/getUnBanRequestStatus/{user_id}")
    fun getUserUnBanRequestStatus(@Path(value = "user_id") userId: String): Single<ApiResponse<Any>>

    @GET("v1/social/{user_id}/remove_follower")
    fun removeFollower(
        @Path(value = "user_id") userId: String
    ): Single<ApiResponse<Any>>

    @GET("v1/social/{user_id}/report_status")
    fun userReportStatus(
        @Path(value = "user_id") userId: String
    ): Single<ApiResponse<Any>>

    @GET("v1/social/most_popular_students")
    suspend fun getUsersToFollow(): ApiResponse<ApiFollowerWidgetItems>

    @PUT("v1/unban/sendUnBanRequest/{user_id}")
    fun sendUnbanRequest(
        @Path(value = "user_id") userId: String
    ): Single<ApiResponse<Any>>
}
