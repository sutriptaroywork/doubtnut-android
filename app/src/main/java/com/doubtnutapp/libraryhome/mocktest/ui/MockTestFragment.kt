package com.doubtnutapp.libraryhome.mocktest.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.isNotRunning
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.TextWidgetData
import com.doubtnutapp.data.remote.models.TextWidgetModel
import com.doubtnutapp.data.remote.models.mocktest.MockTestCourseData
import com.doubtnutapp.databinding.FragmentMockTestLibraryBinding
import com.doubtnutapp.libraryhome.mocktest.viewmodel.MockTestViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.mockTest.event.RefreshMockTestList
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import io.reactivex.disposables.Disposable
import javax.inject.Inject

class MockTestFragment : BaseBindingFragment<MockTestViewModel, FragmentMockTestLibraryBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "MockTestFragment"
        fun newInstance() = MockTestFragment()
    }

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var adapter: WidgetLayoutAdapter

    private var appStateObserver: Disposable? = null

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            if (isViewModelInitialized) {
                viewModel.publishLibraryTabSelectedEvent(TAG)
            }

           
        }
    }

    private fun startShimmer() {
        binding.shimmerFrameLayout.startShimmer()
        binding.shimmerFrameLayout.show()
    }

    private fun stopShimmer() {
        binding.shimmerFrameLayout.stopShimmer()
        binding.shimmerFrameLayout.hide()
    }

    override fun performAction(action: Any) {
        viewModel.handleAction(action)
    }

    private fun setUpObserver() {
        viewModel.mockTestLiveData.observeK(
                this,
                this::onSuccess,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgress
        )
        appStateObserver = DoubtnutApp.INSTANCE
                .bus()?.toObservable()?.subscribe {
                    when (it) {
                        is RefreshMockTestList -> {
                            startShimmer()
                            getLibraryPlaylist()
                        }
                    }
                }
    }

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(requireContext())
        binding.rvMockTest.adapter = adapter
    }

    private fun getLibraryPlaylist() {
        viewModel.getMockTestData()
    }

    private fun onSuccess(mockTestCourse: MockTestCourseData?) {
        if (activity.isNotRunning()) return
        if (mockTestCourse?.widgets.isNullOrEmpty()) {
            val widgets = ArrayList<WidgetEntityModel<*, *>>()
            widgets.add(
                TextWidgetModel().apply {
                    _type = WidgetTypes.TYPE_TEXT_WIDGET
                    _data = TextWidgetData(
                        title = requireContext().getString(R.string.no_data),
                        htmlTitle = null,
                        textColor = null,
                        textSize = null,
                        backgroundColor = null,
                        isBold = null,
                        linkify = null,
                        gravity = "center",
                        layoutPadding = null,
                        deeplink = null,
                        forceHideRightIcon = null,
                        startTimeInMillis = null
                    )
                }
            )
            adapter.setWidgets(widgets)
        } else {
            adapter.setWidgets(mockTestCourse?.widgets.orEmpty())
        }
    }

    private fun unAuthorizeUserError() {
        if (activity.isNotRunning()) return
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireFragmentManager(), "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        if (activity.isNotRunning()) return
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (activity.isNotRunning()) return
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun updateProgress(state: Boolean) {
        if (activity.isNotRunning()) return
        binding.pbPagination.setVisibleState(state)
        if (state.not()) {
            stopShimmer()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        appStateObserver?.dispose()
    }

    override fun provideViewBinding(inflater: LayoutInflater, container: ViewGroup?):
            FragmentMockTestLibraryBinding {
        return FragmentMockTestLibraryBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MockTestViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        startShimmer()
        setUpObserver()
        setUpRecyclerView()
        getLibraryPlaylist()
    }

}