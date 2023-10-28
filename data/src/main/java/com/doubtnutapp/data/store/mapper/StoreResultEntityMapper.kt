package com.doubtnutapp.data.store.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.store.model.ApiStoreData
import com.doubtnutapp.domain.store.entities.StoreResultEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreResultEntityMapper @Inject constructor() : Mapper<ApiStoreData, StoreResultEntity> {

    override fun map(srcObject: ApiStoreData) = with(srcObject) {
        StoreResultEntity(
            id = id,
            resourceType = resourceType,
            resourceId = resourceId,
            title = title,
            description = description,
            imgUrl = imgUrl,
            isActive = isActive,
            price = price,
            createdAt = createdAt,
            displayCategory = displayCategory,
            isLast = isLast,
            redeemStatus = redeemStatus,
            availableDnCash = 0
        )
    }
}
