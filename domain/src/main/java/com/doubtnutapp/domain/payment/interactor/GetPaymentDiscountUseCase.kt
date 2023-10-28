package com.doubtnutapp.domain.payment.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.PaymentDiscount
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

class GetPaymentDiscountUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<PaymentDiscount, GetPaymentDiscountUseCase.Param> {

    override fun execute(param: Param): Single<PaymentDiscount> = paymentRepository.getPaymentDiscount(param.orderId)

    @Keep
    data class Param(val orderId: String)
}
