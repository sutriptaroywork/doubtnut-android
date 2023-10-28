package com.doubtnutapp.domain.store.repository

import com.doubtnutapp.domain.store.entities.ConvertCoinsEntity
import io.reactivex.Single

interface ConvertCoinsRepository {
    fun convertCoins(): Single<ConvertCoinsEntity>
}
