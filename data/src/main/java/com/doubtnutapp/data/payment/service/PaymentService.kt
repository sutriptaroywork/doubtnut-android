package com.doubtnutapp.data.payment.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.domain.payment.entities.*
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.*

/**
 * Created by Anand Gaurav on 2019-12-13.
 */
interface PaymentService {
    @POST("v4/payment/start")
    fun getPaymentInitiationInfo(@Body paymentStartBody: PaymentStartBody?): Single<ApiResponse<Taxation>>

    @POST("v1/payment/temp-reward/continue-payment")
    fun getContinuePaymentInfo(@Body params: RequestBody): Single<ApiResponse<Taxation>>

    @GET("v7/package/info")
    fun getPlanDetail(
        @Query(value = "assortment_id") assortmentId: String,
        @Query(value = "bottom_sheet") bottomSheet: Boolean,
        @Query(value = "source") source: String?
    ): Single<ApiResponse<PlanDetail>>

    @GET("v1/payment/temp-reward/info")
    fun getPaymentDiscount(@Query(value = "order_id") orderId: String): Single<ApiResponse<PaymentDiscount>>

    @GET("v1/package/doubt/info")
    fun getDoubtPlanDetail(): Single<ApiResponse<DoubtPlanDetail>>

    @POST("v1/payment/complete")
    fun onPaymentSuccess(@Body params: RequestBody): Single<ApiResponse<ServerResponsePayment>>

    @GET("v3/payment/transaction-history/{page}")
    fun getTransactionHistoryV2(
        @Path(value = "page") page: Int,
        @Query(value = "pagetype") status: String
    ): Single<ApiResponse<List<TransactionHistoryItem>>>

    @POST("v1/package/feedback")
    fun submitFeedback(@Body params: RequestBody): Single<ApiResponse<Any>>

    @GET("v1/package/plan-days")
    fun fetchBreakthrough(): Single<ApiResponse<List<BreakThrough>>>

    @GET("/v2/package/trial")
    fun trialVip(@Query(value = "category_id") id: String): Single<ApiResponse<TrialVipResponse>>

    @POST("/v2/payment/checkout")
    fun checkout(@Body params: RequestBody): Single<ApiResponse<CheckoutData>>

    @GET("v7/package/my-plans")
    fun getMyPlanDetail(): Single<ApiResponse<List<WidgetEntityModel<WidgetData, WidgetAction>>>>

    @POST("/v1/payment/payment-link/create")
    fun createPaymentLink(@Body params: RequestBody): Single<ApiResponse<PaymentLinkCreate>>

    @GET("/v1/payment/payment-link/info")
    fun getPaymentLinkInfo(@Query(value = "source") source: String?): Single<ApiResponse<PaymentLinkInfo>>

    @POST("v3/payment/checkout")
    fun getPackagePaymentInfo(@Body params: RequestBody): Single<ApiResponse<PackagePaymentInfo>>

    @POST("v1/coupon/applicable-coupon-codes")
    fun getCouponData(@Body params: RequestBody): Single<ApiResponse<CouponData>>
}
