package com.doubtnutapp.domain.payment.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.PaymentLinkInfo
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
class GetPaymentLinkInfoUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<PaymentLinkInfo, GetPaymentLinkInfoUseCase.Param> {

    override fun execute(param: Param): Single<PaymentLinkInfo> = paymentRepository.getPaymentLinkInfo(param.source)

    @Keep
    data class Param(val source: String?)
}
