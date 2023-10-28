package com.doubtnutapp.liveclass.ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.*
import android.content.*
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Rational
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.EventConstants.VIDEO_FAB_CLICK
import com.doubtnut.core.StickyHeadersLinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.sharing.entities.ShareOnWhatApp
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.NetworkUtils
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.*
import com.doubtnutapp.addtoplaylist.AddToPlaylistFragment
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.bottomsheetholder.BottomSheetHolderActivity
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.databinding.ActivityLiveClassBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.dnr.model.DnrReward
import com.doubtnutapp.dnr.model.DnrRewardType
import com.doubtnutapp.dnr.model.DnrVideoWatchReward
import com.doubtnutapp.dnr.ui.fragment.DnrRewardBottomSheetFragment
import com.doubtnutapp.dnr.ui.fragment.DnrRewardDialogFragment
import com.doubtnutapp.dnr.viewmodel.DnrRewardViewModel
import com.doubtnutapp.domain.base.SolutionResourceType
import com.doubtnutapp.downloadedVideos.ExoDownloadTracker
import com.doubtnutapp.liveclass.adapter.LiveClassPollsList
import com.doubtnutapp.liveclass.adapter.LiveClassQuestionDataList
import com.doubtnutapp.liveclass.adapter.VideoPagerAdapter
import com.doubtnutapp.liveclass.adapter.VideoTagsAdapter
import com.doubtnutapp.liveclass.ui.dialog.NextVideoDialogFragment
import com.doubtnutapp.liveclass.viewmodel.LiveClassViewModel
import com.doubtnutapp.sharing.VIDEO_CHANNEL
import com.doubtnutapp.sharing.WhatsAppSharing
import com.doubtnutapp.socket.*
import com.doubtnutapp.studygroup.ui.fragment.SgShareActivity
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewActivity
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.forum.comments.CommentBottomSheetFragment
import com.doubtnutapp.ui.mediahelper.PLAYER_TYPE_YOUTUBE
import com.doubtnutapp.ui.pdfviewer.PdfViewerFragment
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.video.OpenWebViewOnVideoFail
import com.doubtnutapp.video.PlayerTypeOrMediaTypeChangedListener
import com.doubtnutapp.video.VideoFragmentListener
import com.doubtnutapp.video.VideoPlayerManager
import com.doubtnutapp.videoPage.model.PremiumVideoBlockedData
import com.doubtnutapp.videoPage.model.QuestionToShare
import com.doubtnutapp.videoPage.model.ViewAnswerData
import com.doubtnutapp.videoPage.ui.ShareOptionsBottomSheetFragment
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.github.nisrulz.sensey.OrientationDetector
import com.github.nisrulz.sensey.Sensey
import com.google.android.exoplayer2.Player
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.uxcam.UXCam
import dagger.Lazy
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import io.branch.referral.Defines
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import kotlinx.android.synthetic.main.dialog_video_ended.*
import kotlinx.android.synthetic.main.widget_video_action.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 27/04/20.
 */
class LiveClassActivity : BaseBindingActivity<LiveClassViewModel, ActivityLiveClassBinding>(),
    OrientationDetector.OrientationListener,
    HasAndroidInjector, VideoFragmentListener, LiveClassQnaListener, ActionPerformer,
    SupportsPictureInPictureMode {

    companion object {
        //region PIP controls constants

        /** Intent action for media controls from Picture-in-Picture mode.  */
        private const val ACTION_MEDIA_CONTROL = "media_control"

        private const val EXTRA_CONTROL_TYPE = "control_type"

        private const val REQUEST_PLAY = 1
        private const val REQUEST_PAUSE = 2

        private const val CONTROL_TYPE_PLAY = 1
        private const val CONTROL_TYPE_PAUSE = 2
        //endregion

        const val TAG = "LiveClassActivity"
        const val TAG_FREE = "Free" + TAG
        const val TAG_PAID = "Paid" + TAG
        const val TAG_PIP = TAG + "_PIP"

        const val PAGE = "page"
        const val FIREBASE = "firebase"
        const val DN_SOCKET = "dn_socket"
        const val VIEW_VIDEO_DATA = "view_video_data"

        private var videoActionLayout: String = ""

        private const val DEFAULT_LF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD = 300000L

        private const val ROTATION_DELAY = 500L

        private const val OPEN_ANSWERED_DOUBT_COMMENT_ID = "open_answered_doubt_comment_id"

        private const val INTENT_EXTRA_START_POSITION = "start_position"

        fun getStartIntent(
            activity: VideoPageActivity,
            page: String,
            viewAnswerData: ViewAnswerData,
            openAnsweredDoubtWithCommentId: String? = null,
            startPositionInSeconds: Long = 0
        ): Intent =
            Intent(activity, LiveClassActivity::class.java).apply {
                putExtra(PAGE, page)
                putExtra(VIEW_VIDEO_DATA, viewAnswerData)
                putExtra(OPEN_ANSWERED_DOUBT_COMMENT_ID, openAnsweredDoubtWithCommentId)
                putExtra(INTENT_EXTRA_START_POSITION, startPositionInSeconds)
            }
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var disposable: CompositeDisposable

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var whatsAppSharing: WhatsAppSharing

    @Inject
    lateinit var defaultDataStore: Lazy<DefaultDataStore>

    private val startPositionInSeconds: Long
        get() = intent.getLongExtra(INTENT_EXTRA_START_POSITION, 0)

    private var isTopDoubtLandscapeExpanded = true

    private var sentCount = 0
    private var userSelectedState: Int = 90

    private var isQuizListFetched = false
    private var isLiveVideoEnded = false
    private var isYoutubePlaying = false
    private var isVOD = false
    private var isCommentEditTextFocused = false
    private var isStarted = false
    private var isPremium = false
    private var isFullScreen = false
    private var isFeedbackRequired = false
    private var isTopDoubtVisibleEventSent = false

    private var viewId: String = ""
    private var detailId = ""
    private var qid = ""
    private var page = ""
    private var currentPlayerType = "UNKNOWN"
    private var currentMediaType = "UNKNOWN"

    private var id: Int? = null
    private var enterTime: Long? = null
    private var viewAnswerData: ViewAnswerData? = null
    private var videoPlayerManager: VideoPlayerManager? = null
    private var liveQuizData: LiveQuizData? = null
    private var liveClassQnaFragment: LiveClassQnaFragment? = null
    private var announcementFragment: LiveClassCommFragment? = null
    private var liveClassAnnouncementData: LiveClassAnnouncementData? = null
    private var tagsData: TagsData? = null
    private var homeworkData: HomeWorkData? = null
    private var liveClassFeedbackFragment: LiveClassFeedbackFragment? = null
    private var dbReference: DatabaseReference? = null

    private val handler: Handler = Handler()
    private var quizItemMap = HashMap<Long, LiveClassPopUpItem>()
    private var quizItemRangeMap = HashMap<Long, Long>()
    private var topDoubtMap = HashMap<Long, TopDoubtQuestion>()
    private var onGoingTopDoubtKey = -1L
    private var realTimeQuizResourceId = HashSet<Long>()
    private lateinit var adapter: WidgetLayoutAdapter

    private var liveClassCommentFragment: LiveClassCommentFragment? = null

    private var mPipModeLastEnterTimeMillis: Long = -1
    private var positionInSec: Long = 0L

    private var rxBusObserver: Disposable? = null

    private var openAnsweredDoubtWithCommentId: String? = null

    var isBranchLink = false
    private var premiumVideoBlockedData: PremiumVideoBlockedData? = null
    private var premiumVideoOffset: Long? = null
    private var videoStartTime: Long = 0L
    private var isNextButtonShown = false
    private var nextVideoDialog: NextVideoDialogFragment? = null

    // DNR region start
    private var showDnrRewardPopupForLf: Boolean? = true
    private lateinit var dnrRewardViewModel: DnrRewardViewModel
    // DNR region end

    private var chapter = ""

    private val mPictureInPictureParamsBuilder: PictureInPictureParams.Builder? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams.Builder()
        } else {
            null
        }
    }

    private val labelPlay: String by lazy { getString(R.string.exo_controls_play_description) }
    private val labelPause: String by lazy { getString(R.string.exo_controls_pause_description) }

    /** A [BroadcastReceiver] to receive action item events from Picture-in-Picture mode.  */
    private val mPipActionBroadcastReceiver: BroadcastReceiver by lazy {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                if (intent == null || intent.action != ACTION_MEDIA_CONTROL) return

                when (intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)) {
                    CONTROL_TYPE_PLAY -> {
                        if (videoPlayerManager?.getExoPlayerPlaybackState() == Player.STATE_ENDED) {
                            videoPlayerManager?.startExoPlayerVideoFromBeginning()
                        } else {
                            videoPlayerManager?.resumeExoPlayer()
                        }
                        viewModel.sendEvent(EventConstants.PIP_MODE_PLAY, ignoreSnowplow = true)
                    }
                    CONTROL_TYPE_PAUSE -> {
                        videoPlayerManager?.pauseExoPlayer()
                        viewModel.sendEvent(EventConstants.PIP_MODE_PAUSE, ignoreSnowplow = true)
                    }
                }
            }
        }
    }

    private val mPipModeExitListener: PipModeExitListener by lazy {
        object : PipModeExitListener() {
            override fun onPipExpanded() {
                viewModel.sendEvent(EventConstants.PIP_MODE_EXPANDED, ignoreSnowplow = true)
            }

            override fun onPipClosed() {
                viewModel.sendEvent(EventConstants.PIP_MODE_CLOSED, ignoreSnowplow = true)
            }
        }.also {
            lifecycle.addObserver(it)
        }
    }

    private var mCommentBottomSheetFragment: CommentBottomSheetFragment? = null

    private fun showBuyNowButton() {
        if (viewAnswerData?.premiumVideoOffset != null) {
            premiumVideoBlockedData = viewAnswerData?.premiumVideoBlockedData
            premiumVideoOffset = viewAnswerData?.premiumVideoOffset?.toLong()
            binding.btnView.show()
            binding.btnBuyNowVideoPage.text =
                premiumVideoBlockedData?.coursePurchaseButtonText.orEmpty()
            binding.btnBuyNowVideoPage.background = Utils.getShape(
                premiumVideoBlockedData?.coursePurchaseButtonBgColor ?: "#eb532c",
                premiumVideoBlockedData?.coursePurchaseButtonBgColor ?: "#eb532c",
                5f
            )
            binding.btnBuyNowVideoPage.setTextColor(
                Utils.parseColor(
                    premiumVideoBlockedData?.coursePurchaseButtonTextColor
                        ?: "#ffffff"
                )
            )
            binding.btnBuyNowVideoPage.setOnClickListener {
                deeplinkAction.performAction(
                    this,
                    premiumVideoBlockedData?.coursePurchaseButtonDeeplink.orEmpty()
                )
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PAID_CONTENT_SEARCH_EVENTS,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.VIDEO_VIEW_ID, viewAnswerData?.viewId.orEmpty())
                            put(
                                EventConstants.COURSE_ID,
                                viewAnswerData?.premiumVideoBlockedData?.courseId
                                    ?: 0
                            )
                            put(
                                EventConstants.PAID_USER,
                                viewAnswerData?.isPremium == true && viewAnswerData?.isVip == true
                            )
                            put(EventConstants.CTA_VIEWED, true)
                            put(EventConstants.CTA_CLICKED, 2)
                            put(EventConstants.VIEW_FROM, page)
                        })
                )
            }
        } else {
            binding.btnView.hide()
        }
    }

    private fun openCommentBottomSheet() {
        val entityId = viewAnswerData?.videoEntityId
        val entityType = viewAnswerData?.videoEntityType
        val batchId = viewAnswerData?.batchId
        if (!entityId.isNullOrBlank() && !entityType.isNullOrBlank()) {
            startActivity(
                BottomSheetHolderActivity.getCommentsStartIntent(
                    this,
                    entityType,
                    entityId,
                    detailId,
                    tagsData?.commentTags,
                    tagsData?.pinnedPost.orEmpty(), positionInSec, batchId,
                    (videoPlayerManager?.isRtmpPlaying == true || videoPlayerManager?.isTimeShiftVideoPlaying == true),
                    openAnsweredDoubtWithCommentId,
                    assortmentId = homeworkData?.assortmentId,
                    chapter = chapter,
                    qid = qid,
                    isVip = viewAnswerData?.isVip ?: true,
                    isPremium = viewAnswerData?.isPremium ?: true,
                    isRtmp = viewAnswerData?.isRtmp ?: false
                )
            )
        }
        openAnsweredDoubtWithCommentId = null
    }

    private fun shareVideoOnWhatsapp() {
        val sharingMessage = if (viewAnswerData?.shareMessage.isNullOrBlank()) {
            getString(R.string.video_share_message)
        } else {
            viewAnswerData?.shareMessage.orEmpty()
        }
        val questionId = viewAnswerData?.questionId ?: return
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.LIVE_CLASS_VIDEO_SHARE,
                hashMapOf(
                    EventConstants.EVENT_NAME_ID to questionId
                ), ignoreSnowplow = false
            )
        )
        whatsAppSharing.shareOnWhatsApp(
            ShareOnWhatApp(
                VIDEO_CHANNEL,
                featureType = "video",
                imageUrl = viewAnswerData?.thumbnailImage.ifEmptyThenNull(),
                controlParams = hashMapOf(
                    Constants.PAGE to page,
                    Constants.Q_ID to questionId,
                    Constants.PLAYLIST_ID to "",
                    Constants.SOLUTION_RESOURCE_TYPE to SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO,
                    "override_page" to "1"
                ),
                bgColor = "#000000",
                sharingMessage = sharingMessage,
                questionId = questionId
            )
        )
        whatsAppSharing.startShare(this)
    }

    private fun setClickListeners() {

        binding.textViewTopDoubtLandscape.setOnClickListener {
            if (binding.topDoubtContainer.isVisible) {
                binding.topDoubtContainer.hide()
                binding.topDoubtFrameLayout.hide()
                videoPlayerManager?.resumeExoPlayer()
            } else {
                if (onGoingTopDoubtKey != -1L && topDoubtMap.containsKey(onGoingTopDoubtKey)) {
                    val topDoubt = topDoubtMap[onGoingTopDoubtKey]
                    showTopDoubtFragment(
                        true,
                        topDoubt?.id.orEmpty(),
                        topDoubt?.text.orEmpty()
                    )
                    binding.topDoubtContainer.show()
                    binding.topDoubtFrameLayout.show()
                    sendTopDoubtToggleEvent(EventConstants.EXPAND)
                }
            }
        }

        binding.textViewTopDoubt.setOnClickListener {
            if (binding.topDoubtContainerPortrait.isVisible) {
                slideUp()
                videoPlayerManager?.resumeExoPlayer()
                sendTopDoubtToggleEvent(EventConstants.COLLAPSE)
            } else {
                slideDown()
                sendTopDoubtToggleEvent(EventConstants.EXPAND)
            }
        }

        binding.layoutAnnouncement.ivClose.setOnClickListener {
            sendPollCloseEvent()
            binding.layoutAnnouncement.root.hide()
        }

        binding.ivTopDoubtLandscapeToggle.setOnClickListener {
            if (isTopDoubtLandscapeExpanded) {
                isTopDoubtLandscapeExpanded = false
                binding.textViewTopDoubtLandscape.hide()
                binding.ivTopDoubtLandscapeToggle.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_keyboard_arrow_left_black_24dp
                    )
                )
            } else {
                isTopDoubtLandscapeExpanded = true
                binding.textViewTopDoubtLandscape.show()
                binding.ivTopDoubtLandscapeToggle.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_cross
                    )
                )
            }

            binding.ivTopDoubtLandscapeToggle.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.white
                )
            )
        }

        binding.btnAddPlaylist.setOnClickListener {
            onWatchLaterClicked()
        }

        binding.tvSaveVideo.setOnClickListener {
            onWatchLaterClicked()
        }

        binding.btnShare.setOnClickListener {
            shareVideo()
        }

        binding.tvShareCount.setOnClickListener {
            shareVideo()
        }

        binding.btnComment.setOnClickListener {
            openCommentBottomSheet()
        }

        binding.tvCommentCount.setOnClickListener {
            openCommentBottomSheet()
        }

        binding.fab.setOnDebouncedClickListener(1000) {
            val fabDeeplink = homeworkData?.fabDeeplink
            if (!fabDeeplink.isNullOrBlank()) {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        VIDEO_FAB_CLICK,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.SOURCE, "free_class_video_page")
                        }
                    )
                )
                deeplinkAction.performAction(this, fabDeeplink)
            }
        }

        binding.btnLike.setOnClickListener {
            onLikeClicked()
        }

        binding.tvLikeCount.setOnClickListener {
            onLikeClicked()
        }

        binding.btnDislike.setOnClickListener {
            onDislikeClicked()
        }
        binding.tvDisLikeCount.setOnClickListener {
            onDislikeClicked()
        }

    }

    private fun setUpObserver() {
        viewModel.socketMessage.observe(this, EventObserver {
            if (it is SocketErrorEventType) {
                onSocketError(it)
            } else {
                onSocketMessage(it)
            }
        })

        viewModel.quizDetailLiveData.observeK(
            this,
            ::onQuizDetailSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.feedbackLiveData.observeK(
            this,
            ::onFeedbackDataSuccess,
            ::onFeedbackError,
            ::unAuthorizeUserError,
            ::feedbackIoExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.feedbackStatusLiveData.observeK(
            this,
            ::feedbackRequiredSuccess,
            ::onFeedbackError,
            ::unAuthorizeUserError,
            ::feedbackIoExceptionHandler,
            ::updateProgressBarState
        )


        viewModel.homeworkLiveData.observeK(
            this,
            ::onHomeworkDataSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.topDoubtQuestions.observeK(this,
            { topDoubtQuestionList ->
                topDoubtMap = hashMapOf()
                if (!topDoubtQuestionList.isNullOrEmpty()) {
                    topDoubtQuestionList.forEach { topDoubt ->
                        if (topDoubt.offset != null)
                            topDoubtMap[topDoubt.offset] = topDoubt
                    }
                }
            }, { }, { }, { }, { })

        viewModel.onAddToWatchLater.observe(this, EventObserver {
            onWatchLaterSubmit(it)
        })

        rxBusObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            when (event) {
                is PlayAudioEvent -> {
                    if (event.state) {
                        videoPlayerManager?.pauseExoPlayer()
                    }
                }
                is PauseVideoPlayer -> {
                    videoPlayerManager?.pauseExoPlayer()
                }
                is SeeDoubtsAction -> {
                    openCommentBottomSheet()
                }
            }
        }

        viewModel.bookmarkLiveData.observeK(
            this,
            ::onBookmarkDataSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        dnrRewardViewModel.dnrRewardLiveData.observeEvent(this, {
            showDnrReward(it)
        })

        viewModel.snackBarData.observeNonNull(this, { data ->
            val snackbar = Snackbar.make(
                findViewById(android.R.id.content), "",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.duration = 3000
            val customView = View.inflate(this, R.layout.item_custom_snackbar, null)

            snackbar.view.setBackgroundColor(Color.TRANSPARENT)
            val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
            snackbarLayout.removeAllViews()
            snackbarLayout.setPadding(0, 0, 0, 0)

            customView.rootView.applyBackgroundColor(data.bgColor)

            val imageView = customView.findViewById<ImageView>(R.id.iv_snackbar_icon)
            imageView.isVisible = data.iconUrl.isNotNullAndNotEmpty()
            imageView.loadImage(data.iconUrl.ifEmptyThenNull())

            val textView = customView.findViewById<TextView>(R.id.tv_snackbar_title)
            textView.text = data.title.orEmpty()
            textView.applyTextSize(data.titleSize)
            textView.applyTextColor(data.titleColor)

            val action = customView.findViewById<TextView>(R.id.tv_snackbar_action)
            action.isVisible = data.deeplinkText.isNotNullAndNotEmpty()
            action.text = data.deeplinkText.orEmpty()
            action.applyTextSize(data.deeplinkTextSize)
            action.applyTextColor(data.deeplinkTextColor)
            action.setOnClickListener {
                deeplinkAction.performAction(this, data.deeplink)
                snackbar.dismiss()
            }

            snackbarLayout.addView(customView, 0)
            snackbar.show()

            lifecycleScope.launchWhenResumed {
                if (data.nextInterval != null) {
                    delay(data.nextInterval.toLong())
                    viewModel.getSnackBar(qid = qid)
                }
            }
        })
    }

    private fun showDnrReward(dnrReward: DnrReward) {
        dnrRewardViewModel.checkRewardPopupToBeShown(dnrReward)
        dnrRewardViewModel.dnrRewardPopupLiveData.observeEvent(this, { rewardPopupType ->
            when (rewardPopupType) {
                DnrRewardViewModel.RewardPopupType.NO_POPUP -> {
                    return@observeEvent
                }
                DnrRewardViewModel.RewardPopupType.REWARD_BOTTOM_SHEET -> {
                    if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT && isInPipMode.not()) {
                        DnrRewardBottomSheetFragment.newInstance(dnrReward)
                            .show(supportFragmentManager, DnrRewardBottomSheetFragment.TAG)
                    } else {
                        DnrRewardDialogFragment.newInstance(dnrReward)
                            .show(supportFragmentManager, DnrRewardDialogFragment.TAG)
                    }
                }
                DnrRewardViewModel.RewardPopupType.REWARD_DIALOG -> {
                    DnrRewardDialogFragment.newInstance(dnrReward)
                        .show(supportFragmentManager, DnrRewardDialogFragment.TAG)
                }
            }
        })
    }

    private fun openWebView() {
        if (viewAnswerData?.useFallbackWebview == true) {
            val url = "https://doubtnut.com/app-live-class-videos/" +
                    defaultPrefs().getString(Constants.XAUTH_HEADER_TOKEN, "") + "/" +
                    userPreference.getUserStudentId() + "/" +
                    viewAnswerData?.questionId
            startActivity(WebViewActivity.getIntent(this, url, ""))
        }
    }

    private fun onWatchLaterSubmit(id: String) {
        showSnackbar(
            R.string.video_saved_to_watch_later,
            R.string.change,
            Snackbar.LENGTH_LONG,
            id
        ) { idToPost ->
            viewModel.removeFromPlaylist(idToPost, "1")
            AddToPlaylistFragment.newInstance(idToPost)
                .show(supportFragmentManager, AddToPlaylistFragment.TAG)
        }
    }

    private fun handleSecureFlag(blockScreenshot: Boolean?) {
        if (blockScreenshot == true) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun slideUp() {
        val animSlideDown = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_up)
        binding.topDoubtContainerPortrait.startAnimation(animSlideDown)
        binding.topDoubtContainerPortrait.hide()
        binding.topDoubtFrameLayout.hide()
    }

    private fun slideDown() {
        if (onGoingTopDoubtKey != -1L && topDoubtMap.containsKey(onGoingTopDoubtKey)) {
            val topDoubt = topDoubtMap[onGoingTopDoubtKey]
            showTopDoubtFragment(
                false,
                topDoubt?.id.orEmpty(),
                topDoubt?.text.orEmpty()
            )
            val animSlideDown = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down)
            binding.topDoubtContainerPortrait.startAnimation(animSlideDown)
            binding.topDoubtContainerPortrait.show()
            binding.topDoubtFrameLayout.show()
        }
    }

    private fun showTopDoubtFragment(isLandscape: Boolean, id: String, question: String) {
        videoPlayerManager?.pauseExoPlayer()
        val fragment = TopDoubtsFragment.newInstance(
            id,
            question,
            viewAnswerData?.videoEntityId.orEmpty(),
            viewAnswerData?.videoEntityType.orEmpty(),
            viewAnswerData?.batchId
        )
        if (isLandscape) {
            supportFragmentManager.beginTransaction().replace(R.id.topDoubtContainer, fragment)
                .commit()
        } else {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topDoubtContainerPortrait, fragment).commit()
        }
    }

    private fun onStartData() {
        val viewAnswerData = this.viewAnswerData!!
        handleSecureFlag(viewAnswerData.blockScreenshot)
        binding.layoutCommentShare.show()
        if (viewAnswerData.isLiked) {
            binding.btnLike.isSelected = true
        }
        if (viewAnswerData.isDisliked) {
            binding.btnDislike.isSelected = true
        }
        if (viewAnswerData.state == "0") {
            isStarted = false
            supportFragmentManager.beginTransaction()
                .replace(R.id.videoContainer, VideoStatusFragment.newInstance(qid)).commit()
            return
        }
        isStarted = true
        videoStartTime = System.currentTimeMillis()
        videoPlayerManager?.setAndInitPlayFromResource(
            id = viewAnswerData.questionId,
            videoResourceList = viewAnswerData.resources,
            viewId = viewAnswerData.viewId,
            startPositionInSeconds = startPositionInSeconds,
            isPlayedFromTheBackStack = false,
            page = page,
            aspectRatio = viewAnswerData.aspectRatio,
            eventDetail = viewAnswerData.eventMap,
            startTime = viewAnswerData.startTime,
            showFullScreen = true,
            controllerAutoShow = isInPipMode.not(),
            showEnterPipModeButton = canEnterPipMode(checkPlayer = false),
            blockForwarding = viewAnswerData.blockForwarding,
            answerId = viewAnswerData.answerId,
            expertId = viewAnswerData.expertId,
            videoName = viewAnswerData.videoName,
            isPremium = viewAnswerData.isPremium,
            isVip = viewAnswerData.isVip,
            subject = viewAnswerData.logData?.subject,
            chapter = viewAnswerData.logData?.chapter,
            bottomView = viewAnswerData.bottomView,
            videoLanguage = viewAnswerData.logData?.videoLanguage,
            ocrText = viewAnswerData.ocrText,
            adResource = viewAnswerData.adResource,
            isInPipMode = isInPipMode,
            analysisData = viewAnswerData.analysisData,
            imaAdTagResource = viewAnswerData.imaAdTagResourceData
        )
        /*    if (!isFullScreen) {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    isFullScreen = true
                    userSelectedState = 180
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    updateFullScreenUI()
                }, ROTATION_DELAY)
            }*/
        if (viewAnswerData.connectSocket == true) {
            viewModel.connectSocket()
        }
        if (viewAnswerData.connectFirebase == true) {
            observeForQuestionAndOptions(viewAnswerData.firebasePath)
        }
        if (viewAnswerData.showReplayQuiz == true) {
            viewModel.getLiveClassQuizDetail(qid)
        }
        viewModel.getFeedbackData(detailId)
        if (detailId.isNotEmpty()) {
            viewModel.isFeedbackRequired(detailId)
        }

        viewModel.fetchTopDoubtQuestions(
            viewAnswerData.videoEntityType,
            viewAnswerData.videoEntityId,
            "1",
            viewAnswerData.batchId
        )
        sendCommonEvent()
        sendMoEngageVideoWatchEvent()
        sendBranchVideoWatchEvent()

        if (FeaturesManager.isFeatureEnabled(this, Features.FORCE_VIDEO_WEB_VIEW)) {
            openWebView()
        }
    }

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(this, this)
        binding.rvWidgets.layoutManager =
            StickyHeadersLinearLayoutManager<WidgetLayoutAdapter>(this)
        binding.rvWidgets.adapter = adapter
    }

    private fun onQuizDetailSuccess(apiQuizDetail: List<LiveClassPopUpItem>) {
        if (isQuizListFetched) return
        isQuizListFetched = true
        quizItemMap = hashMapOf()
        quizItemRangeMap = hashMapOf()
        apiQuizDetail.forEach {
            if (it.liveAt != null && it.liveAt != -1L) {
                val liveAt = it.liveAt!!
                quizItemMap[liveAt] = it
                if (it is LiveQuizData) {
                    var rangeUpperLimit = liveAt
                    it.list.forEach { qna ->
                        rangeUpperLimit += qna.expiry.toLongOrNull() ?: 0
                    }
                    (rangeUpperLimit downTo liveAt).forEach { i ->
                        quizItemRangeMap[i] = liveAt
                    }
                }
            }
        }
    }

    private fun onFeedbackDataSuccess(tagsData: TagsData) {
        this.tagsData = tagsData
    }

    private fun onHomeworkDataSuccess(homeworkData: HomeWorkData) {
        this.homeworkData = homeworkData
        videoActionLayout = homeworkData.videoActionLayout.orEmpty()
        if (homeworkData.videoActionLayout == "2") {
            binding.layoutCommentShareOne.isVisible = false
            binding.layoutCommentShareTwo.isVisible = true
            binding.tvLikeCount.text = viewAnswerData?.likeCount.toString()
        } else {
            binding.layoutCommentShareOne.isVisible = true
            binding.layoutCommentShareTwo.isVisible = false
        }
        if (homeworkData.fabDeeplink.isNullOrBlank()) {
            binding.fab.hide()
        } else {
            binding.fab.show()
        }
        adapter = WidgetLayoutAdapter(this, this)
        adapter.setWidgets(homeworkData.widgets)
        binding.rvWidgets.adapter = adapter
        binding.tvVideoTitle.text = homeworkData.title.orEmpty()
        binding.tvVideoTitle2.text = viewAnswerData?.title.orEmpty()
        binding.tvVideoTitle2.text = viewAnswerData?.title.orEmpty()
        binding.tvfacultyName.text = homeworkData.facultyName.orEmpty()
        binding.tvBottom.isVisible = !homeworkData.viewsLabel.isNullOrBlank()
        binding.tvBottom.text = homeworkData.viewsLabel.orEmpty()
        if (!homeworkData.nextVideoTitle.isNullOrEmpty() && !isInPipMode) {
            binding.tvNextVideo.text = homeworkData.nextVideoTitle.orEmpty()
            binding.nextVideoContainer.visibility = View.VISIBLE
        } else {
            binding.nextVideoContainer.visibility = View.GONE
        }

        binding.tvSubject.text = homeworkData.subject.orEmpty()
        binding.tvSubject2.text = homeworkData.subject.orEmpty()

        binding.tvSubject.background = Utils.getShape(
            homeworkData.subjectColor ?: "#ffffff",
            homeworkData.subjectColor ?: "#ffffff",
            4f
        )
        binding.tvSubject2.background = Utils.getShape(
            homeworkData.subjectColor ?: "#ffffff",
            homeworkData.subjectColor ?: "#ffffff",
            4f
        )

        binding.playIcon.setOnDebouncedClickListener(1000) {
            onPlaylistClicked()
        }
        binding.tvNextVideo.setOnDebouncedClickListener(1000) {
            onPlaylistClicked()
        }
        binding.ivArrow.setOnDebouncedClickListener(1000) {
            val fragment = NextVideoDialogFragment.newInstance(
                viewAnswerData?.questionId.orEmpty(),
                homeworkData.nextVideoTitle.orEmpty()
            )
            fragment.show(supportFragmentManager, "")
        }
        binding.ivNextButton.setOnClickListener {
            finish()
            deeplinkAction.performAction(this, homeworkData.nextVideoDeeplink.orEmpty())
        }
        binding.commentBtn.loadImageEtx(homeworkData.buttons?.getOrNull(0)?.iconUrl.orEmpty())
        binding.shareBtn.loadImageEtx(homeworkData.buttons?.getOrNull(3)?.iconUrl.orEmpty())
        binding.bookmarkBtn.loadImageEtx(homeworkData.buttons?.getOrNull(1)?.iconUrl.orEmpty())
        binding.downloadBtn.loadImageEtx(homeworkData.buttons?.getOrNull(2)?.iconUrl.orEmpty())
        binding.commentBtn.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.VIDEO_PAGE_ICON_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.ID to homeworkData.buttons?.getOrNull(0)?.id.orEmpty(),
                    )
                )
            )
            if (homeworkData.buttons?.getOrNull(0)?.id == "comment") {
                openCommentBottomSheet()
            } else {
                deeplinkAction.performAction(this, homeworkData.buttons?.getOrNull(0)?.deeplink)
            }
        }

        binding.shareBtn.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.VIDEO_PAGE_ICON_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.ID to homeworkData.buttons?.getOrNull(3)?.id.orEmpty(),
                    )
                )
            )
            if (homeworkData.buttons?.getOrNull(3)?.id == "share") {
                shareVideo()
            } else {
                deeplinkAction.performAction(this, homeworkData.buttons?.getOrNull(3)?.deeplink)
            }
        }

        binding.bookmarkBtn.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.VIDEO_PAGE_ICON_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.ID to homeworkData.buttons?.getOrNull(1)?.id.orEmpty(),
                    )
                )
            )
            when (homeworkData.buttons?.getOrNull(1)?.id) {
                "bookmark" -> {
                    viewModel.bookmark(homeworkData.courseResourceId, homeworkData.assortmentId)
                }
                "share" -> {
                    shareVideo()
                }
                else -> {
                    deeplinkAction.performAction(this, homeworkData.buttons?.getOrNull(1)?.deeplink)
                }
            }
        }

        binding.downloadBtn.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.VIDEO_PAGE_ICON_CLICK,
                    hashMapOf<String, Any>(
                        EventConstants.ID to homeworkData.buttons?.getOrNull(2)?.id.orEmpty(),
                    )
                )
            )
            when (homeworkData.buttons?.getOrNull(2)?.id) {
                "download" -> {
                    downloadVideo(
                        viewAnswerData?.questionId.orEmpty(),
                        viewAnswerData?.title.orEmpty()
                    )
                }
                "comment" -> {
                    openCommentBottomSheet()
                }
                else -> {
                    deeplinkAction.performAction(this, homeworkData.buttons?.getOrNull(2)?.deeplink)
                }
            }
        }
        if (!homeworkData.tabList.isNullOrEmpty()) {
            binding.rvWidgets.hide()
            binding.tabLayout.show()
            binding.viewPager.show()
            binding.viewPager.adapter =
                VideoPagerAdapter(
                    supportFragmentManager,
                    homeworkData.tabList,
                    qid
                )
            binding.tabLayout.addOnTabSelectedListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.VIDEO_TAB_CLICKED,
                        hashMapOf(
                            EventConstants.ASSORTMENT_ID to homeworkData.assortmentId.orEmpty(),
                            EventConstants.TAB_NAME to homeworkData.tabList[it.position].id.orEmpty()
                        )
                    )
                )
            }
            binding.tabLayout.setupWithViewPager(binding.viewPager)
        } else {
            binding.rvWidgets.show()
            binding.tabLayout.hide()
            binding.viewPager.hide()
        }
    }

    private fun shareVideo() {
        val shareOptionBottomSheet = ShareOptionsBottomSheetFragment.newInstance()
        shareOptionBottomSheet.setShareOptionClickListener(object :
            ShareOptionsBottomSheetFragment.ShareOptionClickListener {
            override fun onWhatsappShareClick() {
                shareOptionBottomSheet.dismiss()
                shareVideoOnWhatsapp()
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.SHARE_OPTIONS_WHATSAPP_CLICK,
                        ignoreSnowplow = true
                    )
                )
            }

            override fun onStudyGroupShareClick() {
                shareOptionBottomSheet.dismiss()
                shareVideoOnStudyGroup(viewAnswerData?.ocrText ?: "")
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.SHARE_OPTIONS_SG_CLICK,
                        ignoreSnowplow = true
                    )
                )
            }

            override fun onDismiss() {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.SHARE_OPTIONS_DISMISS,
                        ignoreSnowplow = true
                    )
                )
            }
        })
        shareOptionBottomSheet.show(
            supportFragmentManager,
            ShareOptionsBottomSheetFragment.TAG
        )
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.SHARE_OPTIONS_BOTTOM_SHEET_SHOWN,
                ignoreSnowplow = true
            )
        )
    }

    private fun feedbackRequiredSuccess(response: FeedbackStatusResponse) {
        isFeedbackRequired = response.isFeedbackRequired ?: false
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun onFeedbackError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun feedbackIoExceptionHandler() {

    }

    override fun onBottomSideUp() {

    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onLeftSideUp() {
        if (!isDeviceOrientationOn()) return
        if (userSelectedState == -1) {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                updateFullScreenUI()
            }, ROTATION_DELAY)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onRightSideUp() {
        if (!isDeviceOrientationOn()) return
        if (userSelectedState == 180) {
            userSelectedState = -1
        } else if (userSelectedState == -1) {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                updateFullScreenUI()
            }, ROTATION_DELAY)
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onTopSideUp() {
        if (!isDeviceOrientationOn()) return
        if (userSelectedState == 90) {
            userSelectedState = -1
        } else if (userSelectedState == -1) {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                updatePortraitScreenUI()
            }, ROTATION_DELAY)
        }
    }

    private fun updateFullScreenUI() {
        videoPlayerManager?.videoFragment?.enterFullscreen()
        if (!tagsData?.commentTags.isNullOrEmpty()) {
            videoPlayerManager?.videoFragment?.adjustBottomMargin(
                ViewUtils.dpToPx(60f, this).toInt()
            )
        }
        ObjectAnimator.ofFloat(
            binding.textViewViewerCount,
            "translationY",
            -70f
        ).apply {
            duration = 1000
            start()
        }
        binding.layoutAnnouncement.root.hide()
        binding.layoutCommentShare.hide()
        binding.fab.hide()
        binding.textViewTopDoubt.hide()
        binding.textViewTopDoubtLandscape.hide()
        binding.ivTopDoubtLandscapeToggle.hide()
        binding.nextVideoContainer.hide()
        binding.rvVideoTags.setMargins(0, 0, 0, ViewUtils.dpToPx(110f, this).toInt())
        if (!isCommentEditTextFocused) {
            if (supportFragmentManager.findFragmentByTag(LiveClassCommentFragment.TAG) == null) {
                addLandscapeComments()
            }
            binding.commentContainer.show()
            binding.textViewViewerCount.hide()
            videoPlayerManager?.videoFragment?.adjustBottomMargin(
                ViewUtils.dpToPx(
                    60f,
                    this
                ).toInt()
            )
        } else {
            if (binding.textViewViewerCount.tvComment != null) {
                isCommentEditTextFocused = false
                binding.commentContainer.hide()
                if (!binding.textViewViewerCount.text.isNullOrBlank())
                    binding.textViewViewerCount.show()
                hideKeyboard(binding.textViewViewerCount.tvComment)
            }
        }
    }

    private fun updatePortraitScreenUI() {
        videoPlayerManager?.videoFragment?.exitFullscreen()
        ObjectAnimator.ofFloat(
            binding.textViewViewerCount,
            "translationY",
            0f
        ).apply {
            duration = 1000
            start()
        }
        binding.layoutAnnouncement.root.hide()
        binding.layoutCommentShare.show()
        if (homeworkData?.fabDeeplink.isNullOrBlank()) {
            binding.fab.hide()
        } else {
            binding.fab.show()
        }
        if (binding.layoutCommentShare.tvComment != null) {
            hideKeyboard(binding.layoutCommentShare.tvComment)
        }
        binding.commentContainer.hide()
        if (binding.textViewViewerCount.text.isNullOrBlank()) {
            binding.textViewViewerCount.hide()
        } else {
            binding.textViewViewerCount.show()
        }
        if (::adapter.isInitialized) {
            adapter.notifyDataSetChanged()
        }
        binding.textViewTopDoubt.hide()
        binding.textViewTopDoubtLandscape.hide()
        binding.ivTopDoubtLandscapeToggle.hide()
        if (!isInPipMode && homeworkData?.nextVideoTitle.isNotNullAndNotEmpty())
            binding.nextVideoContainer.show()
        binding.rvVideoTags.setMargins(0, 0, 0, ViewUtils.dpToPx(40f, this).toInt())
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            updatePortraitScreenUI()
        } else {
            mCommentBottomSheetFragment?.dismiss()
            mCommentBottomSheetFragment = null
            updateFullScreenUI()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        videoPlayerManager?.videoFragment?.onWindowFocusChanged(hasFocus)
    }

    private fun addLandscapeComments() {
        supportFragmentManager.beginTransaction().apply {
            if (!tagsData?.commentTags.isNullOrEmpty()) {
                liveClassCommentFragment = LiveClassCommentFragment.newInstance(
                    viewAnswerData?.videoEntityType,
                    viewAnswerData?.videoEntityId,
                    detailId, tagsData?.commentTags, positionInSec,
                    (videoPlayerManager?.isRtmpPlaying == true || videoPlayerManager?.isTimeShiftVideoPlaying == true)
                )
                replace(
                    R.id.commentContainer,
                    liveClassCommentFragment!!,
                    LiveClassCommentFragment.TAG
                )
                commitAllowingStateLoss()
            }
        }
    }

    override fun onVideoStart() {
        updatePictureInPictureActions()
    }

    override fun onVideoPause() {
        updatePictureInPictureActions()
    }

    override fun onExoPlayerProgress(positionMs: Long) {
        if (showDnrRewardPopupForLf == false) return
        checkForDnrReward()
    }

    private fun checkForDnrReward() {
        lifecycleScope.launchWhenStarted {
            val engagementTimeToClaimDnrReward =
                defaultDataStore.get().lfEngagementTimeToClaimDnrReward.firstOrNull()
                    ?: DEFAULT_LF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD
            val currentEngagementTime = (videoPlayerManager?.currentEngagementTime ?: 0) * 1000L
            if (engagementTimeToClaimDnrReward > currentEngagementTime) {
                return@launchWhenStarted
            }
            claimDnrReward(currentEngageTime = currentEngagementTime)

            // Reset variable to avoid showing DNR popup again for the same video
            showDnrRewardPopupForLf = false
        }
    }

    private fun claimDnrReward(currentEngageTime: Long) {
        dnrRewardViewModel.claimReward(
            DnrVideoWatchReward(
                viewId = viewId,
                questionId = id.toString(),
                duration = currentEngageTime,
                source = page,
                type = DnrRewardType.LIVE_CLASS.type
            )
        )
    }

    override fun onVideoCompleted() {
        if (isInPipMode) {
            updatePictureInPictureActions()
        }
        if (videoPlayerManager?.isTimeShiftVideoPlaying == true) {
            if (isLiveVideoEnded) {
                val engageTime = videoPlayerManager?.currentEngagementTime ?: 0
                val feedbackData = getFeedbackData(engageTime)
                if (feedbackData != null && isFeedbackRequired && !isYoutubePlaying && !supportFragmentManager.isDestroyed) {
                    showFeedbackDialog(feedbackData, engageTime.toString())
                } else if (!isYoutubePlaying) {
                    showReplayDialog()
                }
            }
        }
    }

    override fun onHidePlayerControls() {
        super.onHidePlayerControls()
        if (!isCommentEditTextFocused) {
            binding.commentContainer.hide()
            if (binding.textViewViewerCount.text.isNullOrBlank()) {
                binding.textViewViewerCount.hide()
            } else {
                binding.textViewViewerCount.show()
            }
        }
        binding.rvVideoTags.hide()
    }

    override fun onShowPlayerControls() {
        super.onShowPlayerControls()
        if (!tagsData?.commentTags.isNullOrEmpty()) {
            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                binding.commentContainer.show()
                binding.textViewViewerCount.hide()
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onFullscreenRequested() {
        userSelectedState = 180
        isFullScreen = true
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        updateFullScreenUI()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onPortraitRequested() {
        userSelectedState = 90
        isFullScreen = false
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        updatePortraitScreenUI()
    }

    override fun onViewIdPublished(viewId: String) {
        this.viewId = viewId
    }

    override fun singleTapOnPlayerView() {
        val isAdPlaying = videoPlayerManager?.videoFragment?.isAdPlaying() ?: false
        if (videoPlayerManager?.videoFragment == null || isAdPlaying) return
        if ((requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ||
                    requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) &&
            !tagsData?.commentTags.isNullOrEmpty()
        ) {
            handleLandscapeSingeTap()
        } else {
            handlePortraitSingleTap()
        }
    }

    private fun showVideoBlockedFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.videoContainer, VideoBlockedFragment.newInstance(viewAnswerData!!, page))
            .commit()
    }

    override fun onSeekPositionChange(position: Long) {
        if (premiumVideoOffset != null) {
            val videoDurationInSec: Long = when {
                videoPlayerManager?.isRtmpPlaying == true || videoPlayerManager?.isTimeShiftVideoPlaying == true -> {
                    (System.currentTimeMillis() - videoStartTime) / 1000
                }
                else -> {
                    position / 1000
                }
            }
            if (videoDurationInSec >= premiumVideoOffset!!) {
                showVideoBlockedFragment()
                return
            }
        }
        if (videoPlayerManager?.isRtmpPlaying == true
            || videoPlayerManager?.isTimeShiftVideoPlaying == true
        ) return
        positionInSec = position / 1000
        if (!topDoubtMap.isNullOrEmpty()) {
            var nearestTopDoubtPosition = -1L
            topDoubtMap.toSortedMap().forEach {
                if (it.key <= positionInSec) {
                    nearestTopDoubtPosition = it.key
                }
            }
            if (nearestTopDoubtPosition != -1L) {
                showTopDoubtView()
                onGoingTopDoubtKey = nearestTopDoubtPosition
            } else {
                binding.textViewTopDoubt.hide()
                binding.textViewTopDoubtLandscape.hide()
                binding.ivTopDoubtLandscapeToggle.hide()
            }
        }
        if (quizItemMap.containsKey(positionInSec)) {
            val quizDataAtPosition = quizItemMap[positionInSec] ?: return
            if (quizDataAtPosition is LiveQuizData) {
                removeFromRangeMap(positionInSec)
                liveQuizData = quizDataAtPosition
                showQuizFromList()
            } else if (quizDataAtPosition is LiveClassPollsList) {
                showPollsFragment(quizDataAtPosition, true)
            }
        } else if (quizItemRangeMap.containsKey(positionInSec)) {
            val quizIdAtPosition = quizItemRangeMap[positionInSec] ?: return
            removeFromRangeMap(quizIdAtPosition)
            val quizDataAtPosition = quizItemMap[quizIdAtPosition] ?: return
            if (quizDataAtPosition is LiveQuizData) {
                liveQuizData = quizDataAtPosition
                showQuizFromList()
                videoPlayerManager?.setExoCurrentPosition(quizIdAtPosition * 1000)
            }
        }
        mCommentBottomSheetFragment?.setCurrentOffset(positionInSec)
        liveClassCommentFragment?.setCurrentOffset(positionInSec)
        if (homeworkData?.nextButtonTime != null
            && position >= homeworkData?.nextButtonTime!!
            && !isNextButtonShown
        ) {
            addNextButton()
            isNextButtonShown = true
        } else if (homeworkData?.nextButtonTime != null
            && position < homeworkData?.nextButtonTime!!
            && isNextButtonShown
        ) {
            binding.nextBtn.visibility = View.GONE
            isNextButtonShown = false
        }
    }

    private fun removeFromRangeMap(quizId: Long) {
        val iterator = quizItemRangeMap.entries.iterator()
        while (iterator.hasNext()) {
            val next = iterator.next()
            if (next.value == quizId) {
                iterator.remove()
            }
        }
    }

    private fun showTopDoubtView() {
        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            if (binding.textViewTopDoubt.isNotVisible && !isInPipMode) {
                binding.textViewTopDoubt.show()
            }
        } else {
            if (binding.textViewTopDoubtLandscape.isNotVisible && !isInPipMode) {
                if (isTopDoubtLandscapeExpanded) {
                    binding.textViewTopDoubtLandscape.show()
                } else {
                    binding.textViewTopDoubtLandscape.hide()
                }
                binding.ivTopDoubtLandscapeToggle.show()
            }
        }

        if (isInPipMode) {
            binding.textViewTopDoubtLandscape.hide()
            binding.ivTopDoubtLandscapeToggle.hide()
            binding.textViewTopDoubt.hide()
        }
        if (!isTopDoubtVisibleEventSent) {
            isTopDoubtVisibleEventSent = true
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.TOP_DOUBT_VIEW_APPEAR,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.EVENT_NAME_ID, id ?: 0)
                        put(EventConstants.QUESTION_ID, qid)
                        put(EventConstants.ENTITY_ID, viewAnswerData?.videoEntityId.orEmpty())
                        put(EventConstants.ENTITY_TYPE, viewAnswerData?.videoEntityType.orEmpty())
                        put(EventConstants.PAGE, page)
                    }, ignoreSnowplow = true
                )
            )
        }
    }

    override fun onPictureInPictureModeRequested() {
        viewModel.sendEvent(EventConstants.PIP_MODE_BUTTON_CLICKED, ignoreSnowplow = true)
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        startActivity(homeIntent)
    }

    private val playerTypeOrMediaTypeChangedListener:
            PlayerTypeOrMediaTypeChangedListener = { playerType, mediaType ->
        isYoutubePlaying = playerType == PLAYER_TYPE_YOUTUBE
        currentMediaType = mediaType
        currentPlayerType = playerType
    }

    private val openWebViewOnVideoFail: OpenWebViewOnVideoFail = {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                name = EventConstants.WEBVIEW_OPEN_ON_VIDEO_FAIL,
                ignoreFirebase = false,
                ignoreSnowplow = true
            )
        )
        openWebView()
    }

    //region socket start

    private fun onSocketMessage(event: SocketEventType) {
        when (event) {
            is OnConnect -> {
                viewModel.joinSocket(detailId)
            }

            is OnJoin -> {

            }

            is OnMessage -> {
                //show toast with message
                if (event.message != null) {

                }
            }

            is OnResponseData -> {
                onSocketResponseData(event)
            }
        }
    }

    private fun onSocketError(errorEventType: SocketErrorEventType) = when (errorEventType) {
        is OnDisconnect -> {

        }
        is OnConnectError -> {

        }
        is OnConnectTimeout -> {

        }
    }

    private fun onSocketResponseData(responseData: OnResponseData) {
        if (responseData.data == null || isYoutubePlaying) return
        if (responseData.data is LiveClassQuestionDataList) {
            if (!responseData.data.list.isNullOrEmpty()) {
                val list: List<LiveQuizQna> = responseData.data.list
                    .map { data ->
                        LiveQuizQna(
                            quiz_question_id = data?.quizQuestionId.orEmpty(),
                            question = data?.question.orEmpty(),
                            options = data?.optionsList?.map { op ->
                                Option(op.key, op.value.orEmpty())
                            }.orEmpty(),
                            expiry = data?.expiry.orEmpty(),
                            response_expiry = data?.responseExpiry.orEmpty()
                        )
                    }
                val quizData = LiveQuizData(
                    responseData.data.quizResourceId,
                    responseData.data.liveClassResourceId, responseData.data.liveAt, list, false
                )
                showQuizFromRealtime(DN_SOCKET, quizData)
            }
        } else if (responseData.data is LiveClassPollsList && liveClassQnaFragment?.isVisible != true) {
            showPollsFragment(responseData.data, false)
        } else if (responseData.data is LiveClassAnnouncementData && liveClassQnaFragment?.isVisible != true) {
            showLiveClassAnnouncement(responseData.data)
        } else if (responseData.data is LiveClassStats) {
            if (binding.textViewViewerCount.isNotVisible && requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                binding.textViewViewerCount.show()
            }
            if (viewAnswerData?.state == "1" && videoActionLayout == "2") {
                binding.tvBottom.text = responseData.data.countText
            } else {
                binding.textViewViewerCount.text = responseData.data.countText
            }
        }
    }

    private fun showPollsFragment(data: LiveClassPollsList, isVod: Boolean) {
        sendPollShowEvent()
        LiveClassPollsFragment.newInstance(
            data, qid, viewAnswerData?.course.orEmpty(),
            viewAnswerData?.subject.orEmpty(), isFullScreen, isVod
        )
            .show(supportFragmentManager, LiveClassPollsFragment.TAG)
    }

    private fun showLiveClassAnnouncement(data: LiveClassAnnouncementData) {
        sendAnnouncementShowEvent()
        liveClassAnnouncementData = data
        if (isFullScreen) {
            announcementFragment?.dismiss()
            announcementFragment = LiveClassCommFragment.newInstance(data)
            announcementFragment?.show(supportFragmentManager, LiveClassCommFragment.TAG)
        } else {
            handleTimer(data.expiry ?: 0)
            binding.layoutAnnouncement.root.show()
            binding.layoutAnnouncement.tvTeacherName.text = data.title
            binding.layoutAnnouncement.tvAnnouncement.text = data.description
            binding.layoutAnnouncement.ivTeacher.loadImage(data.imageUrl)
        }
    }

    private fun handleTimer(count: Long) {
        disposable.clear()
        disposable.add(
            getTimerObservable(count)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        disposable.clear()
                        doOnTimerComplete()
                    }

                    override fun onNext(t: Long) {
                        val timerText = (count - t).toString() + " Sec"
                        binding.layoutAnnouncement.textViewTimer.text = timerText
                    }

                    override fun onError(e: Throwable) {
                        disposable.clear()
                        binding.layoutAnnouncement.root.hide()
                    }
                })
        )
    }

    fun doOnTimerComplete() {
        binding.layoutAnnouncement.root.hide()
    }

    private fun getTimerObservable(count: Long) =
        Observable.interval(0, 1, TimeUnit.SECONDS).take(count)

    //region socket end

    //region firebase start

    private fun observeForQuestionAndOptions(firebasePath: String) {
        Firebase.database
            .getReference(firebasePath)
            .child(qid)
            .apply {
                addValueEventListener(valueEventListener)
            }.also {
                dbReference = it
            }
    }

    private val valueEventListener = object : ValueEventListener {
        override fun onCancelled(p0: DatabaseError) {

        }

        override fun onDataChange(p0: DataSnapshot) {
            handleOnData(p0)
        }
    }

    private fun handleOnData(dataSnapshot: DataSnapshot) {
        if (videoPlayerManager?.isRtmpPlaying == true || videoPlayerManager?.isTimeShiftVideoPlaying == true) {
            val qna: LiveQuizData = try {
                dataSnapshot.getValue(LiveQuizData::class.java)
            } catch (e: Exception) {
                null
            } ?: return
            if (qna.ended) {
                isLiveVideoEnded = true
                if (videoPlayerManager?.isTimeShiftVideoPlaying == true) {
                    return
                }
                videoPlayerManager?.videoFragment?.resetVideo()
                val engageTime = videoPlayerManager?.currentEngagementTime ?: 0
                val feedbackData = getFeedbackData(engageTime)
                if (feedbackData != null && isFeedbackRequired && !isYoutubePlaying && !supportFragmentManager.isDestroyed) {
                    showFeedbackDialog(feedbackData, engageTime.toString())
                } else if (!isYoutubePlaying) {
                    showReplayDialog()
                }
                return
            }
            if (videoPlayerManager?.isTimeShiftVideoPlaying == true || viewAnswerData?.resourceDetailId == qna.quiz_resource_id
                || liveQuizData?.quiz_resource_id == qna.quiz_resource_id
            ) return
            showQuizFromRealtime(FIREBASE, qna)
        } else {
            if (isQuizListFetched) return
            viewModel.getLiveClassQuizDetail(qid)
        }
    }

    //region firebase end

    //region quiz implementation start

    @Synchronized
    private fun showQuizFromRealtime(type: String, liveQuizRealtimeData: LiveQuizData?) {
        val data = liveQuizRealtimeData ?: return

        val event = AnalyticsEvent(
            EventConstants.QUIZ_RECEIVED_REAL_TIME,
            hashMapOf(
                EventConstants.EVENT_NAME_ID to qid,
                EventConstants.RESOURCE_DETAIL_ID to data.quiz_resource_id,
                EventConstants.TYPE to type,
                EventConstants.TIME_STAMP to System.currentTimeMillis()
            ), ignoreSnowplow = true
        )

        val namedStatusPrefix = type + "_pos_"

        if (realTimeQuizResourceId.contains(data.quiz_resource_id)) {
            analyticsPublisher.publishEvent(event.apply {
                params.apply {
                    put(EventConstants.STATE, "2")
                    put(EventConstants.STATUS, namedStatusPrefix + "2")
                }
            })
            return
        } else {
            analyticsPublisher.publishEvent(event.apply {
                params.apply {
                    put(EventConstants.STATE, "1")
                    put(EventConstants.STATUS, namedStatusPrefix + "1")
                }
            })
        }

        realTimeQuizResourceId.add(data.quiz_resource_id)
        liveQuizData = liveQuizRealtimeData
        showQuizFromList()
    }

    override fun onSubmitOrTimeComplete() {
        mEnterInPipOnLeave = true
        showQuizFromList()
    }

    private fun showQuizFromList() {
        val quizLeftCount = liveQuizData?.list?.count { !it.isShown } ?: 0
        liveQuizData?.list?.firstOrNull { !it.isShown }
            .apply {
                this?.isShown = true
            }?.also {
                showQuizFragment(
                    it,
                    liveQuizData?.liveclass_resource_id ?: 0,
                    liveQuizData?.quiz_resource_id ?: 0,
                    quizLeftCount > 1
                )
            }
    }

    private fun showQuizFragment(
        qna: LiveQuizQna,
        detailId: Long,
        resourceDetailId: Long,
        hasNext: Boolean
    ) {
        if (isInPipMode) return

        mEnterInPipOnLeave = false

        var type: String? = null
        if (qna.type == 0L) {
            type = "SINGLE"
        } else if (qna.type == 1L) {
            type = "MULTI"
        }

        if (type == null) {
            return
        }
        val expiry = qna.expiry.toLongOrNull() ?: return
        val data = LiveQuizQuestionDataOptions(
            testId = qna.quiz_question_id.toIntOrNull() ?: 0,
            text = qna.question,
            type = type,
            doubtnutQuestionId = null,
            options = qna.options.map {
                LiveQuizQuestionDataOptions.TestOptionsId(it.key, it.value)
            }, expiry = expiry,
            responseExpiry = qna.response_expiry.toLongOrNull()
        )

        liveClassQnaFragment?.dismiss()
        if (!isYoutubePlaying) {
            liveClassQnaFragment =
                LiveClassQnaFragment.newInstance(data, qid, detailId, resourceDetailId, hasNext)
            liveClassQnaFragment?.show(supportFragmentManager, LiveClassQnaFragment.TAG)
        }
    }

    //region quiz implementation end

    private fun showReplayDialog() {
        if (isFinishing || isDestroyed || isInPipMode) {
            return
        }
        mEnterInPipOnLeave = false

        Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_video_ended)
            window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#cc000000")))
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT
            )
            show()
            setCancelable(true)
            dialogParentViewChoose.setOnClickListener {
                mEnterInPipOnLeave = true
                dismiss()
                finish()
            }

            buttonReplay.setOnClickListener {
                mEnterInPipOnLeave = true
                dismiss()
                reRequestSameVideo()
            }

            imageViewCloseChoose.setOnClickListener {
                mEnterInPipOnLeave = true
                dismiss()
                finish()
            }

            buttonExit.setOnClickListener {
                mEnterInPipOnLeave = true
                dismiss()
                finish()
            }
        }
    }

    fun getFeedbackData(durationInSec: Int) =
        tagsData?.ratings?.firstOrNull { durationInSec >= it.min && durationInSec <= it.max }?.ratingList

    private fun showFeedbackDialog(ratingData: List<Ratings>, engageTime: String) {
        if (isInPipMode) return

        mEnterInPipOnLeave = false
        sendFeedBackViewEvent()
        liveClassFeedbackFragment = LiveClassFeedbackFragment.newInstance(
            ratingList = ratingData as ArrayList<Ratings>,
            detailId = detailId,
            engageTime = engageTime,
            enableSmiley = tagsData?.enableSmiley ?: true
        )
        if (isFullScreen) {
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({
                if (!isFinishing && !isDestroyed) {
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    updateFullScreenUI()
                    if (!supportFragmentManager.isDestroyed) {
                        try {
                            liveClassFeedbackFragment?.show(
                                supportFragmentManager,
                                LiveClassFeedbackFragment.TAG
                            )
                        } catch (e: Exception) {

                        }
                    }
                }
            }, ROTATION_DELAY)
        } else {
            if (!isFinishing && !isDestroyed) {
                if (!supportFragmentManager.isDestroyed) {
                    try {
                        liveClassFeedbackFragment?.show(
                            supportFragmentManager,
                            LiveClassFeedbackFragment.TAG
                        )
                    } catch (e: Exception) {

                    }
                }
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        if (isDeviceOrientationOn()) {
            Sensey.getInstance().startOrientationDetection(this)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isDeviceOrientationOn()) {
            Sensey.getInstance().stopOrientationDetection(this)
        }
    }

    override fun onStart() {
        super.onStart()
        binding.topDoubtContainer.hide()
        binding.topDoubtContainerPortrait.hide()
        binding.topDoubtFrameLayout.hide()
        dbReference?.addValueEventListener(valueEventListener)
    }

    override fun startActivity(intent: Intent?) {
        isBranchLink = intent?.data?.host.equals(Constants.BRANCH_HOST)
        if (isBranchLink) {
            intent?.putExtra(Defines.IntentKeys.ForceNewBranchSession.key, true)
        }
        super.startActivity(intent)
    }

    override fun onStop() {
        if (isBranchLink) finishAffinity()
        super.onStop()
        binding.topDoubtContainer.hide()
        binding.topDoubtContainerPortrait.hide()
        binding.topDoubtFrameLayout.hide()
        dbReference?.removeEventListener(valueEventListener)
        handleExitEvent()
    }

    override fun onAttachFragment(fragment: Fragment) {
        super.onAttachFragment(fragment)
        when (fragment) {
            is LiveClassQnaFragment -> {
                fragment.setListener(this)
            }
            is LiveClassCommentFragment -> {
                fragment.setActionListener(this)
            }
            is LiveClassFeedbackFragment -> {
                fragment.setActionListener(this)
            }
            is VideoStatusFragment -> {
                fragment.setListener(object : VideoStatusListener {
                    override fun reRequest() {
                        reRequestSameVideo()
                    }
                })
            }
        }
    }

    fun reRequestSameVideo() {
        DoubtnutApp.INSTANCE.bus()?.send(LiveClassVideoEvent(LiveClassVideoActionReRequest))
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun requestNewVideo(questionId: String, page: String) {
        videoPlayerManager?.pauseExoPlayer()
        DoubtnutApp.INSTANCE.bus()
            ?.send(LiveClassVideoEvent(LiveClassVideoActionRequestNew(questionId, page)))
        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
        rxBusObserver?.dispose()
        handler.removeCallbacksAndMessages(null)
    }

    override fun performAction(action: Any) {
        if (action is OnFeedbackClosed) {
            mEnterInPipOnLeave = true
            liveClassFeedbackFragment?.dismiss()
            if (isLiveVideoEnded && !isYoutubePlaying) {
                showReplayDialog()
            } else {
                sendCancelEventToRxBus()
                super.onBackPressed()
            }
        } else if (action is OnCommentEditTextClicked) {
            isCommentEditTextFocused = action.hasFocus
            videoPlayerManager?.videoFragment?.hidePlayerController(isInPipMode)
            if (!action.hasFocus) {
                binding.commentContainer.hide()
                binding.textViewViewerCount.show()
            }
        } else if (action is WatchLaterRequest) {
            viewModel.addToWatchLater(action.id)
        } else if (action is OnSimilarWidgetClick) {
            requestNewVideo(action.questionId, action.page)
        } else if (action is OnVideoTabSelected) {
            viewModel.getHomeworkData(viewAnswerData?.questionId.orEmpty(), action.tabId, page)
        } else if (action is OnHomeWorkListClicked) {
            if (action.status) {
                binding.fragmentContainer.show()
                binding.rvWidgets.hide()
                supportFragmentManager.beginTransaction()
                    .replace(
                        binding.fragmentContainer.id,
                        HomeWorkSolutionFragment.newInstance(viewAnswerData?.questionId.orEmpty())
                    ).commitAllowingStateLoss()
            } else {
                binding.fragmentContainer.show()
                binding.rvWidgets.hide()
                supportFragmentManager.beginTransaction()
                    .replace(
                        binding.fragmentContainer.id,
                        HomeWorkFragment.newInstance(viewAnswerData?.questionId.orEmpty(), true)
                    ).commitAllowingStateLoss()
            }
        } else if (action is OnNotesClicked) {
            binding.rvWidgets.hide()
            binding.fragmentContainer.show()
            checkPDFUrl(action.link, homeworkData?.assortmentId, action.id, action.iconUrl)
        } else if (action is OnVideoOffsetClicked) {
            videoPlayerManager?.setExoCurrentPosition(action.offset?.toLong() ?: 0L)
        } else if (action is OnNotesClosed) {
            binding.fragmentContainer.hide()
            binding.rvWidgets.show()
        } else if (action is OnHomeworkSubmitted) {
            viewModel.getHomeworkData(viewAnswerData?.questionId.orEmpty(), "homework", page)
            supportFragmentManager.beginTransaction()
                .replace(
                    binding.fragmentContainer.id,
                    HomeWorkSolutionFragment.newInstance(action.id)
                )
                .commitAllowingStateLoss()
        }
    }

    private fun checkPDFUrl(link: String?, assortmentId: String?, id: String?, iconUrl: String?) {
        try {
            if (!URLUtil.isValidUrl(link)) {
                showToast(
                    this,
                    resources.getString(R.string.notAvalidLink)
                )
            } else {
                var isDownloaded = 1
                if (link?.contains(".html")!!) {
                    isDownloaded = 0
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    CustomTabActivityHelper.openCustomTab(
                        this,
                        customTabsIntent,
                        Uri.parse(link),
                        WebViewFallback()
                    )
                } else {
                    supportFragmentManager.beginTransaction()
                        .replace(
                            binding.fragmentContainer.id,
                            PdfViewerFragment.previewPdfFromTheUrl(
                                url = link,
                                assortmentId = assortmentId,
                                resourceId = id,
                                iconUrl = iconUrl
                            )
                        ).commitAllowingStateLoss()
                }
                markNotesRead(id, isDownloaded)
            }
        } catch (error: ActivityNotFoundException) {
            showToast(
                this, resources.getString(R.string.donothaveanybrowser)
            )
        }
    }

    private fun markNotesRead(resourceId: String?, isDownloaded: Int) {
        DataHandler.INSTANCE.courseRepository.markNotesRead(resourceId.orEmpty(), isDownloaded)
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({})
    }

    private fun hideAllTopDoubtViews() {
        binding.topDoubtContainer.hide()
        binding.topDoubtContainerPortrait.hide()
        binding.topDoubtFrameLayout.hide()
        binding.textViewTopDoubtLandscape.hide()
        binding.ivTopDoubtLandscapeToggle.hide()
        binding.textViewTopDoubt.hide()
    }

    override fun onBackPressed() {
        hideAllTopDoubtViews()
        val engageTime = videoPlayerManager?.currentEngagementTime ?: 0
        val feedbackData = getFeedbackData(engageTime)
        if (feedbackData != null && isFeedbackRequired
            && liveClassFeedbackFragment == null
            && !isYoutubePlaying && !supportFragmentManager.isDestroyed
        ) {
            showFeedbackDialog(feedbackData, engageTime.toString())
        } else {
            if (isTaskRoot) {
                bringLauncherTaskToFront()
                finishAndRemoveTask()
            }
            super.onBackPressed()
            sendCancelEventToRxBus()
        }
    }

    private fun sendCancelEventToRxBus() {
        DoubtnutApp.INSTANCE.bus()?.send(LiveClassVideoEvent(LiveClassVideoActionCancel))
    }

    private var mPipCloseDisposable: Disposable? = null
    private var mEnterInPipOnLeave: Boolean = true

    override fun onUserLeaveHint() {
        if (mEnterInPipOnLeave) {
            tryToEnterInPictureInPictureMode()
        }
    }

    /**
     * Enters Picture-in-Picture mode.
     */
    private fun tryToEnterInPictureInPictureMode() {
        hideAllTopDoubtViews()
        hidePipViews()
        // Wrapping in try/catch as even when [canEnterPipMode()] returns true, we observe issues
        // where activity can't enter in PiP mode and till now, we've been unable to trace the cause of
        // why this might be happening
        try {
            if (canEnterPipMode()) {
                if (Utils.hasPipModePermission() && videoPlayerManager?.canEnterInPIP() == true) {
                    // Hide the controls in picture-in-picture mode.
                    // Calculate the aspect ratio of the PiP screen.
                    videoPlayerManager?.hidePlayerController(true)
                    mPictureInPictureParamsBuilder?.let {
                        val videoContainerRect = Rect()
                        binding.videoContainer.getDrawingRect(videoContainerRect)
                        binding.rootView.offsetDescendantRectToMyCoords(
                            binding.videoContainer,
                            videoContainerRect
                        )
                        it.setSourceRectHint(videoContainerRect)

                        val videoDimenRational =
                            Rational(binding.videoContainer.width, binding.videoContainer.height)
                        // Check if aspect ratio is in specified range before going in PiP mode.
                        // We take a slightly narrow range than what is defined for PiP to be on a safe side.
                        if (videoDimenRational.toDouble() in 0.42..2.38) {
                            it.setAspectRatio(videoDimenRational)
                            enterPictureInPictureMode(it.build())
                        }
                    }
                    viewModel.sendEvent(
                        EventConstants.PIP_MODE_PERMISSION_ALLOWED,
                        ignoreSnowplow = true
                    )
                } else {
                    viewModel.sendEvent(
                        EventConstants.PIP_MODE_PERMISSION_DENIED,
                        ignoreSnowplow = true
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(e, VideoPageActivity.TAG_PIP)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean, newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        videoPlayerManager?.controllerAutoShow(isInPictureInPictureMode.not())
        videoPlayerManager?.setPipStatus(isInPictureInPictureMode)

        // Try-catch to monitor BroadcastReceiver unregister exceptions without app crash
        try {
            if (isInPictureInPictureMode) {
                subscribeToRxBus()
                registerReceiver(mPipActionBroadcastReceiver, IntentFilter(ACTION_MEDIA_CONTROL))
                mCommentBottomSheetFragment?.dismiss()
                mCommentBottomSheetFragment = null
                binding.commentContainer.hide()

                mPipModeExitListener.pipEntered()
                mPipModeLastEnterTimeMillis = System.currentTimeMillis()
                viewModel.sendEvent(EventConstants.PIP_MODE_ENABLED, ignoreSnowplow = true)
            } else {
                // Make sure that events are sent before anything bad happens
                mPipModeExitListener.pipExited()
                viewModel.sendEvent(
                    EventConstants.PIP_MODE_VIEWED, hashMapOf(
                        Constants.VIDEO_PLAY_TIME to getAndResetPipModeWatchTime(),
                        Constants.QUESTION_ID to qid,
                        Constants.VIEW_ID to viewId
                    ), ignoreSnowplow = true
                )

                unregisterReceiver(mPipActionBroadcastReceiver)
                mPipCloseDisposable?.dispose()
            }
        } catch (e: Exception) {
            Log.e(e, VideoPageActivity.TAG_PIP)
        }
    }

    private fun getAndResetPipModeWatchTime(): Long {
        val sessionWatchTime = System.currentTimeMillis() - mPipModeLastEnterTimeMillis
        mPipModeLastEnterTimeMillis = -1
        return sessionWatchTime
    }

    private fun subscribeToRxBus() {
        val bus = DoubtnutApp.INSTANCE.bus() ?: return
        mPipCloseDisposable = bus.toObservable()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ event ->
                if (event is PipWindowCloseEvent) {
                    finishAndRemoveTask()
                    mPipCloseDisposable?.dispose()
                    mPipCloseDisposable = null
                }
            }, {
                it.printStackTrace()
            })
    }

    private fun updatePictureInPictureActions() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && (isInPipMode || canEnterPipMode())) {
                @DrawableRes val iconId: Int
                val title: String
                val controlType: Int
                val requestCode: Int

                if (videoPlayerManager?.isExoPlayerVideoPlaying == true) {
                    iconId = R.drawable.ic_pause_black_24dp
                    title = labelPause
                    controlType = CONTROL_TYPE_PAUSE
                    requestCode = REQUEST_PAUSE
                } else {
                    iconId = R.drawable.ic_play_arrow_black_24dp
                    title = labelPlay
                    controlType = CONTROL_TYPE_PLAY
                    requestCode = REQUEST_PLAY
                }

                val actions = arrayListOf<RemoteAction>()

                // Use distinct request codes for play and pause, or the PendingIntent won't
                // be properly updated.
                val intent = PendingIntent.getBroadcast(
                    this, requestCode,
                    Intent(ACTION_MEDIA_CONTROL).putExtra(EXTRA_CONTROL_TYPE, controlType), 0
                )
                val icon = Icon.createWithResource(this, iconId)
                icon.setTint(getColor(R.color.white))
                actions.add(RemoteAction(icon, title, title, intent))

                mPictureInPictureParamsBuilder?.let {
                    it.setActions(actions)
                    setPictureInPictureParams(it.build())
                }
            }
        } catch (e: Exception) {
            Log.e(e, TAG_PIP)
        }
    }

    private fun canEnterPipMode(checkPlayer: Boolean = true): Boolean {
        return deviceSupportsPipMode() &&
                if (checkPlayer) {
                    videoPlayerManager?.getExoPlayerPlaybackState() == Player.STATE_READY
                } else {
                    true
                }
    }

    fun isLiveClassOngoingForQid(qid: String): Boolean {
        return qid == viewAnswerData?.questionId
    }

    fun onLiveClassDoubtAnswered(commentId: String) {
        if (requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE && requestedOrientation != ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
            openAnsweredDoubtWithCommentId = commentId
            mCommentBottomSheetFragment?.dismiss()
            mCommentBottomSheetFragment = null
            openCommentBottomSheet()
        } else {
            showSnackbar(
                R.string.comment_doubt_resolved,
                R.string.string_view,
                Snackbar.LENGTH_LONG,
                commentId
            ) {
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed({
                    if (!isFinishing && !isDestroyed) {
                        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        updateFullScreenUI()
                        if (!supportFragmentManager.isDestroyed) {
                            try {
                                openAnsweredDoubtWithCommentId = commentId
                                mCommentBottomSheetFragment?.dismiss()
                                mCommentBottomSheetFragment = null
                                openCommentBottomSheet()
                            } catch (e: Exception) {

                            }
                        }
                    }
                }, 100)
            }
        }
    }

    //region event start

    private fun sendPageOpenEvent() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.LIVE_CLASS_PAGE_OPEN,
                hashMapOf(
                    EventConstants.WIDGET to TAG,
                    EventConstants.SOURCE to id.toString()
                ),
                ignoreSnowplow = true
            )
        )
    }

    private fun sendPollShowEvent() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.LIVE_CLASS_POLL,
                hashMapOf(
                    EventConstants.QUESTION_ID to qid,
                    EventConstants.COURSE to viewAnswerData?.course.orEmpty(),
                    EventConstants.SUBJECT to viewAnswerData?.subject.orEmpty()
                ), ignoreSnowplow = true
            )
        )
    }

    private fun sendPollCloseEvent() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.LIVE_CLASS_POLL_CLOSE,
                hashMapOf(
                    EventConstants.QUESTION_ID to qid,
                    EventConstants.COURSE to viewAnswerData?.course.orEmpty(),
                    EventConstants.SUBJECT to viewAnswerData?.subject.orEmpty()
                )
            )
        )
    }

    private fun sendFeedBackViewEvent() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_FEEDBACK_VIEW,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.DETAIL_ID, detailId)
                }, ignoreSnowplow = true
            )
        )
    }

    private fun sendAnnouncementShowEvent() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.LIVE_CLASS_ANNOUNCEMENT,
                hashMapOf(
                    EventConstants.QUESTION_ID to qid,
                    EventConstants.COURSE to viewAnswerData?.course.orEmpty(),
                    EventConstants.SUBJECT to viewAnswerData?.subject.orEmpty()
                )
            )
        )
    }

    private fun handleExitEvent() {
        if (isStarted) {
            sentCount += 1
            val exitTime = System.currentTimeMillis() / 1000
            val durationActual = exitTime - (enterTime ?: exitTime)
            val duration = Utils.getTimeSpentForEventFromActualDuration(durationActual)
            val liveClassExitEvent = AnalyticsEvent(
                EventConstants.LIVE_CLASS_EXIT,
                hashMapOf(
                    EventConstants.DURATION to duration,
                    EventConstants.DURATION_ACTUAL to durationActual.toString(),
                    EventConstants.EVENT_NAME_ID to qid,
                    EventConstants.TEACHER_NAME to viewAnswerData?.facultyName.orEmpty(),
                    EventConstants.IS_LIVE to if (isVOD) {
                        "0"
                    } else {
                        "1"
                    },
                    EventConstants.SENT_COUNT to sentCount.toString(),
                    EventConstants.COURSE_NAME to viewAnswerData?.course.orEmpty(),
                    EventConstants.SUBJECT to viewAnswerData?.subject.orEmpty(),
                    EventConstants.IS_PREMIUM to isPremium
                ), ignoreSnowplow = true,
                ignoreBranch = durationActual <= 300
            )
            analyticsPublisher.publishEvent(liveClassExitEvent)
            analyticsPublisher.publishMoEngageEvent(liveClassExitEvent.apply {
                params[EventConstants.DURATION_ACTUAL] = durationActual
            })
        }
    }

    private fun sendMoEngageScreenTrackEvent() {
        analyticsPublisher.publishMoEngageEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NAME_VIDEO_PAGE,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.VIDEO_CONTENT_TYPE, EventConstants.VIDEO_TYPE_LIVE_VIDEO)
                    put(EventConstants.EVENT_NAME_ID, id ?: 0)
                    put(EventConstants.PAGE, page)
                    put(EventConstants.SOURCE, TAG)
                })
        )
    }

    private fun sendTopDoubtToggleEvent(state: String) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.TOP_DOUBT_EXPAND_COLLAPSE,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.EVENT_NAME_ID, id ?: 0)
                    put(EventConstants.QUESTION_ID, qid)
                    put(EventConstants.ENTITY_ID, viewAnswerData?.videoEntityId.orEmpty())
                    put(EventConstants.ENTITY_TYPE, viewAnswerData?.videoEntityType.orEmpty())
                    put(
                        EventConstants.ORIENTATION,
                        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE || requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE) {
                            EventConstants.ORIENTATION_LANDSCAPE
                        } else {
                            EventConstants.ORIENTATION_PORTRAIT
                        }
                    )
                    put(EventConstants.TOGGLE, state)
                    put(EventConstants.PAGE, page)
                }, ignoreSnowplow = true
            )
        )
    }

    private fun sendCommonEvent() {
        val eventName = if (isVOD) {
            EventConstants.EVENT_LIVE_VIDEO_REPLAY_WATCHED
        } else {
            EventConstants.EVENT_LIVE_VIDEO_WATCHED
        }
        if (eventName == EventConstants.EVENT_LIVE_VIDEO_WATCHED) {
            Utils.sendClassLangEvents(EventConstants.EVENT_LIVE_VIDEO_WATCHED)
            Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_LIVE_VIDEO_WATCHED)
        }
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, hashMapOf<String, Any>().apply {
            put(EventConstants.VIDEO_CONTENT_TYPE, EventConstants.VIDEO_TYPE_LIVE_VIDEO)
            put(EventConstants.EVENT_NAME_ID, id ?: 0)
            put(EventConstants.QUESTION_ID, qid)
            put(EventConstants.PAGE, page)
            put(EventConstants.SOURCE, TAG)
            put(EventConstants.IS_PREMIUM, isPremium)
        }, ignoreSnowplow = true))
    }

    private fun sendMoEngageVideoWatchEvent() {
        analyticsPublisher.publishMoEngageEvent(
            AnalyticsEvent(
                EventConstants.EVENT_VIDEO_WATCHED,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.VIDEO_CONTENT_TYPE, EventConstants.VIDEO_TYPE_LIVE_VIDEO)
                    put(EventConstants.EVENT_NAME_ID, id ?: 0)
                    put(EventConstants.QUESTION_ID, qid)
                    put(EventConstants.PAGE, page)
                    put(EventConstants.SOURCE, TAG)
                    put(EventConstants.IS_PREMIUM, isPremium)
                })
        )
    }

    private fun sendBranchVideoWatchEvent() {
        repeat((0..1).count()) {
            analyticsPublisher.publishBranchIoEvent(
                AnalyticsEvent(
                    EventConstants.LIVE_VIDEO_WATCHED,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.VIDEO_CONTENT_TYPE, EventConstants.VIDEO_TYPE_LIVE_VIDEO)
                        put(EventConstants.EVENT_NAME_ID, id ?: 0)
                        put(EventConstants.QUESTION_ID, qid)
                        put(EventConstants.PAGE, page)
                        put(EventConstants.SOURCE, TAG)
                        put(EventConstants.IS_PREMIUM, isPremium)
                    })
            )
        }
    }

    override fun provideViewBinding(): ActivityLiveClassBinding {
        return ActivityLiveClassBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): LiveClassViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        statusbarColor(this, R.color.grey_statusbar_color)
        videoPlayerManager = VideoPlayerManager(
            supportFragmentManager, this,
            R.id.videoContainer, playerTypeOrMediaTypeChangedListener, openWebViewOnVideoFail
        )
        enterTime = System.currentTimeMillis() / 1000
        page = intent.getStringExtra(PAGE).orEmpty()
        viewAnswerData = intent.getParcelableExtra(VIEW_VIDEO_DATA)
        id = viewAnswerData?.questionId?.toIntOrNull() ?: 0
        viewId = viewAnswerData.forceUnWrap().viewId
        qid = viewAnswerData.forceUnWrap().questionId
        detailId = viewAnswerData.forceUnWrap().detailId.orEmpty()
        isPremium = viewAnswerData.forceUnWrap().isPremium
        isVOD = !viewAnswerData.forceUnWrap().isRtmp
        chapter = viewAnswerData.forceUnWrap().chapter.orEmpty()
        dnrRewardViewModel = viewModelProvider(viewModelFactory)

        setupUxCam(isPremium)
        setUpObserver()
        setUpRecyclerView()
        viewModel.getHomeworkData(viewAnswerData?.questionId.orEmpty(), page = page)
        lifecycleScope.launchWhenResumed {
            viewModel.getSnackBar(qid)
        }

        handler.removeCallbacksAndMessages(null)
        onStartData()
        setClickListeners()
        sendPageOpenEvent()
        sendMoEngageScreenTrackEvent()
        Utils.removeTasksWithAnyOfGivenActivitiesAsRoot(taskId, *Utils.pipSupportedActivities)
        viewModel.storeLiveClassVideoWatchCoreAction()
        binding.topDoubtFrameLayout.setOnClickListener {
            binding.topDoubtContainer.hide()
            binding.topDoubtContainerPortrait.hide()
            binding.topDoubtFrameLayout.hide()
            videoPlayerManager?.resumeExoPlayer()
            sendTopDoubtToggleEvent(EventConstants.COLLAPSE)
        }
        openAnsweredDoubtWithCommentId =
            intent.getStringExtra(OPEN_ANSWERED_DOUBT_COMMENT_ID).orEmpty().ifEmptyThenNull()
        if (!openAnsweredDoubtWithCommentId.isNullOrBlank()) {
            onLiveClassDoubtAnswered(openAnsweredDoubtWithCommentId.orEmpty())
        }
        showBuyNowButton()
    }
    //region event end

    private fun onBookmarkDataSuccess(data: BookmarkData?) {
        toast(data?.message ?: "")
        binding.bookmarkBtn.loadImageEtx(data?.iconUrl.orEmpty())
    }

    private fun addNextButton() {
        if (homeworkData?.nextVideoTitle.isNotNullAndNotEmpty()) {
            binding.nextBtn.visibility = View.VISIBLE
            binding.nextBtn.text = homeworkData?.nextVideo ?: "Next Chapter"
            binding.nextBtn.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.VIDEO_PAGE_NEXT_CLICK,
                        hashMapOf<String, Any>()
                    )
                )
                finish()
                deeplinkAction.performAction(this, homeworkData?.nextVideoDeeplink)
            }
        }
    }

    private fun showVideoTags() {
        binding.rvVideoTags.visibility = View.VISIBLE
        if (binding.rvVideoTags.adapter == null) {
            binding.rvVideoTags.adapter =
                VideoTagsAdapter(homeworkData?.topicList, this, analyticsPublisher)
        }
    }

    private fun downloadVideo(
        questionId: String,
        title: String
    ) {
        val downloadTracker = ExoDownloadTracker.getInstance(this)
        downloadTracker.downloadVideo(this, questionId, title)
    }

    private fun shareVideoOnStudyGroup(ocrText: String) {
        val questionData = QuestionToShare(
            thumbnail = homeworkData?.thumbnailUrl ?: "",
            ocrText = ocrText,
            questionId = viewAnswerData?.questionId ?: ""
        )
        SgShareActivity.getStartIntent(this, questionData).apply {
            startActivity(this)
        }
    }

    private fun onPlaylistClicked() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.VIDEO_PAGE_PLAYLIST_CLICK,
                hashMapOf()
            )
        )
        nextVideoDialog = NextVideoDialogFragment.newInstance(
            viewAnswerData?.questionId.orEmpty(),
            homeworkData?.nextVideoTitle.orEmpty()
        )
        nextVideoDialog?.show(supportFragmentManager, TAG)
    }

    private fun hidePipViews() {
        binding.nextVideoContainer.hide()
        nextVideoDialog?.dialog?.dismiss()
        mCommentBottomSheetFragment?.dialog?.dismiss()
        videoPlayerManager?.videoFragment?.closeVideoQualityBottomSheet()
        DoubtnutApp.INSTANCE.bus()?.send(HidePipViewEvent())
    }

    private fun setupUxCam(isPremium: Boolean) {
        if (isPremium) {
            UXCam.tagScreenName(TAG_PAID)
        } else {
            UXCam.tagScreenName(TAG_FREE)
        }
    }

    private fun handleLandscapeSingeTap() {
        if (videoPlayerManager?.videoFragment?.isPlayerControllerVisible() == true) {
            videoPlayerManager?.videoFragment?.hidePlayerController(isInPipMode)
        } else {
            if (!isCommentEditTextFocused) {
                if (supportFragmentManager.findFragmentByTag(LiveClassCommentFragment.TAG) == null) {
                    addLandscapeComments()
                }
                binding.commentContainer.show()
                binding.textViewViewerCount.hide()
                videoPlayerManager?.videoFragment?.showPlayerController()
                videoPlayerManager?.videoFragment?.adjustBottomMargin(
                    ViewUtils.dpToPx(
                        60f,
                        this
                    ).toInt()
                )
                showVideoTags()
            } else {
                if (binding.textViewViewerCount.tvComment != null) {
                    isCommentEditTextFocused = false
                    binding.commentContainer.hide()
                    binding.textViewViewerCount.show()
                    hideKeyboard(binding.textViewViewerCount.tvComment)
                }
            }
        }
    }

    private fun handlePortraitSingleTap() {
        if (videoPlayerManager?.videoFragment?.isPlayerControllerVisible() == true) {
            videoPlayerManager?.videoFragment?.hidePlayerController(isInPipMode)
        } else {
            if (!isCommentEditTextFocused)
                videoPlayerManager?.videoFragment?.showPlayerController()
            showVideoTags()
        }
    }

    private fun onLikeClicked() {
        val viewAnswerData = this.viewAnswerData ?: return
        val likeCount = viewAnswerData.likeCount

        binding.btnLike.isSelected = !binding.btnLike.isSelected
        if (binding.btnLike.isSelected) {
            binding.btnDislike.isSelected = false
            binding.tvLikeCount.text = (likeCount + 1).toString()
        } else {
            binding.tvLikeCount.text = (likeCount).toString()
        }

        viewModel.likeButtonClicked(
            viewAnswerData.videoName,
            viewAnswerData.questionId,
            viewAnswerData.answerId,
            (videoPlayerManager?.currentPlayerPosition ?: 0).toLong(),
            page,
            viewId,
            binding.btnLike.isSelected
        )
    }

    private fun onDislikeClicked() {
        val viewAnswerData = this.viewAnswerData ?: return

        binding.btnDislike.isSelected = !binding.btnDislike.isSelected
        if (binding.btnDislike.isSelected) {
            binding.btnLike.isSelected = false
        }

        viewModel.disLikeButtonClicked(
            viewAnswerData.videoName, viewAnswerData.questionId, viewAnswerData.answerId,
            (videoPlayerManager?.currentPlayerPosition ?: 0).toLong(), page, "",
            viewId, !binding.btnDislike.isSelected
        )

    }

    private fun onWatchLaterClicked() {
        val questionId = viewAnswerData?.questionId ?: return
        viewModel.addToWatchLater(questionId)
    }
}