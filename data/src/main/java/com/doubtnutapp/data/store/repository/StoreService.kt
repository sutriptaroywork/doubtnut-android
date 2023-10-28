package com.doubtnutapp.data.store.repository

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.store.model.ApiRedeemStoreItem
import com.doubtnutapp.data.store.model.ApiStore
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface StoreService {

    @GET("v2/gamification/redeemstore/")
    fun getStoreResult(): Single<ApiResponse<ApiStore>>

    @GET("v2/gamification/{id}/redeem/")
    fun redeemStoreItem(@Path("id") id: String): Single<ApiResponse<ApiRedeemStoreItem>>
}
