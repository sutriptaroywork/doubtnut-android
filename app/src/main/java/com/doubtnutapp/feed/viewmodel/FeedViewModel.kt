package com.doubtnutapp.feed.viewmodel

import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.AdStatusUpdated
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.UnbanRequested
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.course.widgets.ParentWidget
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.ButtonWidgetData
import com.doubtnutapp.data.remote.models.ButtonWidgetModel
import com.doubtnutapp.data.remote.models.feed.FeedPostItem
import com.doubtnutapp.data.remote.models.userstatus.StatusApiResponse
import com.doubtnutapp.data.remote.models.userstatus.StatusAttachment
import com.doubtnutapp.data.remote.models.userstatus.UserStatus
import com.doubtnutapp.data.remote.repository.UserActivityRepository
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.db.dao.StatusMetaDao
import com.doubtnutapp.db.entity.StatusMeta
import com.doubtnutapp.domain.videoPage.entities.VideoDataEntity
import com.doubtnutapp.domain.videoPage.interactor.GetVideoData
import com.doubtnutapp.domain.videoPage.interactor.UpdateVideoViewInteractor
import com.doubtnutapp.feed.FeedPostTypes
import com.doubtnutapp.feed.UserStatusTypes
import com.doubtnutapp.feed.entity.CreatePostVisibilityStatusResponse
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.feed.view.FeedFragment
import com.doubtnutapp.feed.view.widgets.FeedPostWidgetModel
import com.doubtnutapp.home.model.ExploreMoreWidgetResponse
import com.doubtnutapp.model.FeedApiResponse
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.videoPage.mapper.VideoMapper
import com.doubtnutapp.videoPage.model.ViewAnswerData
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.widgets.FollowWidget
import com.doubtnutapp.widgetmanager.widgets.RecentStatusWidget
import com.doubtnutapp.workmanager.WorkManagerHelper
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class FeedViewModel @Inject constructor(
    val analyticsPublisher: AnalyticsPublisher,
    val userPreference: UserPreference,
    compositeDisposable: CompositeDisposable,
    private val userActivityRepository: UserActivityRepository,
    private val gson: Gson,
    private val videoDataUseCase: GetVideoData,
    private val updateVideoViewInteractor: UpdateVideoViewInteractor,
    private val workManagerHelper: WorkManagerHelper,
    private val database: DoubtnutDatabase,
    private val networkUtil: NetworkUtil,
    private val videoPageMapper: VideoMapper
) : BaseViewModel(compositeDisposable) {

    private val teslaRepository = DataHandler.INSTANCE.teslaRepository
    private val userStatusRepository = DataHandler.INSTANCE.userStatusRepository

    var extraParams: Map<String, Any> = hashMapOf()

    var itemsAdded = 0

    private val feedItems: ArrayList<WidgetEntityModel<*, *>> = arrayListOf()
    private val hiddenFeedItems: ArrayList<WidgetEntityModel<*, *>> = arrayListOf()

    val debugLogLiveData = MutableLiveData(Event(""))
    val eventLiveData = MutableLiveData(Event(Any()))

    private val _normalFeedLiveData = MutableLiveData<Outcome<FeedApiResponse>>()

    val normalFeedLiveData: LiveData<Outcome<FeedApiResponse>>
        get() = _normalFeedLiveData

    private val _pinnedPostLiveData =
        MutableLiveData<Outcome<WidgetEntityModel<WidgetData, WidgetAction>?>>()

    val pinnedPostLiveData: LiveData<Outcome<WidgetEntityModel<WidgetData, WidgetAction>?>>
        get() = _pinnedPostLiveData

    private val _createPostViewsVisibilityMutableLiveData =
        MutableLiveData<Outcome<CreatePostVisibilityStatusResponse>>()

    val createPostViewsVisibilityLiveData: LiveData<Outcome<CreatePostVisibilityStatusResponse>> =
        _createPostViewsVisibilityMutableLiveData

    private val _exploreMoreWidget: MutableLiveData<Outcome<ExploreMoreWidgetResponse>> =
        MutableLiveData()
    val exploreMoreWidget: LiveData<Outcome<ExploreMoreWidgetResponse>> get() = _exploreMoreWidget

    private val _mutableLiveDataCreateOneTapPost: MutableLiveData<Outcome<BaseResponse>> =
        MutableLiveData()
    val liveDataCreateOneTapPost get():LiveData<Outcome<BaseResponse>> = _mutableLiveDataCreateOneTapPost

    private var totalEngagementTime: Int = 0
    private var engamentTimeToSend: Number = 0
    private var timerTask: TimerTask? = null
    private var engageTimer: Timer? = null
    private var handler: Handler? = null
    private var timeFormatter = SimpleDateFormat("m:ss", Locale.getDefault())

    private var source: String? = null
    private var type: String? = null
    private var page: Int = 1

    var isApplicationBackground = false
    private var isEngageTimerPaused = false
    var offsetCursor: String? = null
    var isAttachingData: Boolean = false

    //Status

    var userStatusList: ArrayList<UserStatus> = arrayListOf()
    var userStatusType: String = UserStatusTypes.FOLLOWING
    var userStatusPage: Int = 1
    var userStatusOffsetCursor: String = ""

    private val viewedStatusSet = ArrayList<String>()
    private val likedStatusSet = ArrayList<String>()
    private var statusMetaDao: StatusMetaDao? = null

    var recentStatusList: ArrayList<UserStatus> = arrayListOf()
    var popularStatusPage: Int = 1
    var popularStatusOffsetCursor: String = ""

    var statusAdsList: ArrayList<UserStatus> = arrayListOf()

    var indexOfP2pWidget = -1

    private val homePageCachedDataChangeListener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            when (key) {
                Constants.HOME_PAGE_DATA_INVALIDATED -> {
                    if (sharedPreferences.getBoolean(key, true)) {
                        fetchNormalFeed(1, Bundle().apply {
                            putBoolean(FeedFragment.IS_NESTED, true)
                            putString(FeedFragment.SOURCE, FeedFragment.SOURCE_HOME)
                            putString(FEED_TYPE, NORMAL_FEED)
                        })
                    }
                }
                Constants.HOME_PAGE_DATA -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        val homePageDataString = sharedPreferences.getString(key, "").orEmpty()
                        if (homePageDataString.isNotBlank()) {
                            _normalFeedLiveData.postValue(
                                Outcome.success(
                                    gson.fromJson(
                                        homePageDataString,
                                        FeedApiResponse::class.java
                                    )
                                )
                            )
                        }
                    }
                    defaultPrefs().edit {
                        putBoolean(Constants.HOME_PAGE_DATA_INVALIDATED, true)
                    }
                }
            }
        }

    companion object {
        const val FEED_TYPE = "type"
        const val TOPIC = "topic"

        const val NORMAL_FEED = "feed"
        const val USER_FEEDS = "user"
        const val USER_FAVOURITES = "user_favourites"
        const val TOPIC_POPULAR = "topic_popular"
        const val TOPIC_RECENT = "topic_recent"
        const val FEED_LIVE = "feed_live"
        const val FEED_LIVE_UPCOMING = "feed_live_upcoming"

        const val SOURCE = "source"
        const val DEFAULT_SOURCE = "friends"
        const val SOURCE_HOME = "home"
        const val SOURCE_USER_FEED = "user_feed"
        const val SOURCE_TOPIC_FEED = "topic_feed"
        const val SOURCE_LIVE_FEED = "live_feed"

        const val MAX_CAROUSEL_WIDGETS_HOME = 4

        const val USER_STATUS_TYPE = "user_status_type"

        val VALID_FEED_TYPES = arrayListOf<String>(
            FeedPostTypes.TYPE_MESSAGE,
            FeedPostTypes.TYPE_LINK,
            FeedPostTypes.TYPE_IMAGE,
            FeedPostTypes.TYPE_PDF,
            FeedPostTypes.TYPE_DN_VIDEO,
            FeedPostTypes.TYPE_VIDEO,
            FeedPostTypes.TYPE_DN_PAID_VIDEO,
            FeedPostTypes.TYPE_LIVE,
            FeedPostTypes.TYPE_POLL,
            FeedPostTypes.TYPE_DN_ACTIVITY
        )
    }

    fun fetchFeed(
        page: Int,
        bundle: Bundle?
    ): RetrofitLiveData<ApiResponse<List<WidgetEntityModel<WidgetData, WidgetAction>>>> {
        // remove stale data if any
        if (page == 1 && feedItems.isNotEmpty()) feedItems.clear()

        type = bundle?.getString(FEED_TYPE, NORMAL_FEED) ?: NORMAL_FEED
        source = bundle?.getString(SOURCE) ?: DEFAULT_SOURCE
        this.page = page
        when (type) {
            USER_FEEDS -> return teslaRepository.getUserFeed(
                page,
                bundle?.getString(Constants.STUDENT_ID, "")!!
            )
            USER_FAVOURITES -> return teslaRepository.getUserFavouritesFeed(
                page,
                bundle?.getString(Constants.STUDENT_ID, "")!!
            )
            TOPIC_POPULAR -> return teslaRepository.getPopularTopicFeed(
                page,
                bundle?.getString(TOPIC)!!
            )
            TOPIC_RECENT -> return teslaRepository.getRecentTopicFeed(
                page,
                bundle?.getString(TOPIC)!!
            )
            FEED_LIVE -> return teslaRepository.getLiveFeed(page)
            FEED_LIVE_UPCOMING -> return teslaRepository.getLiveUpcomingFeed(page)
        }

        throw IllegalArgumentException("Invalid feed type provided")
    }

    fun fetchNormalFeed(page: Int, bundle: Bundle?) {
        if (page == 1 && feedItems.isNotEmpty()) {
            offsetCursor = ""
            feedItems.clear()
        }
        type = bundle?.getString(FEED_TYPE, NORMAL_FEED) ?: NORMAL_FEED
        source = bundle?.getString(SOURCE) ?: DEFAULT_SOURCE
        this@FeedViewModel.page = page
//        val isHomePageCacheDataInvalidated =
//            defaultPrefs().getBoolean(Constants.HOME_PAGE_DATA_INVALIDATED, true)
        _normalFeedLiveData.value = Outcome.loading(true)

        viewModelScope.launch {
            val assortmentId = defaultPrefs().getString(Constants.SELECTED_ASSORTMENT_ID, "")
            teslaRepository.getFeed(page, source!!, offsetCursor.orEmpty(), assortmentId.orEmpty())
                .catch { e ->
                    _normalFeedLiveData.value = if (e is HttpException) {
                        when (e.response()?.code()) {
                            HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(e.message.orEmpty())
                            HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(e)
                            else -> Outcome.Failure(e)
                        }
                    } else {
                        Outcome.Failure(e)
                    }
                }
                .collect {
                    _normalFeedLiveData.value = when (it.meta.code) {
                        200 -> Outcome.success(it.data)
                        401 -> Outcome.apiError(Throwable(it.meta.message))
                        else -> Outcome.apiError(Throwable(it.meta.message))
                    }
                }
        }
        //region Home page data prefetch cache logic
        /**
        if (isHomePageCacheDataInvalidated || page != 1 || source != FeedFragment.SOURCE_HOME) {
        viewModelScope.launch {
        val assortmentId = defaultPrefs().getString(Constants.SELECTED_ASSORTMENT_ID, "")
        teslaRepository.getFeed(
        page,
        source!!,
        offsetCursor.orEmpty(),
        assortmentId.orEmpty()
        )
        .catch { e ->
        _normalFeedLiveData.value = if (e is HttpException) {
        when (e.response()?.code()) {
        HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(e.message.orEmpty())
        HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(e)
        else -> Outcome.Failure(e)
        }
        } else {
        Outcome.Failure(e)
        }
        }
        .collect {
        _normalFeedLiveData.value = when (it.meta.code) {
        200 -> Outcome.success(it.data)
        401 -> Outcome.apiError(Throwable(it.meta.message))
        else -> Outcome.apiError(Throwable(it.meta.message))
        }
        }
        }
        } else {
        viewModelScope.launch(Dispatchers.IO) {
        val homePageDataString =
        defaultPrefs().getString(Constants.HOME_PAGE_DATA, "").orEmpty()
        if (homePageDataString.isNotBlank()) {
        _normalFeedLiveData.postValue(
        Outcome.success(
        gson.fromJson(
        homePageDataString,
        FeedApiResponse::class.java
        )
        )
        )
        }
        }
        defaultPrefs().edit {
        putBoolean(Constants.HOME_PAGE_DATA_INVALIDATED, true)
        }
        defaultPrefs().registerOnSharedPreferenceChangeListener(homePageCachedDataChangeListener)
        }
         */
        //endregion
    }

    fun fetchPinnedPost() {
        viewModelScope.launch {
            teslaRepository.getPinnedPost()
                .catch { e ->
                    _pinnedPostLiveData.value = if (e is HttpException) {
                        when (e.response()?.code()) {
                            HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(e.message.orEmpty())
                            HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(e)
                            else -> Outcome.Failure(e)
                        }
                    } else {
                        Outcome.Failure(e)
                    }
                }
                .collect {
                    _pinnedPostLiveData.value = when (it.meta.code) {
                        200 -> Outcome.success(it.data)
                        401 -> Outcome.apiError(Throwable(it.meta.message))
                        else -> Outcome.apiError(Throwable(it.meta.message))
                    }
                }
        }
    }

    fun addExtraParamsToFeedData(feedItems: List<WidgetEntityModel<*, *>>?) {
        feedItems?.forEachIndexed { index, widgetEntityModel ->
            if (widgetEntityModel != null) {
                if (widgetEntityModel.extraParams == null) {
                    widgetEntityModel.extraParams = hashMapOf()
                }
                widgetEntityModel.extraParams?.putAll(extraParams)
                widgetEntityModel.extraParams?.put(
                    EventConstants.WIDGET_TYPE,
                    widgetEntityModel.type
                )
                widgetEntityModel.extraParams?.put(
                    EventConstants.ITEM_PARENT_POSITION,
                    itemsAdded + index
                )
            }
        }
    }

    fun filterAndUpdateFeeds(
        feedItems: List<WidgetEntityModel<*, *>>,
        pageNumber: Int
    ): List<WidgetEntityModel<*, *>> {

        val postItems = this.feedItems.filter { it.type == WidgetTypes.WIDGET_TYPE_FEED_POST }
        val existingPosts = postItems.map { (it as FeedPostWidgetModel).data.id }

        val updatedFeeds: ArrayList<WidgetEntityModel<*, *>> = arrayListOf()

        var hasPaidVideoContent = false

        feedItems.forEachIndexed { index, it ->
            /*
            for some users, we will only show MAX_CAROUSEL_WIDGETS_HOME carousels on home page.
            carousels are located on page 1 and with source as home, page 1 might also have some
            normal feed posts, so we exclude them from being hidden
             */
            if (FeaturesManager.isFeatureEnabled(
                    DoubtnutApp.INSTANCE,
                    Features.HOME_CAROUSELS_SHOW_MORE
                )
                && source == SOURCE_HOME
                && page == 1
                && it.type != WidgetTypes.WIDGET_TYPE_FEED_POST
            ) {

                if (index < MAX_CAROUSEL_WIDGETS_HOME) {
                    // these carousels are to be added
                    updatedFeeds.add(it)
                } else if (index == MAX_CAROUSEL_WIDGETS_HOME) {
                    // add a show more button at this index
                    updatedFeeds.add(ButtonWidgetModel().apply {
                        _type = WidgetTypes.TYPE_BUTTON
                        _data = ButtonWidgetData(
                            buttonText = "Show more videos",
                            bgColor = "#FF4200"
                        )
                    })
                    hiddenFeedItems.add(it)
                } else {
                    // carousels after this index are not added in visible feeds and are added
                    // in a hidden feed list
                    hiddenFeedItems.add(it)
                }
                return@forEachIndexed
            }
            if (it.type == WidgetTypes.WIDGET_TYPE_FEED_POST) {
                if ((it as FeedPostWidgetModel).data.id !in existingPosts
                    && it.data.type in VALID_FEED_TYPES
                ) {
                    if (it.data.type == FeedPostTypes.TYPE_DN_PAID_VIDEO) {
                        hasPaidVideoContent = true
                    }
                    updatedFeeds.add(it)
                }
            } else if (it.type == WidgetTypes.WIDGET_TYPE_RECENT_STATUS) {
                if (!FeaturesManager.isFeatureEnabled(
                        DoubtnutApp.INSTANCE,
                        Features.RECENT_STATUS
                    )
                ) {
                    hiddenFeedItems.add(it)
                } else {
                    updatedFeeds.add(it)
                    (it as RecentStatusWidget.RecentStatusWidgetModel).source =
                        getRecentStatusSource()
                    (it.data as RecentStatusWidget.RecentStatusWidgetData).items = recentStatusList
                    if (recentStatusList.isNullOrEmpty()) {
                        fetchRecentStatusList()
                    }
                }
            } else if (it.type == WidgetTypes.TYPE_WIDGET_STUDY_DOST) {
                val isStudyDostChatStarted =
                    defaultPrefs().getBoolean(Constants.IS_STUDY_DOST_CHAT_STARTED, false)
                val ignoreStudyDost = defaultPrefs().getBoolean(Constants.IGNORE_STUDY_DOST, false)
                if (isStudyDostChatStarted.not() && ignoreStudyDost.not()) {
                    updatedFeeds.add(it)
                }
            } else if (it.type == WidgetTypes.TYPE_WIDGET_CLASS_BOARD_EXAM) {
                val toRemoveClassBoardExamWidget =
                    defaultPrefs().getBoolean(Constants.REMOVE_CLASS_EXAM_BOARD_WIDGET, false)
                if (toRemoveClassBoardExamWidget.not()) {
                    updatedFeeds.add(it)
                }
            } else if (it.type == WidgetTypes.TYPE_WIDGET_PARENT) {
                val data = it.data as? ParentWidget.WidgetChildData
                val isDoubtP2pVisible = userPreference.isDoubtP2PHomeWidgetVisibility()
                if (data != null && data.items?.filterNotNull()?.isNotEmpty() == true &&
                    data.items[0]?.type == WidgetTypes.TYPE_WIDGET_DOUBT_P2P_HOME
                ) {
                    indexOfP2pWidget = index
                    if (isDoubtP2pVisible) {
                        updatedFeeds.add(it)
                    }
                } else {
                    updatedFeeds.add(it)
                }
            } else {
                updatedFeeds.add(it)
            }
        }


        this.feedItems.addAll(updatedFeeds)



        if (!hasPaidVideoContent) {
            analyticsPublisher.publishEvent(
                StructuredEvent(EventConstants.CATEGORY_FEED,
                    EventConstants.EVENT_NO_PAID_CONTENT,
                    eventParams = hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, source ?: DEFAULT_SOURCE)
                        put(EventConstants.PAGE, pageNumber)
                    })
            )
        }
        return updatedFeeds
    }

    fun addPinnedPost(pinnedPost: WidgetEntityModel<*, *>) {
        feedItems.add(0, pinnedPost)
    }

    private fun getRecentStatusSource(): String {
        if (source == SOURCE_HOME) {
            return EventConstants.STATUS_SOURCE_RECENT_WIDGET_HOME
        }
        return EventConstants.STATUS_SOURCE_RECENT_WIDGET_FEED
    }

    fun filterAndUpdateStatus(statusType: String, statusItems: List<UserStatus>): List<UserStatus> {

        val existingStatus = userStatusList.map { it.studentId }
        val filteredStatus = arrayListOf<UserStatus>()
        if (!statusItems.isNullOrEmpty()) {
            for (status in statusItems) {
                if (!existingStatus.contains(status.studentId)) {
                    filteredStatus.add(status)
                }
            }
        }

        if (statusType == UserStatusTypes.FOLLOWING) {
            for (status in filteredStatus) {
                status.isFollowing = true
            }
        }
        for (status in filteredStatus) {
            for (att: StatusAttachment in status.statusItem!!) {
                att.isLiked = likedStatusSet.contains(att.id)
                att.isViewed = viewedStatusSet.contains(att.id)
            }
        }
        userStatusList.addAll(filteredStatus)
        eventLiveData.value = Event(AdStatusUpdated)
        return filteredStatus
    }

    fun getEmptyFeedStringId(): Int {
        if (type == null) return R.string.no_posts_found
        return when (type) {
            FEED_LIVE_UPCOMING -> R.string.no_posts_feed_live_upcoming
            FEED_LIVE -> R.string.no_post_feed_live
            else -> R.string.no_posts_found
        }
    }

    fun getUserBanStatus() {
        compositeDisposable + DataHandler.INSTANCE.socialRepository
            .getUserBanStatus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.error == null && it.data is LinkedTreeMap<*, *>) {
                    val banStatus = it.data["banStatus"] as Boolean
                    defaultPrefs().edit {
                        putBoolean(Constants.USER_COMMUNITY_BAN, banStatus)
                    }
                    if (banStatus) {
                        getUnbanRequestStatus()
                    }
                }
            }, {

            })
    }

    fun getUnbanRequestStatus() {
        compositeDisposable + DataHandler.INSTANCE.socialRepository
            .getUserUnBanRequestStatus(getStudentId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.error == null && it.data is LinkedTreeMap<*, *>) {
                    val unBanRequestStatus = it.data["Request Status"] as String
                    defaultPrefs().edit {
                        putString(Constants.USER_UNABN_REQUEST_STATE, unBanRequestStatus)
                        eventLiveData.value = Event(UnbanRequested)
                    }
                }
            }, {

            })
    }

    fun trackView(state: ViewTrackingBus.State) {
        when (state.state) {
            ViewTrackingBus.VIEW_ADDED -> {
                eventWith(
                    StructuredEvent(
                        EventConstants.CATEGORY_FEED, EventConstants.EVENT_NAME_FEED_POST_SHOWN,
                        label = state.trackId,
                        property = state.position.toString(),
                        eventParams = state.trackParams
                    )
                )
                if (source == FeedFragment.SOURCE_HOME) {
                    val eventParams = hashMapOf<String, Any>()
                    if (source != null && !state.trackParams.containsKey(SOURCE)) {
                        eventParams[SOURCE] = source.orEmpty()
                    }
                    eventParams[Constants.ID] = state.trackId
                    eventParams.putAll(state.trackParams)
                    eventWith(EventConstants.HOME_PAGE_CAROUSEL_SHOWN, eventParams)
                }

            }
            ViewTrackingBus.VIEW_REMOVED -> {
                eventWith(
                    StructuredEvent(
                        EventConstants.CATEGORY_FEED, EventConstants.EVENT_NAME_FEED_POST_HIDDEN,
                        label = state.trackId,
                        property = state.position.toString(),
                        eventParams = state.trackParams
                    )
                )
                if (source == FeedFragment.SOURCE_HOME) {
                    val eventParams = hashMapOf<String, Any>()
                    if (source != null && !state.trackParams.containsKey(SOURCE)) {
                        eventParams[SOURCE] = source.orEmpty()
                    }
                    eventParams[Constants.ID] = state.trackId
                    eventParams.putAll(state.trackParams)
                    eventWith(
                        EventConstants.HOME_PAGE_CAROUSEL_HIDDEN,
                        eventParams,
                        ignoreSnowplow = true
                    )
                }
            }
            ViewTrackingBus.TRACK_VIEW_DURATION -> {
                eventWith(
                    StructuredEvent(
                        EventConstants.CATEGORY_FEED,
                        EventConstants.EVENT_NAME_FEED_POST_IMPRESSION,
                        label = state.trackId,
                        property = state.position.toString(),
                        value = state.time.toDouble(),
                        eventParams = state.trackParams.apply {
                            put("t_id", state.trackId)
                        }
                    )
                )
            }
        }

        var debugText = ""

        if (state.state == ViewTrackingBus.VIEW_ADDED) {
            debugText = "position ${state.position} visible"
        }
        if (state.state == ViewTrackingBus.VIEW_REMOVED) {
            debugText = "position ${state.position} hidden"
        }
        if (state.state == ViewTrackingBus.TRACK_VIEW_DURATION) {
            debugText = "position ${state.position} hidden, was visible for ${state.time}ms"
        }

        debugLogLiveData.postValue(Event(debugText))
    }

    /*
    get hidden feed items that are to be shown when user clicks on show more,
    the hidden feed items are returned as is, but we also update the visible feedItems to include
    hidden feed items accordingly since it might be used later on
     */
    fun getHiddenItemsAndUpdateFeed(): List<WidgetEntityModel<*, *>> {
        this.feedItems.removeAt(MAX_CAROUSEL_WIDGETS_HOME)
        this.feedItems.addAll(MAX_CAROUSEL_WIDGETS_HOME, hiddenFeedItems)
        return this.hiddenFeedItems
    }

    fun setUpEventObservers() {
        compositeDisposable.add(
            DoubtnutApp.INSTANCE.bus()!!
                .toObservable()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ event ->
                    when (event) {
                        is StatusCreated -> addMyStatus(event.status)
                        is StatusDeleted -> deleteStatus(event.attachmentPostion)
                        is StatusViewed -> {
                            val item = statusViewed(
                                event.source,
                                event.statusPosition,
                                event.attachmentPostion,
                                event.id
                            )
                            saveViewedStatus(event.id, item?.isLiked ?: false)
                        }
                        is StatusLiked -> {
                            statusLiked(event.source, event.statusPosition, event.id, event.value)
                            saveLiked(event.id, event.value)
                        }
                        is StatusFollowed -> {
                            statusFollowed(event.source, event.statusPosition)
                        }
                        else -> eventLiveData.value = Event(event)
                    }

                }, {
                    it.printStackTrace()
                })
        )
    }

    fun setupEngagementTracking() {
        handler = Handler(Looper.getMainLooper())
        startEngagementTimer()
    }

    private fun startEngagementTimer() {

        if (engageTimer == null) {
            engageTimer = Timer()
            timeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        }
        timerTask = object : TimerTask() {
            override fun run() {
                handler?.post {
                    if ((!isApplicationBackground && !isEngageTimerPaused) || isAttachingData) {
                        engamentTimeToSend = totalEngagementTime
                        totalEngagementTime++
                    }
                }
            }
        }
        totalEngagementTime = 0
        engageTimer!!.schedule(timerTask, 0, 1000)
    }

    fun onStop() {
        timerTask?.let { handler?.removeCallbacks(it) }
        var page = source ?: EventConstants.PAGE_FEED
        if (source == SOURCE_HOME) {
            page = EventConstants.PAGE_HOME_FEED
        }
        if (source == DEFAULT_SOURCE) {
            page = EventConstants.PAGE_FEED
        }

        val countToSendEvent: Int =
            Utils.getCountToSend(RemoteConfigUtils.getEventInfo(), EventConstants.FEED_ENGAGEMENT)
        repeat((0 until countToSendEvent).count()) {
            this.sendEventForEngagement(EventConstants.FEED_ENGAGEMENT, engamentTimeToSend, page)
        }
        this.eventWith(
            StructuredEvent(
                action = EventConstants.FEED_ENGAGEMENT,
                category = EventConstants.CATEGORY_FEED,
                value = engamentTimeToSend.toDouble(),
                eventParams = hashMapOf(
                    EventConstants.SOURCE to page,
                    EventConstants.PARAM_TIMESTAMP to System.currentTimeMillis()
                )
            )
        )

        if (source != DEFAULT_SOURCE && source != SOURCE_USER_FEED) {
            pauseEngageTimer()
        }
        totalEngagementTime = 0
    }

    fun eventWith(eventName: String) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf()))
    }

    fun eventWith(
        eventName: String,
        params: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    fun eventWith(event: AnalyticsEvent) {
        analyticsPublisher.publishEvent(event)
    }

    fun eventWith(snowplowEvent: StructuredEvent) {
        analyticsPublisher.publishEvent(snowplowEvent.apply {
            if (source != null && !eventParams.containsKey(SOURCE)) {
                eventParams[SOURCE] = source!!
            }
        })
    }

    private fun sendEventForEngagement(
        eventName: String,
        engagementTimeToSend: Number,
        page: String
    ) {
        val app = DoubtnutApp.INSTANCE
        app.getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(app.applicationContext).toString())
            .addStudentId(getStudentId())
            .addScreenName(page)
            .addEventParameter(EventConstants.ENGAGEMENT_TIME, engagementTimeToSend)
            .track()
    }

    fun getFollowerWidgetItems() {
        viewModelScope.launch {
            DataHandler.INSTANCE.socialRepository.getUsersToFollow().map {
                it.data.items.map {
                    with(it) {
                        FollowWidget.FollowerData(studentId, imageUrl, name, followerText)
                    }
                }
            }.catch { }.collect {
                DoubtnutApp.INSTANCE.bus()?.send(FollowWidgetItemsFetched(it))
            }
        }
    }

    fun fetchStatus(): RetrofitLiveData<ApiResponse<StatusApiResponse>> {
        return userStatusRepository.getUserStories(
            userStatusPage,
            userStatusType,
            userStatusOffsetCursor
        )
    }

    fun fetchRecentStatusList() {
        if (FeaturesManager.isFeatureEnabled(DoubtnutApp.INSTANCE, Features.RECENT_STATUS)) {

            compositeDisposable + userStatusRepository.getPopularStories(
                popularStatusPage,
                popularStatusOffsetCursor
            ).applyIoToMainSchedulerOnSingle()
                .map {
                    it.data
                }
                .subscribeToSingle({
                    recentStatusList.addAll(filterRecentStatusData(it.statusData))
                    popularStatusOffsetCursor = it.offsetCursor.orEmpty()
                    popularStatusPage++
                    DoubtnutApp.INSTANCE.bus()?.send(RecentStatusListUpdated())
                }, {
                })
        }
    }

    private fun filterRecentStatusData(newList: List<UserStatus>?): ArrayList<UserStatus> {
        if (newList.isNullOrEmpty()) {
            return arrayListOf()
        }
        val existingStatus = recentStatusList.map { it.studentId }
        var filteredStatus = arrayListOf<UserStatus>()

        if (!existingStatus.isNullOrEmpty()) {
            if (!newList.isNullOrEmpty()) {
                for (status in newList) {
                    if (!existingStatus.contains(status.studentId)) {
                        filteredStatus.add(status)
                    }
                }
            }
        } else {
            filteredStatus = newList as ArrayList<UserStatus>
        }

        for (status in filteredStatus) {
            for (att: StatusAttachment in status.statusItem!!) {
                att.isLiked = likedStatusSet.contains(att.id)
                att.isViewed = viewedStatusSet.contains(att.id)
            }
        }
        return filteredStatus
    }

    fun onResume() {
        isEngageTimerPaused = false
    }

    private fun pauseEngageTimer() {
        isEngageTimerPaused = true
    }

    private fun saveViewedStatus(id: String, isLiked: Boolean) {
        viewedStatusSet.add(id)
        statusMetaDao?.insertViewedStatus(StatusMeta(id, isLiked, true, System.currentTimeMillis()))
    }

    private fun saveLiked(id: String, value: Boolean) {
        if (value) {
            likedStatusSet.add(id)
        } else {
            likedStatusSet.remove(id)
        }
        statusMetaDao?.insertLikedStatus(StatusMeta(id, true, true, System.currentTimeMillis()))
    }

    fun initStatusSet() {
        defaultPrefs().edit().remove("likedStatus").commit()
        defaultPrefs().edit().remove("viewedStatus").commit()

        statusMetaDao = DoubtnutApp.INSTANCE.getDatabase()?.statusMetaDao()
        if (statusMetaDao != null) {
            statusMetaDao!!.deleteOutdated(System.currentTimeMillis() - (86400000))

            likedStatusSet.clear()
            viewedStatusSet.clear()

            likedStatusSet.addAll(statusMetaDao!!.getLikedStatus(true))
            viewedStatusSet.addAll(statusMetaDao!!.getViewedStatus(true))

        }
    }

    private fun addMyStatus(status: StatusAttachment) {
        if (!userStatusList.isNullOrEmpty()) {
            var myStatus = userStatusList[0]
            if (myStatus != null) {
                if (myStatus.studentId == getStudentId()) {
                    if (myStatus.statusItem == null) {
                        myStatus.statusItem = ArrayList()
                    }
                    myStatus.statusItem!!.add(status)
                } else {
                    myStatus = createMyStatus(status)
                    userStatusList.add(0, myStatus)
                }
            } else {
                myStatus = createMyStatus(status)
                userStatusList.add(0, myStatus)
            }
        } else {
            userStatusList = arrayListOf()
            userStatusList.add(0, createMyStatus(status))
        }
    }

    private fun createMyStatus(status: StatusAttachment): UserStatus {
        val myStatusItem = ArrayList<StatusAttachment>()
        myStatusItem.add(0, status)
        return UserStatus(
            UserUtil.getStudentId(),
            UserUtil.getProfileImage(),
            UserUtil.getStudentName(),
            UserUtil.getStudentClass(),
            status.id,
            myStatusItem,
            -1,
            "",
            true,
        )
    }

    private fun deleteStatus(attachmentPostion: Int) {
        if (!userStatusList.isNullOrEmpty()) {
            val myStatus = userStatusList[0]
            if (myStatus.studentId == UserUtil.getStudentId()) {
                if (!myStatus.statusItem.isNullOrEmpty() && attachmentPostion < myStatus.statusItem!!.size) {
                    myStatus.statusItem!!.removeAt(attachmentPostion)
                }
                if (myStatus.statusItem.isNullOrEmpty()) {
                    userStatusList.removeAt(0)
                }
            }
        }
    }

    private fun statusViewed(
        source: String,
        statusPostion: Int,
        attachmentPostion: Int,
        attachmentId: String
    ): StatusAttachment? {
        when (source) {
            EventConstants.STATUS_SOURCE_HEADER -> {
                if (!userStatusList.isNullOrEmpty() && statusPostion < userStatusList.size) {
                    val myStatus = userStatusList[statusPostion]
                    if (!myStatus.statusItem.isNullOrEmpty()) {
                        for (i in myStatus.statusItem!!) {
                            if (i.id == attachmentId) {
                                i.isViewed = true
                                return i
                            }
                        }
                    }
                }

            }
            EventConstants.STATUS_SOURCE_RECENT_WIDGET_FEED, EventConstants.STATUS_SOURCE_RECENT_WIDGET_HOME -> {
                if (FeaturesManager.isFeatureEnabled(
                        DoubtnutApp.INSTANCE,
                        Features.RECENT_STATUS
                    )
                ) {
                    if (!recentStatusList.isNullOrEmpty() && statusPostion < recentStatusList.size) {
                        val myStatus = recentStatusList[statusPostion]
                        if (!myStatus.statusItem.isNullOrEmpty()) {
                            for (i in myStatus.statusItem!!) {
                                if (i.id == attachmentId) {
                                    i.isViewed = true
                                    return i
                                }
                            }
                        }
                    }
                }
            }
        }


        return null
    }

    private fun statusLiked(
        source: String,
        statusPostion: Int,
        attachmentId: String,
        isLiked: Boolean
    ) {
        if (source == EventConstants.STATUS_SOURCE_HEADER) {
            if (!userStatusList.isNullOrEmpty() && statusPostion < userStatusList.size) {
                val myStatus = userStatusList[statusPostion]
                if (!myStatus.statusItem.isNullOrEmpty()) {
                    for (i in myStatus.statusItem!!) {
                        if (i.id == attachmentId) {
                            i.isLiked = isLiked
                        }
                    }
                }
            }
        }
        if (source == EventConstants.STATUS_SOURCE_RECENT_WIDGET_FEED || source == EventConstants.STATUS_SOURCE_RECENT_WIDGET_HOME) {
            if (FeaturesManager.isFeatureEnabled(DoubtnutApp.INSTANCE, Features.RECENT_STATUS)) {
                if (!recentStatusList.isNullOrEmpty() && statusPostion < recentStatusList.size) {
                    val myStatus = recentStatusList[statusPostion]
                    if (!myStatus.statusItem.isNullOrEmpty()) {
                        for (i in myStatus.statusItem!!) {
                            if (i.id == attachmentId) {
                                i.isLiked = isLiked
                            }
                        }
                    }
                }
            }
        }
    }

    private fun statusFollowed(source: String, statusPostion: Int) {
        if (source == EventConstants.STATUS_SOURCE_HEADER) {
            if (!userStatusList.isNullOrEmpty() && statusPostion < userStatusList.size) {
                val myStatus = userStatusList[statusPostion]
                if (myStatus != null) {
                    myStatus.isFollowing = true
                }
            }
        }

        if (source == EventConstants.STATUS_SOURCE_RECENT_WIDGET_FEED || source == EventConstants.STATUS_SOURCE_RECENT_WIDGET_HOME) {
            if (FeaturesManager.isFeatureEnabled(DoubtnutApp.INSTANCE, Features.RECENT_STATUS)) {
                if (!recentStatusList.isNullOrEmpty() && statusPostion < recentStatusList.size) {
                    val myStatus = recentStatusList[statusPostion]
                    if (myStatus != null) {
                        myStatus.isFollowing = true
                    }
                }
            }
        }
    }

    fun storeFeedSeenCoreAction() {
        viewModelScope.launch {
            userActivityRepository.storeCoreActionDone(CoreActions.FEED_SEEN).catch { }.collect()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerTask?.cancel()
        timerTask = null
//        defaultPrefs().unregisterOnSharedPreferenceChangeListener(homePageCachedDataChangeListener)
    }

    fun fetchViewAnserData(data: FeedPostItem, viewForm: String) {
        compositeDisposable.add(
            videoDataUseCase
                .execute(
                    GetVideoData.Param(
                        data.videoObj?.questionId ?: "", "", "",
                        viewForm, UserUtil.getStudentClass(), "",
                        "", false,
                        Utils.decrypt(defaultPrefs().getString(Constants.GAME_TOKEN, "")).orEmpty(),
                        "", "", false, !networkUtil.isConnectionFast(), false
                    )
                )
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    onVideoPageViewSuccess(it, data)
                }, this::onVideoPageViewError)
        )

    }

    private fun onVideoPageViewSuccess(videoDataEntity: VideoDataEntity, data: FeedPostItem) {
        videoDataEntity.apply {
            this.isFromSmartContent = false
        }
        val viewAnswerData = videoDataEntity.run {
            videoPageMapper.map(videoDataEntity)
        }
        data.viewAnswerData = viewAnswerData
        data.isLoadingViewAnswerData = false
    }

    private fun onVideoPageViewError(error: Throwable) {

    }

    fun postPremiumVideoBlockedEvent(paramsMap: HashMap<String, Any>) {
        compositeDisposable + DataHandler.INSTANCE.courseRepository
            .postPaidContentEvent(paramsMap).applyIoToMainSchedulerOnSingle().map { it.data }
            .subscribeToSingle({
            }, {
            })
    }

    fun getAudioAttributes(): AudioAttributes {
        return AudioAttributes.Builder().apply {
            setUsage(C.USAGE_MEDIA)
            setContentType(C.CONTENT_TYPE_SPEECH)
        }.build()
    }

    fun sendVideoEngagementLogs(
        feedItem: FeedPostItem,
        videoEngagementStats: ExoPlayerHelper.VideoEngagementStats
    ) {
        if (feedItem.viewAnswerData != null && feedItem.viewAnswerData is ViewAnswerData) {
            val viewAnswerData = feedItem.viewAnswerData as ViewAnswerData
            Single.fromCallable {
                workManagerHelper.assignUpdateVideoStatsWork(
                    viewAnswerData.viewId,
                    "0",
                    videoEngagementStats.maxSeekTime.toString(),
                    videoEngagementStats.engagementTime.toString(),
                    viewAnswerData.lockUnlockLogs
                )
            }.flatMap {
                return@flatMap updateVideoViewInteractor.execute(
                    UpdateVideoViewInteractor.Param(
                        viewAnswerData.viewId,
                        "0",
                        videoEngagementStats.maxSeekTime.toString(),
                        videoEngagementStats.engagementTime.toString(),
                        viewAnswerData.lockUnlockLogs
                    )
                ).toSingleDefault<Boolean?>(true)
            }.doAfterSuccess {
                database.videoStatusTrackDao().updateVideoTrackStatus(viewAnswerData.viewId)
                workManagerHelper.cancelUpdateVideoStatsUniqueWork(viewAnswerData.viewId)
            }.applyIoToMainSchedulerOnSingle().subscribe()
        }
    }

    fun fetchStatusAds() {
        if (FeaturesManager.isFeatureEnabled(DoubtnutApp.INSTANCE, Features.FEED_STATUS_ADS)) {
            compositeDisposable + teslaRepository.getStatusAds().applyIoToMainSchedulerOnSingle()
                .map {
                    it.data
                }
                .subscribeToSingle({
                    statusAdsList.addAll(it.statusData as ArrayList)
                    eventLiveData.value = Event(AdStatusUpdated)
                }, {
                })
        }
    }

    fun getStudyGroupData() = userPreference.getStudyGroupData()

    fun getDnrData() = userPreference.getDnrData()

    fun getExploreMoreWidget() {
        if (source != SOURCE_HOME) return
        _exploreMoreWidget.value = Outcome.loading(true)
        viewModelScope.launch {
            DataHandler.INSTANCE.exploreMoreWidgetRepository.getExploreMoreWidget()
                .map {
                    it.data
                }
                .catch {
                    _exploreMoreWidget.value = Outcome.loading(false)
                    onExploreMoreError(it)
                }
                .collect {
                    _exploreMoreWidget.value = Outcome.success(it)
                    _exploreMoreWidget.value = Outcome.loading(false)
                }
        }
    }

    private fun onExploreMoreError(error: Throwable) {
        _exploreMoreWidget.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    fun getCreatePostViewsVisibilityStatus() {
        viewModelScope.launch() {
            try {
                val response = teslaRepository.getCreatePostViewsVisibilityForFeed();
                _createPostViewsVisibilityMutableLiveData.postValue(Outcome.success(response))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createOneTapPost(id: String) {
        viewModelScope.launch {
            _mutableLiveDataCreateOneTapPost.postValue(Outcome.loading(true))
            try {
                val response = teslaRepository.createOneTapPost(id)
                _mutableLiveDataCreateOneTapPost.postValue(Outcome.success(response))
                _mutableLiveDataCreateOneTapPost.value = Outcome.loading(false)
            } catch (e: Exception) {
                e.printStackTrace()
                _mutableLiveDataCreateOneTapPost.postValue(Outcome.apiError(e))
            }
        }
    }
    
}

