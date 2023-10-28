package com.doubtnutapp.domain.store.repository

import com.doubtnutapp.domain.store.entities.MyOrderEntity
import io.reactivex.Single

interface MyOrderRepository {
    fun getMyOrderResult(): Single<MyOrderEntity>
}
