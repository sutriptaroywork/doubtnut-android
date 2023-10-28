package com.doubtnutapp.store.mapper

import com.doubtnutapp.R
import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.domain.store.entities.StoreResultEntity
import com.doubtnutapp.store.model.StoreResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StoreResultMapper @Inject constructor() : Mapper<StoreResultEntity, StoreResult> {

    override fun map(srcObject: StoreResultEntity): StoreResult = with(srcObject) {
        StoreResult(
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
            redeemStatus,
            availableDnCash,
            when (redeemStatus) {
                1 -> R.layout.item_store_result_open
                0 -> {
                    price?.let {
                        if (it <= availableDnCash) {
                            R.layout.item_store_result_buy
                        } else {
                            R.layout.item_store_result_disabled
                        }
                    } ?: R.layout.item_store_result_disabled
                }
                else -> R.layout.item_store_result_disabled
            }

        )
    }
}