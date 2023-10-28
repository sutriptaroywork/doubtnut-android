package com.doubtnutapp.domain.payment.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.Taxation
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

class GetContinuePaymentInfoUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<Taxation, String> {

    override fun execute(orderId: String): Single<Taxation> = paymentRepository.getContinuePaymentInfo(orderId)
}
