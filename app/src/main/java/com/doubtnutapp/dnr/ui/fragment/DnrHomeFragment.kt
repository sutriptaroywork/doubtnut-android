package com.doubtnutapp.dnr.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*

import com.doubtnutapp.base.DnrTncClicked
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentDnrHomeBinding
import com.doubtnutapp.dnr.viewmodel.DnrHomeFragmentViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter

class DnrHomeFragment :
    BaseBindingFragment<DnrHomeFragmentViewModel, FragmentDnrHomeBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "DnrHomeFragment"
    }

    private val navController by findNavControllerLazy()

    private val widgetListAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = TAG
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentDnrHomeBinding =
        FragmentDnrHomeBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DnrHomeFragmentViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mBinding ?: return
        viewModel.sendEvent(EventConstants.DNR_HOME_PAGE_VISIT, ignoreSnowplow = true)
        binding.rvWidgets.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvWidgets.adapter = widgetListAdapter
        getDnrHomeData()
    }

    private fun getDnrHomeData() {
        viewModel.getDnrHomeData()
        binding.toolbar.ivBack.setOnClickListener {
            mayNavigate {
                navController.navigateUpOrFinish(requireActivity())
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.widgetListLiveData.observeEvent(viewLifecycleOwner, {
            mBinding ?: return@observeEvent
            when (it) {
                is Outcome.Progress -> {
                    binding.progressBar.setVisibleState(it.loading)
                }
                is Outcome.Success -> {
                    val response = it.data
                    val widgetList = response.widgets
                    widgetListAdapter.setWidgets(widgetList.orEmpty())

                    binding.toolbar.endLayout.isVisible =
                        response.toolbarData?.dnr.isNotNullAndNotEmpty()
                    binding.toolbar.tvTitle.text = response.toolbarData?.title
                    binding.toolbar.endLayout.setOnClickListener { v ->
                        if (it.data.toolbarData != null &&
                            it.data.toolbarData?.deeplink != null
                        ) {
                            val deeplinkUri = Uri.parse(it.data.toolbarData!!.deeplink)
                            if (navController.graph.hasDeepLink(deeplinkUri)) {
                                navController.navigate(deeplinkUri)
                            }
                        }
                    }
                }
                is Outcome.ApiError -> {
                    binding.progressBar.hide()
                    apiErrorToast(it.e)
                }
                is Outcome.BadRequest -> {
                    binding.progressBar.hide()
                }
                is Outcome.Failure -> {
                    binding.progressBar.hide()
                    apiErrorToast(it.e)
                }
            }
        })
    }

    override fun performAction(action: Any) {
        when (action) {
            is DnrTncClicked -> {
                navController.navigate(
                    R.id.action_dnr_tnc_fragment,
                    bundleOf(
                        DnrTncBottomSheet.TNC_DIALOG_DATA to action.data
                    )
                )
            }
        }
    }
}
