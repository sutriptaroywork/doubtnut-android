package com.doubtnutapp.revisioncorner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*

import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.RcStatsViewAllClick
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.revisioncorner.PerformanceReport
import com.doubtnutapp.databinding.FragmentRcStatsBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.revisioncorner.ui.adapter.StatsAdapter
import com.doubtnutapp.revisioncorner.viewmodel.RcStatsViewModel
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

/**
 * Created by devansh on 16/08/21.
 */

class RcStatsFragment : Fragment(R.layout.fragment_rc_stats), ActionPerformer2 {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val binding by viewBinding(FragmentRcStatsBinding::bind)
    private val viewModel by viewModels<RcStatsViewModel> { viewModelFactory }
    private val navController by findNavControllerLazy()

    private val adapter by lazy { StatsAdapter(this) }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
        viewModel.getPerformanceReport()

        binding.ivBack.setOnClickListener {
            navController.navigateUp()
        }
    }

    private fun setupObservers() {
        viewModel.performanceReport.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.isVisible = it.loading
                }
                is Outcome.Failure -> {
                    apiErrorToast(it.e)
                }
                is Outcome.Success -> {
                    updateUi(it.data)
                }
            }
        }
    }

    private fun updateUi(data: PerformanceReport) {
        with(binding) {
            tvTitle.text = data.title
            ivIcon.loadImage(data.icon)

            if (data.stats.isNullOrEmpty() && data.unavailableStatsData != null) {
                val unavailableStatsData = data.unavailableStatsData
                nestedScrollView.hide()
                childFragmentManager.commit {
                    replace(
                        R.id.unavailableStatsFragment,
                        RcUnavailableStatsFragment.newInstance(unavailableStatsData),
                    )
                }
            } else {
                childFragmentManager.commit {
                    childFragmentManager.findFragmentById(R.id.unavailableFragment)?.let {
                        remove(it)
                    }
                }
                unavailableStatsFragment.hide()
                nestedScrollView.show()
                rvStats.adapter = adapter
                adapter.updateList(data.stats)
            }
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is RcStatsViewAllClick -> {
                deeplinkAction.performAction(requireContext(), action.deeplink)
                viewModel.sendEvent(
                    EventConstants.RC_VIEW_ALL_PREVIOUS_SOLUTIONS, hashMapOf(
                        EventConstants.TYPE to action.title.orEmpty()
                    ), ignoreSnowplow = true
                )
            }
        }
    }
}