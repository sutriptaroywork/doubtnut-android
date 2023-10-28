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
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentSgMyChatsBinding
import com.doubtnutapp.socket.viewmodel.SocketManagerViewModel
import com.doubtnutapp.studygroup.model.SgSearch
import com.doubtnutapp.studygroup.model.SgToolbarData
import com.doubtnutapp.studygroup.viewmodel.SgHomeViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgets.StudyGroupSearchView
import com.doubtnutapp.widgets.itemdecorator.SimpleDividerItemDecoration


class SgMyChatsFragment : BaseBindingFragment<SgHomeViewModel, FragmentSgMyChatsBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "SgMyChatsFragment"
        const val INITIAL_PAGE = 0
        fun newInstance() = SgMyChatsFragment()
    }

    private lateinit var socketManagerViewModel: SocketManagerViewModel

    private val navController by findNavControllerLazy()

    private var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? = null
    private val widgetListAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = SgChatFragment.STUDY_GROUP
        )
    }

    private var myWidgetListPage = 0
    private var searchWidgetListPage = 0
    private var isLastPageReached = false
    private var isSearchInProgress = false
    private var currentQuery = ""
    private var canRefresh = false

    private var toolbarListener: OnSetUpToolbar? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentSgMyChatsBinding =
        FragmentSgMyChatsBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): SgHomeViewModel {
        socketManagerViewModel = viewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setupUi()
        if (canRefresh) {
            resetVariablesToDefault()
        }
        fetchMoreChatList()
    }

    override fun onStop() {
        canRefresh = true
        super.onStop()
    }

    private fun setupUi() {
        binding.rvMyChats.addItemDecoration(
                SimpleDividerItemDecoration(
                        R.color.light_grey,
                        0.5F
                )
        )

        mBinding?.rvMyChats?.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        mBinding?.rvMyChats?.adapter = widgetListAdapter

        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(mBinding?.rvMyChats?.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    fetchMoreChatList()
                }
            }
        infiniteScrollListener?.setStartPage(1)
        infiniteScrollListener?.setVisibleThreshold(SgChatFragment.VISIBLE_THRESHOLD)
        mBinding?.rvMyChats?.addOnScrollListener(infiniteScrollListener!!)

        // Get result for search queries
        mBinding?.etSearch?.setQueryListener(object : StudyGroupSearchView.QueryListener {
            override fun onQuery(query: String) {
                mBinding ?: return
                searchWidgetListPage = INITIAL_PAGE
                currentQuery = query
                if (query.isEmpty()) {
                    isSearchInProgress = false
                    mBinding?.apply {
                        rvMyChats.show()
                        noResultContainer.root.hide()
                    }
                    widgetListAdapter.setWidgets(viewModel.getInitialWidgetList().toMutableList())
                } else {
                    viewModel.getSearchResultList(query, searchWidgetListPage)
                    viewModel.sendEvent(EventConstants.SG_SEARCH_QUERY, hashMapOf(
                        Constants.SEARCH_QUERY to query,
                        Constants.SOURCE to Constants.SG_PERSONAL_CHAT
                    ), ignoreSnowplow = true)
                }
            }
        })
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
        viewModel.sgWidgetListLiveData.observeEvent(this, {
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
                            binding.etSearch.isVisible = it.data.isSearchEnabled
                            binding.etSearch.setMinimumQueryTextLength(it.data.minSearchCharacters)
                            binding.rvMyChats.isVisible = widgetList.isNullOrEmpty().not()
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
                    myWidgetListPage = response.page
                    isLastPageReached = response.isReachedEnd
                    infiniteScrollListener?.isLastPageReached = isLastPageReached
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
        })

        viewModel.sgWidgetSearchResultLiveData.observeEvent(this, {
            when (it) {
                is Outcome.Progress -> {
                    updateProgressBarState(it.loading)
                }
                is Outcome.Success -> {
                    isSearchInProgress = true
                    val response = it.data
                    val widgetList = response.widgets
                    infiniteScrollListener?.setDataLoading(false)
                    when (searchWidgetListPage) {
                        INITIAL_PAGE -> {
                            if (widgetList.isNullOrEmpty()) {
                                setUpUiForNoResult(it.data.noWidgetContainer)
                            }
                            mBinding?.rvMyChats?.isVisible = widgetList.isNullOrEmpty().not()
                            widgetListAdapter.setWidgets(widgetList.orEmpty())
                        }
                        else -> {
                            widgetListAdapter.addWidgets(widgetList.orEmpty())
                        }
                    }
                    searchWidgetListPage = response.page
                    infiniteScrollListener?.isLastPageReached = response.isReachedEnd
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
        })

        viewModel.metaDataLiveData.observe(viewLifecycleOwner, { metaData ->
            toolbarListener?.setUpToolbar(metaData.toolbar)
            setUpBottomCta(metaData.bottomCta)
            setUpSearch(metaData.search)
        })
    }

    private fun setUpSearch(search: SgSearch?) {
        mBinding?.apply {
            etSearch.isVisible = search?.isEnabled == true
            etSearch.hint = search?.searchText
        }
    }

    private fun setUpBottomCta(cta: BottomCta?) {
        mBinding?.btCreateNewChat?.apply {
            isVisible = cta != null
            text = cta?.title
            setOnClickListener {
                val deeplink = cta?.deeplink ?: return@setOnClickListener
                val deeplinkUri = Uri.parse(deeplink)
                if (navController.graph.hasDeepLink(deeplinkUri)) {
                    mayNavigate {
                        navigate(deeplinkUri)
                        viewModel.sendEvent(EventConstants.SG_CREATE_NEW_CHAT_CLICKED, ignoreSnowplow = true)
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

    private fun fetchMoreChatList() {

        when (isSearchInProgress) {
            true -> {
                viewModel.getSearchResultList(currentQuery, searchWidgetListPage)
            }
            else -> {
                if (isLastPageReached.not()) {
                    viewModel.getSgChatList(myWidgetListPage)
                }
            }
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener?.setDataLoading(state)
        mBinding?.progressBar?.setVisibleState(state)
    }

    fun setToolbarListener(onSetUpToolbar: OnSetUpToolbar) {
        this.toolbarListener = onSetUpToolbar
    }

    override fun performAction(action: Any) {

    }

    interface OnSetUpToolbar {
        fun setUpToolbar(sgToolbar: SgToolbarData?)
    }
}