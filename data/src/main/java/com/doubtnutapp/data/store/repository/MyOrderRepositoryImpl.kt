package com.doubtnutapp.data.store.repository

import com.doubtnutapp.data.store.mapper.MyOrderEntityMapper
import com.doubtnutapp.domain.store.entities.MyOrderEntity
import com.doubtnutapp.domain.store.repository.MyOrderRepository
import io.reactivex.Single
import javax.inject.Inject

class MyOrderRepositoryImpl @Inject constructor(
    private val myOrderService: MyOrderService,
    private val myOrderEntityMapper: MyOrderEntityMapper
) : MyOrderRepository {

    override fun getMyOrderResult(): Single<MyOrderEntity> =
        myOrderService.getMyOrderResult().map {
            myOrderEntityMapper.map(it.data)
        }
}
