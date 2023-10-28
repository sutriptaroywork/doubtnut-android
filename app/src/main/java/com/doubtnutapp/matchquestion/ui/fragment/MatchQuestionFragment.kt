package com.doubtnutapp.matchquestion.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.observer.SingleEventObserver
import com.doubtnut.core.utils.immediateParentViewModelStoreOwner
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.MatchResultFragmentBinding
import com.doubtnutapp.matchquestion.listener.BookFeedbackListener
import com.doubtnutapp.matchquestion.listener.FilterDataListener
import com.doubtnutapp.matchquestion.listener.P2pConnectListener
import com.doubtnutapp.matchquestion.model.MatchQuestion
import com.doubtnutapp.matchquestion.model.MatchQuestionViewItem
import com.doubtnutapp.matchquestion.model.MatchedQuestionsList
import com.doubtnutapp.matchquestion.ui.MatchPageConstants
import com.doubtnutapp.matchquestion.ui.activity.MatchQuestionActivity
import com.doubtnutapp.matchquestion.ui.adapter.MatchQuestionListAdapter
import com.doubtnutapp.matchquestion.ui.viewholder.ShowMoreYoutubeVideoViewHolder
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionFragmentViewModel
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel
import com.doubtnutapp.rvexoplayer.RecyclerViewExoPlayerHelper
import com.doubtnutapp.rvexoplayer.RvMuteStatus
import com.doubtnutapp.rvexoplayer.RvPlayStrategy
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.mediahelper.ExoPlayerCacheManager
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.widgetmanager.WidgetTypes
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.uxcam.UXCam


class MatchQuestionFragment :
    BaseBindingFragment<MatchQuestionFragmentViewModel, MatchResultFragmentBinding>(),
    ActionPerformer, FilterDataListener {

    companion object {
        const val TAG = "MatchQuestionFragment"
        private const val PARAM_KEY_ASKED_QUESTION_ID = "askedQuestionId"
        private const val PARAM_KEY_OCR_TEXT = "ocr_text"
        private const val PARAM_KEY_AUTOPLAY = "auto_play"
        private const val PARAM_KEY_AUTOPLAY_DURATION = "auto_play_duration"
        private const val PARAM_KEY_AUTOPLAY_INITIATION = "auto_play_initiation"

        const val DEFAULT_MIN_BUFFER_MS = 5000
        const val DEFAULT_MAX_BUFFER_MS = 5000

        private const val SCROLL_ANIMATION_VISIBILITY_DELAY = 3000L

        private const val UXCAM_TAG = "match_question"
        private const val UXCAM_EVENT_SCROLL_ANIMATION = "scroll_animation"

        fun newInstance(
            askedQuestionId: String,
            ocrText: String?,
            autoPlay: Boolean,
            autoPlayDuration: Long?,
            autoPlayInitiation: Long?
        ) =
            MatchQuestionFragment()
                .also {
                    it.arguments = bundleOf(
                        PARAM_KEY_ASKED_QUESTION_ID to askedQuestionId,
                        PARAM_KEY_OCR_TEXT to ocrText,
                        PARAM_KEY_AUTOPLAY to autoPlay,
                        PARAM_KEY_AUTOPLAY_DURATION to autoPlayDuration,
                        PARAM_KEY_AUTOPLAY_INITIATION to autoPlayInitiation
                    )
                }
    }

    private var p2pConnectListener: P2pConnectListener? = null
    private var bookFeedbackListener: BookFeedbackListener? = null

    private lateinit var matchQuestionViewModel: MatchQuestionViewModel

    private val exoPlayerCacheManager: ExoPlayerCacheManager by lazy {
        ExoPlayerCacheManager.getInstance(requireContext()).apply {
            clearAllCache()
        }
    }

    private val askedQuestionId: String by lazy {
        arguments?.getString(PARAM_KEY_ASKED_QUESTION_ID) ?: ""
    }
    private val ocrText: String? by lazy {
        arguments?.getString(PARAM_KEY_OCR_TEXT)
    }
    private val autoPlay: Boolean by lazy {
        arguments?.getBoolean(PARAM_KEY_AUTOPLAY) ?: false
    }
    private val autoPlayDuration: Long? by lazy {
        arguments?.getLong(PARAM_KEY_AUTOPLAY_DURATION)
    }
    private val autoPlayInitiation: Long? by lazy {
        arguments?.getLong(PARAM_KEY_AUTOPLAY_INITIATION)
    }

    // Start - Scroll animation handler
    private val scrollAnimationHandler: Handler by lazy {
        Handler(Looper.getMainLooper())
    }
    private val scrollAnimationRunnable = Runnable {
        mBinding?.scrollAnimationLayout?.show()
    }
    // End - Scroll animation handler

    private val matchQuestionData = mutableListOf<MatchQuestionViewItem>()

    private val matchQuestionListAdapter: MatchQuestionListAdapter by lazy {
        MatchQuestionListAdapter(
            matchResults = mutableListOf(),
            actionsPerformer = this@MatchQuestionFragment,
            autoPlay = autoPlay,
            autoPlayDuration = autoPlayDuration
        )
    }

    var rVExoPlayerHelper: RecyclerViewExoPlayerHelper? = null

    override fun performAction(action: Any) {
        when (action) {
            is PlayYoutubeResult -> {
                viewModel.sendEvent(
                    EventConstants.EVENT_YT_VIDEO_CLICKED,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.QUESTION_ASKED_ID, askedQuestionId)
                        put(EventConstants.ITEM_POSITION, action.position)
                    })
                playYoutubeResultVideo(action.videoId)
            }

            is ShowMoreMatches -> {
                val showMoreItem = action.showMoreItem
                showMoreItem.status = ShowMoreYoutubeVideoViewHolder.SHOW_PROGRESS_BAR
                matchQuestionListAdapter.updateItemAtPosition(
                    item = action.showMoreItem,
                    position = action.position
                )
                viewModel.getYoutubeResults(
                    questionId = matchQuestionViewModel.parentQuestionId,
                    ocr = ocrText.orEmpty()
                )
            }

            is AutoPlayComplete -> matchQuestionListAdapter.updateItemAtPosition(
                item = action.matchedQuestion,
                position = action.position
            )

            is MuteAutoPlayVideo -> viewModel.muteAutoPlay(action.isMute)

            is ConnectToPeer -> {
                openP2pHostIntroductionScreen()
            }

            is MatchPageFeatureAction -> {
                when (action.actionToPerform) {
                    MatchQuestionViewModel.MatchPageFeature.P2P.feature -> {
                        openP2pHostIntroductionScreen()
                    }
                    MatchQuestionViewModel.MatchPageFeature.BOOK_FEEDBACK.feature -> {
                        bookFeedbackListener?.openBookFeedbackDialog()
                    }
                }
            }

            is TextWidgetClick -> { // Show More Items click
                if (action.widgetData.type == MatchPageConstants.LOAD_MORE_SOLUTION) {
                    matchQuestionViewModel.matchQuestionBannerLiveData.value?.let {
                        matchQuestionData.clear()
                        val matchResultsWithNudges =
                            matchQuestionViewModel.getMatchResultsWithNudges(
                                isP2pConnected = matchQuestionViewModel.isDoubtP2PConnected,
                                isFeedbackSubmitted = matchQuestionViewModel.feedbackSubmitted
                            )
                        matchQuestionData.addAll(matchResultsWithNudges)
                        matchQuestionListAdapter.updateItems(matchQuestionData)
                        // set it false, so that next time while observing matchResultLiveData complete list should be shown
                        matchQuestionViewModel.shouldShowPartialResults = false
                    }
                    viewModel.sendEvent(
                        event = "${MatchPageConstants.MP_EVENT_TAG}_${EventConstants.EVENT_SHOW_MORE_CLICKED}",
                        params = HashMap()
                    )
                }
            }

            else -> viewModel.handleAction(action)
        }
    }

    private fun openP2pHostIntroductionScreen() {
        p2pConnectListener?.showP2pHostScreenToConnect()
        matchQuestionViewModel.sendEvent(EventConstants.P2P_CLICKED_ON_BOTTOM_PAGE)
    }

    fun setP2pConnectListener(p2PConnectListener: P2pConnectListener) {
        this.p2pConnectListener = p2PConnectListener
    }

    fun setBookFeedbackListener(bookFeedbackListener: BookFeedbackListener) {
        this.bookFeedbackListener = bookFeedbackListener
    }

    private fun playYoutubeResultVideo(youtubeId: String) {
        VideoPageActivity.startActivity(
            context = requireContext(),
            questionId = viewModel.youtubeQid?.toString() ?: "",
            parentId = matchQuestionViewModel.parentQuestionId,
            page = Constants.PAGE_YT_ASK,
            ocr = ocrText,
            youtube_id = youtubeId
        ).apply {
            startActivity(this)
        }
    }

    private fun updateUi(matchQuestion: MatchQuestion) {

        // Set all the result - will be passed as argument to video screen
        viewModel.matchResults = matchQuestionViewModel.unmodifiedMatchResults

        val matchResults =
            when {
                matchQuestionViewModel.shouldShowPartialResults && matchQuestion.partialMatchedQuestions.isNotEmpty() -> {
                    matchQuestion.partialMatchedQuestions
                }
                else -> {
                    matchQuestion.matchedQuestions
                }
            }

        matchQuestionData.clear()
        matchQuestionData.addAll(matchResults)

        // Cache 3 videos from starting - chances of clicking first 3 video is higher
        preLoadVideos()

        matchQuestionListAdapter.updateItems(matchQuestionData)

        setUpAutoPlay()

        setUpScrollListener()
    }

    private fun setUpScrollListener() {
        mBinding?.matchResultRecyclerView?.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {

            private var scrollingDirection: MatchQuestionViewModel.ScrollDirection? =
                MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        matchQuestionViewModel.shouldLockBottomSheet.value =
                            !(!recyclerView.canScrollVertically(-1) && matchQuestionViewModel.shouldLockBottomSheet.value != false)

                        // Send event when P2P ViewHolder is visible after user scrolls down.
                        val lastVisibleItem =
                            (recyclerView.layoutManager as? LinearLayoutManager)?.findLastVisibleItemPosition()
                                ?: return
                        val doubtP2pIndex =
                            matchQuestionListAdapter.findDoubtPeCharchaViewItem(
                                widgetType = WidgetTypes.TYPE_MATCH_PAGE_EXTRA_FEATURE,
                                feature = "p2p"
                            )
                        if (doubtP2pIndex <= lastVisibleItem && doubtP2pIndex != RecyclerView.NO_POSITION) {
                            matchQuestionViewModel.sendEvent(EventConstants.P2P_VISIBLE_ON_BOTTOM_PAGE)
                        }
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                // Hide Bottom Layer when scroll down and
                // show it when scrolls up
                when {
                    // Scroll Down
                    dy > 0 -> {
                        // check if recyclerview can scroll down or reached last item

                        if (matchQuestionViewModel.shouldLockBottomSheet.value != true)
                            matchQuestionViewModel.shouldLockBottomSheet.value = true

                        when {
                            recyclerView.canScrollVertically(1) -> {
                                // avoid setting SCROLL_DOWN after reaching last item
                                // because even after reaching last item SCROLL_DOWN event fires
                                if (scrollingDirection != MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN_NONE) {
                                    // Scrolling Down
                                    scrollingDirection =
                                        MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN
                                }

                            }
                            else -> {
                                // When reached to last item
                                scrollingDirection =
                                    MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN_NONE
                            }
                        }

                        matchQuestionViewModel.matchPageScrollDirection.postValue(scrollingDirection)

                        // Hide scroll animation on this page if user scrolls
                        matchQuestionViewModel.setScrollAnimationVisibility(false)
                        scrollAnimationHandler.removeCallbacks(scrollAnimationRunnable)
                    }
                    // Scroll Up
                    dy < 0 -> {
                        // check if recyclerview can scroll up or reached first item
                        if (recyclerView.canScrollVertically(-1)) {
                            // Scrolling Up
                            scrollingDirection = MatchQuestionViewModel.ScrollDirection.SCROLL_UP
                        } else {
                            // avoid setting SCROLL_UP_NONE until scrolls down
                            // because after loading result SCROLL_UP_NONE event fires
                            if (scrollingDirection != MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN) {
                                scrollingDirection =
                                    MatchQuestionViewModel.ScrollDirection.SCROLL_UP_NONE
                            }
                        }

                        matchQuestionViewModel.matchPageScrollDirection.postValue(scrollingDirection)

                        if (!recyclerView.canScrollVertically(-1) && matchQuestionViewModel.shouldLockBottomSheet.value != false)
                            matchQuestionViewModel.shouldLockBottomSheet.value = false
                    }
                }
            }
        })
    }

    override fun setupObservers() {
        super.setupObservers()
        matchQuestionViewModel.matchResultsLiveData.observeK(
            viewLifecycleOwner,
            ::updateUi,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )

        viewModel.navigateLiveData.observeEvent(viewLifecycleOwner, this::openScreen)

        matchQuestionViewModel.isAdvancedSearchEnabled.observe(viewLifecycleOwner) {
            viewModel.isAdvancedSearchEnabled = it
        }

        matchQuestionViewModel.preventExitMatchPage.observe(viewLifecycleOwner) {
            viewModel.preventMatchPageExit = it
        }

        matchQuestionViewModel.matchesFromNotification.observe(viewLifecycleOwner) {
            viewModel.matchesFromNotification = it
        }

        matchQuestionViewModel.matchesFromInApp.observe(viewLifecycleOwner) {
            viewModel.matchesFromNInApp = it
        }

        matchQuestionViewModel.isDialogShowingLiveData.observe(
            viewLifecycleOwner
        ) { isDialogShowing ->
            rVExoPlayerHelper?.canPlay = isDialogShowing.not()
        }

        viewModel.youtubeResults.observe(viewLifecycleOwner) {
            val showMoreItem = matchQuestionListAdapter.findShowMoreItemIfAny()
            if (it.isNotEmpty()) {
                matchQuestionListAdapter.addYoutubeResults(it)
                showMoreItem.first?.status = ShowMoreYoutubeVideoViewHolder.HIDE_PROGRESS_BAR
                matchQuestionListAdapter.removeShowMoreItemIfAny()
                viewModel.sendEvent(
                    EventConstants.EVENT_YT_RESULTS_VIEWED,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.QUESTION_ASKED_ID, askedQuestionId)
                        put(EventConstants.ITEM_COUNT, it.size)
                    })
            } else {
                showMoreItem.first?.status = ShowMoreYoutubeVideoViewHolder.NO_RESULT_FOUND
                if (showMoreItem.second != RecyclerView.NO_POSITION) {
                    matchQuestionListAdapter.notifyItemChanged(showMoreItem.second)
                }
                viewModel.sendEvent(
                    EventConstants.EVENT_YT_RESULTS_NOT_FOUND,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.QUESTION_ASKED_ID, askedQuestionId)
                    })
            }
        }

        viewModel.isAutoPlayMute.observe(viewLifecycleOwner) { isMute ->
            rVExoPlayerHelper?.isMute = isMute
            matchQuestionData.filterIsInstance<MatchedQuestionsList>().forEach {
                it.isMute = isMute
            }
            matchQuestionListAdapter.apply {
                val muteStatus = RvMuteStatus(isMute)
                notifyItemRangeChanged(0, this.itemCount, muteStatus)
            }
        }

        matchQuestionViewModel.autoPlayState.observe(viewLifecycleOwner) {
            mBinding?.apply {
                if (it) {
                    rVExoPlayerHelper?.attachToRecyclerView(matchResultRecyclerView)
                    rVExoPlayerHelper?.playCurrent(matchResultRecyclerView)
                } else {
                    rVExoPlayerHelper?.detachFromRecyclerView(matchResultRecyclerView)
                    rVExoPlayerHelper?.rVExoPlayerView?.removePlayer()
                    rVExoPlayerHelper?.currentlyPlayingId = ""
                }
            }
        }

        matchQuestionViewModel.playerState.observe(viewLifecycleOwner) {
            when (it) {
                MatchQuestionViewModel.PlayerState.PAUSE -> {
                    rVExoPlayerHelper?.canPlay = false
                }
                MatchQuestionViewModel.PlayerState.RESUME -> {
                    rVExoPlayerHelper?.rvPlayerHelper?.setAudioAttributes(
                        viewModel.getAudioAttributes(),
                        true
                    )
                    rVExoPlayerHelper?.canPlay = true
                }
                else -> {
                }
            }
        }

        matchQuestionViewModel.removeFeatureWidget.observe(viewLifecycleOwner, SingleEventObserver {
            matchQuestionListAdapter.removeWidget(
                widgetType = WidgetTypes.TYPE_MATCH_PAGE_EXTRA_FEATURE,
                feature = it.feature
            )
        })

        // Scroll Animation
        matchQuestionViewModel.showScrollAnimation.observe(
            viewLifecycleOwner,
            SingleEventObserver { visibility ->
                when (visibility && matchQuestionViewModel.scrollAnimation == true) {
                    true -> {
                        UXCam.logEvent("${UXCAM_TAG}_${UXCAM_EVENT_SCROLL_ANIMATION}")
                        scrollAnimationHandler.postDelayed(
                            scrollAnimationRunnable,
                            SCROLL_ANIMATION_VISIBILITY_DELAY
                        )
                    }
                    else -> {
                        mBinding?.scrollAnimationLayout?.setVisibleState(false)
                    }
                }
            })
    }

    private fun openScreen(navigationModel: NavigationModel) {
        // Reuse method written in activity's viewModel i.e. MatchQuestionViewModel to navigate
        matchQuestionViewModel.onNavigateToScreen(navigationModel)
        matchQuestionViewModel.onSolutionWatched()
    }

    private fun updateProgress(state: Boolean) {}

    private fun unAuthorizeUserError() {}

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

    private fun setUpAutoPlay() {
        /*
          if AutoPlay, create exoplayer helper instance and attach it
          to recyclerview after setting layout manager and adapter.
         */

        mBinding?.apply {
            if (autoPlay) {

                viewModel.sendEvent(EventConstants.MATCH_PAGE_AUTO_PLAY_SHOWN, hashMapOf(), true)

                rVExoPlayerHelper = RecyclerViewExoPlayerHelper(
                    mContext = requireContext(),
                    id = R.id.rvPlayer,
                    autoPlay = true,
                    autoPlayInitiation = autoPlayInitiation ?: 500L,
                    playStrategy = RvPlayStrategy.FULL_VISIBLE,
                    defaultMute = false,
                    loop = 0,
                    progressRequired = true,
                    defaultMinBufferMs = autoPlayDuration?.toInt() ?: DEFAULT_MIN_BUFFER_MS,
                    defaultMaxBufferMs = autoPlayDuration?.toInt() ?: DEFAULT_MAX_BUFFER_MS,
                    reBufferDuration = 0 // For autoplay no need to buffer extra bytes
                ).apply {
                    getPlayerView()?.apply {
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    }
                    makeLifeCycleAware(this@MatchQuestionFragment)
                }
            }

            matchResultRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            matchResultRecyclerView.adapter = matchQuestionListAdapter
            matchResultRecyclerView.itemAnimator = null
            matchResultRecyclerView.smoothScrollToPosition(0)

            val isAutoPlayToggleEnabled =
                defaultPrefs().getBoolean(MatchQuestionActivity.AUTOPLAY_STATE, true)

            if (autoPlay && isAutoPlayToggleEnabled) {
                rVExoPlayerHelper?.attachToRecyclerView(matchResultRecyclerView)
                rVExoPlayerHelper?.playCurrent(matchResultRecyclerView)
            }
        }
    }

    private fun preLoadVideos() {
        val cacheSize = ExoPlayerCacheManager.DEFAULT_CACHE_SIZE
        exoPlayerCacheManager.stopCaching()
        val urlList = matchQuestionData.asSequence()
            .filter {
                it is MatchedQuestionsList
                        && it.resourceType == "video"
                        && it.videoResource?.mediaType == MEDIA_TYPE_BLOB
            }
            .map {
                it as MatchedQuestionsList
                Pair(it.videoResource?.resource, it.videoResource?.mediaType)
            }
            .chunked(3)
            .toList().firstOrNull()

        urlList?.forEach {
            if (!it.first.isNullOrBlank() && !it.second.isNullOrBlank()) {
                exoPlayerCacheManager.cacheVideo(
                    it.first!!,
                    cacheSize, ExoPlayerHelper.getMediaSourceType(it.second!!)
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        rVExoPlayerHelper?.rvPlayerHelper?.setAudioAttributes(viewModel.getAudioAttributes(), true)
        rVExoPlayerHelper?.rvPlayerHelper?.resumePlayer()
    }

    override fun onDestroy() {
        scrollAnimationHandler.removeCallbacks(scrollAnimationRunnable)
        super.onDestroy()
    }

    override fun onUpdate(
        topicPosition: Int,
        isTopicSelected: Boolean,
        toRefresh: Boolean,
        facetPosition: Int
    ) {
        rVExoPlayerHelper?.canPlay = false
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): MatchResultFragmentBinding =
        MatchResultFragmentBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): MatchQuestionFragmentViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun provideActivityViewModel() {
        // This fragment is created by an activity and a fragment. Get the ViewModelStoreOwner accordingly
        matchQuestionViewModel =
            ViewModelProvider(
                owner = immediateParentViewModelStoreOwner,
                factory = viewModelFactory
            )[MatchQuestionViewModel::class.java]
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        viewModel.askedQuestionId = askedQuestionId

        if (autoPlay) {
            viewModel.sendEvent(
                event = EventConstants.MATCH_PAGE_AUTO_PLAY_ENABLED,
                params = hashMapOf()
            )
        }
    }
}