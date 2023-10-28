package com.doubtnutapp.libraryhome.coursev3.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.ActivateVipTrial
import com.doubtnutapp.base.OnContentFilterSelect
import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.Widgets
import com.doubtnutapp.databinding.FragmentCourseV3Binding
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class CourseTabFragment : BaseBindingFragment<CourseViewModelV3, FragmentCourseV3Binding>(),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var widgets: List<WidgetEntityModel<*, *>>
    private var id = ""
    private var source: String? = ""
    private var assortmentId = ""
    private var contentType : String? = null
    private lateinit var adapter: WidgetLayoutAdapter
    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener
    private var isViewEventSent = false

    //to refresh UI when returns from video playback
    private var refreshUI: Boolean = false

    companion object {
        private const val ID = "id"
        const val TAG = "CourseTabFragment"
        private const val ASSORTMENT_ID = "assortment_id"
        fun newInstance(id: String?, assortmentId: String?, source: String?): CourseTabFragment {
            return CourseTabFragment().apply {
                arguments = bundleOf(
                        ID to id,
                        ASSORTMENT_ID to assortmentId,
                        Constants.SOURCE to source.orEmpty()
                )
            }
        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentCourseV3Binding =
        FragmentCourseV3Binding.inflate(layoutInflater)

    override fun provideViewModel(): CourseViewModelV3 =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            id = it.getString(ID, "")
            assortmentId = it.getString(ASSORTMENT_ID, "")
            source = it.getString(Constants.SOURCE, "")
        }
        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.EVENT_NAME_TAB, id)
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID, assortmentId)
        }
        initUi()
    }

    override fun onResume() {
        super.onResume()
        handlePageViewEvent()
        if (refreshUI) {
            initUi()
            refreshUI = false
        }
    }

    @Synchronized
    private fun handlePageViewEvent() {
        if (!isViewEventSent) {
            isViewEventSent = true
            analyticsPublisher.publishEvent(
                    AnalyticsEvent(TAG + EventConstants.PAGE_VIEW,
                            hashMapOf(
                                    EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME to TAG,
                                    EventConstants.EVENT_SCREEN_PREFIX + EventConstants.EVENT_NAME_TAB to id,
                                    EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to assortmentId
                            ), ignoreBranch = false)
            )
        }
    }

    override fun setupObservers() {
        viewModel.widgetsLiveData.observeK(
                viewLifecycleOwner,
                this::onWidgetListFetched,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgress
        )
    }

    private fun ioExceptionHandler() {
        toast(getString(R.string.api_error))
    }

    private fun unAuthorizeUserError() {
        toast(getString(R.string.api_error))
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        binding.progressBar.setVisibleState(state)
    }

    private fun onWidgetListFetched(data: Widgets) {
        if (data.widgets.isEmpty()) {
            infiniteScrollListener.isLastPageReached = true
        }
        widgets = data.widgets
        if (infiniteScrollListener.currentPage == 1) {
            adapter.setWidgets(data.widgets)
        } else {
            adapter.addWidgets(data.widgets)
        }
    }

    private fun initUi() {
        adapter = WidgetLayoutAdapter(requireActivity(), this, source.orEmpty())
        binding.rvWidgets.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvWidgets.adapter = adapter
        initiateRecyclerListenerAndFetchInitialData()
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {

        val startPage = 1
        binding.rvWidgets.clearOnScrollListeners()
        infiniteScrollListener = object : TagsEndlessRecyclerOnScrollListener(binding.rvWidgets.layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                context?.run {
                    fetchList(currentPage)
                }
            }
        }.also {
            it.setStartPage(startPage)
        }
        binding.rvWidgets.addOnScrollListener(infiniteScrollListener)
        fetchList(startPage)
    }

    private fun fetchList(pageNumber: Int) {
        viewModel.getCourseTabsData(pageNumber, assortmentId, id, contentType)
    }

    override fun performAction(action: Any) {
        if (action is RefreshUI) {
            refreshUI = true
        } else if (action is ActivateVipTrial) {
            (parentFragment as? CourseFragment)?.performAction(ActivateVipTrial(action.assortmentId))
        } else if (action is OnContentFilterSelect) {
            contentType = action.filter
            initiateRecyclerListenerAndFetchInitialData()
        }
    }
}