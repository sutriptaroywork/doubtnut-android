package com.doubtnutapp.dnr.ui.fragment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.DnrOpenWebUrl
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentDnrWidgetListBinding
import com.doubtnutapp.dnr.model.DnrNoWidgetContainer
import com.doubtnutapp.dnr.viewmodel.DnrWidgetListViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter

class DnrWidgetListFragment :
    BaseBindingFragment<DnrWidgetListViewModel, FragmentDnrWidgetListBinding>(),
    ActionPerformer {

    companion object {
        private const val TAG = "DnrWidgetListFragment"
        const val INITIAL_PAGE = "0"
        const val VISIBLE_THRESHOLD = 10
        fun newInstance(args: Bundle): DnrWidgetListFragment {
            return DnrWidgetListFragment().apply {
                arguments = args
            }
        }
    }

    private val args by navArgs<DnrWidgetListFragmentArgs>()
    private val screen by lazy { args.screen }
    private val navController by findNavControllerLazy()
    private var myWidgetListPage = "0"
    private var isLastPageReached = false
    private var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? = null

    private val widgetListAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = screen
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDnrWidgetListBinding =
        FragmentDnrWidgetListBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DnrWidgetListViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setupUi()
        fetchNextWidgets()
        viewModel.sendEvent(
            EventConstants.DNR_WIDGET_LIST_OPEN,
            hashMapOf(
                EventConstants.SOURCE to screen
            ),
            ignoreSnowplow = true
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.widgetListLiveData.observeEvent(this, {
            mBinding ?: return@observeEvent
            when (it) {
                is Outcome.Progress -> {
                    updateProgressBarState(it.loading)
                }
                is Outcome.Success -> {
                    val response = it.data
                    infiniteScrollListener?.setDataLoading(false)
                    val widgetList = response.widgets
                    when (myWidgetListPage) {
                        INITIAL_PAGE -> {
                            if (widgetList.isNullOrEmpty()) {
                                setUpUiForNoResult(it.data.noWidgetContainer)
                            } else {
                                widgetListAdapter.setWidgets(widgetList.orEmpty())
                            }
                        }
                        else -> {
                            widgetListAdapter.addWidgets(widgetList.orEmpty())
                        }
                    }
                    myWidgetListPage = response.page ?: ""
                    isLastPageReached = response.isReachedEnd ?: true
                    infiniteScrollListener?.isLastPageReached = response.isReachedEnd ?: true

                    binding.toolbar.endLayout.isVisible =
                        response.toolbarData?.dnr.isNotNullAndNotEmpty()
                    binding.toolbar.tvTitle.text = response.toolbarData?.title
                    binding.toolbar.endLayout.setOnClickListener { v ->
                        if (it.data.toolbarData != null &&
                            it.data.toolbarData?.deeplink != null
                        ) {
                            mayNavigate {
                                val deeplinkUri = Uri.parse(it.data.toolbarData!!.deeplink)
                                if (navController.graph.hasDeepLink(deeplinkUri)) {
                                    navController.navigate(deeplinkUri)
                                }
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

        viewModel.metaDataLiveData.observe(this) {
            mBinding ?: return@observe
            mBinding?.toolbar?.apply {
                tvTitle.text = it.toolbar?.title
                endLayout.hide()
            }
        }
    }

    private fun setupUi() {
        mBinding ?: return
        binding.toolbar.root.isVisible = args.isToolbarVisible
        binding.rvWidgets.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.rvWidgets.adapter = widgetListAdapter

        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(mBinding?.rvWidgets?.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    fetchNextWidgets()
                }
            }
        infiniteScrollListener?.setStartPage(1)
        infiniteScrollListener?.setVisibleThreshold(VISIBLE_THRESHOLD)
        binding.rvWidgets.addOnScrollListener(infiniteScrollListener!!)

        mBinding?.toolbar?.ivBack?.setOnClickListener {
            mayNavigate {
                navController.navigateUp()
            }
        }
    }

    private fun fetchNextWidgets() {
        if (isLastPageReached.not()) {
            viewModel.getRequiredWidgetData(myWidgetListPage, screen)
        }
    }

    private fun setUpUiForNoResult(noWidgetContainer: DnrNoWidgetContainer?) {
        mBinding ?: return
        mBinding?.noResultContainer?.apply {
            root.show()
            noResultImageView.apply {
                val image = noWidgetContainer?.image
                isVisible = image.isNotNullAndNotEmpty()
                loadImage(image)
            }

            noResultTvTitle.apply {
                val title = noWidgetContainer?.title
                isVisible = title.isNotNullAndNotEmpty()
                text = title
            }

            noResultTvSubTitle.apply {
                val subTitle = noWidgetContainer?.subtitle
                isVisible = subTitle.isNotNullAndNotEmpty()
                text = subTitle
            }
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener?.setDataLoading(state)
        mBinding?.progressBar?.setVisibleState(state)
    }

    override fun performAction(action: Any) {
        when (action) {
            is DnrOpenWebUrl -> {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse(action.url)
                try {
                    startActivity(i)
                } catch (exception: ActivityNotFoundException) {
                    mayNavigate {
                        navController.navigateUp()
                    }
                }
            }
            else -> {
            }
        }
    }
}
