package com.doubtnutapp.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.*
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.NetworkUtils
import com.doubtnut.noticeboard.NoticeBoardConstants
import com.doubtnut.noticeboard.data.entity.UnreadNoticeCountUpdate
import com.doubtnut.noticeboard.data.remote.NoticeBoardRepository
import com.doubtnut.noticeboard.ui.NoticeBoardDetailActivity
import com.doubtnutapp.*
import com.doubtnutapp.Constants.STUDENT_CLASS
import com.doubtnutapp.EventBus.ScrollTopEvent
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.getDatabase
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.common.model.PopUpDialog
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.databinding.FragmentHomeV2Binding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.dnr.model.*
import com.doubtnutapp.dnr.ui.fragment.DnrPopupDialogFragment
import com.doubtnutapp.dnr.ui.fragment.DnrRewardBottomSheetFragment
import com.doubtnutapp.dnr.ui.fragment.DnrRewardDialogFragment
import com.doubtnutapp.dnr.viewmodel.DnrRewardViewModel
import com.doubtnutapp.domain.library.entities.ClassListViewItem
import com.doubtnutapp.domain.survey.entities.ApiCheckSurvey
import com.doubtnutapp.downloadedVideos.DownloadedVideosActivity
import com.doubtnutapp.feed.view.FeedFragment
import com.doubtnutapp.home.model.StudentRatingPopUp
import com.doubtnutapp.model.AppEvent
import com.doubtnutapp.newglobalsearch.ui.InAppSearchActivity
import com.doubtnutapp.notification.NotificationCenterActivity
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.survey.ui.fragments.UserSurveyBottomSheetFragment
import com.doubtnutapp.ui.FragmentHolderActivity
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.browser.CustomTabActivityHelper
import com.doubtnutapp.ui.browser.WebViewFallback
import com.doubtnutapp.ui.games.DnGamesActivity
import com.doubtnutapp.ui.games.GamePlayerActivity
import com.doubtnutapp.ui.main.OcrFromImageDialog
import com.doubtnutapp.ui.onboarding.SelectClassFragment
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.widgets.base.BaseWidgetBottomSheetDialogFragment
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.uxcam.UXCam
import dagger.Lazy
import dagger.android.support.DaggerFragment
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import javax.inject.Inject

@FlowPreview
class HomeFeedFragmentV2 : DaggerFragment(), ActionPerformer, View.OnClickListener {

    companion object {
        private const val REQUEST_CODE_SELECT_CLASS = 101
        private const val REQUEST_CODE_LOGIN_CLASS = 102

        const val TAG = "HomeFeedFragmentV2"
        const val EVENT_TAG = "home_feed_fragment_v2"
        fun newInstance() = HomeFeedFragmentV2()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var networkErrorHandler: NetworkErrorHandler

    @Inject
    lateinit var deeplinkAction: Lazy<DeeplinkAction>

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var defaultDataStore: Lazy<DefaultDataStore>

    private lateinit var homeFragmentViewModel: HomeFragmentViewModel
    private lateinit var mainActivityViewModel: MainViewModel
    private lateinit var dnrRewardViewModel: DnrRewardViewModel
    private lateinit var trialHeaderVM: TrialHeaderVM

    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    private lateinit var eventTracker: Tracker

    private var dialog: DialogFragment? = null

    private var classesList: ArrayList<ClassListViewItem> = ArrayList()

    private var searchHintMap = mapOf<String, List<String>?>()

    private lateinit var mListener: SharedPreferences.OnSharedPreferenceChangeListener

    var showNoticeBoard: Boolean = false
    private var appStateObserver: Disposable? = null

    private lateinit var binding: FragmentHomeV2Binding

    private val selectClassLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                updateClassAtTop()
                setupFeed()
            }
        }

    private val cameraScreenResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent = result.data ?: return@registerForActivityResult
                val screenName = data.getStringExtra(Constants.SCREEN_NAME)
                if (screenName == CameraActivity.TAG) {
                    val shouldClose =
                        data.getBooleanExtra(CameraActivity.FINISH_CALLING_ACTIVITY, false)
                    if (shouldClose) {
                        requireActivity().finish()
                    } else {
                        // Pass Apxor event
                        homeFragmentViewModel.publishEventWith(
                            EventConstants.BACK_FROM_CAMERA
                        )
                        getDnrRewardData()
                    }
                }
            }
        }

    private fun openCameraScreen() {
        cameraScreenResultLauncher.launch(getCameraIntent())
    }

    private fun getCameraIntent() =
        CameraActivity.getStartIntent(
            context = requireContext(),
            source = TAG,
            isUserOpened = true
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeV2Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeFragmentViewModel = viewModelProvider(viewModelFactory)
        dnrRewardViewModel = viewModelProvider(viewModelFactory)
        mainActivityViewModel = activityViewModelProvider(viewModelFactory)
        trialHeaderVM = activityViewModelProvider(viewModelFactory)
        eventTracker = getTracker()

        val countToSendEvent: Int =
            Utils.getCountToSend(
                RemoteConfigUtils.getEventInfo(),
                EventConstants.EVENT_NAME_HOME_IN_BOTTOM_CLICK
            )
        repeat((0 until countToSendEvent).count()) {
            sendEvent(EventConstants.EVENT_NAME_HOME_IN_BOTTOM_CLICK)
        }
        homeFragmentViewModel.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NAME_APP_SESSION_STARTED,
                hashMapOf(),
                ignoreSnowplow = true
            )
        )

        // Set Screen name for UxCam
        UXCam.tagScreenName(TAG)
        setupUI()

        showNoticeBoard =
            FeaturesManager.isFeatureEnabled(requireContext(), Features.NOTICE_BOARD)
                    || defaultPrefs(requireContext()).getBoolean(
                NoticeBoardConstants.NB_HOME_ENABLED,
                false
            )

        appStateObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            if (it is UnreadNoticeCountUpdate) {
                updateUnreadNoticeCount()
            }
        }

        if (showNoticeBoard) {
            binding.ivNoticeBoard.show()
            binding.ivNoticeBoard.setOnClickListener {
                homeFragmentViewModel.publishEvent(
                    AnalyticsEvent(
                        name = EventConstants.DN_NB_ICON_CLICKED,
                        params = hashMapOf(
                            EventConstants.SOURCE to EventConstants.EVENT_SOURCE_HOME,
                            EventConstants.STUDENT_ID to getStudentId(),
                            EventConstants.STUDENT_CLASS to getStudentClass(),
                            EventConstants.BOARD to UserUtil.getUserBoard(),
                        ),
                        ignoreFacebook = true
                    )
                )
                startActivity(NoticeBoardDetailActivity.getStartIntent(requireContext()))
            }
            homeFragmentViewModel.publishEvent(
                AnalyticsEvent(
                    name = EventConstants.DN_NB_ICON_VISIBLE,
                    params = hashMapOf(
                        EventConstants.SOURCE to EventConstants.EVENT_SOURCE_HOME,
                        EventConstants.STUDENT_ID to getStudentId(),
                        EventConstants.STUDENT_CLASS to getStudentClass(),
                        EventConstants.BOARD to UserUtil.getUserBoard(),
                    ),
                    ignoreFacebook = true
                )
            )
        } else {
            binding.ivNoticeBoard.hide()
        }

        binding.rvWidget.visibility = View.GONE
        if (showNoticeBoard.not()) {
            binding.notificationLayout1.hide()
            binding.notificationBell1.hide()
            binding.tvNotificationCount1.hide()
        }

    }

    private fun updateUnreadNoticeCount() {
        if (!showNoticeBoard) {
            binding.tvNoticeBoard.hide()
            return
        }
        if (NoticeBoardRepository.unreadNoticeIds.isEmpty()) {
            binding.tvNoticeBoard.text = ""
            binding.tvNoticeBoard.hide()
        } else {
            binding.tvNoticeBoard.text = NoticeBoardRepository.unreadNoticeIds.size.toString()
            binding.tvNoticeBoard.show()
        }
    }

    private fun setOfflineLayout() {
        val isOfflineVideoEnable =
            FeaturesManager.isFeatureEnabled(requireContext(), Features.OFFLINE_VIDEOS)
        binding.tvNoInternet.setText(if (isOfflineVideoEnable) R.string.watch_downloaded_video_message else R.string.no_internet_message)
        binding.actionGotoDownloads.isVisible = isOfflineVideoEnable
    }

    @FlowPreview
    private fun setupUI() {
        if (NetworkUtils.isConnected(requireContext()).not()) {
            binding.offlineLayout.show()
            binding.actionGotoDownloads.setOnClickListener {
                openMyDownloads()
            }
            binding.actionRetry.setOnClickListener {
                setupUI()
            }
        } else {
            binding.offlineLayout.hide()
            setObservers()
            getPopUpList()
            getBottomSheetData()
            setListeners()
            getActiveFeedback()
            setHeadereText()
            setSearchViewBaseOnRemoteConfig()
            setupFeed()
            fetchClassesList()
        }
        setUpCameraAnimation()
        updateClassAtTop()
        setOfflineLayout()
    }

    private fun setUpCameraAnimation() {
        binding.cameraAnimation.setAnimation("lottie_cam_anim.zip")
        binding.cameraAnimation.repeatCount = LottieDrawable.INFINITE
//        binding.cameraAnimation.imageAssetsFolder = "camera_animation_images"
        binding.cameraAnimation.playAnimation()
    }

    private fun openMyDownloads() {
        homeFragmentViewModel.publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_OFFLINE_GO_TO_DOWNLOADS,
                ignoreFacebook = false
            )
        )
        val intent = DownloadedVideosActivity.getStartIntent(requireContext())
        startActivity(intent)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (context != null && activity != null) {
            FirebaseAnalytics.getInstance(requireContext())
                .setCurrentScreen(requireActivity(), TAG, TAG)
        }

        if (NetworkUtils.isConnected(requireContext()) &&
            !DoubtnutApp.INSTANCE.isInAppDialogShowing
        ) {
            homeFragmentViewModel.checkOcrFromDb()
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

    override fun onStop() {
        super.onStop()
        if (this::mListener.isInitialized) {
            defaultPrefs().unregisterOnSharedPreferenceChangeListener(mListener)
        }
    }

    private fun setNotificationCount() {
        val notificationCount = defaultPrefs().getString(Constants.UNREAD_NOTIFICATION_COUNT, "0")
            .orDefaultValue()
        if (notificationCount != "0") {
            binding.tvNotificationCount1.text = notificationCount
            if (showNoticeBoard) {
                binding.tvNotificationCount1.show()
            } else {
                binding.tvNotificationCount1.hide()
            }
        } else {
            binding.tvNotificationCount1.hide()
        }
    }

    private fun fetchClassesList() {
        homeFragmentViewModel.getClassesList()
    }

    private fun setupFeed() {
        val feedFragment = FeedFragment.newInstance(true, FeedFragment.SOURCE_HOME)

        childFragmentManager.beginTransaction().replace(R.id.homeFeedContainer, feedFragment)
            .commit()
    }

    private fun setSearchViewBaseOnRemoteConfig() {
        binding.globalSearch.isEnabled = false
        setSearchAnimations()
    }

    private fun setSearchAnimations() {
        homeFragmentViewModel.getSearchHintData()
        binding.typeWriter.apply {
            isRepeat = true
            setCharacterDelay(100)
            setChangeStringDelay(1000)
            setRepeatAnimDelay(5)
        }
    }

    private fun setHeadereText() {
        val userClass = defaultPrefs(requireContext()).getString(Constants.STUDENT_CLASS, "") ?: ""
        val hashMap = UserUtil.getConfigMap() ?: HashMap()
        binding.tvSubTitle.text = (hashMap[userClass] as? String?)
            ?: getString(R.string.title_homeFeed_turantMilegaSolution)
        binding.tvTitle.text = (hashMap["home_page_camera_title"] as? String?)
            ?: getString(R.string.ask_doubt)
    }

    private fun getPopUpList() {
        homeFragmentViewModel.getPopUpList()
    }

    private fun getBottomSheetData() {
        homeFragmentViewModel.getBottomSheetData()
    }

    override fun onStart() {
        super.onStart()
        setNotificationCount()
        setPreferenceChangeListener()
    }

    private var isUserSurveyShown = false

    override fun onPause() {
        super.onPause()
        if (mainActivityViewModel.checkUserSurvey.hasObservers()) {
            mainActivityViewModel.checkUserSurvey.removeObserver(observer)
        }
    }

    private val observer = Observer<ApiCheckSurvey> { apiCheckSurvey ->
        apiCheckSurvey.surveyId?.let { surveyId ->
            if (isUserSurveyShown.not()) {
                childFragmentManager.beginTransaction().add(
                    UserSurveyBottomSheetFragment.newInstance(
                        surveyId = surveyId,
                        page = "HOME_PAGE"
                    ),
                    UserSurveyBottomSheetFragment.TAG
                ).commit()
                isUserSurveyShown = !isUserSurveyShown
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val imageUrl = defaultPrefs(requireContext()).getString("image_url", "") ?: ""
        if (imageUrl.isNotBlank()) {
            binding.ivUserProfile.loadImage(imageUrl, R.drawable.ic_person_grey)
        }
        playSearchAnimation()
        if (DoubtnutApp.INSTANCE.isOnboardingStarted && !DoubtnutApp.INSTANCE.isOnboardingCompleted) {
            setupUI()
        }

        if (isUserSurveyShown.not()) {
            mainActivityViewModel.checkUserSurvey.observe(this, observer)
            mainActivityViewModel.checkUserSurvey("HOME_PAGE", null)
        }
        updateUnreadNoticeCount()
    }

    private fun playSearchAnimation() {
        val list = searchHintMap[defaultPrefs().getString(Constants.STUDENT_CLASS, "") ?: ""]
        if (!list.isNullOrEmpty()) {
            binding.typeWriter.setTexts(list)
            binding.typeWriter.setTexts(list)
            binding.typeWriter.playAnim()
        }

    }

    private fun getActiveFeedback() {

        getDatabase(context)?.eventsDao()
            ?.getPendingEventsForTrigger("home", listOf("homepage_continue_watching"))
            ?.observe(viewLifecycleOwner) {
                val events = it;
                if (events.isNotEmpty()) {
                    val event = it[0]
                    if (event.deeplinkUrl.isNotEmpty()) {
                        //prevent showing bottom sheet again if already showing
                        if (requireActivity().supportFragmentManager.findFragmentByTag(
                                BaseWidgetBottomSheetDialogFragment.TAG
                            ) == null
                        ) {
                            deeplinkAction.get().performAction(requireContext(), event.deeplinkUrl)
                            homeFragmentViewModel.publishEventWith(EventConstants.BOTTOM_SHEET_OPEN_FOR_CONTINUE_WATCHING_WIDGET)
                            DoubtnutApp.INSTANCE.runOnDifferentThread {
                                getDatabase(context)?.eventsDao()?.delete(event)
                            }
                        }
                    }

                }
            }

        getDatabase(context)?.eventsDao()
            ?.getPendingEventsForTrigger("home", listOf("notification_setting"))
            ?.observe(viewLifecycleOwner) {
                showBottomDialog(it)
            }

        getDatabase(context)?.eventsDao()
            ?.getPendingEventsForTrigger("home", listOf("video", "playlist"))
            ?.observe(viewLifecycleOwner) {
                showBottomDialog(it)
            }
    }

    @Synchronized
    private fun showBottomDialog(it: List<AppEvent>?) {
        if (activity?.isRunning() == true && it != null && it.isNotEmpty() && dialog == null && dialog?.isVisible != true
            && !DoubtnutApp.INSTANCE.isInAppDialogShowing && !DoubtnutApp.INSTANCE.isOnboardingStarted
        ) {
            val event = it[0]
            dialog = ActionHandler.handleEvent(childFragmentManager, event)
            DoubtnutApp.INSTANCE.runOnDifferentThread {
                getDatabase(context)?.eventsDao()?.delete(event)
            }
            if (event.event == "playlist") {
                homeFragmentViewModel.publishEventWith(EventConstants.NCERT_RE_ENTRY_HOME)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {

            binding.askQuestionLayout -> {
                homeFragmentViewModel.publishCameraButtonClickEvent(EventConstants.HOME_PAGE_TOP)
                openCameraScreen()
                sendEvent(EventConstants.EVENT_NEW_HOME_ASK_QUESTION_CLICK_WITH_FULL_ACTION_BAR)
            }

            binding.typeWriter -> openSearchActivity(false)

            binding.btnVoiceSearch -> openSearchActivity(true)

            binding.ivUserProfile -> {
                if (activity != null && activity is MainActivity) {
                    homeFragmentViewModel.publishEventWith(
                        EventConstants.EVENT_NAME_HAMBURGER_CLICK,
                        ignoreSnowplow = true
                    )
                    val imm =
                        activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(
                        activity?.currentFocus?.windowToken,
                        0
                    )
                    (activity as MainActivity).openDrawer()
                }
            }

            binding.notificationBell1 -> {
                (activity as? MainActivity)?.startActivityForResult(
                    NotificationCenterActivity
                        .getStartIntent(requireContext(), NotificationCenterActivity.HOME),
                    NotificationCenterActivity.REQUEST_CODE_NOTIFICATION
                )
            }

            binding.tvClassChange -> {
                val intent = Intent(context, FragmentHolderActivity::class.java).apply {
                    action = Constants.NAVIGATE_CLASS_FRAGMENT_FROM_NAV
                }
                selectClassLauncher.launch(intent)
            }
        }
    }

    private fun updateClassAtTop() {
        binding.tvClassChange.text = defaultPrefs().getString(Constants.STUDENT_CLASS_DISPLAY, "")
    }

    fun requestClassSelection() {
        val args = Bundle()
        args.putBoolean(SelectClassFragment.INTENT_SOURCE_LIBRARY, false)
        homeFragmentViewModel.publishEventWith(
            EventConstants.EVENT_NAME_OPEN_CLASS_PAGE_HOME,
            ignoreSnowplow = true
        )
        screenNavigator.startActivityForResultFromFragment(
            this,
            ClassSelectionScreen,
            null,
            REQUEST_CODE_SELECT_CLASS
        )
        sendEvent(EventConstants.EVENT_NAME_OPEN_CLASS_PAGE_HOME)
    }

    override fun performAction(action: Any) {
        context?.let {
            homeFragmentViewModel.handleAction(action, it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_SELECT_CLASS) {
            updateClassText()
            updateClassAtTop()
            if (showNoticeBoard) {
                binding.ivNoticeBoard.show()
            } else {
                binding.ivNoticeBoard.hide()
            }
            updateUnreadNoticeCount()
            binding.rvWidget.visibility = View.GONE

            if (showNoticeBoard.not()) {
                binding.notificationLayout1.hide()
                binding.notificationBell1.hide()
                binding.tvNotificationCount1.hide()
            }

        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_LOGIN_CLASS) {
            homeFragmentViewModel.getIncompleteChapter()
            setupFeed()
        }
    }

    private fun updateClassText() {
        setHeadereText()
        homeFragmentViewModel.getIncompleteChapter()
        setupFeed()
    }

    private fun openSearchActivity(startVoiceSearch: Boolean) {
        val userClass = defaultPrefs().getString(STUDENT_CLASS, "")?.toIntOrNull()
        val selectedClass =
            if (userClass == null) null else classesList.find { userClass == it.classNo }
        Utils.executeIfContextNotNull(context) { context: Context ->
            InAppSearchActivity.startActivity(
                context,
                TAG,
                startVoiceSearch,
                classesList = classesList,
                selectedClass = selectedClass
            )
        }
        sendEvent(EventConstants.EVENT_NAME_SEARCH_ICON_CLICK)
        homeFragmentViewModel.publishSearchIconEvent(TAG)
    }

    @FlowPreview
    private fun setObservers() {
        homeFragmentViewModel.whatsAppShareableData.observe(viewLifecycleOwner) {
            it?.getContentIfNotHandled()?.let {
                val (deepLink, imagePath, sharingMessage) = it
                deepLink?.let {
                    shareOnWhatsApp(deepLink, imagePath, sharingMessage)
                } ?: showBranchLinkError()
            }
        }

        mainActivityViewModel.askMeButtonObserver.observe(viewLifecycleOwner) { isPressed ->
            if (isPressed) scrollToTop()
        }

        homeFragmentViewModel.pdfUriLiveData.observe(viewLifecycleOwner) {
            when (it) {
                null -> {
                    toast(getString(R.string.somethingWentWrong))
                    homeFragmentViewModel.showShareProgressBar.value = false
                }
                else -> {
                    val (file, text) = it
                    homeFragmentViewModel.showShareProgressBar.value = false
                    sharePdfOnWhatsApp(file, text)
                }
            }
        }

        homeFragmentViewModel.navigateLiveData.observe(viewLifecycleOwner) {
            val navigationData = it.getContentIfNotHandled()
            if (navigationData != null) {
                val args: Bundle? = navigationData.hashMap?.toBundle()

                context?.let { activityContext ->

                    if (navigationData.screen == LoginScreen) {
                        screenNavigator.startActivityForResultFromFragment(
                            this,
                            navigationData.screen,
                            args,
                            REQUEST_CODE_LOGIN_CLASS
                        )

                    } else if (navigationData.screen == ExternalUrlScreen) {
                        if (AppUtils.appInstalledOrNot(
                                Constants.WHATSAPP_PACKAGE_NAME,
                                requireActivity()
                            )
                        ) {
                            screenNavigator.startActivityForResultFromFragment(
                                this,
                                navigationData.screen,
                                args,
                                REQUEST_CODE_LOGIN_CLASS
                            )
                        } else {
                            toast(getString(R.string.string_install_whatsApp))
                        }
                    } else if (navigationData.screen == LiveClassesScreen ||
                        navigationData.screen == OpenDemoVideoScreen ||
                        navigationData.screen == VideoYouTubeScreen
                    ) {
                        screenNavigator.startActivityForResultFromFragment(
                            this,
                            navigationData.screen,
                            args,
                            REQUEST_CODE_SELECT_CLASS
                        )

                    } else if (navigationData.screen == WebViewScreen) {
                        openWevViewer(args?.getString(Constants.EXTERNAL_URL))
                    } else if (navigationData.screen == DnGamesScreen) {
                        launchGame(
                            args?.getString(Constants.GAME_TITLE),
                            args?.getString(Constants.GAME_URL)
                        )
                    } else {
                        screenNavigator.startActivityFromActivity(
                            activityContext,
                            navigationData.screen,
                            args
                        )
                    }
                }
            }
        }

        homeFragmentViewModel.eventLiveData.observe(viewLifecycleOwner) {
            sendEvent(it)
            sendCleverTapEvent(it)
        }

        homeFragmentViewModel.gridListSubmitMessage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                showToast(requireActivity(), it)
            }
        }

        homeFragmentViewModel.refetchHomeFeed.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                setupFeed()
            }
        })

        homeFragmentViewModel.removeRatingFragmentLiveData.observe(viewLifecycleOwner) {
            if (it) {
                if (childFragmentManager.findFragmentByTag(StudentRatingDialogFragment.FRAGMENT_RATING_TAG) != null) {
                    childFragmentManager
                        .beginTransaction()
                        .remove(childFragmentManager.findFragmentByTag(StudentRatingDialogFragment.FRAGMENT_RATING_TAG)!!)
                        .commit()
                }
                binding.studentDialogFragment.hide()
            }
        }

        homeFragmentViewModel.popUpListLiveData.observeK(
            viewLifecycleOwner,
            this::onPopUpListSuccess,
            networkErrorHandler::onApiError,
            networkErrorHandler::unAuthorizeUserError,
            networkErrorHandler::ioExceptionHandler,
            this::updateProgressBarState
        )

        homeFragmentViewModel.classesListLiveData.observe(viewLifecycleOwner) { outcome ->
            if (outcome is Outcome.Success) {
                updateClassList(outcome.data.classList as ArrayList<ClassListViewItem>)
            }
        }

        homeFragmentViewModel.searchHintMapLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                searchHintMap = it
                playSearchAnimation()
            }
        }

        homeFragmentViewModel.latestOcrFromDb.observe(viewLifecycleOwner) {
            OcrFromImageDialog.newInstance(it.ocr, it.imageUri, it.ts)
                .show(childFragmentManager, OcrFromImageDialog.TAG)
        }

        homeFragmentViewModel.bottomSheetDeeplink.observeNonNull(viewLifecycleOwner) {
            deeplinkAction.get().performAction(requireContext(), it)
        }

        mainActivityViewModel.updateClassOnHomeFragment.observe(viewLifecycleOwner) {
            updateClassAtTop()
        }

        mainActivityViewModel.showDnrRewardLiveData.observe(viewLifecycleOwner) {
            if (it) {
                getDnrRewardData()
            }
        }

        mainActivityViewModel.dnrData.observe(viewLifecycleOwner) {
            updateDnrData(it)
        }

        viewLifecycleOwner.lifecycleScope.launchWhenResumed {
            trialHeaderVM.showTrialHeader
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .debounce(1000)
                .collectLatest {
                    binding.containerTrialInfo.isVisible = it.first == true && it.second != null
                    it.second?.let { item ->
                        binding.containerTrialInfo.background =
                            GradientUtils.getGradientBackground(
                                item.bgColorOne,
                                item.bgColorTwo,
                                GradientDrawable.Orientation.LEFT_RIGHT
                            )

                        binding.containerTrialInfo.setOnClickListener {
                            deeplinkAction.get().performAction(requireContext(), item.deeplink)
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    EVENT_TAG + "_trial_info_container_clicked",
                                    hashMapOf<String, Any>(
                                        EventConstants.ASSORTMENT_ID to item.assortmentId.orEmpty()
                                    ).apply {
                                        putAll(item.extraParams.orEmpty())
                                    }
                                )
                            )
                        }

                        if (item.trialTitle.isNullOrEmpty()) {
                            binding.tvTrialInfo.visibility = View.GONE
                        } else {
                            binding.tvTrialInfo.visibility = View.VISIBLE
                            binding.tvTrialInfo.text = item.trialTitle2
                            binding.tvTrialInfo.applyTextSize(item.trialTitleSize)
                            binding.tvTrialInfo.applyTextColor(item.trialTitleColor)
                        }

                        if (item.imageUrl.isNullOrEmpty()) {
                            binding.ivGif.visibility = View.GONE
                        } else {
                            binding.ivGif.visibility = View.VISIBLE
                            Glide.with(requireContext()).load(item.imageUrl).into(binding.ivGif)
                        }

                        if (item.time == null || item.time <= 0L) {
                            binding.tvTimer.visibility = View.GONE
                            binding.ivGif.visibility = View.GONE
                        } else {
                            val actualTimeLeft =
                                ((item.time.or(0)).minus(System.currentTimeMillis()))

                            if (actualTimeLeft > 0) {
                                binding.tvTimer.visibility = View.VISIBLE
                                binding.tvTimer.applyTextColor(item.timeTextColor)
                                binding.tvTimer.applyTextSize(item.timeTextSize)

                                val timer = object : CountDownTimer(
                                    actualTimeLeft, 1000
                                ) {
                                    override fun onTick(millisUntilFinished: Long) {
                                        view ?: return
                                        binding.tvTimer.text =
                                            DateTimeUtils.formatMilliSecondsToTime(
                                                millisUntilFinished
                                            )
                                    }

                                    override fun onFinish() {
                                        view ?: return
                                        binding.ivGif.visibility = View.GONE
                                        binding.tvTimer.visibility = View.GONE

                                        binding.containerTrialInfo.background =
                                            GradientUtils.getGradientBackground(
                                                item.bgColorOneExpired,
                                                item.bgColorTwoExpired,
                                                GradientDrawable.Orientation.LEFT_RIGHT
                                            )
                                        binding.tvTrialInfo.text = item.trialTitleExpired2

                                        item.trialTitle2 = item.trialTitleExpired2
                                        item.bgColorOne = item.bgColorOneExpired
                                        item.bgColorTwo = item.bgColorTwoExpired
                                    }
                                }
                                timer.start()
                            } else {
                                binding.tvTimer.visibility = View.GONE
                                binding.ivGif.visibility = View.GONE

                                binding.containerTrialInfo.background =
                                    GradientUtils.getGradientBackground(
                                        item.bgColorOneExpired,
                                        item.bgColorTwoExpired,
                                        GradientDrawable.Orientation.LEFT_RIGHT
                                    )
                                binding.tvTrialInfo.text = item.trialTitleExpired2

                                item.trialTitle2 = item.trialTitleExpired2
                                item.bgColorOne = item.bgColorOneExpired
                                item.bgColorTwo = item.bgColorTwoExpired
                            }
                        }
                    }
                }
        }
    }

    private fun getDnrRewardData() {
        lifecycleScope.launchWhenStarted {
            val isNewUser = defaultDataStore.get().isNewUser.firstOrNull() ?: false
            when (isNewUser && defaultDataStore.get().isNewUserClaimedSignupDnrReward.firstOrNull() != true) {
                true -> {
                    dnrRewardViewModel.claimReward(
                        DnrSignupReward(
                            referralCouponCode = "",
                            type = DnrRewardType.SIGN_UP.type
                        )
                    )
                }
                else -> {
                    if (defaultDataStore.get().shouldShowDnrStreakRewardPopUp.firstOrNull() == true) {
                        dnrRewardViewModel.claimReward(
                            DnrStreakReward(
                                type = DnrRewardType.DAILY_STREAK.type
                            )
                        )
                    } else {
                        dnrRewardViewModel.claimReward(
                            ReferAndEarnReward(type = DnrRewardType.REFER_AND_EARN_REWARD.type)
                        )
                    }
                }
            }
        }

        dnrRewardViewModel.dnrRewardLiveData.observeEvent(viewLifecycleOwner) {
            showDnrReward(it)
        }
    }

    private fun showDnrReward(dnrReward: DnrReward) {
        if (dnrReward.type == DnrRewardType.SIGN_UP.type) {
            lifecycleScope.launchWhenStarted {
                defaultDataStore.get().set(DefaultDataStoreImpl.DNR_NEW_USER_REWARD, true)
            }
        }

        dnrRewardViewModel.checkRewardPopupToBeShown(dnrReward)
        dnrRewardViewModel.dnrRewardPopupLiveData.observeEvent(this) { rewardPopupType ->
            when (rewardPopupType) {
                DnrRewardViewModel.RewardPopupType.NO_POPUP -> {
                    return@observeEvent
                }
                DnrRewardViewModel.RewardPopupType.REWARD_BOTTOM_SHEET -> {
                    DnrRewardBottomSheetFragment.newInstance(dnrReward)
                        .show(childFragmentManager, DnrRewardBottomSheetFragment.TAG)
                }
                DnrRewardViewModel.RewardPopupType.REWARD_DIALOG -> {
                    DnrRewardDialogFragment.newInstance(dnrReward)
                        .show(childFragmentManager, DnrRewardDialogFragment.TAG)
                }
            }
        }
    }

    private fun updateClassList(dataList: ArrayList<ClassListViewItem>) {
        classesList = dataList
    }

    private fun openWevViewer(url: String?) {
        if (url.isNullOrEmpty())
            showApiErrorToast(context)
        else {
            homeFragmentViewModel.publishEventWith(
                EventConstants.EVENT_EASY_READER_WEBVIEWER_OPEN, params = hashMapOf(
                    EventConstants.SOURCE to "home"
                )
            )
            CustomTabActivityHelper.openCustomTab(
                context,
                CustomTabsIntent.Builder().build(),
                url.toUri(),
                WebViewFallback()
            )
        }
    }

    private fun updateDnrData(dnr: ApiOnBoardingStatus.ApiDnr?) {
        binding.dnrRupyaLayout.isVisible = dnr != null
        if (dnr == null) return
        binding.ivDnrRupya.loadImage(dnr.image)
        binding.dnrRupyaLayout.setOnClickListener {
            homeFragmentViewModel.publishEventWith(
                EventConstants.APP_BAR_DNR_ICON_CLICK,
                ignoreSnowplow = true
            )
            when (defaultPrefs().getBoolean(Constants.DNR_TOOLTIP_POPUP, false)) {
                true -> {
                    val deeplink = dnr.deeplink ?: return@setOnClickListener
                    deeplinkAction.get().performAction(requireContext(), deeplink)
                }
                else -> {
                    DnrPopupDialogFragment.newInstance(
                        DnrPopupDialogData(
                            imageUrl = dnr.image,
                            title = dnr.title,
                            description = dnr.description,
                            ctaText = dnr.ctaText,
                            deeplink = dnr.deeplink
                        )
                    ).show(childFragmentManager, DnrPopupDialogFragment.TAG)
                }
            }
        }
    }

    private fun launchGame(title: String?, url: String?) {
        if (url.isNullOrEmpty()) {
            homeFragmentViewModel.publishEventWith(
                EventConstants.EVENT_GAME_SECTION_VIEW, params = hashMapOf(
                    EventConstants.SOURCE to EventConstants.EVENT_SOURCE_HOME
                ), ignoreSnowplow = true
            )
            startActivity(Intent(requireContext(), DnGamesActivity::class.java))
        } else {
            homeFragmentViewModel.publishEventWith(
                EventConstants.EVENT_GAME_CLICK, params = hashMapOf(
                    EventConstants.EVENT_GAME to (title ?: ""),
                    EventConstants.SOURCE to EventConstants.EVENT_SOURCE_HOME
                )
            )
            startActivity(GamePlayerActivity.getIntent(requireContext(), title, url))
        }
    }

    private fun setListeners() {
        binding.typeWriter.setOnClickListener(this)
        binding.askQuestionLayout.setOnClickListener(this)
        binding.ivUserProfile.setOnClickListener(this)
        binding.notificationBell1.setOnClickListener(this)
        binding.btnVoiceSearch.setOnClickListener(this)
        binding.tvClassChange.setOnClickListener(this)
    }

    private fun showBranchLinkError() {
        context?.getString(R.string.error_branchLinkNotFound)?.let { msg ->
            toast(msg)
            homeFragmentViewModel.showShareProgressBar.value = false
        }
    }

    private fun onPopUpListSuccess(popUpList: List<PopUpDialog>) {
        popUpList.forEach { popUpDialog ->
            when (popUpDialog) {
                is StudentRatingPopUp -> {
                    if (popUpDialog.shouldShow && popUpDialog.subData != null && popUpDialog.subData.showGoogleReview) {
                        showInAppReview()
                    } else if (popUpDialog.shouldShow && popUpDialog.subData != null) {
                        DoubtnutApp.INSTANCE.isRatingDialogStarted = true
                        binding.studentDialogFragment.show()
                        childFragmentManager.beginTransaction().apply {
                            add(
                                R.id.studentDialogFragment,
                                StudentRatingDialogFragment.newInstance(popUpDialog.subData),
                                StudentRatingDialogFragment.FRAGMENT_RATING_TAG
                            )
                            commit()
                        }
                        try {
                            homeFragmentViewModel.publishEventWith(
                                EventConstants.EVENT_RATING_WIDGET_VISIBLE,
                                true
                            )
                        } catch (e: Exception) {

                        }
                    }
                }
            }
        }
    }

    private fun showInAppReview() {
        if (context == null
            || DoubtnutApp.INSTANCE.isRatingDialogStarted
        ) {
            return
        }
        val manager = ReviewManagerFactory.create(requireContext())
        val reviewRequest = manager.requestReviewFlow()
        reviewRequest.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                // We got the ReviewInfo object
                val reviewInfo = request.result
                if (activity != null) {
                    DoubtnutApp.INSTANCE.isInAppDialogShowing = true
                    val flow = manager.launchReviewFlow(requireActivity(), reviewInfo)
                    homeFragmentViewModel.publishEvent(
                        AnalyticsEvent(
                            EventConstants.GOOGLE_IN_APP_REVIEW_VIEW,
                            ignoreSnowplow = true
                        )
                    )
                    flow.addOnCompleteListener { task ->
                        // The flow has finished. The API does not indicate whether the user
                        // reviewed or not, or even whether the review dialog was shown. Thus, no
                        // matter the result, we continue our app flow.
                        homeFragmentViewModel.submitInAppReviewCompletion()
                        homeFragmentViewModel.publishEvent(
                            AnalyticsEvent(
                                EventConstants.GOOGLE_IN_APP_REVIEW_COMPLETION,
                                ignoreSnowplow = true
                            )
                        )
                        DoubtnutApp.INSTANCE.isInAppDialogShowing = false
                    }
                    flow.addOnFailureListener {
                        DoubtnutApp.INSTANCE.isInAppDialogShowing = false
                    }
                }
            } else {
                DoubtnutApp.INSTANCE.isInAppDialogShowing = false
            }
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
    }

    private fun shareOnWhatsApp(imageUrl: String, imageFilePath: String?, sharingMessage: String?) {
        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_TEXT, "$sharingMessage $imageUrl")

            if (imageFilePath == null) {
                type = "text/plain"
            } else {
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, Uri.parse(imageFilePath))
            }

        }.also {
            if (AppUtils.isCallable(activity, it)) {
                startActivity(it)
            } else {
                ToastUtils.makeText(
                    requireActivity(),
                    R.string.string_install_whatsApp,
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }

    private fun sharePdfOnWhatsApp(pdfFile: File, extraText: String) {

        val pdfUri = FileProvider.getUriForFile(requireActivity(), BuildConfig.AUTHORITY, pdfFile)

        Intent(Intent.ACTION_SEND).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            type = "application/pdf"
            `package` = "com.whatsapp"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            putExtra(Intent.EXTRA_TEXT, extraText)
        }.also {
            if (AppUtils.isCallable(context, it)) {
                startActivity(it)
                sendEvent(EventConstants.EVENT_HOMEPAGE_SHARE_PDF)
                val fileName = FileUtils.fileNameFromUrl(pdfFile.absolutePath) - FileUtils.EXT_PDF
                sendEvent("${EventConstants.EVENT_HOMEPAGE_SHARE_PDF}$fileName")
            } else {
                context?.let { it1 ->
                    ToastUtils.makeText(
                        it1,
                        R.string.string_install_whatsApp,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun scrollToTop() {
        DoubtnutApp.INSTANCE.bus()?.send(ScrollTopEvent())
        sendEvent(EventConstants.EVENT_NEW_HOME_SCROLL_TO_TOP_CLICK)
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = activity?.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEvent(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addEventParameter(EventConstants.PARAM_CLASS, getStudentClass())
                .addScreenName(EventConstants.EVENT_NAME_NEW_HOME_PAGE)
                .track()
        }
    }

    private fun sendCleverTapEvent(eventName: String) {
        activity?.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(requireActivity()).toString())
                .addStudentId(getStudentId())
                .addEventParameter(EventConstants.PARAM_CLASS, getStudentClass())
                .addScreenName(EventConstants.EVENT_NAME_NEW_HOME_PAGE)
                .cleverTapTrack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        homeFragmentViewModel.homeFeedListLiveData.removeObservers(this)
        appStateObserver?.dispose()
    }

}