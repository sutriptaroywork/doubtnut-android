package com.doubtnutapp.libraryhome.coursev3.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.ApiCourseDetailData
import com.doubtnutapp.databinding.ActivityCourseDetailV3Binding
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import com.doubtnutapp.doubt.bookmark.widget.BookmarkListWidgetData
import com.doubtnutapp.doubt.bookmark.widget.BookmarkListWidgetModel
import com.doubtnutapp.leaderboard.widget.LeaderboardPersonalData
import com.doubtnutapp.leaderboard.widget.LeaderboardTabData
import com.doubtnutapp.libraryhome.coursev3.viewmodel.CourseDetailViewModel
import com.doubtnutapp.liveclass.ui.HomeWorkActivity
import com.doubtnutapp.liveclass.ui.HomeWorkSolutionActivity
import com.doubtnutapp.newglobalsearch.model.SearchTabsItem
import com.doubtnutapp.newglobalsearch.ui.adapter.IasAppliedFilterAdapter
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterAdapterNew
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.mockTest.event.RefreshMockTestList
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.disposables.Disposable
import javax.inject.Inject

/**
 * This screen loads test screen, course card screens as well related to courses.
 */
class CourseDetailActivityV3 : AppCompatActivity(), HasAndroidInjector, ActionPerformer {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: CourseDetailViewModel
    private lateinit var adapter: WidgetLayoutAdapter
    private lateinit var filterAdapter: WidgetLayoutAdapter

    private var tab = ""
    private var subTabId = ""
    private var assortmentId = ""
    private var notesType: String? = null
    private var subject: String? = null
    private var source: String? = null
    private var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener? = null
    private var filterV2Adapter: SearchFilterAdapterNew? = null
    private var appliedFilterAdapter: IasAppliedFilterAdapter? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private lateinit var binding: ActivityCourseDetailV3Binding

    private var appStateObserver: Disposable? = null

    companion object {
        private const val TAG = "CourseDetailActivityV3"
        const val PARAM_FILTER_V2 = "filter_v2"
        fun getStartIntent(
            context: Context,
            assortmentId: String,
            tab: String,
            subject: String?,
            isFilterV2: Boolean,
            source: String,
            searchId: String?
        ) =
            Intent(context, CourseDetailActivityV3::class.java).apply {
                putExtra(Constants.ASSORTMENT_ID, assortmentId)
                putExtra(Constants.SUBJECT, subject)
                putExtra(Constants.TAB, tab)
                putExtra(Constants.SOURCE, source)
                putExtra(PARAM_FILTER_V2, isFilterV2)
                putExtra(Constants.SEARCH_ID, searchId)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        statusbarColor(this, R.color.white_20)
        binding = ActivityCourseDetailV3Binding.inflate(layoutInflater)
        setContentView(binding.root)

        assortmentId = intent.getStringExtra(Constants.ASSORTMENT_ID).orEmpty()
        tab = intent.getStringExtra(Constants.TAB).orEmpty()
        if (tab == Constants.TYPE_DOUBTS) {
            defaultPrefs().edit {
                putBoolean(Constants.IS_ACCESS_DOUBT_BOOKMARK_UI_SHOWN, true)
            }
        }
        subject = intent.getStringExtra(Constants.SUBJECT)
        source = intent.getStringExtra(Constants.SOURCE)

        viewModel = viewModelProvider(viewModelFactory)
        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
            put(EventConstants.ASSORTMENT_ID, assortmentId)
        }
        viewModel.isFilterV2Enabled = intent.getBooleanExtra(PARAM_FILTER_V2, false)

        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.LC_COURSE_PAGE_VIEW,
                hashMapOf(
                    EventConstants.ASSORTMENT_ID to assortmentId,
                    EventConstants.TYPE to tab
                ), ignoreBranch = true
            )
        )

        Utils.sendClassLanguageSpecificEvent(EventConstants.LC_COURSE_PAGE_VIEW)

        binding.ivBack.setOnClickListener { onBackPressed() }

        setUpRecyclerView()
        setUpObserver()
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    private fun setUpObserver() {
        viewModel.widgetsLiveData.observeK(
            this,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        appStateObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is RefreshMockTestList -> {
                        initiateRecyclerListenerAndFetchInitialData()
                    }
                }
            }

    }

    private fun ioExceptionHandler() {
    }

    private fun unAuthorizeUserError() {

    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener?.setDataLoading(state)
        binding.progressBar.setVisibleState(state)
    }

    private fun onWidgetListFetched(data: ApiCourseDetailData) {
        if (data.widgets.isNullOrEmpty()) {
            infiniteScrollListener?.isLastPageReached = true
        }

        data.widgets?.forEachIndexed { index, widgetEntityModel ->
            when (widgetEntityModel.type) {
                WidgetTypes.TYPE_WIDGET_LEADERBOARD_PERSONAL -> {
                    (widgetEntityModel.data as? LeaderboardPersonalData)?.testId = assortmentId
                }
                WidgetTypes.TYPE_WIDGET_LEADERBOARD_TAB -> {
                    (widgetEntityModel.data as? LeaderboardTabData)?.testId = assortmentId
                }
                WidgetTypes.TYPE_BOOKMARK_LIST -> {
                    (widgetEntityModel.data as? BookmarkListWidgetData)?.assortmentId = assortmentId
                    (widgetEntityModel.data as? BookmarkListWidgetData)?.itemPosition = index
                }
            }
        }

        if (infiniteScrollListener?.currentPage == 1) {
            adapter.setWidgets(data.widgets.orEmpty())

            binding.tabLayout.isVisible = data.tabData != null
            binding.viewDivider.isVisible = data.tabData != null

            binding.tabLayout.removeAllTabs()
            binding.tabLayout.clearOnTabSelectedListeners()
            data.tabData?.items?.forEach { item ->
                binding.tabLayout.addTab(binding.tabLayout.newTab()
                    .apply {
                        text = item.title
                        tag = item.id
                    }
                )
            }
            data.tabData?.items?.indexOfFirst { it.isSelected == true }
                ?.takeIf { it != -1 }
                ?.let {
                    binding.tabLayout.getTabAt(it)?.select()
                }

            binding.tabLayout.addOnTabSelectedListener { tab ->
                subTabId = tab.tag.toString()
                performClearFilterAction()
            }

            if (data.filterWidgets.isNullOrEmpty()) {
                binding.rvFilterWidgets.hide()
            } else {
                binding.rvFilterWidgets.show()
                filterAdapter.setWidgets(data.filterWidgets.orEmpty())
            }
        } else {
            adapter.addWidgets(data.widgets.orEmpty())
        }

        updateV2Filters(data)
        binding.tvToolbarTitle.text = data.title.orEmpty()
    }

    private fun updateV2Filters(data: ApiCourseDetailData) {
        if (!data.v2Filters.isNullOrEmpty()) {
            viewModel.updateV2Filters(data.v2Filters as ArrayList<SearchFilter>)
        }
        if (!viewModel.v2Filters.isNullOrEmpty()) {
            binding.flFilters.show()
            if (filterV2Adapter == null) {
                binding.rvFilters.layoutManager =
                    object : LinearLayoutManager(this, RecyclerView.HORIZONTAL, false) {
                        override fun canScrollHorizontally(): Boolean {
                            return false
                        }
                    }
                filterV2Adapter = SearchFilterAdapterNew(
                    actionPerformer = this,
                    isYoutube = false,
                    isFromAllChapters = true
                )
                binding.rvFilters.adapter = filterV2Adapter
            }
            binding.clearFiters.setOnClickListener {
              performClearFilterAction()
            }
            formatV2Filters()
            filterV2Adapter?.updateData(
                SearchTabsItem(
                    "",
                    "",
                    false,
                    viewModel.v2Filters
                ), viewModel.v2Filters
            )
            updateAppliedFilter()
        } else {
            binding.flFilters.hide()
        }
    }

    private fun performClearFilterAction() {
        viewModel.isCourseFilterApplied = false
        viewModel.v2filterQueryParams.clear()
        viewModel.apiQueryParams.clear()
        subject = ""
        initiateRecyclerListenerAndFetchInitialData()
        binding.clearFiters.hide()
    }

    private fun updateAppliedFilter() {
        filterV2Adapter?.let {
            val appliedFilters: ArrayList<String> = arrayListOf()
            for (filterType in viewModel.v2Filters) {
                if (filterType.isSelected) {
                    for (filterValue in filterType.filters) {
                        if (filterValue.isSelected) {
                            appliedFilters.add(filterValue.display())
                        }
                    }
                }
            }
            if (appliedFilterAdapter == null) {
                appliedFilterAdapter = IasAppliedFilterAdapter(this)
                binding.rvAppliesFilters.layoutManager =
                    LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
                binding.rvAppliesFilters.adapter = appliedFilterAdapter
            }
            appliedFilterAdapter!!.updateData(appliedFilters, false)
        }

    }

    private fun formatV2Filters() {
        //set disable state

        for (filterType in viewModel.v2Filters) {
            if (!filterType.key.equals("course", true)) {
                if (viewModel.isCourseFilterApplied) {
                    for (filterValue in filterType.filters) {
                        filterValue.isDisabled = true
                    }
                }
                if (filterType.key.equals("class", true)) {
                    filterType.filters = ArrayList(filterType.filters.sortedWith(compareBy {
                        it.value.toInt()
                    }))
                }
            }
        }

    }

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(this, this, source)
        binding.rvWidgets.layoutManager = LinearLayoutManager(this)
        binding.rvWidgets.adapter = adapter

        filterAdapter = WidgetLayoutAdapter(this, this)
        binding.rvFilterWidgets.layoutManager = LinearLayoutManager(this)
        binding.rvFilterWidgets.adapter = filterAdapter

        initiateRecyclerListenerAndFetchInitialData()
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        binding.rvFilterWidgets.hide()
        adapter.clearData()
        filterAdapter.clearData()
        val startPage = 1
        binding.rvWidgets.clearOnScrollListeners()
        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvWidgets.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    fetchList(currentPage, viewModel.apiQueryParams)
                }
            }.also {
                it.setStartPage(startPage)
            }
        binding.rvWidgets.addOnScrollListener(infiniteScrollListener!!)
        fetchList(startPage, viewModel.apiQueryParams)
    }

    private fun fetchList(pageNumber: Int, filterQueryParams: HashMap<String, String>) {
        viewModel.fetchCourseData(
            page = pageNumber,
            assortmentId = assortmentId,
            tab = tab,
            subTabId = subTabId,
            subject = subject,
            notesType = notesType,
            queryParams = filterQueryParams
        )
    }

    override fun performAction(action: Any) {
        when (action) {
            is IasFilterValuePopupStateChanged -> {
                if (action.isVisible) {
                    binding.grayLayer.show()
                } else {
                    binding.grayLayer.hide()
                }
            }
            is FilterSelectAction -> {
                if (action.type == "subject") {
                    subject = action.filterText
                    notesType = null
                    initiateRecyclerListenerAndFetchInitialData()
                }
            }
            is NotesFilterBySelectAction -> {
                notesType = action.data.id
                initiateRecyclerListenerAndFetchInitialData()
            }
            is OnHomeWorkListClicked -> {
                if (action.status) {
                    HomeWorkSolutionActivity.startActivity(this, true, action.qid)
                } else {
                    startActivityForResult(HomeWorkActivity.getIntent(this, action.qid), 1)
                }
            }
            is AllChapterResultClicked -> {
                DoubtnutApp.INSTANCE.bus()?.send(action)
            }
            is IasFilterSelected -> {

                viewModel.apiQueryParams.clear()
                viewModel.v2filterQueryParams.putAll(action.appliedFilterMap)
                viewModel.apiQueryParams.putAll(viewModel.v2filterQueryParams)
                if (viewModel.isCourseFilterApplied) {
                    //Requirement if course filter is applied send class and language filter selected value but
                    // Don't send course filter value if any other filter is applied
                    //clearing to avoid send course filter is any other filter is applied
                    viewModel.v2filterQueryParams.clear()
                    //saving course filter in view model
                    viewModel.v2filterQueryParams.putAll(action.appliedFilterMap)
                }

                subject = ""
                updateAppliedFilter()
                initiateRecyclerListenerAndFetchInitialData()
                binding.clearFiters.show()

            }
            is CourseV2FilterApplied -> {
                viewModel.v2filterQueryParams.putAll(filterV2Adapter!!.getSelectedFilterValues())
                viewModel.isCourseFilterApplied = true
            }
            is GetDoubtSolutions -> {
                viewModel.getComments(
                    authToken = "",
                    entityType = "comment",
                    entityId = action.id,
                    page = "1"
                )
                    .observe(this, Observer { outcome ->
                        when (outcome) {
                            is Outcome.Progress -> {
                                binding.progressBar.show()
                            }
                            is Outcome.Failure -> {
                                binding.progressBar.hide()
                                if (NetworkUtils.isConnected(this)) {
                                    toast(getString(R.string.api_error))
                                    return@Observer
                                }
                                val dialog = NetworkErrorDialog.newInstance()
                                dialog.show(supportFragmentManager, "NetworkErrorDialog")
                            }
                            is Outcome.ApiError -> {
                                binding.progressBar.hide()
                                toast(getString(R.string.api_error))
                            }
                            is Outcome.BadRequest -> {
                                binding.progressBar.hide()
                                val dialog = BadRequestDialog.newInstance("unauthorized")
                                dialog.show(supportFragmentManager, "BadRequestDialog")
                            }
                            is Outcome.Success -> {
                                binding.progressBar.hide()
                                adapter.widgets.forEachIndexed { index, widgetEntityModel ->
                                    if (widgetEntityModel is BookmarkListWidgetModel) {
                                        widgetEntityModel.data.items?.forEach {
                                            if (it.id == outcome.data.data.firstOrNull()?.entityId) {
                                                it.items = outcome.data.data
                                            }
                                        }
                                    }
                                    adapter.notifyItemChanged(index)
                                }
                            }
                        }
                    })
            }
            else -> {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            initiateRecyclerListenerAndFetchInitialData()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appStateObserver?.dispose()
    }

}