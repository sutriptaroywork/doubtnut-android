package com.doubtnutapp.examcorner

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentExamCornerBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.examcorner.model.ApiExamCornerData
import com.doubtnutapp.examcorner.viewmodel.ExamCornerViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class ExamCornerFragment : BaseBindingFragment<ExamCornerViewModel, FragmentExamCornerBinding>() {

    companion object {
        const val TYPE = "type"
        const val TAG = "ExamCornerFragment"
        fun newInstance(type: String) =
            ExamCornerFragment().apply {
                arguments = Bundle().apply {
                    putString(TYPE, type)
                }
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var isViewEventSent = false

    private lateinit var adapter: WidgetLayoutAdapter

    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentExamCornerBinding =
        FragmentExamCornerBinding.inflate(layoutInflater)

    override fun provideViewModel(): ExamCornerViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {

        adapter = WidgetLayoutAdapter(requireContext())
        binding.rvExamCorner.layoutManager = LinearLayoutManager(requireContext())
        binding.rvExamCorner.adapter = adapter
        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
            put(EventConstants.EVENT_NAME_TAB, filterType)
        }
    }

    private val filterType: String
        get() = arguments?.getString(TYPE, "").orEmpty()

    private fun initiateRecyclerListenerAndFetchInitialData() {
        adapter.clearData()
        val startPage = 1
        binding.rvExamCorner.clearOnScrollListeners()
        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvExamCorner.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    context?.run {
                        fetchList(currentPage)
                    }
                }
            }.also {
                it.setStartPage(startPage)
            }
        binding.rvExamCorner.addOnScrollListener(infiniteScrollListener)
        fetchList(startPage)
    }

    override fun onResume() {
        super.onResume()
        handlePageViewEvent()
        initiateRecyclerListenerAndFetchInitialData()
    }

    @Synchronized
    private fun handlePageViewEvent() {
        if (!isViewEventSent) {
            isViewEventSent = true
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.EXAM_CORNER_FRAGMENT_VIEW,
                    hashMapOf<String, Any>().apply {
                        putAll(viewModel.extraParams)
                    },
                    ignoreSnowplow = true
                )
            )
        }
    }

    private fun fetchList(page: Int) {
        viewModel.fetchExamCornerData(filterType, page)
    }

    override fun setupObservers() {
        viewModel.examCornerLiveData.observeK(
            viewLifecycleOwner,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onWidgetListFetched(data: ApiExamCornerData) {
        if (data.widgets.isNullOrEmpty()) {
            infiniteScrollListener.isLastPageReached = true
        } else {
            adapter.addWidgets(data.widgets)
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let { currentContext ->
            if (NetworkUtils.isConnected(currentContext)) {
                toast(getString(R.string.somethingWentWrong))
            } else {
                toast(getString(R.string.string_noInternetConnection))
            }
        }
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        binding.progressBar.setVisibleState(state)
    }
}
