package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.domain.payment.entities.PaymentStartBody
import com.doubtnutapp.domain.payment.entities.Taxation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 24/11/20.
 */
class QrPaymentRepository @Inject constructor(private val networkService: NetworkService) {

    fun fetchQrPaymentInitialData(paymentStartBody: PaymentStartBody): Flow<ApiResponse<Taxation>> =
        flow { emit(networkService.getQrPaymentInitialInfo(paymentStartBody)) }

    fun fetchQrPaymentData(orderId: String): Flow<ApiResponse<Taxation>> =
        flow { emit(networkService.getQrPaymentInfo(orderId)) }
}
