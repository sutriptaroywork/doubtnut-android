package com.doubtnutapp.data.store.repository

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.store.model.ApiConvertCoins
import io.reactivex.Single
import retrofit2.http.GET

interface ConvertCoinsService {

    @GET("v2/gamification/convertcoins/")
    fun convertCoins(): Single<ApiResponse<ApiConvertCoins>>
}
