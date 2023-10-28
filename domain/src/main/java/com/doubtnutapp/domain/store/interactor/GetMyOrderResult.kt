package com.doubtnutapp.domain.store.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.store.entities.MyOrderEntity
import com.doubtnutapp.domain.store.repository.MyOrderRepository
import io.reactivex.Single
import javax.inject.Inject

class GetMyOrderResult @Inject constructor(private val myOrderRepository: MyOrderRepository) : SingleUseCase<MyOrderEntity, GetMyOrderResult.Param> {

    override fun execute(param: Param): Single<MyOrderEntity> = myOrderRepository.getMyOrderResult()

    data class Param(val keyword: String)
}
