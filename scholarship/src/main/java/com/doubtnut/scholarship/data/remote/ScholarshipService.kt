package com.doubtnut.scholarship.data.remote

import com.doubtnut.core.data.remote.CoreResponse
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.scholarship.data.entity.ScholarshipData
import retrofit2.http.*

interface ScholarshipService {
    @GET("v2/scholarship/test")
    suspend fun getScholarshipData(
        @Query("id") id: String,
        @Query("change_test") changeTest: Boolean?
    ): CoreResponse<ScholarshipData>

    @POST("v2/scholarship/register-test")
    @FormUrlEncoded
    suspend fun registerScholarshipTest(
        @Field("test_id") id: String
    ): CoreResponse<BaseResponse>
}