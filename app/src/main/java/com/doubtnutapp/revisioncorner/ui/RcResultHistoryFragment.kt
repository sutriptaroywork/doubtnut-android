package com.doubtnutapp.revisioncorner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.revisioncorner.ResultInfo
import com.doubtnutapp.databinding.FragmentRcResultHistoryBinding
import com.doubtnutapp.hide
import com.doubtnutapp.revisioncorner.ui.adapter.RcResultHistoryViewPagerAdapter
import com.doubtnutapp.revisioncorner.viewmodel.RcResultViewModel
import com.google.android.material.tabs.TabLayoutMediator
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RcResultHistoryFragment : Fragment(R.layout.fragment_rc_result_history) {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val viewModel by viewModels<RcResultViewModel> { viewModelFactory }

    private val binding by viewBinding(FragmentRcResultHistoryBinding::bind)
    private val args by navArgs<RcResultHistoryFragmentArgs>()
    private val navController by findNavControllerLazy()

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
        viewModel.getResultInfoData(args.widgetId)
    }

    private fun setupObservers() {
        viewModel.resultLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBarLoader.isVisible = it.loading
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                is Outcome.Success -> {
                    setupUi(it.data)
                }
            }
        }
    }

    private fun setupUi(data: ResultInfo) {

        binding.tvTitle.text = data.title
        binding.ivBack.setOnClickListener {
            navController.navigateUp()
        }
        setViewPager(data)
    }

    private fun setViewPager(data: ResultInfo) {
        val tabs = data.tabs.orEmpty()
        val activeTabId = args.activeTabId

        val adapter = RcResultHistoryViewPagerAdapter(this, tabs, args.widgetId, data.title.orEmpty())
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = tabs[position].title
        }.attach()

        if (data.hasTabs) {
            tabs.forEachIndexed { index, tabData ->
                if (tabData.id == activeTabId) {
                    binding.viewPager.currentItem = index
                }
            }
        } else {
            binding.tabLayout.hide()
        }
    }
}