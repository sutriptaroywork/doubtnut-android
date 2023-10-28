package com.doubtnutapp.domain.payment.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.PlanDetail
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
class GetPlanDetailUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<PlanDetail, GetPlanDetailUseCase.Param> {

    override fun execute(param: Param): Single<PlanDetail> = paymentRepository.getPlanDetail(
        param.assortmentId,
        param.bottomSheet, param.source
    )

    @Keep
    data class Param(val assortmentId: String, val bottomSheet: Boolean, val source: String?)
}
