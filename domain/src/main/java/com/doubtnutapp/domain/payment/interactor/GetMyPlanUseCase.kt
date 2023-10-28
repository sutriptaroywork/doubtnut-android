package com.doubtnutapp.domain.payment.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
class GetMyPlanUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<List<WidgetEntityModel<WidgetData, WidgetAction>>, Unit> {

    override fun execute(param: Unit): Single<List<WidgetEntityModel<WidgetData, WidgetAction>>> =
        paymentRepository.getMyPlanDetail()
}
