package com.doubtnutapp.domain.store.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.store.entities.StoreEntity
import com.doubtnutapp.domain.store.repository.StoreRepository
import io.reactivex.Single
import javax.inject.Inject

class GetStoreResult @Inject constructor(private val storeRepository: StoreRepository) : SingleUseCase<StoreEntity, GetStoreResult.Param> {

    override fun execute(param: Param): Single<StoreEntity> = storeRepository.getStoreResult()

    data class Param(val keyword: String)
}
