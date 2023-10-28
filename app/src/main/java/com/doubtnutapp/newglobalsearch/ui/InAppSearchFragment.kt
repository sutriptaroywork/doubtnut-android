package com.doubtnutapp.newglobalsearch.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.VipStateEvent
import com.doubtnutapp.base.*
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.newglobalsearch.entities.ChapterDetails
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilterItem
import com.doubtnutapp.newglobalsearch.model.NewSearchCategorizedDataItem
import com.doubtnutapp.newglobalsearch.model.SearchListViewItem
import com.doubtnutapp.newglobalsearch.model.SearchPlaylistViewItem
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.adapter.IasAppliedFilterAdapter
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterAdapterNew
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchResultAdapter
import com.doubtnutapp.newglobalsearch.viewmodel.InAppSearchViewModel
import com.doubtnutapp.newglobalsearch.viewmodel.SearchFragmentViewModel
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.videoPage.ui.YoutubeTypeVideoActivity
import com.google.android.material.snackbar.Snackbar
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.ias_nodata_view.*
import kotlinx.android.synthetic.main.in_app_search_fragment.*
import javax.inject.Inject


class InAppSearchFragment : DaggerFragment(), ActionPerformer {

    companion object {
        private const val SEARCH_DATA = "search_data"
        private const val IS_TRENDING = "is_trending"
        private const val IS_BOOK_FRAGMENT = "is_book_fragment"
        private const val TAB_DATA = "tab_data"
        private const val CHAPTER_DETAILS = "chapter_details"

        var isTrending: Boolean? = null
        var isBookFragment: Boolean = false

        fun newInstance(
            dataList: List<SearchListViewItem>,
            tabKey: String,
            isTrending: Boolean,
            chapterDetails: ChapterDetails?,
            isBookFragment: Boolean = false
        ) =
            InAppSearchFragment().also {
                it.arguments = bundleOf(
                    SEARCH_DATA to dataList,
                    IS_TRENDING to isTrending,
                    IS_BOOK_FRAGMENT to isBookFragment,
                    TAB_DATA to tabKey,
                    CHAPTER_DETAILS to chapterDetails
                )
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var screenNavigator: Navigator

    private lateinit var viewModel: SearchFragmentViewModel

    private lateinit var parentViewModel: InAppSearchViewModel

    private var disposable: Disposable? = null

    private var searchFilterAdapter: SearchFilterAdapterNew? = null
    private var appliedFilterAdapter: IasAppliedFilterAdapter? = null
    private val appliedFilters: ArrayList<String> = arrayListOf()

    var dataListWithFilter: List<NewSearchCategorizedDataItem>? = null
    var bookDataList: List<NewSearchCategorizedDataItem>? = null

    private var tabData: SearchTabsItem? = null
    private var tabKey: String = ""

    private var sortFilter: SearchFilter? = null
    private var chapterDetail: ChapterDetails? = null
    private var isGridTypeUi : Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.in_app_search_fragment, container, false)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel =
            ViewModelProviders.of(this, viewModelFactory).get(SearchFragmentViewModel::class.java)
        parentViewModel = activityViewModelProvider(viewModelFactory)

        setUpObservers()
        setUpListeners()
        init()
    }

    private fun setUpObservers() {

        setVipObserver()

        viewModel.postSearchClicked.observe(viewLifecycleOwner, Observer {
            if (it.type == "video" && it.isVip) {

                parentViewModel.sendEvent(
                    EventConstants.INAPP_SEARCH_CLICK_ETOOS_RESULT,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.ETOOS_CONTENT_TITLE, it.display)
                    },
                    parentViewModel.searchLandingVersion
                )
            }
            (activity as InAppSearchActivity).postUserSearchData(it)
        })

        viewModel.postSearchVipItemClicked.observe(viewLifecycleOwner, Observer {
            when (it.type) {

                Constants.ETOOS_VIDEO ->
                    if (parentViewModel.isVipUser) {
                        openCourseVideoPage(it.id, it.page)
                    } else {
//                        startActivity(VipPlanActivity.getStartIntent(activity as InAppSearchActivity, source = "InAppSearchFragment", paymentDetails = null))
                    }
                Constants.ETOOS_COURSE -> startActivity(
                    Intent(
                        activity as InAppSearchActivity,
                        MainActivity::class.java
                    ).apply {
                        action = Constants.OPEN_LIBRARY_FROM_BOTTOM
                    })
            }
        })

        viewModel.navigateLiveData.observe(viewLifecycleOwner, EventObserver {
            parentViewModel.isActionPerformed = true
            val screen = it.screen
            val args = it.hashMap
            when (screen) {
                is VideoYouTubeScreen -> {
                    startActivity(
                        YoutubeTypeVideoActivity.getStartIntent(
                            requireContext(),
                            args?.get(Constants.QUESTION_ID).toString()
                        )
                    )
                }
                is LiveClassesScreen -> {
                    startActivity(
                        VideoPageActivity.startActivity(
                            context = requireContext(),
                            questionId = args?.get(Constants.QUESTION_ID)?.toString().orEmpty(),
                            page = args?.get(Constants.PAGE).toString()
                        )
                    )
                }
                is NoScreen -> {
                    Snackbar.make(
                        searchResultList,
                        "this video will be available soon",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
                is LibraryVideoPlayListScreen -> {
                    val argsMap = mutableMapOf<String, Any>()
                    if (args != null) {
                        argsMap.putAll(args)
                    }
                    argsMap[Constants.IS_AUTO_PLAY] = true
                    screenNavigator.startActivityFromActivity(
                        requireContext(),
                        screen,
                        argsMap.toBundle()
                    )
                }
                else -> screenNavigator.startActivityFromActivity(
                    requireContext(),
                    screen,
                    args?.toBundle()
                )
            }
        })

        viewModel.recentSearchClicked.observe(viewLifecycleOwner, Observer {
            parentViewModel.recentSearchClicked(it)
        })

        viewModel.sendEventLiveData.observe(viewLifecycleOwner, Observer {
            sendEvent(it)
        })

        viewModel.sendToTabLiveData.observe(viewLifecycleOwner, Observer {
            sendEvent(it)
            parentViewModel.moveToSelectedTab(it)
        })
    }

    private fun openCourseVideoPage(id: String, page: String) {
        if (context == null) return
        val intent = VideoPageActivity.startActivity(
            requireContext(),
            id.toString(), "",
            "", page, "",
            false, "", "", false
        )
        startActivity(intent)
    }

    private fun setUpListeners() {
        searchResultList.setOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0)
                    (activity as InAppSearchActivity).closeKeyboard()
                else if (dy < 0)
                    (activity as InAppSearchActivity).closeKeyboard()
            }
        })
    }

    private fun init() {
        val searchResults = arguments?.getParcelableArrayList<SearchListViewItem>(SEARCH_DATA)
        chapterDetail = arguments?.getParcelable(CHAPTER_DETAILS)
        isTrending = arguments?.getBoolean(IS_TRENDING) ?: false
        isBookFragment = arguments?.getBoolean(IS_BOOK_FRAGMENT) ?: false
        tabKey = arguments?.getString(TAB_DATA).orEmpty()
        tabData = parentViewModel.tabMap[tabKey]
        dataListWithFilter =
            parentViewModel.categrorizedDataList.filter { it.tabType.equals("live_class") }

        appliedFilterAdapter = IasAppliedFilterAdapter(this)
        rvAppliesFilters.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rvAppliesFilters.adapter = appliedFilterAdapter
        appliedFilterAdapter!!.updateData(appliedFilters)

        updateFilter()

        if (searchResults != null && searchResults.isNotEmpty()) {
            searchResultList.show()
            setUpRecyclerView(searchResults)
            if (dataListWithFilter?.size!! > 0 && tabKey.equals("live_class")) {
                ll_tabs.show()
                flFilters.hide()
                if(parentViewModel.liveClassDataFirstTime?.text.isNullOrEmpty()) {
                    parentViewModel.liveClassDataFirstTime = dataListWithFilter?.get(0)
                }
                val data = dataListWithFilter?.get(0)
                if(!data?.secondaryText.isNullOrEmpty()) {
                    tab_item_header_primary.text = data?.text.orEmpty()
                    tab_item_text_primary.text = data?.description.orEmpty()
                    tab_item_header_secondary.text = data?.secondaryText.orEmpty()
                    tab_item_text_secondary.text = data?.secondaryDescription.orEmpty()
                }else{
                    ll_tabs.hide()
                    flFilters.show()
                }

                ll_primary_tab.setOnClickListener {
                    parentViewModel.postResultTabSwitch(data?.text!!,data?.dataList.size,tabKey)
                    parentViewModel.isPrimaryTabSelected = true
                    parentViewModel.searchedText = data?.text!!
                    updateResults(data?.dataList!!, chapterDetail)
                    view_primary.show()
                    view_secondary.hide()
                    flFilters.hide()
                    parentViewModel.tabMap[tabKey]?.filterList = (data.filterList as ArrayList<SearchFilter>?) ?: arrayListOf()
                    if(!appliedFilters.isNullOrEmpty()) {
                        searchFilterAdapter?.performAction(IasClearAllFilters(tabData!!, false))
                    }
                }

                ll_secondary_tab.setOnClickListener {
                    if (data != null) {
                        if(data?.secondaryText != null && data?.secondaryFilterList !=null) {
                            parentViewModel.postResultTabSwitch(
                                data?.secondaryText.orEmpty(),
                                data?.secondaryList!!.size,
                                tabKey)
                            parentViewModel.isPrimaryTabSelected = false
                            parentViewModel.searchedText = data?.secondaryText!!
                            updateResults(data?.secondaryList!!, chapterDetail)
                            view_primary.hide()
                            view_secondary.show()
                            flFilters.hide()
                            parentViewModel.tabMap[tabKey]?.filterList = (data.secondaryFilterList as ArrayList<SearchFilter>?) ?: arrayListOf()
                            if(!appliedFilters.isNullOrEmpty()) {
                                searchFilterAdapter?.performAction(IasClearAllFilters(tabData!!, false))
                            }
                        }
                    }
                }

                ll_all_filter_tab.setOnClickListener {
                  performAction(ShowAllFilters(tabData!!, false))
                }


            }else{
                ll_tabs.hide()
                flFilters.show()
            }
        } else {
            msgNoResultFound.show()
            clearFilterForResult.setOnClickListener {
                searchFilterAdapter?.performAction(IasClearAllFilters(tabData!!, false))
            }
            searchResultList.hide()
        }
    }


    private fun setUpRecyclerView(searchList: List<SearchListViewItem>) {
        for (item in searchList) {
            if (item is SearchPlaylistViewItem && item.viewTypeUi.equals("grid")) {
                isGridTypeUi = true
                val layoutManager = GridLayoutManager(activity, 2)
                searchResultList.layoutManager = layoutManager
                break
            }
        }
        val adapter = SearchResultAdapter(this, deeplinkAction)
        searchResultList.adapter = adapter
        var etoosContentCount = 0
        for (e in searchList) {
            if (e is SearchPlaylistViewItem && e.isVip) {
                etoosContentCount++
            }
        }
        if (etoosContentCount > 0)
            parentViewModel.sendEvent(
                EventConstants.INAPP_SEARCH_ETOOS_RESULT_DISPLAY,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.COUNT_OF_ETOOS_CONTENT, etoosContentCount)
                },
                parentViewModel.searchLandingVersion
            )

        adapter.updateData(searchList, chapterDetail, parentViewModel.searchId)
    }

    override fun performAction(action: Any) {
        when (action) {
            is IasFilterValuePopupStateChanged -> {
                if (action.isVisible) {
                    grayLayer?.show()
                } else {
                    grayLayer?.hide()
                }
            }
            is IasAllChapterClicked -> {
                tabData?.let {
                    parentViewModel.sendAllChapterClickedEvent(action, tabData!!)
                }
            }
            is LiveClassLectureClicked -> {
                deeplinkAction?.performAction(
                    requireContext(),
                    action.data.deeplinkUrl,
                    Constants.PAGE_SEARCH_SRP
                )
            }
            is IasSortByFilterSelected -> {
                if (tabKey.equals(action.tab.key)) {
                    for (filterType in tabData!!.filterList!!) {
                        if (filterType.key.equals("sort", true)) {
                            sortFilter = filterType
                        }
                    }
                    parentViewModel.onFilterAction(action)
                }
            }
            is RemoveFilter -> {
                appliedFilters.remove(action.filterValue)
                appliedFilterAdapter?.updateData(appliedFilters)
                searchFilterAdapter?.onAppliedFilterValueRemoved(action.filterValue)
            }
            is IasFilterSelected -> {
                if (tabKey.equals(action.tab.key)) {
                    appliedFilters.clear()
                    for ((k, v) in action.appliedFilterMap) {
                        appliedFilters.add(v)
                    }
                    appliedFilterAdapter?.updateData(appliedFilters)
                    parentViewModel.onFilterAction(action)
                }
            }
            is IasFilterTypeSelected -> {
                parentViewModel.onFilterAction(action)
            }
            is IasFilterValueSelected -> {
                parentViewModel.onFilterAction(action)
            }
            is IasFilterValueDeselected -> {
                parentViewModel.onFilterAction(action)
            }
            is IasClearAllFilters -> {
                if (tabKey.equals(action.facet.key)) {
                    sortFilter = null
                    appliedFilters.clear()
                    appliedFilterAdapter?.updateData(appliedFilters)
                    parentViewModel.onFilterAction(action)
                }
            }
            is ShowAllFilters -> {
                parentViewModel.onFilterAction(action)
            }
            else -> {
                viewModel.handleAction(action)
            }
        }

    }

    private fun sendEvent(action: Any) {
        when (action) {
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

            is TrendingPlaylistClicked -> (activity as InAppSearchActivity).sendTrendingClickEvent(
                action.type,
                action.searchedData,
                action.type,
                action.itemPosition,
                action.isRecentSearch
            )

            is TrendingPlaylistMongoEvent -> (activity as InAppSearchActivity).sendTrendingRecentMongoClickEvent(
                action
            )
            is SeeAllSearchResults -> parentViewModel.sendSeeAllClickEvent(action)
            is CourseBannerClicked -> {
                parentViewModel.postMongoEvent(hashMapOf<String, Any>().apply {
                    put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_COURSE_BANNER_CLICKED)
                    put(EventConstants.SEARCHED_ITEM, parentViewModel.searchedText)
                    put(EventConstants.SEARCHED_TEXT, parentViewModel.searchedText)
                    put(EventConstants.SIZE, 0)
                    put(EventConstants.IS_CLICKED, true)
                    put(EventConstants.VARIANT_ID,FeaturesManager.getVariantId(DoubtnutApp.INSTANCE,Features.IAS_SERVICE))
                })
            }

            is AdvancedFilterClicked -> {
                parentViewModel.getFilterList(action.tabType)
            }
        }
    }

    private fun setVipObserver() {
        disposable = (context?.applicationContext as? DoubtnutApp)?.bus()?.toObservable()
            ?.subscribe { event ->
                if (event is VipStateEvent) {
                    parentViewModel.isVipUser = event.state
                }
                if (event is IasFilterSelected) {
                    if (event.tab.key.equals(tabKey, true)) {
                        appliedFilters.clear()
                        for ((k, v) in event.appliedFilterMap) {
                            appliedFilters.add(v)
                        }
                        appliedFilterAdapter?.updateData(appliedFilters)
                    }
                }
                if (event is IasClearAllFilters) {
                    if (event.facet.key.equals(tabKey, true)) {
                        appliedFilters.clear()
                        appliedFilterAdapter?.updateData(appliedFilters)
                    }
                }
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    fun updateResults(searchList: List<SearchListViewItem>, chapterDetails: ChapterDetails?) {
        if(appliedFilters.isEmpty() && parentViewModel.liveClassDataFirstTime!=null
            && tabKey.equals("live_class") && !parentViewModel.liveClassDataFirstTime?.secondaryText.isNullOrEmpty()){
            if(parentViewModel.liveClassDataFirstTime?.secondaryText.isNullOrEmpty()){
                (searchResultList.adapter as SearchResultAdapter).updateData(
                    searchList,
                    chapterDetail,
                    parentViewModel.searchId
                )
                flFilters.show()
                ll_tabs.hide()
            }else{
                if(parentViewModel.isPrimaryTabSelected){
                    (searchResultList.adapter as SearchResultAdapter).updateData(
                        parentViewModel.liveClassDataFirstTime?.dataList!!,
                        chapterDetail,
                        parentViewModel.searchId
                    )
                }else{
                    (searchResultList.adapter as SearchResultAdapter).updateData(
                        parentViewModel.liveClassDataFirstTime?.secondaryList!!,
                        chapterDetail,
                        parentViewModel.searchId
                    )
                }
                flFilters.hide()
                ll_tabs.show()
            }
            updateFilter()
            flFilters.hide()
        } else {
            this.chapterDetail = chapterDetails
            dataListWithFilter =
                parentViewModel.categrorizedDataList.filter { it.tabType.equals("live_class") }
            this.tabData = parentViewModel.tabMap[tabKey]

            if (searchList.isNullOrEmpty()) {
                if (dataListWithFilter?.size!! > 0 && tabKey.equals("live_class")) {
                    updateFilter()
                    ll_tabs.show()
                    flFilters.hide()
                } else {
                    ll_tabs.hide()
                    flFilters.show()
                    updateFilter()
                }
                searchResultList.hide()

                clearFilterForResult.setOnClickListener {
                    searchFilterAdapter?.performAction(IasClearAllFilters(tabData!!, false))
                }
            } else {
                msgNoResultFound.hide()
                searchResultList.show()

                if (dataListWithFilter?.size!! > 0 && tabKey.equals("live_class")) {
                    updateFilter()
                    ll_tabs.show()
                    flFilters.hide()
                } else {
                    ll_tabs.hide()
                    flFilters.show()
                    updateFilter()
                }
                if (tabKey.equals("live_class")) {
                    if (dataListWithFilter?.get(0)?.secondaryText.isNullOrEmpty()) {
                        (searchResultList.adapter as SearchResultAdapter).updateData(
                            searchList,
                            chapterDetail,
                            parentViewModel.searchId
                        )
                        flFilters.show()
                        ll_tabs.hide()
                    } else {
                        if (parentViewModel.isPrimaryTabSelected) {
                            (searchResultList.adapter as SearchResultAdapter).updateData(
                                dataListWithFilter?.get(0)?.dataList!!,
                                chapterDetail,
                                parentViewModel.searchId
                            )
                        } else {
                            (searchResultList.adapter as SearchResultAdapter).updateData(
                                dataListWithFilter?.get(0)?.secondaryList!!,
                                chapterDetail,
                                parentViewModel.searchId
                            )
                        }
                        flFilters.hide()
                        ll_tabs.show()
                    }
                } else {

                    (searchResultList.adapter as SearchResultAdapter).updateData(
                        searchList,
                        chapterDetail,
                        parentViewModel.searchId
                    )
                }
            }
        }
    }

    fun sortResult(filterValue: SearchFilterItem) {
        if ((searchResultList.adapter as SearchResultAdapter).itemCount > 0) {
            (searchResultList.adapter as SearchResultAdapter).sortData(
                filterValue.key!!, filterValue.order
                    ?: 0
            )
        }

        appliedFilters.clear()
        for (filterType in tabData!!.filterList!!) {
            if (filterType.key.equals("sort", true) && sortFilter != null) {
                filterType.filters = sortFilter!!.filters
            }
            for (filter in filterType.filters) {
                if (filter.isSelected) {
                    appliedFilters.add(filter.value)
                }
            }
        }
        appliedFilterAdapter?.updateData(appliedFilters)
        searchFilterAdapter?.notifyDataSetChanged()
    }

    private fun updateFilter() {
        flFilters.hide()
        if (tabData != null && !tabData!!.filterList.isNullOrEmpty()) {
            for (filterType in tabData!!.filterList!!) {
                if (filterType.key.equals("class", true)) {
                    filterType.filters = ArrayList(filterType.filters.sortedWith(compareBy {
                        it.value.toInt()
                    }))
                    break
                }
            }
            rvFilters.layoutManager =
                object : LinearLayoutManager(context, RecyclerView.HORIZONTAL, false) {
                    override fun canScrollHorizontally(): Boolean {
                        return false
                    }
                }
            appliedFilters.clear()
            for (filterType in tabData!!.filterList!!) {
                if (filterType.key.equals("sort", true) && sortFilter != null) {
                    filterType.filters = sortFilter!!.filters
                }
                for (filter in filterType.filters) {
                    if (filter.isSelected) {
                        appliedFilters.add(filter.value)
                    }
                }
            }
            appliedFilterAdapter?.updateData(appliedFilters)

            if (searchFilterAdapter == null) {
                searchFilterAdapter = SearchFilterAdapterNew(this)
            }
            val lastSelectedFilterType = searchFilterAdapter!!.selectedFilterTypePosition

            clearFiters.setOnClickListener {
                if (!appliedFilters.isNullOrEmpty()) {
                    parentViewModel.isClearClicked = true;
                    searchFilterAdapter?.performAction(IasClearAllFilters(tabData!!, false))}
                else{
                    parentViewModel.isClearClicked = false;
                }
            }

            if (appliedFilters.isNullOrEmpty()) {
                clearFiters.hide()
            } else {
                clearFiters.show()
            }

            flFilters.show()
            rvFilters.show()
            if (rvFilters.adapter == null) {
                rvFilters.adapter = searchFilterAdapter
            }
            searchFilterAdapter?.updateData(tabData!!, tabData!!.filterList!!)
            searchFilterAdapter?.selectedFilterTypePosition = lastSelectedFilterType
        } else {
            filterText.hide()
            clearFiters.hide()
            appliedFilters.clear()
            appliedFilterAdapter?.updateData(appliedFilters)
        }
    }
}
