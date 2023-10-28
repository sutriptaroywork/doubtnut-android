package com.doubtnutapp.payment.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityPaymentHelpBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity

/**
 * Created by Anand Gaurav on 2019-12-21.
 */
class PaymentHelpActivity : BaseBindingActivity<DummyViewModel, ActivityPaymentHelpBinding>() {

    companion object {
        private const val TAG = "PaymentHelpActivity"

        fun getStartIntent(context: Context, title: String, description: String) =
            Intent(context, PaymentHelpActivity::class.java).apply {
                putExtra("title", title)
                putExtra("description", description)
            }
    }

    override fun provideViewBinding(): ActivityPaymentHelpBinding {
        return ActivityPaymentHelpBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey
    }

    override fun setupView(savedInstanceState: Bundle?) {
        binding.buttonBack.setOnClickListener {
            onBackPressed()
        }

        if (intent.hasExtra("title")) {
            binding.textViewTitle.text = intent.getStringExtra("title")
        }

        if (intent.hasExtra("description")) {
            binding.textViewDescription.text = intent.getStringExtra("description")
        }
    }
}
