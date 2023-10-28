package com.doubtnutapp.domain.store.interactor

import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.store.entities.ConvertCoinsEntity
import com.doubtnutapp.domain.store.repository.ConvertCoinsRepository
import io.reactivex.Single
import javax.inject.Inject

class ConvertCoinsResult @Inject constructor(private val convertCoinsRepository: ConvertCoinsRepository) : SingleUseCase<ConvertCoinsEntity, ConvertCoinsResult.None> {

    override fun execute(param: None): Single<ConvertCoinsEntity> = convertCoinsRepository.convertCoins()

    class None
}
