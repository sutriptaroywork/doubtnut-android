package com.doubtnutapp.data.store.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.store.model.ApiMyOrderData
import com.doubtnutapp.domain.store.entities.MyOrderEntity
import com.doubtnutapp.domain.store.entities.MyOrderResultEntity
import javax.inject.Inject

class MyOrderEntityMapper @Inject constructor(
    private val myOrderResultEntityMapper: MyOrderResultEntityMapper
) : Mapper<List<ApiMyOrderData>, MyOrderEntity> {

    override fun map(srcObject: List<ApiMyOrderData>) = with(srcObject) {
        val result = mutableListOf<MyOrderResultEntity>()
        for (v in srcObject) {
            result.add(getMyOrderResult(v))
        }
        return@with MyOrderEntity(result)
    }

    private fun getMyOrderResult(myOrderData: ApiMyOrderData): MyOrderResultEntity =
        myOrderResultEntityMapper.map(myOrderData)
}
