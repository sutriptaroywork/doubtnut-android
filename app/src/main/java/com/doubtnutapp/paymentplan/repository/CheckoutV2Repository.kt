package com.doubtnutapp.paymentplan.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.paymentplan.data.PaymentData
import com.doubtnutapp.toRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 08-10-2021
 */

class CheckoutV2Repository @Inject constructor(
    private val networkService: NetworkService
) {

    suspend fun getCheckoutData(
        amount: String?,
        variantId: String?,
        coupon: String?,
        paymentFor: String?,
        hasUpiApp: Boolean,
        selectedWallet: List<String>?,
        removeCoupon: Boolean,
        switchAssortmentId: String?
    ): Flow<ApiResponse<PaymentData>> {
        val checkoutMap = hashMapOf<String, Any>()

        if (!amount.isNullOrBlank())
            checkoutMap["amount"] = amount

        if (!variantId.isNullOrEmpty())
            checkoutMap["variant_id"] = variantId

        if (!coupon.isNullOrEmpty())
            checkoutMap["coupon_code"] = coupon

        if (!paymentFor.isNullOrEmpty())
            checkoutMap["payment_for"] = paymentFor

        if (selectedWallet != null)
            checkoutMap["selected_wallet"] = selectedWallet

        switchAssortmentId?.let {
            checkoutMap["switch_assortment"] = it
        }

        checkoutMap["remove_coupon"] = removeCoupon

        checkoutMap["has_upi_app"] = hasUpiApp

        return flow { emit(networkService.getCheckoutData(checkoutMap.toRequestBody())) }
    }
}