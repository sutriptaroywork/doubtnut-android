package com.doubtnutapp.dnr.service

import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.dnr.model.VoucherInitialData
import io.reactivex.Single
import retrofit2.http.POST
import retrofit2.http.Query

interface RedeemDnrService {

    @POST("api/dnr/redeem-voucher")
    fun redeemVoucher(
        @Query(value = "voucher_id") voucherId: String,
        @Query(value = "source") source: String
    ): Single<ApiResponse<VoucherInitialData>>
}
