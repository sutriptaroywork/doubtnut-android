package com.doubtnutapp.feed.view

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.DnException
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.constant.ErrorConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.ui.helpers.LinearLayoutManagerWithSmoothScroller
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.scholarship.widget.ScholarshipProgressCardWidgetModel
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.hasAudioRecordingPermission
import com.doubtnutapp.base.extension.hasWindowFocus
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.course.widgets.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.feed.TopOptionsWidgetModel
import com.doubtnutapp.data.remote.models.userstatus.StatusApiResponse
import com.doubtnutapp.data.remote.models.userstatus.UserStatus
import com.doubtnutapp.databinding.FragmentFeedBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.feed.FeedPostTypes
import com.doubtnutapp.feed.UserStatusTypes
import com.doubtnutapp.feed.entity.CreatePostVisibilityStatusResponse
import com.doubtnutapp.feed.tracking.ViewTrackingBus
import com.doubtnutapp.feed.viewmodel.FeedViewModel
import com.doubtnutapp.home.DoubtFeedBannerBottomSheetFragment
import com.doubtnutapp.home.model.ExploreMoreWidgetResponse
import com.doubtnutapp.liveclass.ui.HomeWorkActivity
import com.doubtnutapp.liveclass.ui.HomeWorkSolutionActivity
import com.doubtnutapp.liveclass.ui.practice_english.PracticeEnglishWidget
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionFragment
import com.doubtnutapp.notification.NotificationCenterActivity
import com.doubtnutapp.rvexoplayer.RecyclerViewExoPlayerHelper
import com.doubtnutapp.rvexoplayer.RvPlayStrategy
import com.doubtnutapp.sales.PrePurchaseCallingCardModel2
import com.doubtnutapp.sales.event.PrePurchaseCallingCardDismiss
import com.doubtnutapp.sales.widget.PrePurchaseCallingCardModel
import com.doubtnutapp.sticker.BaseActivity
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.userstatus.TagsEndlessHorizontalRecyclerOnScrollListener
import com.doubtnutapp.ui.userstatus.UserStatusListAdapter
import com.doubtnutapp.utils.*
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.doubtnutapp.widgetmanager.widgets.CarouselListWidget
import com.doubtnutapp.widgetmanager.widgets.DoubtFeedBannerWidget
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.math.absoluteValue

/**
 * This fragment is loaded in HomeFeedFragmentV2 as a child fragment and also
 * Reused for friends section tab.
 */
class FeedFragment : BaseBindingFragment<FeedViewModel, FragmentFeedBinding>(), ActionPerformer,
    SharedPreferences.OnSharedPreferenceChangeListener,
    ExoPlayerHelper.VideoEngagementStatusListener {

    var restartJob: Job? = null

    companion object {
        const val IS_NESTED = "is_nested"
        const val SHOW_TOPIC = "show_topic"

        const val SOURCE = FeedViewModel.SOURCE
        const val DEFAULT_SOURCE = FeedViewModel.DEFAULT_SOURCE
        const val SOURCE_HOME = FeedViewModel.SOURCE_HOME

        const val TAG = "FeedFragment"

        private var refreshUI: Boolean = false
        private var practiceWidgetPosition = 0
        private var isTrialHeaderShown = false

        fun newInstance(
            isNested: Boolean = true,
            source: String = DEFAULT_SOURCE
        ) =
            FeedFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(IS_NESTED, isNested)
                    putString(SOURCE, source)
                    putString(FeedViewModel.FEED_TYPE, FeedViewModel.NORMAL_FEED)
                }
            }

        fun newUserFeedInstance(type: String, studentId: String) = FeedFragment().apply {
            arguments = Bundle().apply {
                putBoolean(IS_NESTED, true)
                putString(Constants.STUDENT_ID, studentId)
                putString(FeedViewModel.FEED_TYPE, type)
                putString(SOURCE, FeedViewModel.SOURCE_USER_FEED)
            }
        }

        fun newTopicFeedInstance(type: String, topic: String) = FeedFragment().apply {
            arguments = Bundle().apply {
                putBoolean(IS_NESTED, true)
                putString(FeedViewModel.FEED_TYPE, type)
                putString(FeedViewModel.TOPIC, topic)
                putBoolean(SHOW_TOPIC, false)
                putString(SOURCE, FeedViewModel.SOURCE_TOPIC_FEED)
            }
        }

        fun newLiveFeedInstance(type: String) = FeedFragment().apply {
            arguments = Bundle().apply {
                putBoolean(IS_NESTED, true)
                putString(FeedViewModel.FEED_TYPE, type)
                putString(SOURCE, FeedViewModel.SOURCE_LIVE_FEED)
            }
        }
    }

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            val practiceEnglishWidget =
                binding.rvFeed.layoutManager?.findViewByPosition(practiceWidgetPosition) as? PracticeEnglishWidget
            practiceEnglishWidget?.onPermissionFlowCompleted(isGranted)
        }

    private var appStateObserver: Disposable? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private lateinit var trialHeaderVM: TrialHeaderVM

    private lateinit var adapter: FeedAdapter
    private var statusAdapter: UserStatusListAdapter? = null
    private var feeds: List<WidgetEntityModel<*, *>>? = null

    private var isNested: Boolean = false
    private var showTopic: Boolean = true
    private var source: String? = DEFAULT_SOURCE
    private var viewTrackingBus: ViewTrackingBus? = null

    private var scrollListener: TagsEndlessRecyclerOnScrollListener? = null
    private var nestedScrollListener: EndlessNestedScrollListener? = null
    private var statusScrollListener: TagsEndlessHorizontalRecyclerOnScrollListener? = null

    private lateinit var mListener: SharedPreferences.OnSharedPreferenceChangeListener
    private lateinit var progressBar: ProgressBar

    var isEmpty: Boolean = false

    var rVExoPlayerHelper: RecyclerViewExoPlayerHelper? = null

    private var isFirstTimeVisit = true

    private val scrollToId: String?
        get() = activity?.intent?.getStringExtra(MainActivity.KEY_SCROLL_TO_ID)

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentFeedBinding =
        FragmentFeedBinding.inflate(layoutInflater)

    override fun provideViewModel(): FeedViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        removeVideoFragmentIfAny()

        viewModel = ViewModelProvider(this, viewModelFactory).get(FeedViewModel::class.java)
        trialHeaderVM = ViewModelProvider(requireActivity(), viewModelFactory).get(TrialHeaderVM::class.java)

        isNested = arguments?.getBoolean(IS_NESTED) ?: false
        showTopic = arguments?.let {
            if (it.containsKey(SHOW_TOPIC)) it.getBoolean(SHOW_TOPIC) else true
        } ?: true
        source = arguments?.getString(SOURCE, DEFAULT_SOURCE)

        viewModel.extraParams = getExtraParams()

        if (source == DEFAULT_SOURCE) {
            if (context != null && activity != null) {
                FirebaseAnalytics.getInstance(requireContext())
                    .setCurrentScreen(requireActivity(), TAG, TAG)
            }
            viewModel.storeFeedSeenCoreAction()
        }

        if (source == FeedViewModel.DEFAULT_SOURCE) {
            viewModel.getUserBanStatus()
        }

        setupUI()
        setupEventObservers()
        setupFeedTracking()
        loadFeedData(1)
        getVisibilityOfCreatePostActionViews()

        viewModel.userStatusPage = 1
        viewModel.userStatusType = UserStatusTypes.FOLLOWING
        loadStatusWidgetData()
        viewModel.setUpEventObservers()
    }

    private fun setupUI() {
        setupFeedRecyclerview()
        updateStudyGroupData()
        updateDnrData()

        val isUserBanned = defaultPrefs().getBoolean(Constants.USER_COMMUNITY_BAN, false)

        if (!isNested && !isUserBanned) {
            binding.btnCreatePost.setOnClickListener {
                viewModel.eventWith(
                    EventConstants.POST_CREATE_CLICK,
                    hashMapOf(Pair(EventConstants.SOURCE, "fab")),
                    ignoreSnowplow = true
                )
                CreatePostActivity.startActivity(requireActivity())
            }
            binding.fabScrollUp.hide()
        } else {
            binding.btnCreatePost.hide()
            binding.fabScrollUp.show()
            binding.fabScrollUp.setOnClickListener {
                binding.rvFeed.scrollToPosition(0)
            }
        }
        setupStatusWidget(isUserBanned)

        binding.ivNotification.setOnClickListener {
            activity?.startActivityForResult(
                NotificationCenterActivity.getStartIntent(
                    requireContext(),
                    NotificationCenterActivity.FEED
                ), NotificationCenterActivity.REQUEST_CODE_NOTIFICATION
            )
        }
        binding.toolbar.setVisibleState(source == DEFAULT_SOURCE)
        setNotificationCount()
    }

    private fun refreshUI() {
        refreshUI = false
        setupUI()
        loadFeedData(1)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onResume()
        if (refreshUI) {
            refreshUI()
        }

        if (source == FeedViewModel.DEFAULT_SOURCE) {
            if (!viewModel.userStatusList.isNullOrEmpty()) {
                statusAdapter?.resetDataset(viewModel.userStatusList)
            }
        }

        if (!isFirstTimeVisit) {
            viewModel.getExploreMoreWidget()
        }

        if (isFirstTimeVisit) {
            isFirstTimeVisit = false
        }

        rVExoPlayerHelper?.rvPlayerHelper?.resumePlayer()
    }

    private fun setNotificationCount() {

        val notificationCount = defaultPrefs()
            .getString(Constants.UNREAD_NOTIFICATION_COUNT, "0").orDefaultValue()
        if (notificationCount != "0") {
            binding.tvNotificationCount.show()
            binding.tvNotificationCount.text = notificationCount
        } else {
            binding.tvNotificationCount.hide()
        }
    }

    private fun setPreferenceChangeListener() {
        mListener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == Constants.UNREAD_NOTIFICATION_COUNT) {
                setNotificationCount()
            }
        }
        defaultPrefs().registerOnSharedPreferenceChangeListener(mListener)
    }

    override fun onStart() {
        super.onStart()
        setNotificationCount()
        setPreferenceChangeListener()
    }

    override fun setupObservers() {
        viewModel.pinnedPostLiveData.observe(viewLifecycleOwner) { outcome ->
            when (outcome) {
                is Outcome.ApiError -> {
                    progressBar.hide()
                    apiErrorToast(outcome.e)
                }
                is Outcome.Failure -> {
                    progressBar.hide()
                    apiErrorToast(outcome.e)
                }
                is Outcome.BadRequest -> {
                    progressBar.hide()
                    showApiErrorToast(activity)
                }
                is Outcome.Success -> {
                    if (outcome.data != null) {
                        if (outcome.data!!._widgetType == WidgetTypes.TYPE_WIDGET_AUTOPLAY) {
                            val autoPlayWidget =
                                outcome.data as ParentAutoplayWidget.ParentAutoplayWidgetModel
                            if (!autoPlayWidget._widgetData?.items.isNullOrEmpty()) {
                                viewModel.addPinnedPost(outcome.data!!)
                                adapter?.addItem(outcome.data!!, 0)
                            }
                        } else {
                            viewModel.addPinnedPost(outcome.data!!)
                            adapter?.addItem(outcome.data!!, 0)
                        }
                    }
                }
                else -> {}
            }
        }

        viewModel.normalFeedLiveData.observe(viewLifecycleOwner) { outcome ->
            when (outcome) {
                is Outcome.Progress -> {
                    scrollListener?.setDataLoading(true)
                    nestedScrollListener?.setDataLoading(true)
                    progressBar.show()
                }
                is Outcome.ApiError -> {
                    progressBar.hide()
                    apiErrorToast(outcome.e)
                }
                is Outcome.Failure -> {
                    progressBar.hide()
                    apiErrorToast(outcome.e)
                }
                is Outcome.BadRequest -> {
                    progressBar.hide()
                    showApiErrorToast(activity)
                }
                is Outcome.Success -> {
                    progressBar.hide()
                    var data: List<WidgetEntityModel<WidgetData, WidgetAction>?>? = null
                    // adding safe check so that no lint warning appears for the same.
                    @Suppress("ConstantConditionIf")
                    if (true) {
                        data = outcome.data.data
                    }
                    if (data == null || data.any { it == null } || data.any { it?._type.isNullOrEmpty() && it?._widgetType.isNullOrEmpty() }) {
                        with(FirebaseCrashlytics.getInstance()) {
                            FirebaseCrashlytics.getInstance()
                                .setCustomKey(ErrorConstants.DN_FATAL, true)
                            recordException(DnException("Inconsistent Feed Data"))
                        }
                    }
                    onDataFetched(
                        data?.filterNotNull()
                            ?.filter { !it._type.isNullOrEmpty() || !it._widgetType.isNullOrEmpty() })
                    viewModel.offsetCursor = outcome.data.offsetCursor
                }
            }
        }

        viewModel.exploreMoreWidget.observeK(
            viewLifecycleOwner,
            ::onExploreMoreWidgetReceived,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.createPostViewsVisibilityLiveData.observe(
            viewLifecycleOwner, {
                if (it is Outcome.Success) {
                    handleVisibilityOfCreatePostViews(it.data)
                }
            }
        )

        viewModel.liveDataCreateOneTapPost.observe(viewLifecycleOwner,
            {
                when (it) {
                    is Outcome.Success -> {
                        it.data.data?.let { baseResponse ->
                            deeplinkAction.performAction(requireContext(), baseResponse.deeplink)
                            if (baseResponse.message.isNotNullAndNotEmpty()) {
                                showToast(requireContext(), baseResponse.message!!)
                            }
                        }
                        loadFeedData(1)
                    }
                    is Outcome.Progress -> {
                        binding.progressBarNormal.isVisible = it.loading
                    }
                    is Outcome.ApiError -> {
                        apiErrorToast(it.e)
                    }
                    else -> {
                        showApiErrorToast(requireContext())
                    }
                }
            })
    }

    private fun handleVisibilityOfCreatePostViews(response: CreatePostVisibilityStatusResponse) {
        if (response.createPostStickyButton != null && response.createPostStickyButton) {
            if (!isNested) {
                binding.btnCreatePost.show()
            }
        }
        if (response.createButtonTopView != null && response.createButtonTopView) {
            adapter.showCreatePostHeaderView()
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun setupEventObservers() {
        appStateObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is ApplicationStateEvent -> viewModel.isApplicationBackground = !it.state
                    is FeedAttachmentInProgessEvent -> viewModel.isAttachingData = it.state
                    is WidgetClickedEvent -> {
                        if (it.extraParams?.get(SOURCE) == SOURCE_HOME) {
                            viewModel.eventWith(
                                EventConstants.HOME_PAGE_CAROUSEL_CLICKED,
                                it.extraParams
                            )
                        }
                    }
                    is WidgetSwipeEvent -> {
                        if (it.extraParams[SOURCE] == SOURCE_HOME) {
                            viewModel.eventWith(
                                EventConstants.HOME_PAGE_CAROUSEL_SWIPED,
                                it.extraParams,
                                ignoreSnowplow = true
                            )
                        }
                    }
                    is PrePurchaseCallingCardDismiss -> {
                        adapter.removeWidgetUsingType(WidgetTypes.TYPE_PRE_PURCHASE_CALL_CARD)
                        adapter.removeWidgetUsingType(WidgetTypes.TYPE_PRE_PURCHASE_CALL_CARD_V2)
                    }
                    is RefreshUI -> {
                        performAction(it)
                    }
                }
            }

        viewModel.eventLiveData.observe(viewLifecycleOwner) {
            val event = it.getContentIfNotHandled() ?: return@observe
            when (event) {
                is PostCreatedEvent -> {
                    if (event.post.username.isNullOrEmpty()) {
                        event.post.username = UserUtil.getStudentName()
                    }
                    if (event.post.studentImageUrl.isNullOrEmpty()) {
                        event.post.studentImageUrl = UserUtil.getProfileImage()
                    }
                    if (event.post.createdAt.isNullOrEmpty()) {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                        event.post.createdAt = sdf.format(Date())
                    }
                    adapter.addPostItem(event.post, 0)
                    lifecycleScope.launchWhenStarted {
                        delay(700)
                        binding.rvFeed.scrollToPosition(0)
                    }
                }
                is PostDeletedEvent -> {
                    adapter.removeItem(event.postId)
                }
                is ScrollTopEvent -> {
                    binding.rvFeed.scrollToPosition(0)
                }
                is ButtonWidgetClickEvent -> {
                    // remove the show more button widget
                    adapter.removeItemAt(FeedViewModel.MAX_CAROUSEL_WIDGETS_HOME)
                    // add the hidden feed items where show more button was present
                    adapter.addItems(
                        viewModel.getHiddenItemsAndUpdateFeed(),
                        FeedViewModel.MAX_CAROUSEL_WIDGETS_HOME
                    )
                    viewModel.eventWith(EventConstants.EVENT_HOME_CAROUSELS_SHOW_MORE)
                }
                is PostUpdatedEvent -> {
                    adapter.updatePostItem(event.updatedPostItem)
                }
                is UnbanRequested -> {
                    val isUserBanned =
                        defaultPrefs().getBoolean(Constants.USER_COMMUNITY_BAN, false)
                    statusAdapter?.refresh(isUserBanned)
                }
                is AdStatusUpdated -> {
                    statusAdapter?.updateAdStatusList(viewModel.statusAdsList)
                }
                is OneTapPostCreatedEvent -> {
                    loadFeedData(1)
                }
            }

        }
    }

    private val onScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                if (indexOfMyCourseWidget == -1) return
                if (trialMyCourseWidgetItem == null) return

                val findFirstCompletelyVisibleItemPosition =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()
                        ?.takeIf { it != -1 } ?: return

                val findLastVisibleItemPosition =
                    (recyclerView.layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition()
                        ?: return

                if (indexOfMyCourseWidget in findFirstCompletelyVisibleItemPosition..findLastVisibleItemPosition) {
                    isTrialHeaderShown = false
                    trialHeaderVM.showTrialHeader(false, trialMyCourseWidgetItem)
                } else {
                    isTrialHeaderShown = true
                    trialHeaderVM.showTrialHeader(true, trialMyCourseWidgetItem)
                }
            }
        }
    }

    private fun setupFeedRecyclerview() {
        progressBar = if (isNested) binding.progressBarNested else binding.progressBarNormal
        removeVideoFragmentIfAny()
        adapter = FeedAdapter(childFragmentManager, isNested, showTopic, source!!, this)
        val layoutManager =  LinearLayoutManagerWithSmoothScroller(
            context = requireContext(),
            orientation = LinearLayoutManager.VERTICAL,
            reverseLayout = false
        )
        val dividerItemDecoration = DividerItemDecoration(
            requireContext(),
            layoutManager.orientation
        )
        dividerItemDecoration.setDrawable(requireContext().getDrawable(R.drawable.divider_feed_items)!!)
        binding.rvFeed.addItemDecoration(dividerItemDecoration)
        binding.rvFeed.layoutManager = layoutManager
        binding.rvFeed.adapter = adapter

        binding.rvFeed.removeOnScrollListener(onScrollListener)
        binding.rvFeed.addOnScrollListener(onScrollListener)

        rVExoPlayerHelper = RecyclerViewExoPlayerHelper(
            mContext = requireContext(),
            id = R.id.rvPlayer,
            autoPlay = true,
            autoPlayInitiation = 100L,
            playStrategy = RvPlayStrategy.DEFAULT,
            defaultMute = true,
            loop = 0,
            useController = true,
            progressRequired = true,
            defaultMinBufferMs = MatchQuestionFragment.DEFAULT_MIN_BUFFER_MS,
            defaultMaxBufferMs = MatchQuestionFragment.DEFAULT_MAX_BUFFER_MS,
            reBufferDuration = 0 // For autoplay no need to buffer extra bytes
        ).apply {
            getPlayerView()?.apply {
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
            }
            makeLifeCycleAware(this@FeedFragment)
        }
        rVExoPlayerHelper?.attachToRecyclerView(binding.rvFeed)

        // only setup normal scroll listener if a parent scroller isn't set
        if (nestedScrollListener == null) {
            scrollListener =
                object : TagsEndlessRecyclerOnScrollListener(binding.rvFeed.layoutManager) {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val topReached = dy == 0 || recyclerView.computeVerticalScrollOffset() == 0
                        manageScrollUpFabAnimation(topReached)
                    }

                    override fun onLoadMore(current_page: Int) {
                        loadFeedData(current_page)
                    }
                }
            binding.rvFeed.addOnScrollListener(scrollListener!!)
        }
        nestedScrollListener?.setChildRecyclerView(binding.rvFeed)

        scrollListener?.setStartPage(1)
        nestedScrollListener?.setStartPage(1)

    }

    override fun registerVideoEngagementStatus(videoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {
    }

    private fun loadFeedData(page: Int) {
        val type = arguments?.getString(FeedViewModel.FEED_TYPE, FeedViewModel.NORMAL_FEED)
            ?: FeedViewModel.NORMAL_FEED
        if (type == FeedViewModel.NORMAL_FEED) {
            if (DEFAULT_SOURCE == source && page == 1) {
                viewModel.fetchPinnedPost()
            }
            viewModel.fetchNormalFeed(page, arguments)
        } else {
            viewModel.fetchFeed(page, arguments).observe(viewLifecycleOwner) { outcome ->
                when (outcome) {
                    is Outcome.Progress -> {
                        scrollListener?.setDataLoading(true)
                        nestedScrollListener?.setDataLoading(true)
                        progressBar.show()
                    }
                    is Outcome.ApiError -> {
                        progressBar.hide()
                        apiErrorToast(outcome.e)
                    }
                    is Outcome.Failure -> {
                        progressBar.hide()
                        apiErrorToast(outcome.e)
                    }
                    is Outcome.BadRequest -> {
                        progressBar.hide()
                        showApiErrorToast(activity)
                    }
                    is Outcome.Success -> {
                        progressBar.hide()

                        var data: List<WidgetEntityModel<WidgetData, WidgetAction>?>? = null
                        // adding safe check so that no lint warning appears for the same.
                        @Suppress("ConstantConditionIf")
                        if (true) {
                            data = outcome.data.data
                        }
                        if (data == null || data.any { it == null } || data.any { it?._type.isNullOrEmpty() && it?._widgetType.isNullOrEmpty() }) {
                            with(FirebaseCrashlytics.getInstance()) {
                                FirebaseCrashlytics.getInstance()
                                    .setCustomKey(ErrorConstants.DN_FATAL, true)
                                recordException(DnException("Inconsistent Feed Data"))
                            }
                        }

                        onDataFetched(
                            data?.filterNotNull()
                                ?.filter { !it._type.isNullOrEmpty() || !it._widgetType.isNullOrEmpty() })
                    }
                }
            }
        }
    }

    private fun getVisibilityOfCreatePostActionViews() {
        viewModel.getCreatePostViewsVisibilityStatus()
    }

    private var indexOfMyCourseWidget = -1
    private var indexOfScrollToId = -1
    private var trialMyCourseWidgetItem: MyCourseWidgetItem? = null

    private fun onDataFetched(feeds: List<WidgetEntityModel<*, *>>?) {
        indexOfMyCourseWidget = -1
        indexOfMyCourseWidget = -1
        trialMyCourseWidgetItem = null
        isTrialHeaderShown = false

        if (!DoubtnutApp.INSTANCE.isOnboardingCompleted && DoubtnutApp.INSTANCE.isOnboardingStarted) {
            feeds?.find { it is TopOptionsWidgetModel }.apply {
                if (this != null) {
                    val model = (this as TopOptionsWidgetModel)
                    model.isOnboardingEnabled = true
                }
            }
        }

        feeds?.forEachIndexed { index, widgetEntityModel ->
            if (scrollToId.isNotNullAndNotEmpty() && widgetEntityModel.id == scrollToId) {
                indexOfScrollToId = index
            }

            if (widgetEntityModel is PrePurchaseCallingCardModel) {
                widgetEntityModel.data.source = Constants.SOURCE_HOME
                widgetEntityModel.data.isDismissable = true
            }

            if (widgetEntityModel is PrePurchaseCallingCardModel2) {
                widgetEntityModel.data.source = Constants.SOURCE_HOME
            }

            if (widgetEntityModel is MyCourseWidgetModel) {
                indexOfMyCourseWidget = index
                trialMyCourseWidgetItem =
                    widgetEntityModel.data.items?.firstOrNull { it.trialTitle.isNotNullAndNotEmpty() }
            }

            if (widgetEntityModel is ScholarshipProgressCardWidgetModel) {
                restartJob?.cancel()
                restartJob = lifecycleScope.launchWhenResumed {
                    if (widgetEntityModel.data.startTimeInMillis != null && widgetEntityModel.data.startTimeInMillis!! > 0L) {
                        delay(widgetEntityModel.data.startTimeInMillis!!)
                        refreshUI()
                    }
                }
            }

            if (widgetEntityModel is DoubtFeedBannerWidget.Model) {
                val isBottomSheetExperimentEnabled = FeaturesManager.isFeatureEnabled(
                    requireContext(),
                    Features.DAILY_GOAL_HOME_PAGE_BOTTOM_SHEET
                )
                if (isBottomSheetExperimentEnabled) {
                    val data = widgetEntityModel.data
                    val lastShownTopic =
                        defaultPrefs().getString(Constants.LAST_DOUBT_FEED_BOTTOM_SHEET_TOPIC, null)

                    if (data.type == DoubtFeedBannerWidget.Data.TYPE_NO_TASK_GENERATED
                        && data.topic != lastShownTopic
                        && hasWindowFocus()
                        && DoubtnutApp.INSTANCE.isInAppDialogShowing.not()
                        && DoubtnutApp.INSTANCE.isRatingDialogStarted.not()
                    ) {
                        DoubtFeedBannerBottomSheetFragment
                            .newInstance(data)
                            .show(childFragmentManager, DoubtFeedBannerBottomSheetFragment.TAG)
                        defaultPrefs().edit {
                            putString(Constants.LAST_DOUBT_FEED_BOTTOM_SHEET_TOPIC, data.topic)
                        }
                    }
                }
            }

            if (widgetEntityModel is PracticeEnglishWidget.PracticeEnglishWidgetModel) {
                practiceWidgetPosition = index
            }
        }

        this.feeds = feeds
        scrollListener?.setDataLoading(false)
        nestedScrollListener?.setDataLoading(false)
        binding.tvEmpty.setText(viewModel.getEmptyFeedStringId())

        if (feeds == null) {
            isEmpty = true
            binding.tvEmpty.show()
            return
        }

        if (feeds.isEmpty()) {
            if (nestedScrollListener != null && nestedScrollListener!!.currentPage == 1) {
                isEmpty = true
                binding.tvEmpty.show()
            } else if (scrollListener != null && scrollListener!!.currentPage == 1) {
                isEmpty = true
                binding.tvEmpty.show()
            } else {
                binding.tvEmpty.hide()
            }
        } else {
            binding.tvEmpty.hide()
        }


        feeds.forEachIndexed { index, model ->
            if (model is SaleWidgetModel) {
                model.data.items?.forEach { _saleItem ->
                    _saleItem.responseAtTimeInMillis = System.currentTimeMillis()
                }
            } else if (model is CarouselListWidget.CarouselListWidgetModel){
                model.data.parentPosition = index
            }
        }

        feeds.find { it is NudgeWidget.NudgeWidgetModel }.apply {
            if (this != null) {
                val model = (this as NudgeWidget.NudgeWidgetModel)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_NUDGE_VIEW,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.NUDGE_ID, model.data.widgetId.orEmpty())
                            put(EventConstants.NUDGE_TYPE, model.data.nudgeType.orEmpty())
                            putAll(model.extraParams ?: hashMapOf())
                        }
                    ))
            }
        }

        if (feeds.isNotEmpty()) {
            val isEmptyAdapter = adapter.feedItems.size == 0
            val pageNumber = when {
                nestedScrollListener != null -> {
                    nestedScrollListener!!.currentPage
                }
                scrollListener != null -> {
                    scrollListener!!.currentPage
                }
                else -> 0
            }

            val updatedFeeds = viewModel.filterAndUpdateFeeds(feeds, pageNumber)
            viewModel.addExtraParamsToFeedData(updatedFeeds)
            adapter.updateData(updatedFeeds)
            viewModel.getExploreMoreWidget()

            if (isEmptyAdapter) {
                rVExoPlayerHelper?.playCurrent(binding.rvFeed)
                if (indexOfScrollToId != -1) {
                   activity?.intent?.removeExtra(MainActivity.KEY_SCROLL_TO_ID)
                   lifecycleScope.launchWhenResumed {
                       delay(700)
                       binding.rvFeed.scrollToPosition(indexOfScrollToId)
                   }
                }
            }
        } else {
            scrollListener?.isLastPageReached = true
            nestedScrollListener?.setLastPageReached(true)
        }
    }

    private fun getExtraParams(): Map<String, Any> =
        hashMapOf(Constants.SOURCE to source.orEmpty())

    private fun setupFeedTracking() {
        viewTrackingBus = ViewTrackingBus(
            { viewModel.trackView(it) },
            {}
        )

        adapter.registerViewTracking(viewTrackingBus!!)

        viewModel.debugLogLiveData.observe(viewLifecycleOwner) { log ->
            log.getContentIfNotHandled()?.let {
                if (binding.debugLogView.isDebugLogEnabled()) {
                    binding.debugLogView.addLog(it)
                }
            }
        }

        binding.rvFeed.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy.absoluteValue > 150) viewTrackingBus?.pause()
                else viewTrackingBus?.resume()
            }
        })

        viewModel.setupEngagementTracking()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewTrackingBus?.unsubscribe()
        appStateObserver?.dispose()
    }

    fun setParentScroller(recyclerView: RecyclerView) {
        this.nestedScrollListener = object : EndlessNestedScrollListener() {
            override fun onLoadMore(current_page: Int) {
                if (isAdded) {
                    loadFeedData(current_page)
                }
            }
        }
        recyclerView.addOnScrollListener(this.nestedScrollListener!!)
    }

    override fun onStop() {
        super.onStop()
        trackPendingFeedImpressions()
        if (this::mListener.isInitialized) {
            defaultPrefs().unregisterOnSharedPreferenceChangeListener(mListener)
        }
        viewModel.onStop()
    }

    /*
    regular scrolling impressions events are captured by the adapter itself.
    In some cases, adapter is not able to intercept possible view no longer visible events such as
    app is going in background (since onViewDetachedToWindow is never called). We trigger such
    pending viewRemoved events manually in onStop by finding the currently visible views in adapter
    that will no longer be visible to user
     */
    private fun trackPendingFeedImpressions() {
        val layoutManager = binding.rvFeed.layoutManager as? LinearLayoutManager ?: return
        val visibleItemsRange =
            layoutManager.findFirstCompletelyVisibleItemPosition()..layoutManager.findLastVisibleItemPosition()
        visibleItemsRange.forEach {
            if (it != -1)
                adapter.trackViewRemoved(
                    binding.rvFeed.findViewHolderForAdapterPosition(it) as FeedAdapter.FeedViewHolder?
                )
        }
    }

    private fun removeVideoFragmentIfAny() {
        try {
            childFragmentManager.fragments.filterIsInstance<VideoFragment>().forEach {
                childFragmentManager.beginTransaction().apply {
                    remove(it)
                    commitNow()
                }
            }
        } catch (e: Exception) {
            //ignore
        }
        childFragmentManager.executePendingTransactions()
    }

    private fun manageScrollUpFabAnimation(topReached: Boolean) {
        if (!isNested) return

        if (topReached) {
            if (scaleDownAnimator.isRunning || binding.fabScrollUp.scaleX == 0F) return
            scaleDownAnimator.start()

        } else {
            if (scaleUpAnimator.isRunning || binding.fabScrollUp.scaleX == 1F) return
            scaleUpAnimator.start()
        }
    }

    private val scaleDownAnimator by lazy {
        val objectAnimator = ValueAnimator.ofFloat(1f, 0f)
        objectAnimator.addUpdateListener {
            view ?: return@addUpdateListener
            binding.fabScrollUp.scaleX = it.animatedValue as Float
            binding.fabScrollUp.scaleY = it.animatedValue as Float
        }
        objectAnimator
    }

    private val scaleUpAnimator by lazy {
        val objectAnimator = ValueAnimator.ofFloat(0f, 1f)
        objectAnimator.addUpdateListener {
            view ?: return@addUpdateListener
            binding.fabScrollUp.scaleX = it.animatedValue as Float
            binding.fabScrollUp.scaleY = it.animatedValue as Float
        }
        objectAnimator
    }

    override fun performAction(action: Any) {
        when (action) {
            is HandleDeeplink -> {
                deeplinkAction.performAction(requireContext(), action.deeplink , source.orEmpty())
            }
            is FeedDNVideoWatched -> {
                viewModel.sendVideoEngagementLogs(action.feedItem, action.videoEngagementStats)
            }
            is FeedPremiumVideoItemVisible -> {
                action.feedItem?.let {
                    var viewForm = Constants.PAGE_COMMUNITY
                    if (it.type == FeedPostTypes.TYPE_DN_PAID_VIDEO) {
                        viewForm = EventConstants.PAGE_PAID_CONTENT_FEED
                    }
                    if (it.viewAnswerData == null) {
                        viewModel.fetchViewAnserData(it, viewForm)
                    }
                }
            }
            is FeedPinnedVideoItemVisible -> {
                action.feedItem?.let {
                    val viewForm = Constants.PAGE_COMMUNITY
                    if (it.viewAnswerData == null && !it.isLoadingViewAnswerData) {
                        it.isLoadingViewAnswerData = true
                        viewModel.fetchViewAnserData(it, viewForm)
                    }
                }
            }
            is MuteAutoPlayVideo -> {
                rVExoPlayerHelper?.isMute = action.isMute
            }

            is FeedPremiumBlockedScreenVisible -> {
                viewModel.postPremiumVideoBlockedEvent(hashMapOf<String, Any>().apply {
                    put(EventConstants.VIDEO_VIEW_ID, action.viewAnswerData.viewId)
                    put(
                        EventConstants.COURSE_ID,
                        action.viewAnswerData.premiumVideoBlockedData?.courseId
                            ?: 0
                    )
                    put(
                        EventConstants.PAID_USER,
                        action.viewAnswerData.isPremium && action.viewAnswerData.isVip
                    )
                    put(EventConstants.CTA_VIEWED, true)
                    put(EventConstants.CTA_CLICKED, 1)
                    put(EventConstants.VIEW_FROM, EventConstants.PAGE_PAID_CONTENT_FEED)
                })
            }
            is RefreshUI -> {
                refreshUI = true
            }
            is OnHomeWorkListClicked -> {
                if (action.status) {
                    HomeWorkSolutionActivity.startActivity(requireContext(), true, action.qid)
                } else {
                    startActivityForResult(
                        HomeWorkActivity.getIntent(requireContext(), action.qid),
                        1
                    )
                }
            }
            is LoadMoreRecentStatus -> {
                viewModel.fetchRecentStatusList()
            }
            is GetFollowerWidgetItems -> {
                viewModel.getFollowerWidgetItems()
            }
            is UnbanRequested -> {
                startActivityForResult(
                    UnbannedRequestActivity.getStartIntent(requireContext()),
                    100
                )
            }
            is RemoveP2PHomeWidget -> {
                if (viewModel.indexOfP2pWidget != -1) {
                    adapter.removeItemAt(viewModel.indexOfP2pWidget)
                }
            }
            is OnNudgeClosed -> {
                feeds?.filterIsInstance<NudgeWidget.NudgeWidgetModel>().apply {
                    this?.forEach {
                        if (it.data.widgetId == action.nudgeId) {
                            adapter.removeWidget(it)
                        }
                    }
                }
            }
            is RemoveWidget -> {
                action.widget?.let { widget ->
                    if (feeds?.contains(widget) == true) {
                        adapter.removeWidget(widget)
                    }
                }
            }
            is AskPermission -> {
                if (hasAudioRecordingPermission()) {
                    val practiceEnglishWidget = binding.rvFeed.layoutManager?.findViewByPosition(practiceWidgetPosition) as? PracticeEnglishWidget
                    practiceEnglishWidget?.onPermissionFlowCompleted(true)
                } else {
                    requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
            is OnAutoPostItemSelected -> {
                viewModel.createOneTapPost(action.id)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        OneTapPostsListActivity.EVENT_TAG_ONE_TAP_POST,
                        hashMapOf<String, Any>(
                            EventConstants.SOURCE to source.orEmpty(),
                            EventConstants.ID to action.id
                        )
                    )
                )
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == BaseActivity.RESULT_OK) {
            viewModel.getUserBanStatus()
        } else if (resultCode == BaseActivity.RESULT_OK) {
            setupUI()
            loadFeedData(1)
        }
    }

    private fun setupStatusWidget(isUserBanned: Boolean) {
        viewModel.initStatusSet()
        if (source == FeedViewModel.DEFAULT_SOURCE) {
            statusAdapter = UserStatusListAdapter(
                context = requireContext(),
                items = ArrayList(),
                actionPerformer = this,
                isUserBanned = isUserBanned,
                analyticsPublisher = analyticsPublisher
            )
            binding.rvStatus.adapter = statusAdapter
            binding.rvStatus.setHasFixedSize(true)
            binding.rvStatus.show()
            binding.statusDivider.show()

        } else {
            binding.rvStatus.hide()
            binding.statusDivider.hide()
        }
    }

    private fun loadStatusWidgetData() {
        view ?: return
        context?.let {
            if (source == FeedViewModel.DEFAULT_SOURCE) {
                binding.rvStatus.show()
                binding.statusDivider.show()
                viewModel.fetchStatus().observe(viewLifecycleOwner) {
                    when (it) {
                        is Outcome.Progress -> {
                            statusScrollListener?.setDataLoading(true)
                            progressBar.show()
                        }
                        is Outcome.ApiError -> {
                            progressBar.hide()
                            apiErrorToast(it.e)
                        }
                        is Outcome.Failure -> {
                            progressBar.hide()
                            apiErrorToast(it.e)
                        }
                        is Outcome.BadRequest -> {
                            progressBar.hide()
                            showApiErrorToast(activity)
                        }
                        is Outcome.Success -> {
                            progressBar.hide()
                            onStatusDataFetched(it.data.data)
                        }
                    }
                }
            } else {
                binding.rvStatus.hide()
                binding.statusDivider.hide()
            }
        }
    }

    private fun onStatusDataFetched(statusResponse: StatusApiResponse) {
        val type = viewModel.userStatusType
        viewModel.fetchStatusAds()
        viewModel.userStatusOffsetCursor = statusResponse.offsetCursor.orEmpty()
        val statusList = statusResponse.statusData?.filter { it.statusItem != null }

        if (isStatusDataInsufficient(statusList, statusResponse.pageSize ?: 50)) {
            if (type == UserStatusTypes.FOLLOWING) {
                viewModel.userStatusType = UserStatusTypes.RANDOM
                if (statusList.isNullOrEmpty()) {
                    loadStatusWidgetData()
                    return
                }
            } else {
                if (statusList.isNullOrEmpty()) {
                    statusScrollListener?.setLastPageReached(true)
                } else {
                    viewModel.userStatusPage++
                }
            }
        }

        if (statusScrollListener == null) {
            statusScrollListener =
                object :
                    TagsEndlessHorizontalRecyclerOnScrollListener(binding.rvStatus.layoutManager) {
                    override fun onLoadMore(current_page: Int) {
                        loadStatusWidgetData()
                    }
                }
            binding.rvStatus.addOnScrollListener(statusScrollListener!!)
            statusScrollListener?.setStartPage(1)
        }
        statusScrollListener?.setDataLoading(false)

        if (!statusList.isNullOrEmpty()) {
            statusAdapter?.updateList(
                viewModel.filterAndUpdateStatus(
                    type,
                    statusList
                ) as ArrayList<UserStatus>
            )
        }
    }

    private fun isStatusDataInsufficient(
        statusList: List<UserStatus>?,
        pageSize: Int
    ): Boolean {
        return (statusList.isNullOrEmpty() || statusList.size < pageSize)
    }

    override fun onSharedPreferenceChanged(
        sharedPreferences: SharedPreferences?,
        key: String?
    ) {
        if (key.equals(Constants.IGNORE_STUDY_DOST) && defaultPrefs().getBoolean(
                Constants.IGNORE_STUDY_DOST,
                false
            )
        ) {
            adapter.removeWidget(WidgetTypes.TYPE_WIDGET_STUDY_DOST)
        }
    }

    private fun updateStudyGroupData() {
        val studyGroupData = viewModel.getStudyGroupData()
        val isStudyGroupFeatureEnabled = studyGroupData != null
        if (isStudyGroupFeatureEnabled) {
            binding.ivStudyGroup.loadImage(studyGroupData?.image)
            binding.ivStudyGroup.setOnClickListener {
                val deeplink = studyGroupData?.deeplink ?: return@setOnClickListener
                viewModel.eventWith(
                    EventConstants.SG_FEED_CLICK,
                    hashMapOf(),
                    ignoreSnowplow = true
                )
                deeplinkAction.performAction(requireContext(), deeplink)
            }
        }
    }

    private fun updateDnrData() {
        val dnr = viewModel.getDnrData()
        if (dnr != null) {
            binding.ivDnrRupya.loadImage(dnr?.image)
            binding.ivDnrRupya.setOnClickListener {
                val deeplink = dnr?.deeplink ?: return@setOnClickListener
                viewModel.eventWith(
                    EventConstants.FEED_DNR_ICON_CLICK,
                    hashMapOf(),
                    ignoreSnowplow = true
                )
                deeplinkAction.performAction(requireContext(), deeplink)
            }
        } else {
            binding.ivDnrRupya.hide()
        }
    }

    private fun onExploreMoreWidgetReceived(data: ExploreMoreWidgetResponse) {
        if (source != FeedViewModel.SOURCE_HOME) return
        // if explore more widget already present remove it and add it again
        if (adapter.feedItems.size > 0) {
            adapter.removeWidgetUsingType(WidgetTypes.TYPE_WIDGET_EXPLORE_MORE)
        }
        if (adapter.feedItems.size > data.position) {
            adapter.addItems(data.items, data.position)
        }

        // Update Index as explore item was added here.
        adapter.feedItems.forEachIndexed { index, widgetEntityModel ->
            if (scrollToId.isNotNullAndNotEmpty() && widgetEntityModel.id == scrollToId) {
                indexOfScrollToId = index
            }

            if (widgetEntityModel is PracticeEnglishWidget.PracticeEnglishWidgetModel) {
                practiceWidgetPosition = index
            }
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(parentFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBarNested.setVisibleState(state)
    }
}
