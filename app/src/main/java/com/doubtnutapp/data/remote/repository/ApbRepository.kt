package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.MicroService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.payment.ApbCashPaymentData
import com.doubtnutapp.payment.ApbLocationData
import io.reactivex.Single

class ApbRepository(val microService: MicroService) {

    fun getApbCashPaymentData(): Single<ApiResponse<ApbCashPaymentData>> =
        microService.getApbCashPaymentData()

    fun getApbLocationData(lat: String?, long: String?): Single<ApiResponse<ApbLocationData>> =
        microService.getApbLocationData(lat, long)
}
