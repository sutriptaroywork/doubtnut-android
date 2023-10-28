package com.doubtnutapp.data.store.repository

import com.doubtnutapp.data.store.mapper.StoreEntityMapper
import com.doubtnutapp.domain.store.entities.RedeemStoreItemEntity
import com.doubtnutapp.domain.store.entities.StoreEntity
import com.doubtnutapp.domain.store.repository.StoreRepository
import io.reactivex.Single
import javax.inject.Inject

class StoreRepositoryImpl @Inject constructor(
    private val storeService: StoreService,
    private val storeEntityMapper: StoreEntityMapper
) : StoreRepository {

    override fun redeemStoreItem(id: Int): Single<RedeemStoreItemEntity> =
        storeService.redeemStoreItem(id.toString())
            .map {
                RedeemStoreItemEntity(
                    message = it.data.message
                )
            }

    override fun getStoreResult(): Single<StoreEntity> =
        storeService.getStoreResult().map {
            storeEntityMapper.map(it.data)
        }
}
