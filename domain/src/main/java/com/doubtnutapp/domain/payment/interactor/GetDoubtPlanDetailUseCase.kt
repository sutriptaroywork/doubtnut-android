package com.doubtnutapp.domain.payment.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.DoubtPlanDetail
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
class GetDoubtPlanDetailUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<DoubtPlanDetail, Unit> {

    override fun execute(param: Unit): Single<DoubtPlanDetail> = paymentRepository.getDoubtPlanDetail()
}
