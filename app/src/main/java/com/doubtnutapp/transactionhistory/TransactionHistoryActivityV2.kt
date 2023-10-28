package com.doubtnutapp.transactionhistory

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ActivityTransactionHistoryV2Binding
import com.doubtnutapp.store.ui.adapter.StoreResultPagerAdapter
import com.doubtnutapp.transactionhistory.viewmodel.TransactionHistoryViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.EventObserver
import javax.inject.Inject

/**
 * Created by Akshat Jindal 19/4/21
 */

class TransactionHistoryActivityV2 :
    BaseBindingActivity<TransactionHistoryViewModel, ActivityTransactionHistoryV2Binding>() {

    companion object {
        @Suppress("unused")
        private const val TAG = "TransactionHistoryActivityV2"
        fun getStartIntent(context: Context) =
            Intent(context, TransactionHistoryActivityV2::class.java)
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun provideViewBinding(): ActivityTransactionHistoryV2Binding {
        return ActivityTransactionHistoryV2Binding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): TransactionHistoryViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setUpToolbar()
        setUp()
    }

    private fun setUpToolbar() {
        setSupportActionBar(binding.toolBar)
        binding.toolBar.title = getString(R.string.payment_history)
        binding.toolBar.setNavigationOnClickListener {
            onBackPressed()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setUp() {
        val fragmentList = listOf(
            TransactionHistoryFragment.newInstance(true),
            TransactionHistoryFragment.newInstance(false)
        )
        val titleList = listOf(getString(R.string.successful), getString(R.string.failed))

        val viewPagerAdapter =
            StoreResultPagerAdapter(supportFragmentManager, fragmentList, titleList)
        binding.viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.messageStringIdLiveData.observe(this, EventObserver {
            showMessage(it)
        })
    }
}

