package com.doubtnut.olympiad.data.remote

import com.doubtnut.core.data.remote.CoreResponse
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.olympiad.data.entity.OlympiadDetailResponse
import com.doubtnut.olympiad.data.entity.OlympiadSuccessResponse
import retrofit2.http.*

interface OlympiadService {
    @GET("v1/olympiad/get-details")
    suspend fun getOlympiadDetails(
        @Query("type") type: String,
        @Query("id") id: String,
    ): CoreResponse<OlympiadDetailResponse>

    @POST("v1/olympiad/register")
    @FormUrlEncoded
    suspend fun postOlympiadRegister(
        @Field("type") type: String,
        @Field("id") id: String,
        @Field("payload") payload: Map<String, String>,
    ): CoreResponse<BaseResponse>

    @GET("v1/olympiad/get-success")
    suspend fun getOlympiadSuccessData(
        @Query("type") type: String,
        @Query("id") id: String,
    ): CoreResponse<OlympiadSuccessResponse>
}