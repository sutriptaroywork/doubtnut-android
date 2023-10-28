package com.doubtnutapp.dnr.service

import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.dnr.model.*
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

interface DnrMicroService {

    @GET("api/dnr/home")
    fun getDnrHome(): Single<ApiResponse<DnrWidgetListData>>

    @POST("api/dnr/earn-history")
    fun getEarnedHistory(@Body requestBody: RequestBody): Single<ApiResponse<DnrWidgetListData>>

    @GET("api/dnr/milestones")
    fun getDnrSummary(@Query(value = "page") page: String): Single<ApiResponse<DnrWidgetListData>>

    @GET("api/dnr/faq")
    fun getDnrFaq(): Single<ApiResponse<DnrWidgetListData>>

    @POST("api/dnr/list-redeem-vouchers")
    fun getRedeemVoucherList(@Query(value = "page") page: String): Single<ApiResponse<DnrWidgetListData>>

    @POST("api/dnr/list-unlocked-vouchers")
    fun getUnlockVoucherList(@Query(value = "page") page: String): Single<ApiResponse<DnrWidgetListData>>

    @GET("api/dnr/voucher-tabs")
    fun getVoucherTabList(): Single<ApiResponse<VoucherData>>

    @GET("api/dnr/get-pending-redemption-details")
    fun getPendingVoucherBottomSheetData(): Single<ApiResponse<DnrPendingVoucherData>>

    @GET("api/dnr/mark-app-open")
    fun markAppOpen(): Single<ApiResponse<DnrMarkAppOpen>>

    @POST("api/dnr/voucher-page")
    fun getRewardData(
        @Query(value = "voucher_id") voucherId: String,
        @Query(value = "redeem_id") redeemId: String,
        @Query(value = "source") source: String
    ): Single<ApiResponse<VoucherInitialData>>

    @POST("api/dnr/claim-reward")
    fun claimReward(@Body requestBody: RequestBody): Single<ApiResponse<DnrReward>>

    @GET("api/dnr/get-course-purchase-popup")
    fun markCoursePurchased(): Single<ApiResponse<DnrReward>>

    @GET("api/dnr/mystery-box")
    fun getMysteryBoxData(): Single<ApiResponse<DnrMysteryBoxInitialData>>

    @GET("api/dnr/spin-wheel-page")
    fun getSpinTheWheelData(): Single<ApiResponse<DnrSpinTheWheelInitialData>>

    @PUT("api/dnr/remind-streak")
    suspend fun setReminderForDnrReward(): BaseResponse
}
