package com.doubtnutapp.domain.store.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.store.entities.RedeemStoreItemEntity
import com.doubtnutapp.domain.store.repository.StoreRepository
import io.reactivex.Single
import javax.inject.Inject

class RedeemStoreItem @Inject constructor(private val storeRepository: StoreRepository) : SingleUseCase<RedeemStoreItemEntity, RedeemStoreItem.Param> {

    override fun execute(param: Param): Single<RedeemStoreItemEntity> = storeRepository.redeemStoreItem(param.id)

    data class Param(val id: Int)
}
