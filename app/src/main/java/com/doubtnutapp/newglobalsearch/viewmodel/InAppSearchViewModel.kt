package com.doubtnutapp.newglobalsearch.viewmodel

import android.net.Uri
import android.nfc.FormatException
import android.text.TextUtils
import android.text.format.DateFormat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.IN_APP_SEARCH_LANDING_VERSION
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.domain.library.entities.ClassListViewItem
import com.doubtnutapp.domain.newglobalsearch.entities.*
import com.doubtnutapp.domain.newglobalsearch.interactor.*
import com.doubtnutapp.globalsearch.event.InAppSearchEventManager
import com.doubtnutapp.home.HomeFeedFragmentV2
import com.doubtnutapp.libraryhome.library.LibraryFragmentHome
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.newglobalsearch.mapper.NewSearchSuggestionsItemMapper
import com.doubtnutapp.newglobalsearch.mapper.NewSearchViewItemMapper
import com.doubtnutapp.newglobalsearch.mapper.SearchSuggestionsItemMapper
import com.doubtnutapp.newglobalsearch.mapper.TrendingSearchViewItemMapper
import com.doubtnutapp.newglobalsearch.model.*
import com.doubtnutapp.newglobalsearch.ui.InAppSearchFragment
import com.doubtnutapp.newglobalsearch.ui.InAppSearchTrendingDataFragment
import com.doubtnutapp.newglobalsearch.ui.InAppYoutubeSearchFragment
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.UserUtil
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import org.json.JSONArray
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class
InAppSearchViewModel @Inject constructor(
    private val getTrendingSearchUseCase: GetTrendingSearchUseCase,
    private val getGlobalSearchUseNewCase: GetGlobalSearchNewUseCase,
    private val getGlobalSearchSuggestionsUseCase: GetGlobalSearchSuggestionsUseCase,
    private val postUserSearchDataUseCase: PostUserSearchDataUseCase,
    private val postMongoEventUseCase: PostMongoEventUseCase,
    private val postSuggestionClickDataUseCase: PostSuggestionClickDataUseCase,
    private val userPreference: UserPreference,
    private val newSearchViewItemMapper: NewSearchViewItemMapper,
    private val trendingSearchViewItemMapper: TrendingSearchViewItemMapper,
    private val searchSuggestionItemMapper: SearchSuggestionsItemMapper,
    private val newSearchSuggestionsItemMapper: NewSearchSuggestionsItemMapper,
    private val inAppSearchEventManager: InAppSearchEventManager,
    private val publishSubject: PublishSubject<String>,
    private val newSuggestionsPubSub: PublishSubject<String>,
    private val searchPubSub: PublishSubject<String>,
    private val filterSearchPubSub: PublishSubject<String>,
    private val analyticsPublisher: AnalyticsPublisher,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private var recentSearches: ArrayList<String> = ArrayList(3)
    private var isSuggesterEnabled: Double = -1.0
    private var isTrendingChapterEnabled: Double = 0.0
    private var isVideoQueryChangeEnabled: Double = 0.0
    private var dataLogsdelay: Double = 1000.0
    var mLastClickTime: Long = 0
    var mLastEventType: String = ""
    var isSearchIconEnabled: Double = 0.0

    var searchId = ""

    init {
        getSuggesterFlagger()
        setUpSearchObserver()
        setUpSuggesterObserver()
        setUpNewSuggesterObserver()
        setUpFilterSearchObserver()
    }

    private fun getSuggesterFlagger() {
        val iasServicePayload =
            FeaturesManager.getFeaturePayload(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
        if (!iasServicePayload.isNullOrEmpty()) {
            isSuggesterEnabled = iasServicePayload["is_suggester_enabled"] as? Double ?: -1.0
            isTrendingChapterEnabled = iasServicePayload["is_trending_chapter_enabled"] as? Double ?: -1.0
            isVideoQueryChangeEnabled = iasServicePayload["is_video_query_change_enabled"] as? Double ?: 0.0
            dataLogsdelay = iasServicePayload["data_logs_delay"] as? Double ?: 1000.0
            isSearchIconEnabled = iasServicePayload["search_icon"] as? Double ?: 0.0
        }
    }

    companion object {
        /**
         * Time delay to fetch suggestions while user is typing the search keywords.
         */
        const val TYPING_DELAY = 350L
        const val TYPING_DELAY_SUGGESTER = 100L
    }

    var isYoutubeFeatureEnabled: Boolean = false
    var searchedText: String = ""
    var searchedDisplayTrending: String = ""
    var suggesterPayloadMap: HashMap<String, Any>? = null
    var appliedFilterMap: HashMap<String, Any>? = null
    var advancedFilterTabType: String = ""

    private var searchSessionsList: MutableList<SearchSessionModel> = mutableListOf()

    private var allTabDataList: List<SearchListViewItem> = emptyList()
    var categrorizedDataList: List<NewSearchCategorizedDataItem> = emptyList()
    private lateinit var classesListData: ArrayList<ClassListViewItem>
    var isVipUser: Boolean = false
    var liveClassDataFirstTime: NewSearchCategorizedDataItem? = null

    var isPrimaryTabSelected: Boolean = true
    var isClearClicked: Boolean = false

    var searchLandingVersion: Int = 2
    lateinit var tabslist: List<SearchTabsItem>
    var tabMap = hashMapOf<String, SearchTabsItem>()
    var filteredFacet: SearchTabsItem? = null

    var isActionPerformed = false
    var isSearchResultClicked = false
    var isAdvancedSearchPopupShown = false
    var isAdvancedSerachDataRequest = false

    val filterOptionsMap = HashMap<String, Any>()

    private val _trendingSearchLiveData =
        MutableLiveData<Outcome<List<TrendingSearchDataListViewItem>>>()

    val trendingSearchLiveData: LiveData<Outcome<List<TrendingSearchDataListViewItem>>>
        get() = _trendingSearchLiveData

    private val _filterEventLiveData = MutableLiveData<Any>()

    val filterEventLiveData: LiveData<Any>
        get() = _filterEventLiveData

    private val _userNewSearchLiveData = MutableLiveData<Outcome<NewSearchDataItem>>()

    val userNewSearchLiveData: LiveData<Outcome<NewSearchDataItem>>
        get() = _userNewSearchLiveData

    private val _searchSuggestionsLiveData = MutableLiveData<Outcome<SearchSuggestionsDataItem>>()

    val searchSuggestionsLiveData: LiveData<Outcome<SearchSuggestionsDataItem>>
        get() = _searchSuggestionsLiveData

    private val _recentSearchClicked = MutableLiveData<Pair<String, Boolean>>()

    val recentSearchClicked: LiveData<Pair<String, Boolean>>
        get() = _recentSearchClicked

    private val _topTagSubjectClicked = MutableLiveData<Triple<String, String, Boolean>>()

    val topTagSubjectClicked: LiveData<Triple<String, String, Boolean>>
        get() = _topTagSubjectClicked

    private val _seeAllClicked = MutableLiveData<Int>()

    val seeAllClicked: LiveData<Int>
        get() = _seeAllClicked

    private val _searchHints = MutableLiveData<Map<String, List<String>?>>()
    val searchHints: LiveData<Map<String, List<String>?>>
        get() = _searchHints

    private val _advancedSearchResponse = MutableLiveData<Outcome<ApiSearchAdvanceFilterResponse>>()
    val advancedSearchResponse: LiveData<Outcome<ApiSearchAdvanceFilterResponse>>
        get() = _advancedSearchResponse

    private val _filterOptionResponse = MutableLiveData<Outcome<ApiSearchAdvanceFilterResponse>>()
    val filterOptionResponse: LiveData<Outcome<ApiSearchAdvanceFilterResponse>>
        get() = _filterOptionResponse

    private var _searchTextLiveData: MutableLiveData<Event<String>> = MutableLiveData()

    val searchTextLiveData: MutableLiveData<Event<String>>
        get() = _searchTextLiveData

    private val _tabPosition = MutableLiveData<Int>()
    val tabPosition: LiveData<Int>
        get() = _tabPosition

    var source = ""
    private var isVoiceSearch: Boolean = false
    var searchTrigger: String? = null
    private var liveOrder: Boolean = false
    var classSelectedForSearch: ClassListViewItem? = null

    private var searchResultEntity: NewSearchDataEntity? = null

    fun getTrendingSearchResult() {
        liveClassDataFirstTime = null
        compositeDisposable + getTrendingSearchUseCase
            .execute(GetTrendingSearchUseCase.Params(false, source,isTrendingChapterEnabled,isVideoQueryChangeEnabled))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onTrendingSearchSuccess, this::onTrendingSearchError)
    }

    fun getUserSearchNewResults(
        text: String,
        isVoiceSearch: Boolean,
        liveOrder: Boolean,
        appliedFilterMap: HashMap<String, Any>? = null
    ) {
        this.isAdvancedSerachDataRequest = false
        this.advancedFilterTabType = ""
        searchedText = text
        this.isVoiceSearch = isVoiceSearch
        this.liveOrder = liveOrder
        this.appliedFilterMap = appliedFilterMap
        searchPubSub.onNext(text)
    }

    fun getAdvancedSearchNewResults(
            text: String,
            isVoiceSearch: Boolean,
            liveOrder: Boolean,
            appliedFilterMap: HashMap<String, Any>? = null,
            advancedFilterTabType : String
    ) {
        this.filteredFacet = null
        this.isAdvancedSerachDataRequest = true
        this.searchedText = text
        this.isVoiceSearch = isVoiceSearch
        this.liveOrder = liveOrder
        this.appliedFilterMap = appliedFilterMap
        this.advancedFilterTabType = advancedFilterTabType
        filterSearchPubSub.onNext(text)

    }

    fun getSearchSuggestions(text: String, newSuggester: Boolean = true) {
        searchedText = text
        if (text.isNotEmpty()) {
            when {
                (isSuggesterEnabled.toInt() == 0 || source.equals("LibraryFragmentHome")) -> {
                    searchPubSub.onNext(text)
                }
                isSuggesterEnabled.toInt() == 1 -> {
                    publishSubject.onNext(text)
                }
                else -> {
                    newSuggestionsPubSub.onNext(text)
                }
            }
        }
    }


    private fun onTrendingSearchSuccess(searchDataList: List<TrendingSearchDataListEntity>) {
        var filterData = searchDataList
        setupLocalRecentSearch()
        if (recentSearches.isNullOrEmpty()) {
            filterData = searchDataList.filter { it.dataType != "recent" }
        }
        _trendingSearchLiveData.value = Outcome.loading(false)
        _trendingSearchLiveData.value = Outcome.success(filterData.map {
            if (it.dataType == "recent" && recentSearches.isNotEmpty()) {
                (it.playlist as ArrayList).clear()
                (it.playlist as ArrayList).addAll(getRecentSearchSuggestions(it))
            }
            trendingSearchViewItemMapper.map(it)
        })
    }

    fun getFragmentListWithNewSearchData(
        searchDataItem: NewSearchDataItem,
        isSingleTab: Boolean
    ): TempDataModel {
        tabslist = searchDataItem.tabsList
        val fragmentList = mutableListOf<Fragment>()
        allTabDataList = searchDataItem.allDataList

        tabMap.clear()
        for (tab in tabslist) {
            tabMap.put(tab.key, tab)
        }
        if (!isSingleTab) {
            fragmentList.add(getFragment(searchDataItem.allDataList, false, tabslist[0].key, null))
        }

        searchDataItem.categorizedDataList.forEach { data ->
            when (data.tabType) {
                "book" -> fragmentList.add(
                    getFragmentForBooks(
                        data.dataList,
                        false,
                        data.tabType,
                        data.chapterDetails
                    )
                )
                "youtube" -> fragmentList.add(InAppYoutubeSearchFragment.newInstance(data.tabType))
                else -> fragmentList.add(
                    getFragment(
                        data.dataList,
                        false,
                        data.tabType,
                        data.chapterDetails
                    )
                )
            }
        }
        return TempDataModel(emptyList(), fragmentList)
    }

    private fun getFragmentForBooks(
        dataList: List<SearchListViewItem>,
        isTrending: Boolean,
        tabKey: String,
        chapterDetails: ChapterDetails?
    ): Fragment =
        InAppSearchFragment.newInstance(dataList, tabKey, isTrending, chapterDetails, true)


    fun getTrendingFragmentList(trendingSearchData: List<TrendingSearchDataListViewItem>): MutableList<Fragment> {
        val fragmentList = mutableListOf<Fragment>()
        fragmentList.add(InAppSearchTrendingDataFragment.newInstance(trendingSearchData))
        return fragmentList
    }
    

    private fun getFragment(
        searchList: List<SearchListViewItem>,
        isTrending: Boolean,
        tabKey: String,
        chapterDetails: ChapterDetails?
    ) =
        InAppSearchFragment.newInstance(searchList, tabKey, isTrending, chapterDetails)

    private fun onTrendingSearchError(error: Throwable) {
        _trendingSearchLiveData.value = Outcome.loading(false)
        _searchSuggestionsLiveData.value = Outcome.loading(false)

        _trendingSearchLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }

        _searchSuggestionsLiveData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        logException(error)
    }


    private fun onUserSearchError(error: Throwable) {
        _userNewSearchLiveData.value = Outcome.loading(false)

        _userNewSearchLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                || error is NullPointerException
                || error is ClassCastException
                || error is FormatException
                || error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    private fun onUserSearchNewSuccess(searchDataEntity: NewSearchDataEntity) {
        _userNewSearchLiveData.value = Outcome.loading(false)
        newSearchViewItemMapper.youtubeFeatureEnabled = isYoutubeFeatureEnabled
        newSearchViewItemMapper.source = source
        _userNewSearchLiveData.value =
            Outcome.success(newSearchViewItemMapper.map(searchDataEntity))
    }


    private fun onSearchSuggestionSuccess(searchSuggestionDataEntity: SearchSuggestionsDataEntity) {

        _searchSuggestionsLiveData.value = Outcome.loading(false)
        _searchSuggestionsLiveData.value =
            Outcome.success(searchSuggestionItemMapper.map(searchSuggestionDataEntity))
    }

    private fun onNewSearchSuggestionSuccess(searchSuggestionDataEntity: NewSearchSuggestionDataEntity) {
        searchResultEntity = searchSuggestionDataEntity.searchResultEntity
        _searchSuggestionsLiveData.value = Outcome.loading(false)
        _searchSuggestionsLiveData.value = Outcome.success(
            SearchSuggestionsDataItem(
                newSearchSuggestionsItemMapper.map(searchSuggestionDataEntity).suggestionsList
            )
        )
    }


    fun postMongoEvent(paramsMap: HashMap<String, Any>) {
        if (paramsMap.get(EventConstants.EVENT_TYPE)?.equals(mLastEventType) == true
            && (System.currentTimeMillis() - mLastClickTime < dataLogsdelay )
        ) {
            return
        }
        mLastEventType = paramsMap.get(EventConstants.EVENT_TYPE).toString()
        mLastClickTime = System.currentTimeMillis()
        if (paramsMap.get(EventConstants.SOURCE) == null) {
            paramsMap.put(EventConstants.SOURCE, source)
        }
        if (paramsMap.get(EventConstants.FACET) == null) {
            paramsMap.put(EventConstants.FACET, "SLP")
        }

        if (paramsMap.get(EventConstants.SECTION) == null) {
            paramsMap.put(EventConstants.SECTION, "SLP")
        }
        val searchIdValue = if (!TextUtils.isEmpty(searchId)) searchId else generateSearchId()
        paramsMap.put(EventConstants.SEARCH_ID, searchIdValue)
        postMongoEventUseCase
            .execute(PostMongoEventUseCase.Param(paramsMap))
            .applyIoToMainSchedulerOnCompletable()
            .subscribe()
    }

    fun postSuggestionClickEvent(
        data: SearchSuggestionItem,
        clickedItem: String,
        id: String,
        version: String,
        itemPosition: Int,
        suggestionsListSize: Int
    ) {
        searchId = generateSearchId()
        val paramsMap = hashMapOf<String, Any>().apply {
            put("searchText", searchedText)
            put(Constants.DATA, clickedItem)
            put(EventConstants.SIZE, suggestionsListSize)
            put("id", id)
            put(EventConstants.ITEM_POSITION, itemPosition)
            put(EventConstants.CLICKED_POSITION, itemPosition)
            put("ias_suggestion_itearartion", version)
        }
        postSuggestionClickDataUseCase
            .execute(PostSuggestionClickDataUseCase.Param(paramsMap))
            .applyIoToMainSchedulerOnCompletable()
            .subscribe()

        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_TRIGGER_SUGGESTION_RECOMMENDATION)
            put(EventConstants.SEARCHED_TEXT, searchedText)
            put(EventConstants.SEARCHED_ITEM, clickedItem)
            put(EventConstants.SIZE, suggestionsListSize)
            put(EventConstants.CLICKED_POSITION, itemPosition)
            put(EventConstants.CLICKED_ITEM_TYPE, "search_suggestion")
            put(EventConstants.CLICKED_ITEM, clickedItem)
            put(Constants.DATA, Gson().toJson(data))
            put(EventConstants.CLICKED_ITEM_ID, data.id)
            put(EventConstants.IS_CLICKED, false)
            put(EventConstants.SOURCE, source)
            put(EventConstants.SUGGESTER_VARIANT_ID, data.variantId)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        })
    }

    fun postResultTabSwitch(resultsText: String, dataSize: Int, tabKey: String) {
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_RESULTS_TAB_SWITCH)
            put(EventConstants.SEARCHED_TEXT, searchedText)
            put(EventConstants.SEARCHED_ITEM, searchedText)
            put(EventConstants.SIZE, dataSize)
            put(EventConstants.FACET, tabKey)
            put(EventConstants.SECTION, tabKey)
            put(EventConstants.CLICKED_ITEM_TYPE, "results_tab")
            put(EventConstants.CLICKED_ITEM, resultsText)
            put(EventConstants.IS_CLICKED, true)
            put(EventConstants.SOURCE, source)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        })
    }

    fun postIasLaunchEvent() {
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.IN_APP_SEARCH_ACTIVITY_STARTED)
            put(EventConstants.SOURCE, source)
            put(
                    EventConstants.VARIANT_ID,
                    FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        })
    }

    fun postIasExitEvent() {
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.IN_APP_SEARCH_ACTIVITY_EXIT)
            put(EventConstants.SOURCE, source)
            put(
                    EventConstants.VARIANT_ID,
                    FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        })
    }

    fun postAdvanceFilterAppliedEvent() {
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.IN_APP_SEARCH_ADVANCED_FILTER_APPLIED)
            put(EventConstants.SOURCE, source)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
            put(EventConstants.CLASS_FILTER, filterOptionsMap.get("class").toString())
            put(EventConstants.SUBJECT_FILTER, filterOptionsMap.get("subject").toString())
            put(EventConstants.CHAPTER_FILTER, filterOptionsMap.get("chapter").toString())
            put(EventConstants.BOOK_NAME_FILTER, filterOptionsMap.get("book").toString())
            put(EventConstants.AUTHOR_FILTER, filterOptionsMap.get("author").toString())
            put(EventConstants.PUBLICATION_FILTER, filterOptionsMap.get("publication").toString())
            put(EventConstants.BOARD_FILTER, filterOptionsMap.get("board").toString())
            put(EventConstants.EXAM_FILTER, filterOptionsMap.get("exam").toString())
            put(EventConstants.TEACHER_FILTER, filterOptionsMap["teacher"].toString())
        })
    }

    fun postPopularOnDoubtNutEvent(itemClickedName: String){
        postMongoEvent(hashMapOf<String,Any>().apply {
            put(EventConstants.EVENT_TYPE,EventConstants.IN_APP_SEARCH_POPULAR_ON_DOUBTNUT_CLICKED)
            put(EventConstants.CLICKED_ITEM,itemClickedName)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        })
    }

    fun recentSearchClicked(pair: Pair<String, Boolean>) {
        searchTrigger =
            if (pair.second) EventConstants.SEARCH_TRIGGER_RECENT_SEARCH else EventConstants.SEARCH_TRIGGER_TRENDING_TOPICS
        _recentSearchClicked.value = pair
    }

    fun topTagSubjectClicked(action: NewTrendingSubjectClicked) {
        searchId = generateSearchId()
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_TRIGGER_TOP_TAGS)
            put(EventConstants.SOURCE, source)
            put(EventConstants.IS_CLICKED, false)

            put(EventConstants.SEARCHED_TEXT, action.data.searchKey)
            put(EventConstants.SEARCHED_ITEM, action.data.searchKey)
            put(EventConstants.SIZE, action.resultCount)
            put(EventConstants.CLICKED_POSITION, action.position + 1)

            put(Constants.DATA, Gson().toJson(action.data))
            put(EventConstants.CLICKED_ITEM, action.data.display)
            put(EventConstants.CLICKED_ITEM_TYPE, action.data.dataType)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )

        })
        searchTrigger = EventConstants.SEARCH_TRIGGER_TOP_TAGS
        _topTagSubjectClicked.value =
            Triple(action.data.searchKey, action.data.tabType, action.data.liveOrder)
    }

    private fun setUpSuggesterObserver() {
        compositeDisposable + publishSubject
            .debounce(TYPING_DELAY_SUGGESTER, TimeUnit.MILLISECONDS)
            .filter {
                it.isNotEmpty()
            }
            .switchMap {
                getGlobalSearchSuggestionsUseCase
                    .execute(
                        GetGlobalSearchSuggestionsUseCase.Params(
                            it,
                            suggesterPayloadMap.apply {
                                suggesterPayloadMap?.put(
                                    EventConstants.VARIANT_ID,
                                    FeaturesManager.getVariantId(
                                        DoubtnutApp.INSTANCE,
                                        Features.IAS_AUTO_SUGGESTER
                                    )
                                )
                            },
                            source
                        )
                    )
                    .doOnSubscribe { _ ->
                        inAppSearchEventManager.onSearchInitiated(source, it)
                    }
                    .toObservable()
                    .subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onSearchSuggestionSuccess, this::onTrendingSearchError)
            .also {
                compositeDisposable.add(it)
            }
    }

    private fun setUpNewSuggesterObserver() {
        compositeDisposable + newSuggestionsPubSub
            .debounce(TYPING_DELAY_SUGGESTER, TimeUnit.MILLISECONDS)
            .filter {
                it.isNotEmpty()
            }
            .switchMap {
                val suggesterObserver = suggestorObserver(it)
                val resultObserver = searchObserver(it)
                val zipper =
                    BiFunction<NewSearchDataEntity, SearchSuggestionsDataEntity, NewSearchSuggestionDataEntity> { t1, t2 ->
                        NewSearchSuggestionDataEntity(t2.suggestionsList, t1)
                    }
                Observable.zip(resultObserver, suggesterObserver, zipper).doOnSubscribe { _ ->
                    inAppSearchEventManager.onSearchInitiated(source, it)
                }.subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onNewSearchSuggestionSuccess(it)
            }, {
                onTrendingSearchError(it)
            })
            .also {
                compositeDisposable.add(it)
            }
    }

    private fun suggestorObserver(it: String) =
        getGlobalSearchSuggestionsUseCase
            .execute(GetGlobalSearchSuggestionsUseCase.Params(it, suggesterPayloadMap.apply {
                suggesterPayloadMap?.put(
                    EventConstants.VARIANT_ID,
                    FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_AUTO_SUGGESTER)
                )
            }, source))
            .doOnSubscribe { _ ->
                inAppSearchEventManager.onSearchInitiated(source, it)
            }
            .toObservable()


    private fun searchObserver(it: String): Observable<NewSearchDataEntity>? {
        val shouldSendClassFilter =
            source == LibraryFragmentHome.TAG || source == HomeFeedFragmentV2.TAG
        val trigger = searchTrigger
        searchTrigger = null
        return getGlobalSearchUseNewCase
            .execute(
                GetGlobalSearchNewUseCase.Params(
                    it,
                    if (shouldSendClassFilter) classSelectedForSearch?.classNo?.toString()
                        ?: "" else "",
                    isVoiceSearch,
                    liveOrder = liveOrder,
                    searchTrigger = trigger,
                    appliedFilterMap = appliedFilterMap, source,
                        filterOptionsMap,
                        advancedFilterTabType
                )
            )
            .doOnSubscribe { _ ->
                inAppSearchEventManager.onSearchInitiated(source, it)
            }
            .toObservable()

    }


    private fun setUpSearchObserver() {
        compositeDisposable + searchPubSub
            .debounce(TYPING_DELAY, TimeUnit.MILLISECONDS)
            .filter {
                it.isNotEmpty()
            }
            .switchMap {
                val shouldSendClassFilter =
                    source == LibraryFragmentHome.TAG || source == HomeFeedFragmentV2.TAG
                val trigger = searchTrigger
                searchTrigger = null
                getGlobalSearchUseNewCase
                    .execute(
                        GetGlobalSearchNewUseCase.Params(
                            it,
                            if (shouldSendClassFilter) classSelectedForSearch?.classNo?.toString()
                                ?: "" else "",
                            isVoiceSearch,
                            liveOrder = liveOrder,
                            searchTrigger = trigger,
                            appliedFilterMap = appliedFilterMap, source,
                                null,
                                advancedFilterTabType
                        )
                    )
                    .doOnSubscribe { _ ->
                        inAppSearchEventManager.onSearchInitiated(source, it)
                    }
                    .toObservable()
                    .subscribeOn(Schedulers.io())
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onUserSearchNewSuccess, this::onUserSearchError)
    }

    private fun setUpFilterSearchObserver() {
        compositeDisposable + filterSearchPubSub
                .switchMap {
                    val shouldSendClassFilter =
                            source == LibraryFragmentHome.TAG || source == HomeFeedFragmentV2.TAG
                    val trigger = searchTrigger
                    searchTrigger = null
                    getGlobalSearchUseNewCase
                            .execute(
                                    GetGlobalSearchNewUseCase.Params(
                                            it,
                                            if (shouldSendClassFilter) classSelectedForSearch?.classNo?.toString()
                                                    ?: "" else "",
                                            isVoiceSearch,
                                            liveOrder = liveOrder,
                                            searchTrigger = trigger,
                                            appliedFilterMap = appliedFilterMap, source,
                                            filterOptionsMap,
                                            advancedFilterTabType
                                    )
                            )
                            .doOnSubscribe { _ ->
                                inAppSearchEventManager.onSearchInitiated(source, it)
                            }
                            .toObservable()
                            .subscribeOn(Schedulers.io())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onUserSearchNewSuccess, this::onUserSearchError)
    }

    fun sendSearchClickEvent(
        source: String,
        searchedItem: String,
        clickedItem: String,
        clickedDataTitle: String,
        itemId: String,
        section: String,
        type: String,
        position: Int,
        isAllTab: Boolean,
        selectedTab: String,
        selectedTabPosition: Int,
        isToppersChoice: Boolean,
        resultCount: Int = 0,
        assortmentId : String
    ) {
        val absolutePosition =
            if (isAllTab) getAbsolutePosition(clickedDataTitle) else position
        val sendSection: String = if (isAllTab) getSection(clickedDataTitle) else section
        addClickedItemsToLastSession(
            searchedItem,
            clickedItem,
            itemId,
            absolutePosition,
            selectedTab,
            selectedTabPosition,
            isToppersChoice
        )
        val sendItemId =
            if (selectedTab == "Youtube") "https://www.youtube.com/watch?v=" + itemId else itemId
        val itemType = if (selectedTab == "Youtube") type + " video" else type
        isSearchResultClicked = true
        inAppSearchEventManager.sendSearchEvent(
            EventConstants.SEARCH_CLICK_EVENT,
            itemId,
            source,
            searchedItem,
            clickedItem,
            sendSection,
            type,
            absolutePosition,
            assortmentId
        )
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_RESULT_CLICKED)
            put(EventConstants.SEARCHED_TEXT, searchedItem)
            put(EventConstants.SEARCHED_ITEM, searchedItem)
            put(EventConstants.FACET, selectedTab)
            put(EventConstants.SECTION, sendSection)
            put(EventConstants.SIZE, resultCount)
            put(EventConstants.CLICKED_POSITION, absolutePosition)
            put(EventConstants.CLICKED_ITEM_TYPE, itemType)
            put(EventConstants.CLICKED_ITEM, clickedDataTitle)
            put(EventConstants.CLICKED_ITEM_ID, sendItemId)
            put(Constants.DATA, clickedItem)
            put(EventConstants.IS_CLICKED, true)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
            put(EventConstants.SOURCE, source)
        })
    }

    private fun getAbsolutePosition(clickedDataTitle: String): Int {
        var positionOfSection = 0
        for (element in allTabDataList) {
            if (element is AllSearchHeaderViewItem) {
                positionOfSection = 0
            } else {
                positionOfSection += 1
            }
            if (element is SearchPlaylistViewItem && element.display == clickedDataTitle)
                break
        }
        return positionOfSection
    }

    private fun getSection(clickedDataTitle: String): String {
        var section = ""
        for (element in allTabDataList) {
            if (element is AllSearchHeaderViewItem) {
                section = element.tabType
            }
            if (element is SearchPlaylistViewItem && element.display == clickedDataTitle)
                break
        }
        return section
    }

    fun sendKeyboardSearchEvent(source: String, searchedItem: String) {
        inAppSearchEventManager.sendKeyboardSearchEvent(
            EventConstants.SEARCH_KEYBOARD_EVENT,
            source,
            searchedItem,
            ignoreSnowplow = true
        )
    }

    fun sendVoiceSearchEvent(source: String, searchedItem: String) {
        searchId = generateSearchId()
        inAppSearchEventManager.sendKeyboardSearchEvent(
            EventConstants.SEARCH_VOICE_EVENT,
            source,
            searchedItem,
            ignoreSnowplow = true
        )
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_TRIGGER_VOICE_SEARCH)
            put(EventConstants.SEARCHED_ITEM, searchedItem)
            put(EventConstants.SEARCHED_TEXT, searchedItem)
            put(EventConstants.SIZE, 0)
            put(EventConstants.SOURCE, source)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        })
    }

    fun sendDirectSearchEvent(searchedItem: String) {
        searchId = generateSearchId()
        inAppSearchEventManager.sendKeyboardSearchEvent(
            EventConstants.SEARCH_VOICE_EVENT,
            source,
            searchedItem,
            ignoreSnowplow = true
        )
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_TRIGGER_DIRECT_SEARCH)
            put(EventConstants.SEARCHED_ITEM, searchedItem)
            put(EventConstants.SEARCHED_TEXT, searchedItem)
            put(EventConstants.SIZE, 0)
            put(EventConstants.SOURCE, source)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        })
    }

    fun sendSearchFilterAppliedEvent(
        searchedItem: String,
        appliedFilterMap: HashMap<String, String>,
        facet: String
    ) {
        inAppSearchEventManager.sendKeyboardSearchEvent(
            EventConstants.SEARCH_TRIGGER_FILTER,
            source,
            searchedItem
        )
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_TRIGGER_FILTER)
            put(EventConstants.SEARCHED_ITEM, searchedItem)
            put(EventConstants.SEARCHED_TEXT, searchedItem)
            put(EventConstants.SIZE, 0)
            put(EventConstants.FACET, facet)
            put(EventConstants.SECTION, facet)
            put(EventConstants.SOURCE, source)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        }
        for ((k, v) in appliedFilterMap) {
            params.put(k + "_filter", v)

        }
        postMongoEvent(params)
    }

    fun sendSortFilterAppliedEvent(
        searchedItem: String,
        sortFilter: SearchFilterItem,
        facet: String
    ) {
        inAppSearchEventManager.sendKeyboardSearchEvent(
            EventConstants.SEARCH_TRIGGER_FILTER,
            source,
            searchedItem
        )
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_TRIGGER_FILTER)
            put(EventConstants.SEARCHED_ITEM, searchedItem)
            put(EventConstants.SEARCHED_TEXT, searchedItem)
            put(EventConstants.SIZE, 0)
            put(EventConstants.FACET, facet)
            put(EventConstants.SECTION, facet)
            put(EventConstants.SOURCE, source)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        }

        params.put("sort_filter", sortFilter.value)
        params.put("order", sortFilter.order.toString())

        postMongoEvent(params)
    }

    fun sendTrendingRecentMongoClickEvent(action: TrendingPlaylistMongoEvent) {
        searchId = generateSearchId()
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(
                EventConstants.EVENT_TYPE,
                if (action.isRecentSearch) EventConstants.SEARCH_TRIGGER_RECENT_SEARCH else EventConstants.SEARCH_TRIGGER_TRENDING_TOPICS
            )
            put(EventConstants.SOURCE, source)
            put(EventConstants.IS_CLICKED, false)

            put(EventConstants.SEARCHED_TEXT, action.data.display)
            put(EventConstants.SEARCHED_ITEM, action.data.display)
            put(EventConstants.SIZE, action.resultCount)
            put(EventConstants.CLICKED_POSITION, (action.itemPosition + 1))

            put(Constants.DATA, Gson().toJson(action.data))
            put(EventConstants.CLICKED_ITEM, action.data.display)
            put(EventConstants.CLICKED_ITEM_TYPE, action.data.type)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )

        })
    }

    fun sendTrendingClickEvent(
        source: String,
        menu: String,
        clickedItem: String,
        type: String,
        position: Int,
        isRecentSearch: Boolean
    ) {
        val event =
            if (isRecentSearch) EventConstants.TRENDING_CLICK_RECENT else EventConstants.TRENDING_CLICK_EVENT
        inAppSearchEventManager.sendTrendingClickEvent(
            event,
            source,
            menu,
            clickedItem,
            type,
            searchLandingVersion,
            (position + 1).toString()
        )
    }


    fun sendTrendingBookClickEvent(source: String, action: TrendingBookClicked) {
        searchId = generateSearchId()
        var event = action.data.eventType.orEmpty()
        inAppSearchEventManager.sendTrendingClickEvent(
            event,
            source,
            action.data.type,
            action.data.display,
            action.data.type,
            searchLandingVersion,
            (action.itemPosition + 1).toString()
        )
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, event)
            put(EventConstants.SOURCE, source)
            put(EventConstants.IS_CLICKED, false)

            put(EventConstants.SEARCHED_TEXT, action.data.display)
            put(EventConstants.SEARCHED_ITEM, action.data.display)
            put(EventConstants.SIZE, action.resultCount)
            put(EventConstants.CLICKED_POSITION, action.itemPosition + 1)

            put(Constants.DATA, Gson().toJson(action.data))
            put(EventConstants.CLICKED_ITEM, action.data.display)
            put(EventConstants.CLICKED_ITEM_TYPE, action.data.type)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        })
    }

    fun sendTrendingCourseClickEvent(source: String, action: TrendingCourseClicked) {
        searchId = generateSearchId()
        var event = action.data.eventType.orEmpty()

        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, event)
            put(EventConstants.SOURCE, source)
            put(EventConstants.IS_CLICKED, true)
            put(EventConstants.CLICKED_ITEM_TYPE, action.data.type)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        })
    }

    fun sendMostRecentDoubtClicked(action: NewTrendingRecentDoubtClicked) {
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.EVENT_MOST_RECENT_DOUBT_CLICKED)
            put(EventConstants.SOURCE, source)
            put(EventConstants.IS_CLICKED, true)

            put(EventConstants.SEARCHED_TEXT, action.data.ocrText)
            put(EventConstants.SEARCHED_ITEM, action.data.ocrText)
            put(EventConstants.SIZE, action.resultCount)
            put(EventConstants.CLICKED_POSITION, action.position + 1)

            put(Constants.DATA, Gson().toJson(action.data))
            put(EventConstants.CLICKED_ITEM, action.data.ocrText)
            put(EventConstants.CLICKED_ITEM_ID, action.data.questionId)
            put(EventConstants.CLICKED_ITEM_TYPE, action.data.type)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )

        })
    }

    fun sendNoDataEvent(source: String, searchedItem: String) {
        inAppSearchEventManager.sendNoDataFoundEvent(
            EventConstants.NO_DATA_EVENT,
            source,
            searchedItem,
            ignoreSnowplow = true
        )
    }

    fun sendSearchLandingClickEvent(
        eventName: String,
        itemName: String,
        position: Int,
        searchLandingVersion: Int
    ) {
        inAppSearchEventManager.sendNewLandingPageClickEvent(
            eventName,
            source,
            itemName,
            (position + 1).toString(),
            searchLandingVersion
        )
    }

    fun sendEvent(event: String, params: HashMap<String, Any>, searchLandingVersion: Int, ignoreSnowplow: Boolean = false) {
        params.put(IN_APP_SEARCH_LANDING_VERSION, searchLandingVersion)
        inAppSearchEventManager.eventWith(event, params, ignoreSnowplow = ignoreSnowplow)
    }

    fun publishEvent(analyticsEvent: AnalyticsEvent) {
        inAppSearchEventManager.eventWith(analyticsEvent)
    }

    fun getClassesList(): ArrayList<ClassListViewItem> {
        return classesListData
    }

    fun setClassesList(list: ArrayList<ClassListViewItem> = classesListData) {

        if (classSelectedForSearch != null) {
            var indexOfCurrentClass = -1
            for (it in list) {
                if (it.classNo == classSelectedForSearch?.classNo) {
                    indexOfCurrentClass = list.indexOf(it)
                    break
                }
            }
            if (indexOfCurrentClass >= 0) {
                val currentClassItem = list[indexOfCurrentClass]
                list.removeAt(indexOfCurrentClass)
                Collections.sort(list, kotlin.Comparator { o1, o2 ->
                    return@Comparator o2.classNo.compareTo(o1.classNo)
                })
                list.add(0, currentClassItem)
            }
        }
        classesListData = list
    }

    fun moveToSelectedTab(action: Any?) {
        _seeAllClicked.value =
            tabslist.indexOfFirst { it.key == (action as SeeAllSearchResults).tabType }
    }

    fun sendSeeAllClickEvent(action: SeeAllSearchResults) {
        sendEvent(EventConstants.INAPP_SEARCH_CLICK_SEE_ALL, hashMapOf<String, Any>().apply {
            put(EventConstants.CLICKED_ITEM, action.category)
        }, searchLandingVersion, ignoreSnowplow = true)
    }

    fun addSession(data: SearchSessionModel) {
        searchSessionsList.add(data)
    }

    fun addEventsToLastSession(eventType: String) {
        if (searchSessionsList.isNotEmpty())
            searchSessionsList.last().eventTypes.add(eventType)
    }

    fun updateIsSearched(isSearched: Boolean) {
        if (searchSessionsList.isNotEmpty())
            searchSessionsList.last().isSearched = isSearched
    }


    private fun updateIsMatched(isMatched: Boolean) {
        if (searchSessionsList.isNotEmpty())
            searchSessionsList.last().isMatched = isMatched
    }

    fun handleLastSearchSession(lastSearchQuery: String) {
        if (searchSessionsList.isEmpty())
            return
        addEventsToLastSession(EventConstants.EVENT_IN_APP_SERACH_EMPTY_BACK)
        if (!searchSessionsList.last().isMatched)
            searchSessionsList.last().apply {
                searchedText = lastSearchQuery
                isSearched = true
            }
    }

    private fun addClickedItemsToLastSession(
        searchQuery: String, item: String, itemId: String, position: Int,
        tabName: String, tabPosition: Int, toppersChoice: Boolean
    ) {
        if (searchSessionsList.isNotEmpty())
            if (searchSessionsList.last().isMatched) {
                val session = SearchSessionModel(
                    uscId = searchSessionsList.last().uscId,
                    eventTypes = searchSessionsList.last().eventTypes,
                    isMatched = true,
                    isSearched = true,
                    timeStamp = System.currentTimeMillis(),
                    size = 0,
                    searchedText = searchQuery,
                    clickedItem = item,
                    clickedUniqueItemId = itemId,
                    selectedItemPosition = position,
                    selectedTabName = tabName,
                    selectedTabPosition = tabPosition,
                    isToppersChoice = toppersChoice
                )
                searchSessionsList.add(session)
            } else {
                addEventsToLastSession(EventConstants.SEARCH_CLICK_EVENT)
                searchSessionsList.last().apply {
                    isMatched = true
                    isSearched = true
                    clickedItem = item
                    clickedUniqueItemId = itemId
                    selectedItemPosition = position
                    selectedTabName = tabName
                    selectedTabPosition = tabPosition
                    isToppersChoice = toppersChoice
                }
            }

    }

    fun postPreviousSessions() {
        searchSessionsList
            .filter {
                it.isSearched != null
            }.forEach {
                analyticsPublisher.publishEvent(
                    StructuredEvent(
                        "inAppSearch",
                        "USCLogs",
                        null,
                        null,
                        null,
                        hashMapOf<String, Any>().apply {
                            put(
                                EventConstants.USC_ID,
                                "${it.uscId}_${userPreference.getUserStudentId()}"
                            )
                            put(EventConstants.CREATED_AT, getFormattedTime(it.timeStamp))
                            put(EventConstants.EVENT_TYPE, it.eventTypes)
                            put(EventConstants.CLICKED_ITEM, it.clickedItem)
                            put(EventConstants.CLICKED_UNIQUE_IDS, it.clickedUniqueItemId)
                            put(EventConstants.SEARCHED_TEXT, it.searchedText)
                            put(EventConstants.SIZE, it.size)
                            put(EventConstants.SOURCE, source)
                            put(EventConstants.IS_SEARCHED, it.isSearched!!)
                            put(EventConstants.IS_MATCHED, it.isMatched)
                            put(EventConstants.USC_LOGGING_VERSION, Constants.USC_LOGGING_VERSION)
                            put(EventConstants.USC_TAB_POSITION, it.selectedTabPosition)
                            put(EventConstants.USC_TAB_NAME, it.selectedTabName)
                            put(EventConstants.USC_ITEM_POSITION, it.selectedItemPosition)
                            put(EventConstants.IAS_TOPPERS_CHOICE, it.isToppersChoice)
                        })
                )
            }
        searchSessionsList.clear()
    }

    private fun getFormattedTime(timeStamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timeStamp
        return DateFormat.format("yyyy-MMM-dd HH:mm:ss", calendar).toString()
    }

    fun fetchSearchHints() {
        compositeDisposable + DataHandler.INSTANCE.searchRepository
            .getSearchHints()
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _searchHints.value = it.data
            })
    }

    fun getSearchHints(studentClass: String): List<String>? {
        val map = _searchHints.value ?: return null
        val list = map[studentClass]?.toMutableList() ?: return null
        list.shuffle()
        return if (list.size <= 3)
            list
        else
            list.subList(0, 3)
    }

    fun handleAction(action: Any, searchQuery: String = "") {
        when (action) {
            is PlayVideo -> {
                openVideoScreen(action)
                updateUSCLog(action.videoId, searchQuery)
            }
            is SearchTopResultClicked -> {
                postMongoEvent(hashMapOf<String, Any>().apply {
                    put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_TRIGGER_TOP_RESULT)
                    put(EventConstants.SEARCHED_TEXT, searchQuery)
                    put(EventConstants.SEARCHED_ITEM, searchQuery)
                    put(EventConstants.FACET, "SLP")
                    put(EventConstants.SECTION, "TopResult")
                    put(EventConstants.SIZE, 3)
                    put(EventConstants.CLICKED_POSITION, action.itemPosition - 3)
                    put(EventConstants.CLICKED_ITEM_TYPE, action.playlist.type)
                    put(EventConstants.CLICKED_ITEM, action.playlist.display)
                    put(EventConstants.CLICKED_ITEM_ID, action.playlist.id)
                    put(Constants.DATA, Gson().toJson(action.playlist))
                    put(EventConstants.IS_CLICKED, true)
                    put(EventConstants.SOURCE, source)
                    put(
                        EventConstants.VARIANT_ID,
                        FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
                    )
                })
            }
            is SeeAllSearchResults -> {
                searchId = generateSearchId()
                _tabPosition.value = action.tabPosition
                postMongoEvent(hashMapOf<String, Any>().apply {
                    put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_TRIGGER_TOP_RESULT_SEE_ALL)
                    put(EventConstants.SEARCHED_ITEM, searchQuery)
                    put(EventConstants.SEARCHED_TEXT, searchQuery)
                    put(EventConstants.SIZE, 0)
                    put(EventConstants.IS_CLICKED, false)
                    put(EventConstants.SOURCE, source)
                    put(
                        EventConstants.VARIANT_ID,
                        FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
                    )
                })
                handleSuggestionsSeeAll()
            }

            is CourseBannerClicked -> {
                postMongoEvent(hashMapOf<String, Any>().apply {
                    put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_COURSE_BANNER_CLICKED)
                    put(EventConstants.SEARCHED_ITEM, searchQuery)
                    put(EventConstants.SEARCHED_TEXT, searchQuery)
                    put(EventConstants.SIZE, 0)
                    put(EventConstants.IS_CLICKED, true)
                    put(EventConstants.SOURCE, source)
                    put(
                        EventConstants.VARIANT_ID,
                        FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
                    )
                })
            }

            is AdvancedFilterClicked -> {
               getFilterList(action.tabType)
            }

            is OpenLibraryVideoPlayListScreen -> {
                openPlayListScreen(action)
                updateUSCLog(action.playlistId, searchQuery)
            }

            is OpenLibraryPlayListActivity -> {
                openLibraryListingActivity(action)
                updateUSCLog(action.playlistId, searchQuery)
            }
            else -> {
            }
        }

    }


    private fun handleSuggestionsSeeAll() {
        if (searchResultEntity != null) {
            onUserSearchNewSuccess(searchResultEntity!!)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.INAPP_SEARCH_CLICK_SEE_ALL,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.TYPE, "new_suggester_page")
                    }, ignoreSnowplow = true)
            )
        }
    }

    private fun updateUSCLog(itemId: String, searchQuery: String) {
        addEventsToLastSession(EventConstants.EVENT_NAME_IN_APP_SEARCH_SUGGESTION_CLICK)
        updateIsMatched(true)
        updateIsSearched(true)
        addClickedItemsToLastSession(searchQuery, "", itemId, 0, "", 0, false)
    }

    private fun openVideoScreen(action: PlayVideo) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.SEARCH_CLICK_EVENT,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.TYPE, "new_suggester_page")
                    put(EventConstants.CLICKED_ITEM, action.categoryTitle)
                    put(EventConstants.ITEM_ID, action.videoId)
                })
        )
        val screen = when (action.resourceType) {
            SOLUTION_RESOURCE_TYPE_VIDEO -> VideoScreen
            "youtube" -> VideoYouTubeScreen
            "liveclass" -> LiveClassesScreen
            "livevideo" -> VideoScreen
            else -> TextSolutionScreen
        }
        _navigateLiveData.value = Event(
            NavigationModel(
                screen, hashMapOf(
                    Constants.PAGE to action.page,
                    Constants.QUESTION_ID to action.videoId,
                    Constants.PARENT_ID to 0,
                    Constants.PLAYLIST_ID to action.playlistId
                )
            )
        )
    }

    private fun openPlayListScreen(action: OpenLibraryVideoPlayListScreen) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.SEARCH_CLICK_EVENT,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.TYPE, "new_suggester_page")
                    put(EventConstants.CLICKED_ITEM, action.playlistName)
                    put(EventConstants.ITEM_ID, action.playlistId)
                })
        )
        _navigateLiveData.value = Event(
            NavigationModel(
                LibraryVideoPlayListScreen, hashMapOf(
                    SCREEN_NAV_PARAM_PLAYLIST_ID to action.playlistId,
                    SCREEN_NAV_PARAM_PLAYLIST_TITLE to (action.playlistName),
                    Constants.PAGE to Constants.PAGE_SEARCH_SRP
                )
            )
        )
    }

    private fun openLibraryListingActivity(action: OpenLibraryPlayListActivity) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.SEARCH_CLICK_EVENT,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.TYPE, "new_suggester_page")
                    put(EventConstants.CLICKED_ITEM, action.playlistName)
                    put(EventConstants.ITEM_ID, action.playlistId)
                })
        )
        _navigateLiveData.value = Event(
            NavigationModel(
                LibraryPlayListScreen, hashMapOf(
                    LibraryListingActivity.INTENT_EXTRA_PLAYLIST_ID to action.playlistId,
                    LibraryListingActivity.INTENT_EXTRA_PACKAGE_ID to action.packageDetailsId.orEmpty(),
                    LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE to action.playlistName,
                    LibraryListingActivity.INTENT_EXTRA_PLAYLIST_PAGE to Constants.PAGE_SEARCH_SRP
                )
            )
        )
    }

    private fun setupLocalRecentSearch() {
        var recentSearchesSet = defaultPrefs().getString(Constants.RECENT_SEARCHES, "")
        val gson = Gson()
        val type = object : TypeToken<List<String?>?>() {}.type
        val list: ArrayList<String>? = gson.fromJson(recentSearchesSet, type)
        if (!list.isNullOrEmpty()) {
            recentSearches.clear()
            recentSearches.addAll(list)
        }
    }

    fun addRecentSearchText(text: String) {
        if (!recentSearches.contains(text)) {
            if (recentSearches.size == 3) {
                recentSearches.removeAt(recentSearches.size - 1)
            }
            recentSearches.add(0, text)
            defaultPrefs().edit()
                .putString(Constants.RECENT_SEARCHES, getRecentSearchSet(recentSearches)).commit()
        }
    }

    private fun getRecentSearchSet(recentSearches: ArrayList<String>): String {
        val str = JSONArray(recentSearches).toString()
        return str
    }

    private fun getRecentSearchSuggestions(trendingSearchDataListEntity: TrendingSearchDataListEntity?): ArrayList<SearchSuggestionsFeedItem> {
        var iconImageUrl = trendingSearchDataListEntity?.itemImageUrl
        if (iconImageUrl.isNullOrEmpty()) {
            iconImageUrl = "https://d10lpgp6xz60nq.cloudfront.net/images/ias_recent.png"
        }
        defaultPrefs().edit().putString(Constants.RECENT_SEARCHES_ICON_URL, iconImageUrl).commit()

        val list = ArrayList<SearchSuggestionsFeedItem>()
        for (searchStr in recentSearches) {
            list.add(
                TrendingAndRecentSearchEntity(
                    "recent",
                    searchStr,
                    iconImageUrl,
                    "",
                    "",
                    false,
                    false,
                    ""
                )
            )
        }
        return list
    }

    fun onFilterAction(action: Any) {
        _filterEventLiveData.value = action
    }

    private fun generateSearchId(): String {
        return "${System.currentTimeMillis()}_${UserUtil.getStudentId()}"
    }

    fun sendAllChapterClickedEvent(action: IasAllChapterClicked, tabData: SearchTabsItem) {
        val params = hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.EVENT_IAS_ALL_CHAPTER_CLICK)
            put(EventConstants.SOURCE, source)
            put(EventConstants.IS_CLICKED, true)
            put(EventConstants.FACET, tabData.description)

            put(EventConstants.SEARCHED_TEXT, searchedText)
            put(EventConstants.SEARCHED_ITEM, searchedText)
            put(EventConstants.SIZE, action.resultCount)

            put(EventConstants.CLICKED_POSITION, action.resultCount)
            put(EventConstants.CLICKED_ITEM, "View All Chapter")
            put(EventConstants.CLICKED_ITEM_TYPE, "Chapter Listing")
            put(Constants.DATA, Gson().toJson(action.chapterDetails))
            put(
                EventConstants.CLICKED_ITEM_ID,
                Uri.parse(action.chapterDetails.deeplink)
                    .getQueryParameter(Constants.ASSORTMENT_ID).orEmpty(),
            )

            put(EventConstants.SECTION, "SRP")
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        }

        val appliedFilterMap = HashMap<String, String>()
        if (!tabData.filterList.isNullOrEmpty()) {
            for (filterType in tabData.filterList!!) {
                if (filterType.isSelected) {
                    for (filterValue in filterType.filters) {
                        if (filterValue.isSelected) {
                            appliedFilterMap.put(filterType.key, filterValue.value)
                        }
                    }
                }
            }
        }
        for ((k, v) in appliedFilterMap) {
            params.put(k + "_filter", v)

        }

        postMongoEvent(params)
    }

    fun postAllChapterResultClickedEvent(action: AllChapterResultClicked) {
        postMongoEvent(hashMapOf<String, Any>().apply {
            put(EventConstants.EVENT_TYPE, EventConstants.SEARCH_RESULT_CLICKED)
            put(EventConstants.SEARCHED_TEXT, searchedText)
            put(EventConstants.SEARCHED_ITEM, searchedText)
            put(EventConstants.FACET, "Live Classes")
            put(EventConstants.SECTION, "Chapter Listing")
            put(EventConstants.SIZE, action.resultCount)
            put(EventConstants.CLICKED_POSITION, action.position + 1)
            put(EventConstants.CLICKED_ITEM_TYPE, "video")
            put(EventConstants.CLICKED_ITEM, action.item.title.orEmpty())
            put(EventConstants.CLICKED_ITEM_ID, action.item.id.orEmpty())
            put(Constants.DATA, Gson().toJson(action.item))
            put(EventConstants.IS_CLICKED, true)
            put(EventConstants.SOURCE, Constants.PAGE_SEARCH_SRP)
            put(
                EventConstants.VARIANT_ID,
                FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
            )
        })
    }

    fun getFilterList(tabType: String) {
        val requestBodyMap = HashMap<String, Any>()
        requestBodyMap["tab_type"] = tabType
        compositeDisposable + DataHandler.INSTANCE.searchRepository
                .getAdvanceSearchFilters(requestBodyMap.toRequestBody())
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    _advancedSearchResponse.value = Outcome.success(it.data)
                })
    }

    fun getFilterListOptions(tabType: String,requiredFilter : String) {
        if(requiredFilter=="class"){
            filterOptionsMap.clear()
        }
        val requestBodyMap = HashMap<String, Any>()
        requestBodyMap["tab_type"] = tabType
        requestBodyMap["required_filter"] = requiredFilter
        //ading applied filters
        requestBodyMap.putAll(filterOptionsMap)
        compositeDisposable + DataHandler.INSTANCE.searchRepository
                .getAdvanceSearchFilters(requestBodyMap.toRequestBody())
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    _filterOptionResponse.value = Outcome.success(it.data)
                })
    }
}