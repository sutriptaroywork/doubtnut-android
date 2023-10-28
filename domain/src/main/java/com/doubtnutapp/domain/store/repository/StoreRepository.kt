package com.doubtnutapp.domain.store.repository

import com.doubtnutapp.domain.store.entities.RedeemStoreItemEntity
import com.doubtnutapp.domain.store.entities.StoreEntity
import io.reactivex.Single

interface StoreRepository {
    fun getStoreResult(): Single<StoreEntity>
    fun redeemStoreItem(id: Int): Single<RedeemStoreItemEntity>
}
