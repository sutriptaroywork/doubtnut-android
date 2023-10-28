package com.doubtnutapp.domain.payment.repository

import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.domain.payment.entities.*
import io.reactivex.Single

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
interface PaymentRepository {
    //    fun getPaymentInitiationInfo(amount: String, source: String, extraParams: HashMap<String, String>?): Single<Taxation>
    fun getPaymentInitiationInfo(paymentStartBody: PaymentStartBody?): Single<Taxation>
    fun getContinuePaymentInfo(orderId: String): Single<Taxation>
    fun getPlanDetail(
        assortmentId: String,
        bottomSheet: Boolean,
        source: String?
    ): Single<PlanDetail>

    fun getPaymentDiscount(orderId: String): Single<PaymentDiscount>
    fun onPaymentSuccess(
        source: String,
        paymentProviderData: HashMap<String, String>
    ): Single<ServerResponsePayment>

    fun submitFeedback(message: String): Single<Any>
    fun getTransactionHistoryV2(status: String, page: Int): Single<List<TransactionHistoryItem>>
    fun fetchBreakthrough(): Single<List<BreakThrough>>
    fun trialVip(id: String): Single<TrialVipResponse>
    fun fetchCheckoutData(
        variantId: String?,
        coupon: String?,
        paymentFor: String?,
        amount: String?,
        hasUpiApp: Boolean
    ): Single<CheckoutData>

    fun getMyPlanDetail(): Single<List<WidgetEntityModel<WidgetData, WidgetAction>>>
    fun getDoubtPlanDetail(): Single<DoubtPlanDetail>
    fun getPaymentLinkInfo(source: String?): Single<PaymentLinkInfo>
    fun createPaymentLink(params: HashMap<String, Any>): Single<PaymentLinkCreate>
    fun getPackagePaymentInfo(params: HashMap<String, Any>): Single<PackagePaymentInfo>
    fun getCouponData(hashMap: HashMap<String, Any>): Single<CouponData>
}
