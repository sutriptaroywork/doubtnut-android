package com.doubtnutapp.domain.payment.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.PaymentStartBody
import com.doubtnutapp.domain.payment.entities.Taxation
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-13.
 */
class GetPaymentInitiateInfoUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<Taxation, PaymentStartBody> {

    override fun execute(paymentStartBody: PaymentStartBody): Single<Taxation> = paymentRepository.getPaymentInitiationInfo(paymentStartBody)
}
