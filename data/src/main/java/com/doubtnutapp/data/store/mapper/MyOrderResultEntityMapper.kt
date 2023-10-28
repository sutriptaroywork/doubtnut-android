package com.doubtnutapp.data.store.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.store.model.ApiMyOrderData
import com.doubtnutapp.domain.store.entities.MyOrderResultEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyOrderResultEntityMapper @Inject constructor() :
    Mapper<ApiMyOrderData, MyOrderResultEntity> {

    override fun map(srcObject: ApiMyOrderData) = with(srcObject) {
        MyOrderResultEntity(
            id,
            resourceType,
            resourceId,
            title,
            description,
            imgUrl,
            isActive,
            price,
            createdAt,
            displayCategory,
            isLast
        )
    }
}
