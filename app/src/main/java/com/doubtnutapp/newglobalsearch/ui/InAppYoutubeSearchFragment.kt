package com.doubtnutapp.newglobalsearch.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.toast
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.R
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.hide
import com.doubtnutapp.newglobalsearch.model.SearchListViewItem
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.adapter.IasAppliedFilterAdapter
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterAdapterNew
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchResultAdapter
import com.doubtnutapp.newglobalsearch.viewmodel.InAppSearchViewModel
import com.doubtnutapp.newglobalsearch.viewmodel.YoutubeSearchFragmentViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.show
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.VideoFragmentListener
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.in_app_youtube_search_fragment.*
import javax.inject.Inject

class InAppYoutubeSearchFragment :
    DaggerFragment(),
    VideoFragmentListener,
    ActionPerformer {

    companion object {
        private const val TAB_DATA = "tab_data"

        fun newInstance(tabKey: String) = InAppYoutubeSearchFragment().also {
            it.arguments = bundleOf(
                TAB_DATA to tabKey
            )
        }
    }

    private lateinit var adapter: SearchResultAdapter
    private var currentVideofragment: VideoFragment? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var screenNavigator: Navigator

    private lateinit var viewModel: YoutubeSearchFragmentViewModel

    private lateinit var parentViewModel: InAppSearchViewModel

    private var disposable: Disposable? = null

    private var searchFilterAdapter: SearchFilterAdapterNew? = null
    private var appliedFilterAdapter: IasAppliedFilterAdapter? = null
    private val appliedFilters: ArrayList<String> = arrayListOf()
    private var appliedFiltersMap: HashMap<String, String> = hashMapOf()

    private var tabData: SearchTabsItem? = null
    private var tabKey: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.in_app_youtube_search_fragment, container, false)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            viewModelFactory
        ).get(YoutubeSearchFragmentViewModel::class.java)
        parentViewModel = activityViewModelProvider(viewModelFactory)
        setUpObservers()
        setUpListeners()
        init()

        progressBarYoutubeSearch.show()
        viewModel.fetchDataFromYoutube(parentViewModel.searchedText)

        tabKey = arguments?.getString(TAB_DATA).orEmpty()
        tabData = parentViewModel.tabMap[tabKey]
        tabData?.filterList = viewModel.getFilterList() as ArrayList<SearchFilter>
        parentViewModel.tabMap[tabKey]?.filterList = tabData?.filterList
        updateFilter()

        appliedFilterAdapter = IasAppliedFilterAdapter(this)
        rvAppliesFilters.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rvAppliesFilters.adapter = appliedFilterAdapter
        appliedFilterAdapter!!.updateData(appliedFilters)

    }

    private fun setUpObservers() {
        viewModel.youtubeSearchResultsLiveData.observeK(
            viewLifecycleOwner,
            ::onYoutubeSearchSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )
    }

    private fun onYoutubeSearchSuccess(youtubeSearchList: List<SearchListViewItem>) {
        progressBarYoutubeSearch.hide()
        var resultCount = 0
        if (youtubeSearchList.isNotEmpty()) {
            showResultsView()
            resultCount = youtubeSearchList.size
        } else {
            showErrorView()
        }
        adapter.updateData(youtubeSearchList, null, parentViewModel.searchId)

        DoubtnutApp.INSTANCE.bus()?.send(YoutubeResultsFetched(resultCount))
    }

    private fun unAuthorizeUserError() {
        progressBarYoutubeSearch.hide()
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(requireActivity().supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        progressBarYoutubeSearch.hide()
        showErrorView()
    }

    private fun showErrorView() {
        clYoutubeResults.hide()
        clErrorMsg.show()
    }

    private fun showResultsView() {
        clYoutubeResults.show()
        clErrorMsg.hide()
    }

    private fun updateProgressBarState(state: Boolean) {
        progressBarYoutubeSearch.hide()
    }

    private fun ioExceptionHandler() {
        progressBarYoutubeSearch.hide()
        if (NetworkUtils.isConnected(requireActivity()).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun setUpListeners() {
        youtubeResultList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0)
                    (activity as InAppSearchActivity).closeKeyboard()
                else if (dy < 0)
                    (activity as InAppSearchActivity).closeKeyboard()
            }
        })

        disposable = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            when (it) {
                is IasClearAllFilters -> {
                    if (tabData!!.key.equals(it.facet.key, true)) {
                        searchFilterAdapter?.performAction(it)
                    }
                }
                is IasFilterSelected -> {
                    if (tabData!!.key.equals(it.tab.key, true)) {

                        searchFilterAdapter?.notifyDataSetChanged()
                        this.appliedFiltersMap = it.appliedFilterMap

                        appliedFilters.clear()
                        for ((k, v) in it.appliedFilterMap) {
                            appliedFilters.add(v)
                        }
                        appliedFilterAdapter?.updateData(appliedFilters)
                        performAction(it)
                    }
                }
            }
        }
    }

    private fun init() {
        setUpRecyclerView()
    }

    private fun setUpRecyclerView() {
        adapter = SearchResultAdapter(this, null)
        val itemAnimator = youtubeResultList.itemAnimator
        if (itemAnimator is SimpleItemAnimator)
            itemAnimator.supportsChangeAnimations = false
        youtubeResultList.adapter = adapter
    }

    override fun performAction(action: Any) {
        handleAction(action)
    }

    private fun handleAction(action: Any) {
        when (action) {
            is IasFilterValuePopupStateChanged -> {
                if (action.isVisible) {
                    grayLayer.show()
                } else {
                    grayLayer.hide()
                }
            }
            is SearchPlaylistClickedEvent -> (activity as InAppSearchActivity).sendSearchClickEvent(
                action.clickedData,
                action.clickedDataTitle,
                action.itemId,
                action.section,
                action.type,
                action.position,
                action.isToppersChoice,
                resultCount = action.resultCount,
                action.assortmentId
            )

            is YoutubeVideo -> if (action.videoId.isNotBlank()) {
                currentVideofragment = VideoFragment.newYoutubeInstance(
                    VideoFragment.Companion.YoutubeVideoData(
                        action.videoId,
                        showFullScreen = false
                    ),
                    this
                )
                videoFragmentContainer.show()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.videoFragmentContainer, currentVideofragment!!)
                    .commitAllowingStateLoss()
            }

            is ShowAllFilters -> {
                if (action.facet.key.equals(tabData!!.key)) {
                    parentViewModel.onFilterAction(action)
                }
            }

            is RemoveFilter -> {
                appliedFilters.remove(action.filterValue)
                appliedFilterAdapter?.updateData(appliedFilters)
                searchFilterAdapter?.onAppliedFilterValueRemoved(action.filterValue)
            }
            is IasClearAllFilters -> {
                if (action.facet.key.equals(tabData!!.key)) {
                    appliedFilters.clear()
                    appliedFilterAdapter?.updateData(appliedFilters)
                }

                if (appliedFilters.isNullOrEmpty()) {
                    clearFiters.hide()
                } else {
                    clearFiters.show()
                }
            }
            is IasFilterSelected -> {
                if (action.tab.key.equals(tabData!!.key)) {
                    this.appliedFiltersMap = action.appliedFilterMap

                    var filterString = ""
                    var filterStringSubject = ""
                    var filterStringClass = ""
                    var filterStringLanguage = ""
                    for (filter in action.appliedFilterMap) {
                        if (filter.key.equals("subject", true)) {
                            filterStringSubject = filterString.plus(" " + filter.value)
                        }
                        if (filter.key.equals("class", true)) {
                            filterStringClass = filterString.plus(" doubtnut class " + filter.value)
                        }
                        if (filter.key.equals("language", true)) {
                            filterStringLanguage = filterString.plus(" in " + filter.value)
                        }
                    }
                    filterString =
                        filterStringSubject.plus(filterStringClass.plus(filterStringLanguage))
                    viewModel.fetchDataFromYoutube(parentViewModel.searchedText + filterString)

                    val params = hashMapOf<String, Any>().apply {
                        put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_TRIGGER_FILTER)
                        put(
                            EventConstants.SEARCHED_ITEM,
                            YoutubeSearchFragmentViewModel.lastSearchedQuery
                        )
                        put(
                            EventConstants.SEARCHED_TEXT,
                            YoutubeSearchFragmentViewModel.lastSearchedQuery
                        )
                        put(EventConstants.SIZE, 0)
                        put(EventConstants.FACET, "Youtube")
                        put(EventConstants.SOURCE, parentViewModel.source)
                    }
                    for ((k, v) in action.appliedFilterMap) {
                        params.put(k + "_filter", v)

                    }
                    appliedFilters.clear()
                    for ((k, v) in action.appliedFilterMap) {
                        appliedFilters.add(v)
                    }

                    if (appliedFilters.isNullOrEmpty()) {
                        clearFiters.hide()
                    } else {
                        clearFiters.show()
                    }

                    appliedFilterAdapter?.updateData(appliedFilters)
                    parentViewModel.postMongoEvent(params)
                    searchFilterAdapter?.notifyDataSetChanged()
                }
            }
        }
    }


    fun stopVideoPlayer() {
        requireActivity().supportFragmentManager.beginTransaction().remove(currentVideofragment!!)
            .commitAllowingStateLoss()
        currentVideofragment = null
        videoFragmentContainer.hide()
    }

    private fun updateFilter() {
        if (tabData != null && !tabData!!.filterList.isNullOrEmpty()) {
            tabData?.filterList = viewModel.getFilterList() as ArrayList<SearchFilter>
            parentViewModel.tabMap[tabKey]?.filterList = tabData?.filterList
        }

        flFilters.hide()
        if (tabData != null && !tabData!!.filterList.isNullOrEmpty()) {
            if (searchFilterAdapter == null) {
                searchFilterAdapter = SearchFilterAdapterNew(this, true)
            }
            rvFilters.layoutManager =
                object : LinearLayoutManager(context, RecyclerView.HORIZONTAL, false) {
                    override fun canScrollHorizontally(): Boolean {
                        return false
                    }
                }

            appliedFilters.clear()
            for (filterType in tabData!!.filterList!!) {
                for (filter in filterType.filters) {
                    if (filter.isSelected) {
                        appliedFilters.add(filter.value)
                    }
                }
            }
            appliedFilterAdapter?.updateData(appliedFilters)

            if (appliedFilters.isNullOrEmpty()) {
                clearFiters.hide()
            } else {
                clearFiters.show()
            }

            clearFiters.setOnClickListener {
                if (!appliedFilters.isEmpty()) {
                    appliedFilters.clear()
                    appliedFilterAdapter?.updateData(appliedFilters)
                    searchFilterAdapter?.performAction(IasClearAllFilters(tabData!!, true))
                }
            }
            flFilters.show()
            rvFilters.show()
            if (rvFilters.adapter == null) {
                rvFilters.adapter = searchFilterAdapter
            }
            searchFilterAdapter?.updateData(tabData!!, tabData!!.filterList!! ?: arrayListOf())

        } else {
            appliedFilters.clear()
            appliedFilterAdapter?.updateData(appliedFilters)

        }
    }

    fun onFilterApplied(searchString: String) {
        viewModel.fetchDataFromYoutube(searchString)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }
}
