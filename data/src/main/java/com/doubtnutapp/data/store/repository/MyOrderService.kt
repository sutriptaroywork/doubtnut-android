package com.doubtnutapp.data.store.repository

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.store.model.ApiMyOrderData
import io.reactivex.Single
import retrofit2.http.GET

interface MyOrderService {

    @GET("/v2/gamification/myorders")
    fun getMyOrderResult(): Single<ApiResponse<List<ApiMyOrderData>>>
}
