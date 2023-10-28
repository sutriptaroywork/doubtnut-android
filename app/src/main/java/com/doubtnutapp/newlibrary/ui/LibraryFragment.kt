package com.doubtnutapp.newlibrary.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.R
import com.doubtnutapp.base.LibraryWidgetClick
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentLibraryTabBinding
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.newlibrary.viewmodel.LibraryViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgets.itemdecorator.SimpleDividerItemDecoration
import com.uxcam.UXCam

class LibraryFragment : BaseBindingFragment<LibraryViewModel, FragmentLibraryTabBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "LibraryFragment"
        private const val UX_CAM_EVENT_NAME = "library_screen_shown"
        fun newInstance() = LibraryFragment()
    }

    private var viewTrackingBus: ViewTrackingBus? = null

    private val widgetListAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = TAG
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLibraryTabBinding =
        FragmentLibraryTabBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): LibraryViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding?.apply {
            rvWidgets.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            rvWidgets.addItemDecoration(
                SimpleDividerItemDecoration(
                    R.color.light_grey,
                    0.5F
                )
            )
            rvWidgets.adapter = widgetListAdapter
        }
        registerViewTracking()
        viewModel.getLibraryWidgets()
        viewModel.sendEvent(EventConstants.SCREEN_VIEW, hashMapOf<String, Any>().apply {
            put(EventConstants.SCREEN_NAME, TAG)
        })
    }

    override fun onResume() {
        super.onResume()
        UXCam.tagScreenName(TAG)
        UXCam.logEvent(UX_CAM_EVENT_NAME)
    }

    private fun registerViewTracking() {
        viewTrackingBus = ViewTrackingBus({}, {})
        widgetListAdapter.registerViewTracking(viewTrackingBus!!)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.libraryWidgets.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Success -> {
                    widgetListAdapter.setWidgets(it.data)
                }
                is Outcome.ApiError -> {

                }
                is Outcome.BadRequest -> {

                }
                is Outcome.Failure -> {

                }
                is Outcome.Progress -> {
                    mBinding?.progressBar?.isVisible = it.loading
                }
            }
        }
    }

    override fun onDestroyView() {
        viewTrackingBus?.unsubscribe()
        super.onDestroyView()
    }

    override fun performAction(action: Any) {
        when (action) {
            is LibraryWidgetClick -> {
                val libraryExamBottomSheetFragment =
                    LibraryExamsBottomSheetFragment.newInstance(
                        id = action.id,
                        commaSeparatedTabIds = action.tabIds?.joinToString(","),
                        position = action.position
                    )
                libraryExamBottomSheetFragment.setUpChangeExamListener(object :
                    LibraryExamsBottomSheetFragment.UpdateExamListener {
                    override fun updateExamWidget(
                        position: Int,
                        examWidget: WidgetEntityModel<WidgetData, WidgetAction>
                    ) {
                        widgetListAdapter.removeWidgetAt(position)
                        widgetListAdapter.addWidgetToPosition(
                            widget = examWidget,
                            index = position,
                            checkSize = true
                        )
                    }
                })
                libraryExamBottomSheetFragment.show(
                    childFragmentManager,
                    LibraryExamsBottomSheetFragment.TAG
                )
            }
            else -> {}
        }
    }
}