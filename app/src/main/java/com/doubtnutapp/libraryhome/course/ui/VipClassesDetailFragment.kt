package com.doubtnutapp.libraryhome.course.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.FragmentVipClassesDetailBinding
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.libraryhome.course.viewmodel.CoursesViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class VipClassesDetailFragment :
    BaseBindingFragment<CoursesViewModel, FragmentVipClassesDetailBinding>(),
    ActionPerformer {

    companion object {
        private const val TAG = "VipClassesDetailFragment"
        fun newInstance(): VipClassesDetailFragment = VipClassesDetailFragment()
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var adapter: WidgetLayoutAdapter

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpRecyclerView()
        setUpObserver()
        viewModel.publishEvent(
            AnalyticsEvent(EventConstants.COURSE_PAGE_VIEW, hashMapOf(), ignoreSnowplow = true),
            false
        )
        mBinding?.ivBack?.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(requireActivity(), this)
        mBinding?.rvWidgets?.layoutManager = LinearLayoutManager(requireActivity())
        mBinding?.rvWidgets?.adapter = adapter
        fetchList()
    }

    private fun fetchList() {
        viewModel.getVmcDetail()
    }

    override fun performAction(action: Any) {
    }

    private fun setUpObserver() {
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
    }

    private fun unAuthorizeUserError() {

    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgress(state: Boolean) {
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun onWidgetListFetched(list: List<WidgetEntityModel<*, *>>) {
        adapter.setWidgets(list)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVipClassesDetailBinding {
        return FragmentVipClassesDetailBinding.inflate(layoutInflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): CoursesViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }

}