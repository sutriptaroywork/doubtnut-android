package com.doubtnutapp.revisioncorner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.RcResultHistoryWatchSolutionClick
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.databinding.FragmentRcResultHistoryListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.revisioncorner.ui.adapter.ResultHistoryAdapter
import com.doubtnutapp.revisioncorner.viewmodel.RcResultListViewModel
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RcResultHistoryListFragment : Fragment(R.layout.fragment_rc_result_history_list),
    ActionPerformer2 {

    companion object {

        const val REVISION_CORNER_SHORT_TEST = "revision_corner_short_test"
        private const val TAB_ID = "tab_id"
        private const val WIDGET_ID = "widget_id"
        private const val WIDGET_TEXT = "widget_text"

        fun newInstance(widgetId: String, tabId: Int, widgetTitle: String) = RcResultHistoryListFragment().apply {
            arguments = Bundle().apply {
                putString(WIDGET_ID, widgetId)
                putInt(TAB_ID, tabId)
                putString(WIDGET_TEXT, widgetTitle)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val viewModel by viewModels<RcResultListViewModel> { viewModelFactory }
    private val binding by viewBinding(FragmentRcResultHistoryListBinding::bind)

    private val resultAdapter by lazy { ResultHistoryAdapter(this) }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val tabId = arguments?.getInt(TAB_ID) ?: -1
        val widgetId = arguments?.getString(WIDGET_ID) ?: "-1"

        viewModel.setListId(widgetId, tabId)

        setupObservers()
        binding.rvResult.adapter = resultAdapter
    }

    private fun setupObservers() {
        viewModel.resultListLiveData.observe(viewLifecycleOwner) {
            resultAdapter.submitList(it)
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is RcResultHistoryWatchSolutionClick -> {
                deeplinkAction.performAction(requireContext(), action.deeplink)
                viewModel.sendEvent(EventConstants.RC_WATCH_SOLUTION_CLICK, hashMapOf(
                    EventConstants.TYPE to arguments?.getString(WIDGET_TEXT).orEmpty()
                ), ignoreSnowplow = true)
            }
        }
    }
}