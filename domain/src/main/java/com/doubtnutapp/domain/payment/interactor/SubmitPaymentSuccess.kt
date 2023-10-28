package com.doubtnutapp.domain.payment.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.ServerResponsePayment
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-21.
 */
class SubmitPaymentSuccess @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<ServerResponsePayment, SubmitPaymentSuccess.RequestValues> {

    override fun execute(param: RequestValues): Single<ServerResponsePayment> = paymentRepository.onPaymentSuccess(
        param.source, param.paymentProviderData
    )

    data class RequestValues(
        val source: String,
        val paymentProviderData: HashMap<String, String>
    )
}
