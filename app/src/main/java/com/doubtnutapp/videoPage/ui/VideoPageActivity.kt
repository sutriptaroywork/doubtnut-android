package com.doubtnutapp.videoPage.ui

import android.Manifest
import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.util.Rational
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.LottieAnimDataStore
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.observer.SingleEventObserver
import com.doubtnut.core.ui.helpers.LinearLayoutManagerWithSmoothScroller
import com.doubtnut.core.utils.LottieAnimationViewUtils.applyAnimationFromUrl
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.EventBus.*
import com.doubtnutapp.addtoplaylist.AddToPlaylistFragment
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.PlayVideo
import com.doubtnutapp.base.extension.observeL
import com.doubtnutapp.base.extension.viewBinding
import com.doubtnutapp.bottomnavigation.model.BottomNavigationTabsData
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.course.widgets.PopularCourseWidget
import com.doubtnutapp.course.widgets.PopularCourseWidgetModel
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.doubtfeed.DoubtFeedBanner
import com.doubtnutapp.data.remote.models.videopageplaylist.VideoPagePlaylist
import com.doubtnutapp.databinding.ActivityPageVideoBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.dnr.model.DnrReward
import com.doubtnutapp.dnr.model.DnrRewardType
import com.doubtnutapp.dnr.model.DnrVideoWatchReward
import com.doubtnutapp.dnr.ui.fragment.DnrRewardBottomSheetFragment
import com.doubtnutapp.dnr.ui.fragment.DnrRewardDialogFragment
import com.doubtnutapp.dnr.viewmodel.DnrRewardViewModel
import com.doubtnutapp.domain.base.SolutionResourceType.SOLUTION_RESOURCE_TYPE_VIDEO
import com.doubtnutapp.domain.videoPage.entities.AnalysisData
import com.doubtnutapp.domain.videoPage.entities.NcertVideoDetails
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.liveclass.ui.LiveClassActivity
import com.doubtnutapp.liveclass.ui.VideoBlockedFragment
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.resourcelisting.ui.adapter.ResourcePlaylistAdapter
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.similarVideo.model.SimilarVideoList
import com.doubtnutapp.similarVideo.ui.LandscapeSimilarVideoBottomDialog
import com.doubtnutapp.similarVideo.ui.NcertBooksBottomSheetFragment
import com.doubtnutapp.similarVideo.ui.NcertSimilarFragment
import com.doubtnutapp.similarVideo.ui.SimilarVideoFragment
import com.doubtnutapp.similarVideo.viewmodel.NcertSimilarViewModel
import com.doubtnutapp.similarplaylist.ui.SimilarPlaylistFragment
import com.doubtnutapp.studygroup.ui.fragment.SgShareActivity
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.ui.answer.VideoDislikeFeedbackDialog
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.forum.comments.CommentBottomSheetFragment
import com.doubtnutapp.ui.forum.comments.CommentsActivity
import com.doubtnutapp.ui.mediahelper.PLAYER_TYPE_YOUTUBE
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.checkisClass9to12User
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.video.*
import com.doubtnutapp.videoPage.model.*
import com.doubtnutapp.videoPage.viewmodel.VideoPageViewModel
import com.doubtnutapp.youtubeVideoPage.model.VideoTagViewItem
import com.doubtnutapp.youtubeVideoPage.ui.adapter.VideoTagListAdapter
import com.facebook.appevents.AppEventsConstants
import com.github.nisrulz.sensey.OrientationDetector
import com.github.nisrulz.sensey.Sensey
import com.google.android.exoplayer2.Player
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.appindexing.Action
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.appindexing.Indexable
import com.google.gson.Gson
import com.uxcam.UXCam
import dagger.Lazy
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import kotlinx.android.synthetic.main.activity_page_video.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

class VideoPageActivity : BaseActivity(),
    OrientationDetector.OrientationListener,
    SimilarPlaylistFragment.OnInteractionListener,
    SimilarVideoFragment.OnFragmentInteractionListener,
    VideoFragmentListener,
    HasAndroidInjector,
    ActionPerformer,
    SupportsPictureInPictureMode {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var timerDisposable: CompositeDisposable

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

/*    @Inject
    lateinit var convivaVideoAnalytics: Lazy<ConvivaVideoAnalytics>*/

    @Inject
    lateinit var defaultDataStore: Lazy<DefaultDataStore>

    @Inject
    lateinit var dataStore: DefaultDataStore

    @Inject
    lateinit var lottieAnimDataStore: LottieAnimDataStore

    private var rxObserver: Disposable? = null

    private val PAGE_VIDEO = "video_page"

    private val binding by viewBinding(ActivityPageVideoBinding::inflate)

    private lateinit var videoPageViewModel: VideoPageViewModel
    private lateinit var ncertViewModel: NcertSimilarViewModel
    private var similarFragmentInstance: SimilarVideoFragment? = null

    private val handler: Handler = Handler()

    private var userSelectedState: Int = 90
    private var isFullScreen = false

    private var questionId: String? = ""
    private var commentData: CommentData? = null
    private var commentBottomSheetFragment: CommentBottomSheetFragment? = null
    private var playlistId: String? = null
    private var mcId: String? = null
    private var page: String = ""
    private var parentPage: String? = null
    private var isMicroConcept = false
    private var entityId: String? = ""
    private var entityType: String? = ""
    private var batchId: String? = null
    private var ocr: String? = null
    private var youtubeId: String? = null

    private var isLiked: Boolean = false
    private var isDisLiked: Boolean = false
    private var likeCount: Int = 0
    private var disLikeCount: Int = 0
    private var answerId: String = ""
    private var videoTitle: String = ""
    private var thumbnailImage: String = ""
    private var viewId: String = ""
    private var mcClass: String = ""
    private var parentId: String? = ""
    private var startPositionInSeconds: Long = 0

    private var isPlayedFromTheBackStack = false

    private var nextConceptData: VideoPageMicroConcept? = null
    private var averageVideoTime: Long = 0
    private var minWatchTime: Long = 0
    private var isPlaylistAdded = false

    private var referredStudentId: String? = ""
    private val entityPosition = 1

    private var titleIndex: String = ""
    private var descriptionIndex: String = ""
    private var lockUnlockLogs: String? = null
    private var analysisData: AnalysisData? = null
    private var similarCount = 1

    private var isFromTopicBooster: Boolean = false

    private var pageStack = Stack<String>()

    private var playFromAppIndexingWithoutOnBoarding: Boolean = false

    private var downloadUrl: String? = null
    private var preLoadVideoData: VideoResource? = null

    private var isVideoImageSummaryAvailable: Boolean = false
    private var isAppIndexingEventSent = false

    private var isFromSmartContent: Boolean = false

    private var videoPlayerManager: VideoPlayerManager? = null

    private var viewAnswerData: ViewAnswerData? = null
    private var wasMovedToLiveClass = false
    private lateinit var userSelectedExamsList: List<String>

    private var isNcertExperimentEnabled: Boolean? = false
    private var isNcertBookBottomSheetShown: Boolean = false
    private var nextNcertVideoPlayed: Boolean? = false

    private var premiumVideoBlockedData: PremiumVideoBlockedData? = null
    private var premiumVideoOffset: Long? = null
    private var openAnsweredDoubtWithCommentId: String? = null

    private var ncertBooksBottomSheet: NcertBooksBottomSheetFragment? = null

    // DNR region start
    private var shouldShowDnrRewardPopupForNonSrp: Boolean? = true
    private var shouldShowDnrRewardPopupForSrp: Boolean? = true
    private lateinit var dnrRewardViewModel: DnrRewardViewModel
    // DNR region end

    private var mPipModeLastEnterTimeMillis: Long = -1

    private val mPictureInPictureParamsBuilder: PictureInPictureParams.Builder? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PictureInPictureParams.Builder()
        } else {
            null
        }
    }

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
                        videoPageViewModel.sendEvent(
                            EventConstants.PIP_MODE_PLAY,
                            ignoreSnowplow = true
                        )
                    }
                    CONTROL_TYPE_PAUSE -> {
                        videoPlayerManager?.pauseExoPlayer()
                        videoPageViewModel.sendEvent(
                            EventConstants.PIP_MODE_PAUSE,
                            ignoreSnowplow = true
                        )
                    }
                    CONTROL_TYPE_NEXT -> {
                        playNextVideo()
                        videoPageViewModel.sendEvent(
                            EventConstants.PIP_MODE_NEXT,
                            ignoreSnowplow = true
                        )
                    }
                    CONTROL_TYPE_PREVIOUS -> {
                        videoPageViewModel.getPreviousVideo(page)
                        videoPageViewModel.sendEvent(
                            EventConstants.PIP_MODE_NEXT,
                            ignoreSnowplow = true
                        )
                    }
                }
            }
        }
    }

    private val mPipModeExitListener: PipModeExitListener by lazy {
        object : PipModeExitListener() {
            override fun onPipExpanded() {
                videoPageViewModel.sendEvent(
                    EventConstants.PIP_MODE_EXPANDED,
                    ignoreSnowplow = true
                )
            }

            override fun onPipClosed() {
                videoPageViewModel.sendEvent(EventConstants.PIP_MODE_CLOSED, ignoreSnowplow = true)
                // Pause here as by lifecycle it takes some time to pause player
                videoPlayerManager?.pauseExoPlayer()
            }
        }.also {
            lifecycle.addObserver(it)
        }
    }

    private val labelPlay: String by lazy { getString(R.string.exo_controls_play_description) }
    private val labelPause: String by lazy { getString(R.string.exo_controls_pause_description) }
    private val labelNext: String by lazy { getString(R.string.exo_controls_next_description) }
//    private val labelPrevious: String by lazy { getString(R.string.exo_controls_previous_description) }

    private val mShowNextButtonInPipMode: Boolean
        get() = videoPageViewModel.firstSimilarPipPlayableVideo.value != null

//    private val mShowPreviousButtonInPipMode: Boolean
//        get() = similarCount > 1

    private var mPipCloseDisposable: Disposable? = null

    private var mLiveClassObserverDisposable: Disposable? = null

    private var mPdfBannerVersion: Int? = null
    private var mPdfBannerData: PdfBannerData? = null
    private var mPdfBannerProgressBarAnimator: Animator? = null
    private val mPdfBannerClickListener: () -> Unit by lazy {
        {
            val pdfUrl = videoPageViewModel.pdfUrlDataLiveData.value?.url.orEmpty()
            PdfViewerActivity.previewPdfFromTheUrl(
                context = this,
                url = pdfUrl,
                questionId = questionId,
                showDownloadButton = true,
                source = Constants.SRP_PDF
            )
        }
    }
    private var mCanShowPdfDownloadBannerAfterVideo: Boolean = false

    private var shouldShowVideoPlaylistBottomSheet: Boolean = false
    private var updateSimilarVideoFragment: Boolean = true
    private var videoStartTime: Long = 0L

    private val playlistBottomSheetBehavior: BottomSheetBehavior<View> by lazy {
        BottomSheetBehavior.from(binding.bottomSheetVideoPlaylist.root as View).apply {
            binding.extraViewOverlay.show()
            expandedOffset = (getScreenWidth() * (9f / 16)).toInt()
            halfExpandedRatio =
                (peekHeight.toFloat() / binding.rootLayout.height).coerceIn(.001F, .999F)
        }
    }

    private val bottomSheetPlaylistAdapter: ResourcePlaylistAdapter by lazy {
        ResourcePlaylistAdapter(
            fm = supportFragmentManager,
            page = page,
            actionPerformer = this,
            isFromVideoTag = false,
            source = TAG
        )
    }

    private var chapter: String? = null
    private var isDoubtFeedBannerApiCalled: Boolean = false

    companion object {
        //region PIP controls constants

        /** Intent action for media controls from Picture-in-Picture mode.  */
        private const val ACTION_MEDIA_CONTROL = "media_control"

        private const val EXTRA_CONTROL_TYPE = "control_type"

        private const val REQUEST_PLAY = 1
        private const val REQUEST_PAUSE = 2
        private const val REQUEST_NEXT = 3

        private const val CONTROL_TYPE_PLAY = 1
        private const val CONTROL_TYPE_PAUSE = 2
        private const val CONTROL_TYPE_NEXT = 3
        private const val CONTROL_TYPE_PREVIOUS = 4
        //endregion

        private const val PDF_BANNER_AFTER_VIDEO = 2

        private const val INTENT_EXTRA_QUESTION_ID = "question_id"
        private const val INTENT_EXTRA_PLAYLIST_ID = "playlist_id"
        private const val INTENT_EXTRA_MC_ID = "mc_id"
        private const val INTENT_EXTRA_PAGE = "page"
        private const val INTENT_EXTRA_MC_CLASS = "mc_class"
        private const val INTENT_EXTRA_IS_MICRO_CONCEPT = "is_micro_concept"
        private const val INTENT_EXTRA_REFERRED_STUDENT_ID = "referred_student_id"
        private const val INTENT_EXTRA_PARENT_ID = "parent_id"
        private const val INTENT_EXTRA_FROM_NOTIFICATION_VIDEO = "from_notification_video"
        private const val INTENT_EXTRA_START_POSITION = "start_position"
        private const val INTENT_EXTRA_YOUTUBE_ID = "youtube_id"
        private const val INTENT_EXTRA_THUMBNAIL_URL = "thumbnail_url"
        private const val INTENT_EXTRA_SOURCE = "source"
        private const val INTENT_VIDEO_DATA = "video_data"
        private const val INTENT_EXTRA_OCR_TEXT = "ocr_text"
        private const val FEATURE_TYPE = "video"
        private const val ROTATION_DELAY = 500L
        private const val NCERT_EXPERIMENT_VIDEO_PAGE = "NCERT_EXPERIMENT_VIDEO_PAGE"
        private const val INTENT_EXTRA_IS_FULLSCREEN = "IS_FULLSCREEN"
        private const val OPEN_ANSWERED_DOUBT_COMMENT_ID = "open_answered_doubt_comment_id"
        const val EVENT_TAG_VIDEO_ICON_CLICK = "ReferQA_video_icon_click"

        const val TAG = "VideoPageActivity"
        private const val VIDEO_PLAYLIST_BOTTOM_SHEET = "VideoPagePlaylist"
        const val TAG_PIP = TAG + "_PIP"

        private const val DEFAULT_SRP_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD = 30000L
        private const val DEFAULT_NON_SRP_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD = 60000L

        fun startActivity(
            context: Context, questionId: String, playlistId: String? = "",
            mcId: String? = "", page: String, mcClass: String? = "",
            isMicroConcept: Boolean? = false, referredStudentId: String? = "",
            parentId: String? = "", fromNotificationVideo: Boolean? = false,
            ocr: String? = null, youtube_id: String? = null,
            startPositionInSeconds: Long = 0,
            thumbnailUrl: String = "",
            source: String = "",
            preLoadVideoData: VideoResource? = null,
            isFullScreen: Boolean = false,
            openAnsweredDoubtWithCommentId: String? = null,
            parentPage: String? = null,
            createNewInstance: Boolean = false
        ): Intent {
            return Intent(context, VideoPageActivity::class.java).apply {
                putExtra(INTENT_EXTRA_QUESTION_ID, questionId)
                putExtra(INTENT_EXTRA_PLAYLIST_ID, playlistId)
                putExtra(INTENT_EXTRA_MC_ID, mcId)
                putExtra(INTENT_EXTRA_PAGE, page)
                putExtra(INTENT_EXTRA_MC_CLASS, mcClass)
                putExtra(INTENT_EXTRA_IS_MICRO_CONCEPT, isMicroConcept)
                putExtra(INTENT_EXTRA_REFERRED_STUDENT_ID, referredStudentId)
                putExtra(INTENT_EXTRA_PARENT_ID, parentId)
                putExtra(INTENT_EXTRA_FROM_NOTIFICATION_VIDEO, fromNotificationVideo)
                putExtra(INTENT_EXTRA_START_POSITION, startPositionInSeconds)
                putExtra(INTENT_VIDEO_DATA, preLoadVideoData)
                putExtra(INTENT_EXTRA_YOUTUBE_ID, youtube_id)
                putExtra(INTENT_EXTRA_OCR_TEXT, ocr)
                putExtra(INTENT_EXTRA_THUMBNAIL_URL, thumbnailUrl)
                putExtra(INTENT_EXTRA_SOURCE, source)
                putExtra(INTENT_EXTRA_IS_FULLSCREEN, isFullScreen)
                putExtra(OPEN_ANSWERED_DOUBT_COMMENT_ID, openAnsweredDoubtWithCommentId)
                putExtra(Constants.PARENT_PAGE, parentPage)
                if (!createNewInstance) {
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        Glide.get(this).clearMemory()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        statusbarColor(this, R.color.white_50)
        requestFullScreen()
        setContentView(binding.root)

        videoPlayerManager = VideoPlayerManager(
            supportFragmentManager,
            this, R.id.videoContainer, playerTypeOrMediaTypeChangedListener, null
        )
        init()
        Utils.saveIsEmulatorAndSafetyNetResponseToPref()
        Utils.removeTasksWithAnyOfGivenActivitiesAsRoot(taskId, *Utils.pipSupportedActivities)
        // Invalidate cached data as there are home page widgets that change data depending on what videos user has watched
        defaultPrefs().edit {
            putBoolean(Constants.HOME_PAGE_DATA_INVALIDATED, true)
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private fun init() {
        binding.bottomNavigationView.menu.setGroupCheckable(0, false, true)
        videoPageViewModel =
            ViewModelProvider(this, viewModelFactory).get(VideoPageViewModel::class.java)
        ncertViewModel =
            ViewModelProvider(this, viewModelFactory).get(NcertSimilarViewModel::class.java)
        // DNR region start
        dnrRewardViewModel = viewModelProvider(viewModelFactory)
        // DNR region end
        setupObservers()
        Sensey.getInstance().init(this)
        getDataIntent()
        ncertViewModel.page = page
        setListeners()
        sendVideoWatchedBranchEvents()
        sendEvent(EventConstants.EVENT_NAME_VIDEO_PAGE)
        if (!userPreference.getUserHasWatchedVideo()) {
            userPreference.putUserHasWatchedVideo(true)
            sendEvent(EventConstants.EVENT_USER_HAS_WATCHED_VIDEO)
        }
        setLibraryTabText()
        PopularCourseWidget.isClicked = false
    }

    private fun setLibraryTabText() {
        lifecycleScope.launchWhenStarted {
            val bottomNavIconsData = dataStore.bottomNavigationIconsData.firstOrNull()
            if (bottomNavIconsData.isNullOrEmpty()) {
                val bottomLibraryText = Utils.getLibraryBottomText(this@VideoPageActivity)
                if (!bottomLibraryText.isNullOrBlank()) {
                    val menu = binding.bottomNavigationView.menu
                    menu.findItem(R.id.libraryFragment).title = bottomLibraryText
                }
            }
        }
    }

    private fun getDataIntent() {
        extractIntentData()
        if (this@VideoPageActivity.isFullScreen) {
            onFullscreenRequested()
        }
        if (preLoadVideoData != null) {
            videoPlayerManager?.setAndInitPlayFromResource(
                "",
                listOf(preLoadVideoData!!),
                viewId = "",
                startPositionInSeconds = startPositionInSeconds,
                isPlayedFromTheBackStack = false,
                page = page,
                aspectRatio = VideoFragment.DEFAULT_ASPECT_RATIO,
                eventDetail = null,
                startTime = null,
                showFullScreen = true,
                ocrText = ocr,
                lockUnlockLogs = lockUnlockLogs,
                analysisData = analysisData
            )
        }

        if (intent.getBooleanExtra(Constants.FROM_NOTIFICATION_VIDEO, false)) {

            if (!intent.getStringExtra(Constants.VIDEO_FIREBASE_EVENT_TAG).isNullOrBlank()) {
                sendEvent(EventConstants.NOTIFICATION_OPEN_TAGGED + intent.getStringExtra(Constants.VIDEO_FIREBASE_EVENT_TAG))
            }
            videoPageViewModel.publishPlayVideoClickEvent(
                intent.getStringExtra(Constants.QUESTION_ID)
                    ?: "", TAG
            )
            sendEvent(EventConstants.NOTIFICATION_OPEN)
            (this.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addEventParameter(
                    Constants.QUESTION_ID,
                    intent.getStringExtra(Constants.QUESTION_ID).orEmpty()
                )
                .addEventParameter(Constants.PAGE, intent.getStringExtra(Constants.PAGE).orEmpty())
                .addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
                .track()
        }

        if (defaultPrefs(this).getString(Constants.STUDENT_LOGIN, "false") != "true"
            || !defaultPrefs(this).getBoolean(Constants.ONBOARDING_COMPLETED, false)
        ) {
            val videoId =
                intent?.data?.path?.split("-")?.get(intent?.data?.path?.split("-")!!.size - 1)
                    .orEmpty()
            if (videoId.isNotEmpty()) {
                CoreApplication.pendingDeeplink = "doubtnutapp://video?qid=$videoId"
            }
            startActivity(
                Intent(
                    this,
                    SplashActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        } else {
            handleIntent(intent)
            playFromAppIndexingWithoutOnBoarding = false
        }

        if (intent.getStringExtra(Constants.SOURCE) == Constants.SRP_PDF) {
            videoPageViewModel.sendEvent(
                EventConstants.VIDEO_PLAYED_FROM_SRP_PDF, hashMapOf(
                    Constants.QUESTION_ID to questionId.orEmpty()
                )
            )
        }
        sendMoEngageScreenTrackEvent()
    }

    private fun extractIntentData() {
        this@VideoPageActivity.parentId = if (!intent.getStringExtra(INTENT_EXTRA_PARENT_ID)
                .isNullOrBlank()
        ) intent.getStringExtra(INTENT_EXTRA_PARENT_ID) else "0"
        this@VideoPageActivity.questionId =
            intent.getStringExtra(INTENT_EXTRA_QUESTION_ID).orDefaultValue()
        this@VideoPageActivity.page = intent.getStringExtra(INTENT_EXTRA_PAGE).orDefaultValue()
        this@VideoPageActivity.playlistId = intent.getStringExtra(INTENT_EXTRA_PLAYLIST_ID)
        this@VideoPageActivity.isMicroConcept =
            intent.getBooleanExtra(INTENT_EXTRA_IS_MICRO_CONCEPT, false)
        this@VideoPageActivity.mcId = intent.getStringExtra(INTENT_EXTRA_MC_ID)
        this@VideoPageActivity.mcClass = intent.getStringExtra(INTENT_EXTRA_MC_CLASS) ?: ""
        this@VideoPageActivity.referredStudentId =
            intent.getStringExtra(INTENT_EXTRA_REFERRED_STUDENT_ID)
        this@VideoPageActivity.startPositionInSeconds =
            intent.getLongExtra(INTENT_EXTRA_START_POSITION, 0)
        this@VideoPageActivity.thumbnailImage =
            intent.getStringExtra(INTENT_EXTRA_THUMBNAIL_URL).orDefaultValue()
        this@VideoPageActivity.preLoadVideoData =
            intent.getParcelableExtra(INTENT_VIDEO_DATA) as VideoResource?
        this@VideoPageActivity.ocr = intent.getStringExtra(INTENT_EXTRA_OCR_TEXT)
        this@VideoPageActivity.youtubeId = intent.getStringExtra(INTENT_EXTRA_YOUTUBE_ID)
        this@VideoPageActivity.isFullScreen =
            intent.getBooleanExtra(INTENT_EXTRA_IS_FULLSCREEN, false)
        this@VideoPageActivity.openAnsweredDoubtWithCommentId =
            intent.getStringExtra(OPEN_ANSWERED_DOUBT_COMMENT_ID)
        this@VideoPageActivity.parentPage = intent.getStringExtra(Constants.PARENT_PAGE)
    }

    private fun sendMoEngageScreenTrackEvent() {
        analyticsPublisher.publishMoEngageEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NAME_VIDEO_PAGE,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.QUESTION_ID, questionId ?: "")
                })
        )
    }

    fun setListeners() {

        binding.btnLike.setOnClickListener {
            videoPageViewModel.likeButtonClicked(
                videoTitle, questionId!!, answerId,
                (videoPlayerManager?.currentPlayerPosition ?: 0).toLong(), page, viewId
            )
            sendEventByQid(EventConstants.EVENT_NAME_LIKE_CLICK, questionId.toString())
        }

        binding.btnDislike.setOnClickListener {
            if (isDisLiked) {
                toast(getString(R.string.removed_from_dislike), Toast.LENGTH_LONG)
                videoPageViewModel.disLikeButtonClicked(
                    videoTitle, questionId!!, answerId,
                    (videoPlayerManager?.currentPlayerPosition ?: 0).toLong(), page, "", viewId
                )
                sendEventByQid(
                    EventConstants.EVENT_NAME_REMOVE_FROM_DISLIKE_CLICK,
                    questionId.toString()
                )
            } else {
                videoPageViewModel.disLikeButtonClicked(
                    videoTitle, questionId!!, answerId,
                    (videoPlayerManager?.currentPlayerPosition ?: 0).toLong(), page, "", viewId
                )
                val dialog = VideoDislikeFeedbackDialog.newInstance(
                    videoTitle, questionId!!, answerId,
                    (videoPlayerManager?.currentPlayerPosition
                        ?: 0).toLong(), false, page, Constants.VIDEO_DISLIKE_SCREEN_VIDEO, viewId
                )
                dialog.show(supportFragmentManager, "DislikeFeedbackDialog")
                sendEventByQid(EventConstants.EVENT_NAME_DISLIKE_CLICK, questionId.toString())
            }
            isDisLiked = !isDisLiked
        }

        binding.btnComment.setOnClickListener {
            if (!entityId.isNullOrBlank() && !entityType.isNullOrBlank()) {
                onCommentButtonClicked(entityId!!, entityType!!, entityPosition, batchId)
                videoPageViewModel.onVideoCommentEvent(questionId.toString())
                sendEventByQid(EventConstants.EVENT_NAME_COMMENT_ICON_CLICK, questionId.toString())
            }

        }

        binding.btnAddPlaylist.setOnClickListener {
            questionId?.let { it1 ->
                addToPlayList(it1)
                sendEventByQid(EventConstants.EVENT_NAME_ADD_VIDEO_CLICK, it1)
            }
        }

        binding.btnExpand.setOnClickListener {
            collapseExpandView()
            sendEvent(EventConstants.EVENT_NAME_QUESTION_DETAILS_COLLAPSE_EXPAND)

        }

        binding.ivBackFromVideo.setOnClickListener {
            onBackPressed()
            sendEvent(EventConstants.EVENT_NAME_BACK_FROM_VIDEO_VIEW)
        }


        lifecycleScope.launchWhenStarted {
            val bottomNavIconsDataJson = dataStore.bottomNavigationIconsData.firstOrNull() ?: ""
            val bottomNavigationTabsData = Gson().fromJson<BottomNavigationTabsData>(
                bottomNavIconsDataJson,
                BottomNavigationTabsData::class.java
            )
            val isBackendIconsAvailableForBottomNav =
                Utils.isBottomNavigationIconsApiDataAvailable(bottomNavIconsDataJson)

            binding.bottomNavigationView.setOnNavigationItemSelectedListener {
                closeConvivaSession()

                when (it.itemId) {
                    R.id.homeFragment -> {
                        if (isBackendIconsAvailableForBottomNav) {
                            deeplinkAction.performAction(
                                this@VideoPageActivity,
                                bottomNavigationTabsData.tab1?.deeplink
                            )
                            Utils.publishBottomNavTabClickEvent(
                                analyticsPublisher,
                                bottomNavigationTabsData.tab1?.name.orEmpty(),
                                "1",
                            )
                        } else {
                            startActivity(
                                Intent(
                                    this@VideoPageActivity,
                                    MainActivity::class.java
                                ).apply {
                                    flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                })
                        }


                    }

                    R.id.libraryFragment -> {
                        if (isBackendIconsAvailableForBottomNav) {

                            deeplinkAction.performAction(
                                this@VideoPageActivity,
                                bottomNavigationTabsData.tab2?.deeplink
                            )

                            Utils.publishBottomNavTabClickEvent(
                                analyticsPublisher,
                                bottomNavigationTabsData.tab2?.name.orEmpty(),
                                "2",
                            )
                        } else {
                            startActivity(
                                Intent(
                                    this@VideoPageActivity,
                                    MainActivity::class.java
                                ).apply {
                                    action = Constants.OPEN_LIBRARY_FROM_BOTTOM
                                })
                        }


                    }

                    R.id.forumFragment -> {
                        if (isBackendIconsAvailableForBottomNav) {
                            deeplinkAction.performAction(
                                this@VideoPageActivity,
                                bottomNavigationTabsData.tab3?.deeplink
                            )

                            Utils.publishBottomNavTabClickEvent(
                                analyticsPublisher,
                                bottomNavigationTabsData.tab3?.name.orEmpty(),
                                "3"
                            )
                        } else {
                            startActivity(
                                Intent(
                                    this@VideoPageActivity,
                                    MainActivity::class.java
                                ).apply {
                                    action = Constants.OPEN_FORUM_FROM_BOTTOM
                                })
                        }


                    }

                    R.id.userProfileFragment -> {
                        if (isBackendIconsAvailableForBottomNav) {

                            deeplinkAction.performAction(
                                this@VideoPageActivity,
                                bottomNavigationTabsData.tab4?.deeplink
                            )

                            Utils.publishBottomNavTabClickEvent(
                                analyticsPublisher,
                                bottomNavigationTabsData.tab4?.name.orEmpty(),
                                "4",
                            )

                        } else {
                            startActivity(
                                Intent(
                                    this@VideoPageActivity,
                                    MainActivity::class.java
                                ).apply {
                                    action = Constants.OPEN_PROFILE_FROM_BOTTOM
                                })
                        }


                    }

                    else -> {
                        startActivity(Intent(this@VideoPageActivity, MainActivity::class.java))

                    }

                }

                if (isTaskRoot) {
                    finishAndRemoveTask()
                }

                return@setOnNavigationItemSelectedListener true

            }

            // If video page switched to PiP mode, that will be the root of a new task,
            // so we'll finish that task

        }

        binding.askQuestionCameraButton.setOnClickListener {
            closeConvivaSession()
            videoPageViewModel.publishCameraButtonClickEvent(TAG)
            val intent = Intent(this, MainActivity::class.java)
            intent.action = Constants.NAVIGATE_CAMERA_SCREEN
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            this.startActivity(intent)
            sendEvent(EventConstants.EVENT_NAME_BACK_FROM_VIDEO_VIEW)
            sendEvent(EventConstants.EVENT_NAME_ASK_QUESTION_CAMERA_BUTTON)
            // If video page switched to PiP mode, that will be the root of a new task,
            // so we'll finish that task
            if (isTaskRoot) {
                finishAndRemoveTask()
            }
        }

        binding.layoutTopicText.setOnClickListener {
            if (binding.similarFragmentOverLap.visibility == View.GONE) {
                slideDown()
            } else {
                slideUp()
            }
        }

        binding.ivSearch.setOnClickListener {
            if (ncertBooksBottomSheet == null) {
                ncertBooksBottomSheet = NcertBooksBottomSheetFragment.newInstance(
                    getBottomSheetUiConfig()
                )
            }
            val similarBooks = ncertViewModel.bookWidgetData.value
            if (!similarBooks.isNullOrEmpty() && supportFragmentManager.findFragmentByTag(
                    NcertBooksBottomSheetFragment.TAG
                ) == null
            ) {
                ncertBooksBottomSheet?.show(
                    supportFragmentManager,
                    NcertBooksBottomSheetFragment.TAG
                )
                videoPageViewModel.sendEvent(
                    EventConstants.NCERT_BOOK_BOTTOM_SHEET_VISIBLE,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, "search_icon")
                    }, ignoreSnowplow = true
                )
            }
        }
    }

    /**
     * show Refer and Earn animation button in buttons view
     * when showReferAndEarn key value true
     */
    private fun showReferAndEarnButtonBelowVideo(showReferAndEarn: Boolean?) {
        if (showReferAndEarn == null || !showReferAndEarn) {
            return
        }
        binding.btnAddPlaylist.visibility = View.GONE
        binding.tvSaveVideo.visibility = View.GONE
        binding.btnReferAndEarn.visibility = View.VISIBLE
        binding.tvReferAndEarn.visibility = View.VISIBLE
        binding.btnReferAndEarn.updateLayoutParams<ConstraintLayout.LayoutParams> {
            endToStart = binding.btnLike.id
            startToStart = binding.layoutButtons.id
        }

        binding.btnLike.updateLayoutParams<ConstraintLayout.LayoutParams> {
            startToEnd = binding.btnReferAndEarn.id
            startToStart = ConstraintLayout.LayoutParams.UNSET
        }

        binding.btnReferAndEarn.setOnClickListener {
            deeplinkAction.performAction(this@VideoPageActivity, "doubtnutapp://refer_and_earn")
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EVENT_TAG_VIDEO_ICON_CLICK,
                )
            )
        }

        lifecycleScope.launchWhenStarted {
            lottieAnimDataStore.videoPageReferAndEarnAnimationUrl
                .firstOrNull()?.let {
                    btnReferAndEarn.apply {
                        applyAnimationFromUrl(it)
                    }
                }

        }


    }

    private fun shareVideoOnStudyGroup(ocrText: String) {
        val questionData = QuestionToShare(
            thumbnail = thumbnailImage,
            ocrText = ocrText,
            questionId = questionId.orEmpty()
        )
        SgShareActivity.getStartIntent(this, questionData).apply {
            startActivity(this)
        }
    }

    private fun shareVideoOnWhatsapp(shareMessage: String) {
        val sharingMessage = if (shareMessage.isBlank()) {
            getString(R.string.video_share_message)
        } else {
            shareMessage
        }
        if (FeaturesManager.isFeatureEnabled(
                this@VideoPageActivity,
                Features.VIDEO_CONTENT_SHARING
            ) && !downloadUrl.isNullOrEmpty()
        )
            DialogShareVideo.getInstance(downloadUrl!!, sharingMessage, {
                showAllowPermissionSnack()
            }) {
                videoPageViewModel.shareOnWhatsApp(
                    FEATURE_TYPE,
                    thumbnailImage,
                    getControlParams(),
                    questionId!!,
                    sharingMessage
                )
            }.show(supportFragmentManager, DialogShareVideo.TAG)
        else
            videoPageViewModel.shareOnWhatsApp(
                FEATURE_TYPE,
                thumbnailImage,
                getControlParams(),
                questionId!!,
                sharingMessage
            )
        videoPageViewModel.onVideoShareEvent(questionId.toString())
        sendEventByQid(EventConstants.EVENT_NAME_SHARE_CLICK, questionId.toString())
    }

    private fun sendVideoWatchedBranchEvents() {
        val hasUserGivenCameraPermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val timeDiff = Utils.getInstallationDays(baseContext)
        val videoWatchedCount = userPreference.getVideoWatchedCount() + 1
        userPreference.putVideoWatchedCount(videoWatchedCount)

        lifecycleScope.launchWhenStarted {
            val isNewUser = defaultDataStore.get().isNewUser.firstOrNull() == true
            if (isNewUser && hasUserGivenCameraPermission) {
                if (timeDiff <= 2 && videoWatchedCount == 3) {
                    userPreference.putThreeVideosWatchedInTwoDays(true)
                }
                if (timeDiff <= 3) {
                    if (videoWatchedCount == 3) {
//                        analyticsPublisher.publishBranchIoEvent(
//                            AnalyticsEvent(
//                                EventConstants.DV3D3,
//                                hashMapOf()
//                            )
//                        )
                    } else if (videoWatchedCount == 4) {
//                        analyticsPublisher.publishBranchIoEvent(
//                            AnalyticsEvent(
//                                EventConstants.DV3D3,
//                                hashMapOf()
//                            )
//                        )
                    } else if (videoWatchedCount == 5 || videoWatchedCount == 6) {
//                        analyticsPublisher.publishBranchIoEvent(
//                            AnalyticsEvent(
//                                EventConstants.DV3D3,
//                                hashMapOf()
//                            )
//                        )
                    } else if (videoWatchedCount >= 7) {
//                        analyticsPublisher.publishBranchIoEvent(
//                            AnalyticsEvent(
//                                EventConstants.DV3D3,
//                                hashMapOf()
//                            )
//                        )
                    }
                }
                when (timeDiff) {
                    0 -> {
                        userPreference.putVideoWatchedOnDayZero(true)
                    }
                    1 -> {
                        if (userPreference.getVideoWatchedOnDayZero() && (userPreference.getLastDayBranchEventSend() != 1)) {
                            userPreference.putLastDayBranchEventSend(timeDiff)
                        }
                    }
                    else -> {
                    }
                }
            }

            if (isNewUser && timeDiff == 0 && videoWatchedCount == 7) {
                analyticsPublisher.publishBranchIoEvent(
                    AnalyticsEvent(
                        EventConstants.D0V7,
                        hashMapOf()
                    )
                )
                sendEvent(EventConstants.D0V7)
            }

            if (timeDiff in 2..4) {
                val videoPageEventSentDay = UserUtil.getVideoPageEventSentDay()
                if (videoPageEventSentDay < timeDiff) {
                    val eventPrefix = if (timeDiff == 3) {
                        EventConstants.VV_USER_D3
                    } else {
                        EventConstants.VV_USER
                    }
                    analyticsPublisher.publishBranchIoEvent(
                        AnalyticsEvent(
                            eventPrefix + "_d" + timeDiff,
                            hashMapOf()
                        )
                    )
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            eventPrefix + "_d" + timeDiff,
                            hashMapOf()
                        )
                    )
                    UserUtil.putVideoPageEventSentDay(timeDiff)
                }
            }
        }
    }

    private fun fetchAnswerDetail(addToStack: Boolean = true) {
        videoPlayerManager?.resetVideo()
        videoPageViewModel.viewVideo(
            questionId = questionId!!,
            playListId = this.playlistId,
            mcId = this.mcId,
            page = page,
            mcClass = this.mcClass,
            referredStudentId = this.referredStudentId,
            parentId = this.parentId,
            isFromTopicBooster = this.isFromTopicBooster,
            isFromSmartContent = isFromSmartContent,
            youtube_id = youtubeId,
            ocr = ocr,
            addToStack = addToStack,
            isVideoInPipMode = isInPipMode,
            isFilter = true,
            parentPage = parentPage
        )
        isFromSmartContent = false
    }

    private fun showAllowPermissionSnack() {
        Snackbar.make(
            this.findViewById(android.R.id.content),
            R.string.storage_permission_blocked_for_video,
            Snackbar.LENGTH_LONG
        )
            .setAction("Allow") {
                startActivity(
                    Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + BuildConfig.APPLICATION_ID)
                    )
                )
            }.show()
    }

    private fun collapseExpandView() {
        if (binding.mathView.height == ceil(
                this.resources.getDimension(R.dimen.video_question_minimum_height).toDouble()
            ).toInt()
            || binding.mathView.height == floor(
                this.resources.getDimension(R.dimen.video_question_minimum_height).toDouble()
            ).toInt()
        ) {
            wrapMathView()
        } else {
            minimizeMathView()
        }
    }

    private fun wrapMathView() {
        val width = binding.mathView.width
        binding.mathView.layoutParams =
            ConstraintLayout.LayoutParams(width, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        binding.btnExpand.setImageResource(R.drawable.ic_expand_less)
    }

    private fun minimizeMathView() {
        val width = binding.mathView.width
        binding.mathView.layoutParams = ConstraintLayout.LayoutParams(
            width,
            this.resources.getDimension(R.dimen.video_question_minimum_height).toInt()
        )
        binding.btnExpand.setImageResource(R.drawable.ic_expand_more)
    }

    private fun setupObservers() {
        rxObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is VipStateEvent) {
                if (event.state) {
                    recreate()
                } else {
                    if (PopularCourseWidget.isClicked) {
                        return@subscribe
                    }
                    onBackPressed()
                }
            } else if (event is PauseVideoPlayer) {
                videoPlayerManager?.pauseExoPlayer()
            }
        }

        videoPageViewModel.getVideoLiveData.observeL(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        videoPageViewModel.emptyStackLiveData.observe(this) {
            emptyVideoStack()
        }

        videoPageViewModel.likeCountLiveData.observe(this) {
            val (likeCount, isSelected) = it
            likeVideo(likeCount, isSelected)
        }

        videoPageViewModel.disLikeCountLiveData.observe(this) {
            val (disLikeCount, isSelected) = it
            disLikeVideo(disLikeCount, isSelected)
        }

        videoPageViewModel.whatsAppShareableData.observe(this) {
            it?.getContentIfNotHandled()?.let { event ->
                val (deepLink, imagePath, sharingMessage) = event
                deepLink?.let { shareOnWhatsApp(deepLink, imagePath, sharingMessage) }
                    ?: showBranchLinkError()
            }
        }

        videoPageViewModel.showProgressLiveData.observe(
            this,
            Observer(this::updateProgressBarState)
        )

        videoPageViewModel.showWhatsappProgressLiveData.observe(
            this,
            Observer(this::updateProgressBarState)
        )

        videoPageViewModel.shareCountLiveData.observe(this) {
            shareCount(it)
        }

        videoPageViewModel.commentsCountLiveData.observe(this) {
            binding.tvCommentCount.text = it
        }

        videoPageViewModel.onAddToWatchLater.observe(this, SingleEventObserver {
            onWatchLaterSubmit(it)
        })

        videoPageViewModel.navigateLiveData.observe(this) {
            val navigationData = it.getContentIfNotHandled()
            if (navigationData != null) {
                val args: Bundle? = navigationData.hashMap?.toBundle()
                screenNavigator.startActivityFromActivity(this, navigationData.screen, args)
            }
        }

        videoPageViewModel.firstSimilarPipPlayableVideo.observe(this) {
            updatePictureInPictureActions()
        }

        videoPageViewModel.pdfUrlDataLiveData.observe(this) {
            // Call it here in case data is loaded after the condition to show banner is met
            if (it == null) {
                videoPageViewModel.sendEvent(
                    EventConstants.PDF_URL_NULL, hashMapOf(
                        Constants.QUESTION_ID to questionId.orEmpty()
                    ), ignoreSnowplow = true
                )
            }
        }

        ncertViewModel.playingVideoResource.observe(this) {
            // close conviva session as next video is going to play
            videoPlayerManager?.closeConvivaSession()
            val ncertData = it.second
            videoPlayerManager?.setAndInitPlayFromResource(
                ncertData.questionId,
                ncertData.resources.map { ncertViewModel.getVideoResource(it) },
                viewId = "",
                startPositionInSeconds = 0,
                isPlayedFromTheBackStack = false,
                page = page,
                aspectRatio = VideoFragment.DEFAULT_ASPECT_RATIO,
                eventDetail = null,
                startTime = null,
                controllerAutoShow = isInPipMode.not(),
                showEnterPipModeButton = canEnterPipMode(checkPlayer = false),
                isInPipMode = isInPipMode,
                showFullScreen = true,
                ncertExperiment = true,
                ocrText = ocr,
                lockUnlockLogs = lockUnlockLogs,
                imaAdTagResource = ncertData.imaAdTagResource?.map { imaAdTagResource ->
                    ImaAdTagResourceData(
                        adTag = imaAdTagResource.adTag,
                        adTimeout = imaAdTagResource.adTimeout
                    )
                }
            )
            setQuestionOcr(ncertData.questionId, ncertData.ocrText, ncertData.question)
            binding.tvVideoTitle.text = ncertData.videoTitle
        }

        ncertViewModel.refreshVideoPage.observe(this) { deeplink ->
            deeplinkAction.performAction(this, deeplink)
        }

        videoPageViewModel.videoPlaylistLiveData.observe(this) {
            if (it != null) {
                setupPlaylistBottomSheet(it)
            }
            viewAnswerData?.let { data ->
                addFragment(createSimilarVideoFragment(data))
            }
        }

        videoPageViewModel.autoplayVideoBottomBarLiveData.observe(this) {
            if (it != null) {
                updateNextVideoBottomBar(it)
            } else {
                binding.nextVideoBottomBar.hide()
            }
        }

        videoPageViewModel.doubtFeedBannerLiveData.observe(this) {
            if (it.isShow) {
                setupDoubtFeedBanner(it)
            }
        }

        ncertViewModel.bookWidgetData.observe(this) { bookWidgets ->
            binding.ivSearch.visibility =
                if (!bookWidgets.isNullOrEmpty()) View.VISIBLE else View.GONE
        }

        dnrRewardViewModel.dnrRewardLiveData.observeEvent(this) {
            showDnrReward(it)
        }
    }

    private fun showDnrReward(dnrReward: DnrReward) {
        dnrRewardViewModel.checkRewardPopupToBeShown(dnrReward)
        dnrRewardViewModel.dnrRewardPopupLiveData.observeEvent(this) { rewardPopupType ->
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
        }
    }

//    private fun openWebView() {
//        val url = "https://doubtnut.com/app-live-class-videos/" +
//                defaultPrefs().getString(Constants.XAUTH_HEADER_TOKEN, "") + "/" +
//                userPreference.getUserStudentId() + "/" +
//                questionId
//
//        startActivity(WebViewActivity.getIntent(this, url, ""))
//    }

    private fun onWatchLaterSubmit(id: String) {
        showSnackbar(
            R.string.video_saved_to_watch_later,
            R.string.change,
            Snackbar.LENGTH_LONG,
            id
        ) { idToPost ->
            videoPageViewModel.removeFromPlaylist(idToPost, "1")
            AddToPlaylistFragment.newInstance(idToPost)
                .show(supportFragmentManager, AddToPlaylistFragment.TAG)
        }
    }

    private fun likeVideo(likeCount: String, isSelected: Boolean) {
        binding.btnLike.isSelected = isSelected
        binding.tvLikeCount.text = likeCount

    }

    private fun disLikeVideo(disLikeCount: String, isSelected: Boolean) {
        binding.btnDislike.isSelected = isSelected
        binding.tvDisLikeCount.text = disLikeCount

    }

    private fun shareCount(count: String) {
        binding.tvShareCount.text = count
    }

    private fun emptyVideoStack() {
        onBackPressed()
    }

    private fun handleSecureFlag(blockScreenshot: Boolean?) {
        if (blockScreenshot == true) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    private fun onSuccess(viewAnswerData: ViewAnswerData) {
        if (viewAnswerData.resourceType == "text"
            && viewAnswerData.resources.isNullOrEmpty()
            && page == Constants.APP_INDEXING
        ) {
            val intent = TextSolutionActivity.startActivity(
                this, questionId = questionId.orEmpty(),
                playlistId = null, mcId = null, page = page,
                mcClass = null, isMicroConcept = null, referredStudentId = null,
                parentId = null, fromNotificationVideo = null,
                resourceType = "text", resourceData = null,
                ocrText = null
            )
            startActivity(intent)
            finish()
            return
        }
        videoStartTime = System.currentTimeMillis()
        shouldShowVideoPlaylistBottomSheet = viewAnswerData.isFilter
        if (showBottomSheetPlaylist()) {
            if (isInPipMode.not()) {
                if (binding.bottomSheetVideoPlaylist.root.isNotVisible) {
                    //This logic is needed to get same set of videos in bottom sheet every time in a session
                    //this is useful on backpress when we fetch bottom sheet videos
                    if (videoPageViewModel.videoPlaylistQuestionId == null) {
                        videoPageViewModel.videoPlaylistQuestionId = viewAnswerData.questionId
                    }
                    videoPageViewModel.videoPlaylistQuestionId?.let {
                        videoPageViewModel.getVideoPlaylist(it)
                    }
                }
            }
        } else {
            binding.bottomSheetVideoPlaylist.root.hide()
            binding.extraViewOverlay.alpha = 0f
            binding.nextVideoBottomBar.hide()
            if (isFullScreen.not() && isInPipMode.not()) {
                // For D0 User hide bottom nav
                if (videoPageViewModel.hideBottomNavigation == true) {
                    binding.bottomNavigationView.hide()
                } else {
                    binding.bottomNavigationView.show()
                }
                binding.askQuestionCameraButton.show()
            }
        }
        timerDisposable.clear()
        handleSecureFlag(viewAnswerData.blockScreenshot)
        sendMoEngageEvent(viewAnswerData)
        sendBranchVideoWatchEvent()
        binding.btnView.hide()
        if (viewAnswerData.isPremium && !viewAnswerData.isVip && viewAnswerData.premiumVideoOffset == null) {
            deeplinkAction.performAction(this, viewAnswerData.paymentDeeplink)
            return
        }
        if (viewAnswerData.premiumVideoOffset != null) {
            premiumVideoBlockedData = viewAnswerData.premiumVideoBlockedData
            premiumVideoOffset = viewAnswerData.premiumVideoOffset.toLong()
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
                videoPageViewModel.postEvent(hashMapOf<String, Any>().apply {
                    put(EventConstants.VIDEO_VIEW_ID, viewAnswerData.viewId)
                    put(
                        EventConstants.COURSE_ID, viewAnswerData.premiumVideoBlockedData?.courseId
                            ?: 0
                    )
                    put(EventConstants.PAID_USER, viewAnswerData.isPremium && viewAnswerData.isVip)
                    put(EventConstants.CTA_VIEWED, true)
                    put(EventConstants.CTA_CLICKED, 2)
                    put(EventConstants.VIEW_FROM, page)
                })
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PAID_CONTENT_SEARCH_EVENTS,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.VIDEO_VIEW_ID, viewAnswerData.viewId)
                            put(
                                EventConstants.COURSE_ID,
                                viewAnswerData.premiumVideoBlockedData?.courseId
                                    ?: 0
                            )
                            put(
                                EventConstants.PAID_USER,
                                viewAnswerData.isPremium && viewAnswerData.isVip
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

        if (viewAnswerData.bottomView == Constants.VIDEO_BOTTOM_VIEW_LIVE_CLASS) {
            wasMovedToLiveClass = true
            startActivity(
                LiveClassActivity.getStartIntent(
                    activity = this,
                    page = page,
                    viewAnswerData = viewAnswerData,
                    openAnsweredDoubtWithCommentId = if (this.questionId == viewAnswerData.questionId) {
                        openAnsweredDoubtWithCommentId
                    } else {
                        null
                    },
                    startPositionInSeconds = startPositionInSeconds
                )
            )
            startPositionInSeconds = 0
            openAnsweredDoubtWithCommentId = null
            mLiveClassObserverDisposable =
                DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
                    if (event is LiveClassVideoEvent) {
                        when (event.status) {
                            is LiveClassVideoActionReRequest -> {
                                fetchAnswerDetail(addToStack = false)
                                isPlayedFromTheBackStack = false
                                wasMovedToLiveClass = false
                            }
                            is LiveClassVideoActionRequestNew -> {
                                pageStack.push(page)
                                this.parentId = "0"
                                isPlayedFromTheBackStack = false
                                wasMovedToLiveClass = false
                                this@VideoPageActivity.questionId = event.status.questionId
                                this@VideoPageActivity.page = event.status.page
                                this@VideoPageActivity.playlistId = null
                                this@VideoPageActivity.isMicroConcept = false
                                this@VideoPageActivity.mcId = null
                                this@VideoPageActivity.referredStudentId = ""
                                this@VideoPageActivity.isFromTopicBooster = false
                                startPositionInSeconds = 0
                                fetchAnswerDetail()
                                videoPageViewModel.publishPlayVideoClickEvent(
                                    event.status.questionId,
                                    TAG
                                )
                                sendEventByQid(
                                    EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
                                    event.status.questionId
                                )
                                // Send this event to Branch
//                                BranchIOUtils.userCompletedAction(
//                                    EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//                                    JSONObject().apply {
//                                        put(EventConstants.QUESTION_ID, event.status.questionId)
//                                        put(EventConstants.SOURCE, TAG)
//                                    })
                                sendEventByQid(
                                    EventConstants.EVENT_NAME_PLAY_VIDEO_FROM_SIMILAR,
                                    event.status.questionId
                                )
                                similarCount += 1
                            }
                            is LiveClassVideoActionCancel -> {
                                onBackPressed()
                            }
                        }
                    }
                }
            return
        }

        changeVisibilityOfVideoActionButtons(page != Constants.PAGE_YT_ASK)
        handleShareVisibility(viewAnswerData.isShareable)
        minimizeMathView()
        thumbnailImage = viewAnswerData.thumbnailImage
        answerId = viewAnswerData.answerId
        videoTitle = viewAnswerData.videoName
        viewId = viewAnswerData.viewId
        entityId = viewAnswerData.videoEntityId
        batchId = viewAnswerData.batchId
        entityType = viewAnswerData.videoEntityType
        isPlaylistAdded = viewAnswerData.isPlaylistAdded
        isLiked = viewAnswerData.isLiked
        isDisLiked = viewAnswerData.isDisliked
        likeCount = viewAnswerData.likeCount
        disLikeCount = viewAnswerData.dislikesCount
        isMicroConcept = viewAnswerData.nextMicroConcept?.mcId != null
        averageVideoTime = viewAnswerData.averageVideoTime ?: 0
        minWatchTime = viewAnswerData.minWatchTime ?: 0
        downloadUrl = viewAnswerData.downloadUrl
        titleIndex = viewAnswerData.title
        descriptionIndex = viewAnswerData.description
        lockUnlockLogs = viewAnswerData.lockUnlockLogs
        analysisData = viewAnswerData.analysisData
        indexVideo(viewAnswerData.title, viewAnswerData.description, viewAnswerData.webUrl)
        startFireBaseUserAction()
        this.viewAnswerData = viewAnswerData
        if (preLoadVideoData != null) {
            preLoadVideoData = null
            startPositionInSeconds = videoPlayerManager?.currentPlayerPosition?.toLong() ?: 0L
        }
        videoPlayerManager?.setAndInitPlayFromResource(
            id = viewAnswerData.questionId,
            videoResourceList = viewAnswerData.resources,
            viewId = viewAnswerData.viewId,
            startPositionInSeconds = startPositionInSeconds,
            isPlayedFromTheBackStack = isPlayedFromTheBackStack,
            page = page,
            aspectRatio = viewAnswerData.aspectRatio,
            eventDetail = viewAnswerData.eventMap,
            startTime = viewAnswerData.startTime,
            showFullScreen = true,
            controllerAutoShow = isInPipMode.not(),
            showEnterPipModeButton = canEnterPipMode(checkPlayer = false),
            adResource = viewAnswerData.adResource,
            isInPipMode = isInPipMode,
            blockForwarding = viewAnswerData.blockForwarding,
            ncertExperiment = viewAnswerData.ncertVideoDetails?.ncertVideoExperiment ?: false,
            answerId = answerId,
            expertId = viewAnswerData.expertId,
            videoName = videoTitle,
            isPremium = viewAnswerData.isPremium,
            isVip = viewAnswerData.isVip,
            subject = viewAnswerData.logData?.subject,
            chapter = viewAnswerData.logData?.chapter,
            bottomView = viewAnswerData.bottomView,
            videoLanguage = viewAnswerData.logData?.videoLanguage,
            isLive = viewAnswerData.forceUnWrap().isRtmp,
            typeOfContent = viewAnswerData.logData?.typeOfContent,
            ocrText = viewAnswerData.ocrText,
            lockUnlockLogs = lockUnlockLogs,
            analysisData = viewAnswerData.analysisData,
            imaAdTagResource = viewAnswerData.imaAdTagResourceData
        )

        startPositionInSeconds = 0

        updateUiAsPerMpvpExperiment(viewAnswerData)
        wasMovedToLiveClass = false
        handleSimilarOverLapView(viewAnswerData.topicVideoText)

        binding.bannerPdfDownloadBeforeAutoplayContainer.hide()
        mPdfBannerData = viewAnswerData.pdfBannerData
        mCanShowPdfDownloadBannerAfterVideo = false
        viewAnswerData.pdfBannerData?.let {
            videoPageViewModel.getPdfUrlData(it.qid, it.limit, it.title, it.fileName, it.persist)
            mPdfBannerVersion = it.version
        }
        chapter = viewAnswerData.chapter
        isDoubtFeedBannerApiCalled = false
        binding.bannerVideoPageDoubtFeed.root.hide()

        setupUi(viewAnswerData)
        questionId = viewAnswerData.questionId
        handleTagList(viewAnswerData.tagsList)
        videoPageViewModel.publishEventWith(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, true)
        handleCommentView(viewAnswerData)
        sendBranchEvents()
        updatePictureInPictureActions()
        if (viewAnswerData.isNcert) {
            videoPageViewModel.storeNcertVideoWatchCoreAction()
        }

        setUpShareOptionClick(
            viewAnswerData.ocrText,
            viewAnswerData.shareMessage
        )

        if (viewAnswerData.popularCourseWidget == null) {
            binding.popularCourseWidget.hide()
        } else {
            lifecycleScope.launchWhenResumed {
                viewAnswerData.popularCourseWidget.delayInSec?.let {
                    delay(TimeUnit.SECONDS.toMillis(it))
                }
                binding.popularCourseWidget.source = TAG
                binding.popularCourseWidget.show()
                binding.popularCourseWidget.bindWidget(
                    binding.popularCourseWidget.widgetViewHolder,
                    PopularCourseWidgetModel().apply {
                        _data = viewAnswerData.popularCourseWidget.data
                        extraParams = viewAnswerData.popularCourseWidget.extraParams
                    }
                )
            }
        }

        showReferAndEarnButtonBelowVideo(viewAnswerData.showReferAndEarn)
    }

    private fun setUpShareOptionClick(
        ocrText: String,
        shareMessage: String?
    ) {
        binding.btnShare.apply {
            setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_share_black_24dp,
                    null
                )
            )
            setOnClickListener {
                val shareOptionBottomSheet = ShareOptionsBottomSheetFragment.newInstance()
                shareOptionBottomSheet.setShareOptionClickListener(object :
                    ShareOptionsBottomSheetFragment.ShareOptionClickListener {
                    override fun onWhatsappShareClick() {
                        shareOptionBottomSheet.dismiss()
                        shareVideoOnWhatsapp(shareMessage.orEmpty())
                        videoPageViewModel.sendEvent(
                            EventConstants.SHARE_OPTIONS_WHATSAPP_CLICK,
                            ignoreSnowplow = true
                        )
                    }

                    override fun onStudyGroupShareClick() {
                        shareOptionBottomSheet.dismiss()
                        shareVideoOnStudyGroup(ocrText)
                        videoPageViewModel.sendEvent(
                            EventConstants.SHARE_OPTIONS_SG_CLICK,
                            ignoreSnowplow = true
                        )
                    }

                    override fun onDismiss() {
                        videoPageViewModel.sendEvent(
                            EventConstants.SHARE_OPTIONS_DISMISS,
                            ignoreSnowplow = true
                        )
                    }
                })
                shareOptionBottomSheet.show(
                    supportFragmentManager,
                    ShareOptionsBottomSheetFragment.TAG
                )
                videoPageViewModel.sendEvent(
                    EventConstants.SHARE_OPTIONS_BOTTOM_SHEET_SHOWN,
                    ignoreSnowplow = true
                )
            }
        }
    }

    private fun showVideoBlockedFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.videoContainer, VideoBlockedFragment.newInstance(viewAnswerData!!, page))
            .commit()
    }

    private fun handleShareVisibility(isShareable: Boolean) {
        if (isShareable) {
            binding.btnShare.show()
            binding.tvShareCount.show()
            binding.btnAddPlaylist.show()
            binding.tvSaveVideo.show()
        } else {
            binding.btnShare.hide()
            binding.tvShareCount.hide()
            binding.btnAddPlaylist.hide()
            binding.tvSaveVideo.hide()
        }
    }

    private fun updateUiAsPerMpvpExperiment(viewAnswerData: ViewAnswerData) {
        val ncertVideoDetails = viewAnswerData.ncertVideoDetails
        if (ncertVideoDetails != null && ncertVideoDetails.ncertVideoExperiment == true) {
            isNcertExperimentEnabled = true
            videoPageViewModel.publishPlayVideoClickEvent(viewAnswerData.questionId, TAG)
            UXCam.tagScreenName(NCERT_EXPERIMENT_VIDEO_PAGE)
            updateUiForNcertExperiment(ncertVideoDetails.ncertVideoTitle)
            addNcertVideoFragment(
                createNcertSimilarVideoFragment(
                    viewAnswerData.questionId,
                    ncertVideoDetails
                )
            )
        } else if (updateSimilarVideoFragment && showBottomSheetPlaylist().not()) {
            addFragment(createSimilarVideoFragment(viewAnswerData))
        }
    }

    private fun updateUiForNcertExperiment(ncertVideoTitle: String?) {
        binding.askQuestionCameraButton.hide()
        binding.bottomNavigationView.hide()
        binding.similarFragment.setMargins(0, 0, 0, 0)
        binding.ivBackFromVideo.hide()
        binding.topLayout.show()
        binding.tvVideoTitle.text = ncertVideoTitle.orDefaultValue()
        binding.ivBack.setOnClickListener {
            videoPageViewModel.sendEvent(EventConstants.BACK_FROM_NCERT_PAGE, ignoreSnowplow = true)
            videoPlayerManager?.closeConvivaSession()
            finish()
        }
        binding.bottomSheetVideoPlaylist.root.hide()
        binding.nextVideoBottomBar.hide()
    }

    private fun handleSimilarOverLapView(topicVideoText: String) {
        binding.similarFragmentOverLap.hide()
        if (topicVideoText.isBlank()) {
            binding.layoutTopicText.hide()
            binding.imageViewStar.hide()
            binding.textViewPlaylist.hide()
            binding.imageViewDropDown.hide()
        } else {
            binding.layoutTopicText.show()
            binding.imageViewStar.show()
            binding.textViewPlaylist.show()
            binding.imageViewDropDown.show()
            binding.imageViewDropDown.setImageDrawable(resources.getDrawable(R.drawable.ic_arrow_down_24_px))
            binding.textViewPlaylist.text = topicVideoText
            addFragmentOverlap(SimilarPlaylistFragment.newInstance(questionId!!) as SimilarPlaylistFragment)
            videoPageViewModel.publishEventWith(EventConstants.RELATED_CONCEPT_BAR_VISIBLE)
        }
    }

    private fun handleTagList(tagList: List<VideoTagViewItem>?) {
        if (!tagList.isNullOrEmpty()) {
            binding.tagsRecyclerView.show()
            val adapter = VideoTagListAdapter(this)
            binding.tagsRecyclerView.adapter = adapter
            adapter.updateFeeds(tagList)
        } else {
            binding.tagsRecyclerView.hide()
        }
    }

    private fun handleCommentView(viewAnswerData: ViewAnswerData) {
        commentData = viewAnswerData.commentData
        if (commentData != null) {
            updateCommentView(false)
            scheduleCommentPopup(commentData!!.start, commentData!!.end)
        } else {
            updateCommentView(true)
        }
    }

    private fun setupUi(viewAnswerData: ViewAnswerData) {
        setQuestionOcr(viewAnswerData.questionId, viewAnswerData.ocrText, viewAnswerData.question)
        binding.btnLike.isSelected = viewAnswerData.isLiked
        binding.btnDislike.isSelected = viewAnswerData.isDisliked
    }

    private fun setQuestionOcr(questionId: String, ocrText: String, question: String?) {
        binding.mathJaxView.show()
        binding.mathJaxView.setTextColor("black")
        binding.mathJaxView.setFontSize(15)
        binding.mathJaxView.text = ""
        binding.mathJaxView.text = "$questionId : " +
                if (ocrText.contains("<math")) question
                else ocrText
    }

    private fun scrollToTop() {
        binding.layoutVideoSocialInteractionButtons.setExpanded(true, true)
    }

    private fun addFragment(similarFragmentInstance: SimilarVideoFragment) {
        with(supportFragmentManager) {
            findFragmentByTag(SimilarVideoFragment.TAG)?.let {
                beginTransaction().remove(it).commit()
                popBackStack()
                executePendingTransactions()
            }
            beginTransaction().add(
                R.id.similarFragment, similarFragmentInstance,
                SimilarVideoFragment.TAG
            ).commit()
        }
    }

    private fun addNcertVideoFragment(ncertSimilarFragment: NcertSimilarFragment) {
        with(supportFragmentManager) {
            findFragmentByTag(NcertSimilarFragment.TAG)?.let {
                beginTransaction().remove(it).commit()
                popBackStack()
                executePendingTransactions()
            }
            beginTransaction().add(
                R.id.similarFragment, ncertSimilarFragment,
                NcertSimilarFragment.TAG
            ).commit()
        }
    }

    private fun addFragmentOverlap(similarPlaylistFragment: SimilarPlaylistFragment) {
        supportFragmentManager.beginTransaction()
            .add(R.id.similarFragmentOverLap, similarPlaylistFragment).commit()
    }

    private fun createSimilarVideoFragment(viewAnswerData: ViewAnswerData): SimilarVideoFragment {
        return SimilarVideoFragment.newInstance(
            questionId = viewAnswerData.questionId,
            mc_id = mcId ?: "",
            playlistId = playlistId ?: "",
            page = if (page == Constants.PAGE_YT_ASK) Constants.PAGE_SRP else page,
            fromMicroConcept = isMicroConcept,
            parentId = parentId ?: "",
            fromBackpressed = isPlayedFromTheBackStack,
            supportResourceType = SOLUTION_RESOURCE_TYPE_VIDEO,
            ocr = ocr,
            autoPlay = viewAnswerData.autoPlay,
            wasMovedToLiveClass = wasMovedToLiveClass,
            isFilter = viewAnswerData.isFilter,
            chapter = viewAnswerData.chapter
        ).also {
            similarFragmentInstance = it
        }
    }

    private fun createNcertSimilarVideoFragment(
        questionId: String,
        ncertVideoDetails: NcertVideoDetails
    ): NcertSimilarFragment =
        NcertSimilarFragment.newInstance(
            playlistId = ncertVideoDetails.ncertPlaylistId,
            type = ncertVideoDetails.ncertPlaylistType,
            questionId = questionId,
            page = page
        )

    private fun slideUp() {
        binding.imageViewDropDown.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_arrow_down_24_px
            )
        )
        val animSlideDown = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_up)
        binding.similarFragmentOverLap.startAnimation(animSlideDown)
        binding.similarFragmentOverLap.visibility = View.GONE
        videoPageViewModel.publishEventWith(EventConstants.RELATED_CONCEPT_DROP_DOWN_CLOSE)
    }

    private fun slideDown() {
        binding.imageViewDropDown.setImageDrawable(
            ContextCompat.getDrawable(
                this,
                R.drawable.ic_arrow_up_24_px
            )
        )
        val animSlideDown = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down)
        binding.similarFragmentOverLap.startAnimation(animSlideDown)
        binding.similarFragmentOverLap.visibility = View.VISIBLE
        videoPageViewModel.publishEventWith(EventConstants.RELATED_CONCEPT_DROP_DOWN_OPEN)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
        sendVideoPlaybackFailedEvent(reason = Constants.UNAUTHORIZED_USER_ERROR)
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
        videoPageViewModel.sendEvent(
            EventConstants.VIDEO_SCREEN_EXIT_BEFORE_PLAYING,
            hashMapOf<String, Any>().apply {
                put(EventConstants.VIDEO_ANALYTICS_TOTAL_INIT_TIME_V1, -1)
            },
            true
        )

        sendVideoPlaybackFailedEvent(e)
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    private fun ioExceptionHandler(e: Throwable) {
        val message = if (NetworkUtils.isConnected(this)) {
            getString(R.string.somethingWentWrong)
        } else {
            getString(R.string.string_noInternetConnection)
        }
        toast(message)
        sendVideoPlaybackFailedEvent(e, reason = message)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent != null) {
            extractIntentData()
        }
        handleIntent(intent)
    }

    override fun onFragmentInteraction(
        qid: String,
        isFromTopicBooster: Boolean,
        nextSimilarVideoData: SimilarVideoList?
    ) {
        if (shouldShowVideoPlaylistBottomSheet) {
            videoPageViewModel.clearBottomPlaylistVideoStack()
            shouldShowVideoPlaylistBottomSheet = false
        }
        updateSimilarVideoFragment = true
        // DNR region start
        // Reset variables to show DNR popup again
        shouldShowDnrRewardPopupForSrp = true
        shouldShowDnrRewardPopupForNonSrp = true
        // DNR region end
        loadNextVideo(qid, isFromTopicBooster, nextSimilarVideoData)
    }

    private fun loadNextVideo(
        qid: String,
        isFromTopicBooster: Boolean = false,
        nextSimilarVideoData: SimilarVideoList? = null,
        addToStack: Boolean = true,
        newPage: String? = null
    ) {
        val action = {
            scrollToTop()
            pageStack.push(page)
            this.parentId = "0"
            isPlayedFromTheBackStack = false
            this@VideoPageActivity.questionId = qid
            if (newPage == null) {
                this@VideoPageActivity.page =
                    if (page == Constants.PAGE_SRP) Constants.PAGE_MPVP else Constants.PAGE_SIMILAR
            } else {
                page = newPage
            }
            this@VideoPageActivity.playlistId = null
            this@VideoPageActivity.isMicroConcept = false
            this@VideoPageActivity.mcId = null
            this@VideoPageActivity.referredStudentId = ""
            this@VideoPageActivity.isFromTopicBooster = isFromTopicBooster
            startPositionInSeconds = 0
            fetchAnswerDetail(addToStack)
            videoPageViewModel.publishPlayVideoClickEvent(qid, TAG)
            sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, qid)
            // Send this event to Branch
//            BranchIOUtils.userCompletedAction(
//                EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK,
//                JSONObject().apply {
//                    put(EventConstants.QUESTION_ID, qid)
//                    put(EventConstants.SOURCE, TAG)
//                })
            sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_FROM_SIMILAR, qid)
            if (addToStack) {
                similarCount += 1
            }
            endFireBaseUserAction()
        }

        if (mPdfBannerVersion == PDF_BANNER_AFTER_VIDEO && nextSimilarVideoData != null
            && videoPageViewModel.pdfUrlDataLiveData.value?.url != null
            && mPdfBannerData != null && isInPipMode.not()
        ) {
            showBeforeAutoplayPdfDownloadBanner(mPdfBannerData!!, nextSimilarVideoData) {
                action()
            }
        } else {
            action()
        }
    }

    override fun onNextCourseVideoPlay() {
        videoPlayerManager?.resetVideo()
    }

    override fun onResourceSelected(qid: String) {
        onFragmentInteraction(qid, false)
        supportFragmentManager.findFragmentById(R.id.similarFragmentOverLap)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        videoPageViewModel.publishEventWith(EventConstants.RELATED_CONCEPT_VIDEO_PLAYED)
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

    override fun onShowPlayerControls() {
        if (isVideoImageSummaryAvailable) {
            binding.bannerVideoImageSummary.show()
        }
    }

    override fun onHidePlayerControls() {
        binding.bannerVideoImageSummary.hide()
    }

    override fun onPictureInPictureModeRequested() {
        videoPageViewModel.sendEvent(EventConstants.PIP_MODE_BUTTON_CLICKED, ignoreSnowplow = true)
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        startActivity(homeIntent)
    }

    override fun onExoPlayerProgress(positionMs: Long) {
        if (positionMs >= 30_000) {
            getDoubtFeedBanner()
        }
        checkForDnrReward()
    }

    private fun checkForDnrReward() {
        lifecycleScope.launchWhenStarted {
            val engagementTimeToClaimDnrReward = when (page) {
                Constants.PAGE_SRP -> {
                    if (shouldShowDnrRewardPopupForSrp == false) return@launchWhenStarted
                    defaultDataStore.get().srpSfEngagementTimeToClaimDnrReward.firstOrNull()
                        ?: DEFAULT_SRP_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD
                }
                else -> {
                    if (shouldShowDnrRewardPopupForNonSrp == false) return@launchWhenStarted
                    defaultDataStore.get().nonSrpSfEngagementTimeToClaimDnrReward.firstOrNull()
                        ?: DEFAULT_NON_SRP_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD
                }
            }
            val currentEngagementTime = (videoPlayerManager?.currentEngagementTime ?: 0) * 1000L
            if (engagementTimeToClaimDnrReward > currentEngagementTime) {
                return@launchWhenStarted
            }
            claimDnrReward(currentEngageTime = currentEngagementTime)

            // Reset variable to avoid showing DNR popup again for the same video
            shouldShowDnrRewardPopupForSrp = false
            shouldShowDnrRewardPopupForNonSrp = false
        }
    }

    private fun claimDnrReward(currentEngageTime: Long) {
        dnrRewardViewModel.claimReward(
            DnrVideoWatchReward(
                viewId = viewId,
                questionId = questionId.orDefaultValue(),
                duration = currentEngageTime,
                source = page,
                type = DnrRewardType.VIDEO_VIEW.type
            )
        )
    }

    private fun updateFullScreenUI() {
        binding.askQuestionCameraButton.hide()
        binding.bottomNavigationView.hide()
        binding.extraView.hide()
        videoPlayerManager?.enterFullScreen()
        if (videoPlayerManager?.videoFragment != null
            && videoPlayerManager?.isYoutubeVideoPlaying != true
        ) {
            videoPageViewModel.sendEvent(
                EventConstants.HORIZONTAL_VIEW_ENABLED,
                hashMapOf(), true
            )
            addLandscapeSimilarFragment()
        }
        binding.bannerPdfDownloadBeforeAutoplay.root.hide()
        binding.bannerPdfDownloadBeforeAutoplayLandscape.root.isVisible =
            mCanShowPdfDownloadBannerAfterVideo
        binding.bottomSheetVideoPlaylist.root.hide()
        binding.nextVideoBottomBar.hide()
    }

    private fun updatePortraitScreenUI() {
        // Show bottom navigation only if ncert experiment is not enabled
        if (isNcertExperimentEnabled != true && showBottomSheetPlaylist().not()) {
            binding.askQuestionCameraButton.show()
            // For D0 user hide bottom nav
            if (videoPageViewModel.hideBottomNavigation == true) {
                binding.bottomNavigationView.hide()
            } else {
                binding.bottomNavigationView.show()
            }
        }
        binding.extraView.show()
        videoPlayerManager?.exitFullScreen()
        if (videoPlayerManager?.videoFragment != null
            && videoPlayerManager?.isYoutubeVideoPlaying != true
        ) {
            removeLandscapeSimilarFragment()
        }
        binding.bannerPdfDownloadBeforeAutoplayLandscape.root.hide()
        binding.bannerPdfDownloadBeforeAutoplay.root.isVisible = mCanShowPdfDownloadBannerAfterVideo
        if (showBottomSheetPlaylist()) {
            binding.bottomSheetVideoPlaylist.root.show()
            binding.nextVideoBottomBar.show()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            updatePortraitScreenUI()
        } else {
            videoPageViewModel.setLandscapeBottomSheetExpandedState(false)
            updateFullScreenUI()
        }

        if (isInPipMode) {
            binding.bottomNavigationView.hide()
            binding.askQuestionCameraButton.hide()
            binding.filterSheet.hide()
            binding.bottomSheetVideoPlaylist.root.hide()
            binding.nextVideoBottomBar.hide()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        videoPlayerManager?.onWindowFocusChanged(hasFocus)
    }

    override fun addLandscapeSimilarFragment() {
        if (supportFragmentManager.findFragmentByTag(LandscapeSimilarVideoBottomDialog.TAG) == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(
                    R.id.filterSheet,
                    LandscapeSimilarVideoBottomDialog.newInstance(),
                    LandscapeSimilarVideoBottomDialog.TAG
                )
                commitAllowingStateLoss()
            }
            binding.filterSheet.show()
        }
    }

    override fun removeLandscapeSimilarFragment() {
        if (supportFragmentManager.findFragmentByTag(LandscapeSimilarVideoBottomDialog.TAG) != null) {
            supportFragmentManager.findFragmentByTag(LandscapeSimilarVideoBottomDialog.TAG)?.let {
                supportFragmentManager.beginTransaction().remove(it).commitAllowingStateLoss()
            }
            binding.filterSheet.hide()
        }
    }

    override fun showLandscapeSimilarFragment() {
        binding.filterSheet.show()
        videoPageViewModel.setLandscapeBottomSheetExpandedState(false)
    }

    override fun hideLandscapeSimilarFragment() {
        videoPageViewModel.isLandscapeFragmentExpanded.let {
            if (!it) {
                videoPageViewModel.setLandscapeBottomSheetExpandedState(false)
                binding.filterSheet.hide()
            }
        }
    }

    override fun changeLandscapeSimilarFragmentState(toExpand: Boolean) {
        videoPageViewModel.setLandscapeBottomSheetExpandedState(toExpand)
    }

    override fun singleTapOnPlayerView() {
        if (videoPlayerManager?.videoFragment == null) return
        val isAdPlaying = videoPlayerManager?.videoFragment?.isAdPlaying() ?: false
        if (videoPlayerManager?.isPlayerControllerVisible == true
            && videoPageViewModel.isLandscapeFragmentExpanded
        ) {
            videoPageViewModel.setLandscapeBottomSheetExpandedState(false)
        } else if (videoPlayerManager?.isPlayerControllerVisible == true && isAdPlaying.not()) {
            videoPlayerManager?.hidePlayerController(isInPipMode)
            videoPageViewModel.setLandscapeBottomSheetExpandedState(false)
            binding.filterSheet.hide()
        } else if (videoPlayerManager?.isPlayerControllerVisible != true && isAdPlaying.not()) {
            binding.filterSheet.show()
            videoPlayerManager?.showPlayerController()
        }
    }

    override fun showSuggestions() {
        super.showSuggestions()
        binding.filterSheet.show()
    }

    override fun hideSuggestions() {
        super.hideSuggestions()
        binding.filterSheet.hide()
    }

    override fun onVideoStart() {
        updatePictureInPictureActions()
        nextNcertVideoPlayed = false
    }

    override fun onVideoPause() {
        updatePictureInPictureActions()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1001) {
            userSelectedState = data?.getIntExtra("userSelectedState", -1) ?: -1
        }
    }

    private fun handleIntent(intent: Intent?) {
        val appLinkData = intent?.data
        when {
            appLinkData != null -> {
                val videoId1 =
                    appLinkData.path?.split("-")?.get(appLinkData.path?.split("-")!!.size - 1)!!
                val videoId2 =
                    appLinkData.path?.split("/")?.get(appLinkData.path?.split("/")!!.size - 1)!!
                val videoId = if (videoId1.length <= videoId2.length) {
                    videoId1
                } else {
                    videoId2
                }
                intent.putExtra(Constants.QUESTION_ID, videoId)
                intent.putExtra(Constants.PAGE, Constants.APP_INDEXING)
                startNewVideo(videoId)
                handleAppIndexingEvent()
            }
            intent?.getStringExtra(INTENT_EXTRA_SOURCE) in setOf(
                Constants.SMART_CONTENT,
                Constants.LIVE_CLASS_SIMILAR_VIDEO_PAGE
            ) -> {
                startNewVideoFromIntent(intent)
            }
            else -> {
                fetchAnswerDetail()
            }
        }
    }

    private fun startNewVideoFromIntent(intent: Intent?) {
        if (intent != null) {
            scrollToTop()
            pageStack.push(page)
            isPlayedFromTheBackStack = false

            this@VideoPageActivity.parentId = "0"
            this@VideoPageActivity.questionId =
                intent.getStringExtra(INTENT_EXTRA_QUESTION_ID).orDefaultValue()
            this@VideoPageActivity.page = intent.getStringExtra(INTENT_EXTRA_PAGE).orDefaultValue()
            this@VideoPageActivity.playlistId = intent.getStringExtra(INTENT_EXTRA_PLAYLIST_ID)
            this@VideoPageActivity.isMicroConcept =
                intent.getBooleanExtra(INTENT_EXTRA_IS_MICRO_CONCEPT, false)
            this@VideoPageActivity.mcId = intent.getStringExtra(INTENT_EXTRA_MC_ID)
            this@VideoPageActivity.referredStudentId =
                intent.getStringExtra(INTENT_EXTRA_REFERRED_STUDENT_ID)
            this@VideoPageActivity.startPositionInSeconds =
                intent.getLongExtra(INTENT_EXTRA_START_POSITION, 0)

            fetchAnswerDetail()
            similarCount += 1
        }
    }

    private fun indexVideo(title: String, description: String, webUrl: String) {
        val videoToIndex = Indexable.Builder()
            .setName(title)
            .setUrl(webUrl)
            .setDescription(description)
            .build()

        val task = FirebaseAppIndex.getInstance(applicationContext).update(videoToIndex)
        task.addOnSuccessListener { Log.d("IndexApp", title + "\n" + webUrl) }
    }

    private fun startFireBaseUserAction() {
        FirebaseUserActions.getInstance(applicationContext)
            .start(getVideoViewAction(titleIndex, descriptionIndex))
    }

    private fun endFireBaseUserAction() {
        FirebaseUserActions.getInstance(applicationContext)
            .end(getVideoViewAction(titleIndex, descriptionIndex))
    }

    private fun getVideoViewAction(title: String, webUrl: String): Action {
        return Action.Builder(Action.Builder.WATCH_ACTION)
            .setObject(title, webUrl)
            .build()
    }

    private fun startNewVideo(questionId: String) {
        this.parentId = "0"
        this@VideoPageActivity.questionId = questionId
        this@VideoPageActivity.page = Constants.APP_INDEXING
        this@VideoPageActivity.playlistId = null
        this@VideoPageActivity.isMicroConcept = false
        this@VideoPageActivity.mcId = null
        this@VideoPageActivity.referredStudentId = ""
        startPositionInSeconds = 0
        fetchAnswerDetail()
        videoPageViewModel.publishPlayVideoClickEvent(questionId, TAG)
        sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, questionId)
        // Send this event to Branch
//        BranchIOUtils
//            .userCompletedAction(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, JSONObject().apply {
//                put(EventConstants.QUESTION_ID, questionId)
//                put(EventConstants.SOURCE, TAG)
//            })
        BranchIOUtils
            .userCompletedAction(
                EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK + "_v2",
                JSONObject().apply {
                    put(EventConstants.QUESTION_ID, questionId)
                    put(EventConstants.SOURCE, TAG)
                })
    }

    private fun playNextMicroConcept(nextMicroConceptData: VideoPageMicroConcept?) {
        this.parentId = "0"
        this.questionId = nextMicroConceptData?.mcId
        this.page = Constants.PAGE_CC
        this.isPlayedFromTheBackStack = false
        if (getStudentClass() == "14") {
            this@VideoPageActivity.playlistId = Constants.PAGE_SSC
        }
        this@VideoPageActivity.mcId = null
        this@VideoPageActivity.referredStudentId = ""
        startPositionInSeconds = 0
        if (!this.questionId.isNullOrBlank()) {
            fetchAnswerDetail()
        }
        similarCount += 1
    }

    override fun onStart() {
        super.onStart()
        DoubtnutApp.INSTANCE.handleStickyNotification()
        mPdfBannerProgressBarAnimator?.resume()
    }

    public override fun onResume() {
        super.onResume()

        if (isDeviceOrientationOn()) {
            Sensey.getInstance().startOrientationDetection(this)
        }
        videoPageViewModel.executeIfVideoViewStackIsEmpty {
            if (wasMovedToLiveClass && liveClassActivityEnteredPip()) {
                finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isDeviceOrientationOn()) {
            Sensey.getInstance().stopOrientationDetection(this)
        }
    }

    override fun onStop() {
        mPdfBannerProgressBarAnimator?.pause()
        endFireBaseUserAction()
        videoPlayerManager?.let {
            sendIasBounceEvent(it.currentPlayerPosition.toLong(), it.videoDuration.toLong())
        }
        videoPlayerManager?.let {
            averageVideoTime =
                if (averageVideoTime == 0L) (it.videoDuration.toLong() - 30) else averageVideoTime
            val currentPositionSeconds = it.currentPlayerPosition
            if (currentPositionSeconds in minWatchTime..averageVideoTime) {
                val remainingTimeSeconds = it.videoDuration - currentPositionSeconds
                DoubtnutApp.INSTANCE.handleVideoStickyNotification(
                    VideoStickyNotificationData(
                        questionId = questionId ?: "",
                        page = page,
                        watchedTimeSeconds = currentPositionSeconds,
                        totalTimeSeconds = it.videoDuration,
                        imageUrl = thumbnailImage,
                        playListId = playlistId ?: "",
                        notificationTitle = getNotificationTitleText(),
                        remainingDurationText = getNotificationRemainingDurationText(
                            remainingTimeSeconds
                        )
                    )
                )
            }
        }
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        rxObserver?.dispose()
        mLiveClassObserverDisposable?.dispose()
        timerDisposable.dispose()
        if (defaultPrefs(this).getBoolean(
                NotificationConstants.NOTIFICATION_USER_DISMISS_VIDEO_STICKY, false
            ).not()
        ) {
            Handler(Looper.getMainLooper()).postDelayed({
                DoubtnutApp.INSTANCE.handleStickyNotification()
            }, 500)
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

    override fun onVideoCompleted() {
        if (isNcertExperimentEnabled == true) {
            if (nextNcertVideoPlayed != true) {
                nextNcertVideoPlayed = true
                ncertViewModel.playNextVideo()
            }
        } else {
            playNextVideo()
        }
    }

    private fun playNextVideo() {
        if ((page == Constants.PAGE_SRP || page == Constants.PAGE_MPVP_BOTTOM_SHEET) &&
            videoPageViewModel.isLastPlaylistVideo().not()
        ) {
            videoPageViewModel.autoplayVideoBottomBarLiveData.value?.let {
                playNextPlaylistVideo(it.questionId)
                videoPageViewModel.sendBottomSheetEvent(
                    EventConstants.MPVP_BOTTOM_SHEET_VIDEO_AUTOPLAYED,
                    ignoreSnowplow = true
                )
            }
        } else {
            if (isInPipMode) {
                similarFragmentInstance?.onNextVideoRequestedInPip()
                // Reset and hide next button till we get next video
                videoPageViewModel.firstSimilarPipPlayableVideo.value = null
            } else {
                if (nextConceptData != null) {
                    playNextMicroConcept(nextConceptData)
                } else {
                    similarFragmentInstance?.onVideoComplete()
                }
            }
        }
    }

    override fun onViewIdPublished(viewId: String) {
        this.viewId = viewId
    }

    fun onCommentButtonClicked(
        entityId: String,
        entityType: String,
        feedPosition: Int,
        batchId: String?
    ) {
        if (commentData != null) {
            if (commentBottomSheetFragment?.isVisible != true) {
                commentBottomSheetFragment = CommentBottomSheetFragment
                    .newInstance(
                        entityType,
                        entityId,
                        "",
                        null,
                        null,
                        0L,
                        batchId,
                        false,
                        assortmentId = ""
                    )
                commentBottomSheetFragment?.show(
                    supportFragmentManager,
                    CommentBottomSheetFragment.TAG
                )
            }
        } else {
            CommentsActivity.startActivityForResult(
                this,
                entityId,
                entityType,
                feedPosition,
                PAGE_VIDEO,
                batchId
            )
        }
        sendEvent(EventConstants.EVENT_NAME_COMMENT_ICON_CLICK + entityType)
    }

    private fun sendIasBounceEvent(currentVideoDuration: Long, totalVideoLength: Long) {
        if (page != Constants.PAGE_SEARCH_SRP)
            return
        val watchPercent = (currentVideoDuration.toDouble() / totalVideoLength.toDouble()) * 100
        val map = hashMapOf<String, Any>().apply {
            put(Constants.STUDENT_ID, defaultPrefs().getString(Constants.STUDENT_ID, "") ?: "")
            put(Constants.QUESTION_ID, questionId ?: "")
            put("watch_duration", currentVideoDuration)
        }
        if (watchPercent > 80) {
            val event = AnalyticsEvent(EventConstants.IAS_80PER, map, ignoreSnowplow = true)
            analyticsPublisher.publishEvent(event)
            analyticsPublisher.publishMoEngageEvent(event)
            DoubtnutApp.INSTANCE.getEventTracker().addEventNames(EventConstants.IAS_80PER)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addEventParameter(map)
                .track()

        }
        val eventName = when {
            currentVideoDuration < 30 -> EventConstants.IAS_BOUNCE
            currentVideoDuration > 140 -> EventConstants.IAS_140S
            else -> null
        } ?: return
        val event = AnalyticsEvent(eventName, map, ignoreSnowplow = true)
        analyticsPublisher.publishEvent(event)
        analyticsPublisher.publishMoEngageEvent(event)
        DoubtnutApp.INSTANCE.getEventTracker().addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(this).toString())
            .addStudentId(getStudentId())
            .addEventParameter(map)
            .track()
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onBackPressed() {
        try {
            when {
                isFullScreen -> {
                    userSelectedState = 90
                    isFullScreen = false
                    requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    updatePortraitScreenUI()
                    videoPlayerManager?.exitFullScreen()
                    return
                }
                videoPageViewModel.backPressBottomSheetDeeplink.isNotNullAndNotEmpty() &&
                        playlistBottomSheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED && similarCount <= 1 -> {
                    deeplinkAction.performAction(
                        this@VideoPageActivity,
                        videoPageViewModel.backPressBottomSheetDeeplink
                    )
                    videoPageViewModel.backPressBottomSheetDeeplink = ""
                    return
                }
                isNcertExperimentEnabled == true -> {
                    if (ncertViewModel.hasUserWatchedMoreThanOneVideo()) {
                        ncertViewModel.playPreviousVideo()
                    } else {
                        val similarBooks = ncertViewModel.bookWidgetData.value
                        if (isNcertBookBottomSheetShown.not() &&
                            !similarBooks.isNullOrEmpty() &&
                            supportFragmentManager.findFragmentByTag(NcertBooksBottomSheetFragment.TAG) == null
                        ) {
                            ncertBooksBottomSheet = NcertBooksBottomSheetFragment.newInstance(
                                getBottomSheetUiConfig()
                            )
                            ncertBooksBottomSheet?.show(
                                supportFragmentManager,
                                NcertBooksBottomSheetFragment.TAG
                            )
                            isNcertBookBottomSheetShown = true
                            videoPageViewModel.sendEvent(
                                EventConstants.NCERT_BOOK_BOTTOM_SHEET_VISIBLE,
                                hashMapOf<String, Any>().apply {
                                    put(EventConstants.SOURCE, "back_press")
                                }, ignoreSnowplow = true
                            )
                        } else {
                            finish()
                            closeConvivaSession()
                        }
                    }
                }
            }
        } catch (e: Exception) {

        }
        if (!pageStack.empty()) {
            page = pageStack.pop()
        }
        when {
            playFromAppIndexingWithoutOnBoarding -> {
                startActivity(
                    Intent(
                        this,
                        SplashActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
                this.finish()
                supportFragmentManager.popBackStack()
            }
            similarCount <= 1 -> {
                if (isTaskRoot) {
                    bringLauncherTaskToFront()
                    finishAndRemoveTask()
                }
                if (isNcertExperimentEnabled == true) {
                    videoPageViewModel.sendEvent(
                        EventConstants.BACK_FROM_NCERT_PAGE
                    )
                }
                if (isNcertExperimentEnabled != true) {
                    if (playlistBottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) {
                        playlistBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    } else {
                        finish()
                    }
                }
            }
            else -> {
                val lastBottomSheetPlaylistVideoId =
                    videoPageViewModel.currentBottomSheetPlaylistVideoId
                videoPageViewModel.getPreviousVideo(page)
                similarCount -= 1
                isPlayedFromTheBackStack = true
                videoPlayerManager?.resetVideo()
                closeConvivaSession()
                if (page == Constants.PAGE_MPVP_BOTTOM_SHEET || page == Constants.PAGE_SRP) {
                    when {
                        lastBottomSheetPlaylistVideoId != null -> {
                            updateBottomSheetPlaylistUi(
                                questionId = videoPageViewModel.currentBottomSheetPlaylistVideoId,
                                currentVideoId = lastBottomSheetPlaylistVideoId,
                                addToBottomSheetStack = false
                            )
                        }
                        else -> {
                            // If there is no video left for bottom sheet, reset count
                            similarCount = 0
                        }
                    }
                }
            }
        }
    }

    private fun getBottomSheetUiConfig(): NcertBooksBottomSheetFragment.BottomSheetDetails =
        NcertBooksBottomSheetFragment.BottomSheetDetails(
            title = R.string.ncert_book_bottomsheet_back_press_title,
            titleSize = 15.0F,
            titleColor = R.color.color_535151,
            subtitle = R.string.select_a_book,
            subtitleSize = 14.0F,
            subtitleColor = R.color.color_7d7c7c
        )

//    fun dislikeVideo(selectedOptions: String) {
//        videoPageViewModel.disLikeButtonClicked(
//            videoTitle, questionId!!, answerId,
//            (videoPlayerManager?.currentPlayerPosition
//                ?: 0).toLong(), FEATURE_TYPE, selectedOptions, viewId
//        )
//    }

    private fun addToPlayList(videoId: String) {
        videoPageViewModel.addToWatchLater(videoId)
    }

    private fun getControlParams(): HashMap<String, String> {
        val id = if (this.questionId != null) this.questionId!! else ""
        val playListId = if (playlistId != null) playlistId!! else ""
        return hashMapOf(
            Constants.PAGE to "video",
            Constants.Q_ID to id,
            Constants.PLAYLIST_ID to playListId,
            Constants.STUDENT_ID to getStudentId(),
            Constants.SOLUTION_RESOURCE_TYPE to SOLUTION_RESOURCE_TYPE_VIDEO
        )
    }

    private fun shareOnWhatsApp(deepLink: String, imageFilePath: String?, sharingMessage: String?) {
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, "$sharingMessage $deepLink")
            if (imageFilePath == null) {
                type = "text/plain"
            } else {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFilePath))
            }
        }.also {
            if (AppUtils.isCallable(this, it)) {
                startActivity(it)
            } else {
                ToastUtils.makeText(this, R.string.string_install_whatsApp, Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun showBranchLinkError() {
        this.getString(R.string.error_branchLinkNotFound).let { msg ->
            toast(msg)
        }
    }

    fun onBookmark() {}

    private fun setUpViewTextSolutionButton(textSolutionLink: String, questionId: String) {
        isVideoImageSummaryAvailable = true
        videoPageViewModel.sendEvent(
            EventConstants.EVENT_VIDEO_IMAGE_SUMMARY_VIEW,
            hashMapOf(Constants.QUESTION_ID to questionId),
            ignoreSnowplow = true
        )

        binding.bannerVideoImageSummary.setOnClickListener {
            videoPageViewModel.sendEvent(
                EventConstants.EVENT_VIDEO_IMAGE_SUMMARY_CLICKED,
                hashMapOf(Constants.QUESTION_ID to questionId),
                ignoreSnowplow = true
            )
            val intent = VideoImageSummaryActivityDialog.getStartIntent(this, textSolutionLink)
            startActivity(intent)
        }
    }

    private fun getNotificationTitleText(): String =
        resources.getString(R.string.you_were_watching)

    private fun getNotificationRemainingDurationText(remainingTimeSeconds: Int): String {
        val minutes = remainingTimeSeconds / 60
        val seconds = remainingTimeSeconds % 60

        val text1 = if (minutes > 0) resources.getQuantityString(
            R.plurals.minutes_left,
            minutes,
            minutes
        ) else ""
        val text2 = if (seconds > 0) resources.getQuantityString(
            R.plurals.seconds_left,
            seconds,
            seconds
        ) else ""

        return "$text1 $text2"
    }

    private fun showBeforeAutoplayPdfDownloadBanner(
        pdfBannerData: PdfBannerData,
        nextSimilarVideoData: SimilarVideoList,
        autoplayAction: () -> Unit
    ) {
        // Need this check to prevent progress bar animation starting multiple times
        if (binding.bannerPdfDownloadBeforeAutoplayContainer.isNotVisible) {
            videoPlayerManager?.hidePlayerController(isInPipMode)
            mCanShowPdfDownloadBannerAfterVideo = true

            if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                binding.bannerPdfDownloadBeforeAutoplayLandscape.root.hide()
                binding.bannerPdfDownloadBeforeAutoplay.root.show()
            } else {
                binding.bannerPdfDownloadBeforeAutoplay.root.hide()
                binding.bannerPdfDownloadBeforeAutoplayLandscape.root.show()
            }

            if (nextSimilarVideoData.ocrTextSimilar.isNotBlank()) {
                binding.bannerPdfDownloadBeforeAutoplay.mathViewPdfBanner.apply {
                    setTextColor("black")
                    setFontSize(10)
                    text = nextSimilarVideoData.ocrTextSimilar
                }
                binding.bannerPdfDownloadBeforeAutoplayLandscape.mathViewPdfBanner.apply {
                    setTextColor("black")
                    setFontSize(16)
                    text = nextSimilarVideoData.ocrTextSimilar
                }
                binding.bannerPdfDownloadBeforeAutoplay.ivPdfBanner.hide()
                binding.bannerPdfDownloadBeforeAutoplayLandscape.ivPdfBanner.hide()
            } else {
                binding.bannerPdfDownloadBeforeAutoplay.mathViewPdfBanner.isInvisible = true
                binding.bannerPdfDownloadBeforeAutoplayLandscape.mathViewPdfBanner.isInvisible =
                    true
                binding.bannerPdfDownloadBeforeAutoplay.ivPdfBanner.show()
                binding.bannerPdfDownloadBeforeAutoplay.ivPdfBanner.loadImage(nextSimilarVideoData.thumbnailImageSimilar)
                binding.bannerPdfDownloadBeforeAutoplayLandscape.ivPdfBanner.show()
                binding.bannerPdfDownloadBeforeAutoplayLandscape.ivPdfBanner.loadImage(
                    nextSimilarVideoData.thumbnailImageSimilar
                )
            }

            binding.bannerPdfDownloadBeforeAutoplay.apply {
                tvPdfDescriptionBannerAfterVideo.text = pdfBannerData.pdfDescription
                tvViewsInBanner.text = getString(
                    R.string.pdf_banner_video_views_count_text,
                    nextSimilarVideoData.views
                )
                tvLikesInBanner.text = getString(
                    R.string.pdf_banner_video_likes_count_text,
                    nextSimilarVideoData.likeCountSimilar.toString()
                )
            }
            binding.bannerPdfDownloadBeforeAutoplayLandscape.apply {
                tvPdfDescriptionBannerAfterVideo.text =
                    pdfBannerData.pdfDescription
                tvViewsInBanner.text =
                    getString(
                        R.string.pdf_banner_video_views_count_text,
                        nextSimilarVideoData.views
                    )
                tvLikesInBanner.text =
                    getString(
                        R.string.pdf_banner_video_likes_count_text,
                        nextSimilarVideoData.likeCountSimilar.toString()
                    )
            }

            val updatedAutoplayAction = {
                autoplayAction()
                binding.bannerPdfDownloadBeforeAutoplayContainer.hide()
                mPdfBannerProgressBarAnimator = null
                mCanShowPdfDownloadBannerAfterVideo = false
            }

            mPdfBannerProgressBarAnimator = ValueAnimator.ofFloat(1f, 100f).apply {
                duration = pdfBannerData.bannerShowTime * 1000L
                addUpdateListener {
                    videoPlayerManager?.hidePlayerController(isInPipMode)
                    binding.bannerPdfDownloadBeforeAutoplay.progressBarPdfBannerAutoplay.progress =
                        it.animatedValue as Float
                    binding.bannerPdfDownloadBeforeAutoplayLandscape.progressBarPdfBannerAutoplay.progress =
                        it.animatedValue as Float
                }
                doOnEnd {
                    updatedAutoplayAction()
                }
                start()
            }

            binding.bannerPdfDownloadBeforeAutoplayContainer.setOnClickListener {
                mPdfBannerClickListener()
                videoPageViewModel.sendEvent(
                    EventConstants.PDF_BANNER_CLICK, hashMapOf(
                        Constants.QUESTION_ID to questionId.orEmpty()
                    ), ignoreSnowplow = true
                )
            }

            val similarVideoClickAction = {
                mPdfBannerProgressBarAnimator?.end()
                videoPageViewModel.sendEvent(
                    EventConstants.PDF_AUTOPLAY_VIDEO_CLICKED,
                    ignoreSnowplow = true
                )
            }
            binding.bannerPdfDownloadBeforeAutoplay.similarCard.setOnClickListener {
                similarVideoClickAction()
            }
            binding.bannerPdfDownloadBeforeAutoplayLandscape.similarCard
                .setOnClickListener {
                    similarVideoClickAction()
                }
            binding.bannerPdfDownloadBeforeAutoplayContainer.show()
            videoPageViewModel.sendEvent(
                EventConstants.PDF_BANNER_VIEW, hashMapOf(
                    Constants.QUESTION_ID to questionId.orEmpty()
                ), ignoreSnowplow = true
            )
        }
    }

    private fun setupPlaylistBottomSheet(data: VideoPagePlaylist) {
        binding.askQuestionCameraButton.hide()
        binding.bottomNavigationView.hide()
        binding.bottomSheetVideoPlaylist.root.show()
        binding.bottomSheetVideoPlaylist.tvToolbarTitle.text = data.bottomSheetTitle

        binding.bottomSheetVideoPlaylist.rvPlaylist.layoutManager =
            LinearLayoutManagerWithSmoothScroller(this, LinearLayoutManager.VERTICAL, false)
        binding.bottomSheetVideoPlaylist.rvPlaylist.itemAnimator = null
        binding.bottomSheetVideoPlaylist.rvPlaylist.adapter = bottomSheetPlaylistAdapter.apply {
            clearList()
            updateList(data.similarQuestions, null)
        }
        binding.bottomSheetVideoPlaylist.rvPlaylist.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        videoPageViewModel.sendBottomSheetEvent(
                            EventConstants.MPVP_BOTTOM_SHEET_SCROLLED,
                            ignoreSnowplow = true
                        )
                    }
                }
            }
        })
        binding.bottomSheetVideoPlaylist.fabCamera.show()
        binding.bottomSheetVideoPlaylist.fabCamera.setOnClickListener {
            videoPageViewModel.sendEvent(
                EventConstants.VIDEO_PLAYLIST_CAMERA_ICON_CLICK,
                ignoreSnowplow = true
            )
            CameraActivity.getStartIntent(this@VideoPageActivity, VIDEO_PLAYLIST_BOTTOM_SHEET)
                .also { intent ->
                    startActivity(intent)
                }
        }

        playlistBottomSheetBehavior.apply {

            addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                @SuppressLint("SwitchIntDef")
                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    when (newState) {
                        BottomSheetBehavior.STATE_EXPANDED -> {
                            binding.nextVideoBottomBar.isClickable = false
                            videoPageViewModel.sendBottomSheetEvent(
                                EventConstants.MPVP_BOTTOM_SHEET_EXPANDED,
                                ignoreSnowplow = true
                            )
                        }
                        BottomSheetBehavior.STATE_COLLAPSED -> {
                            binding.nextVideoBottomBar.isClickable = true
                            videoPageViewModel.sendBottomSheetEvent(
                                EventConstants.MPVP_BOTTOM_SHEET_COLLAPSED,
                                ignoreSnowplow = true
                            )
                        }
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    binding.nextVideoBottomBar.alpha = 1 - (slideOffset / .20f).coerceIn(0f, 1f)
                    binding.extraViewOverlay.alpha = (slideOffset / 1.3f).coerceIn(0f, 1f)
                }
            })
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        binding.bottomSheetVideoPlaylist.rvPlaylist.updateMargins(bottom = playlistBottomSheetBehavior.expandedOffset)
        binding.extraViewOverlay.alpha = 0.75f

        binding.bottomSheetVideoPlaylist.ivBottomSheetDownArrow.setOnClickListener {
            playlistBottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }

        binding.nextVideoBottomBar.alpha = 0f
        binding.nextVideoBottomBar.setOnClickListener {
            playlistBottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        binding.bottomSheetVideoPlaylist.toolbarBottomSheet.setOnClickListener {
            /* no-op Intercept touch events*/
        }
        videoPageViewModel.sendBottomSheetEvent(
            EventConstants.MPVP_BOTTOM_SHEET_SHOWN,
            ignoreSnowplow = true
        )
    }

    private fun playNextPlaylistVideo(questionId: String) {
        updateBottomSheetPlaylistUi(questionId)
        updateSimilarVideoFragment = false
        loadNextVideo(questionId, true, newPage = Constants.PAGE_MPVP_BOTTOM_SHEET)
    }

    private fun updateBottomSheetPlaylistUi(
        questionId: String?,
        currentVideoId: String? = null,
        addToBottomSheetStack: Boolean = true
    ) {
        val positionsPair = videoPageViewModel.changeCurrentPlaylistPlayingVideo(
            questionId,
            currentVideoId,
            addToBottomSheetStack
        )

        positionsPair.toList().filter { it >= 0 }.forEach {
            bottomSheetPlaylistAdapter.notifyItemChanged(it)
        }

        positionsPair.second.takeIf { it >= 0 }?.let {
            binding.bottomSheetVideoPlaylist.rvPlaylist.smoothScrollToPosition(it)
        }
    }

    private fun updateNextVideoBottomBar(nextVideoData: VideoBottomBarData) {
        binding.nextVideoBottomBar.show()
        binding.tvNextVideoQuestionMeta.isInvisible = nextVideoData.questionMeta == null
        binding.tvNextVideoQuestionMeta.text = nextVideoData.questionMeta
        binding.mathJaxViewNextVideo.apply {
            setTextColor("black")
            setFontSize(12)
            text = nextVideoData.ocrText.orEmpty()
        }
    }

    private fun showBottomSheetPlaylist(): Boolean =
        shouldShowVideoPlaylistBottomSheet && requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    private fun getDoubtFeedBanner() {
        if (isDoubtFeedBannerApiCalled.not() && page == Constants.PAGE_SRP && videoPageViewModel.isDoubtFeed2Enabled()) {
            isDoubtFeedBannerApiCalled = true
            chapter?.let {
                videoPageViewModel.getDoubtFeedVideoBanner(it)
            }
        }
    }

    private fun setupDoubtFeedBanner(data: DoubtFeedBanner) {
        binding.bannerVideoPageDoubtFeed.root.show()

        binding.bannerVideoPageDoubtFeed.tvDoubtFeedTitle.text = data.title
        binding.bannerVideoPageDoubtFeed.tvDoubtFeedSubtitle.text = data.subtitle
        binding.bannerVideoPageDoubtFeed.buttonDoubtFeed.text = data.ctaText

        binding.bannerVideoPageDoubtFeed.root.setOnClickListener {
            deeplinkAction.performAction(this, data.deeplink)
            videoPageViewModel.sendEvent(
                EventConstants.DF_MPVP_BANNER_CLICKED, hashMapOf(
                    Constants.TOPIC to data.topic
                ), ignoreSnowplow = true
            )

            videoPageViewModel.sendEvent(
                EventConstants.DG_MPVP_BANNER_CLICKED, hashMapOf(
                    Constants.TOPIC to data.topic
                )
            )
        }

        binding.bannerVideoPageDoubtFeed.ivDoubtFeedClose.setOnClickListener {
            binding.bannerVideoPageDoubtFeed.root.hide()
        }

        videoPageViewModel.sendEvent(
            EventConstants.DF_MPVP_BANNER_VIEWED, hashMapOf(
                Constants.TOPIC to data.topic
            ), ignoreSnowplow = true
        )

        videoPageViewModel.sendEvent(
            EventConstants.DG_MPVP_BANNER_VIEWED, hashMapOf(
                Constants.TOPIC to data.topic
            )
        )
    }

    private fun sendMoEngageEvent(viewAnswerData: ViewAnswerData) {
        viewAnswerData.moeEventMap?.forEach {
            MoEngageUtils.setUserAttribute(this, it.key, it.value)
        }
        val eventMap = hashMapOf<String, Any>().apply {
            put(
                EventConstants.VIDEO_CONTENT_TYPE, if (viewAnswerData.isDnVideo)
                    EventConstants.FREE_VIDEO_CONTENT
                else
                    EventConstants.PAID_VIDEO_CONTENT
            )
            put(EventConstants.QUESTION_ID, viewAnswerData.questionId)
            put(EventConstants.PAGE, page)
            put(EventConstants.VIDEO_VIEW_ID, viewAnswerData.viewId)
        }
        analyticsPublisher.publishMoEngageEvent(
            AnalyticsEvent(
                EventConstants.EVENT_VIDEO_WATCHED,
                eventMap
            )
        )

        if (!viewAnswerData.eventVideoType.isNullOrBlank()) {
            analyticsPublisher.publishMoEngageEvent(
                AnalyticsEvent(
                    viewAnswerData.eventVideoType,
                    eventMap
                )
            )
        }
    }

    private fun sendEvent(eventName: String) {
        this@VideoPageActivity.apply {
            (this@VideoPageActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@VideoPageActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_VIDEO_VIEW_ACTIVITY)
                .track()
        }
    }

    private fun sendEventByQid(eventName: String, qid: String) {
        this@VideoPageActivity.apply {
            (this@VideoPageActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@VideoPageActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_VIDEO_VIEW_ACTIVITY)
                .addEventParameter(Constants.PAGE, page)
                .addEventParameter(Constants.QUESTION_ID, qid)
                .addEventParameter(Constants.STUDENT_CLASS, getStudentClass())
                .track()
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is PlayVideo -> {
                if (questionId != action.videoId) {
                    playNextPlaylistVideo(
                        action.videoId
                    )
                    videoPageViewModel.sendBottomSheetEvent(
                        EventConstants.MPVP_BOTTOM_SHEET_VIDEO_SELECTED,
                        ignoreSnowplow = true
                    )
                } else {
                    toast("Currently playing!")
                }
            }
            else -> videoPageViewModel.handleAction(action, playlistId ?: "", page)
        }
    }

    private fun changeVisibilityOfVideoActionButtons(isVisible: Boolean) {
        binding.btnLike.setVisibleState(isVisible)
        binding.tvLikeCount.setVisibleState(isVisible)
        binding.btnDislike.setVisibleState(isVisible)
        binding.tvDisLikeCount.setVisibleState(isVisible)
        binding.btnComment.setVisibleState(isVisible)
        binding.tvCommentCount.setVisibleState(isVisible)
        binding.btnShare.setVisibleState(isVisible)
        binding.tvShareCount.setVisibleState(isVisible)
        binding.btnAddPlaylist.setVisibleState(isVisible)
        binding.tvSaveVideo.setVisibleState(isVisible)
    }

    private fun scheduleCommentPopup(startTimeInMillis: Long, endTimeInMillis: Long) {
        val currentTimeInMillis = System.currentTimeMillis()
        if (endTimeInMillis < currentTimeInMillis) return
        val delay = if (startTimeInMillis <= currentTimeInMillis) {
            1L
        } else {
            startTimeInMillis - currentTimeInMillis
        }
        timerDisposable.add(
            Observable.timer(delay, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        updateCommentView(true)
                        timerDisposable.clear()
                        if (requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            && !entityId.isNullOrBlank() && !entityType.isNullOrBlank()
                        ) {
                            onCommentButtonClicked(
                                entityId!!,
                                entityType!!,
                                entityPosition,
                                batchId
                            )
                            videoPageViewModel.onVideoCommentEvent(questionId.toString())
                            sendEventByQid(
                                EventConstants.EVENT_NAME_COMMENT_ICON_CLICK,
                                questionId.toString()
                            )
                        }
                    }

                    override fun onNext(t: Long) {

                    }

                    override fun onError(e: Throwable) {
                        timerDisposable.clear()
                        updateCommentView(true)
                    }
                })
        )
    }

    private fun updateCommentView(state: Boolean) {
        binding.tvCommentCount.text =
            if (state) getString(R.string.string_comment) else getString(R.string.string_comment_disabled)
        binding.btnComment.isClickable = state
        binding.btnComment.isEnabled = state
    }

    private fun handleAppIndexingEvent() {
        if (!isAppIndexingEventSent && intent.data != null) {
            isAppIndexingEventSent = true
            videoPageViewModel.sendEvent(
                EventConstants.EVENT_NAME_APP_OPEN_DN,
                hashMapOf(EventConstants.SOURCE to Constants.APP_INDEXING),
                true
            )

            Utils.sendClassLangEvents(EventConstants.EVENT_NAME_APP_OPEN_DN)
            Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_APP_OPEN_DN)

            DoubtnutApp.INSTANCE.getEventTracker()
                .addEventNames(EventConstants.EVENT_NAME_APP_OPEN_DN)
                .addEventParameter(EventConstants.SOURCE, Constants.APP_INDEXING)
                .addStudentId(getStudentId())
                .track()

            analyticsPublisher.publishEvent(
                StructuredEvent(category = EventConstants.EVENT_NAME_APP_OPEN_DN_CATEGORY,
                    action = EventConstants.EVENT_NAME_APP_OPEN_DN,
                    eventParams = hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, Constants.APP_INDEXING)
                    })
            )

//            DoubtnutApp.INSTANCE.analyticsPublisher.get().publishBranchIoEvent(
//                AnalyticsEvent(
//                    EventConstants.EVENT_NAME_APP_OPEN_DN,
//                    hashMapOf<String, Any>().apply {
//                        put(EventConstants.SOURCE, Constants.APP_INDEXING)
//                    })
//            )

            val countToSendEvent: Int =
                Utils.getCountToSend(RemoteConfigUtils.getEventInfo(), EventConstants.SESSION_START)
            repeat((0 until countToSendEvent).count()) {
                sendEvent(EventConstants.SESSION_START)
            }
        }
    }

    private val playerTypeOrMediaTypeChangedListener:
            PlayerTypeOrMediaTypeChangedListener = { playerType, mediaType ->
        if (playerType == PLAYER_TYPE_YOUTUBE) {
            binding.bannerVideoImageSummary.hide()
            isVideoImageSummaryAvailable = false
        } else {
            if (viewAnswerData?.textSolutionLink != null) {
                isVideoImageSummaryAvailable = true
                setUpViewTextSolutionButton(
                    viewAnswerData?.textSolutionLink.orEmpty(),
                    viewAnswerData?.questionId.orEmpty()
                )
            } else {
                isVideoImageSummaryAvailable = false
                binding.bannerVideoImageSummary.hide()
            }
        }
    }

//    private val openWebViewOnVideoFail: OpenWebViewOnVideoFail = {
//        openWebView()
//    }

    private fun sendVideoPlaybackFailedEvent(e: Throwable? = null, reason: String? = null) {
        videoPageViewModel.sendEvent(
            EventConstants.NO_VIDEO_PLAYED_IN_LIST, hashMapOf(
                Constants.CAUSE to (e?.cause?.toString() ?: reason.toString()),
                Constants.MESSAGE to (e?.message ?: reason.toString()),
                Constants.IS_API_ERROR to true,
                EventConstants.VIDEO_VIEW_ID to viewId,
                EventConstants.EVENT_NAME_ID to questionId.orEmpty(),
                Constants.PAGE to page
            ), ignoreSnowplow = true
        )
    }

    private fun sendBranchEvents() {
        userSelectedExamsList = userPreference.getUserSelectedExams().split(",")
        if (UserUtil.checkIsIITExamAnd11to13ClassUser(userSelectedExamsList)) {
//            analyticsPublisher.publishBranchIoEvent(
//                AnalyticsEvent(
//                    EventConstants.VIDEO_WATCH_IIT_11_TO_13,
//                    hashMapOf()
//                )
//            )
        }
        if (!isPlayedFromTheBackStack) {
            if (checkisClass9to12User()) {
                val countToSendEvent: Int =
                    Utils.getCountToSend(
                        RemoteConfigUtils.getEventInfo(),
                        EventConstants.VIDEO_WATCH_9_TO_12
                    )
//                repeat((0 until countToSendEvent).count()) {
//                    analyticsPublisher.publishBranchIoEvent(
//                        AnalyticsEvent(
//                            EventConstants.VIDEO_WATCH_9_TO_12,
//                            hashMapOf()
//                        )
//                    )
//                }
            }
            if (userSelectedExamsList.contains("IIT JEE")) {
                val countToSendEvent: Int =
                    Utils.getCountToSend(
                        RemoteConfigUtils.getEventInfo(),
                        EventConstants.VIDEO_WATCH_IIT
                    )
//                repeat((0 until countToSendEvent).count()) {
//                    analyticsPublisher.publishBranchIoEvent(
//                        AnalyticsEvent(
//                            EventConstants.VIDEO_WATCH_IIT,
//                            hashMapOf()
//                        )
//                    )
//                }
            }
            if (userSelectedExamsList.contains("NEET")) {
                val countToSendEvent: Int =
                    Utils.getCountToSend(
                        RemoteConfigUtils.getEventInfo(),
                        EventConstants.VIDEO_WATCH_NEET
                    )
//                repeat((0 until countToSendEvent).count()) {
//                    analyticsPublisher.publishBranchIoEvent(
//                        AnalyticsEvent(
//                            EventConstants.VIDEO_WATCH_NEET,
//                            hashMapOf()
//                        )
//                    )
//                }
            }
        }
    }

    private fun sendBranchVideoWatchEvent() {
        if (!isPlayedFromTheBackStack) {
            if (page.equals("COURSE_DETAIL", true)
                || page.equals("COURSE_LANDING", true)
                || page.equals("COURSE_RESOURCE", true)
                || page.equals("HOME_FEED_LIVE", true)
                || page.equals("LIVECLASS_NOTIFICATION", true)
            ) {
                analyticsPublisher.publishBranchIoEvent(
                    AnalyticsEvent(
                        getStudentClass() + EventConstants.UNDERSCORE +
                                page + EventConstants.UNDERSCORE
                                + EventConstants.EVENT_VIDEO_WATCH, hashMapOf()
                    )
                )
            }
        }
    }

    override fun onUserLeaveHint() {
        tryToEnterInPictureInPictureMode()
    }

    /**
     * Enters Picture-in-Picture mode.
     */
    @SuppressLint("NewApi")
    private fun tryToEnterInPictureInPictureMode() {
        // Wrapping in try/catch as even when [canEnterPipMode()] returns true, we observe issues
        // where activity can't enter in PiP mode and till now, we've been unable to trace the cause of
        // why this might be happening
        try {
            if (canEnterPipMode() && videoPlayerManager?.canEnterInPIP() == true) {
                if (Utils.hasPipModePermission()) {
                    // Hide the controls in picture-in-picture mode.
                    // Calculate the aspect ratio of the PiP screen.
                    videoPlayerManager?.hidePlayerController(true)

                    // Hide top header - video title in case of NCERT experiment
                    binding.topLayout.hide()
                    // Hide ncert Book Bottom Sheet - in case of NCERT experiment
                    ncertBooksBottomSheet?.dismiss()

                    mPictureInPictureParamsBuilder?.let {
                        val videoContainerRect = Rect()
                        binding.videoContainer.getDrawingRect(videoContainerRect)
                        binding.contentView.offsetDescendantRectToMyCoords(
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
                    videoPageViewModel.sendEvent(
                        EventConstants.PIP_MODE_PERMISSION_ALLOWED,
                        ignoreSnowplow = true
                    )
                } else {
                    videoPageViewModel.sendEvent(
                        EventConstants.PIP_MODE_PERMISSION_DENIED,
                        ignoreSnowplow = true
                    )
                }
            }
        } catch (e: Exception) {
            com.doubtnutapp.Log.e(e, TAG_PIP)
        }
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean, newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)

        binding.ivBackFromVideo.isVisible = isInPictureInPictureMode.not()
        videoPlayerManager?.controllerAutoShow(isInPictureInPictureMode.not())
        videoPlayerManager?.setPipStatus(isInPictureInPictureMode)
        // Try-catch to monitor BroadcastReceiver unregister exceptions without app crash
        try {
            if (isInPictureInPictureMode) {
                binding.bannerPdfDownloadBeforeAutoplayContainer.hide()
                subscribeToRxBus()
                registerReceiver(mPipActionBroadcastReceiver, IntentFilter(ACTION_MEDIA_CONTROL))
                mPipModeExitListener.pipEntered()
                mPipModeLastEnterTimeMillis = System.currentTimeMillis()
                videoPageViewModel.sendEvent(EventConstants.PIP_MODE_ENABLED)
            } else {
                // Make sure that events are sent before anything bad happens
                mPipModeExitListener.pipExited()

                // Show top header - video title in case of NCERT experiment
                binding.topLayout.isVisible = isNcertExperimentEnabled == true
                binding.ivBackFromVideo.isVisible = isNcertExperimentEnabled != true

                videoPageViewModel.sendEvent(
                    EventConstants.PIP_MODE_VIEWED, hashMapOf(
                        Constants.VIDEO_PLAY_TIME to getAndResetPipModeWatchTime(),
                        Constants.QUESTION_ID to questionId.orEmpty(),
                        Constants.VIEW_ID to viewId
                    ), ignoreSnowplow = true
                )
                unregisterReceiver(mPipActionBroadcastReceiver)
                mPipCloseDisposable?.dispose()
            }
        } catch (e: Exception) {
            com.doubtnutapp.Log.e(e, TAG_PIP)
        }
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

    private fun getAndResetPipModeWatchTime(): Long {
        val sessionWatchTime = System.currentTimeMillis() - mPipModeLastEnterTimeMillis
        mPipModeLastEnterTimeMillis = -1
        return sessionWatchTime
    }

    private fun updatePictureInPictureActions() {
        try {
            if (deviceSupportsPipMode() && (isInPipMode || canEnterPipMode())) {
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

                if (mShowNextButtonInPipMode) {
                    actions.add(
                        RemoteAction(
                            Icon.createWithResource(this, R.drawable.exo_controls_next),
                            labelNext, labelNext,
                            PendingIntent.getBroadcast(
                                this,
                                REQUEST_NEXT,
                                Intent(ACTION_MEDIA_CONTROL).putExtra(
                                    EXTRA_CONTROL_TYPE,
                                    CONTROL_TYPE_NEXT
                                ),
                                0
                            )
                        )
                    )
                }

                mPictureInPictureParamsBuilder?.let {
                    it.setActions(actions)
                    setPictureInPictureParams(it.build())
                }
            }
        } catch (e: Exception) {
            com.doubtnutapp.Log.e(e, TAG_PIP)
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

    private fun closeConvivaSession() {
        // close conviva session
        /*ThreadUtils.runOnAnalyticsThread {
            convivaVideoAnalytics.get().reportPlaybackEnded()
        }*/
    }

    private fun liveClassActivityEnteredPip(): Boolean =
        (getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager)?.appTasks?.any { task ->
            task.taskInfo.baseActivity?.className == LiveClassActivity::class.java.name
        } ?: false

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
                binding.ivBackFromVideo.hide()
                return
            }
        }
        startPositionInSeconds = position / 1000
    }
}
