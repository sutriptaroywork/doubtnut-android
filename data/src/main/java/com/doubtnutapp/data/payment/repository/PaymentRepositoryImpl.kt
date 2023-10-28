package com.doubtnutapp.data.payment.repository

import com.doubtnutapp.data.payment.service.PaymentService
import com.doubtnutapp.data.toRequestBody
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.domain.payment.entities.*
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-13.
 */
class PaymentRepositoryImpl @Inject constructor(
    private val paymentService: PaymentService,
) : PaymentRepository {

    override fun getPaymentInitiationInfo(paymentStartBody: PaymentStartBody?): Single<Taxation> {

        return paymentService.getPaymentInitiationInfo(paymentStartBody).map {
            it.data
        }
    }

    override fun getContinuePaymentInfo(orderId: String): Single<Taxation> {
        val params: HashMap<String, Any> = HashMap()
        params["payment_info_id"] = orderId
        return paymentService.getContinuePaymentInfo(params.toRequestBody()).map {
            it.data
        }
    }

    override fun getPlanDetail(
        assortmentId: String,
        bottomSheet: Boolean,
        source: String?
    ): Single<PlanDetail> {
        return paymentService.getPlanDetail(assortmentId, bottomSheet, source).map {
            it.data
        }
    }

    override fun getPaymentDiscount(orderId: String): Single<PaymentDiscount> {
        return paymentService.getPaymentDiscount(orderId).map {
            it.data
        }
    }

    override fun onPaymentSuccess(
        source: String,
        paymentProviderData: HashMap<String, String>
    ): Single<ServerResponsePayment> {

        val params: HashMap<String, Any> = HashMap()
        params["source"] = source
        params["payment_response"] = paymentProviderData

        return paymentService.onPaymentSuccess(params.toRequestBody()).map {
            it.data
        }
    }

    override fun getTransactionHistoryV2(
        status: String,
        page: Int
    ): Single<List<TransactionHistoryItem>> {
        return paymentService.getTransactionHistoryV2(page, status).map {
            it.data
        }
    }

    override fun submitFeedback(message: String): Single<Any> {
        val params: HashMap<String, Any> = HashMap()
        params["feedback"] = message
        return paymentService.submitFeedback(params.toRequestBody()).map {
            it.data
        }
    }

    override fun fetchBreakthrough(): Single<List<BreakThrough>> {
        return paymentService.fetchBreakthrough().map {
            it.data
        }
    }

    override fun trialVip(id: String): Single<TrialVipResponse> {
        return paymentService.trialVip(id).map {
            it.data
        }
    }

    override fun fetchCheckoutData(
        variantId: String?,
        coupon: String?,
        paymentFor: String?,
        amount: String?,
        hasUpiApp: Boolean
    ): Single<CheckoutData> {

        val checkoutMap = hashMapOf<String, Any>()

        if (!variantId.isNullOrEmpty())
            checkoutMap["variant_id"] = variantId

        if (!coupon.isNullOrEmpty())
            checkoutMap["coupon_code"] = coupon

        if (!paymentFor.isNullOrEmpty())
            checkoutMap["payment_for"] = paymentFor

        if (!amount.isNullOrEmpty())
            checkoutMap["amount"] = amount

        checkoutMap["has_upi_app"] = hasUpiApp
        return paymentService.checkout(checkoutMap.toRequestBody()).map {
            it.data
        }
    }

    override fun getMyPlanDetail(): Single<List<WidgetEntityModel<WidgetData, WidgetAction>>> {
        return paymentService.getMyPlanDetail()
            .map {
                it.data
            }
    }

    override fun getDoubtPlanDetail(): Single<DoubtPlanDetail> {
        return paymentService.getDoubtPlanDetail().map {
            it.data
        }
    }

    override fun getPaymentLinkInfo(source: String?): Single<PaymentLinkInfo> {
        return paymentService.getPaymentLinkInfo(source)
            .map {
                it.data
            }
    }

    override fun createPaymentLink(params: HashMap<String, Any>): Single<PaymentLinkCreate> {
        return paymentService.createPaymentLink(params.toRequestBody())
            .map {
                it.data
            }
    }

    override fun getPackagePaymentInfo(params: HashMap<String, Any>): Single<PackagePaymentInfo> {
        return paymentService.getPackagePaymentInfo(params.toRequestBody())
            .map {
                it.data
            }
    }

    override fun getCouponData(hashMap: HashMap<String, Any>): Single<CouponData> {
        return paymentService.getCouponData(hashMap.toRequestBody())
            .map { it.data }
    }
}
