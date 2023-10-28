package com.doubtnutapp.store.mapper

import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.store.entities.MyOrderResultEntity
import com.doubtnutapp.store.model.MyOrderResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MyOrderResultMapper @Inject constructor() : Mapper<MyOrderResultEntity, MyOrderResult> {

    override fun map(srcObject: MyOrderResultEntity): MyOrderResult = with(srcObject) {
        MyOrderResult(
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
            isLast,
            R.layout.item_store_my_order
        )
    }
}