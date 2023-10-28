package com.doubtnutapp.revisioncorner.ui

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher

import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.RcTestListItemClicked
import com.doubtnutapp.base.TwoTextsVerticalTabWidgetTabChanged
import com.doubtnutapp.base.ViewModelFactory
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.data.remote.models.revisioncorner.TestMetaData
import com.doubtnutapp.databinding.FragmentRcTestListBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.navigateUpOrFinish
import com.doubtnutapp.revisioncorner.viewmodel.RcTestListViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class RcTestListFragment : Fragment(R.layout.fragment_rc_test_list),
    ActionPerformer2 {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private val viewModel by viewModels<RcTestListViewModel> { viewModelFactory }
    private val binding by viewBinding(FragmentRcTestListBinding::bind)
    private val navArgs by navArgs<RcTestListFragmentArgs>()
    private val navController by findNavControllerLazy()
    private var isTabChanged: Boolean = false
    private var tabSelected: String = ""
    private var page = INITIAL_PAGE
    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    companion object {
        const val PAGE_SIZE = 10
        const val INITIAL_PAGE = 0
        private const val TAG = "RcTestListFragment"
    }

    private val testAdapter by lazy { WidgetLayoutAdapter(requireContext(), this, TAG) }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.setExamType(navArgs.examType)
        setupObservers()

        binding.apply {
            rvResult.adapter = testAdapter
            rvResult.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            infiniteScrollListener =
                object : TagsEndlessRecyclerOnScrollListener(rvResult.layoutManager) {
                    override fun onLoadMore(currentPage: Int) {
                        page = currentPage
                        fetchMoreDataList()
                    }
                }.also {
                    it.setStartPage(INITIAL_PAGE)
                }
            binding.rvResult.addOnScrollListener(infiniteScrollListener)
        }

        fetchMoreDataList()
    }

    private fun setupObservers() {
        viewModel.testMetaDataLiveData.observe(viewLifecycleOwner) {
            setupUi(it)
        }

        viewModel.testListLiveData.observe(viewLifecycleOwner) {

            if (it.isNullOrEmpty()) {
                infiniteScrollListener.isLastPageReached = true
                return@observe
            }

            if (isTabChanged) {
                isTabChanged = false
                testAdapter.setWidgets(it)
            } else {
                testAdapter.addWidgets(it)
            }
        }
    }

    private fun setupUi(testMetaData: TestMetaData?) {
        with(binding) {
            tvTitle.text = testMetaData?.title.orEmpty()
            ivBack.setOnClickListener {
                navController.navigateUpOrFinish(activity)
            }
        }
    }

    private fun fetchMoreDataList() {
        viewModel.getTestListData(page, tabSelected)
    }

    override fun performAction(action: Any) {
        when (action) {
            is RcTestListItemClicked -> {
                deeplinkAction.performAction(requireContext(), action.deeplink)
            }
            is TwoTextsVerticalTabWidgetTabChanged -> {
                isTabChanged = true
                tabSelected = action.tabSelected
                page = INITIAL_PAGE
                fetchMoreDataList()
            }
        }
    }
}