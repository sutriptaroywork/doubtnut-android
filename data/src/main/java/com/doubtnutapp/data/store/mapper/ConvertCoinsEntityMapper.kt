package com.doubtnutapp.data.store.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.store.model.ApiConvertCoins
import com.doubtnutapp.domain.store.entities.ConvertCoinsEntity
import javax.inject.Inject

class ConvertCoinsEntityMapper @Inject constructor(
    private val storeResultEntityMapper: StoreResultEntityMapper
) : Mapper<ApiConvertCoins, ConvertCoinsEntity> {

    override fun map(srcObject: ApiConvertCoins): ConvertCoinsEntity = with(srcObject) {
        ConvertCoinsEntity(
            isConverted,
            message,
            convertedXp
        )
    }
}
