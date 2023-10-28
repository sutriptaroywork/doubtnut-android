package com.doubtnutapp.studygroup.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.common.data.entity.BottomCta
import com.doubtnut.core.common.data.entity.NoWidgetContainer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*

import com.doubtnutapp.base.StudyGroupClickToShare
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.getNavigationResult
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentSgMyGroupsBinding
import com.doubtnutapp.studygroup.model.SgSearch
import com.doubtnutapp.studygroup.model.SgToolbarData
import com.doubtnutapp.studygroup.viewmodel.SgHomeViewModel
import com.doubtnutapp.studygroup.viewmodel.StudyGroupListViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgets.StudyGroupSearchView
import com.doubtnutapp.widgets.itemdecorator.SimpleDividerItemDecoration


class SgMyGroupsFragment : BaseBindingFragment<SgHomeViewModel, FragmentSgMyGroupsBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "SgMyGroupsFragment"
        const val INITIAL_PAGE = 0
        const val MY_GROUP = "my_groups"
        const val SHARE = "share"
        private const val PAGE_TYPE = "page_type"

        fun newInstance(pageType: String) = SgMyGroupsFragment().apply {
            arguments = Bundle().apply {
                putString(PAGE_TYPE, pageType)
            }
        }
    }

    private val infiniteScrollListener: TagsEndlessRecyclerOnScrollListener by lazy {
        object : TagsEndlessRecyclerOnScrollListener(mBinding?.rvMyGroups?.layoutManager) {
            override fun onLoadMore(currentPage: Int) {
                fetchMoreGroupList()
            }
        }
    }

    private val widgetListAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = arguments?.getString(PAGE_TYPE) ?: MY_GROUP
        )
    }

    private var myWidgetListPage = 0
    private var searchWidgetListPage = 0
    private var isLastPageReached = false
    private var isSearchInProgress = false
    private var currentQuery = ""
    private var canRefresh = false
    private val pageType: String by lazy { arguments?.getString(PAGE_TYPE) ?: MY_GROUP }

    private var toolbarListener: OnSetUpToolbar? = null

    private val navController by findNavControllerLazy()

    private val sgShareListViewModel: StudyGroupListViewModel by lazy {
        activityViewModelProvider(
            viewModelFactory
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentSgMyGroupsBinding =
        FragmentSgMyGroupsBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): SgHomeViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpClickListeners()
        setupUi()
        if (canRefresh) {
            resetVariablesToDefault()
        }
        fetchMoreGroupList()
    }

    override fun onStop() {
        canRefresh = true
        super.onStop()
    }

    private fun setupUi() {
        when (viewModel.getJoinNewGroupFlaggerVariant()) {
            3 -> { // As fab and toolbar
                mBinding?.fabScrollUp?.show()
            }
            else -> { // As new tab or first item
                mBinding?.fabScrollUp?.hide()
            }
        }

        binding.rvMyGroups.addItemDecoration(
            SimpleDividerItemDecoration(
                R.color.light_grey,
                0.5F
            )
        )
        mBinding?.rvMyGroups?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        mBinding?.rvMyGroups?.adapter = widgetListAdapter

        infiniteScrollListener.setStartPage(1)
        infiniteScrollListener.setVisibleThreshold(SgChatFragment.VISIBLE_THRESHOLD)
        mBinding?.rvMyGroups?.addOnScrollListener(infiniteScrollListener)

        // Get result for search queries
        mBinding?.etSearch?.setQueryListener(object : StudyGroupSearchView.QueryListener {
            override fun onQuery(query: String) {
                mBinding ?: return
                currentQuery = query
                searchWidgetListPage = 0
                if (query.isEmpty()) {
                    isSearchInProgress = false
                    mBinding?.apply {
                        rvMyGroups.show()
                        mBinding?.noResultContainer?.root?.hide()
                    }
                    val list = viewModel.getInitialWidgetList().toMutableList()
                    widgetListAdapter.setWidgets(list)
                } else {
                    viewModel.getSearchResultList(query, searchWidgetListPage)
                    viewModel.sendEvent(
                        EventConstants.SG_SEARCH_QUERY, hashMapOf(
                            Constants.SEARCH_QUERY to query,
                            Constants.SOURCE to Constants.SG_GROUP
                        )
                    )
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        resetVariablesToDefault()
    }

    private fun resetVariablesToDefault() {
        myWidgetListPage = 0
        searchWidgetListPage = 0
        isLastPageReached = false
        isSearchInProgress = false
        currentQuery = ""
        canRefresh = false
    }

    override fun setupObservers() {
        super.setupObservers()

        viewModel.sgWidgetListLiveData.observeEvent(this) {
            when (it) {
                is Outcome.Progress -> {
                    updateProgressBarState(it.loading)
                }
                is Outcome.Success -> {
                    val response = it.data
                    infiniteScrollListener.setDataLoading(false)
                    val widgetList = response.widgets
                    when (myWidgetListPage) {
                        INITIAL_PAGE -> {
                            binding.etSearch.isVisible = it.data.isSearchEnabled
                            binding.etSearch.setMinimumQueryTextLength(it.data.minSearchCharacters)
                            binding.rvMyGroups.isVisible = widgetList.isNullOrEmpty().not()
                            if (widgetList.isNullOrEmpty()) {
                                setUpUiForNoResult(it.data.noWidgetContainer)
                            } else {
                                mBinding?.noResultContainer?.root?.hide()
                            }
                            if (response.isMyGroupsAvailable.not() && response.noWidgetContainer != null) {
                                val data = response.noWidgetContainer
                                binding.apply {
                                    studyGroupIntro.show()
                                    ivStudyGroup.loadImage(data?.image)
                                    tvTitle.text = data?.title
                                    tvSubtitle.text = data?.subtitle
                                    tvJoinGroupTitle.isVisible =
                                        response.joinHeading.isNullOrEmpty().not()
                                    tvJoinGroupTitle.text = response.joinHeading
                                }
                            } else {
                                mBinding?.studyGroupIntro?.hide()
                                mBinding?.tvJoinGroupTitle?.hide()
                            }
                            widgetListAdapter.setWidgets(widgetList.orEmpty())
                        }
                        else -> {
                            if (isLastPageReached.not()) {
                                widgetListAdapter.addWidgets(widgetList.orEmpty())
                            }
                        }
                    }
                    myWidgetListPage = response.page
                    isLastPageReached = response.isReachedEnd
                    infiniteScrollListener.isLastPageReached = isLastPageReached
                }
                is Outcome.ApiError -> {
                    mBinding?.progressBar?.hide()
                    apiErrorToast(it.e)
                }
                is Outcome.BadRequest -> {
                    mBinding?.progressBar?.hide()
                }
                is Outcome.Failure -> {
                    mBinding?.progressBar?.hide()
                    apiErrorToast(it.e)
                }
            }
        }

        viewModel.sgWidgetSearchResultLiveData.observeEvent(this) {
            when (it) {
                is Outcome.Progress -> {
                    updateProgressBarState(it.loading)
                }
                is Outcome.Success -> {
                    isSearchInProgress = true
                    val response = it.data
                    val widgetList = response.widgets
                    infiniteScrollListener.setDataLoading(false)
                    when (searchWidgetListPage) {
                        INITIAL_PAGE -> {
                            if (widgetList.isNullOrEmpty()) {
                                setUpUiForNoResult(it.data.noWidgetContainer)
                            } else {
                                mBinding?.noResultContainer?.root?.hide()
                            }
                            widgetListAdapter.setWidgets(widgetList.orEmpty())
                        }
                        else -> {
                            widgetListAdapter.addWidgets(widgetList.orEmpty())
                        }
                    }
                    searchWidgetListPage = response.page
                    infiniteScrollListener.isLastPageReached = response.isReachedEnd
                }
                is Outcome.ApiError -> {
                    mBinding?.progressBar?.hide()
                    apiErrorToast(it.e)
                }
                is Outcome.BadRequest -> {
                    mBinding?.progressBar?.hide()
                }
                is Outcome.Failure -> {
                    mBinding?.progressBar?.hide()
                    apiErrorToast(it.e)
                }
            }
        }

        viewModel.metaDataLiveData.observe(viewLifecycleOwner) { metaData ->
            toolbarListener?.setUpToolbar(metaData.toolbar)
            setupJoinGroupAction(metaData.toolbar)
            setUpBottomCta(metaData.bottomCta)
            setUpSearch(metaData.search)
        }

        if (pageType == MY_GROUP) {
            getNavigationResult<Boolean>(SgCreateBottomSheetFragment.CAN_REFRESH_LIST)?.observe(
                viewLifecycleOwner
            ) {
                if (it) {
                    resetVariablesToDefault()
                }
            }
        }
    }

    private fun setUpSearch(search: SgSearch?) {
        mBinding?.apply {
            etSearch.isVisible = search?.isEnabled == true
            etSearch.hint = search?.searchText
        }
    }

    private fun setupJoinGroupAction(toolbarData: SgToolbarData?) {
        toolbarData?.let {
            mBinding?.fabScrollUp?.setOnClickListener {
                val deeplink = toolbarData.cta?.deeplink ?: return@setOnClickListener
                val deeplinkUri = Uri.parse(deeplink)
                if (navController.graph.hasDeepLink(deeplinkUri)) {
                    mayNavigate {
                        navigate(deeplinkUri)
                        viewModel.sendEvent(EventConstants.SG_JOIN_GROUP_FAB_CLICKED, ignoreSnowplow = true)
                    }
                }
            }
        }

    }

    private fun setUpBottomCta(cta: BottomCta?) {
        mBinding?.btCreateNewGroup?.apply {
            isVisible = cta != null
            text = cta?.title
            setOnClickListener {
                val deeplink = cta?.deeplink ?: return@setOnClickListener
                val deeplinkUri = Uri.parse(deeplink)
                if (navController.graph.hasDeepLink(deeplinkUri)) {
                    mayNavigate {
                        navigate(deeplinkUri)
                        viewModel.sendEvent(EventConstants.SG_CREATE_NEW_GROUP_CLICKED)
                    }
                }
            }
        }
    }

    private fun setUpUiForNoResult(noWidgetContainer: NoWidgetContainer?) {
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

    private fun fetchMoreGroupList() {
        when (isSearchInProgress) {
            true -> {
                viewModel.getSearchResultList(currentQuery, searchWidgetListPage)
            }
            else -> {
                if (isLastPageReached.not()) {
                    viewModel.getSgGroupList(myWidgetListPage, pageType)
                }
            }
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun setUpClickListeners() {
        mBinding?.btCreateNewGroup?.setOnClickListener {
            SgCreateBottomSheetFragment.newInstance()
                .show(childFragmentManager, SgCreateBottomSheetFragment.TAG)
        }
    }

    fun setToolbarListener(onSetUpToolbar: OnSetUpToolbar) {
        this.toolbarListener = onSetUpToolbar
    }

    override fun performAction(action: Any) {
        when (action) {
            is StudyGroupClickToShare -> {
                sgShareListViewModel.addIdIntoSelectedGroupList(action.groupId)
                widgetListAdapter.notifyItemChanged(action.position, action.isSelected)
            }
            else -> {
            }
        }
    }

    interface OnSetUpToolbar {
        fun setUpToolbar(sgToolbar: SgToolbarData?)
    }
}