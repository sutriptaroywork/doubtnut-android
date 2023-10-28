package com.doubtnut.referral.data.remote

import com.doubtnut.core.data.remote.CoreResponse
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.core.utils.ContactData
import com.doubtnut.core.utils.ContactDataRequest
import com.doubtnut.referral.data.entity.ReferAndEarnLandingPageResponse
import com.doubtnut.referral.data.entity.ReferralInfoResponse
import okhttp3.RequestBody
import retrofit2.http.*

interface ReferralService {
    @GET("v2/course/referral-info")
    suspend fun getReferralInfo(
        @Query("page") page: Int
    ): CoreResponse<ReferralInfoResponse>

    @POST("v2/course/referral-share-info")
    @FormUrlEncoded
    suspend fun referralShareInfo(
        @Field("referee_name") refereeName: String,
        @Field("referee_phone") refereePhone: String
    ): CoreResponse<BaseResponse?>

    // referral page data
    @GET("v1/student/refer-and-earn")
    suspend fun getReferAndEarnLandingPageData(): CoreResponse<ReferAndEarnLandingPageResponse>

    @GET("v1/student/refer-and-earn-faq")
    suspend fun getReferAndEarnFAQData(): CoreResponse<ReferAndEarnLandingPageResponse>

    @POST("v1/student/storing-referrer-id")
    suspend fun sendReferralCode(@Body requestBody: RequestBody): BaseResponse

    @GET("v1/contacts/read")
    suspend fun getContacts(): CoreResponse<List<ContactData>>

    @POST("v1/contacts/insert")
    suspend fun storeContact(
        @Body body: ContactDataRequest
    ): CoreResponse<BaseResponse?>


}