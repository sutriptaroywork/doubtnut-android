package com.doubtnutapp.liveclass.viewmodel

import com.doubtnutapp.data.remote.models.ApiResponse
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ReferralService {

    @GET("/v1/course/referral-info")
    suspend fun getReferralData(
        @Query("type") type: String?,
        @Query("assortment_type") assortmentType: String?,
        @Query("assortment_id") assortmentId: String?
    ): ApiResponse<ReferralData>

    @POST("/v1/tesla/post-feed")
    suspend fun postFeed(@Body requestBody: RequestBody): ApiResponse<ShareFeedData>
}