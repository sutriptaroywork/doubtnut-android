package com.doubtnutapp.payment.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityPaymentStatusBinding
import com.doubtnutapp.payment.model.PaymentLinkData
import com.doubtnutapp.statusbarColor
import dagger.android.AndroidInjection

/**
 * Created by Anand Gaurav on 2019-12-14.
 */
class PaymentStatusActivity : AppCompatActivity() {

    companion object {
        const val PAYMENT_LINK_DATA = "payment_link_data"
        const val REASON = "reason"
        const val CURRENCY_SYMBOL = "currency_symbol"
        fun getStartIntent(
            context: Context, status: Boolean,
            transactionId: String, amount: String,
            hideButton: Boolean, paymentFor: String,
            type: String, paymentLinkData: PaymentLinkData? = null, reason: String = "", currencySymbol: String
        ) = Intent(context, PaymentStatusActivity::class.java).apply {
            putExtra("status", status)
            putExtra("transactionId", transactionId)
            putExtra("amount", amount)
            putExtra("hideButton", hideButton)
            putExtra("paymentFor", paymentFor)
            putExtra("type", type)
            putExtra(PAYMENT_LINK_DATA, paymentLinkData)
            putExtra(REASON, reason)
            putExtra(CURRENCY_SYMBOL, currencySymbol)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.grey)
        val binding = ActivityPaymentStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.getBooleanExtra("status", false)) {
            supportFragmentManager.beginTransaction()
                .add(R.id.paymentStatusFragment, PaymentSuccessFragment.newInstance()).commit()
        } else {
            supportFragmentManager.beginTransaction()
                .add(R.id.paymentStatusFragment, PaymentFailureFragment.newInstance()).commit()
        }
    }
}


