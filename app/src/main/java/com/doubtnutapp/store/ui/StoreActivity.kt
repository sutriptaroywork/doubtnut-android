package com.doubtnutapp.store.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.base.extension.viewBinding
import com.doubtnutapp.store.dto.StoreResultDTO
import com.doubtnutapp.store.ui.adapter.StoreResultPagerAdapter
import com.doubtnutapp.store.viewmodel.StoreViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.Utils
import com.google.android.material.tabs.TabLayout
import com.doubtnutapp.databinding.ActivityStoreBinding
import com.doubtnutapp.databinding.LayoutRedeemStoreBadgesBinding

class StoreActivity : BaseBindingActivity<StoreViewModel, ActivityStoreBinding>(),
    View.OnClickListener {

    private lateinit var storeResultPagerAdapter: StoreResultPagerAdapter

    private var unUsedPoints: Int = 0
    private var availableDnCoins: Int = 0

    private val redeemBinding by viewBinding(LayoutRedeemStoreBadgesBinding::inflate)

    companion object {
        const val TAG = "StoreActivity"
        const val MY_ORDER_COUNT = "my_order_count"
        const val DN_CASH = "dn_cash"
        fun startActivity(context: Context) {
            Intent(context, StoreActivity::class.java).also {
                context.startActivity(it)
            }
        }

        fun getStartIntent(context: Context) = Intent(context, StoreActivity::class.java)
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityStoreBinding =
        ActivityStoreBinding.inflate(layoutInflater)

    override fun provideViewModel(): StoreViewModel =
        viewModelProvider(viewModelFactory)

    override fun onResume() {
        super.onResume()
        viewModel.getStoreResults()
    }

    override fun setupView(savedInstanceState: Bundle?) {
        statusbarColor(this, R.color.grey_statusbar_color)
        init()
    }

    private fun init() {
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(viewModel::class.java)
        setUpStoreViewPager()
        setUpObservers()
        setListeners()
    }

    private fun setListeners() {
        binding.closeStoreScreen.setOnClickListener(this)
        binding.myOrderIcon.setOnClickListener(this)
        binding.myOrderTitle.setOnClickListener(this)
        binding.myOrderCount.setOnClickListener(this)
    }

    private fun setUpStoreViewPager() {
        storeResultPagerAdapter =
            StoreResultPagerAdapter(supportFragmentManager, emptyList(), emptyList())
        binding.storeResultViewPager.adapter = storeResultPagerAdapter
    }

    private fun setUpObservers() {

        viewModel.storeResultLiveData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.coinsLiveData.observeK(
            this,
            ::onCoinSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.freeExpLiveData.observeK(
            this,
            ::onFreeExpSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.orderCountLiveData.observeK(
            this,
            ::onOrderCountSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

    }

    private fun onSuccess(storeResult: StoreResultDTO) {
        updateStoreResult(storeResult)
    }

    private fun updateStoreResult(storeResult: StoreResultDTO) {
        val fragmentList = mutableListOf<Fragment>()

        for ((_, result) in storeResult.storeResult) {
            fragmentList.add(StoreFragment.newInstance(result, storeResult.coins))
        }

        storeResultPagerAdapter.updateStoreResultList(storeResult.storeTabNameList, fragmentList)
        storeResultPagerAdapter.notifyDataSetChanged()

        if (fragmentList.size > 1) {
            binding.storeCategoryTab.setupWithViewPager(binding.storeResultViewPager)
            if (fragmentList.size > 3) {
                binding.storeCategoryTab.tabMode = TabLayout.MODE_SCROLLABLE
            } else {
                binding.storeCategoryTab.tabMode = TabLayout.MODE_FIXED
            }
        } else {
            binding.storeCategoryTab.visibility = View.GONE
        }

        binding.storeCategoryTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabReselected(p0: TabLayout.Tab?) {

            }

            override fun onTabUnselected(p0: TabLayout.Tab?) {

            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                viewModel.publishEvent(EventConstants.STORE_SCREEN_TAB_SELECTED + "_" + p0.toString())
            }
        })
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {

    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.closeStoreScreen -> {
                finish()
            }
            binding.myOrderIcon, binding.myOrderTitle, binding.myOrderCount -> {
                viewModel.publishEvent(EventConstants.EVENT_NAME_MY_ORDER_CLICK, ignoreSnowplow = true)
                openMyOrderActivity()
            }
        }
    }

    private fun openConvertCoinsActivity() {
        Utils.executeIfContextNotNull(this) { context: Context ->
            ConvertCoinsActivity.startActivity(context, unUsedPoints)
        }
    }

    private fun openMyOrderActivity() {
        Utils.executeIfContextNotNull(this) { context: Context ->
            MyOrderActivity.startActivity(context)
        }
    }

    private fun onCoinSuccess(dnCoins: Int) {
        availableDnCoins = dnCoins
        redeemBinding.textDnCoins.text = dnCoins.toString()

        defaultPrefs(this).edit().putInt(DN_CASH, dnCoins).apply()
    }

    private fun onFreeExpSuccess(expPoints: Int) {
        unUsedPoints = expPoints
    }

    private fun onOrderCountSuccess(orderCount: Int) {
        if (orderCount > 0) {
            binding.myOrderCount.show()
            binding.myOrderCount.text = orderCount.toString()
        } else {
            binding.myOrderCount.invisible()
        }

        // Set my order count in preference after buying item.
        defaultPrefs(this).edit().putInt(MY_ORDER_COUNT, orderCount).apply()
    }
}


