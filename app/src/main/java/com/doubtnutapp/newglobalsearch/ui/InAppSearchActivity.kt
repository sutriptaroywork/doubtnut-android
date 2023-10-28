package com.doubtnutapp.newglobalsearch.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.base.SearchSuggestionClicked.Companion.ACTION_ITEM_SELECTED
import com.doubtnutapp.base.SearchSuggestionClicked.Companion.ACTION_ITEM_TEXT_SUBMITTED
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.domain.library.entities.ClassListViewItem
import com.doubtnutapp.domain.newglobalsearch.entities.*
import com.doubtnutapp.newglobalsearch.model.*
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchFilterAdapter
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchPagerAdapter
import com.doubtnutapp.newglobalsearch.ui.adapter.SearchSuggestionsAdapter
import com.doubtnutapp.newglobalsearch.viewmodel.InAppSearchViewModel
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.DateUtils.isToday
import com.doubtnutapp.utils.wrapperinterface.SimpleTextWatcherListener
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.videoPage.ui.YoutubeTypeVideoActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_ias_search.*
import kotlinx.android.synthetic.main.in_app_search_fragment.*
import kotlinx.android.synthetic.main.in_app_youtube_search_fragment.*
import kotlinx.coroutines.delay
import java.util.*
import javax.inject.Inject

class InAppSearchActivity : AppCompatActivity(), View.OnClickListener, SimpleTextWatcherListener,
    HasAndroidInjector, ActionPerformer {

    private var currentPage: Int = 0

    private var busObserver: Disposable? = null

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var screenNavigator: Navigator

    private lateinit var searchResultPagerAdapter: SearchPagerAdapter
    private var searchFilterAdapter: SearchFilterAdapter? = null

    private lateinit var inAppSearchViewModel: InAppSearchViewModel

    private lateinit var eventTracker: Tracker

    private var directSearchQuery: String? = ""

    private var isAutoSuggestorEnabled: Boolean = false

    private var isUserClickedDropdown: Boolean = false

    private var isBlankSearchCycle: Boolean = true

    private var isQuickSearch: Boolean = false

    /**
     * Suggestions recycler view adapter
     */
    private lateinit var adapter: SearchSuggestionsAdapter

    private var dataSize = 0

    private var startVoiceSearch: Boolean = false
    private var isDirectSearch: Boolean = false
    private var isSearchButtonClicked: Boolean = false

    private var searchEvent: String = ""

    /**
     * It is a boolean used to manage the case if we should fetch suggestions from api or not
     * whenever text is changing in the search EditText.
     * we toggle it to false/true before setting text directly to the EditText, and checking in
     * text change listener before calling api to manage the use cases.
     */
    private var searchTextChanged: Boolean = true

    private var isBackDialogShown: Boolean = false

    private var isSearchPerformed: Boolean = false

    private var tabTitleList = arrayListOf<String>()

    private var tabList: ArrayList<SearchTabsItem> = arrayListOf<SearchTabsItem>()

    private var maxSearchQuery = ""

    private var lastPage: Int = 0

    private var redirectTab: String = ""

    private var isNewSuggester = false

    private var viewsDataList: List<TrendingSearchDataListViewItem>? = null
    private var isLiveNowTag: Boolean = false
    private var singleResult: Boolean = true
    private var noUserAction: Boolean = true

    private var feedBackDataEntity: FeedBackDataEntity? = null

    private var alertDialog: AlertDialog? = null

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    companion object {
        const val TAG = "InAppSearch"
        const val SOURCE = "source"
        const val START_VOICE_SEARCH = "start_voice_search"
        const val IS_DIRECT_SEARCH = "is_direct_search"
        const val DIRECT_SEARCH_QUERY = "direct_search_query"
        const val SELECTED_CLASS = "selected_class"
        const val CLASSES_LIST = "classes_list"
        const val QUICK_SEARCH = "quick_search"
        const val REDIRECT_TAB = "redirect_tab"
        const val REQ_CODE_SPEECH_INPUT = 100
        const val DIRECT_SEARCH_QUERY_EVENT_TYPE = "DIRECT_SEARCH_QUERY_EVENT_TYPE"

        fun startActivity(
            context: Context, source: String, startVoiceSearch: Boolean,
            selectedClass: ClassListViewItem? = null,
            classesList: ArrayList<ClassListViewItem> = ArrayList()
        ) {
            Intent(context, InAppSearchActivity::class.java).apply {
                putExtra(SOURCE, source)
                putExtra(START_VOICE_SEARCH, startVoiceSearch)
                putExtra(SELECTED_CLASS, selectedClass)
                putExtra(CLASSES_LIST, classesList)
            }.also {
                context.startActivity(it)
            }
        }

        fun getStartIntent(
            context: Context,
            source: String,
            startVoiceSearch: Boolean,
            searchQuery: String? = null,
            selectedClass: ClassListViewItem? = null,
            eventType: String? = null,
            redirectTab: String = ""
        ) =
            Intent(context, InAppSearchActivity::class.java).apply {
                putExtra(SOURCE, source)
                putExtra(START_VOICE_SEARCH, startVoiceSearch)
                putExtra(DIRECT_SEARCH_QUERY, searchQuery)
                putExtra(SELECTED_CLASS, selectedClass)
                putExtra(DIRECT_SEARCH_QUERY_EVENT_TYPE, eventType)
                putExtra(QUICK_SEARCH, !searchQuery.isNullOrEmpty())
                putExtra(REDIRECT_TAB, redirectTab)
            }

        fun startActivityForSearchResult(
            context: Context, source: String, searchQuery: String,
            startVoiceSearch: Boolean, isDirectSearch: Boolean,
            eventType: String
        ) {
            Intent(context, InAppSearchActivity::class.java).apply {
                putExtra(SOURCE, source)
                putExtra(START_VOICE_SEARCH, startVoiceSearch)
                putExtra(IS_DIRECT_SEARCH, isDirectSearch)
                putExtra(DIRECT_SEARCH_QUERY, searchQuery)
                putExtra(DIRECT_SEARCH_QUERY_EVENT_TYPE, eventType)
            }.also {
                context.startActivity(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.light_grey)
        setContentView(R.layout.activity_ias_search)
        init()
        sendEvent(EventConstants.IN_APP_SEARCH_ACTIVITY_STARTED)
        inAppSearchViewModel.postIasLaunchEvent()
        inAppSearchViewModel.suggesterPayloadMap = FeaturesManager.getFeaturePayload(
            this,
            Features.IAS_AUTO_SUGGESTER
        )

        val searchFlowVersion = if (isAutoSuggestorEnabled)
            EventConstants.OLDER_SEARCH_FLOW else EventConstants.NEWER_SEARCH_FLOW

        /**
         * ApXor/Snowplow event to identify that which of the search flow is distributed to the current user
         * as per the two i.e newer or older flow with A/B Experiment named
         * as IN_APP_SEARCH_VERSION.
         */
        inAppSearchViewModel.sendEvent(
            EventConstants.EVENT_NAME_IN_APP_SEARCH_VERSION,
            hashMapOf<String, Any>().apply {
                put(
                    EventConstants.SOURCE,
                    searchFlowVersion
                )
            }, inAppSearchViewModel.searchLandingVersion, ignoreSnowplow = true
        )

        if (startVoiceSearch) {
            closeKeyboard()
            promptSpeechInput()
        } else {
            if (isDirectSearch || isQuickSearch) {
                searchTextChanged = false
                directSearchQuery?.let {
                    getUserSearchResult(
                        it,
                        intent.getStringExtra(DIRECT_SEARCH_QUERY_EVENT_TYPE)
                            ?: EventConstants.SEARCH_KEYBOARD_EVENT,
                        redirectTab = intent.getStringExtra(REDIRECT_TAB).orEmpty()
                    )
                }
            } else
                searchKeywordInput.requestFocus()
        }
        startShimmer()
    }

    private fun startShimmer() {
        searchProgressBar?.hide()
        // for grid view type we will show different shimmer animation
        shimmerFrameLayout?.startShimmer()
        shimmerFrameLayout?.show()
    }

    private fun stopShimmer() {
        shimmerFrameLayout?.stopShimmer()
        shimmerFrameLayout?.hide()
    }

    private fun init() {
        isAutoSuggestorEnabled =
            FeaturesManager.isFeatureEnabled(this, Features.IAS_AUTO_SUGGESTER) &&
                    (defaultPrefs().getString(
                        Constants.STUDENT_LANGUAGE_CODE,
                        ""
                    ) == "en" || defaultPrefs().getString(
                        Constants.STUDENT_LANGUAGE_CODE,
                        ""
                    ) == "hi")
        isNewSuggester = if (isAutoSuggestorEnabled) {
            FeaturesManager.getVariantId(
                this,
                Features.IAS_AUTO_SUGGESTER
            ) == Constants.VARIANT_ID_NEW_SUGGESTER
        } else false

        isNewSuggester = true

        val clearIcon = if (searchKeywordInput.text?.isNotEmpty() == true) R.drawable.ic_clear
        else R.drawable.ic_voice_search_tomato
        searchKeywordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, clearIcon, 0)

        eventTracker = getTracker()
        inAppSearchViewModel = ViewModelProvider(this, viewModelFactory)
            .get(InAppSearchViewModel::class.java)
        inAppSearchViewModel.source = intent.getStringExtra(SOURCE) ?: "default_source"
        inAppSearchViewModel.classSelectedForSearch = intent.getParcelableExtra(SELECTED_CLASS)

        startVoiceSearch = intent.getBooleanExtra(START_VOICE_SEARCH, false)
        isDirectSearch = intent.getBooleanExtra(IS_DIRECT_SEARCH, false)
        directSearchQuery = intent.getStringExtra(DIRECT_SEARCH_QUERY)
        isQuickSearch = intent.getBooleanExtra(QUICK_SEARCH, false)

        val classesList = intent.getParcelableArrayListExtra<ClassListViewItem>(CLASSES_LIST)

        inAppSearchViewModel.setClassesList(classesList ?: ArrayList())

        if (isAutoSuggestorEnabled) {
            setUpRecyclerView()
        }

        setUpViewPager()

        setUpObservers()


        if (!isDirectSearch && !isQuickSearch) {
            getTrendingSearch()
        }

        setListeners()

        inAppSearchViewModel.fetchSearchHints()
    }

    override fun onClick(v: View?) {
        when (v) {
            closeSearchScreen -> {
                val currentVisibleFragment = searchResultPagerAdapter
                    .getFragmentForPosition(searchResultViewPager.currentItem)
                if (currentVisibleFragment != null && isYoutubePlayerRunning(currentVisibleFragment))
                    (currentVisibleFragment as InAppYoutubeSearchFragment).stopVideoPlayer()
                else
                    handleBackFunctionality()
            }

            cameraButton -> {
                inAppSearchViewModel.sendEvent(
                    EventConstants.EVENT_NAME_IN_APP_SEARCH_CAMERA_ICON_CLICK,
                    hashMapOf(), inAppSearchViewModel.searchLandingVersion, ignoreSnowplow = true
                )
                CameraActivity.getStartIntent(this, TAG).also {
                    startActivity(it)
                }
            }
        }
    }

    private fun handleBackFunctionality() {
        val filterFragment = supportFragmentManager.findFragmentByTag(Constants.FILTER_FRAGMENT)
        if (filterFragment != null) {
            val fragmentTransaction: FragmentTransaction =
                supportFragmentManager!!.beginTransaction()
            fragmentTransaction.remove(filterFragment)
            fragmentTransaction.commitAllowingStateLoss()
            return
        }

        when {
            isDirectSearch -> {
                TypeYourDoubtActivity.startActivity(this, inAppSearchViewModel.source)
                finish()
            }
            searchResultViewPager.currentItem != 0 -> searchResultViewPager.currentItem = 0
            else -> {
                when {
                    searchKeywordInput.text?.isNotEmpty() == true -> {
                        inAppSearchViewModel.sendEvent(
                            EventConstants.EVENT_IN_APP_SERACH_EMPTY_BACK,
                            hashMapOf<String, Any>().apply {
                                put(
                                    EventConstants.SEARCHED_ITEM,
                                    searchKeywordInput.text.toString()
                                )
                            }, inAppSearchViewModel.searchLandingVersion, ignoreSnowplow = true
                        )
                        searchKeywordInput.setText("")
                    }
                    else -> {
                        inAppSearchViewModel.sendEvent(
                            EventConstants.EVENT_IN_APP_SEARCH_BACK,
                            hashMapOf(),
                            inAppSearchViewModel.searchLandingVersion,
                            ignoreSnowplow = true
                        )

                        if (isBlankSearchCycle)
                            addBlankSearchSession()

                        closeKeyboard()
                        finish()
                    }
                }
            }
        }
    }

    private fun addBlankSearchSession() {
        val timeStamp = System.currentTimeMillis()
        val searchedText = searchKeywordInput.text.toString()
        val eventTypes = mutableListOf<String>()
        if (isBackDialogShown)
            eventTypes.add(EventConstants.IN_APP_SEARCH_POPUP_SLP)
        eventTypes.add(EventConstants.EVENT_IN_APP_SERACH_EMPTY_BACK)
        inAppSearchViewModel.addSession(
            SearchSessionModel(
                uscId = timeStamp,
                searchedText = searchedText,
                size = 0,
                timeStamp = timeStamp,
                isSearched = false,
                isMatched = false,
                eventTypes = eventTypes
            )
        )
        if (inAppSearchViewModel.classSelectedForSearch?.classNo ?: 0 >= 10) {
            notMatchEvent()
        }
    }

    private fun notMatchEvent() {
        val map = hashMapOf<String, Any>().apply {
            put(Constants.SEARCH_QUERY, intent.getStringExtra(DIRECT_SEARCH_QUERY) ?: "")
            put(Constants.STUDENT_ID, defaultPrefs().getString(Constants.STUDENT_ID, "") ?: "")
        }
        val event = AnalyticsEvent(EventConstants.IAS_NOT_MATCHED, map, ignoreSnowplow = true)
        analyticsPublisher.publishMoEngageEvent(event)
        analyticsPublisher.publishEvent(event)
        DoubtnutApp.INSTANCE.getEventTracker().addEventNames(EventConstants.IAS_NOT_MATCHED)
            .addNetworkState(NetworkUtils.isConnected(this).toString())
            .addStudentId(UserUtil.getStudentId())
            .addEventParameter(map)
            .track()
    }

    private fun showBackDialog() {
        if (isSearchPerformed)
            inAppSearchViewModel.addEventsToLastSession(EventConstants.IN_APP_SEARCH_POPUP_SRP)
        else
            inAppSearchViewModel.addEventsToLastSession(EventConstants.IN_APP_SEARCH_POPUP_SLP)

        val selectedClass = inAppSearchViewModel.classSelectedForSearch?.classNo?.toString()
            ?: defaultPrefs().getString(Constants.STUDENT_CLASS, "") ?: ""
        val list = inAppSearchViewModel.getSearchHints(selectedClass) ?: listOf()

        isBackDialogShown = true
        searchEvent =
            if (isSearchPerformed) EventConstants.IN_APP_SEARCH_POPUP_SRP else EventConstants.IN_APP_SEARCH_POPUP_SLP
        inAppSearchViewModel.sendEvent(
            searchEvent,
            hashMapOf(),
            inAppSearchViewModel.searchLandingVersion
        )

        InAppSearchCloseDialog.getInstance(list.toTypedArray(), isSearchPerformed) {
            searchTextChanged = false
            isQuickSearch = true
            if (searchEvent.isEmpty())
                searchEvent = EventConstants.SEARCH_QUERY_EVENT
            getUserSearchResult(it, searchEvent)
            searchEvent = ""
        }.show(supportFragmentManager, InAppSearchCloseDialog.TAG)
    }

    private fun isShowBackDialog() = !isBackDialogShown &&
            !inAppSearchViewModel.isActionPerformed &&
            inAppSearchViewModel.searchHints.value != null &&
            isSearchPerformed.not() &&
            !hasReachedBackDialogLimit()

    private fun hasReachedBackDialogLimit(): Boolean {
        val key = if (isSearchPerformed)
            Constants.IAS_BACK_DIALOG_SRP_COUNT
        else
            Constants.IAS_BACK_DIALOG_SLP_COUNT
        val count = defaultPrefs().getLong(key, 0)
        val lastDate =
            defaultPrefs().getLong(Constants.IAS_BACK_DIALOG_LAST_DATE, Date().time).run {
                Date(this)
            }
        return if (count >= 3) {
            return if (lastDate.isToday()) {
                true
            } else {
                defaultPrefs().edit {
                    putLong(key, 0)
                    putLong(Constants.IAS_BACK_DIALOG_LAST_DATE, Date().time)
                }
                false
            }
        } else {
            defaultPrefs().edit {
                putLong(Constants.IAS_BACK_DIALOG_LAST_DATE, Date().time)
            }
            false
        }

    }

    private fun setUpViewPager() {
        searchResultPagerAdapter = SearchPagerAdapter(
            supportFragmentManager, emptyList(),
            emptyList()
        )
        searchResultViewPager.adapter = searchResultPagerAdapter
        searchResultViewPager.offscreenPageLimit = 3
        searchResultCategoryTab.setupWithViewPager(searchResultViewPager)
        searchResultViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrolled(
                position: Int, positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                val currentVisibleFragment =
                    searchResultPagerAdapter.getFragmentForPosition(currentPage)
                if (currentVisibleFragment != null && isYoutubePlayerRunning(currentVisibleFragment))
                    (currentVisibleFragment as InAppYoutubeSearchFragment).stopVideoPlayer()
                currentPage = position
            }

            override fun onPageSelected(position: Int) {
                lastPage = position
                inAppSearchViewModel.sendEvent(
                    EventConstants.EVENT_IN_APP_FACET_CLICKED,
                    hashMapOf<String, Any>().apply {
                        put(
                            EventConstants.SEARCHED_ITEM,
                            searchKeywordInput.text.toString()
                        )
                        put(EventConstants.FACET, tabTitleList[position])
                        put(
                            EventConstants.VARIANT_ID,
                            FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE)
                        )
                    }, inAppSearchViewModel.searchLandingVersion, ignoreSnowplow = true
                )
                //facet click event added to globalSearchLogs
                inAppSearchViewModel.postMongoEvent(hashMapOf<String, Any>().apply {
                    put(EventConstants.EVENT_TYPE,EventConstants.EVENT_IN_APP_FACET_CLICKED)
                    put(EventConstants.SEARCHED_ITEM, searchKeywordInput.text.toString())
                    put(EventConstants.FACET, tabTitleList[position])
                    put(EventConstants.VARIANT_ID, FeaturesManager.getVariantId(DoubtnutApp.INSTANCE, Features.IAS_SERVICE))
                })


            }

            override fun onPageScrollStateChanged(state: Int) {
            }

        })
    }

    private fun setUpObservers() {

        inAppSearchViewModel.advancedSearchResponse.observeK(
            this,
            ::onAdvancedSearchSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        inAppSearchViewModel.trendingSearchLiveData.observeK(
            this,
            ::onTrendingSearchSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        inAppSearchViewModel.userNewSearchLiveData.observeK(
            this,
            ::onUserSearchSuccessNew,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        inAppSearchViewModel.searchSuggestionsLiveData.observeK(
            this,
            ::onSearchSuggestionSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        inAppSearchViewModel.recentSearchClicked.observe(this, Observer {
            /**
             * Making searchTextChanged as false because no need to fetch suggestions as this is the
             * case to click over trending or recent search items.
             */
            val event =
                if (it.second) EventConstants.TRENDING_CLICK_RECENT else EventConstants.TRENDING_CLICK_EVENT
            if (isAutoSuggestorEnabled) {
                searchTextChanged = false
                getUserSearchResult(it.first, event)
            } else {
                searchEvent = event
                if (inAppSearchViewModel.searchedDisplayTrending.isNotEmpty()) {
                    searchKeywordInput.setText(inAppSearchViewModel.searchedDisplayTrending)
                } else {
                    searchKeywordInput.setText(it.first)
                }
                searchKeywordInput.setSelection(searchKeywordInput.text?.length ?: 0)
            }
            closeKeyboard()
        })

        inAppSearchViewModel.topTagSubjectClicked.observe(this, Observer {
            /**
             * Making searchTextChanged as false because no need to fetch suggestions as this is the
             * case to click over trending or recent search items.
             */
            inAppSearchViewModel.liveClassDataFirstTime = null
            if (isAutoSuggestorEnabled) {
                searchTextChanged = false
                getUserSearchResult(
                    it.first,
                    EventConstants.SEARCH_NEW_LANDING_SUBJECT_CLICKED,
                    liveOrder = it.third
                )
            } else {
                searchEvent = EventConstants.SEARCH_NEW_LANDING_SUBJECT_CLICKED
                searchKeywordInput.setText(it.first)
                searchKeywordInput.setSelection(searchKeywordInput.text?.length ?: 0)
            }
            redirectTab = it.second
            closeKeyboard()
        })

        inAppSearchViewModel.seeAllClicked.observe(this, Observer {
            searchResultViewPager.currentItem = it
        })

        inAppSearchViewModel.navigateLiveData.observe(this, EventObserver {
            inAppSearchViewModel.isActionPerformed = true
            val screen = it.screen
            val args = it.hashMap
            when (screen) {
                is VideoYouTubeScreen -> {
                    startActivity(
                        YoutubeTypeVideoActivity.getStartIntent(
                            this,
                            args?.get(Constants.QUESTION_ID).toString()
                        )
                    )
                }
                is LiveClassesScreen -> {
                    startActivity(
                        VideoPageActivity.startActivity(
                            context = this,
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
                    screenNavigator.startActivityFromActivity(this, screen, argsMap.toBundle())
                }
                else -> screenNavigator.startActivityFromActivity(this, screen, args?.toBundle())
            }
        })
        inAppSearchViewModel.tabPosition.observe(this, Observer {
            lastPage = it
        })

        inAppSearchViewModel.filterEventLiveData.observe(this, Observer {
            performAction(it)
        })

        busObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            when (it) {
                is IasClearAllFilters -> {
                    searchFilterAdapter?.performAction(it)
                }
                is IasSortByFilterSelected -> {
                    searchFilterAdapter?.notifyDataSetChanged()
                    performAction(it)
                }
                is IasFilterSelected -> {
                    searchFilterAdapter?.notifyDataSetChanged()
                    performAction(it)
                }
                is AllChapterResultClicked -> {
                    inAppSearchViewModel.postAllChapterResultClickedEvent(it)
                }
            }
        }


    }


    private fun onAdvancedSearchSuccess(apiSearchAdvanceFilterResponse: ApiSearchAdvanceFilterResponse) {
        alertDialog?.dismiss()
        openAdvanceFilterScreen(isYoutube = false)

    }

    private fun getTrendingSearch() {
        if (!viewsDataList.isNullOrEmpty()) {
            onTrendingSearchSuccess(viewsDataList!!)
            return
        }
        dataSize = 0
        searchProgressBar?.show()

        inAppSearchViewModel.getTrendingSearchResult()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setListeners() {
        closeSearchScreen.setOnClickListener(this)

        cameraButton.setOnClickListener(this)

        searchKeywordInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {

                if (text?.length ?: 0 >= maxSearchQuery.length)
                    maxSearchQuery = text?.toString() ?: ""

                isBlankSearchCycle = false
                if (text?.trim()?.isNotEmpty() == true
                    && TextUtil.graphemeLength(text.trim().toString()) > 2
                    && TextUtil.graphemeLength(text.trim().toString()) <= 120
                ) {
                    if (inAppSearchViewModel.source.equals("LibraryFragmentHome")) {
                        return
                    }
                    if (isAutoSuggestorEnabled) {
                        /**
                         * checking searchTextChanged value to manage whether user typed some query or
                         * user selected/clicked some suggestion to fetch search results directly.
                         */
                        if (searchTextChanged) {
                            getSearchSuggestions(text.trim().toString(), false)
                        }

                        // re-initializing the value of searchTextChanged to the default value.

                    } else {
                        searchProgressBar.show()
                        if (searchTextChanged) {
                            if (searchEvent.isEmpty())
                                searchEvent = EventConstants.SEARCH_QUERY_EVENT

                            getUserSearchResult(
                                text.toString(),
                                searchEvent,
                                searchEvent == EventConstants.SEARCH_VOICE_EVENT
                            )
                            searchEvent = ""
                        }
                    }
                    searchTextChanged = true
                } else {
                    if (!inAppSearchViewModel.isAdvancedSerachDataRequest) {
                        inAppSearchViewModel.handleLastSearchSession(maxSearchQuery)
                        maxSearchQuery = ""
                        manageSuggestionsListVisibility(false)

                        getTrendingSearch()

                        KeyboardUtils.showKeyboard(searchKeywordInput)
                    }
                }
            }

            override fun afterTextChanged(editable: Editable?) {
                val clearIcon = if (editable?.trim()?.isNotEmpty() == true) {
                    R.drawable.ic_clear
                } else {
                    searchActionButton.hide()
                    R.drawable.ic_voice_search_tomato
                }
                searchKeywordInput.setCompoundDrawablesWithIntrinsicBounds(0, 0, clearIcon, 0)

                if (editable?.trim()?.isNotEmpty() == true && inAppSearchViewModel.isSearchIconEnabled == 1.0){
                    searchImageView.show()
                } else {
                    searchImageView.hide()
                }
            }
        })

        searchActionButton.setOnClickListener {
            callActionOnSearchButtonClick()
        }

        searchImageView.setOnClickListener {
            callActionOnSearchButtonClick()
        }

        searchKeywordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                callActionOnSearchButtonClick()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        searchKeywordInput.setOnTouchListener { _, event ->

            val drawableRight = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (searchKeywordInput.right -
                            searchKeywordInput.compoundDrawables[drawableRight].bounds.width())
                ) {

                    if (searchKeywordInput.text?.isEmpty() == true) {
                        inAppSearchViewModel.sendEvent(
                            EventConstants.EVENT_NAME_IN_APP_SEARCH_VOICE,
                            hashMapOf(),
                            inAppSearchViewModel.searchLandingVersion
                        )
                        promptSpeechInput()
                    } else {
                        inAppSearchViewModel.sendEvent(
                            EventConstants.EVENT_IN_APP_SERACH_CROSS_ICON,
                            hashMapOf<String, Any>().apply {
                                put(
                                    EventConstants.SEARCHED_ITEM,
                                    searchKeywordInput.text.toString()
                                )
                            }, inAppSearchViewModel.searchLandingVersion, ignoreSnowplow = true
                        )
                        inAppSearchViewModel.searchedDisplayTrending = ""
                        endSearchSessionOnEvent(EventConstants.EVENT_IN_APP_SERACH_CROSS_ICON)

                        searchKeywordInput.setText("")
                        return@setOnTouchListener true
                    }
                } else if (event.rawX < (searchKeywordInput.right - searchKeywordInput
                        .compoundDrawables[drawableRight].bounds.width())
                ) {
                    searchKeywordInput.requestFocus()
                    KeyboardUtils.showKeyboard(searchKeywordInput)
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun callActionOnSearchButtonClick() {
        sendKeyboardSearchEvent()
        KeyboardUtils.hideKeyboard(searchKeywordInput)
        /**
         * Making searchTextChanged as false because no need to fetch suggestions as this
         * is the case to click on the search button on the soft input keyboard.
         */
        if (isAutoSuggestorEnabled) {
            searchTextChanged = false
            isSearchButtonClicked = true
            getUserSearchResult(
                searchKeywordInput.text.toString(),
                EventConstants.SEARCH_KEYBOARD_EVENT,
            )

            inAppSearchViewModel.sendDirectSearchEvent(searchKeywordInput.text.toString())

        }
        endSearchSessionOnEvent(EventConstants.SEARCH_KEYBOARD_EVENT)
    }

    private fun endSearchSessionOnEvent(event: String) {
        inAppSearchViewModel.updateIsSearched(true)
        inAppSearchViewModel.addEventsToLastSession(event)
    }

    /**
     * Method to handle the api response for new search results.
     */
    private fun onUserSearchSuccessNew(searchDataItem: NewSearchDataItem) {
        if (isAutoSuggestorEnabled)
            closeKeyboard()

        inAppSearchViewModel.categrorizedDataList = searchDataItem.categorizedDataList
        var resultCount = 0
        if (inAppSearchViewModel.filteredFacet == null || "all".equals(
                inAppSearchViewModel.filteredFacet?.key.orEmpty(),
                true
            )
        ) {
            if (!searchDataItem.landingFacet.isNullOrEmpty()) {
                redirectTab = searchDataItem.landingFacet
            }
            setTabPosition(searchDataItem.tabsList)
            checkForClassMessageVisibility()
            tvClasssMessage.text = "${getString(R.string.search_results_for)} ${
                inAppSearchViewModel
                    .classSelectedForSearch?.let { it.className }
            }"
            searchProgressBar.hide()
            stopShimmer()
            if (!searchDataItem.tabsList.isNullOrEmpty()) {
                for (tab in searchDataItem.tabsList) {
                    inAppSearchViewModel.tabMap.put(tab.key, tab)
                }
            }
            if (searchKeywordInput.text!!.isNotEmpty() ||
                inAppSearchViewModel.isAdvancedSerachDataRequest
            ) {
                manageSuggestionsListVisibility(false)
                dataSize = 0
                for (element in searchDataItem.categorizedDataList) {
                    dataSize += element.dataList.size
                }
                if (dataSize < 1) {
                    inAppSearchViewModel.sendNoDataEvent(
                        inAppSearchViewModel.source,
                        searchKeywordInput.text.toString()
                    )
                    updateViewPagerV2(searchDataItem, isEmpty = true)
                } else {
                    searchResultViewPager.currentItem = 0
                    updateViewPagerV2(searchDataItem, isEmpty = false)
                }
            }
        } else {
            searchProgressBar.hide()
            stopShimmer()


            for (tab in searchDataItem.tabsList) {
                inAppSearchViewModel.tabMap.put(tab.key, tab)
            }
            var index = 0
            for (list in searchDataItem.categorizedDataList) {
                if (list.tabType != inAppSearchViewModel.filteredFacet!!.key) {
                    index++
                } else {
                    break
                }
            }

            val currentVisibleFragment =
                searchResultPagerAdapter.getFragmentForPosition(searchResultViewPager.currentItem)
            if (currentVisibleFragment != null && currentVisibleFragment is InAppSearchFragment) {
                if (index < searchDataItem.categorizedDataList.size) {
                    currentVisibleFragment.updateResults(
                        searchDataItem.categorizedDataList[index].dataList,
                        searchDataItem.categorizedDataList[index].chapterDetails
                    )
                    resultCount = searchDataItem.categorizedDataList[index].dataList.size

                } else {
                    currentVisibleFragment.updateResults(arrayListOf(), null)
                }
            }

        }

        //update all filter fragment if added
        val allFilterFragment = supportFragmentManager?.findFragmentByTag(Constants.FILTER_FRAGMENT)
        if (allFilterFragment != null && allFilterFragment is IasAllFilterFragment) {
            allFilterFragment.updateFilters(resultCount)
        }

        //update searchbox if advanced filter search
        if (inAppSearchViewModel.isAdvancedSerachDataRequest) {
            searchKeywordInput.setText("")
        }
    }

    private fun setTabPosition(tabList: List<SearchTabsItem>) {
        if (redirectTab.isBlank())
            return
        tabList.forEachIndexed { index, searchTabsItem ->
            if (searchTabsItem.key == redirectTab) {
                lastPage = index
                return
            }
        }
    }

    private fun checkForClassMessageVisibility() {
        if (inAppSearchViewModel.classSelectedForSearch?.className == defaultPrefs(this)
                .getString("student_class", "")
            || inAppSearchViewModel.classSelectedForSearch?.className.isNullOrEmpty()
        )
            tvClasssMessage.hide()
    }

    /**
     * Method to handle the api response for search suggestions.
     */
    private fun onSearchSuggestionSuccess(searchSuggestionsDataItem: SearchSuggestionsDataItem) {
        if (searchKeywordInput.text!!.isNotEmpty()) {
            manageSuggestionsListVisibility(true)
            searchProgressBar.hide()
            if (!searchSuggestionsDataItem.suggestionsList.isNullOrEmpty()) {
                adapter.updateData(searchSuggestionsDataItem.suggestionsList)
                rvSuggestions.show()
            }
        }
    }

    private fun onTrendingSearchSuccess(viewsDataList: List<TrendingSearchDataListViewItem>) {
        this.viewsDataList = viewsDataList
        searchProgressBar.hide()
        stopShimmer()
        dataSize = viewsDataList.size
        updateViewPagerWithTrendingData(viewsDataList)
    }

    private fun updateViewPagerV2(searchDataItem: NewSearchDataItem, isEmpty: Boolean) {

        var tabsList = ArrayList<String>()
        val fragmentList: List<Fragment>

        if (isEmpty) {
            searchResultCategoryTab.hide()
            tvClasssMessage.hide()
            tabsList = ArrayList()
            fragmentList = listOf(NoDataFoundFragment.newInstance())
        } else {
            isSearchPerformed = true
            searchResultCategoryTab.show()
            checkForClassMessageVisibility()
            val tempData = inAppSearchViewModel.getFragmentListWithNewSearchData(
                searchDataItem,
                searchDataItem.tabsList.size <= 1
            )

            tabList = searchDataItem.tabsList as ArrayList<SearchTabsItem>
            searchDataItem.tabsList.forEach {
                tabsList.add(it.description)
            }
            fragmentList = tempData.fragmentList
        }
        tabTitleList = tabsList
        searchResultPagerAdapter.updateSearchResultList(tabsList, fragmentList)
        searchDataItem.tabsList.forEachIndexed { index, searchTabsItem ->
            if (searchTabsItem.isVip)
                searchResultCategoryTab.getTabAt(index)?.let {
                    val view = ImageView(applicationContext)
                    view.layoutParams = ViewGroup.LayoutParams(92, 48)
                    view.setImageResource(R.drawable.ic_tag_vip)
                    it.setCustomView(view)
                }
        }
        if (lastPage < tabsList.size)
            searchResultViewPager.currentItem = lastPage
    }

    private fun updateViewPagerWithTrendingData(
        trendingSearchData: List<TrendingSearchDataListViewItem>
    ) {
        val tabsList = listOf<String>()
        val fragmentList: List<Fragment>
        searchResultCategoryTab.hide()
        tvClasssMessage.hide()
        fragmentList = inAppSearchViewModel.getTrendingFragmentList(trendingSearchData)
        searchResultPagerAdapter.updateSearchResultList(tabsList, fragmentList)
    }

    fun postUserSearchData(searchPlaylistEntity: SearchPlaylistViewItem) {
        inAppSearchViewModel.addRecentSearchText(searchKeywordInput.text.toString())
        // check postSearchData uses
    }

    private fun unAuthorizeUserError() {
        stopShimmer()
        searchProgressBar.hide()
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        stopShimmer()
        searchProgressBar.hide()
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {
        searchProgressBar.hide()
        stopShimmer()
    }

    private fun ioExceptionHandler() {
        stopShimmer()
        searchProgressBar.hide()
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    fun sendSearchClickEvent(
        clickedData: String,
        clickedDataTitle: String,
        itemId: String,
        section: String,
        type: String,
        position: Int,
        isToppersChoice: Boolean,
        resultCount: Int = 0,
        assortmentId: String
    ) {
        val seletedTab = tabTitleList.getOrNull(currentPage) ?: ""
        inAppSearchViewModel.sendSearchClickEvent(
            inAppSearchViewModel.source,
            searchKeywordInput.text.toString(),
            clickedData,
            clickedDataTitle,
            itemId,
            section,
            type,
            position + 1,
            searchResultCategoryTab.selectedTabPosition == 0,
            seletedTab,
            currentPage + 1,
            isToppersChoice,
            resultCount,
            assortmentId
        )
    }

    private fun sendKeyboardSearchEvent() {
        inAppSearchViewModel.sendKeyboardSearchEvent(
            inAppSearchViewModel.source,
            searchKeywordInput.text.toString()
        )
    }

    private fun sendVoiceSearchEvent() {
        inAppSearchViewModel.sendVoiceSearchEvent(
            inAppSearchViewModel.source,
            searchKeywordInput.text.toString()
        )
    }

    fun sendTrendingClickEvent(
        trending: String,
        searchedData: String,
        type: String,
        itemPosition: Int,
        isRecentSearch: Boolean
    ) {
        inAppSearchViewModel.sendTrendingClickEvent(
            inAppSearchViewModel.source, trending,
            searchedData, type, itemPosition, isRecentSearch
        )
    }

    fun sendTrendingRecentMongoClickEvent(action: TrendingPlaylistMongoEvent) {
        inAppSearchViewModel.sendTrendingRecentMongoClickEvent(action)
    }

    fun sendTrendingBookClickEvent(action: TrendingBookClicked) {
        inAppSearchViewModel.sendTrendingBookClickEvent(inAppSearchViewModel.source, action)
        handleSearchSessions(action)
    }

    fun sendTrendingCourseClickEvent(action: TrendingCourseClicked) {
        inAppSearchViewModel.sendTrendingCourseClickEvent(inAppSearchViewModel.source, action)
    }

    fun closeKeyboard() = KeyboardUtils.hideKeyboard(searchKeywordInput)

    /**
     * Showing google speech input dialog
     */
    private fun promptSpeechInput() {
        singleResult = true
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speech Prompt"
        )
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            ToastUtils.makeText(
                applicationContext,
                "Speech not supported",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == RESULT_OK && null != data) {
                    val result =
                        data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).orEmpty()

                    /**
                     * Making searchTextChanged as false because no need to fetch suggestions as
                     * this is the case to enter search  query through voice speech.
                     */
                    if (isAutoSuggestorEnabled) {
                        searchTextChanged = false
                        inAppSearchViewModel.searchTrigger =
                            EventConstants.SEARCH_TRIGGER_VOICE_SEARCH
                        getUserSearchResult(
                            result[0].toString(),
                            EventConstants.SEARCH_VOICE_EVENT,
                            true
                        )
                    } else {
                        inAppSearchViewModel.searchTrigger =
                            EventConstants.SEARCH_TRIGGER_VOICE_SEARCH
                        searchEvent = EventConstants.SEARCH_VOICE_EVENT
                        searchKeywordInput.setText(result[0])
                        searchKeywordInput.setSelection(searchKeywordInput.text?.length ?: 0)
                    }
                    if (singleResult) {
                        sendVoiceSearchEvent()
                        singleResult = false
                    }
                }
            }
        }
    }

    /**
     * Setting Up Suggestions RecyclerView.
     */
    private fun setUpRecyclerView() {
        adapter = SearchSuggestionsAdapter(this)
        rvSuggestions.adapter = adapter
        adapter.updateData(emptyList())
        rvSuggestions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    closeKeyboard()
                }
            }
        })
    }

    /**
     * Callback Method to handle the click events on the suggestions list items.
     * If user clicks on the suggestion text, then it will call the search result api with the
     * selected  text or If user clicks on the arrow then it call the suggestions api for the text
     * before the arrow.
     * @param action : Action object with two properties i.e, the text data and the click widget
     *                 identifier
     */
    override fun performAction(action: Any) {
        searchTextChanged = false
        when (action) {
            is ShowAllFilters -> {
                openAllFilterScreen(action.facet.filterList!!, action.facet, action.isYoutube)
            }
            is IasFilterSelected -> {
                applyFilter(action)
            }
            is IasSortByFilterSelected -> {
                sortResult(action)
            }
            is IasClearAllFilters -> {
                if (!action.isYoutube) {
                    getUserSearchResult(
                        inAppSearchViewModel.searchedText,
                        EventConstants.SEARCH_TRIGGER_FILTER,
                        false,
                        false,
                        hashMapOf()
                    )
                }
            }
            is SearchSuggestionClicked -> {
                when (action.type) {
                    ACTION_ITEM_SELECTED -> {
                        // ApXor/Snowplow event for suggestion text click
                        inAppSearchViewModel.sendEvent(
                            EventConstants.EVENT_NAME_IN_APP_SEARCH_SUGGESTION_CLICK,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.SOURCE, inAppSearchViewModel.source)
                                put(EventConstants.CLICKED_ITEM, action.suggestionData)
                                put(EventConstants.ITEM_POSITION, action.itemPosition)
                                if (isNewSuggester)
                                    put("type", "new_suggester_page")
                            }, inAppSearchViewModel.searchLandingVersion, ignoreSnowplow = true
                        )
                        inAppSearchViewModel.postSuggestionClickEvent(
                            action.data, action.suggestionData,
                            action.id,
                            action.version,
                            action.itemPosition,
                            adapter.itemCount - 1
                        )

                        getUserSearchResult(
                            action.suggestionData,
                            EventConstants.EVENT_NAME_IN_APP_SEARCH_SUGGESTION_CLICK
                        )
                    }

                    ACTION_ITEM_TEXT_SUBMITTED -> {
                        getSearchSuggestions(action.suggestionData, true)
                        // ApXor/Snowplow event for suggestion text arrow click
                        inAppSearchViewModel.sendEvent(
                            EventConstants.EVENT_NAME_IN_APP_SEARCH_ICON_CLICK,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.SOURCE, inAppSearchViewModel.source)
                                put(EventConstants.CLICKED_ITEM, action.suggestionData)
                            }, inAppSearchViewModel.searchLandingVersion, ignoreSnowplow = true
                        )
                    }
                }
            }
            else -> {
                inAppSearchViewModel.handleAction(action, searchKeywordInput.text.toString())
            }
        }
    }

    private fun openAllFilterScreen(
        filters: ArrayList<SearchFilter>,
        facet: SearchTabsItem,
        isYoutube: Boolean
    ) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager!!.beginTransaction()
        fragmentTransaction.add(
            R.id.fragmentContainer,
            IasAllFilterFragment.newInstance(facet, filters, isYoutube),
            Constants.FILTER_FRAGMENT
        )
        fragmentTransaction.commitAllowingStateLoss()
    }

    private fun openAdvanceFilterScreen(
        isYoutube: Boolean
    ) {
        val fragmentTransaction: FragmentTransaction = supportFragmentManager!!.beginTransaction()
        fragmentTransaction.add(
            R.id.fragmentContainer,
            IasAdvancedSearchFragment.newInstance(isYoutube),
            Constants.FILTER_OPTION_FRAGMENT
        )
        fragmentTransaction.commitAllowingStateLoss()
    }

    private fun applyFilter(action: IasFilterSelected) {
        if (!action.isYoutube) {
            inAppSearchViewModel.sendSearchFilterAppliedEvent(
                inAppSearchViewModel.searchedText,
                action.appliedFilterMap,
                action.tab.description
            )

            val appliedFilterMap: HashMap<String, Any> = HashMap()
            val filterMap = HashMap<String, ArrayList<SearchFilterItemApiParam>>()

            val tabFilters = inAppSearchViewModel.tabMap[action.tab.key]?.filterList ?: emptyList()
            val tabFilterMap = hashMapOf<String, ArrayList<SearchFilterItemApiParam>>()

            for (tf in tabFilters) {
                tabFilterMap.put(tf.key, mapToSearchFilterItemApiParam(tf.filters))
            }

            for (appliedFilterPair in action.appliedFilterMap) {
                if (!"sort".equals(appliedFilterPair.key, true)) {
                    filterMap.put(appliedFilterPair.key, tabFilterMap[appliedFilterPair.key]!!)
                }
            }

            if (!filterMap.isNullOrEmpty()) {
                appliedFilterMap.put(action.tab.key, filterMap)
            }
            inAppSearchViewModel.filteredFacet = action.tab

            for (tab in tabList) {
                if (tab.key == action.tab.key) {
                    setFilterSelection(tab, action.appliedFilterMap)
                }
            }
            getUserSearchResult(
                inAppSearchViewModel.searchedText,
                EventConstants.SEARCH_TRIGGER_FILTER,
                false,
                false,
                appliedFilterMap
            )
        } else {
            var filterString = ""
            for (filter in action.appliedFilterMap) {
                if (filter.key.equals("subject", true)) {
                    filterString.plus(" " + getAppliedFilterArray(filter.value))
                }
                filterString.plus(" doubtnut ")
                if (filter.key.equals("class", true)) {
                    filterString.plus("class " + getAppliedFilterArray(filter.value))
                }
                if (filter.key.equals("language", true)) {
                    filterString.plus(" in " + getAppliedFilterArray(filter.value))
                }
            }
            val currentVisibleFragment =
                searchResultPagerAdapter.getFragmentForPosition(searchResultViewPager.currentItem)
            if (currentVisibleFragment != null && currentVisibleFragment is InAppYoutubeSearchFragment)
                currentVisibleFragment.onFilterApplied(inAppSearchViewModel.searchedText + filterString)
        }
    }

    private fun mapToSearchFilterItemApiParam(filters: java.util.ArrayList<SearchFilterItem>): java.util.ArrayList<SearchFilterItemApiParam> {
        val paramArray = arrayListOf<SearchFilterItemApiParam>()
        for (filterItem in filters) {
            paramArray.add(filterItem.toSearchFilterItemApiParam())
        }
        return paramArray
    }

    private fun sortResult(action: IasSortByFilterSelected) {
        val currentVisibleFragment =
            searchResultPagerAdapter.getFragmentForPosition(searchResultViewPager.currentItem)
        if (currentVisibleFragment != null && currentVisibleFragment is InAppSearchFragment)
            currentVisibleFragment.sortResult(action.filterValue)

        inAppSearchViewModel.sendSortFilterAppliedEvent(
            inAppSearchViewModel.searchedText,
            action.filterValue,
            action.tab.description
        )
    }

    private fun setFilterSelection(tab: SearchTabsItem, appliedFilterMap: HashMap<String, String>) {
        for ((filterKey, selectedFilter) in appliedFilterMap) {
            for (filter in tab.filterList!!) {
                if (filter.key == filterKey) {
                    filter.isSelected = true
                    updateFilterValueSelection(filter.filters, selectedFilter)
                    continue
                }
            }
        }
    }

    private fun updateFilterValueSelection(
        filters: java.util.ArrayList<SearchFilterItem>,
        selectedFilter: String
    ) {
        for (filterValue in filters) {
            if (filterValue.value.equals(selectedFilter, true)) {
                filterValue.isSelected = true
                return
            }
        }
    }

    private fun getAppliedFilterArray(filterValue: String): ArrayList<SearchFilterItem> {
        val arr: ArrayList<SearchFilterItem> = arrayListOf()
        arr.add(SearchFilterItem(filterValue, null, "", 0, true))
        return arr
    }

    /**
     * Method to manage the visibility of the widgets for the use cases whether we're showing the
     * suggestions list or the search results for some particular text.
     * @param shouldShow boolean to check whether to show suggestions list or the search results.
     */
    private fun manageSuggestionsListVisibility(shouldShow: Boolean) {
        if (isAutoSuggestorEnabled) {
            rvSuggestions.visibility = if (shouldShow) View.VISIBLE else View.GONE
            searchResultViewPager.visibility = if (shouldShow) View.GONE else View.VISIBLE

            if (shouldShow) {
                searchResultCategoryTab.hide()
                tvClasssMessage.hide()
            } else {
                searchResultCategoryTab.show()
                checkForClassMessageVisibility()
            }
        }
    }

    /**
     * Method to call the api to fetch the search result against some text query and to set that
     * text to the EditText.
     *
     * @param text text of search query for which result has to be fetched.
     */
    private fun getUserSearchResult(
        text: String,
        eventType: String,
        isVoiceSearch: Boolean = false,
        liveOrder: Boolean = false,
        appliedFilterMap: HashMap<String, Any>? = null,
        redirectTab: String = ""
    ) {
        if (text.trim().isEmpty()) {
            toast("Results dekhne ke liye kisi topic, subject ya class ka search karein")
            return
        }
        searchProgressBar.show()

        if (appliedFilterMap == null) {
            inAppSearchViewModel.filteredFacet = null
        }
        this.redirectTab = redirectTab
        if (eventType === EventConstants.TRENDING_CLICK_EVENT) {
            this.redirectTab = "live_class"
        }
        handleSearchSessions(text, eventType)
        dataSize = 0
        if (!isSearchButtonClicked && (isAutoSuggestorEnabled || isDirectSearch || isQuickSearch)) {
            searchTextChanged = false
            if (inAppSearchViewModel.searchedDisplayTrending.isNotEmpty()) {
                searchKeywordInput.setText(inAppSearchViewModel.searchedDisplayTrending)
            } else {
                searchKeywordInput.setText(text)
            }
            searchKeywordInput.setSelection(searchKeywordInput.text?.length ?: 0)
            searchKeywordInput.requestFocus()
        }
        isSearchButtonClicked = false
        lastPage = 0
        inAppSearchViewModel.getUserSearchNewResults(
            text,
            isVoiceSearch,
            liveOrder,
            appliedFilterMap
        )
    }

    private fun handleSearchSessions(text: String, eventType: String) {
        val timeStamp = System.currentTimeMillis()
        inAppSearchViewModel.addSession(
            SearchSessionModel(
                uscId = timeStamp,
                searchedText = text,
                size = 0,
                timeStamp = timeStamp,
                isSearched = null,
                isMatched = false,
                eventTypes = mutableListOf(eventType)
            )
        )
    }

    private fun handleSearchSessions(action: TrendingBookClicked) {
        var event = if (action.data.type == "live_class_course") {
            EventConstants.EVENT_TRENDING_COURSES_CLICKED
        } else if (action.data.type == "book") {
            EventConstants.EVENT_TRENDING_BOOK_CLICKED
        } else {
            EventConstants.EVENT_TRENDING_POPULAR_ON_DOUBTNUT_CLICKED
        }
        val timeStamp = System.currentTimeMillis()
        inAppSearchViewModel.addSession(
            SearchSessionModel(
                uscId = timeStamp,
                searchedText = action.data.display,
                size = action.resultCount,
                selectedItemPosition = action.itemPosition,
                selectedTabName = action.data.type,
                timeStamp = timeStamp,
                isSearched = true,
                isMatched = false,
                eventTypes = mutableListOf(event)
            )
        )
    }

    /**
     * Method to call the api to fetch the search suggestions against some text query and to set that
     * text to the EditText.
     *
     * @param text text of search query for which suggestions has to be fetched.
     * @param setText boolean to check whether we need to set the text to EditText or not as in the
     * where user is already typing the query there.
     */
    private fun getSearchSuggestions(text: String, setText: Boolean) {
        if (setText) {
            searchKeywordInput.setText(text)
            searchKeywordInput.setSelection(text.length)
        }
        searchProgressBar.show()
        handleSearchSessions(text, "onChangedSearchQuery")
        inAppSearchViewModel.getSearchSuggestions(text, isNewSuggester)
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@InAppSearchActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEvent(eventName: String) {

        this@InAppSearchActivity.apply {
            (this@InAppSearchActivity.applicationContext as DoubtnutApp)
                .getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(
                    NetworkUtils.isConnected(this@InAppSearchActivity)
                        .toString()
                )
                .addStudentId(
                    defaultPrefs(this@InAppSearchActivity)
                        .getString(Constants.STUDENT_ID, "")
                        ?: ""
                )
                .addScreenName(EventConstants.PAGE_INAPP_SEARCH_ACTIVITY)
                .track()
        }
    }

    override fun onDestroy() {
        busObserver?.dispose()
        inAppSearchViewModel.postPreviousSessions()
        if (alertDialog?.isShowing == true) {
            alertDialog?.dismiss()
        }
        inAppSearchViewModel.postIasExitEvent()
        super.onDestroy()
    }

    override fun onBackPressed() {
        val currentVisibleFragment = searchResultPagerAdapter
            .getFragmentForPosition(searchResultViewPager.currentItem)
        if (currentVisibleFragment != null && isYoutubePlayerRunning(currentVisibleFragment))
            (currentVisibleFragment as InAppYoutubeSearchFragment).stopVideoPlayer()
        else
            handleBackFunctionality()
    }

    private fun isYoutubePlayerRunning(currentVisibleFragment: Fragment) =
        currentVisibleFragment is InAppYoutubeSearchFragment
                && currentVisibleFragment.isAdded
                && currentVisibleFragment.videoFragmentContainer.isVisible

    override fun onUserInteraction() {
        noUserAction = false
        super.onUserInteraction()
    }

    private fun setTag(dialogView: View, tagList: List<TabTypeDataEntity>?) {
        val chipGroup: ChipGroup = dialogView.findViewById(R.id.tag_group)
        if (tagList != null) {
            for (item in tagList) {
                val chip = Chip(this)
                chip.text = item.value
                chip.setOnClickListener {
                    getFilters(item?.key)
                }
                chipGroup.addView(chip)
            }
        }
    }

    private fun getFilters(tabType: String?) {
        if (tabType != null) {
            inAppSearchViewModel.getFilterList(tabType)
        }
    }

}
