package com.doubtnutapp.vipplan.ui

import com.doubtnutapp.domain.payment.entities.PaymentActivityBody

/**
 * Created by Anand Gaurav on 02/10/20.
 */
interface CheckoutListener {
    fun onPayButtonClick(paymentActivityBody: PaymentActivityBody)
}