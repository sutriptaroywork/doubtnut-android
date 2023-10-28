package com.doubtnutapp.domain.payment.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.BreakThrough
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-09-29.
 */
class FetchBreakThroughUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<List<BreakThrough>, Unit> {

    override fun execute(param: Unit): Single<List<BreakThrough>> = paymentRepository.fetchBreakthrough()
}
