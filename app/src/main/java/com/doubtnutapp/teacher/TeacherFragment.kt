package com.doubtnutapp.teacher

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentTeacherBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.teacher.model.TeacherListData
import com.doubtnutapp.teacher.viewmodel.TeacherViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class TeacherFragment : BaseBindingFragment<TeacherViewModel, FragmentTeacherBinding>() {

    companion object {
        const val TAG = "TeacherFragment"
        fun newInstance() = TeacherFragment()
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
    ): FragmentTeacherBinding =
        FragmentTeacherBinding.inflate(layoutInflater)

    override fun provideViewModel(): TeacherViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        adapter = WidgetLayoutAdapter(requireContext())
        binding.rvWidgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWidgets.adapter = adapter
        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
        }
        initiateRecyclerListenerAndFetchInitialData()
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        adapter.clearData()
        val startPage = 1
        binding.rvWidgets.clearOnScrollListeners()
        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvWidgets.layoutManager) {
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

    override fun onResume() {
        super.onResume()
        handlePageViewEvent()
    }

    @Synchronized
    private fun handlePageViewEvent() {
        if (!isViewEventSent) {
            isViewEventSent = true
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.TEACHER_FRAGMENT_PAGE_VIEW,
                    hashMapOf<String, Any>().apply {
                        putAll(viewModel.extraParams)
                    })
            )
        }
    }

    private fun fetchList(page: Int) {
        viewModel.fetchTeacherList(page)
    }

    override fun setupObservers() {
        viewModel.teacherLiveData.observeK(
            viewLifecycleOwner,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onWidgetListFetched(data: TeacherListData) {
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