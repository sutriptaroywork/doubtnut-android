package com.doubtnutapp.data.store.mapper

import com.doubtnutapp.data.base.Mapper
import com.doubtnutapp.data.store.model.ApiStore
import com.doubtnutapp.data.store.model.ApiStoreData
import com.doubtnutapp.domain.store.entities.StoreEntity
import com.doubtnutapp.domain.store.entities.StoreResultEntity
import javax.inject.Inject

class StoreEntityMapper @Inject constructor(
    private val storeResultEntityMapper: StoreResultEntityMapper
) : Mapper<ApiStore, StoreEntity> {

    override fun map(srcObject: ApiStore) = with(srcObject) {
        val result = HashMap<String, List<StoreResultEntity>>()
        for ((k, v) in srcObject.tabsData) {
            result[k] = getStoreResult(v, coins)
        }
        return@with StoreEntity(
            coins,
            freexp,
            tabs,
            result
        )
    }

    private fun getStoreResult(storeResultList: List<ApiStoreData>, coins: Int): List<StoreResultEntity> =
        storeResultList.map {
            storeResultEntityMapper.map(it).also {
                it.availableDnCash = coins
            }
        }
}
