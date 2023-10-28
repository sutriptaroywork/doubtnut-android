package com.doubtnutapp.domain.payment.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.*
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
class GetCheckoutDataUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<CheckoutData, GetCheckoutDataUseCase.Param> {

    override fun execute(param: Param): Single<CheckoutData> =
        paymentRepository.fetchCheckoutData(
            param.variantId,
            param.coupon,
            param.paymentFor,
            param.amount,
            param.hasUpiApp
        )

    @Keep
    data class Param(
        val variantId: String?,
        val coupon: String?,
        val paymentFor: String?,
        val amount: String?,
        val hasUpiApp: Boolean
    )

    fun getPaymentInitiationInfo(paymentStartBody: PaymentStartBody?): Single<Taxation> =
        paymentRepository.getPaymentInitiationInfo(paymentStartBody)

    fun getPackagePaymentInfo(params: HashMap<String, Any>): Single<PackagePaymentInfo> =
        paymentRepository.getPackagePaymentInfo(params)

    fun getCouponData(hashMap: HashMap<String, Any>): Single<CouponData> =
        paymentRepository.getCouponData(hashMap)
}
