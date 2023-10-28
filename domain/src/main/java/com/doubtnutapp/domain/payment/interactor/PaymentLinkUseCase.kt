package com.doubtnutapp.domain.payment.interactor

import androidx.annotation.Keep
import com.doubtnutapp.domain.base.SingleUseCase
import com.doubtnutapp.domain.payment.entities.PaymentLinkCreate
import com.doubtnutapp.domain.payment.repository.PaymentRepository
import io.reactivex.Single
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
class PaymentLinkUseCase @Inject constructor(private val paymentRepository: PaymentRepository) :
    SingleUseCase<PaymentLinkCreate, PaymentLinkUseCase.Param> {

    override fun execute(param: Param): Single<PaymentLinkCreate> {
        val apiParams = hashMapOf<String, Any>(
            "amount" to param.amount,
            "payment_for" to param.paymentFor
        ).apply {
            if (param.coupon.isNotBlank()) {
                put("coupon_code", param.coupon)
            }
            if (param.variantId.isNotBlank()) {
                put("variant_id", param.variantId)
            }
            if (param.paymentForId.isNotBlank()) {
                put("payment_for_id", param.paymentForId)
            }
            if (param.totalAmount.isNotBlank()) {
                put("total_amount", param.totalAmount)
            }
            if (param.discount.isNotBlank()) {
                put("discount", param.discount)
            }

            if (param.walletAmount.isNotBlank()) {
                put("wallet_amount", param.walletAmount)
            }

            if (param.payUsingWallet != null) {
                put("pay_using_wallet", param.payUsingWallet)
            }

            if (param.useWallet != null) {
                put("use_wallet", param.useWallet)
            }

            if (!param.finalAmountWithWallet.isNullOrBlank()) {
                put("final_amount_with_wallet", param.finalAmountWithWallet)
            }
        }
        return paymentRepository.createPaymentLink(apiParams)
    }

    @Keep
    data class Param(
        val amount: String,
        val coupon: String,
        val variantId: String,
        val paymentFor: String,
        val paymentForId: String,
        val totalAmount: String,
        val discount: String,
        val walletAmount: String,
        val useWallet: Boolean?,
        val payUsingWallet: Boolean?,
        val finalAmountWithWallet: String?
    )
}
