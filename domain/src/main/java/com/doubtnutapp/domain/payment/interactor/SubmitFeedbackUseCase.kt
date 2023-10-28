package com.doubtnutapp.domain.payment.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-21.
 */
class SubmitFeedbackUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<Any, String> {

    override fun execute(param: String): Single<Any> = paymentRepository.submitFeedback(param)
}
