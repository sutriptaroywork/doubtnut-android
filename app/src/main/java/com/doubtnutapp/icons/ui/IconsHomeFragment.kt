package com.doubtnutapp.icons.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentIconsHomeBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.hide
import com.doubtnutapp.icons.ui.viewmodel.IconsActivityVM
import com.doubtnutapp.icons.ui.viewmodel.IconsHomeFragmentVM
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.extension.observeNonNull
import javax.inject.Inject

class IconsHomeFragment :
    BaseBindingFragment<IconsHomeFragmentVM, FragmentIconsHomeBinding>() {

    private lateinit var activityViewModel: IconsActivityVM

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private val args by navArgs<IconsHomeFragmentArgs>()
    private val type by lazy { args.type.orEmpty() }
    private val id by lazy { args.id.orEmpty() }
    private val title by lazy { args.title.orEmpty() }

    private var page = 0
    private var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentIconsHomeBinding {
        return FragmentIconsHomeBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): IconsHomeFragmentVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        activityViewModel = activityViewModelProvider(viewModelFactory)

        if (title.isEmpty().not()) {
            activityViewModel.updateTitle(title)
        }

        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvMain.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    page++
                    getCategories()
                }
            }
        binding.rvMain.addOnScrollListener(infiniteScrollListener ?: return)
        getCategories()
    }

    fun getCategories() {
        viewModel.getCategories(
            type = type,
            id = id,
            page = page
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.iconsDetailResponse.observeNonNull(viewLifecycleOwner) { outcome ->

            mBinding ?: return@observeNonNull

            when (outcome) {
                is Outcome.Progress -> {
                    binding.progressBar.setVisibleState(outcome.loading)
                }
                is Outcome.Success -> {
                    val response = outcome.data

                    if (outcome.data.widgets.isNullOrEmpty()) {
                        infiniteScrollListener?.isLastPageReached = true
                    }

                    if (response.headerTitle.isNullOrEmpty().not()) {
                        activityViewModel.updateTitle(response.headerTitle.orEmpty())
                    }

                    if (response.deeplink.isNullOrEmpty().not()) {
                        deeplinkAction.performAction(requireContext(), response.deeplink)
                        if (response.finishActivity == true) {
                            requireActivity().finish()
                        }
                        return@observeNonNull
                    }

                    binding.rvMain.setWidgets(outcome.data.widgets)

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            "${EVENT_TAG}_${EventConstants.VIEWED}",
                            hashMapOf<String, Any>(
                                EventConstants.WIDGET to type,
                                EventConstants.ID to id,
                                EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                                EventConstants.PAGE to page,
                            ).apply {
                                putAll(outcome.data.extraParams.orEmpty())
                            }
                        )
                    )
                }
                is Outcome.ApiError -> {
                    binding.progressBar.hide()
                    apiErrorToast(outcome.e)
                }
                is Outcome.BadRequest -> {
                    binding.progressBar.hide()
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(childFragmentManager, "BadRequestDialog")
                }
                is Outcome.Failure -> {
                    binding.progressBar.hide()
                    apiErrorToast(outcome.e)
                }
            }
        }
    }

    companion object {
        private const val TAG = "IconsHomeFragment"
        private const val EVENT_TAG = "icons_home_fragment"
    }
}