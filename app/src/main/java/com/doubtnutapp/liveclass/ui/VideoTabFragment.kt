package com.doubtnutapp.liveclass.ui

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
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.ActivateVipTrial
import com.doubtnutapp.base.OnContentFilterSelect
import com.doubtnutapp.base.RefreshUI
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.Widgets
import com.doubtnutapp.databinding.FragmentVideoTabBinding
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.libraryhome.coursev3.ui.CourseFragment
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseViewModelV3
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class VideoTabFragment : BaseBindingFragment<CourseViewModelV3, FragmentVideoTabBinding>(),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var widgets: List<WidgetEntityModel<*, *>>
    private var id = ""
    private var qid: String? = ""
    private var assortmentId = ""
    private var contentType: String? = null
    private lateinit var adapter: WidgetLayoutAdapter
    private var isViewEventSent = false

    //to refresh UI when returns from video playback
    private var refreshUI: Boolean = false

    companion object {
        private const val ID = "id"
        const val TAG = "VideoTabFragment"
        private const val ASSORTMENT_ID = "assortment_id"
        private const val QID = "qid"
        fun newInstance(id: String?, qid: String?): VideoTabFragment {
            return VideoTabFragment().apply {
                arguments = bundleOf(
                    ID to id,
                    ASSORTMENT_ID to assortmentId,
                    QID to qid.orEmpty()
                )
            }
        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentVideoTabBinding =
        FragmentVideoTabBinding.inflate(layoutInflater)

    override fun provideViewModel(): CourseViewModelV3 =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            id = it.getString(ID, "")
            assortmentId = it.getString(ASSORTMENT_ID, "")
            qid = it.getString(QID, "")
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
                AnalyticsEvent(
                    TAG + EventConstants.PAGE_VIEW,
                    hashMapOf(
                        EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME to TAG,
                        EventConstants.EVENT_SCREEN_PREFIX + EventConstants.EVENT_NAME_TAB to id,
                        EventConstants.EVENT_SCREEN_PREFIX + EventConstants.ASSORTMENT_ID to assortmentId
                    ), ignoreBranch = false
                )
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
        binding.progressBar.setVisibleState(state)
    }

    private fun onWidgetListFetched(data: Widgets) {
        if (data.widgets.isEmpty()) {
        }
        widgets = data.widgets
        adapter.setWidgets(data.widgets)
    }

    private fun initUi() {
        adapter = WidgetLayoutAdapter(requireActivity(), activity as? LiveClassActivity)
        binding.rvWidgets.layoutManager = LinearLayoutManager(requireActivity())
        binding.rvWidgets.adapter = adapter
        fetchList()
    }

    private fun fetchList() {
        viewModel.getVideoTabsData(id, qid)
    }

    override fun performAction(action: Any) {
        if (action is RefreshUI) {
            refreshUI = true
        } else if (action is ActivateVipTrial) {
            (parentFragment as? CourseFragment)?.performAction(ActivateVipTrial(action.assortmentId))
        } else if (action is OnContentFilterSelect) {
            contentType = action.filter
        }
    }
}