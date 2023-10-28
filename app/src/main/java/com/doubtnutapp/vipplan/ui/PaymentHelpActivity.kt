package com.doubtnutapp.vipplan.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.databinding.ActivityPaymentHelp2Binding
import com.doubtnutapp.domain.payment.entities.PaymentHelpData
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.vipplan.viewmodel.PaymentHelpViewModel

class PaymentHelpActivity :
    BaseBindingActivity<PaymentHelpViewModel, ActivityPaymentHelp2Binding>() {

    companion object {
        private const val TAG = "PaymentHelpActivity"
        private const val INTENT_EXTRA_PAYMENT_HELP_DATA = "payment_help_data"
        fun getStartIntent(
            context: Context,
            paymentHelpData: PaymentHelpData
        ) =
            Intent(context, PaymentHelpActivity::class.java).apply {
                putExtra(INTENT_EXTRA_PAYMENT_HELP_DATA, paymentHelpData)
            }
    }

    override fun provideViewBinding(): ActivityPaymentHelp2Binding =
        ActivityPaymentHelp2Binding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): PaymentHelpViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        val adapter = PaymentHelpAdapter()
        binding.recyclerView.adapter = adapter
        binding.imageViewBack.setOnClickListener {
            goBack()
        }
        val paymentHelpData: PaymentHelpData =
            intent.getParcelableExtra(INTENT_EXTRA_PAYMENT_HELP_DATA)!!
        binding.toolbarTitle.text = paymentHelpData.content?.title.orEmpty()
        adapter.updateList(paymentHelpData.content?.paymentHelpItems.orEmpty())
    }

}