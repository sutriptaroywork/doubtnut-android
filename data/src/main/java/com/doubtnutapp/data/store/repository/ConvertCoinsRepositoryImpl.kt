package com.doubtnutapp.data.store.repository

import com.doubtnutapp.data.store.mapper.ConvertCoinsEntityMapper
import com.doubtnutapp.domain.store.entities.ConvertCoinsEntity
import com.doubtnutapp.domain.store.repository.ConvertCoinsRepository
import io.reactivex.Single
import javax.inject.Inject

class ConvertCoinsRepositoryImpl @Inject constructor(
    private val convertCoinsService: ConvertCoinsService,
    private val convertCoinsEntityMapper: ConvertCoinsEntityMapper
) : ConvertCoinsRepository {

    override fun convertCoins(): Single<ConvertCoinsEntity> =
        convertCoinsService.convertCoins().map {
            convertCoinsEntityMapper.map(it.data)
        }
}
