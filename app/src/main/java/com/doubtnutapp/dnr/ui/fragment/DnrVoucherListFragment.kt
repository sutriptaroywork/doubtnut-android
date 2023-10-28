package com.doubtnutapp.dnr.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.databinding.FragmentDnrVoucherListBinding
import com.doubtnutapp.dnr.model.VoucherData
import com.doubtnutapp.dnr.ui.adapter.DnrVoucherViewPagerAdapter
import com.doubtnutapp.dnr.viewmodel.DnrVoucherViewModel
import com.doubtnutapp.hide
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnut.core.utils.viewModelProvider
import com.google.android.material.tabs.TabLayoutMediator

class DnrVoucherListFragment :
    BaseBindingFragment<DnrVoucherViewModel, FragmentDnrVoucherListBinding>() {

    companion object {
        private const val TAG = "DnrRedeemFragment"
    }

    private val args by navArgs<DnrVoucherListFragmentArgs>()
    private val navController by findNavControllerLazy()

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDnrVoucherListBinding =
        FragmentDnrVoucherListBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DnrVoucherViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel.getTabList()
        binding.toolbar.ivBack.setOnClickListener {
            mayNavigate {
                navController.navigateUp()
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.tabListLiveData.observe(this) {
            setViewPager(it)
        }
    }

    private fun setViewPager(data: VoucherData) {

        mBinding?.toolbar?.apply {
            val toolbarData = data.toolbarData
            tvTitle.text = toolbarData?.title
            tvEndTitle.apply {
                text = toolbarData?.dnr.toString()
                isVisible = toolbarData?.dnr.toString().isNotNullAndNotEmpty()
            }
            ivEnd.loadImage(toolbarData?.dnrImage)
            ivBack.setOnClickListener {
                mayNavigate {
                    navController.navigateUp()
                }
            }
            endLayout.isVisible = toolbarData?.dnr.isNotNullAndNotEmpty()
            endLayout.setOnClickListener {
                if (data.toolbarData?.deeplink != null) {
                    mayNavigate {
                        val deeplinkUri = Uri.parse(data.toolbarData.deeplink)
                        if (navController.graph.hasDeepLink(deeplinkUri)) {
                            navController.navigate(deeplinkUri)
                        }
                    }
                }
            }
        }
        val tabs = data.tabs.orEmpty()
        val activeTabId = args.activeTabId

        val adapter = DnrVoucherViewPagerAdapter(this, data.tabs.orEmpty())
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabs[position].title
        }.attach()

        if (data.tabs.orEmpty().size > 1) {
            tabs.forEachIndexed { index, tabData ->
                if (tabData.id == activeTabId) {
                    binding.viewPager.currentItem = index
                }
            }
        } else {
            binding.tabLayout.hide()
        }

        // Show BottomSheet if pending vouchers are available
        if (data.pendingVoucherDeeplink.isNullOrEmpty()) return
        mayNavigate {
            val deeplinkUri = Uri.parse(data.pendingVoucherDeeplink)
            if (navController.graph.hasDeepLink(deeplinkUri)) {
                navController.navigate(deeplinkUri)
            }
        }
    }
}
