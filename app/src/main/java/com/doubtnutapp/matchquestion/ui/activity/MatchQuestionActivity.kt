package com.doubtnutapp.matchquestion.ui.activity

import android.animation.ValueAnimator
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.*
import com.bumptech.glide.Glide
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.LottieAnimDataStore
import com.doubtnut.core.observer.SingleEventObserver
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.LottieAnimationViewUtils.applyAnimationFromUrl
import com.doubtnut.core.utils.ViewPager2Utils.reduceDragSensitivity
import com.doubtnut.core.view.audiotooltipview.AudioTooltipView
import com.doubtnut.core.view.audiotooltipview.AudioTooltipViewData
import com.doubtnut.core.view.mediaplayer.MediaPlayerState
import com.doubtnutapp.*
import com.doubtnutapp.Constants.MATCH_PAGE_SOURCE_BACK_PRESS
import com.doubtnutapp.Constants.SCALE_DOWN_IMAGE_AREA
import com.doubtnutapp.Constants.SCALE_DOWN_IMAGE_QUALITY
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.getDatabase
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.bottomnavigation.model.BottomNavigationTabsData
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.ActivityMatchQuestionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.doubtpecharcha.ui.fragment.P2pHostIntroductionFragment
import com.doubtnutapp.matchquestion.listener.*
import com.doubtnutapp.matchquestion.model.MatchQuestion
import com.doubtnutapp.matchquestion.model.MatchQuestionBanner
import com.doubtnutapp.matchquestion.ui.adapter.MatchQuestionAdapter
import com.doubtnutapp.matchquestion.ui.fragment.MatchPageCarousalsFragment
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionFragment
import com.doubtnutapp.matchquestion.ui.fragment.MatchQuestionWebViewFragment
import com.doubtnutapp.matchquestion.ui.fragment.bottomsheet.AdvanceSearchBottomSheetFragment
import com.doubtnutapp.matchquestion.ui.fragment.dialog.*
import com.doubtnutapp.matchquestion.viewmodel.MatchQuestionViewModel
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.screennavigator.VideoScreen
import com.doubtnutapp.textsolution.ui.TextSolutionActivity
import com.doubtnutapp.ui.ask.SelectImageDialog
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.doubtnutapp.widgets.TriangleBiasEdgeTreatment
import com.doubtnutapp.workmanager.workers.MatchesByFileNameWorker
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import kotlinx.coroutines.flow.firstOrNull
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class MatchQuestionActivity :
    BaseBindingActivity<MatchQuestionViewModel, ActivityMatchQuestionBinding>(),
    View.OnClickListener,
    OnImageSelectListener,
    DialogInterface.OnDismissListener,
    DialogInterface.OnShowListener,
    MatchPageFilterListener,
    FilterDataListener, AudioTooltipView.IMediaPlayerStateCallback {

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var dataStore: DefaultDataStore

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var lottieAnimDataStore: LottieAnimDataStore

    // Start Intent extras
    private val questionText: String by lazy {
        intent.getStringExtra(
            INTENT_EXTRA_TYPED_QUESTION_TEXT
        ) ?: ""
    }
    private val source: String by lazy { intent.getStringExtra(SOURCE) ?: "" }
    private val questionIdFromNotification: String by lazy {
        intent.getStringExtra(Constants.QUESTION_ID) ?: ""
    }
    private val isMatchFromInApp: Boolean by lazy {
        intent.getBooleanExtra(
            INTENT_EXTRA_FROM_IN_APP,
            false
        )
    }
    private val ocrNotificationId: Long? by lazy {
        intent.getStringExtra(
            INTENT_EXTRA_OCR_NOTIFICATION_ID
        )?.toLong()
    }
    private val askedQuestionImageUri: String? by lazy {
        intent.getStringExtra(
            INTENT_EXTRA_ASKED_QUE_URI
        )
    }
    private val askedQuestionImageUrl: String? by lazy {
        intent.getStringExtra(
            INTENT_EXTRA_ASKED_QUE_IMAGE_URL
        )
    }
    private val uploadedImageQuestionId: Long? by lazy {
        intent.getLongExtra(
            INTENT_EXTRA_UPLOADED_IMAGE_QUESTION_ID,
            0L
        )
    }
    // End Intent extras

    private val imageFileName: String by lazy {
        "uploads_" + getStudentId() + "_" + System.currentTimeMillis() / 1000 + ".png"
    }

    private var isBackPressDialogShown: Boolean = false
    private var tabDefaultSelectedOnce = false
    private var isApiRequestInProgress = true
    private var isApiError = false
    private var alertDialog: AlertDialog? = null
    private var autoPlay: Boolean? = null
    private var canShowAdvancedSearch: Boolean = false
    private var questionImageUrl: String? = null
    private var mImageOcrFeedback: String? = null
    private var shouldShowOcrEditScreen: Boolean = false
    private var ocrEditBackpressDialogShown: Boolean = false
    private var screenTime: Long? = null
    private var selectedTab: String? = null
    private var isUserPressedBack = false
    private var audioTooltipPlaying = false

    // OCR Edit experiment properties
    private val mOcrEditDialogUiConfig: OcrEditActivity.UiConfig by lazy {
        getOcrEditBackpressDialogUiConfig()
    }

    companion object {
        const val TAG = "MatchQuestionActivity"
        const val INTENT_EXTRA_TYPED_QUESTION_TEXT = "typed_question_text"
        const val INTENT_EXTRA_ASKED_QUE_URI = "ask_que_uri"
        const val NO_MATCH_FOUND_ERROR_DIALOG = "NoMatchFoundErrorDialog"
        const val BLUR_QUESTION_IMAGE_ERROR_DIALOG = "BlurQuestionImageError"
        const val SOURCE = "SOURCE"
        const val IMAGE_FILE_NAME = "image_file_name"
        const val INTENT_EXTRA_FROM_IN_APP = "match_from_in_app"
        const val INTENT_EXTRA_OCR_NOTIFICATION_ID = "ocr_notification_id"
        const val INTENT_EXTRA_ASKED_QUE_IMAGE_URL = "ask_que_url"
        const val INTENT_EXTRA_UPLOADED_IMAGE_QUESTION_ID = "uploaded_image_question_id"
        const val AUTOPLAY_STATE = "autoplay_state"

        // Calling this intent from asked history, one of the condition must be true.
        // either (questionText != null) or (questionImage and uploadedImageQuestionId) != null.
        fun getStartIntent(
            context: Context,
            quesImageUri: String?,
            questionText: String,
            source: String,
            imageUrl: String? = null,
            uploadedImageQuestionId: Long? = null
        ) =
            Intent(context, MatchQuestionActivity::class.java).apply {
                putExtra(INTENT_EXTRA_ASKED_QUE_URI, quesImageUri)
                putExtra(INTENT_EXTRA_TYPED_QUESTION_TEXT, questionText)
                putExtra(SOURCE, source)
                putExtra(INTENT_EXTRA_ASKED_QUE_IMAGE_URL, imageUrl)
                putExtra(INTENT_EXTRA_UPLOADED_IMAGE_QUESTION_ID, uploadedImageQuestionId)
            }
    }

    override fun provideViewBinding(): ActivityMatchQuestionBinding =
        ActivityMatchQuestionBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): MatchQuestionViewModel = viewModelProvider(viewModelFactory)

    override fun getStatusBarColor(): Int = R.color.libraryDark

    override fun setupView(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        Glide.get(this).clearMemory()
        init()
        sendEventRemoteConfigVariants()
        setUpAutoPlayToggle()
        setUpToolTipAudio()
        defaultPrefs().edit {
            putLong(
                Constants.QUESTION_ASK_COUNT,
                defaultPrefs().getLong(Constants.QUESTION_ASK_COUNT, 0) + 1
            )
        }
    }

    private fun setUpToolTipAudio() {
        if (AudioToolTipUtils.checkForCoreLoopSessionCount(Constants.SCREEN_LOADING)) {
            val list = AudioToolTipUtils.isAudioToolTipAvailable(Constants.SCREEN_LOADING).orEmpty()
            if (list.isNotEmpty()) {
                val audioTooltipViewData =
                    AudioToolTipUtils.buildAudioTooltipViewData(list, Constants.SCREEN_LOADING)
                if (list.isNotEmpty()) {
                    setUpMediaPlayer(audioTooltipViewData)
                }
            } else {
                binding.audioTooltipView.gone()
            }
        } else {
            binding.audioTooltipView.gone()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.matchResultsLiveData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgressBarState
        )

        viewModel.imageBitmapLiveData.observe(this) {
            when (it) {
                is Outcome.Success -> {
                    it.data?.let { bitmap ->
                        onImageSuccess(bitmap)
                    }
                }

                is Outcome.Failure -> {
                    fileNotFoundException()
                }

                else -> {
                }
            }
        }

        viewModel.matchQuestionBannerLiveData.observeK(
            this,
            ::onMatchQuestionBannerData,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateBannerProgressBarState
        )

        viewModel.navigateLiveData.observeEvent(this, this::openScreen)

        viewModel.message.observe(this) {
            if (it.isNotNullAndNotEmpty()) showToast(this, it.orEmpty())
        }

        viewModel.showFilterBottomSheet.observe(this) {
            if (it) {
                showFilterBottomSheet()
            }
        }

        viewModel.matchPageScrollDirection.observe(this) {
            /**
             * change visibility of bottom layout based on scrolling events
             * @see MatchQuestionViewModel.ScrollDirection
             */

            when (it) {
                MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN -> {
                    setVisibilityOfBottomLayer(isVisible = false)
                }
                MatchQuestionViewModel.ScrollDirection.SCROLL_UP,
                MatchQuestionViewModel.ScrollDirection.SCROLL_DOWN_NONE,
                MatchQuestionViewModel.ScrollDirection.SCROLL_UP_NONE
                -> {
                    setVisibilityOfBottomLayer(isVisible = true)
                }
                else -> {
                    setVisibilityOfBottomLayer(isVisible = false)
                }
            }
        }

        viewModel.advancedSearchOptionsLiveData.observe(this) {
            if (it.displayFilter && it.facetList.isNotEmpty()) {
                canShowAdvancedSearch = true
                binding.matchFilterFacetListView.apply {
                    show()
                    setMatchPageFilterListener(this@MatchQuestionActivity)
                    setFilterDataListener(this@MatchQuestionActivity)
                    bindData(it)
                }
                binding.appBarLayout.setExpanded(false)
            }
        }

        viewModel.showNoInternetActivity.observe(this) {
            if (it.first) {
                startActivityForResult(NoInternetRetryActivity.getStartIntent(this), it.second)
            }
        }

        viewModel.uploadImageError.observe(this) {
            toast(it)
        }

        viewModel.popupDeeplink.observe(this, SingleEventObserver { popupDeeplink ->
            deeplinkAction.performAction(this@MatchQuestionActivity, popupDeeplink)
        })
    }

    override fun onImageSelected(selectedBitmap: Bitmap, rotationAngle: Int) {
        viewModel.questionMetaData = MatchQuestionViewModel.ImageMetaData(
            rotationAngle = rotationAngle,
            sendEvent = true
        )
        binding.questionImage.setImageBitmap(selectedBitmap)
        binding.questionProgress.show()
        setProgressBitmap(selectedBitmap)
        toGreyScaleAndGetMatches(selectedBitmap)
    }

    override fun showMatchFilterFragment() {
        viewModel.showFilterBottomSheet.postValue(true)
        viewModel.sendEvent(EventConstants.EVENT_ALL_FILTERS_SELECTED, hashMapOf())
    }

    override fun onUpdate(
        topicPosition: Int,
        isTopicSelected: Boolean,
        toRefresh: Boolean,
        facetPosition: Int
    ) {
        viewModel.updateMatchFilter(
            topicPosition,
            isTopicSelected,
            toRefresh,
            facetPosition
        )
    }

    override fun clearFilter() {
        viewModel.clearMatchFilter()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (isUserPressedBack.not()) {
            viewModel.sendMatchPageExitEvent("home_key")
        }
        isUserPressedBack = false
    }

    override fun onResume() {
        super.onResume()
        // Reset time onResume
        screenTime = System.currentTimeMillis()
    }

    override fun onStop() {
        super.onStop()
        if (isApiRequestInProgress) {
            viewModel.sendEvent(
                EventConstants.ASK_QUESTION_BACK_EXIT_BEFORE_MATCHES,
                hashMapOf()
            )
        }
        if (!viewModel.isAnySolutionWatched && viewModel.isMatchResponseFetched) {
            var counter = defaultPrefs(this).getInt(Constants.VIDEO_NOT_WATCHED_COUNTER, 0)
            counter++
            defaultPrefs(this).edit {
                putInt(Constants.VIDEO_NOT_WATCHED_COUNTER, counter)
            }
            DoubtnutApp.INSTANCE.runOnDifferentThread {
                val count = getDatabase()?.feedbackDao()
                    ?.getCountForType(Constants.FEEDBACK_TYPE_ASKED_NOT_WATCHED)

                if (count != null && count != 0 && counter >= count) {
                    getDatabase()?.feedbackDao()?.updateFeedbackStatus(
                        "active",
                        Constants.FEEDBACK_TYPE_ASKED_NOT_WATCHED
                    )
                    defaultPrefs(this).edit {
                        putInt(Constants.VIDEO_NOT_WATCHED_COUNTER, 0)
                    }
                }
            }
        }

        // Pause animation if user leaves this screen
        binding.loaderAnimation.pauseAnimation()

        viewModel.addScreenTime(selectedTab, screenTime)
        // Reset screen time
        screenTime = null

        viewModel.sendEventForTabScreenTime()
    }

    override fun onShow(dialog: DialogInterface?) {
        viewModel.isDialogShowing = true
    }

    override fun onDismiss(dialog: DialogInterface?) {
        viewModel.isDialogShowing = false
    }

    override fun onUserInteraction() {
        binding.tvEditOcrCoachmark.hide()
    }

    override fun onStart() {
        super.onStart()
        if (isApiRequestInProgress) {
            binding.loaderAnimation.resumeAnimation()
        }
    }

    override fun onBackPressed() {

        if (UserUtil.getIsGuestLogin() || isApiError) {
            finish()
            return
        }

        isUserPressedBack = true

        // If user presses back button while loading, show dialog for confirmation
        if (isApiRequestInProgress) {
            viewModel.sendEvent(
                EventConstants.ASK_QUESTION_BACK_PRESS_BEFORE_MATCHES,
                hashMapOf()
            )

            showAlertDialogForUserConfirmation()
            return
        }

        if (binding.questionMatchViewPager.currentItem != 0) {
            binding.questionMatchViewPager.setCurrentItem(0, false)
        } else if (!viewModel.isAnySolutionWatched && shouldShowOcrEditScreen && ocrEditBackpressDialogShown.not()) {
            val helpType =
                if (viewModel.imageOcrText.isNotNullAndNotEmpty()) getString(R.string.edit) else null
            startActivityForResult(
                OcrEditActivity.getStartIntent(
                    context = this,
                    ocrText = viewModel.imageOcrText,
                    uiConfig = mOcrEditDialogUiConfig.copy(
                        helpText = getEditOcrDialogHelpText(helpType)
                    ),
                    source = Constants.BACKPRESS,
                    questionId = viewModel.parentQuestionId
                ), Constants.REQUEST_CODE_OCR_EDIT
            )
            ocrEditBackpressDialogShown = true
        } else {
            if (shouldShowBackPressDialog()) {
                return
            }

            viewModel.sendMatchPageExitEvent("back_press")
            defaultPrefs().edit()
                .putInt(Constants.USER_BACK_TO_CAMERA_PAGE, 1).apply()
            super.onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.questionImageFrame -> {
                startActivity(
                    FullImageViewActivity.getStartIntent(
                        this, askedQuestionImageUrl
                            ?: askedQuestionImageUri.orEmpty()
                    )
                )
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                sendEvent(EventConstants.EVENT_NAME_ASK_IMAGE_VIEW_FULL)
                viewModel.sendEvent(
                    EventConstants.EVENT_NAME_ASK_QUESTION_ZOOM_OUT,
                    hashMapOf()
                )
            }

            binding.askQuestion -> {
                CameraActivity.getStartIntent(this, TAG).also {
                    it.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(it)
                }
                viewModel.sendMatchPageExitEvent("camera")
            }

            binding.ivBack -> onBackPressed()

            binding.tvBottomTitle -> {
                val bottomTextData = viewModel.bottomTextData ?: return
                val deeplink = bottomTextData[selectedTab]?.deeplink
                viewModel.sendEvent(
                    event = EventConstants.STICKY_LINK_CLICK,
                    param = hashMapOf<String, Any>().apply {
                        put(key = Constants.SOURCE, value = "mp_" + selectedTab + "_tab")
                    }
                )
                deeplinkAction.performAction(
                    this,
                    deeplink
                )
            }
        }
    }

    private fun openPopupFeedbackDialog() {
        val matchQuestionPopupDialogFragment =
            MatchQuestionPopupDialogFragment.newInstance(viewModel.parentQuestionId)
        matchQuestionPopupDialogFragment.show(
            supportFragmentManager,
            MatchQuestionPopupDialogFragment.TAG
        )
        viewModel.doShowFeedbackPopDialog = false
    }

    private fun setUpAutoPlayToggle() {

        binding.switchAutoPlay.setOnCheckedChangeListener { _, isChecked ->
            defaultPrefs().edit {
                putBoolean(AUTOPLAY_STATE, isChecked)
            }

            setAutoPlaySwitchTrackColor(isChecked)
            viewModel.autoPlayState.postValue(isChecked && audioTooltipPlaying.not())

            viewModel.sendEvent(
                EventConstants.MATCH_PAGE_AUTO_PLAY_TOGGLE,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.AUTO_PLAY_TOGGLE_ENABLE, isChecked)
                },
                true
            )
        }
    }

    private fun setAutoPlaySwitchTrackColor(isChecked: Boolean) {
        if (isChecked) {
            binding.switchAutoPlay.trackDrawable.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.switch_track_enabled
                ), PorterDuff.Mode.SRC_IN
            )
        } else {
            binding.switchAutoPlay.trackDrawable.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.grey
                ), PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun sendEventRemoteConfigVariants() {
        viewModel.sendEvent(
            EventConstants.EVENT_RETRY_ASK_FLOW_COUNT,
            hashMapOf<String, Any>().apply {
                put(
                    EventConstants.EVENT_VARIANT_VALUE,
                    CoreRemoteConfigUtils.getAskFlowRetryCount()
                )
            },
            true
        )

        viewModel.sendEvent(
            EventConstants.EVENT_ASK_FLOW_TIME_OUT,
            hashMapOf<String, Any>().apply {
                put(EventConstants.EVENT_VARIANT_VALUE, CoreRemoteConfigUtils.getAskFlowTimeOut())
            },
            true
        )
    }

    private fun showFilterBottomSheet() {
        val dialog = AdvanceSearchBottomSheetFragment.newInstance()
        dialog.show(supportFragmentManager, AdvanceSearchBottomSheetFragment.TAG)
        onShow(null)
    }

    private fun setProgressBitmap(askQuestionBitmap: Bitmap) {
        binding.questionProgress.setImageBitmap(askQuestionBitmap)
    }

    private fun init() {
        binding.bottomNavigationView.menu.setGroupCheckable(0, false, true)
        loadLoaderAnimation(1)
        Log.d(EventConstants.EVENT_NAME_REACHED_MATCH_PAGE)
        viewModel.source = source
        setUpUiForAskedQuestionData()
        setLibraryBottomNavigationTabText()
        viewModel.getMatchQuestionBannerData()
        getMatchResults()
        setListeners()

        val countToSendEvent: Int =
            Utils.getCountToSend(
                RemoteConfigUtils.getEventInfo(),
                EventConstants.EVENT_NAME_MATCH_PAGE_VIEW
            )
        repeat((0 until countToSendEvent).count()) {
            sendEvent(EventConstants.EVENT_NAME_MATCH_PAGE_VIEW)
        }

        viewModel.sendEvent(
            EventConstants.EVENT_NAME_REACHED_MATCH_PAGE,
            hashMapOf(),
            true
        )
    }

    private fun setLibraryBottomNavigationTabText() {
        lifecycleScope.launchWhenStarted {
            val bottomNavIconsData = dataStore.bottomNavigationIconsData.firstOrNull()
            if (bottomNavIconsData.isNullOrEmpty()) {
                val bottomLibraryText = Utils.getLibraryBottomText(this@MatchQuestionActivity)
                if (!bottomLibraryText.isNullOrBlank()) {
                    val menu = binding.bottomNavigationView.menu
                    menu.findItem(R.id.libraryFragment).title = bottomLibraryText
                }
            }
        }
    }

    private fun setUpUiForAskedQuestionData() {
        if (questionText.isBlank()) {
            binding.questionImageFrame.show()
            binding.questionTextView.hide()
        } else {
            binding.questionTextView.text = questionText
            binding.questionImageFrame.hide()
            binding.questionTextView.show()
        }
    }

    private fun convertImageToString() {
        if (askedQuestionImageUri.isNullOrEmpty()) {
            ToastUtils.makeText(this, R.string.msg_invalid_question, Toast.LENGTH_SHORT).show()
            finish()
        } else {
            viewModel.getImageAsByteArray(askedQuestionImageUri!!)
        }

    }

    private fun getMatchResults() {

        viewModel.setMatchesFromInApp(isMatchFromInApp)
        if (ocrNotificationId != null && ocrNotificationId != -1L) {
            viewModel.deleteOcrFromDb(ocrNotificationId!!)
        }

        when {
            // These conditions are written in priority order.
            // 1. Notification
            // 2. Text search (TYD flow and Asked History for text search)
            // 3. Asked history for image search
            // 4. image picked after cropping

            // when user clicks on any notification, which is created after storing match page results
            // in local db of the question, for which user exits match page before loading solution.
            questionIdFromNotification.isNotBlank() -> {
                viewModel.setMatchesFromNotification()
                viewModel.getMatchResultsFromDb(questionIdFromNotification)
            }
            // for TYD flow (text search) and asked text question history (AskedQuestionHistory)
            questionText.isNotBlank() -> {
                viewModel.getTextQuestionMatchResults(questionText = questionText)
            }
            // for asked image question history (AskedQuestionHistory)
            askedQuestionImageUrl.isNotNullAndNotEmpty() &&
                    uploadedImageQuestionId != null &&
                    uploadedImageQuestionId != 0L -> {
                showQuestionImageProgress(askedQuestionImageUrl!!)
                getResultsForImageQuestionAskedHistory(
                    askedQuestionImageUrl = askedQuestionImageUrl!!,
                    uploadedImageQuestionId = uploadedImageQuestionId!!
                )
            }
            // Image comes from cropper screen
            else -> {
                convertImageToString()
            }
        }
    }

    private fun showQuestionImageProgress(imageUrl: String) {
        binding.questionProgress.show()
        binding.questionImage.show()
        binding.questionProgress.findViewById<ImageView>(R.id.askQuestionImage)?.loadImage(imageUrl)
    }

    private fun toGreyScaleAndGetMatches(bitmap: Bitmap) {
        BitmapUtils.greyScaleBitmap(bitmap) { greyedBitmap ->
            greyedBitmap?.let {
                getMatchResults(it)
            }
        }
    }

    private fun getMatchResults(bitmap: Bitmap) {
        BitmapUtils.scaleDownBitmap(
            bitmap,
            SCALE_DOWN_IMAGE_QUALITY,
            SCALE_DOWN_IMAGE_AREA
        ) { imageData ->
            imageData?.first?.let {
                viewModel.scaleDownImageData = imageData
                // Extract OCR from image uri
                viewModel.runTextRecognition()
                viewModel.getSignedUrl(imageFileName)
            }
        }
    }

    private fun getResultsForImageQuestionAskedHistory(
        askedQuestionImageUrl: String,
        uploadedImageQuestionId: Long
    ) {
        viewModel.makeParallelRequestsToGetImageResultsAndOtherWidgets(
            uploadedImageName = getCroppedImageName(croppedImageUrl = askedQuestionImageUrl)
                ?: imageFileName,
            uploadedImageQuestionId = uploadedImageQuestionId.toString(),
            croppedImageUrl = askedQuestionImageUrl,
            retryCounter = 1
        )
    }

    private fun getCroppedImageName(croppedImageUrl: String?): String? {
        return croppedImageUrl?.let {
            val uploadStrIndex = it.indexOf("uploads_")
            if (uploadStrIndex != -1) {
                it.substring(uploadStrIndex)
            } else {
                val lastIndex = it.lastIndexOf("/")
                if (lastIndex != -1) {
                    "${it.substring(lastIndex + 1)}.png"
                } else null
            }
        }
    }

    private fun setListeners() {

        binding.questionImageFrame.setOnClickListener(this)
        binding.ivBack.setOnClickListener(this)
        binding.tvBottomTitle.setOnClickListener(this)
        binding.askQuestion.setOnClickListener(this)

        lifecycleScope.launchWhenStarted {
            val bottomNavIconsDataJson = dataStore.bottomNavigationIconsData.firstOrNull() ?: ""
            val isBackendIconsAvailableForBottomNav =
                Utils.isBottomNavigationIconsApiDataAvailable(bottomNavIconsDataJson)

            val bottomNavigationTabsData =
                Gson().fromJson(bottomNavIconsDataJson, BottomNavigationTabsData::class.java)

            binding.bottomNavigationView.setOnItemSelectedListener {

                when (it.itemId) {
                    R.id.homeFragment -> {
                        if (!shouldShowBackPressDialog()) {
                            viewModel.sendMatchPageExitEvent("home")
                            if (isBackendIconsAvailableForBottomNav) {
                                deeplinkAction.performAction(
                                    this@MatchQuestionActivity,
                                    bottomNavigationTabsData.tab1?.deeplink
                                )

                                Utils.publishBottomNavTabClickEvent(
                                    analyticsPublisher,
                                    bottomNavigationTabsData.tab1?.name.orEmpty(),
                                    "1",
                                )

                            } else {
                                goToMainActivity()
                            }
                        }
                        return@setOnItemSelectedListener true
                    }

                    R.id.libraryFragment -> {
                        if (!shouldShowBackPressDialog()) {
                            viewModel.sendMatchPageExitEvent("library")
                            if (isBackendIconsAvailableForBottomNav) {
                                deeplinkAction.performAction(
                                    this@MatchQuestionActivity,
                                    bottomNavigationTabsData.tab2?.deeplink
                                )
                                Utils.publishBottomNavTabClickEvent(
                                    analyticsPublisher,
                                    bottomNavigationTabsData.tab2?.name.orEmpty(),
                                    "2",
                                )
                            } else {
                                goToLibraryScreen()
                            }
                        }
                        return@setOnItemSelectedListener true
                    }

                    R.id.forumFragment -> {
                        if (!shouldShowBackPressDialog()) {
                            viewModel.sendMatchPageExitEvent("friends")
                            if (isBackendIconsAvailableForBottomNav) {
                                deeplinkAction.performAction(
                                    this@MatchQuestionActivity,
                                    bottomNavigationTabsData.tab3?.deeplink
                                )
                                Utils.publishBottomNavTabClickEvent(
                                    analyticsPublisher,
                                    bottomNavigationTabsData.tab3?.name.orEmpty(),
                                    "3",
                                )
                            } else {
                                goToForumScreen()
                            }

                        }
                        return@setOnItemSelectedListener true
                    }

                    R.id.userProfileFragment -> {
                        if (!shouldShowBackPressDialog()) {
                            viewModel.sendMatchPageExitEvent("profile")
                            if (isBackendIconsAvailableForBottomNav) {
                                deeplinkAction.performAction(
                                    this@MatchQuestionActivity,
                                    bottomNavigationTabsData.tab4?.deeplink
                                )
                                Utils.publishBottomNavTabClickEvent(
                                    analyticsPublisher,
                                    bottomNavigationTabsData.tab4?.name.orEmpty(),
                                    "4",
                                )

                            } else {
                                goToProfileScreen()
                            }

                        }
                        return@setOnItemSelectedListener true
                    }

                    else -> {
                        if (!shouldShowBackPressDialog()) {
                            goToMainActivity()
                        }
                        return@setOnItemSelectedListener true
                    }
                }
            }
        }


        binding.buttonEditOcr.setOnClickListener {
            startActivityForResult(
                OcrEditActivity.getStartIntent(
                    context = this,
                    ocrText = viewModel.imageOcrText,
                    uiConfig = OcrEditActivity.UiConfig(
                        showSuggestions = false,
                        showHelp = false
                    ),
                    source = Constants.EDIT_BUTTON,
                    questionId = viewModel.parentQuestionId
                ), Constants.REQUEST_CODE_OCR_EDIT
            )
            viewModel.sendEvent(
                EventConstants.OCR_EDIT_BUTTON_CLICKED
            )
        }
    }

    private fun goToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
    }

    private fun goToLibraryScreen() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            action = Constants.OPEN_LIBRARY_FROM_BOTTOM
        })
    }

    private fun goToForumScreen() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            action = Constants.OPEN_FORUM_FROM_BOTTOM
        })
    }

    private fun goToProfileScreen() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            action = Constants.OPEN_PROFILE_FROM_BOTTOM
        })
    }

    private fun setupViewPager(tabTitleList: List<String>, fragmentList: List<Fragment>) {
        binding.questionMatchViewPager.apply {
            // called this extension function to fix scroll issue in MatchQuestionWebViewFragment
            reduceDragSensitivity()
            adapter = MatchQuestionAdapter(this@MatchQuestionActivity, fragmentList)
            TabLayoutMediator(
                binding.questionMatchTab,
                this
            ) { tab, position ->
                tab.text = tabTitleList[position]
            }.attach()
        }

        binding.questionMatchTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            var unselected: Int = -1

            override fun onTabReselected(p0: TabLayout.Tab?) {}

            override fun onTabUnselected(p0: TabLayout.Tab?) {
                viewModel.addScreenTime(p0?.text.toString(), screenTime)
                // Reset screen time
                screenTime = null

                unselected = p0?.position ?: -1
                if (p0?.position == 0) {
                    binding.matchFilterFacetListView.hide()
                }
            }

            override fun onTabSelected(p0: TabLayout.Tab?) {
                selectedTab = p0?.text?.toString()
                screenTime = System.currentTimeMillis()
                if (tabDefaultSelectedOnce) {
                    viewModel.sendTabClickEvent(source = p0?.text?.toString().orEmpty())
                } else {
                    tabDefaultSelectedOnce = true
                }
                if (p0?.position == 0) {
                    if (unselected !in -1..0 && canShowAdvancedSearch) {
                        binding.matchFilterFacetListView.show()
                    }
                } else {
                    binding.appBarLayout.setExpanded(true)
                }

                // set bottom text to navigate to some other screen
                val bottomTextData = viewModel.bottomTextData ?: hashMapOf()
                val bottomTextTitle = bottomTextData[selectedTab]?.title
                binding.tvBottomTitle.isVisible = bottomTextTitle.isNotNullAndNotEmpty()
                binding.tvBottomTitle.text = bottomTextTitle
            }
        })
    }

    /**
     * change visibility of bottom layout
     * @param isVisible - true/false
     */
    private fun setVisibilityOfBottomLayer(isVisible: Boolean) {
        binding.askQuestion.setVisibleState(isVisible)
        val hideBottomNavigationForD0User = viewModel.d0UserData?.hideBottomNav ?: false
        if (hideBottomNavigationForD0User) {
            binding.bottomNavigationView.setVisibleState(false)
        } else {
            binding.bottomNavigationView.setVisibleState(isVisible)
        }
    }

    private fun onImageSuccess(quesBitmap: Bitmap) {
        if ((quesBitmap.width / quesBitmap.height) <= 0.5) {
            showImageSelectDialog(quesBitmap)
        } else {
            binding.questionImage.setImageBitmap(quesBitmap)
            setProgressBitmap(quesBitmap)
            toGreyScaleAndGetMatches(quesBitmap)
        }
    }

    private fun fileNotFoundException() {
        toast(getString(R.string.string_fileNotPresent))
    }

    private fun showImageSelectDialog(questionBitmap: Bitmap) {
        viewModel.sendEvent(
            EventConstants.CAMERA_CHOOSE_IMAGE_ORIENTATION_DIALOG,
            hashMapOf(),
            true
        )
        supportFragmentManager.beginTransaction()
            .add(SelectImageDialog.newInstance(questionBitmap), SelectImageDialog.TAG)
            .commitAllowingStateLoss()
    }

    private fun onSuccess(matchQuestion: MatchQuestion) {

        matchQuestion.questionImage?.let {
            binding.questionImage.loadImage(it, null, null)
            questionImageUrl = matchQuestion.questionImage
        }

        binding.questionImage.visible()
        binding.zoomImageButton.visible()
        binding.questionMatchViewPager.visible()
        binding.questionMatchTab.visible()

        // Clear loader animation
        binding.loaderAnimation.pauseAnimation()
        binding.loaderAnimation.clearAnimation()

        isApiRequestInProgress = false
        // Dismiss alert dialog if user has pressed back while api was in progress.
        alertDialog?.dismiss()

        updateUiForEditOcr(
            isImageBlur = matchQuestion.isImageBlur,
            isBlur = matchQuestion.isBlur,
            isImageHandwritten = matchQuestion.isImageHandwritten,
            ocrText = matchQuestion.ocrText
        )

        updateSearchResult(matchQuestion)

        if (viewModel.backPressExperimentVariant == MatchQuestionViewModel.BackPressPopup.BOOK_FEEDBACK.variant) {
            makeFetchFeedbackPopupDataFetchCall()
        }
    }

    private fun setProgressVisibilityStatus(visibility: Boolean) {
        binding.loaderAnimation.setVisibleState(visibility)
        binding.loaderAnimationTextSwitcher.setVisibleState(visibility)
        binding.questionProgress.setVisibleState(visibility)
        binding.bannerLayout.setVisibleState(visibility)
        binding.askQuestion.setVisibleState(visibility)
        binding.bottomNavigationView.setVisibleState(visibility)
    }

    private fun updateUiForEditOcr(
        isImageBlur: Boolean,
        isBlur: Boolean?,
        isImageHandwritten: Boolean,
        ocrText: String
    ) {
        shouldShowOcrEditScreen = isImageBlur || isImageHandwritten

        when (mImageOcrFeedback) {
            null -> {
                mImageOcrFeedback = when {
                    isImageBlur -> Constants.BLUR
                    isImageHandwritten -> Constants.HANDWRITTEN
                    else -> Constants.BLUR
                }
            }
        }

        // Handle OCR edit UI changes
        binding.tvOcr.text = ocrText
        (isImageBlur || isImageHandwritten).run {
            binding.ocrEditContainer.isVisible =
                (binding.ocrEditContainer.isVisible || (this)).also {
                    if (it) {
                        viewModel.sendEvent(
                            EventConstants.OCR_EDIT_BUTTON_VISIBLE
                        )
                    }
                }
        }
        if (binding.ocrEditContainer.isVisible
            && isBlur != true
            && !defaultPrefs().getBoolean(Constants.OCR_EDIT_COACHMARK_SHOWN, false)
        ) {
            binding.tvEditOcrCoachmark.apply {
                val feedbackText = getString(
                    when (mImageOcrFeedback) {
                        Constants.BLUR -> R.string.blur
                        Constants.HANDWRITTEN -> R.string.handwritten
                        else -> R.string.blur
                    }
                )
                text = getString(R.string.ocr_edit_coachmark, feedbackText)
                background = MaterialShapeDrawable(
                    ShapeAppearanceModel()
                        .toBuilder()
                        .setAllCornerSizes(8f.dpToPx())
                        .setBottomEdge(TriangleBiasEdgeTreatment(6f.dpToPx(), false, 0.85f))
                        .build()
                )
                show()
            }
            defaultPrefs().edit {
                putBoolean(Constants.OCR_EDIT_COACHMARK_SHOWN, true)
            }
        }
    }

    private fun makeFetchFeedbackPopupDataFetchCall() {
        if (viewModel.isPopupFeedbackDialogAlreadyShown) {
            return
        }
        viewModel.getPopupOptionsData(
            MatchQuestionPopupDialogFragment.PAGE_SRP,
            MatchQuestionPopupDialogFragment.NO_VIDEO_WATCHED
        )
    }

    private fun onMatchQuestionBannerData(matchQuestionBanner: MatchQuestionBanner) {

        if (viewModel.isMatchResponseFetched.not()) binding.bannerLayout.show() else binding.bannerLayout.hide()

        if (matchQuestionBanner.dnCash != null && matchQuestionBanner.dnCash > 0) {
            binding.bannerText.layoutParams = ConstraintLayout.LayoutParams(
                (getScreenWidth() * 0.6).toInt(),
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            binding.dnCashLayout.show()
            binding.dnCash.text = String.format(
                resources.getString(R.string.activity_match_page_dn_cash),
                matchQuestionBanner.dnCash
            )
        } else {
            binding.dnCashLayout.hide()
            binding.bannerText.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
        }
        binding.bannerText.show()
        binding.bannerText.text = matchQuestionBanner.content
    }

    private fun updateSearchResult(matchQuestion: MatchQuestion) {

        // stop audio player if it is playing
        binding.audioTooltipView.apply {
            stopMedia()
            gone()
        }

        // show pop ups in each case of handwritten, blur or empty list, screen should not be blank
        // Only exception is when user get empty list after selecting advanced search filter
        when {
            matchQuestion.matchedCount > 0 ||
                    viewModel.isAdvancedSearchEnabled.value == true -> setUpMatchPage(
                matchQuestion
            )

            matchQuestion.isBlur == true && viewModel.isAdvancedSearchEnabled.value == false -> showDialogForBlurImage(
                matchQuestion
            )

            else -> showDialogForHandwritten()
        }
    }

    private fun setUpMatchPage(matchQuestion: MatchQuestion) {

        setUpToolTipAudioForMatch()

        viewModel.isMatchResponseFetched = true
        autoPlay = matchQuestion.autoPlay

        setupViewPager(
            matchQuestion.ocrText,
            matchQuestion.questionId,
            autoPlay,
            matchQuestion.autoPlayDuration,
            matchQuestion.autoPlayInitiation,
            matchQuestion.tabUrls ?: hashMapOf()
        )
    }

    private fun setUpToolTipAudioForMatch() {
        if (AudioToolTipUtils.checkForCoreLoopSessionCount(Constants.SCREEN_MATCH_PAGE)) {
            val list =
                AudioToolTipUtils.isAudioToolTipAvailable(Constants.SCREEN_MATCH_PAGE).orEmpty()
            if (list.isNotEmpty()) {
                val audioTooltipViewData =
                    AudioToolTipUtils.buildAudioTooltipViewData(list, Constants.SCREEN_MATCH_PAGE)
                setUpMediaPlayer(audioTooltipViewData)
            } else {
                binding.audioTooltipView.gone()
            }
        } else {
            binding.audioTooltipView.gone()
        }
    }

    private fun setUpMediaPlayer(data: AudioTooltipViewData) {
        binding.audioTooltipView.apply {
            visible()
            setMediaPlayerStateListener(this@MatchQuestionActivity)
            setData(data)
            registerLifecycle(lifecycle)
        }
    }

    private fun showDialogForBlurImage(matchQuestion: MatchQuestion) {
        viewModel.isMatchResponseFetched = false
        val dialog = BlurQuestionImageDialogFragment.newInstance(
            matchQuestion.questionImage,
            matchQuestion.questionId
        )
        dialog.show(supportFragmentManager, BLUR_QUESTION_IMAGE_ERROR_DIALOG)
    }

    private fun showDialogForHandwritten() {
        viewModel.isMatchResponseFetched = false
        val dialog = HandWrittenQuestionDialogFragment.newInstance()
        dialog.show(supportFragmentManager, NO_MATCH_FOUND_ERROR_DIALOG)
        sendEvent(EventConstants.EVENT_NAME_NO_MATCH_FOUND)
        viewModel.sendEvent(
            EventConstants.EVENT_NAME_NO_MATCH_FOUND,
            hashMapOf()
        )
    }

    private fun setupViewPager(
        ocrText: String,
        parentQuestionId: String,
        autoPlay: Boolean?,
        autoPlayDuration: Long?,
        autoPlayInitiation: Long?,
        tabUrls: HashMap<String, String>
    ) {

        val isAutoPlayToggleEnabled = defaultPrefs().getBoolean(AUTOPLAY_STATE, true)
        val liveTabText = viewModel.liveTabData?.tabText.orEmpty()

        viewModel.sendEvent(
            EventConstants.AUTOPLAY_TOGGLE_STATE, hashMapOf(
                Constants.ASKED_QUESTION_ID to parentQuestionId,
                Constants.CURRENT_STATE to isAutoPlayToggleEnabled
            )
        )

        binding.switchAutoPlay.setVisibleState(autoPlay == true)
        binding.switchAutoPlay.isChecked = isAutoPlayToggleEnabled

        setAutoPlaySwitchTrackColor(binding.switchAutoPlay.isChecked)

        val titleList = mutableListOf(
            Constants.DOUBTNUT,
            Constants.GOOGLE,
            Constants.YOUTUBE,
            liveTabText
        )

        val matchQuestionFragment = MatchQuestionFragment.newInstance(
            askedQuestionId = parentQuestionId,
            ocrText = ocrText,
            autoPlay = autoPlay ?: false,
            autoPlayDuration = autoPlayDuration,
            autoPlayInitiation = autoPlayInitiation
        )
        matchQuestionFragment.apply {
            setP2pConnectListener(object : P2pConnectListener {
                override fun showP2pHostScreenToConnect() {
                    showP2pHostAnimationFragment()
                }
            })

            setBookFeedbackListener(object : BookFeedbackListener {
                override fun openBookFeedbackDialog() {
                    showBookFeedBackDialog(MatchQuestionFragment.TAG)
                }
            })
        }

        val matchPageCarousalsFragment =
            MatchPageCarousalsFragment.newInstance(liveTabText)
        matchPageCarousalsFragment.setP2pConnectListener(object : P2pConnectListener {
            override fun showP2pHostScreenToConnect() {
                showP2pHostAnimationFragment()
            }
        })

        val googleTabUrl =
            tabUrls[Constants.GOOGLE.lowercase()] ?: (Constants.GOOGLE_URL + URLEncoder.encode(
                ocrText,
                Constants.SUPPORTED_CHARACTER_ENCODING
            ))

        val youtubeTabUrl =
            tabUrls[Constants.YOUTUBE.lowercase()] ?: (Constants.YOUTUBE_URL + URLEncoder.encode(
                ocrText,
                Constants.SUPPORTED_CHARACTER_ENCODING
            ))

        val cyMathTabUrl =
            tabUrls[Constants.CYMATH.lowercase()] ?: (Constants.CYMATH_URL + URLEncoder.encode(
                ocrText.replace("`", ""),
                "UTF-8"
            ))

        val fragmentList = mutableListOf(
            matchQuestionFragment,
            MatchQuestionWebViewFragment.newInstance(
                urlToLoad = googleTabUrl, pageName = Constants.GOOGLE, questionId = parentQuestionId
            ),
            MatchQuestionWebViewFragment.newInstance(
                urlToLoad = youtubeTabUrl,
                pageName = Constants.YOUTUBE,
                questionId = parentQuestionId
            ),
            matchPageCarousalsFragment
        )

        val userClass = userPreference.getUserClass()
        val classesToAvoid = listOf("6", "7", "8", "14")
        if (userClass !in classesToAvoid) {
            titleList.add(4, Constants.VIP)
            fragmentList.add(4, MatchPageCarousalsFragment.newInstance(Constants.VIP).also {
                it.setP2pConnectListener(
                    object : P2pConnectListener {
                        override fun showP2pHostScreenToConnect() {
                            showP2pHostAnimationFragment()
                        }
                    }
                )
            })
        }

        if (ocrText.contains("`")) {
            fragmentList.add(
                1,
                MatchQuestionWebViewFragment.newInstance(
                    urlToLoad = cyMathTabUrl,
                    pageName = Constants.CYMATH,
                    questionId = parentQuestionId
                )
            )
            titleList.add(1, Constants.CYMATH)
        }

        setupViewPager(titleList, fragmentList)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
        sendEvent(EventConstants.EVENT_NAME_ASK_QUESTION_API_FAILURE)
    }

    private fun onApiError(e: Throwable) {
        isApiError = true
        // Loading complete, set false
        isApiRequestInProgress = false
        apiErrorToast(e)

        sendEvent(EventConstants.EVENT_NAME_ASK_QUESTION_API_ERROR)
        viewModel.sendEvent(EventConstants.ASK_QUESTION_API_ERROR, hashMapOf())
    }

    private fun updateProgressBarState(state: Boolean) {
        if (state) {
            binding.matchFilterFacetListView.gone()
        }
        setProgressVisibilityStatus(state)
    }

    private fun updateBannerProgressBarState(state: Boolean) {
        setProgressVisibilityStatus(state)
    }

    private fun ioExceptionHandler() {
        isApiError = true
        isApiRequestInProgress = false

        if (NetworkUtils.isConnected(this)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }

        viewModel.sendEvent(EventConstants.ASK_QUESTION_API_ERROR, hashMapOf())
    }

    private fun showAlertDialogForUserConfirmation() {
        alertDialog = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton(getString(R.string.yes_i_will_wait)) { dialog, id ->
                    viewModel.setPreventMatchExit()
                    viewModel.sendEvent(
                        EventConstants.PREVENT_MATCH_PAGE_EXIT_YES,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                        }
                    )
                }
                setNegativeButton(getString(R.string.skip_results)) { dialog, id ->
                    viewModel.sendEvent(
                        EventConstants.PREVENT_MATCH_PAGE_EXIT_NO,
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                        }
                    )

                    viewModel.sendMatchPageExitEvent("exit_pop_up")
                    CameraActivity.getStartIntent(this@MatchQuestionActivity, TAG).also { intent ->
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(intent)
                    }

                    // Store match page result in DB and show notification
                    if (isApiRequestInProgress) {
                        isApiRequestInProgress = false
                        checkForMatchResultForNotification(imageFileName, askedQuestionImageUri)
                    }
                    defaultPrefs().edit()
                        .putInt(Constants.USER_BACK_TO_CAMERA_PAGE, 1).apply()
                    finish()
                }
            }.setTitle(getString(R.string.match_page_alert_dialog_title))
                .setMessage(R.string.match_page_alert_dialog_sub_title)

            builder.create()
        }

        viewModel.sendEvent(
            EventConstants.PREVENT_MATCH_PAGE_EXIT_VISIBLE,
            hashMapOf<String, Any>().apply {
                put(EventConstants.TIME_STAMP, System.currentTimeMillis())
            }
        )

        alertDialog?.setOnShowListener {
            alertDialog?.getButton(AlertDialog.BUTTON_NEGATIVE)
                ?.setTextColor(ContextCompat.getColor(this, R.color.black_50))
            onShow(it)
        }
        alertDialog?.setOnDismissListener(this)

        alertDialog?.show()
    }

    private fun shouldShowBackPressDialog(): Boolean {
        if (isBackPressDialogShown || viewModel.isAnySolutionWatched) return false
        val shouldShowBackPressDialog =
            when (viewModel.d0UserData?.backPressDialogVariant
                ?: viewModel.backPressExperimentVariant) {
                MatchQuestionViewModel.BackPressPopup.FIRST_MATCH_WITH_P2P.variant -> {
                    showMatchQuestionP2pDialog()
                }
                MatchQuestionViewModel.BackPressPopup.USER_FEEDBACK.variant -> {
                    launchFeedbackPopupDialogIfFeedbackOptionsReceived()
                }
                MatchQuestionViewModel.BackPressPopup.BOOK_FEEDBACK.variant -> {
                    showBookFeedBackDialog(
                        MATCH_PAGE_SOURCE_BACK_PRESS
                    )
                }
                else -> false
            }
        isBackPressDialogShown = true
        return shouldShowBackPressDialog
    }

    /** if options size is greater than one then only we need to show
     *  popup-feedback page
     */
    private fun launchFeedbackPopupDialogIfFeedbackOptionsReceived(): Boolean {
        if (viewModel.doShowFeedbackPopDialog && !viewModel.isPopupFeedbackDialogAlreadyShown) {
            openPopupFeedbackDialog()
            return true
        }
        return false
    }

    private fun showBookFeedBackDialog(source: String): Boolean {
        if (viewModel.feedbackSubmitted) return false
        val dialog = MatchQuestionBookFeedbackDialogFragment.newInstance(source = source)
        supportFragmentManager.beginTransaction()
            .add(dialog, MatchQuestionBookFeedbackDialogFragment.TAG).commit()
        onShow(null)
        return true
    }

    private fun showMatchQuestionP2pDialog(): Boolean {
        val backPressDialogFragment = MatchQuestionP2pDialogFragment.newInstance()
        backPressDialogFragment.setUpP2pListener(object :
            MatchQuestionP2pDialogFragment.P2pConnectListener {
            override fun openP2pHostAnimationFragment() {
                lifecycleScope.launchWhenResumed {
                    backPressDialogFragment.dismiss()
                    val deeplink = viewModel.d0UserData?.backPressDialogCtaDeeplink
                    if (deeplink != null) {
                        deeplinkAction.performAction(this@MatchQuestionActivity, deeplink)
                    } else {
                        showP2pHostAnimationFragment()
                    }
                }
            }
        })
        supportFragmentManager.beginTransaction()
            .add(backPressDialogFragment, MatchQuestionP2pDialogFragment.TAG).commit()
        return true
    }

    private fun showP2pHostAnimationFragment() {
        viewModel.setDataToRequestP2p(
            questionImageUrl = questionImageUrl,
            questionText = questionText,
            questionId = viewModel.parentQuestionId
        )
        val doubtP2pHostAnimationFragment = P2pHostIntroductionFragment.newInstance()
        doubtP2pHostAnimationFragment.show(
            supportFragmentManager,
            P2pHostIntroductionFragment.TAG
        )
    }

    private fun sendEvent(eventName: String) {
        this@MatchQuestionActivity.apply {
            (this@MatchQuestionActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@MatchQuestionActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_MATCH_ACTIVITY)
                .track()
        }
    }

    private fun checkForMatchResultForNotification(
        imageFileName: String,
        askedQuestionImageUri: String?
    ) {

        if (askedQuestionImageUri == null) return

        val constraint = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val blurRequest = OneTimeWorkRequestBuilder<MatchesByFileNameWorker>()
            .setConstraints(constraint)
            .setInitialDelay(5, TimeUnit.SECONDS)
            .setInputData(createInputDataForUri(imageFileName, askedQuestionImageUri))
            .build()
        WorkManager.getInstance(this).enqueue(blurRequest)
    }

    /**
     * Creates the input data bundle which includes the Image file name to operate on
     * @return Data which contains the Image File Name as a String
     */
    private fun createInputDataForUri(imageFileName: String, askedQuestionImageUri: String): Data {
        val builder = Data.Builder()
        builder.putString(IMAGE_FILE_NAME, imageFileName)
        builder.putString(INTENT_EXTRA_ASKED_QUE_URI, askedQuestionImageUri)
        return builder.build()
    }

    private fun loadLoaderAnimation(loaderNumber: Int) {
        lifecycleScope.launchWhenResumed {
            if (binding.loaderAnimation.isAnimating) {
                binding.loaderAnimation.pauseAnimation()
                binding.loaderAnimation.clearAnimation()
            }
            when (loaderNumber) {
                1 -> {
                    lottieAnimDataStore.matchPageLoader1DataAnimationUrl.firstOrNull()
                        ?.let { loader1AnimationUrl ->
                            binding.loaderAnimation.apply {
                                applyAnimationFromUrl(
                                    loader1AnimationUrl,
                                    repeatCount = 0,
                                    onAnimationEnd = {
                                        loadLoaderAnimation(2)
                                    })
                            }
                        }
                }
                2 -> {
                    lottieAnimDataStore.matchPageLoader2DataAnimationUrl.firstOrNull()
                        ?.let { loader2AnimationUrl ->
                            binding.loaderAnimation.apply {
                                applyAnimationFromUrl(loader2AnimationUrl)
                            }
                        }
                }
            }

            setUpLoaderTextAnimation(loaderNumber)
        }
    }

    private fun setUpLoaderTextAnimation(loaderNumber: Int) {
        var textMap = mutableMapOf<Int, CharSequence>()
        var animationDuration = 0

        when (loaderNumber) {
            1 -> {
                textMap = getLoaderAnimationTextMap(loaderNumber)
                animationDuration = 10016 // 10 s 16 ms
            }
            2 -> {
                textMap = getLoaderAnimationTextMap(loaderNumber)
                animationDuration = 0
            }
        }
        binding.loaderAnimationTextSwitcher.setInAnimation(this, R.anim.slide_in_from_bottom)
        binding.loaderAnimationTextSwitcher.setOutAnimation(this, R.anim.slide_out_to_top)

        ValueAnimator.ofInt(0, animationDuration).apply {
            duration = animationDuration.toLong()
            interpolator = LinearInterpolator()

            addUpdateListener {
                if ((it.animatedValue as Int) / 100 in textMap) {
                    binding.loaderAnimationTextSwitcher.setText(textMap.remove(it.animatedValue as Int / 100))
                }
            }
            start()
        }
    }

    private fun getLoaderAnimationTextMap(loaderNumber: Int): MutableMap<Int, CharSequence> =
        when (loaderNumber) {
            1 -> {
                mutableMapOf(
                    0 to getString(R.string.searching_among_10_lakh_solution_videos),
                    21 to getString(R.string.found_most_relevant_videos),
                    64 to getString(R.string.arranging_it_in_order_of_relevance)
                )
            }
            2 -> {
                mutableMapOf(0 to getString(R.string.loading_solutions))
            }
            else -> mutableMapOf()
        }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            RESULT_OK -> {
                handleRequestCodes(requestCode, data)
            }
            RESULT_CANCELED -> {
                finish()
            }
        }
    }

    private fun handleRequestCodes(requestCode: Int, data: Intent?) {
        when (requestCode) {

            Constants.REQUEST_CODE_ASK_IMAGE_SEARCH -> {
                val signedUrlEntity = viewModel.signedUrlEntity ?: return
                viewModel.makeParallelRequestsToGetImageResultsAndOtherWidgets(
                    uploadedImageName = signedUrlEntity.fileName,
                    uploadedImageQuestionId = signedUrlEntity.questionId
                )
            }

            Constants.REQUEST_CODE_UPLOAD_ASK_IMAGE -> {
                viewModel.retryUploadImage()
            }

            Constants.REQUEST_CODE_ASK_TEXT_SEARCH -> viewModel.getTextQuestionMatchResults(
                questionText = questionText
            )

            Constants.REQUEST_CODE_GET_SIGNED_URL -> {
                viewModel.getSignedUrl(fileName = imageFileName)
            }

            Constants.REQUEST_CODE_OCR_EDIT -> {
                binding.questionMatchViewPager.gone()
                loadLoaderAnimation(1)
                researchWithEditedOcr(data)
            }

            Constants.REQUEST_CODE_FILTER_RESULT -> {
                viewModel.clearMatchFilter()
            }
        }
    }

    private fun openScreen(navigationModel: NavigationModel) {
        viewModel.isAnySolutionWatched = true
        val args: Bundle? = navigationModel.hashMap?.toBundle()
        val intent: Intent = when (navigationModel.screen) {
            VideoScreen -> {
                VideoPageActivity.startActivity(
                    context = this@MatchQuestionActivity,
                    questionId = args?.getString(Constants.QUESTION_ID).orDefaultValue(),
                    playlistId = args?.getString(Constants.PLAYLIST_ID),
                    mcId = "",
                    page = args?.getString(Constants.PAGE).orDefaultValue(),
                    mcClass = args?.getString(Constants.MC_CLASS),
                    isMicroConcept = args?.getBoolean("isMicroconcept"),
                    referredStudentId = "",
                    parentId = args?.getString(Constants.PARENT_ID) ?: "0",
                    fromNotificationVideo = false,
                    preLoadVideoData = args?.getParcelable(Constants.VIDEO_DATA),
                    ocr = args?.getString(Constants.OCR_TEXT),
                    source = args?.getString(Constants.PAGE).orDefaultValue(),
                    parentPage = args?.getString(Constants.PARENT_PAGE),
                    createNewInstance = true
                )
            }
            else -> {
                TextSolutionActivity.startActivity(
                    context = this@MatchQuestionActivity,
                    questionId = args?.getString(Constants.QUESTION_ID).orDefaultValue(),
                    playlistId = args?.getString(Constants.PLAYLIST_ID),
                    mcId = "",
                    page = args?.getString(Constants.PAGE).orDefaultValue(),
                    mcClass = args?.getString(Constants.MC_CLASS),
                    isMicroConcept = args?.getBoolean("isMicroconcept"),
                    referredStudentId = "",
                    parentId = args?.getString(Constants.PARENT_ID) ?: "0",
                    fromNotificationVideo = false,
                    resourceType = args?.getString(Constants.RESOURCE_TYPE),
                    resourceData = args?.getString(Constants.RESOURCE_DATA),
                    ocrText = args?.getString(Constants.OCR_TEXT)
                )
            }
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun researchWithEditedOcr(intentData: Intent?) {
        intentData?.let {
            viewModel.source = it.getStringExtra(OcrEditActivity.RESULT_SOURCE)
                ?: viewModel.source
            val questionText = it.getStringExtra(OcrEditActivity.RESULT_EDITED_OCR) ?: questionText
            binding.tvOcr.text = questionText
            viewModel.getTextQuestionMatchResults(
                questionText = questionText,
                uploadedImageQuestionId = viewModel.parentQuestionId,
                imageOcrFeedback = mImageOcrFeedback
            )
        }
    }

    private fun getOcrEditBackpressDialogUiConfig(): OcrEditActivity.UiConfig {
        return OcrEditActivity.UiConfig(
            showSuggestions = false,
            showHelp = true,
            helpText = getEditOcrDialogHelpText()
        )
    }

    private fun getEditOcrDialogHelpText(helpType: String? = null): CharSequence {
        val type = when {
            helpType != null -> helpType
            else -> getString(R.string.enter)
        }
        return getString(R.string.info_edit_ocr, mImageOcrFeedback, type)
    }

    override fun onPlayerStateChange(state: MediaPlayerState, data: Any?) {
        when (state) {
            MediaPlayerState.STARTED -> {
                audioTooltipPlaying = true
                viewModel.autoPlayState.postValue(false)
            }

            else -> {
                audioTooltipPlaying = false
                viewModel.autoPlayState.postValue(
                    autoPlay == true && defaultPrefs().getBoolean(
                        AUTOPLAY_STATE,
                        false
                    )
                )
            }
        }
    }
}
