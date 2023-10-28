package com.doubtnutapp.domain.payment.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.TrialVipResponse
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
class TrialVipUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<TrialVipResponse, String> {

    override fun execute(param: String): Single<TrialVipResponse> = paymentRepository.trialVip(param)
}
