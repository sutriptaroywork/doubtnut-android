package com.doubtnutapp.studygroup.ui.fragment

import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.common.data.entity.BottomCta
import com.doubtnut.core.common.data.entity.NoWidgetContainer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.SgBlockMember
import com.doubtnutapp.base.SgRequestPrimaryCtaClick
import com.doubtnutapp.base.SgRequestSecondaryCtaClick
import com.doubtnutapp.base.extension.findNavControllerLazy
import com.doubtnutapp.base.extension.getNavigationResult
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentSgExtraInfoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.studygroup.model.SgSearch
import com.doubtnutapp.studygroup.model.SgToolbarData
import com.doubtnutapp.studygroup.model.StudyGroupBlockData
import com.doubtnutapp.studygroup.viewmodel.SgExtraInfoViewModel
import com.doubtnutapp.studygroup.viewmodel.StudyGroupViewModel
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.doubtnutapp.widgets.StudyGroupSearchView
import com.doubtnutapp.widgets.itemdecorator.SimpleDividerItemDecoration
import javax.inject.Inject


class SgExtraInfoFragment :
    BaseBindingFragment<SgExtraInfoViewModel, FragmentSgExtraInfoBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "SgExtraInfoFragment"
        const val INITIAL_PAGE = 0

        fun newInstance(screen: String? = null, hideToolbar: Boolean = false) = SgExtraInfoFragment().apply {
            arguments = Bundle().apply {
                putString("screen", screen)
                putBoolean("hide_toolbar",hideToolbar)
            }
        }
    }

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var myWidgetListPage = 0
    private var searchWidgetListPage = 0
    private var isSearchInProgress = false
    private var currentQuery = ""
    private var isLastPageReached = false
    private var canRefresh = false

    private var itemPositionToRemove = RecyclerView.NO_POSITION

    private val args by navArgs<SgExtraInfoFragmentArgs>()
    private val screen: String by lazy { args.screen?: "public_groups" }
    private val navController by findNavControllerLazy()

    private var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? = null
    private lateinit var studyGroupViewModel: StudyGroupViewModel
    private val widgetListAdapter: WidgetLayoutAdapter by lazy {
        WidgetLayoutAdapter(
            context = requireContext(),
            actionPerformer = this,
            source = screen
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentSgExtraInfoBinding =
        FragmentSgExtraInfoBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): SgExtraInfoViewModel {
        studyGroupViewModel = activityViewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setupUi()
        if (canRefresh) {
            resetVariablesToDefault()
        }
        fetchNextWidgets()
        viewModel.sendEvent(EventConstants.SG_EXTRA_INFO_SCREEN, hashMapOf<String, Any>().apply {
            put(EventConstants.TYPE, screen)
        }, ignoreSnowplow = true)
    }

    override fun onStop() {
        canRefresh = true
        super.onStop()
    }

    private fun setupUi() {
        mBinding ?: return
        if(arguments?.getBoolean("hide_toolbar") == true) {
            binding.toolbar.toolbar.hide()
        }
        binding.rvWidgets.addItemDecoration(
            SimpleDividerItemDecoration(
                R.color.light_grey,
                0.5F
            )
        )
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
        infiniteScrollListener?.setVisibleThreshold(SgChatFragment.VISIBLE_THRESHOLD)
        binding.rvWidgets.addOnScrollListener(infiniteScrollListener!!)

        // Get result for search queries
        binding.etSearch.setQueryListener(object : StudyGroupSearchView.QueryListener {
            override fun onQuery(query: String) {
                mBinding ?: return
                currentQuery = query
                searchWidgetListPage = 0
                if (query.isEmpty()) {
                    isSearchInProgress = false
                    mBinding?.apply {
                        rvWidgets.show()
                        noResultContainer.root.hide()
                    }
                    widgetListAdapter.setWidgets(viewModel.getMyGroupChatList().toMutableList())
                } else {
                    viewModel.getSearchResultList(query, searchWidgetListPage)
                    viewModel.sendEvent(EventConstants.SG_SEARCH_QUERY, hashMapOf(
                        Constants.SEARCH_QUERY to query,
                        Constants.SOURCE to screen
                    ), ignoreSnowplow = true)
                }
            }
        })

        mBinding?.toolbar?.ivBack?.setOnClickListener {
            navController.navigateUp()
        }
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
                            binding.etSearch.isVisible = it.data.isSearchEnabled
                            binding.etSearch.setMinimumQueryTextLength(it.data.minSearchCharacters)
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
                    myWidgetListPage = response.page
                    isLastPageReached = response.isReachedEnd
                    infiniteScrollListener?.isLastPageReached = response.isReachedEnd
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

        viewModel.searchResultList.observeEvent(this, {
            mBinding ?: return@observeEvent
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
                    infiniteScrollListener?.isLastPageReached = response.isReachedEnd
                }
                is Outcome.ApiError -> {
                    mBinding?.progressBar?.hide()
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

        viewModel.metaDataLiveData.observe(viewLifecycleOwner, { metaData ->
            setToolbarData(metaData.toolbar)
            setUpBottomCta(metaData.bottomCta)
            setUpSearch(metaData.search)
        })

        studyGroupViewModel.blockUserLiveData.observeEvent(this) {
            toast(it.message.orEmpty())
        }

        studyGroupViewModel.unblockUserLiveData.observeEvent(this) {
            val data = it
            if (it.isUnblock == true) {
                itemPositionToRemove = data.itemPosition
                removeItemAtPosition()
                toast(it.message.orEmpty())
                if (widgetListAdapter.itemCount == 0){
                    resetVariablesToDefault()
                    fetchNextWidgets()
                }
            }
        }

        viewModel.requestRequest.observe(viewLifecycleOwner, {
            removeWidget()
        })

        getNavigationResult<String?>("join_info")?.observe(viewLifecycleOwner, {
            removeWidget()
        })
    }

    fun removeWidget() {
        removeItemAtPosition()
        if (widgetListAdapter.widgets.isEmpty()) {
            resetVariablesToDefault()
            fetchNextWidgets()
        }
    }

    private fun setToolbarData(sgToolbarData: SgToolbarData?) {
        mBinding?.apply {
            toolbar.tvTitle.text = sgToolbarData?.title
            toolbar.tvJoinStudyGroup.apply {
                isVisible = sgToolbarData?.cta?.title != null
                text = sgToolbarData?.cta?.title
                Glide.with(requireContext()).load(sgToolbarData?.cta?.image)
                    .apply(RequestOptions().fitCenter()).into(
                        object : CustomTarget<Drawable>(28, 28) {
                            override fun onLoadCleared(placeholder: Drawable?) {

                            }

                            override fun onResourceReady(
                                resource: Drawable,
                                transition: com.bumptech.glide.request.transition.Transition<in Drawable>?,
                            ) {
                                setCompoundDrawablesWithIntrinsicBounds(
                                    resource,
                                    null,
                                    null,
                                    null
                                )
                            }
                        }
                    )
                setOnClickListener {
                    deeplinkAction.performAction(requireContext(), sgToolbarData?.cta?.deeplink)
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

    private fun setUpBottomCta(cta: BottomCta?) {
        mBinding?.btCreateNewGroup?.apply {
            isVisible = cta != null
            text = cta?.title
            setOnClickListener {
                val deeplinkUri = Uri.parse(cta?.deeplink)
                if (navController.graph.hasDeepLink(deeplinkUri)) {
                    mayNavigate {
                        navigate(deeplinkUri)
                    }
                }
            }
        }
    }

    private fun removeItemAtPosition() {
        if (itemPositionToRemove == RecyclerView.NO_POSITION) return
        widgetListAdapter.removeWidgetAt(itemPositionToRemove)
        itemPositionToRemove = RecyclerView.NO_POSITION
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

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener?.setDataLoading(state)
        mBinding?.progressBar?.setVisibleState(state)
    }

    private fun resetVariablesToDefault() {
        myWidgetListPage = 0
        searchWidgetListPage = 0
        isLastPageReached = false
        isSearchInProgress = false
        currentQuery = ""
        canRefresh = false
    }

    private fun fetchNextWidgets() {
        when (isSearchInProgress) {
            true -> {
                viewModel.getSearchResultList(currentQuery, searchWidgetListPage)
            }
            else -> {
                if (isLastPageReached.not()) {
                    viewModel.getRequiredWidgetData(myWidgetListPage, screen)
                }
            }
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is SgRequestPrimaryCtaClick -> {
                itemPositionToRemove = action.adapterPosition
                mayNavigate {
                    val deeplinkUri = Uri.parse(action.deeplink)
                    if (navController.graph.hasDeepLink(deeplinkUri)) {
                        navController.navigate(deeplinkUri)
                        viewModel.sendEvent(
                            EventConstants.SG_EXTRA_INFO_PRIMARY_CTA_CLICK,
                            hashMapOf<String, Any>().apply {
                                put(Constants.TYPE, screen)
                            }, ignoreSnowplow = true
                        )
                    }
                }
            }
            is SgRequestSecondaryCtaClick -> {
                val deeplinkUri = Uri.parse(action.deeplink)
                val inviter = deeplinkUri.getQueryParameter("inviter") ?: return
                val groupId = deeplinkUri.getQueryParameter("group_id") ?: return
                itemPositionToRemove = action.adapterPosition
                viewModel.ignoreRequest(inviter, groupId)
                viewModel.sendEvent(
                    EventConstants.SG_EXTRA_INFO_SECONDARY_CTA_CLICK,
                    hashMapOf<String, Any>().apply {
                        put(Constants.TYPE, screen)
                    }, ignoreSnowplow = true
                )
            }
            is SgBlockMember -> {
                studyGroupViewModel.block(
                    StudyGroupBlockData(
                        roomId = action.roomId.orEmpty(),
                        studentId = action.studentId!!,
                        action.name.orEmpty(),
                        confirmationPopup = action.confirmationPopup,
                        adapterPosition = action.adapterPosition,
                        members = emptyList(),
                        actionSource = action.actionSource,
                        actionType = action.actionType
                    )
                )
            }
            else -> {
            }
        }
    }
}