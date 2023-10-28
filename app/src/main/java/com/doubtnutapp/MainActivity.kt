package com.doubtnutapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils.isToday
import android.view.LayoutInflater
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.IdRes
import androidx.annotation.NonNull
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.view.GravityCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.data.*
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.entitiy.CoreAnalyticsEvent
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.toast
import com.doubtnut.referral.ui.ReferralActivityV2
import com.doubtnutapp.EventBus.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.appexitdialog.ui.AppExitDialogFragment
import com.doubtnutapp.base.extension.getDatabase
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.bottomnavigation.model.BottomNavigationTabsData
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.common.UserProfileData
import com.doubtnutapp.config.SyncConfigDataWorker
import com.doubtnutapp.course.widgets.StudyDostWidget
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.camerascreen.datasource.LocalConfigDataSource
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.homefeed.interactor.ConfigData
import com.doubtnutapp.fallbackquiz.fallbackjob.FallbackReceiver
import com.doubtnutapp.fallbackquiz.worker.FallbackQuizWorkerHelper
import com.doubtnutapp.faq.ui.FaqActivity
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.fcm.notification.NotificationConstants.PENDING_INTENT_ALARM_10PM
import com.doubtnutapp.feed.view.FeedFragment
import com.doubtnutapp.feed.viewmodel.FeedViewModel
import com.doubtnutapp.gamification.mybio.ui.MyBioActivity
import com.doubtnutapp.gamification.settings.profilesetting.ui.ProfileSettingActivity
import com.doubtnutapp.home.HomeFeedFragmentV2
import com.doubtnutapp.home.WebViewBottomSheetFragment
import com.doubtnutapp.inappupdate.InAppUpdateManager
import com.doubtnutapp.libraryhome.library.LibraryFragmentHome
import com.doubtnutapp.libraryhome.library.ui.adapter.LibraryTabAdapter
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.login.ui.fragment.AnonymousLoginBlockerFragment
import com.doubtnutapp.login.ui.fragment.ChangeLoginPinDialogFragment
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.notification.NotificationCenterActivity
import com.doubtnutapp.profile.userprofile.UserProfileFragment
import com.doubtnutapp.reward.receiver.AttendanceMarkedReceiver
import com.doubtnutapp.reward.receiver.LoginRewardReceiver
import com.doubtnutapp.reward.receiver.RewardNotificationReceiver
import com.doubtnutapp.reward.receiver.RewardRepeatedNotificationReceiver
import com.doubtnutapp.reward.ui.RewardActivity
import com.doubtnutapp.scheduledquiz.ScheduledNotificationWorkManagerHelper
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.transactionhistory.TransactionHistoryActivityV2
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.contest.ContestListActivity
import com.doubtnutapp.ui.feedback.FeedbackFragment
import com.doubtnutapp.ui.feedback.NPSFeedbackFragment
import com.doubtnutapp.ui.main.MatchQuestionDialog
import com.doubtnutapp.ui.quiz.EvernoteUtils
import com.doubtnutapp.ui.quiz.FetchQuizJob
import com.doubtnutapp.ui.quiz.FetchQuizJobWorker
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.wallet.WalletActivity
import com.doubtnutapp.widgets.mathview.ASCIIMathView
import com.doubtnutapp.workmanager.WorkManagerHelper
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.branch.referral.Defines
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.navigation_view.*
import kotlinx.android.synthetic.main.navigation_view.view.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MainActivity : BaseActivity(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var inAppUpdateManager: InAppUpdateManager

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var quizNotificationDatastore: QuizNotificationDataStore

    @Inject
    lateinit var defaultDataStore: DefaultDataStore

    @Inject
    lateinit var bottomNavIconsNotificationsDataStore: BottomNavIconsNotificationDataStore

    lateinit var navigationController: NavController

    private lateinit var viewModel: MainViewModel
    private lateinit var widgetPlanButtonVM: WidgetPlanButtonVM

    private lateinit var eventTracker: Tracker

    private var busDisposable: Disposable? = null

    private var isBranchLink = false

    private var database: DoubtnutDatabase? = null

    private var currentFragment = Fragment()

    private var changeLoginPinDialogFragment: ChangeLoginPinDialogFragment? = null
    private var firstPageDeeplink: String = ""
    private var popupDeeplink: String = ""
    private var defaultOnboardingDeeplink: String? = null

    private var lastSelectedTabKey = ""
    private var lastSelectedBottomTab = ""

    private val cameraScreenResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data ?: return@registerForActivityResult
                val screenName = data.getStringExtra(Constants.SCREEN_NAME)
                if (screenName == CameraActivity.TAG) {
                    val shouldClose =
                        data.getBooleanExtra(CameraActivity.FINISH_CALLING_ACTIVITY, false)
                    if (shouldClose) {
                        finish()
                    } else {
                        // Pass Apxor event
                        viewModel.publishEventWith(EventConstants.BACK_FROM_CAMERA)
                        viewModel.shouldShowDnrRewardData(true)
                    }
                }
            }
        }

    companion object {
        const val TAG = "MainActivity"
        private const val BOTTOM_NAVIGATION_POSITION_HOME = 0
        private const val BOTTOM_NAVIGATION_POSITION_LIBRARY = 1
        private const val BOTTOM_NAVIGATION_POSITION_DOUBT = 2
        private const val BOTTOM_NAVIGATION_POSITION_FORUM = 3
        private const val BOTTOM_NAVIGATION_POSITION_PROFILE = 4
        private const val REQUEST_CODE_CHNAGE_LANGUAGE = 111
        private const val REQUEST_CODE_CHANGE_CLASS = 101
        private const val MAX_IN_APP_MATCH_DIALOG_COUNT = 3

        const val KEY_RECREATE = "recreate"
        private const val KEY_OPEN_HOME_PAGE = "open_home_page"
        const val KEY_SCROLL_TO_ID = "scroll_to_id"

        fun getStartIntent(
            context: Context,
            recreate: Boolean = false,
            showCamera: Boolean = true,
            doOpenHomePage: Boolean = false,
            scrollToId: String? = null,
        ): Intent {
            return Intent(context, MainActivity::class.java).apply {
                // TODO: 25/3/21 Unused. Camera open always
                if (showCamera && defaultPrefs(context).getLong(
                        Constants.PREF_KEY_CAMERA_SCREEN_SHOWN_COUNT,
                        0
                    )
                    < RemoteConfigUtils.getCameraCountToShow()
                ) {
                    putExtra("hasToShowCamera", true)
                }
                putExtra(KEY_RECREATE, recreate)
                putExtra(KEY_OPEN_HOME_PAGE, doOpenHomePage)
                putExtra(KEY_SCROLL_TO_ID, scrollToId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        FirebaseCrashlytics.getInstance()
            .setUserId(getStudentId())
        inAppUpdateManager.isFlexibleUpdateEnabled = true
        statusbarColor(this, android.R.color.transparent)
        setContentView(R.layout.activity_home)
        KeyboardUtils.hideKeyboard(currentFocus ?: View(this))
        eventTracker = getTracker()
        setupAndroidJob()
        init()
        setObserver()

        firstPageDeeplink = defaultPrefs().getString(Constants.FIRST_PAGE_DEEPLINK, "").orEmpty()
        popupDeeplink = defaultPrefs().getString(Constants.POPUP_DEEPLINK, "").orEmpty()
        defaultOnboardingDeeplink =
            defaultPrefs().getString(Constants.DEFAULT_ONBOARDING_DEEPLINK, null)

        handleBranchDeepLink()
        navigationController = findNavController(R.id.nav_host_fragment)

        // only set up bottomNav with navController if back-end driven
        // bottom nav response is not available.
        lifecycleScope.launchWhenStarted {
            val bottomNavIconsDataJson =
                defaultDataStore.bottomNavigationIconsData.firstOrNull() ?: ""

            if (!Utils.isBottomNavigationIconsApiDataAvailable(
                    bottomNavIconsDataJson
                )
            ) {
                NavigationUI.setupWithNavController(bottomNavigationView, navigationController)
            }
        }

        setDataFromIntent(intent)
        viewModel.getNotices()
        viewModel.getUserProfile()

        viewModel.getUserBanStatus()

        val newSession = intent.getBooleanExtra(Constants.NEW_SESSION, true)
        if (newSession && getStudentId() != "" && authToken(this) != "") {
            DataHandler.INSTANCE.notificationRepository.getPendingEvents().observe(this) {
                when (it) {
                    is Outcome.Success -> {
                        try {
                            DoubtnutApp.INSTANCE.runOnDifferentThread {
                                getDatabase()
                                    ?.eventsDao()
                                    ?.insertAllEvents(it.data.data.toDBAppInappFeedback())
                            }

                            if (it.data.data.isNullOrEmpty()) {
                                viewModel.checkDataForWebViewBottomSheet(getStudentClass())

                                // Show in app match result dialog when flagr is enabled
                                viewModel.getLatestMatchResultFromDb()
                            }

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    else -> {
                    }
                }
            }
        }
        if (getStudentId() != "" && authToken(this) != "") {
            getActiveFeedbacks()
            addActiveFeedbackObservers()
        }

        Log.d(EventConstants.EVENT_NAME_SCREEN_STATE_MAIN_ACTIVITY_PAGE)

        viewModel.isProfileButtonClicked()
        viewModel.isLibraryButtonClicked()
        viewModel.isForumButtonClicked()
        viewModel.updateClassAndLanguage()
        viewModel.clearExoPlayerCaches()

        navigationController.addOnDestinationChangedListener { controller, destination, arguments ->
            Utils.saveIsEmulatorAndSafetyNetResponseToPref()
            viewModel.checkForLevel(destination.label.toString())
            widgetPlanButtonVM.updateWidgetViewPlanButtonModel(null)
            widgetPlanButtonVM.updateWidgetViewPlanButtonVisibility(false)
        }

        viewModel.updateUserprofile()
        setUpNavigationView()
        handleEtoosContentRegisteredEvent()

        warmupMathView()
        setLibraryTabText()
        updateSeenNotifications()
        setFallbackPushAlarm()

        //region Attendance reward
        markAttendance()
        sendRewardNotifTenMinutes()
        sendScheduledRewardNotifications()
        busDisposable = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            when (it) {
                is GetMarkAttendancePopup -> {
                    viewModel.getManualAttendancePopupData()
                }
                is OpenRewardActivity -> {
                    startActivity(RewardActivity.getStartIntent(this))
                }
                is MarkAttendance -> {
                    viewModel.markDailyAttendance()
                }
                is UncheckAllBottomNavItems -> {
                    if (it.uncheck && showDoubtFeed()) {
                        bottomNavigationView.uncheckAllItems()
                    }
                }
            }
        }
        //endregion

        WorkManagerHelper(applicationContext).assignViewStatusUpdateWorker()
        FallbackQuizWorkerHelper(applicationContext).assignWorkForFallbackQuiz(applicationContext)
        ScheduledNotificationWorkManagerHelper(applicationContext).assignPeriodicWork(
            applicationContext
        )

        showNotificationBadgeForBottomNav()
        handleBottomNavItemSelectedListener()

        if (intent.action == null) {
            var navSource = intent.getStringExtra("nav_source").orEmpty()
            if (navSource.isBlank()) {
                navSource = "default"
            }
            sendHomePageOpenEvent(navSource)
        }
    }

    private fun sendHomePageOpenEvent(navSource: String) {
        analyticsPublisher.publishEvent(CoreAnalyticsEvent("home_page_open_v2", hashMapOf<String, Any>().apply {
            put("nav_source", navSource)
            put("last_selected_bottom_tab", lastSelectedBottomTab)
        }))
    }

    fun setLibraryTabText() {
        lifecycleScope.launchWhenStarted {
            val bottomNavIconsData = defaultDataStore.bottomNavigationIconsData.firstOrNull()
            if (bottomNavIconsData.isNullOrEmpty()) {
                val bottomLibraryText = Utils.getLibraryBottomText(this@MainActivity)
                if (!bottomLibraryText.isNullOrBlank()) {
                    val menu = bottomNavigationView.menu
                    menu.findItem(R.id.libraryFragment).title = bottomLibraryText
                }
            }
        }
    }

    private fun updateSeenNotifications() {
        viewModel.updateClickedNotifications()
    }

    /**
     * MathView is a webview that depends on mathjax js, this js is fetched through a cdn and then
     * cached by the webview, the first load is very slow due to this network request, we init the webview
     * here which loads the mathjax template html and the js is pre cached.
     *
     * Update #1: Data from CDN is cached in disk thus calling once in a lifetime.
     */
    private fun warmupMathView() {
        if (defaultPrefs().getBoolean(Constants.IS_MATH_WEB_VIEW_WARMED_UP, false).not()) {
            defaultPrefs().edit {
                putBoolean(Constants.IS_MATH_WEB_VIEW_WARMED_UP, true)
            }
            ASCIIMathView(this)
        }
    }

    private fun setUpNavigationView() {
        drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        setupMobileNumberNavigationView()

        setupBoardExamsNavigationView()

        navigationView.changeClassView.setOnClickListener {
            closeDrawer()
            defaultPrefs().edit().putString(Constants.FIRST_PAGE_DEEPLINK, "").apply()
            viewModel.publishEventWith(
                EventConstants.CHANGE_CLASS_FROM_DRAWER,
                hashMapOf(), ignoreSnowplow = true
            )
            onClassChangeRequest()
        }

        navigationView.questionAskedHistoryView.setVisibleState(true)
        navigationView.textViewQuestionAskedHistory.setVisibleState(true)
        navigationView.imageViewQuestionAskedHistory.setVisibleState(true)

        navigationView.changePinView.setOnClickListener {
            closeDrawer()
            changeLoginPinDialogFragment = ChangeLoginPinDialogFragment.newInstance()
            changeLoginPinDialogFragment?.show(
                supportFragmentManager,
                ChangeLoginPinDialogFragment.TAG
            )
        }

        navigationView.changeLanguageView.setOnClickListener {
            closeDrawer()
            screenNavigator.startActivityFromActivity(
                this, ChangeLanguageScreen,
                null
            )
            viewModel.publishEventWith(
                EventConstants.CHANGE_LANGUAGE_CLICK,
                hashMapOf(), ignoreSnowplow = true
            )
        }

        navigationView.shareAndSaveView.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.REFERRAL_SHARE_CLICK, hashMapOf(
                        EventConstants.SOURCE to EventConstants.HAMBURGER
                    ), ignoreSnowplow = true
                )
            )
            closeDrawer()
            startActivity(ReferralActivityV2.getStartIntent(this))

            viewModel.publishEventWith(
                EventConstants.SHARE_AND_SAVE_CLICKED,
                hashMapOf(), ignoreSnowplow = true
            )
        }

        navigationView.view_free_classes.setOnClickListener {
            closeDrawer()
            val intent = Intent(this@MainActivity, MainActivity::class.java).also {
                it.action = Constants.NAVIGATE_LIBRARY
                val selectedTab = "0"
                val mSource = ""
                it.putExtra(Constants.LIBRARY_SCREEN_SELECTED_TAB, selectedTab)
                it.putExtra(Constants.SOURCE, mSource)
                it.putExtra(Constants.TAG, "free_classes")
                it.putExtra(
                    MainActivity.KEY_RECREATE, true
                )
            }
            startActivity(intent)
            viewModel.publishEventWith(
                eventName = EventConstants.FREE_CLASSES + "_" + EventConstants.CLICKED,
                params = hashMapOf(
                    EventConstants.SOURCE to EventConstants.EVENT_NAME_HAMBURGER_MENU
                ), ignoreSnowplow = true
            )
        }

        navigationView.historyView.setOnClickListener {
            closeDrawer()
            screenNavigator.startActivityFromActivity(
                this, UserVideoHistoryScreen,
                null
            )
            viewModel.publishEventWith(
                EventConstants.VIDEO_WATCHED_CLICK, hashMapOf(
                    EventConstants.SOURCE to EventConstants.EVENT_NAME_HAMBURGER_MENU
                ), ignoreSnowplow = true
            )
        }

        navigationView.questionAskedHistoryView.setOnClickListener {
            closeDrawer()
            screenNavigator.startActivityFromActivity(this, QuestionAskedHistoryScreen, null)
        }

        navigationView.myPdfView.setOnClickListener {
            closeDrawer()
            screenNavigator.startActivityFromActivity(
                this, MyPdfScreen,
                null
            )
            viewModel.publishEventWith(EventConstants.MY_PDF_CLICK)
        }

        navigationView.savedPlaylistView.setOnClickListener {
            closeDrawer()
            screenNavigator.startActivityFromActivity(
                this, LibraryPlayListScreen, hashMapOf(
                    LibraryListingActivity.INTENT_EXTRA_PLAYLIST_ID to "0",
                    LibraryListingActivity.INTENT_EXTRA_PLAYLIST_TITLE to getString(R.string.saved_playlist)
                ).toBundle()
            )
            viewModel.publishEventWith(EventConstants.MY_PLAYLIST_CLICK)
        }

        navigationView.watchLaterView.setOnClickListener {
            closeDrawer()
            screenNavigator.startActivityFromActivity(
                this, LibraryVideoPlayListScreen, hashMapOf(
                    SCREEN_NAV_PARAM_PLAYLIST_ID to "1",
                    SCREEN_NAV_PARAM_PLAYLIST_TITLE to getString(R.string.watch_later)
                ).toBundle()
            )
            viewModel.publishEventWith(
                EventConstants.WATCH_LATER_CLICK,
                hashMapOf(),
                ignoreSnowplow = true
            )
        }

        navigationView.gamesView.setOnClickListener {
            closeDrawer()
            screenNavigator.startActivityFromActivity(
                this, DnGamesScreen,
                null
            )
            viewModel.publishEventWith(
                EventConstants.EVENT_GAME_SECTION_VIEW, hashMapOf(
                    EventConstants.SOURCE to EventConstants.EVENT_NAME_HAMBURGER_MENU
                ), ignoreSnowplow = true
            )
        }

        navigationView.groupRewardView.show()
        navigationView.rewardView.setOnClickListener {
            closeDrawer()
            startActivity(RewardActivity.getStartIntent(this))
            viewModel.publishRewardSystemEvent(
                EventConstants.REWARD_PAGE_OPEN_SOURCE_HAMBURGER,
                ignoreSnowplow = false
            )
        }
        addBadgeToRewardsIcon()

        navigationView.settingView.setOnClickListener {
            closeDrawer()
            startActivity(Intent(this, ProfileSettingActivity::class.java))
            viewModel.publishEventWith(
                EventConstants.EVENT_NAME_SETTING_BUTTON_CLICK,
                hashMapOf(),
                ignoreSnowplow = true
            )
        }

        if (defaultPrefs(this).getBoolean(Constants.IS_VIP, false) || defaultPrefs(this).getBoolean(
                Constants.IS_TRIAL,
                false
            )
        ) {
            navigationView.faqView.visibility = View.VISIBLE
            navigationView.imageViewFaq.visibility = View.VISIBLE
            navigationView.textViewFaq.visibility = View.VISIBLE
            navigationView.faqView.setOnClickListener {
                closeDrawer()
                startActivity(Intent(this, FaqActivity::class.java))
            }
        } else {
            navigationView.faqView.visibility = View.GONE
            navigationView.imageViewFaq.visibility = View.GONE
            navigationView.textViewFaq.visibility = View.GONE
        }

        navigationView.paymentHistoryView.setOnClickListener {
            closeDrawer()
            startActivity(Intent(this, TransactionHistoryActivityV2::class.java))
            viewModel.publishTransactionEventClick(EventConstants.EVENT_NAME_HAMBURGER_MENU)
        }

        navigationView.viewProfileView.setOnClickListener {
            closeDrawer()
            if (::navigationController.isInitialized) {
                val params = Bundle()
                params.putString(Constants.SOURCE, EventConstants.EVENT_NAME_HAMBURGER_MENU)
                navigationController.navigate(R.id.userProfileFragment, params)
                setSelectedTabId(Constants.KEY_PROFILE)
            }
            viewModel.publishEventWith(
                EventConstants.PROFILE_CLICK,
                hashMapOf(),
                ignoreSnowplow = true
            )
        }

        navigationView.textViewEditBio.setOnClickListener {
            closeDrawer()
            goToEditBio()
        }

        navigationView.videoWatchedToday.setOnClickListener {
            closeDrawer()
            startActivity(Intent(this, ContestListActivity::class.java))
            viewModel.publishEventWith(
                EventConstants.DRAWER_CONTEST_CLICK,
                hashMapOf(), ignoreSnowplow = true
            )
        }

        navigationView.walletView.setOnClickListener {
            startActivity(WalletActivity.getStartIntent(this))
            viewModel.publishEventWith(
                EventConstants.DRAWER_WALLET_CLICK,
                hashMapOf(), ignoreSnowplow = true
            )
        }

        navigationView.studyDostView.setOnClickListener {
            when (defaultPrefs().getInt(Constants.STUDY_DOST_LEVEL, -1)) {
                StudyDostWidget.LEVEL_0 -> {
                    viewModel.requestForStudyDost()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.STUDY_DOST_REQUESTED, hashMapOf(
                                EventConstants.SOURCE to "Navigation"
                            )
                        )
                    )
                }
                StudyDostWidget.LEVEL_2, StudyDostWidget.LEVEL_3 -> {
                    openChatActivity()
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.STUDY_DOST_START_CHAT_CLICKED, hashMapOf(
                                EventConstants.SOURCE to "Navigation"
                            )
                        )
                    )
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.STUDY_DOST_START_CHAT_CLICKED, hashMapOf(
                                EventConstants.SOURCE to "Navigation"
                            )
                        )
                    )
                }
            }
            closeDrawer()
        }

        navigationView.doubtP2pView.setOnClickListener {
            val deeplink = viewModel.getDoubtP2pData()?.deeplink
            viewModel.publishEventWith(
                EventConstants.P2P_ICON_CLICKED, hashMapOf(
                    EventConstants.SOURCE to EventConstants.HAMBURGER
                ), ignoreSnowplow = true
            )
            if (deeplink.isNullOrEmpty().not()) {
                deeplinkAction.performAction(this, deeplink)
            }
            closeDrawer()
        }

        navigationView.studyGroupView.setOnClickListener {
            val studyGroupData = userPreference.getStudyGroupData() ?: return@setOnClickListener
            deeplinkAction.performAction(this@MainActivity, studyGroupData.deeplink)
            closeDrawer()
        }

        fabResetCoreActions.setOnClickListener {
            viewModel.resetCoreActions {
                toast(it.message)
            }
        }

        fabResetAppOpenCount.setOnClickListener {
            viewModel.resetAppOpenCount {
                CountingManager.resetAppOpenCount(this)
                toast(it.message)
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    private fun addBadgeToRewardsIcon() {
        if (navigationView.groupRewardView.isVisible && defaultPrefs().getInt(
                Constants.UNSCRATCHED_CARD_COUNT,
                0
            ) > 0
        ) {
            navigationView.imageViewReward.viewTreeObserver.addOnGlobalLayoutListener {
                val badgeDrawable = BadgeDrawable.create(this)
                badgeDrawable.isVisible = true

                val horizontalShiftPx = 10.dpToPx()
                val verticalShiftPx = 9.dpToPx()
                badgeDrawable.horizontalOffset = horizontalShiftPx
                badgeDrawable.verticalOffset = verticalShiftPx
                badgeDrawable.number = defaultPrefs().getInt(Constants.UNSCRATCHED_CARD_COUNT, 0)
                BadgeUtils.attachBadgeDrawable(
                    badgeDrawable,
                    navigationView.imageViewReward,
                    navigationView.imageViewRewardContainer
                )
            }
        }
    }

    private fun onClassChangeRequest() {
        try {
            val currentFragmentInstance =
                supportFragmentManager.fragments.getOrNull(0)?.childFragmentManager?.fragments?.getOrNull(
                    0
                )
            if (currentFragmentInstance != null && currentFragmentInstance.isVisible) {
                when (currentFragmentInstance) {
                    is HomeFeedFragmentV2 -> currentFragmentInstance.requestClassSelection()
                    is LibraryFragmentHome -> currentFragmentInstance.requestClassSelection()
                    else -> {
                        screenNavigator.startActivityForResultFromActivity(
                            this, ClassSelectionScreen,
                            null,
                            REQUEST_CODE_CHANGE_CLASS
                        )
                    }
                }
            } else {
                screenNavigator.startActivityForResultFromActivity(
                    this, ClassSelectionScreen,
                    null,
                    REQUEST_CODE_CHANGE_CLASS
                )
            }
        } catch (exception: Exception) {

        }
    }

    private fun openChatActivity() {
        val deeplink = defaultPrefs().getString(Constants.STUDY_DOST_DEEPLINK, null)
        if (deeplink.isNullOrEmpty().not()) {
            deeplinkAction.performAction(this, deeplink)
        }
    }

    fun openDrawer() {
        drawer.openDrawer(GravityCompat.END)
        viewModel.getUserProfile()
        updateLoginPin()
        updateStudyDostData()
        updateStudyGroupData()
        updateDnrData()
        updateDoubtP2pData()
        updateKheloAurJeetoData()
        updateDoubtFeedData()
        updateRevisionCornerData()
        getContestData()
        updateWhatsappData()
    }

    private fun updateWhatsappData() {
        whatsappView.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.WHATSAPP_ICON_CLICKED,
                    params = hashMapOf(
                        EventConstants.SOURCE to EventConstants.HAMBURGER,
                        EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                        EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                        EventConstants.BOARD to UserUtil.getUserBoard(),
                    ),
                    ignoreFacebook = true
                )
            )
            deeplinkAction.performAction(
                this,
                defaultPrefs().getString(Constants.HAMBURGER_WHATSAPP_DEEPLINK, "")
            )
        }
        tvWhatsapp.text = defaultPrefs().getString(Constants.HAMBURGER_WHATSAPP_TEXT, "")
        ivWhatsapp.loadImageEtx(
            defaultPrefs().getString(Constants.HAMBURGER_WHATSAPP_ICON_URL, "").orEmpty()
        )
    }

    private fun updateLoginPin() {
        when (defaultPrefs().getBoolean(Constants.IS_PIN_SET, false)) {
            true -> {
                navigationView.textViewPin.isVisible = true
                navigationView.changePin.text = resources.getString(R.string.change_pin)
            }
            false -> {
                navigationView.textViewPin.isVisible = false
                navigationView.changePin.text = resources.getString(R.string.set_pin)
            }
        }
    }

    private fun getContestData() {
        viewModel.getDailyPrize("2").observe(this) { response ->
            when (response) {
                is Outcome.Success -> {
                    val count: Int = response.data.data.userDetails.count ?: 0
                    val countText = "Videos Watched \n Today: $count"
                    navigationView.textViewCountInfo.text = countText
                }
            }
        }
    }

    private fun closeDrawer() {
        drawer.closeDrawers()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        inAppUpdateManager.dispatchTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun showNewBadge(position: Int) {
        val itemCountView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        val badgeView = LayoutInflater.from(this)
            .inflate(R.layout.bottomnavigationview_badge_new, bottomNavigationView, false)

        val notificationBadge = itemCountView.getChildAt(position) as BottomNavigationItemView
        notificationBadge.addView(badgeView)

        badgeView.doOnPreDraw {
            val child = notificationBadge.getChildAt(0)
            badgeView.x = child.x + child.width / 2 + 18
            badgeView.y = child.y - 10
        }
    }

    private fun setObserver() {
        viewModel.eventNameData.observe(this) {
            sendEventByClick(it)
            if (it == EventConstants.EVENT_NAME_PROFILE_IN_BOTTOM_CLICK) {
                sendEventForHomePageCleverTap(it)
            }
        }

        viewModel.userProfileLiveData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.navigateLiveData.observe(this) {
            val navigationData = it.getContentIfNotHandled()
            if (navigationData != null) {
                val args: Bundle? = navigationData.hashMap?.toBundle()

                this.let { activityContext ->

                    if (navigationData.screen == WebViewBottomSheet && !DoubtnutApp.INSTANCE.isInAppDialogShowing) {
                        val isVideo = checkForVideoDialog()
                        val count = defaultPrefs(this).getInt(Constants.BOTTOM_SHEET_COUNT, 0)
                        val showBottomSheet =
                            defaultPrefs(this).getBoolean(Constants.SHOW_BOTTOM_SHEET, false)
                        if (count < args?.getInt(
                                WebViewBottomSheetFragment.COUNT,
                                0
                            ) ?: 0 && showBottomSheet && !isVideo
                        ) {
                            screenNavigator.openDialogFromFragment(
                                this,
                                navigationData.screen,
                                args,
                                supportFragmentManager
                            )
                            defaultPrefs(this).edit()
                                .putInt(Constants.BOTTOM_SHEET_COUNT, count + 1).apply()
                            defaultPrefs(this).edit().putBoolean(Constants.SHOW_BOTTOM_SHEET, false)
                                .apply()
                        } else {

                        }
                    }
                }
            }
        }

        viewModel.latestMatchResultFromDb.observe(this) {
            val count = defaultPrefs(this).getInt(Constants.IN_APP_MATCH_DIALOG_COUNT, 0)
            if (count < MAX_IN_APP_MATCH_DIALOG_COUNT && !DoubtnutApp.INSTANCE.isInAppDialogShowing) {
                MatchQuestionDialog.newInstance(
                    it.matchQuestion.questionId,
                    it.matchQuestion.questionImage,
                    it.matchQuestion.notificationId
                ).show(supportFragmentManager, MatchQuestionDialog.TAG)
                defaultPrefs().edit {
                    putInt(Constants.IN_APP_MATCH_DIALOG_COUNT, count + 1)
                }
            }

        }

        viewModel.loginPinSetUp.observe(this) {
            updateLoginPin()
        }

        viewModel.studyDost.observe(this) {
            updateStudyDostData()
        }

        viewModel.studyGroup.observe(this) {
            updateStudyGroupData()
        }

        viewModel.dnrData.observe(this) {
            updateDnrData()
        }

        viewModel.doubtP2p.observe(this) {
            updateDoubtP2pData()
        }

        viewModel.kheloAurJeeto.observe(this) {
            updateKheloAurJeetoData()
        }

        viewModel.doubtFeed2.observe(this) {
            updateDoubtFeedData()
        }

        viewModel.revisionCorner.observe(this) {
            updateRevisionCornerData()
        }

        viewModel.message.observe(this) {
            toast(it)
        }

        viewModel.isCourseActive.observe(this) {
            if (it == true) {
                navigationView.shareAndSaveView.setVisibleState(true)
                navigationView.imageShareAndSave.setVisibleState(true)
                navigationView.textViewShareAndSave.setVisibleState(true)
            }
        }

        viewModel.popupLiveData.observe(this) {
            DoubtnutApp.INSTANCE.bus()?.send(ShowMarkAttendanceWidget(it))
            addBadgeToRewardsIcon()
        }

        viewModel.attendanceLiveData.observe(this) {
            dismissRewardSystemNotifications()
            val notificationData = it.notificationData
            if (notificationData?.notificationHeading != null && notificationData.notificationDescription != null) {
                sendAttendanceMarkNotification()
            }
            sendRewardNotifThirtyMinutes()
        }

        viewModel.configLiveData.observeK(this,
            ::onConfigDataSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            {})

        val updateWidgetPlanButton = {
            if (widgetPlanButtonVM.widgetViewPlanButtonModel.value != null && widgetPlanButtonVM.widgetViewPlanButtonVisibility.value == true) {
                view_plan_button2.show()
                view_plan_button2.bindWidget(
                    view_plan_button2.widgetViewHolder,
                    widgetPlanButtonVM.widgetViewPlanButtonModel.value!!
                )
            } else {
                view_plan_button2.hide()
            }
        }
        widgetPlanButtonVM.widgetViewPlanButtonModel.observe(this) {
            updateWidgetPlanButton()
        }

        widgetPlanButtonVM.widgetViewPlanButtonVisibility.observe(this) {
            updateWidgetPlanButton()
        }
    }

    private fun updateDoubtP2pData() {
        val doubtP2pData = viewModel.getDoubtP2pData()
        setVisibilityOfDoubtP2p(doubtP2pData != null)
        doubtP2pData?.let { doubtP2p ->
            navigationView.imageViewDoubtP2p.loadImage(doubtP2p.image)
            navigationView.textViewDoubtP2p.text = doubtP2p.title
        }
    }

    private fun setVisibilityOfDoubtP2p(doubtP2pEnabled: Boolean) {
        navigationView.doubtP2pView.isVisible = doubtP2pEnabled
        navigationView.imageViewDoubtP2p.isVisible = doubtP2pEnabled
        navigationView.textViewDoubtP2p.isVisible = doubtP2pEnabled
    }

    private fun updateStudyDostData() {
        val studyDostImage = defaultPrefs().getString(Constants.STUDY_DOST_IMAGE, null)
        val studyDostMessage = defaultPrefs().getString(Constants.STUDY_DOST_DESCRIPTION, null)
        val studyDostCount = defaultPrefs().getInt(Constants.STUDY_DOST_UNREAD_COUNT, 0)
        if (studyDostImage == null && studyDostMessage == null) {
            setVisibilityOfStudyDostUi(false)
        } else {
            setVisibilityOfStudyDostUi(true)
            navigationView.imageViewStudyDost.loadImage(studyDostImage)
            navigationView.textViewStudyDost.text = studyDostMessage
            if (studyDostCount == 0) {
                navigationView.textViewChatCount.isVisible = false
            } else {
                navigationView.textViewChatCount.text = studyDostCount.toString()
            }
        }
    }

    private fun updateKheloAurJeetoData() {
        val kheloAurJeetoData = viewModel.getKheloAurJeetoData()
        navigationView.kheloAurJeetoView.isVisible = kheloAurJeetoData != null
        navigationView.imageViewKheloAurJeetoView.isVisible = kheloAurJeetoData != null
        navigationView.textViewKheloAurJeetoView.isVisible = kheloAurJeetoData != null
        kheloAurJeetoData?.let { kheloAurJeeto ->
            navigationView.imageViewKheloAurJeetoView.loadImage(kheloAurJeeto.image)
            navigationView.textViewKheloAurJeetoView.text = kheloAurJeeto.title

            navigationView.kheloAurJeetoView.setOnClickListener {
                closeDrawer()
                viewModel.publishEventWith(
                    EventConstants.TOPIC_BOOSTER_GAME_HOME_PAGE_VISITED, hashMapOf(
                        EventConstants.SOURCE to EventConstants.EVENT_NAME_HAMBURGER_MENU
                    )
                )
                deeplinkAction.performAction(this@MainActivity, kheloAurJeeto.deeplink)
            }
        }
    }

    private fun updateDoubtFeedData() {
        val doubtFeedData = viewModel.getDoubtFeed2Data()
        navigationView.dailyGoalView.isVisible = doubtFeedData != null
        navigationView.imageViewDailyGoal.isVisible = doubtFeedData != null
        navigationView.textViewDailyGoal.isVisible = doubtFeedData != null
        doubtFeedData?.let { doubtFeed ->
            navigationView.imageViewDailyGoal.loadImage(doubtFeed.image)
            navigationView.textViewDailyGoal.text = doubtFeed.title
            navigationView.dailyGoalView.setOnClickListener {
                closeDrawer()
                viewModel.publishEventWith(EventConstants.DF_OPEN_SOURCE_HAMBURGER)
                viewModel.publishEventWith(
                    EventConstants.DG_ICON_CLICK, hashMapOf(
                        Constants.SOURCE to Constants.HAMBURGER
                    )
                )
                deeplinkAction.performAction(this@MainActivity, doubtFeed.deeplink)
            }
        }
    }

    private fun updateStudyGroupData() {
        val studyGroupData = userPreference.getStudyGroupData()
        val isStudyGroupFeatureEnabled = studyGroupData != null
        setVisibilityOfStudyGroupUi(isStudyGroupFeatureEnabled)
        if (isStudyGroupFeatureEnabled) {
            navigationView.imageViewStudyGroup.loadImage(studyGroupData?.image)
            navigationView.textViewStudyGroup.text = studyGroupData?.title
        }
    }

    private fun updateDnrData() {
        val dnrData = viewModel.getDnrData()
        navigationView.apply {
            if (dnrData.image.isNotNullAndNotEmpty() && dnrData.deeplink.isNotNullAndNotEmpty() && dnrData.title.isNotNullAndNotEmpty()) {
                dnrView.show()
                imageViewDnr.show()
                imageViewDnr.loadImageEtx(dnrData.image)
                textViewDnr.show()
                textViewDnr.text = dnrData.title1
                dnrView.setOnClickListener {
                    closeDrawer()
                    deeplinkAction.performAction(this@MainActivity, dnrData.deeplink)
                }
            } else {
                dnrView.hide()
                imageViewDnr.hide()
                textViewDnr.hide()
            }
        }
    }

    private fun updateRevisionCornerData() {
        val revisionCornerData = viewModel.getRevisionCornerData()
        navigationView.revisionCornerView.isVisible = revisionCornerData != null
        navigationView.imageViewRevisionCorner.isVisible = revisionCornerData != null
        navigationView.textViewRevisionCorner.isVisible = revisionCornerData != null
        revisionCornerData?.let { revisionCorner ->
            navigationView.imageViewRevisionCorner.loadImage(revisionCorner.image)
            navigationView.textViewRevisionCorner.text = revisionCorner.title
            navigationView.revisionCornerView.setOnClickListener {
                closeDrawer()
                viewModel.publishEventWith(
                    EventConstants.RC_ICON_CLICK, hashMapOf(
                        Constants.SOURCE to Constants.HAMBURGER
                    ), ignoreSnowplow = true
                )
                deeplinkAction.performAction(this@MainActivity, revisionCorner.deeplink)
            }
        }
    }

    private fun setVisibilityOfStudyDostUi(visibility: Boolean) {
        navigationView.studyDostView.isVisible = visibility
        navigationView.imageViewStudyDost.isVisible = visibility
        navigationView.textViewStudyDost.isVisible = visibility
        navigationView.textViewChatCount.isVisible = visibility
    }

    private fun setVisibilityOfStudyGroupUi(visibility: Boolean) {
        navigationView.studyGroupView.isVisible = visibility
        navigationView.imageViewStudyGroup.isVisible = visibility
        navigationView.textViewStudyGroup.isVisible = visibility
    }

    private fun checkForVideoDialog(): Boolean {
        val list = getDatabase()?.eventsDao()
            ?.getPendingEvents("home", "video")
        return list != null && list.isNotEmpty()
    }

    private fun removeBadge(position: Int) {
        val itemCountView = bottomNavigationView.getChildAt(0) as BottomNavigationMenuView
        val notificationBadge = itemCountView.getChildAt(position) as BottomNavigationItemView
        notificationBadge.findViewById<View>(R.id.notification_badge)?.let {
            notificationBadge.removeView(it)
        }
        updatePreferenceForNotificationBadge(position)

    }

    private fun updatePreferenceForNotificationBadge(position: Int) {
        lifecycleScope.launchWhenStarted {
            when (position) {
                BOTTOM_NAVIGATION_POSITION_HOME -> bottomNavIconsNotificationsDataStore.set(
                    BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB1_SHOW_NOTIFICATION_BADGE,
                    false
                )
                BOTTOM_NAVIGATION_POSITION_LIBRARY -> bottomNavIconsNotificationsDataStore.set(
                    BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB2_SHOW_NOTIFICATION_BADGE,
                    false
                )
                BOTTOM_NAVIGATION_POSITION_FORUM -> bottomNavIconsNotificationsDataStore.set(
                    BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB3_SHOW_NOTIFICATION_BADGE,
                    false
                )
                BOTTOM_NAVIGATION_POSITION_PROFILE -> bottomNavIconsNotificationsDataStore.set(
                    BottomNavIconsNotificationsDataStoreImpl.PREF_KEY_TAB4_SHOW_NOTIFICATION_BADGE,
                    false
                )
            }
        }
    }

    private fun onSuccess(userProfileDTO: UserProfileData) {
        userProfileDTO.apply {
            navigationView.imageUserProfile.loadImage(profileImage, R.drawable.ic_person_grey)
            navigationView.textViewUserProfileName.text = name
            navigationView.imageViewUserBannerImage.loadImageEtx(bannerImage)
            navigationView.levelButtonMyBio.text = level
            navigationView.textViewClass.text = "(${
                defaultPrefs(this@MainActivity).getString(
                    Constants.STUDENT_CLASS_DISPLAY,
                    ""
                )
            })"
            navigationView.textViewLanguage.text = "(${
                defaultPrefs(this@MainActivity).getString(
                    Constants.STUDENT_LANGUAGE_NAME_DISPLAY,
                    ""
                )
            })"
            setupMobileNumberNavigationView()
            setupBoardExamsNavigationView()
        }
    }

    private fun unAuthorizeUserError() {
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun updateProgressBarState(state: Boolean) {
        navigationView.progressUserProfile.setVisibleState(state)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun setDataFromIntent(intent: Intent?) {
        intent?.let {
            @IdRes var navDestinationId: Int? = null
            var navOptionsArgs: Bundle? = null

            when (it.action) {
                Constants.NAVIGATE_HOME -> {
                    navDestinationId = R.id.homeFragment
                    setSelectedTabId(Constants.KEY_HOME)
                    var navSource = intent.getStringExtra("nav_source").orEmpty()
                    if (navSource.isBlank()) {
                        navSource = "default"
                    }
                    sendHomePageOpenEvent(navSource)
                }

                Constants.NAVIGATE_FORUM_FEED -> {
                    val feedFilter = it.getStringExtra(Constants.INTENT_EXTRA_FORUM_FILTER)
                    navOptionsArgs = bundleOf(
                        "filter" to feedFilter
                    )
                    navDestinationId = R.id.forumFragment
                    sendEventByClick(EventConstants.PAGE_FORUM_FEED_PAGE)
                    sendEventForHomePageBottomScreenCleverTap(
                        EventConstants.EVENT_NAME_HOME_PAGE_BOTTOM_SHEET,
                        Constants.FORUM_FRAGMENT,
                        false
                    )

                }

                Constants.NAVIGATE_FEED -> {
                    navOptionsArgs = bundleOf(
                        FeedFragment.IS_NESTED to false,
                        FeedViewModel.FEED_TYPE to FeedViewModel.NORMAL_FEED
                    )
                    navDestinationId = R.id.forumFragment
                    setSelectedTabId(Constants.KEY_FRIENDS)
                }

                Constants.NAVIGATE_PROFILE -> {
                    navDestinationId = R.id.userProfileFragment
                    navOptionsArgs =
                        bundleOf(UserProfileFragment.PARAM_KEY_OPEN_DOUBT_FEED to false)
                    sendEventByClick(EventConstants.PAGE_MAIN_PROFILE_PAGE)
                    sendEventForHomePageBottomScreenCleverTap(
                        EventConstants.EVENT_NAME_HOME_PAGE_BOTTOM_SHEET,
                        Constants.PROFILE_FRAGMENT,
                        false
                    )
                    setSelectedTabId(Constants.KEY_PROFILE)
                }

                Constants.NAVIGATE_DOUBT_FEED -> {
                    navDestinationId = R.id.doubtFeedFragment
                    if (it.extras?.getBoolean(Constants.DOUBT_FEED_REFRESH) == true) {
                        DoubtnutApp.INSTANCE.bus()?.send(RefreshDoubtFeed)
                    }
                }

                Constants.NAVIGATE_LIBRARY -> {
                    when {
                        it.hasExtra(Constants.TAG)
                                && !it.getStringExtra(Constants.TAG).isNullOrBlank() -> {
                            navOptionsArgs = bundleOf(
                                Constants.TAG to it.getStringExtra(Constants.TAG),
                                Constants.SOURCE to it.getStringExtra(Constants.SOURCE)
                            )
                        }
                        it.hasExtra(Constants.LIBRARY_SCREEN_SELECTED_TAB)
                                && !it.getStringExtra(Constants.LIBRARY_SCREEN_SELECTED_TAB)
                            .isNullOrBlank() -> {
                            navOptionsArgs = bundleOf(
                                Constants.LIBRARY_SCREEN_SELECTED_TAB to it.getStringExtra(Constants.LIBRARY_SCREEN_SELECTED_TAB),
                                Constants.SOURCE to it.getStringExtra(Constants.SOURCE)
                            )
                        }
                        it.getStringExtra(Constants.PLAYLIST_ID).isNullOrBlank() -> {
                            navOptionsArgs = null
                            sendEventByClick(EventConstants.PAGE_MAIN_LIBRARY_PAGE)
                            sendEventForHomePageBottomScreenCleverTap(
                                EventConstants.EVENT_NAME_HOME_PAGE_BOTTOM_SHEET,
                                Constants.LIBRARY_FRAGMENT,
                                false
                            )
                        }
                        else -> {
                            navOptionsArgs = bundleOf(
                                Constants.LIBRARY_FRAGMENT_LEVEL_PAGE to it.getIntExtra(
                                    Constants.PAGE,
                                    1
                                ),
                                Constants.LIBRARY_FRAGMENT_LEVEL_PLAYLIST_ID to it.getStringExtra(
                                    Constants.PLAYLIST_ID
                                ),
                                Constants.LIBRARY_FRAGMENT_LEVEL_PLAYLIST_TITLE to it.getStringExtra(
                                    Constants.PLAYLIST_TITLE
                                ),
                                Constants.LIBRARY_FRAGMENT_LEVEL to true
                            )

                            sendEventByClick(EventConstants.PAGE_MAIN_LIBRARY_PAGE)
                            sendEventForHomePageBottomScreenCleverTap(
                                EventConstants.EVENT_NAME_HOME_PAGE_BOTTOM_SHEET,
                                Constants.LIBRARY_FRAGMENT,
                                false
                            )

                        }
                    }
                    navDestinationId = R.id.libraryFragment

                    // select tab for back-end driven icons
                    lifecycleScope.launchWhenStarted {
                        val bottomNavDataJson =
                            defaultDataStore.bottomNavigationIconsData.firstOrNull() ?: ""
                        if (Utils.isBottomNavigationIconsApiDataAvailable(bottomNavDataJson)) {
                            if (!it.hasExtra(Constants.LIBRARY_SCREEN_SELECTED_TAB)
                            ) {
                                setSelectedTabId(Constants.KEY_ONLINE_CLASS)
                            } else {
                                setSelectedTabId(
                                    "${Constants.NAVIGATE}_${
                                        it.getStringExtra(
                                            Constants.TAG
                                        )
                                    }"
                                )
                            }
                        }
                    }
                }

                Constants.NAVIGATE_CAMERA_SCREEN -> {
                    openCameraScreen(viewModel.cameraScreenAutoOpenParams.getBoolean(CameraActivity.INTENT_EXTRA_IS_USER_OPENED))
                }

                Constants.CHANGE_LANGUAGE -> {
                    screenNavigator.startActivityForResultFromActivity(
                        this, ChangeLanguageScreen,
                        null,
                        REQUEST_CODE_CHNAGE_LANGUAGE
                    )
                }

                Constants.CHANGE_CLASS -> {
                    onClassChangeRequest()
                }

                Constants.OPEN_LIBRARY_FROM_BOTTOM -> {
                    navDestinationId = R.id.libraryFragment
                    setSelectedTabId(Constants.KEY_ONLINE_CLASS)
                }

                Constants.OPEN_FORUM_FROM_BOTTOM -> {
                    navDestinationId = R.id.forumFragment
                    setSelectedTabId(Constants.KEY_FRIENDS)
                }

                Constants.OPEN_PROFILE_FROM_BOTTOM -> {
                    navDestinationId = R.id.userProfileFragment
                    setSelectedTabId(Constants.KEY_PROFILE)
                }
                Constants.NAVIGATE_TO_FREE_TRIAL_COURSE -> {
                    navDestinationId = R.id.freeTrialCourseFragment
                }
            }
            if (navDestinationId != null) {
                if (it.getBooleanExtra(
                        KEY_RECREATE,
                        false
                    ) || navigationController.currentDestination?.id != navDestinationId
                ) {
                    navigateToDestination(navDestinationId, navOptionsArgs)
                }
            }

            //open home page nav id
            if (it.getBooleanExtra(KEY_OPEN_HOME_PAGE, false)) {
                navigationController.navigate(R.id.homeFragment, navOptionsArgs)
            }
        }
        sendEventForHomePageCleverTap(EventConstants.EVENT_NAME_HOME_PAGE_VIEW)
    }

    // to prevent multiple instances of same fragment and
    // ensure only 2 back press - one from target tab to home and one from home to APP EXIT
    private fun navigateToDestination(navDestinationId: Int, navOptionsArgs: Bundle?) {
        lifecycleScope.launchWhenStarted {
            val bottomNavigationDataJson =
                defaultDataStore.bottomNavigationIconsData.firstOrNull() ?: ""
            if (Utils.isBottomNavigationIconsApiDataAvailable(bottomNavigationDataJson)) {
                navigationController.popBackStack(navDestinationId, true)
                navigationController.popBackStack(R.id.libraryFragment, true)
                navigationController.popBackStack(R.id.forumFragment, true)
                navigationController.popBackStack(R.id.userProfileFragment, true)
            }
            navigationController.navigate(navDestinationId, navOptionsArgs)
        }
    }

    private fun setSelectedTabId(key: String) {
        lifecycleScope.launchWhenStarted {
            val bottomNavigationData =
                defaultDataStore.bottomNavigationIconsData.firstOrNull().orEmpty()


            bottomNavigationView.menu.setGroupCheckable(0, true, true);
            var found = false

            if (Utils.isBottomNavigationIconsApiDataAvailable(bottomNavigationData)) {
                val response =
                    Gson().fromJson(bottomNavigationData, BottomNavigationTabsData::class.java)
                response.tab1?.let {
                    if (it.tag.equals(key)) {
                        removeBadge(BOTTOM_NAVIGATION_POSITION_HOME)
                        bottomNavigationView.menu.findItem(R.id.homeFragment).isChecked =
                            true
                        found = true

                    }

                }
                response.tab2?.let {
                    if (it.tag.equals(key)) {
                        removeBadge(BOTTOM_NAVIGATION_POSITION_LIBRARY)
                        bottomNavigationView.menu.findItem(R.id.libraryFragment).isChecked =
                            true
                        found = true
                    }

                }
                response.tab3?.let {
                    if (it.tag.equals(key)) {
                        removeBadge(BOTTOM_NAVIGATION_POSITION_FORUM)
                        bottomNavigationView.menu.findItem(R.id.forumFragment).isChecked =
                            true
                        found = true
                    }

                }
                response.tab4?.let {
                    if (it.tag.equals(key)) {
                        removeBadge(BOTTOM_NAVIGATION_POSITION_PROFILE)
                        bottomNavigationView.menu.findItem(R.id.userProfileFragment).isChecked =
                            true
                        found = true
                    }

                }
                val lastKey = lastSelectedTabKey

                if (!found) {
                    bottomNavigationView.getMenu().setGroupCheckable(0, false, true);
                } else {
                    lastSelectedTabKey = key
                }
                launch {
                    delay(1000)
                    lastSelectedBottomTab = lastKey
                }
            }

        }
    }

    private fun handleBottomNavItemSelectedListener() {
        bottomNavigationView?.setOnItemSelectedListener { item ->
            var isBottomNavIconsDataAvailable = false
            lifecycleScope.launch {
                val responseJson =
                    defaultDataStore.bottomNavigationIconsData.firstOrNull().orEmpty()
                isBottomNavIconsDataAvailable =
                    Utils.isBottomNavigationIconsApiDataAvailable(responseJson)
                if (isBottomNavIconsDataAvailable) {

                    val responseBottomNavData =
                        Gson().fromJson(responseJson, BottomNavigationTabsData::class.java)

                    when (item.itemId) {
                        R.id.homeFragment -> {

                            launchDeeplinkAndSendEvent(responseBottomNavData.tab1, "1")
                            setSelectedTabId(responseBottomNavData.tab1?.tag!!)
                            removeBadge(BOTTOM_NAVIGATION_POSITION_HOME)
                        }
                        R.id.libraryFragment -> {

                            launchDeeplinkAndSendEvent(responseBottomNavData.tab2, "2")
                            setSelectedTabId(responseBottomNavData.tab2?.tag!!)
                            removeBadge(BOTTOM_NAVIGATION_POSITION_LIBRARY)
                        }
                        R.id.forumFragment -> {

                            launchDeeplinkAndSendEvent(responseBottomNavData.tab3, "3")
                            setSelectedTabId(responseBottomNavData.tab3?.tag!!)
                            removeBadge(BOTTOM_NAVIGATION_POSITION_FORUM)
                        }
                        R.id.userProfileFragment -> {

                            launchDeeplinkAndSendEvent(responseBottomNavData.tab4, "4")
                            setSelectedTabId(responseBottomNavData.tab4?.tag!!)
                            removeBadge(BOTTOM_NAVIGATION_POSITION_PROFILE)

                        }
                        else -> {
                            false
                        }
                    }

                } else {
                    when (item.itemId) {
                        R.id.homeFragment -> {
                            val intent = Intent(this@MainActivity, MainActivity::class.java)
                            intent.action = Constants.NAVIGATE_HOME
                            this@MainActivity.startActivity(intent)
                        }
                        R.id.libraryFragment -> {
                            val intent = Intent(this@MainActivity, MainActivity::class.java).also {
                                it.action = Constants.NAVIGATE_LIBRARY
                                it.putExtra(Constants.TAG, LibraryTabAdapter.TAG_MY_COURSES)
                            }
                            this@MainActivity.startActivity(intent)
                        }
                        R.id.forumFragment -> {
                            val intent = Intent(this@MainActivity, MainActivity::class.java).apply {
                                action = Constants.NAVIGATE_FEED
                            }
                            this@MainActivity.startActivity(intent)

                        }
                        R.id.userProfileFragment -> {
                            val intent = Intent(this@MainActivity, MainActivity::class.java)
                            intent.action = Constants.NAVIGATE_PROFILE
                            this@MainActivity.startActivity(intent)
                        }
                    }
                }
            }
            true
        }
    }

    private fun showNotificationBadgeForBottomNav() {
        lifecycleScope.launchWhenStarted {
            val showNotificationForTab1 =
                bottomNavIconsNotificationsDataStore.tab1ShowNotificationBadge.first()
            if (showNotificationForTab1) {
                showNewBadge(BOTTOM_NAVIGATION_POSITION_HOME)
            }
            val showNotificationForTab2 =
                bottomNavIconsNotificationsDataStore.tab2ShowNotificationBadge.first()
            if (showNotificationForTab2) {
                showNewBadge(BOTTOM_NAVIGATION_POSITION_LIBRARY)
            }
            val showNotificationForTab3 =
                bottomNavIconsNotificationsDataStore.tab3ShowNotificationBadge.first()
            if (showNotificationForTab3) {
                showNewBadge(BOTTOM_NAVIGATION_POSITION_FORUM)
            }
            val showNotificationForTab4 =
                bottomNavIconsNotificationsDataStore.tab4ShowNotificationBadge.first()
            if (showNotificationForTab4) {
                showNewBadge(BOTTOM_NAVIGATION_POSITION_PROFILE)
            }
        }
    }

    private fun launchDeeplinkAndSendEvent(
        responseTabData: BottomNavigationTabsData.TabData?,
        position: String
    ) {
        deeplinkAction.performAction(
            this,
            responseTabData?.deeplink
        )
        Utils.publishBottomNavTabClickEvent(
            analyticsPublisher,
            responseTabData?.name.orEmpty(),
            position,
        )
    }

    override fun onStart() {
        super.onStart()
        if (defaultPrefs().getBoolean(Constants.ATTENDANCE_MARKED_FROM_REWARD_PAGE, false)) {
            DoubtnutApp.INSTANCE.bus()?.send(HideMarkAttendanceWidget)
            defaultPrefs().edit {
                putBoolean(Constants.ATTENDANCE_MARKED_FROM_REWARD_PAGE, false)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (currentFragment == HomeFeedFragmentV2.newInstance()) {
            val inflater = menuInflater
            inflater.inflate(R.menu.menu_home_toolbar, menu)
            viewModel.updateFCMRegId()
        }
        return super.onCreateOptionsMenu(menu)
    }

    private fun getActiveFeedbacks() {
        DataHandler.INSTANCE.feedbackRepository.getActiveFeedbacks().observe(this) {
            when (it) {
                is Outcome.Success -> {
                    try {
                        DoubtnutApp.INSTANCE.runOnDifferentThread {
                            database?.feedbackDao()?.clearActiveFeedbacks()
                            database?.feedbackDao()
                                ?.insertAllFeedbacks(it.data.data.toDBAppActiveFeedback())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun unregisterOnSharedPreferenceChangeListener() {
        onSharedPreferenceChangeListener?.let {
            defaultPrefs().unregisterOnSharedPreferenceChangeListener(it)
            onSharedPreferenceChangeListener = null
        }
    }

    override fun onStop() {
        unregisterOnSharedPreferenceChangeListener()
        changeLoginPinDialogFragment?.dismissAllowingStateLoss()

        if (isBranchLink) finishAffinity()
        super.onStop()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.END)) {
            drawer.closeDrawers()
        } else {
            if (navigationController.currentDestination?.id == R.id.homeFragment
                && viewModel.showAppExitDialog
            ) {
                AppExitDialogFragment.newInstance(
                    viewModel.appExitDialogExperiment
                ).show(supportFragmentManager, AppExitDialogFragment.TAG)
            } else {
                super.onBackPressed()
                if (nav_host_fragment?.childFragmentManager?.backStackEntryCount == 1) {
                    setSelectedTabId(Constants.KEY_HOME)
                } else {
                    setSelectedTabId(lastSelectedTabKey)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        busDisposable?.dispose()
    }

    override fun startActivity(intent: Intent?) {
        isBranchLink = intent?.data?.host.equals(Constants.BRANCH_HOST)
        if (isBranchLink) {
            intent?.putExtra(Defines.IntentKeys.ForceNewBranchSession.key, true)
        }
        super.startActivity(intent)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    private fun addActiveFeedbackObservers() {

        database?.feedbackDao()
            ?.getActiveFeedbackForType(Constants.FEEDBACK_TYPE_ASKED_NOT_WATCHED)
            ?.observe(this) {
                if (it != null && it.isNotEmpty()) {
                    FeedbackFragment.newInstance(it[0]).show(supportFragmentManager, "Feedback")
                    DoubtnutApp.INSTANCE.runOnDifferentThread {
                        database?.feedbackDao()?.updateFeedbackStatus(
                            "inactive",
                            Constants.FEEDBACK_TYPE_ASKED_NOT_WATCHED
                        )
                    }
                }
            }
        database?.feedbackDao()
            ?.getActiveFeedbackForType(Constants.FEEDBACK_TYPE_NO_MATCH_WATCHED)
            ?.observe(this) {
                if (it != null && it.isNotEmpty()) {
                    FeedbackFragment.newInstance(it[0]).show(supportFragmentManager, "Feedback")
                    DoubtnutApp.INSTANCE.runOnDifferentThread {
                        database?.feedbackDao()?.updateFeedbackStatus(
                            "inactive",
                            Constants.FEEDBACK_TYPE_NO_MATCH_WATCHED
                        )
                    }
                }
            }


        DoubtnutApp.INSTANCE.runOnDifferentThread {
            database?.feedbackDao()?.updateFeedbackStatus(
                "active",
                Constants.FEEDBACK_NPS
            )
        }
        database?.feedbackDao()
            ?.getActiveFeedbackForType(Constants.FEEDBACK_NPS)
            ?.observe(this) {
                if (it != null && it.isNotEmpty()) {
                    NPSFeedbackFragment.newInstance(it[0]).show(supportFragmentManager, "Feedback")
                    DoubtnutApp.INSTANCE.runOnDifferentThread {
                        database?.feedbackDao()
                            ?.updateFeedbackStatus("inactive", Constants.FEEDBACK_NPS)
                        database?.feedbackDao()?.clearNpsFeedbacks(it[0].id)
                    }
                }
            }

    }

    private fun init() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[MainViewModel::class.java]
        widgetPlanButtonVM =
            ViewModelProviders.of(this, viewModelFactory)[WidgetPlanButtonVM::class.java]
        database = getDatabase()
        viewModel.getAudioToolTipList()
        askQuestionButton.setOnClickListener {
            if (!AppUtils.isImmediateUpdate()) {
                viewModel.publishCameraClickEvent(EventConstants.BOTTOM_BAR)
                openCameraScreen(false)
                sendEventByClick(EventConstants.EVENT_NAME_ASK_IN_BOTTOM_CLICK)
                sendEventForHomePageBottomScreenCleverTap(
                    EventConstants.EVENT_NAME_HOME_PAGE_BOTTOM_SHEET,
                    Constants.ASK_FRAGMENT,
                    true
                )
            }
        }
        ApxorUtils.setUserIdentifier(getStudentId())
        ApxorUtils.addDefaultAttributes()
        ApxorUtils.addAppExperimentAttributes()
        ApxorUtils.setUserCustomInfo()
        MoEngageUtils.setStudentData(applicationContext)
        viewModel.publishEventWith(
            EventConstants.EVENT_FEATURE_CONFIG_STATUS,
            hashMapOf(Pair("is_updated", FeaturesManager.isFeatureConfigUpdated)), true
        )

        viewModel.setMoEngageInfo()
        if (UserUtil.getIsAnonymousLogin()) {
            AnonymousLoginBlockerFragment.newInstance()
                .show(supportFragmentManager, AnonymousLoginBlockerFragment.TAG)
        }

        SyncConfigDataWorker.enqueue(this)
    }

    private fun sendInvitationData() {

        val invitorId = defaultPrefs(this).getString(Constants.INTENT_EXTRA_INVITOR_ID, "")
        //access token of the user can be used as the id of Invitee.
        val inviteeId = defaultPrefs(this).getString(Constants.XAUTH_HEADER_TOKEN, "")

        viewModel.sendInviteFriendData(invitorId, inviteeId)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == NotificationCenterActivity.REQUEST_CODE_NOTIFICATION) {
                viewModel.updateNotificationCount()
            } else if (requestCode == REQUEST_CODE_CHANGE_CLASS) {
                getConfigData()
                navigationController.navigate(R.id.homeFragment, null)
            }
            for (fragment in supportFragmentManager.fragments) {
                fragment.onActivityResult(requestCode, resultCode, data)
            }
        }
        inAppUpdateManager.onActivityResult(requestCode, resultCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (fragment in supportFragmentManager.fragments) {
            fragment.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun showCameraPageFirst() {
        defaultOnboardingDeeplink =
            defaultPrefs().getString(Constants.DEFAULT_ONBOARDING_DEEPLINK, null)

        if (defaultOnboardingDeeplink == null) {
            openCameraScreen(viewModel.cameraScreenAutoOpenParams.getBoolean(CameraActivity.INTENT_EXTRA_IS_USER_OPENED))
            val cameraScreenShownCount =
                defaultPrefs(application).getLong(Constants.PREF_KEY_CAMERA_SCREEN_SHOWN_COUNT, 0)
            defaultPrefs(this).edit {
                putLong(Constants.PREF_KEY_CAMERA_SCREEN_SHOWN_COUNT, cameraScreenShownCount + 1)
            }
        } else {
            if (defaultOnboardingDeeplink!!.contains("camera")) {
                openCameraScreen(viewModel.cameraScreenAutoOpenParams.getBoolean(CameraActivity.INTENT_EXTRA_IS_USER_OPENED))
            } else {
                if (!defaultOnboardingDeeplink!!.isEmpty()) {
                    deeplinkAction.performAction(application, defaultOnboardingDeeplink)
                }
            }
        }
    }

    private fun openCameraScreen(isUserOpened: Boolean) {
        cameraScreenResultLauncher.launch(getCameraIntent(isUserOpened))
    }

    private fun getCameraIntent(isUserOpened: Boolean) =
        CameraActivity.getStartIntent(
            context = this@MainActivity,
            source = TAG,
            isUserOpened = isUserOpened
        )

    private fun handleBranchDeepLink() {
        if (AppUtils.isImmediateUpdate()) {
            return
        }

        if (!CoreApplication.pendingDeeplink.isNullOrEmpty()) {
            deeplinkAction.performAction(this, CoreApplication.pendingDeeplink)
            CoreApplication.pendingDeeplink = null
            return
        }

        if (!checkForBranchDeepLink()) {
            // If work is not done then register.
            registerOnSharedPreferenceChangeListener()
        }
    }

    private var onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener? =
        null

    private fun registerOnSharedPreferenceChangeListener() {
        onSharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == Constants.REFFERING_PARAMS) {
                    if (checkForBranchDeepLink()) {
                        // If work done then unregister.
                        unregisterOnSharedPreferenceChangeListener()
                    }
                }
            }
        defaultPrefs().registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
    }

    private fun checkForBranchDeepLink(): Boolean {
        val referringParams = BranchIOUtils.getReferringParam(applicationContext)
        if (!referringParams.isNullOrBlank()) {
            if (!AppUtils.isImmediateUpdate()) {
                val deeplink = BranchDeeplinkHandler.getParsedDeeplink(
                    applicationContext,
                    referringParams,
                    (applicationContext as DoubtnutApp).getEventTracker(),
                    analyticsPublisher
                )
                when {
                    deeplink != null -> {
                        deeplinkAction.performAction(
                            this,
                            deeplink,
                            EventConstants.PAGE_DEEPLINK_CLICK
                        )
                    }
                    !defaultPrefs(this).getString(Constants.NOTIFICATION_DATA, "")
                        .isNullOrBlank() -> {
                        val dataMap = Gson().fromJson<HashMap<String, String>>(
                            defaultPrefs(this).getString(
                                Constants.NOTIFICATION_DATA,
                                ""
                            ), object : TypeToken<HashMap<String, String>>() {
                            }.type
                        )
                        val intent = Intent(this, ActionHandlerActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        intent.putExtra("data", dataMap)
                        this.startActivity(intent)
                    }
                    !DoubtnutApp.INSTANCE.isOnboardingStarted -> {
                        if (popupDeeplink.isNotEmpty()) {
                            deeplinkAction.performAction(this, popupDeeplink)
                        }
                        val campaignDeeplink = defaultPrefs(this).getString(
                            LocalConfigDataSource.CAMPAIGN_LANDING_DEEPLINK,
                            ""
                        )
                        if (!campaignDeeplink.isNullOrBlank()) {
                            deeplinkAction.performAction(this, campaignDeeplink)
                            defaultPrefs().edit {
                                putString(LocalConfigDataSource.CAMPAIGN_LANDING_DEEPLINK, "")
                            }
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    EventConstants.NAVIGATE_FROM_CAMPAIGN, hashMapOf(
                                        EventConstants.EVENT_NAME_DEEPLINK to campaignDeeplink.orEmpty(),
                                        LocalConfigDataSource.INSTALL_CAMPAIGN_NAME to defaultPrefs().getString(
                                            LocalConfigDataSource.INSTALL_CAMPAIGN_NAME,
                                            ""
                                        ).orEmpty()
                                    ), ignoreSnowplow = true
                                )
                            )
                        } else if (firstPageDeeplink.isEmpty()) {
                            showCameraPageFirst()
                        } else {
                            deeplinkAction.performAction(this, firstPageDeeplink)
                        }
                    }
                }
            }
            sendInvitationData()
            unregisterOnSharedPreferenceChangeListener()
            BranchIOUtils.clearReferringParam(applicationContext)
            return true
        } else if (UserUtil.getIsAnonymousLogin()
            && !AppUtils.isImmediateUpdate()
            && defaultPrefs(this).getLong("camera_s_v_c", 0) == 0L
        ) {
            showCameraPageFirst()
            return true
        }
        return false
    }

    private fun sendEventByClick(eventName: String) {
        this@MainActivity.apply {
            eventTracker.addEventNames(eventName)
                .addScreenState(
                    EventConstants.EVENT_PRAMA_SCREEN_STATE,
                    EventConstants.EVENT_NAME_SCREEN_STATE_MAIN_ACTIVITY_PAGE
                )
                .addNetworkState(NetworkUtils.isConnected(this@MainActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_MAIN_ACTIVITY)
                .track()
        }
    }

    private fun sendEventForHomePageCleverTap(eventName: String) {
        this@MainActivity.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@MainActivity).toString())
                .addStudentId(getStudentId())
                .addEventParameter(EventConstants.PARAM_SUCCESS, true)
                .cleverTapTrack()

        }
    }

    private fun sendEventForHomePageBottomScreenCleverTap(
        eventName: String,
        tabName: String,
        isClick: Boolean
    ) {
        this@MainActivity.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@MainActivity).toString())
                .addStudentId(getStudentId())
                .addEventParameter(EventConstants.PARAM_IS_USER_CLICK, isClick)
                .addEventParameter(EventConstants.PARAM_NAME, tabName)
                .cleverTapTrack()

        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@MainActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        dismissAppExitDialog()
        handleBranchDeepLink()
        setDataFromIntent(intent)

        LocaleManager.setLocale(this)
    }

    private fun setupAndroidJob() {
        lifecycleScope.launchWhenStarted {
            val checkFetch = quizNotificationDatastore.checkFetch.firstOrNull() ?: false
            if (checkFetch) return@launchWhenStarted
            if (FeaturesManager.isFeatureEnabled(
                    this@MainActivity,
                    Features.EVERNOTE_ANDROID_JOB,
                    true
                )
            ) {
                if (authToken(this@MainActivity).isNotEmpty()) {
                    quizNotificationDatastore.set(
                        QuizNotificationDatastoreImpl.CHECK_FETCH_KEY,
                        true
                    )
                    FetchQuizJob.enqueue(this@MainActivity)
                }
            } else {
                EvernoteUtils.cancelAll(this@MainActivity)
                FetchQuizJobWorker.enqueue(this@MainActivity)
            }
        }
    }

    /**
     * This function is completely not required as the event which was sending earlier is removed now
     * This is just for showcasing datastore and flow usage.
     */
    private fun handleEtoosContentRegisteredEvent() {
        lifecycleScope.launchWhenStarted {
            val isNewUser = defaultDataStore.isNewUser.firstOrNull() ?: false
            if (!isNewUser) return@launchWhenStarted

            val etoosContentRegistrationEventSent =
                defaultDataStore.etoosContentRegistrationEventSent.firstOrNull() ?: false
            if (etoosContentRegistrationEventSent) return@launchWhenStarted

            val studentClass = getStudentClass()
            val isEligibleForEtoosContent = studentClass == "9"
                    || studentClass == "10"
                    || studentClass == "11"
                    || studentClass == "12"
                    || studentClass == "13"

            if (!isEligibleForEtoosContent) return@launchWhenStarted

            if (isEligibleForEtoosContent && !etoosContentRegistrationEventSent) {
                defaultDataStore.set(
                    DefaultDataStoreImpl.ETOOS_CONTENT_REGISTRATION_EVENT_SENT,
                    true
                )
            }
        }
    }

    private fun dismissAppExitDialog() {
        supportFragmentManager.fragments.forEach {
            if (it is AppExitDialogFragment) {
                it.dismissAllowingStateLoss()
            }
        }
    }

    private fun setFallbackPushAlarm() {
        val intent = Intent(this, FallbackReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            PENDING_INTENT_ALARM_10PM,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis(),
                pendingIntent
            )
        }
    }

    private fun markAttendance() {
        if (isAttendanceUnmarked()) {
            viewModel.getManualAttendancePopupData()
        }
    }

    private fun isAttendanceUnmarked(): Boolean {
        val lastMarkedAttendanceTime = defaultPrefs().getLong(Constants.LAST_MARKED_DAY, 0)
        return !isToday(lastMarkedAttendanceTime)
    }

    private fun sendRewardNotifThirtyMinutes() {
        if (isUnscratchedCardPresent()) {
            if (!defaultPrefs().getBoolean(Constants.IS_FIRST_UNSCRATCHED_SHOWN, false)) {
                val intent = Intent(this, RewardNotificationReceiver::class.java)
                val pendingIntent =
                    PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
                val alarmManager: AlarmManager =
                    getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val triggerTimeMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
                }
            }
        }
    }

    private fun sendRewardNotifTenMinutes() {
        if (!defaultPrefs().getBoolean(Constants.IS_FIRST_REWARD_SHOWN, false)) {
            val intent = Intent(this, LoginRewardReceiver::class.java)
            val pendingIntent =
                PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val triggerTimeMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
            }
            //Set flag for shown
            defaultPrefs().edit {
                putBoolean(Constants.IS_FIRST_REWARD_SHOWN, true)
            }
        }
    }

    private fun sendScheduledRewardNotifications() {
        val intent = Intent(this, RewardRepeatedNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            NotificationConstants.PENDING_INTENT_REWARD,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 2000,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 2000,
                pendingIntent
            )
        }
    }

    private fun isUnscratchedCardPresent(): Boolean {
        return defaultPrefs().getInt(Constants.UNSCRATCHED_CARD_COUNT, 0) > 0
    }

    private fun sendAttendanceMarkNotification() {
        val intent = Intent(this, AttendanceMarkedReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            NotificationConstants.PENDING_INTENT_ATTENDANCE_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 1000,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                System.currentTimeMillis() + 1000,
                pendingIntent
            )
        }
    }

    private fun dismissRewardSystemNotifications() {
        val mNotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.cancel(NotificationConstants.NOTIFICATION_ID_REWARD)
        mNotificationManager.cancel(NotificationConstants.NOTIFICATION_ID_ATTENDANCE)
    }

    private fun showDoubtFeed(): Boolean {
        return FeaturesManager.isFeatureEnabled(this, Features.DOUBT_FEED)
                && viewModel.isDoubtFeedAvailable()
    }

    private fun setupMobileNumberNavigationView() {
        navigationView.textViewMobileNumber.text = buildSpannedString {
            append(getString(R.string.registered_mobile))
            color(ContextCompat.getColor(this@MainActivity, R.color.profile_language_color)) {
                append("${UserUtil.countryCode} ${UserUtil.getPhoneNumber()}")
            }
        }

        navigationView.mobileNumberView.setOnClickListener {
            // intercept click
        }
    }

    private fun setupBoardExamsNavigationView() {
        fun hideDivider(guidelinePercent: Int) {
            boardExamsDivider.hide()
            guidelineBoardExamsMiddle.setGuidelinePercent(guidelinePercent.toFloat())
        }

        val board = userPreference.getUserSelectedBoard()
        val exams = userPreference.getUserSelectedExams()

        navigationView.apply {
            tvBoard.text = board
            tvExams.text = exams

            boardExamsView.show()
            groupBoard.show()
            groupExams.show()
            boardExamsDivider.show()
            guidelineBoardExamsMiddle.setGuidelinePercent(.5f)

            boardExamsView.setOnClickListener {
                closeDrawer()
                goToEditBio()
            }

            if (board.isEmpty()) {
                groupBoard.hide()
                hideDivider(0)
            }

            if (exams.isEmpty()) {
                groupExams.hide()
                hideDivider(1)
            }

            if (board.isEmpty() && exams.isEmpty()) {
                boardExamsView.hide()
            }
        }
    }

    private fun goToEditBio() {
        MyBioActivity.getStartIntent(context = this@MainActivity, refreshHomeFeed = true)
            .apply {
                startActivity(this)
            }
    }

    private fun getConfigData() {
        val postPurchaseSessionCount =
            defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0)
        val sessionCount = defaultPrefs().getInt(Constants.SESSION_COUNT, 1)
        viewModel.getConfigData(sessionCount, postPurchaseSessionCount)
    }

    private fun onConfigDataSuccess(data: ConfigData) {
        ConfigUtils.saveToPref(data)
    }

}

