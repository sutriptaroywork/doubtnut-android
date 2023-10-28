package com.doubtnutapp.camera.ui

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.ContentObserver
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Size
import android.util.TypedValue
import android.view.*
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.core.ImageCapture.OutputFileOptions
import androidx.camera.extensions.BokehImageCaptureExtender
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.graphics.toRectF
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.data.remote.PopupDetails
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.*
import com.doubtnut.core.utils.LottieAnimationViewUtils.applyAnimationFromAsset
import com.doubtnut.core.utils.NetworkUtils
import com.doubtnut.core.view.audiotooltipview.AudioTooltipViewData
import com.doubtnutapp.*
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.bottomnavigation.model.BottomNavigationItemData
import com.doubtnutapp.bottomnavigation.ui.adapter.BottomNavigationAdapter
import com.doubtnutapp.camera.viewmodel.CameraActivityViewModel
import com.doubtnutapp.common.FragmentWrapperActivity
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.models.feed.TopOptionWidgetItem
import com.doubtnutapp.databinding.ActivityCameraBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.camerascreen.entity.CameraSettingEntity
import com.doubtnutapp.domain.camerascreen.entity.PackageStatusEntity
import com.doubtnutapp.domain.camerascreen.entity.ShortsData
import com.doubtnutapp.domain.camerascreen.entity.SubjectEntity
import com.doubtnutapp.domain.payment.entities.PackageDesc
import com.doubtnutapp.doubtplan.DoubtPackageActivity
import com.doubtnutapp.doubtplan.DoubtPackageDialog
import com.doubtnutapp.feed.view.TopOptionsAdapter
import com.doubtnutapp.gallery.adapter.GalleryImagesAdapter
import com.doubtnutapp.gallery.adapter.GalleryPlaceholderImageAdapter
import com.doubtnutapp.gallery.model.GalleryImageViewItem
import com.doubtnutapp.gallery.ui.GalleryFragment
import com.doubtnutapp.home.HomeFeedFragmentV2
import com.doubtnutapp.librarylisting.ui.LibraryListingActivity
import com.doubtnutapp.login.ui.activity.FailedGuestLoginActivity
import com.doubtnutapp.matchquestion.ui.activity.MatchQuestionActivity
import com.doubtnutapp.matchquestion.ui.activity.TYDActivity
import com.doubtnutapp.newglobalsearch.ui.InAppSearchActivity
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.similarVideo.model.NcertViewItemEntity
import com.doubtnutapp.ui.FragmentHolderActivity
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.main.NcertViewItemCameraAdapter
import com.doubtnutapp.ui.main.demoanimation.DemoAnimationActivity
import com.doubtnutapp.ui.main.demoanimation.DemoAnimationFragmentV1
import com.doubtnutapp.ui.main.samplequestion.BackPressSampleQuestionFragment
import com.doubtnutapp.ui.main.samplequestion.BackPressSampleQuestionFragmentV2
import com.doubtnutapp.ui.main.samplequestion.SampleQuestionAdapter
import com.doubtnutapp.ui.main.samplequestion.SampleQuestionFragment
import com.doubtnutapp.ui.main.tooltip.ToolTipFragment
import com.doubtnutapp.ui.questionAskedHistory.QuestionAskedHistoryActivity
import com.doubtnutapp.ui.splash.RequiredPermissionDialog
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgets.OnSwipeTouchListener
import com.facebook.appevents.AppEventsConstants
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.theartofdev.edmodo.cropper.CropConstant
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import com.uxcam.UXCam
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class CameraActivity :
    BaseBindingActivity<CameraActivityViewModel, ActivityCameraBinding>(),
    OnItemClickListener,
    View.OnClickListener,
    ActionPerformer2 {

    companion object {
        const val TAG = "CameraActivity"

        const val INTENT_EXTRA_IS_USER_OPENED = "is_user_opened"

        const val SHOULD_SHOW_TOOL_TIP_FRAGMENT = "SHOULD_SHOW_TOOL_TIP_FRAGMENT"
        const val SHOULD_SHOW_SAMPLE_QUESTION_BOTTOM_LAYOUT =
            "SHOULD_SHOW_SAMPLE_QUESTION_BOTTOM_LAYOUT"
        const val TOOL_TIP_SHOW_COUNT = "TOOL_TIP_SHOW_COUNT"
        const val SAMPLE_QUESTION_SHOW_COUNT = "SAMPLE_QUESTION_SHOW_COUNT"
        const val SAMPLE_QUESTION_BOTTOM_LAYOUT_SHOW_COUNT =
            "SAMPLE_QUESTION_BOTTOM_LAYOUT_SHOW_COUNT"
        const val GALLERY_DEMO_QUESTION_SHOW_COUNT = "GALLERY_DEMO_QUESTION_SHOW_COUNT"
        const val MAX_TOOL_TIP_SHOW_COUNT = 5
        const val MAX_SAMPLE_QUESTION_BOTTOM_LAYOUT_SHOW_COUNT = 3
        const val MAX_GALLERY_DEMO_QUESTION_SHOW_COUNT = 3
        const val BACK_PRESS_SAMPLE_QUESTION_DIALOG_COUNT =
            "BACK_PRESS_SAMPLE_QUESTION_DIALOG_COUNT"
        const val MAX_BACK_PRESS_SAMPLE_QUESTION_DIALOG_COUNT = 3
        const val SHOULD_SHOW_BACK_PRESS_SAMPLE_QUESTION = "SHOULD_SHOW_BACK_PRESS_SAMPLE_QUESTION"
        const val IS_BACK_PRESS_SAMPLE_QUESTION_SHOWN = "IS_BACK_PRESS_SAMPLE_QUESTION_SHOWN"
        const val INVALID_IMAGE_DIALOG = "InvalidImageDialog"
        const val NO_CAMERA_FOUND = -1

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0

        private const val UXCAM_TAG = "camera"
        private const val UXCAM_EVENT_BACK_PRESS_BOTTOM_SHEET = "back_press_bottom_sheet"
        private const val UXCAM_EVENT_D0_5_QA = "d0_5_qa"
        private const val UXCAM_EVENT_AUDIO_TOOLTIP = "audio_tooltip"
        const val EVENT_TAG_QA_CAMERA_CLICK = "ReferQA_camera_banner_click"
        const val FINISH_CALLING_ACTIVITY = "finish_calling_activity"
        private const val REQUEST_SELECT_IMAGE_GALLERY = 777

        fun getStartIntent(
            context: Context,
            source: String,
            cropImageUrl: String? = null,
            isUserOpened: Boolean = true
        ) =
            Intent(context, CameraActivity::class.java).apply {
                putExtra(Constants.SOURCE, source)
                putExtra(Constants.CROP_IMAGE_URL, cropImageUrl)
                putExtra(INTENT_EXTRA_IS_USER_OPENED, isUserOpened)
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private lateinit var eventTracker: Tracker

    private val ncertAdapter: NcertViewItemCameraAdapter by lazy { NcertViewItemCameraAdapter(this) }

    private var doNotAskAgain: Boolean = false

    private var startCameraOnResume: Boolean = false

    private val sampleQuestiondisposable: CompositeDisposable = CompositeDisposable()

    private val shortsIntentDisposable: CompositeDisposable = CompositeDisposable()
    private var isShortsIntentOpened = false

    private val sampleQuestionAdapter: SampleQuestionAdapter by lazy { SampleQuestionAdapter(this) }

    var isAlertShowing = false

    private var shouldProceed: Boolean = false
    private var isForStatus: Boolean = false
    private var mImageSource: String = ""

    private var countDownTimer: CountDownTimer? = null

    private var shouldShowToolTip: Boolean = true

    private var shouldShowSampleQuestionBottomLayout: Boolean = false
    private var toolTipFragmentCount: Int = 0
    private var sampleQuestionShowCount: Int = 0
    private var sampleQuestionBottomLayoutShowCount: Int = 0
    private var tydSearchFlow =
        Constants.ONLY_TEXT_SEARCH // 1 - Voice search first 2 - Text search first 3 - Old flow
    private var mShowBottomNavigation: Boolean = true

    private var imageCapture: ImageCapture? = null
    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var isFromNewIntent = false

    private var referAndEarnDeeplink = ""
    private var titleReferAndEarn = ""

    private val flashImage = arrayOf(
        R.drawable.ic_flash_auto,
        R.drawable.ic_flash_off,
        R.drawable.ic_flash_on
    )
    private val flashOptions = arrayOf(
        ImageCapture.FLASH_MODE_AUTO,
        ImageCapture.FLASH_MODE_OFF,
        ImageCapture.FLASH_MODE_ON
    )
    private var mCurrentFlash: Int = 0

    private var isTakingPicture: Boolean = false

    private var orientationEventListener: CameraActivityOrientationEventListener? = null
    private var currentOrientation = OrientationEventListener.ORIENTATION_UNKNOWN

    private val galleryImagesAdapter: GalleryImagesAdapter by lazy {
        GalleryImagesAdapter(
            mActionPerformer = this
        )
    }

    private var noImagesInGallery: Boolean = false
    private var noImagesToastShown: Boolean = true

    private var mGalleryBottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var mIconsBottomSheetBehavior: BottomSheetBehavior<View>? = null

    private val mGalleryImagesContentObserver: ContentObserver by lazy { viewModel.getGalleryImagesContentObserver() }

    override fun provideViewBinding(): ActivityCameraBinding =
        ActivityCameraBinding.inflate(layoutInflater)

    private var day0UserTimer: CountDownTimer? = null

    override fun providePageName(): String = TAG

    override fun provideViewModel(): CameraActivityViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        init()

        // Start worker to check if any offline OCR exist, if yes show notification else cancel worker if it is running.
        viewModel.startWorkerToShowOcrNotification()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
    }

    private fun showSampleQuestionDialogOnBackPress(fragment: Fragment, tag: String) {

        supportFragmentManager.beginTransaction().add(fragment, tag).commit()

        setBackPressDialogCount()
    }

    private fun setBackPressDialogCount() {
        val backPressDialogCount =
            defaultPrefs(this).getInt(BACK_PRESS_SAMPLE_QUESTION_DIALOG_COUNT, 0)

        // increase back press sample question dialog count
        defaultPrefs(this).edit {
            putInt(BACK_PRESS_SAMPLE_QUESTION_DIALOG_COUNT, backPressDialogCount + 1)
        }

        // Set true for this session as dialog has been shown
        defaultPrefs(this).edit {
            putBoolean(IS_BACK_PRESS_SAMPLE_QUESTION_SHOWN, true)
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.cameraSettingConfig.observeK(
            this,
            this::onCameraSettingSuccess,
            this::onCameraApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandlerCamera,
            this::updateCameraProgressBarState
        )

        viewModel.packageStatus.observeK(
            this,
            this::onPackageStatusSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )

        viewModel.navigateAsk.observe(
            this,
            EventObserver { uri ->
                viewModel.getCropScreenConfig(uri)
            }
        )

        viewModel.cropConfig.observe(
            this,
            EventObserver {

                viewModel.sendEvent(EventConstants.REACHED_CROP_SCREEN, hashMapOf(), true)

                val cropConfigValue = with(it.first) {
                    var buttonText = this.findSolutionButtonText
                    if (isForStatus) {
                        buttonText = "Post"
                    }
                    bundleOf(
                        CropConstant.INTENT_EXTRA_KEY_TITLE to this.cropScreenTitle,
                        CropConstant.INTENT_EXTRA_KEY_FIND_SOLUTION_BUTTON_TEXT to buttonText
                    )
                }

                val imageUri =
                    if (it.second == null) Uri.fromFile(File(it.first.sampleImageUri)) else it.second
                openCropImageActivity(cropConfigValue, imageUri)
                if (!isForStatus) {
                    viewModel.setTrickQuestionShownToTrue()
                    sendEvent(EventConstants.EVENT_NAME_CROP_PAGE)

                    lifecycleScope.launchWhenStarted {
                        delay(500)
                        isTakingPicture = false
                    }
                }
            }
        )

        viewModel.launchSampleQuestionFragment.observe(this) {
            showSampleQuestionFragment()
        }

        viewModel.removeSampleQuestionFragment.observe(this) {
            removeSampleQuestionFragment()
        }

        viewModel.launchDemoAnimation.observe(this) {
            openDemoAnimationScreen(it)
        }

        viewModel.removeDemoAnimation.observe(this) {
            removeDemoAnimationFragment()
        }

        viewModel.isAnyImagePresentInStorageLiveData.observe(this) {
            noImagesInGallery = it.not()
            if (noImagesInGallery && binding.galleryBottomSheet.isVisible) {
                binding.galleryBottomSheet.hide()
                openGalleryForImage(noImagesInGallery)
            }
        }

        viewModel.navigationBottomSheetIconItemsLiveData.observe(this) {
            if (it.isNotEmpty()) {
                setupIconsBottomSheet(it)
            }
        }

        viewModel.galleryImageListLiveData.observe(this) {
            galleryImagesAdapter.submitList(it)
        }

        viewModel.navigationBottomIconItemsLiveData.observe(this) {
            if (!it.isNullOrEmpty()) {
                setupBottomNavigationRecyclerView(it)
            } else {
                binding.bottomNavigationContainer.hide()
            }
        }

        viewModel.hideAskHistory.observe(this) {
            binding.imgQuestionAskedHistory.isVisible = it != true
        }

        viewModel.hideSearchIcon.observe(this) {
            binding.imgSearch.isVisible = it != true
        }
    }

    private fun handleTopIconsAndClickListeners() {

        binding.imgSearch.setImageResource(R.drawable.ic_search)
        binding.imgSearch.background =
            ContextCompat.getDrawable(this, R.drawable.bg_shape_circle_grey)

        binding.imgSearch.setOnClickListener {
            startInAppSearchPage()
        }
        binding.imgQuestionAskedHistory.setImageResource(R.drawable.ic_ques_asked_history)
        binding.toolTipTxt.background = ContextCompat.getDrawable(this, R.drawable.ic_tooltip_left)
        binding.imgQuestionAskedHistory.setOnClickListener {
            startQuestionAskedHistoryPage()
        }

        binding.imgFlash.setImageResource(R.drawable.ic_flash_auto)
        binding.imgFlash.setOnClickListener {
            shortsIntentDisposable.clear()
            handleFlashClick()
        }
    }

    private fun openCropImageActivity(cropConfigValue: Bundle, imageUri: Uri?) {
        val screenName = Constants.SCREEN_CROP
        var audioTooltipViewData: AudioTooltipViewData? = null
        if (AudioToolTipUtils.checkForCoreLoopSessionCount(screenName)) {
            AudioToolTipUtils.isAudioToolTipAvailable(screenName)?.let { list ->
                if (list.isNotEmpty()) {
                    audioTooltipViewData =
                        AudioToolTipUtils.buildAudioTooltipViewData(list, screenName)
                }
            }
        }

        CropImage.activity(imageUri).run {
            setBorderLineColor(ContextCompat.getColor(this@CameraActivity, R.color.colorAccent))
            setBorderCornerColor(ContextCompat.getColor(this@CameraActivity, R.color.colorAccent))
            val rect = Rect()
            binding.seeThroughView.getGlobalVisibleRect(rect)
            setGuidelines(CropImageView.Guidelines.ON)
            setMinCropWindowSize(
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    20f,
                    Resources.getSystem().displayMetrics
                ).toInt(),
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    20f,
                    Resources.getSystem().displayMetrics
                ).toInt()
            )

            setAutoZoomEnabled(true)

            // this whole flow of setting the crop window rect is hack in the next screen
            setCropWindow(rect.toRectF())
            cropConfigValue.putBoolean("isForStatus", isForStatus)
            cropConfigValue.putString(Constants.IMAGE_SOURCE, mImageSource)
            cropConfigValue.putString(Constants.SOURCE, intent.getStringExtra(Constants.SOURCE))
            cropConfigValue.putString(Constants.AUDIO_URL, audioTooltipViewData?.audioUrl.orEmpty())
            cropConfigValue.putString(
                Constants.IMAGE_URL_MUTE,
                audioTooltipViewData?.muteImageUrl.orEmpty()
            )
            cropConfigValue.putString(
                Constants.IMAGE_URL_UNMUTE,
                audioTooltipViewData?.unMuteImageUrl.orEmpty()
            )
            cropConfigValue.putString(
                Constants.MUTE_TEXT,
                audioTooltipViewData?.tooltipText.orEmpty()
            )
            start(this@CameraActivity, cropConfigValue)
        }
    }

    private fun removeDemoAnimationFragment() {
        supportFragmentManager.findFragmentByTag(DemoAnimationFragmentV1.TAG)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun openDemoAnimationScreen(value: Pair<Boolean, Int>) {
        shortsIntentDisposable.clear()
        binding.askQueDemoVideoButton.show()
        defaultPrefs().edit {
            putBoolean(SHOULD_SHOW_TOOL_TIP_FRAGMENT, false)
        }
        supportFragmentManager.findFragmentByTag(ToolTipFragment.TAG)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
        supportFragmentManager.beginTransaction().replace(
            R.id.toolTipContainer,
            DemoAnimationFragmentV1.newInstance(value.second, "camera_screen_animation"),
            DemoAnimationFragmentV1.TAG
        ).commit()
    }

    private fun setUpFlagrExperiment() {
        // Set false to show back press sample question dialog for this session
        defaultPrefs(this).edit {
            putBoolean(IS_BACK_PRESS_SAMPLE_QUESTION_SHOWN, false)
        }

        shouldShowToolTip = defaultPrefs(this).getBoolean(SHOULD_SHOW_TOOL_TIP_FRAGMENT, true)
        shouldShowSampleQuestionBottomLayout =
            defaultPrefs(this).getBoolean(SHOULD_SHOW_SAMPLE_QUESTION_BOTTOM_LAYOUT, true)
        toolTipFragmentCount = defaultPrefs(this).getInt(TOOL_TIP_SHOW_COUNT, 0)
        sampleQuestionShowCount = defaultPrefs(this).getInt(SAMPLE_QUESTION_SHOW_COUNT, 0)
        sampleQuestionBottomLayoutShowCount =
            defaultPrefs(this).getInt(SAMPLE_QUESTION_BOTTOM_LAYOUT_SHOW_COUNT, 0)

        if (userPreference.getSelectedLanguage() == "hi") {
            tydSearchFlow = Constants.VOICE_SEARCH_FIRST
        }

        if (userPreference.getSelectedLanguage() == "en") {
            tydSearchFlow = Constants.TEXT_SEARCH_FIRST
        }

        if (shouldShowToolTip && toolTipFragmentCount < MAX_TOOL_TIP_SHOW_COUNT) {
            binding.askQueDemoVideoButton.hide()
            supportFragmentManager.beginTransaction()
                .add(R.id.toolTipContainer, ToolTipFragment.newInstance(), ToolTipFragment.TAG)
                .commit()
            defaultPrefs().edit {
                putInt(TOOL_TIP_SHOW_COUNT, toolTipFragmentCount + 1)
            }
        } else {
            binding.askQueDemoVideoButton.show()
        }

        mShowBottomNavigation = (
                shouldShowSampleQuestionBottomLayout &&
                        sampleQuestionBottomLayoutShowCount < MAX_SAMPLE_QUESTION_BOTTOM_LAYOUT_SHOW_COUNT
                ).not() &&
                isForStatus.not() &&
                intent?.getBooleanExtra(INTENT_EXTRA_IS_USER_OPENED, true)?.not() ?: true
    }

    override fun onClick(v: View?) {
        if (v == null) return

        when (v) {
            binding.askQueDemoVideoButton -> {
                shortsIntentDisposable.clear()
                if (!shouldProceed) {
                    return
                }
                sendEvent(EventConstants.EVENT_NAME_DEMO_VIDEO_CLICKED)
                viewModel.publishDemoVideoClickEvent(EventConstants.EVENT_NAME_CAMERA_V2)
                val demoAnimationActivityIntent =
                    DemoAnimationActivity.getStartIntent(v.context, 0, "question_kaise_poochen")
                startActivity(demoAnimationActivityIntent)
            }
            binding.imgFlash -> {
                shortsIntentDisposable.clear()
                handleFlashClick()
            }
            binding.imageClose -> {
                shortsIntentDisposable.clear()
                viewModel.publishEventWith(
                    EventConstants.EVENT_NAME_CAMERA_CLOSE_BUTTON_CLICKED,
                    EventConstants.EVENT_NAME_CAMERA_V2,
                    ignoreSnowplow = true
                )
                onBackPressed()
            }
            binding.imgCamera -> {
                shortsIntentDisposable.clear()
                if (!PermissionUtils.hasPermissions(this, arrayOf(Manifest.permission.CAMERA))) {
                    requestPermission()
                } else {
                    if (!shouldProceed) {
                        return
                    }
                    takePicture()
                    sendEvent(EventConstants.EVENT_NAME_CAPTURE_IMAGE_BUTTON_CLICKED)
                    sendEventForCameraScreenASKCleverTap(EventConstants.EVENT_NAME_ASK_QUESTION_CAMERA_BUTTON)
                    viewModel.sendEvent(
                        EventConstants.EVENT_NAME_CAPTURE_IMAGE_BUTTON_CLICKED,
                        getScreenOrientationEventParams(), true
                    )
                }
            }
            binding.imgGallery -> {
                shortsIntentDisposable.clear()
                if (!shouldProceed) {
                    return
                }

                if (isForStatus) {
                    openGalleryForImage()
                    return
                }

                if (binding.galleryBottomSheet.isVisible) {
                    binding.galleryPlaceholderImagesRecyclerView.hide()
                    binding.galleryBottomSheet.hide()
                    viewModel.sendEvent(EventConstants.GALLERY_TAPPED_CLOSE, ignoreSnowplow = true)
                } else if (hasExternalStorageReadPermission()) {
                    showImagesFromGallery()
                    viewModel.sendEvent(EventConstants.GALLERY_TAPPED_OPEN, ignoreSnowplow = true)
                } else if (defaultPrefs().getBoolean(
                        Constants.READ_STORAGE_DENIED_ON_GALLERY_CLICK,
                        false
                    ).not()
                ) {
                    requestReadPermission()
                } else {
                    openGalleryForImage()
                }
                viewModel.sendEvent(
                    EventConstants.GALLERY_BUTTON_CLICKED_NEW,
                    ignoreSnowplow = true
                )
            }
            binding.imgText -> {
                shortsIntentDisposable.clear()
                if (!shouldProceed) {
                    return
                }
                Utils.executeIfContextNotNull(this) {
                    TYDActivity.getStartIntent(this@CameraActivity, tydSearchFlow)
                        .apply {
                            startActivity(this)
                        }
                }

                sendEvent(EventConstants.EVENT_NAME_TEXT_SEARCH_BUTTON_CLICKED)
                sendEventForCameraScreenASKCleverTap(EventConstants.EVENT_NAME_ASK_QUESTION_TEXT_BUTTON)
                viewModel.sendEvent(
                    EventConstants.EVENT_NAME_TEXT_SEARCH_BUTTON_CLICKED,
                    getScreenOrientationEventParams(),
                    ignoreSnowplow = true
                )
            }
            binding.ivVoiceSearch -> {
                shortsIntentDisposable.clear()
                if (!shouldProceed) {
                    return
                }
                viewModel.sendEvent(
                    EventConstants.VOICE_SEARCH_CAMERA_BUTTON_TAPPED,
                    hashMapOf(),
                    ignoreSnowplow = true
                )
                TYDActivity.getStartIntent(this@CameraActivity, tydSearchFlow)
                    .apply {
                        startActivity(this)
                    }
            }
            binding.imgQuestionAskedHistory -> {
                shortsIntentDisposable.clear()
                startQuestionAskedHistoryPage()
            }
            binding.layoutBottomInfoView -> {
                shortsIntentDisposable.clear()
                viewModel.publishEventWith(
                    EventConstants.EVENT_NAME_CAMERA_DUMMY_BUTTON_CLICKED,
                    EventConstants.EVENT_NAME_CAMERA_V2
                )
            }
            binding.layoutBottomListView -> {
                shortsIntentDisposable.clear()
                if (!shouldProceed) {
                    return
                }
                performAction(ShowSampleQuestion(sampleQuestionAdapter.subjectEntityList[0]))
            }

            binding.imgSearch -> {
                shortsIntentDisposable.clear()
                startInAppSearchPage()
            }

            binding.cardViewReferralWidget -> {
                deeplinkAction.performAction(this@CameraActivity, referAndEarnDeeplink)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EVENT_TAG_QA_CAMERA_CLICK,
                        hashMapOf("title" to titleReferAndEarn)
                    )
                )
            }

            binding.imgClose -> {
                binding.cardViewReferralWidget.visibility = View.GONE
                binding.imgReferral.visibility = View.GONE
            }
        }
    }

    private fun startInAppSearchPage() {
        if (UserUtil.getIsGuestLogin()) {
            FailedGuestLoginActivity.getStartIntent(
                this@CameraActivity, popupDetails = PopupDetails(
                    imageUrl = null,
                    title = getString(R.string.guest_login_popup_title),
                    subtitle = null,
                    ctaText = getString(R.string.login)
                ),
                source = TAG
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                startActivity(this)
            }
            return
        }
        startActivity(
            InAppSearchActivity.getStartIntent(
                this, TAG, false,
                "", null, "", ""
            )
        )
    }

    private fun startQuestionAskedHistoryPage() {
        viewModel.sendEvent(EventConstants.EVENT_CAMERA_HISTORY_ICON_CLICKED, hashMapOf())
        defaultPrefs().edit {
            putBoolean("question_ask_history_clicked", true)
            apply()
        }
        startActivity(QuestionAskedHistoryActivity.getStartIntent(baseContext))
    }

    private fun handleFlashClick() {
        mCurrentFlash = (mCurrentFlash + 1) % flashImage.size
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Glide.with(this)
                .load(resources.getDrawable(flashImage[mCurrentFlash], this.theme))
                .into(binding.imgFlash)
        } else {
            Glide.with(this)
                .load(ContextCompat.getDrawable(this, flashImage[mCurrentFlash]))
                .into(binding.imgFlash)
        }
        imageCapture?.flashMode = flashOptions[mCurrentFlash]
        sendEvent(EventConstants.EVENT_NAME_FLASH_SWITCHING)
        if (mCurrentFlash == 1) {
            viewModel.publishEventWith(
                EventConstants.EVENT_NAME_CAMERA_FLASH_TURN_OFF,
                EventConstants.EVENT_NAME_CAMERA_V2
            )
        } else {
            viewModel.publishEventWith(
                EventConstants.EVENT_NAME_CAMERA_FLASH_TURN_ON,
                EventConstants.EVENT_NAME_CAMERA_V2
            )
        }
    }

    private fun setListeners() {
        binding.askQueDemoVideoButton.setOnClickListener(this)
        binding.imgFlash.setOnClickListener(this)
        binding.imageClose.setOnClickListener(this)
        binding.imgCamera.setOnClickListener(this)
        binding.imgGallery.setOnClickListener(this)
        binding.imgText.setOnClickListener(this)
        binding.ivVoiceSearch.setOnClickListener(this)
        binding.imgQuestionAskedHistory.setOnClickListener(this)
        binding.layoutBottomInfoView.setOnClickListener(this)
        binding.imgSearch.setOnClickListener(this)
        binding.imgClose.setOnClickListener(this)
        binding.cardViewReferralWidget.setOnClickListener(this)
        binding.main.isFocusableInTouchMode = true
        binding.main.requestFocus()
    }

    private fun handleShortsView(shortsData: ShortsData?) {
        if (shortsData == null || shortsData.showShorts != true || isForStatus || UserUtil.getIsGuestLogin()) {
            binding.shortsView.isVisible = false
            toggleShortsAnimationView(false)
            return
        }
        binding.shortsView.isVisible = true
        handleShortsAnimationTimer(shortsData)
        lifecycleScope.launchWhenResumed {
            binding.shortsView.setOnTouchListener(object :
                OnSwipeTouchListener(this@CameraActivity) {
                override fun onSwipeTop() {
                    super.onSwipeTop()
                    if (shortsData.showShorts != true) return
                    openShortsActivity()
                }
            })
        }
    }

    private fun openShortsActivity() {
        isShortsIntentOpened = true
        shortsIntentDisposable.clear()
        if (isForStatus || UserUtil.getIsGuestLogin()) {
            return
        }
        val intent = FragmentWrapperActivity.getShortsIntent(
            this@CameraActivity,
            null,
            "DEFAULT",
            "CAMERA"
        )
        startActivity(intent)
    }

    private fun handleShortsAnimationTimer(shortsData: ShortsData?) {

        if (isForStatus || UserUtil.getIsGuestLogin() ||
            isShortsIntentOpened || shortsData == null ||
            shortsData.showShortsAnimation != true
        ) {
            toggleShortsAnimationView(false)
            return
        }
        shortsIntentDisposable.clear()
        shortsIntentDisposable.add(
            Observable.timer(shortsData.shortsAnimationTimeout ?: 0L, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        if (!isFromNewIntent) {
                            lifecycleScope.launchWhenResumed {
                                binding.tvShortsTitle.text = shortsData.shortsAnimationTitle
                                toggleShortsAnimationView(true)
                                binding.shortsAnimation.applyAnimationFromAsset(
                                    animationFile = "lottie_camera_to_shorts.zip",
                                    repeatCount = 1,
                                    onAnimationEnd = {
                                        toggleShortsAnimationView(false)
                                    }
                                )
                            }
                        }
                        shortsIntentDisposable.clear()
                    }

                    override fun onNext(t: Long) {}

                    override fun onError(e: Throwable) {
                        shortsIntentDisposable.clear()
                    }
                })
        )
    }

    private fun toggleShortsAnimationView(shouldShow: Boolean) {
        binding.shortsAnimationView.isVisible = shouldShow
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        isFromNewIntent = true
    }

    private fun unAuthorizeUserError() {
        supportFragmentManager.beginTransaction()
            .add(BadRequestDialog.newInstance("unauthorized"), "BadRequestDialog").commit()
    }

    private fun onCameraApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandlerCamera() {}

    private fun updateCameraProgressBarState(state: Boolean) {}

    private fun setUpToolTipAudio(screenName: String) {
        if (AudioToolTipUtils.checkForCoreLoopSessionCount(screenName)) {
            val list = AudioToolTipUtils.isAudioToolTipAvailable(screenName).orEmpty()
            if (list.isNotEmpty()) {
                val audioTooltipViewData =
                    AudioToolTipUtils.buildAudioTooltipViewData(list, screenName)
                setUpMediaPlayer(audioTooltipViewData)
                UXCam.logEvent("${UXCAM_TAG}_${UXCAM_EVENT_AUDIO_TOOLTIP}")
            }
        } else {
            removeAudioTooltip()
        }
    }

    private fun removeAudioTooltip() {
        binding.audioTooltipView.apply {
            stopMedia()
            gone()
        }
    }

    private fun setUpMediaPlayer(data: AudioTooltipViewData) {
        binding.audioTooltipView.apply {
            visible()
            setData(data)
            registerLifecycle(lifecycle)
        }
    }

    private fun onCameraSettingSuccess(cameraSettingEntity: CameraSettingEntity) {

        if (isAlertShowing) {
            return
        }

        // Start - D0 User Ui
        val d0UserOverlayData = cameraSettingEntity.d0UserData?.overlayData
        val d0UserTimerData = cameraSettingEntity.d0UserData?.timerData

        // UX cam event
        if (d0UserOverlayData != null || d0UserTimerData != null) {
            UXCam.logEvent("{$UXCAM_TAG}_${UXCAM_EVENT_D0_5_QA}")
        }

        if (d0UserOverlayData != null) {
            binding.d0UserCameraOverlay.apply {
                root.visible()
                ivHeader.loadImageEtx(d0UserOverlayData.image)
                tvTitle.text = d0UserOverlayData.title
                btCta.apply {
                    text = d0UserOverlayData.cta
                }
                listOf(btCta, root).forEach {
                    it.setOnClickListener {
                        root.gone()
                    }
                }
            }
        } else {
            binding.d0UserCameraOverlay.root.gone()
        }

        if (d0UserTimerData != null) {
            binding.d0UserTimerLayout.apply {
                root.visible()
                TextViewUtils.setTextFromHtml(tvD0Title, d0UserTimerData.title.orEmpty())
                d0UserTimerData.expirationTime?.let { expirationTime ->
                    val remainingTime = expirationTime - System.currentTimeMillis()
                    startTimerForD0User(remainingTime)
                }
                ivD0TimerStart.apply {
                    isVisible = d0UserTimerData.icon.isNotNullAndNotEmpty()
                    loadImageEtx(d0UserTimerData.icon)
                }
                ivD0TimerClose.setOnClickListener {
                    root.gone()
                }
            }
        } else {
            binding.d0UserTimerLayout.root.gone()
        }
        // End - D0 User Ui

        if (shouldShowSampleQuestionBottomLayout && sampleQuestionBottomLayoutShowCount < MAX_SAMPLE_QUESTION_BOTTOM_LAYOUT_SHOW_COUNT) {
            setUpSampleQuestionRecyclerView(cameraSettingEntity.bottomOverlay?.subjectList!!)
            defaultPrefs().edit {
                putInt(
                    SAMPLE_QUESTION_BOTTOM_LAYOUT_SHOW_COUNT,
                    sampleQuestionBottomLayoutShowCount + 1
                )
            }
            binding.layoutBottomListView.show()
        } else {
            binding.layoutBottomListView.hide()
            setUpNcertView(cameraSettingEntity)
        }

        handleShortsView(cameraSettingEntity.shortsData)
        showReferAndEarnWidget(cameraSettingEntity)
    }

    private fun showReferAndEarnWidget(cameraSettingEntity: CameraSettingEntity) {
        if (!isForStatus) {
            cameraSettingEntity.referralWidgetData?.let { referralData ->
                binding.cardViewReferralWidget.visibility = View.VISIBLE
                binding.imgReferral.visibility = View.VISIBLE
                binding.imgReferral.loadImage(referralData.imageUrl)
                TextViewUtils.setTextFromHtml(
                    binding.tvTitleReferAndEarn,
                    referralData.title.orEmpty()
                )
                TextViewUtils.setTextFromHtml(
                    binding.tvSubtitleReferandEarn, referralData.subtitle.orEmpty(),
                )
                binding.tvTitleReferAndEarn.applyTextSize(referralData.titleTextSize)
                binding.tvSubtitleReferandEarn.applyTextSize(referralData.subtitleTextSize)

                referAndEarnDeeplink = referralData.deepLink.orEmpty()
                titleReferAndEarn = referralData.title.orEmpty()
            }

            changeConstraintsOfReferralCardView()

            val radius = resources.getDimension(R.dimen.dimen_152dp)
            val shapeAppearanceModel = ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(CornerFamily.ROUNDED, radius)
                .build()
            val materialShapeDrawable = MaterialShapeDrawable(shapeAppearanceModel)
            materialShapeDrawable.fillColor =
                ContextCompat.getColorStateList(this, R.color.light_blue_camera_page)
            binding.cardViewReferralWidget.background = materialShapeDrawable
        }
    }

    private fun changeConstraintsOfReferralCardView() {
        if (referAndEarnDeeplink.isNotNullAndNotEmpty()) {
            binding.cardViewReferralWidget.updateLayoutParams<ConstraintLayout.LayoutParams> {
                val marginTop = 60.dpToPx()
                val marginEnd = 25.dpToPx()
                val marginStart = 60.dpToPx()
                setMargins(marginStart, marginTop, marginEnd, 0)
            }
            binding.cardViewReferralWidget.visibility = View.VISIBLE
        }
    }

    private fun startTimerForD0User(millisInFuture: Long) {
        day0UserTimer = object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                        TimeUnit.HOURS.toMinutes(hours)
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(minutes) - TimeUnit.HOURS.toSeconds(hours)

                val remainingTime = "${hours}h : ${minutes}m : ${seconds}s"
                binding.d0UserTimerLayout.tvRemainingTime.text = remainingTime
            }

            override fun onFinish() {
                binding.d0UserTimerLayout.tvRemainingTime.text =
                    getString(R.string.d0_user_end_time)
            }
        }
        day0UserTimer?.start()
    }

    private fun setUpNcertView(cameraSettingEntity: CameraSettingEntity) {
        if (cameraSettingEntity.ncertWatchedPlaylist != null &&
            cameraSettingEntity.ncertWatchedPlaylist?.list.isNullOrEmpty().not() &&
            mShowBottomNavigation.not()
        ) {
            val list: List<NcertViewItemEntity> =
                cameraSettingEntity.ncertWatchedPlaylist?.list?.map {
                    NcertViewItemEntity(
                        id = it.id.orEmpty(),
                        name = it.name.orEmpty(),
                        description = it.description.orEmpty(),
                        isLast = it.isLast.orEmpty(),
                        parent = it.parent.orEmpty(),
                        resourceType = it.resourceType.orEmpty(),
                        studentClass = it.studentClass.orEmpty(),
                        subject = it.subject.orEmpty(),
                        mainDescription = it.mainDescription.orEmpty()
                    )
                }.orEmpty()

            binding.layoutBottomNcertListView.show()
            binding.ncertTitle.show()
            binding.ncertTitle.text = cameraSettingEntity.ncertWatchedPlaylist?.title.orEmpty()
            binding.ncertRecyclerView.show()
            setUpNcertRecyclerView(list)
            viewModel.publishEventWith(
                EventConstants.NCERT_RE_ENTRY_CAMERA,
                EventConstants.EVENT_NAME_CAMERA_V2,
                true
            )
        }
    }

    private fun setUpNcertRecyclerView(ncertViewItemEntityList: List<NcertViewItemEntity>) {
        binding.ncertRecyclerView.adapter = ncertAdapter
        ncertAdapter.updateData(ncertViewItemEntityList)
        binding.ncertRecyclerView.post {
            setGalleryBottomSheetPeekHeight()
        }
    }

    private fun showSampleQuestionFragment() {
        shortsIntentDisposable.clear()
        supportFragmentManager.findFragmentByTag(ToolTipFragment.TAG)?.let {
            supportFragmentManager.beginTransaction().remove(it)
        }
        binding.toolTipContainer.hide()
        defaultPrefs().edit {
            putBoolean(SHOULD_SHOW_TOOL_TIP_FRAGMENT, false)
        }
        binding.askQueDemoVideoButton.show()
    }

    private fun removeSampleQuestionFragment() {
        supportFragmentManager.findFragmentByTag(SampleQuestionFragment.TAG)?.let {
            supportFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) {
            // TODO show proper msg to the user
            if (isForStatus) {
                isTakingPicture = false
            }
            return
        }

        val result = CropImage.getActivityResult(data)

        viewModel.sendAskedQuestionEvent(AppEventsConstants.EVENT_NAME_SEARCHED, true)

        when (requestCode) {

            REQUEST_SELECT_IMAGE_GALLERY -> {
                if (resultCode == Activity.RESULT_OK) {
                    mImageSource = EventConstants.GALLERY
                    val uri = data.data ?: return
                    if (!isForStatus) {
                        viewModel.detectFaceInImage(uri)
                    }
                    viewModel.processGalleryImage(uri)
                    sendEventForCameraScreenCleverTap(EventConstants.EVENT_NAME_GALLERY_RETRIEVAL)
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {

                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if (isForStatus) {
                            openCreateStatusActivity(data)
                        } else {
                            openMatchResultScreen(data)
                        }
                    }

                    Activity.RESULT_CANCELED -> onCancelFromCrop()
                }
            }

            CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                onCropError(data)
            }
        }
    }

    private fun onCropError(data: Intent?) {
        val result = CropImage.getActivityResult(data)
        val cropError: Throwable? = result.error
        if (cropError != null) {
            ToastUtils.makeText(this, cropError.message ?: "", Toast.LENGTH_LONG).show()
        } else {
            ToastUtils.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show()
        }

        if (isForStatus) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun onCancelFromCrop() {
        if (isForStatus) {
            setResult(Activity.RESULT_CANCELED)
            finish()
        } else {
            // Close fully expanded gallery
            mGalleryBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            val params = Bundle()
            params.putString("student_id", getStudentId())
            sendEvent(EventConstants.EVENT_NAME_CLOSE_FROM_CROP)
            viewModel.publishEventWith(
                EventConstants.EVENT_NAME_CLOSE_FROM_CROP,
                EventConstants.EVENT_NAME_CAMERA_V2
            )
        }
    }

    private fun openMatchResultScreen(data: Intent?) {
        val result = CropImage.getActivityResult(data)
        if (viewModel.anyFaceExists) {
            showInvalidImageDialog()
            viewModel.sendEvent(
                EventConstants.EVENT_SELFIE_IMAGE,
                hashMapOf("source" to "Gallery"),
                ignoreSnowplow = true
            )
        } else {

            // Save Ocr locally if network is not available and show notification when internet will be available.
            processOcrFromImage(result.uri)

            val countToSendEvent: Int =
                Utils.getCountToSend(RemoteConfigUtils.getEventInfo(), EventConstants.QUESTION_ASK)
            repeat((0 until countToSendEvent).count()) {
                sendEvent(EventConstants.QUESTION_ASK)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        AppEventsConstants.EVENT_NAME_SUBMIT_APPLICATION,
                        hashMapOf()
                    )
                )
            }
            MatchQuestionActivity.getStartIntent(
                this,
                result.uri.path
                    ?: "",
                "", intent.getStringExtra(Constants.SOURCE).orEmpty()
            ).apply {
                startActivity(this)
            }
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)

            val cropWindowAdjusted = data?.getBooleanExtra(
                CropConstant.RESULT_INTENT_EXTRA_KEY_CROP_WINDOW_ADJUSTED,
                false
            )
            if (cropWindowAdjusted != null) {
                sendEvent(EventConstants.EVENT_NAME_CROPPER_USED)
            }
            Utils.sendClassLangEvents(EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK)
            Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK)

            sendEvent(EventConstants.EVENT_NAME_SUBMIT_FROM_CROP)
            sendEvent(EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK)
            viewModel.sendEvent(
                EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                    put(
                        EventConstants.ORIENTATION,
                        viewModel.screenOrientationLiveData.value
                            ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                    )
                },
                true
            )

            val countToSend: Int =
                Utils.getCountToSend(
                    RemoteConfigUtils.getEventInfo(),
                    EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK
                )
            val eventMap = hashMapOf<String, Any>().apply {
                put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                put(
                    EventConstants.ORIENTATION,
                    viewModel.screenOrientationLiveData.value
                        ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                )
            }
//            repeat((0 until countToSend).count()) {
//                analyticsPublisher.publishBranchIoEvent(
//                    AnalyticsEvent(
//                        EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK,
//                        eventMap,
//                        ignoreSnowplow = true
//                    )
//                )
//            }

            if (UserUtil.checkisClass9to13User()) {
//                analyticsPublisher.publishBranchIoEvent(
//                    AnalyticsEvent(
//                        EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK_9_13,
//                        eventMap
//                    )
//                )
            }

            val userSelectedExamsList = userPreference.getUserSelectedExams().split(",")
            if (userSelectedExamsList.contains("IIT JEE")) {
                val countToSendEvent: Int =
                    Utils.getCountToSend(
                        RemoteConfigUtils.getEventInfo(),
                        EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK_IIT
                    )
                repeat((0 until countToSendEvent).count()) {
                    analyticsPublisher.publishBranchIoEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK_IIT,
                            eventMap
                        )
                    )
                }
            }
            if (userSelectedExamsList.contains("NEET")) {
                val countToSendEvent: Int =
                    Utils.getCountToSend(
                        RemoteConfigUtils.getEventInfo(),
                        EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK_NEET
                    )
                repeat((0 until countToSendEvent).count()) {
                    analyticsPublisher.publishBranchIoEvent(
                        AnalyticsEvent(
                            EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK_NEET,
                            eventMap
                        )
                    )
                }
            }

            sendEventForCameraScreenASKCleverTap(EventConstants.EVENT_NAME_ASK_QUESTION_SUBMIT_BUTTON_CLICK)
            sendEventForCameraScreenASKCleverTap(EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK_CLEVER_TAP)

            viewModel.storeQuestionAskCoreAction()
        }
    }

    private fun processOcrFromImage(uri: Uri) {
        if (NetworkUtil(this).isConnected()) return
        viewModel.runTextRecognition(uri)
        viewModel.sendEvent(EventConstants.OCR_FROM_IMAGE_STORED_IN_DB, ignoreSnowplow = true)
    }

    private fun openCreateStatusActivity(data: Intent?) {
        val result = CropImage.getActivityResult(data)

        val intent = Intent()
        intent.putExtras(data!!.extras!!)
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun showInvalidImageDialog() {
        val dialog = InvalidImageDialogFragment.newInstance()
        dialog.show(supportFragmentManager, INVALID_IMAGE_DIALOG)
    }

    private fun takePicture() {
        if (isTakingPicture) return
        val destinationFileName = "CameraTempImage" + ".jpg"
        val file = File(cacheDir, destinationFileName)
        binding.progressBarTwo.show()
        isTakingPicture = true
        val outputFileOptions: OutputFileOptions = OutputFileOptions.Builder(file).build()
        imageCapture?.takePicture(
            outputFileOptions,
            Executors.newSingleThreadExecutor(),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    runOnUiThread { binding.progressBarTwo.hide() }
                    viewModel.detectFaceInImage(Uri.fromFile(file))
                    viewModel.processCameraImage(file)
                    mImageSource = EventConstants.CAMERA
                }

                // Occurs every time when user closes the CameraActivity too fast after capturing the image
                override fun onError(exception: ImageCaptureException) {
                    runOnUiThread { binding.progressBarTwo.hide() }
                    Log.e(exception)
                    isTakingPicture = false
                }
            }
        )
    }

    private fun updateProfile() {
        viewModel.updateProfileClass()
            .observe(this) { response ->
                when (response) {

                    is Outcome.Progress -> {
                    }
                    is Outcome.Failure -> {
                        supportFragmentManager.beginTransaction()
                            .add(NetworkErrorDialog.newInstance(), "NetworkErrorDialog").commit()
                    }
                    is Outcome.ApiError -> {
                        toast(getString(R.string.api_error))
                    }
                    is Outcome.Success -> {
                        defaultPrefs(this).edit {
                            putBoolean(Constants.CLASSAPIHIT, true)
                        }
                    }
                    else -> {}
                }
            }
    }

    private fun setUpForStatus() {
        binding.apply {
            askQueDemoVideoButton.hide()
            galleryBottomSheet.hide()
            captureQuestionPhotoText.hide()
            alertView.hide()
            layoutBottomView.hide()
            askQueDemoVideoButton.hide()
            imgText.hide()
            ivVoiceSearch.hide()
            toolTipContainer.hide()
            imageClose.hide()
            questionAskedHistoryLayout.hide()
            iconsBottomSheet.hide()
            bottomNavigationContainer.hide()
            seeThroughView.visibility = View.INVISIBLE // not hide
            imgSearch.visibility = View.GONE
            imgCameraRotate.show()
            imgCameraRotate.setOnClickListener { swapCamera() }
        }
    }

    private fun init() {
        if (intent.getStringExtra(Constants.SOURCE).orEmpty() in setOf(
                "status",
                Constants.STUDY_GROUP
            )
        ) {
            isForStatus = true
        }
        setUpFlagrExperiment()

        viewModel.isForStatus = isForStatus

        if (tydSearchFlow == 1) {
            binding.ivVoiceSearch.isVisible = true
            binding.imgText.isVisible = false
        } else {
            binding.ivVoiceSearch.isVisible = false
            binding.imgText.isVisible = true
        }

        setUpOrientationChangedListener()

        if (defaultPrefs().getBoolean("question_ask_history_clicked", false)) {
            binding.toolTipTxt.setVisibleState(false)
        }

        val cameraScreenShownCount = defaultPrefs(this).getLong("camera_s_v_c", 0)
        defaultPrefs(this).edit {
            putLong("camera_s_v_c", cameraScreenShownCount + 1)
        }

        viewModel.getCameraScreenConfig()
        // Saving the screen config value required by the next crop screen
        viewModel.saveCropScreenConfigValue()

        viewModel.checkTrickyQuestionButtonShown()

        setListeners()

        viewModel.getDemoAnimation()
        eventTracker = getTracker()
        sendEvent(EventConstants.EVENT_NAME_REACH_ASK_PAGE)
        permissionAllow()

        if (!TextUtils.isEmpty(getStudentId()) &&
            !defaultPrefs(this).getBoolean(Constants.CLASSAPIHIT, false)
        ) {
            updateProfile()
        }

        viewModel.updateFCMRegId()

        intent.getStringExtra(Constants.CROP_IMAGE_URL)?.let {
            trySampleQuestion(it)
        }

        setupImageGallery()

        if (mShowBottomNavigation) {
            setupBottomNavigation()
        }

        handleTopIconsAndClickListeners()

        if (isForStatus) {
            setUpForStatus()
        }

    }

    private fun setupImageGallery() {
        if (hasExternalStorageReadPermission()) {
            viewModel.checkIfAnyImagePresentInStorage()
        }

        binding.galleryPlaceholderImagesRecyclerView.adapter = GalleryPlaceholderImageAdapter(
            getDummyGalleryImageItemsList()
        )

        binding.galleryImagesRecyclerView.adapter = galleryImagesAdapter

        binding.galleryImagesRecyclerView.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    binding.galleryPlaceholderImagesRecyclerView.hide()
                }
            }
        })

        val galleryScrollUpArrowHeightPx = 12.dpToPx()
        val galleryScrollUpArrowVerticalPaddingPx = 16.dpToPx()

        // We will translate up gallery recycler views to hide up arrow
        val scrollUpArrowHideTranslationYValue = -galleryScrollUpArrowHeightPx -
                galleryScrollUpArrowVerticalPaddingPx

        mGalleryBottomSheetBehavior = BottomSheetBehavior.from(binding.galleryBottomSheet)
        mGalleryBottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.galleryImagesRecyclerView.hide()
                        viewModel.sendEvent(EventConstants.GALLERY_SWIPED_UP, ignoreSnowplow = true)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.galleryFragmentContainer.hide()
                        viewModel.loadAllImagesInGalleryLiveData.value = true
                    }
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        binding.galleryPlaceholderImagesRecyclerView.hide()
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        // STATE_HALF_EXPANDED is not required.
                        // If bottom sheet falls in this state, change to STATE_COLLAPSED
                        mGalleryBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                }
            }

            // Animated hide/show of views by changing transparency
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                if (mGalleryBottomSheetBehavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
                    binding.galleryImagesRecyclerView.show()
                }
                binding.apply {
                    galleryFragmentContainer.show()
                    galleryImagesRecyclerView.alpha = 1 - slideOffset
                    galleryPlaceholderImagesRecyclerView.alpha = 1 - slideOffset
                    layoutCameraInteractionView.alpha = 1 - slideOffset
                    ivGalleryScrollUp.alpha = 1 - slideOffset
                    iconsBottomSheet.alpha = 1 - slideOffset
                    rvBottomNavigation.alpha = 1 - slideOffset
                    galleryFragmentContainer.alpha = slideOffset
                    galleryFragmentContainer.translationY =
                        scrollUpArrowHideTranslationYValue * slideOffset
                    galleryImagesRecyclerView.translationY =
                        scrollUpArrowHideTranslationYValue * slideOffset
                }
            }
        })

        supportFragmentManager.commit {
            add(R.id.galleryFragmentContainer, GalleryFragment.newInstance())
        }

        setGalleryBottomSheetPeekHeight()
    }

    private fun setGalleryBottomSheetPeekHeight() {
        binding.layoutCameraInteractionView.post {
            // Get galleryBottomSheet's peek height
            val galleryImageItemHeightPx = 64.dpToPx()
            val galleryImageRecyclerViewBottomMarginPx = 8.dpToPx()
            val galleryScrollUpArrowHeightPx = 12.dpToPx()
            val galleryScrollUpArrowVerticalPaddingPx = 16.dpToPx()

            mGalleryBottomSheetBehavior?.peekHeight = (
                    binding.main.height -
                            binding.layoutCameraInteractionView.y.toInt() +
                            (galleryImageItemHeightPx + galleryImageRecyclerViewBottomMarginPx) +
                            (galleryScrollUpArrowHeightPx + galleryScrollUpArrowVerticalPaddingPx)
                    ).also {
                    // Also update halfExpandedRatio to prevent a bounce effect
                    mGalleryBottomSheetBehavior?.halfExpandedRatio =
                        (it.toFloat() / binding.main.height).coerceIn(.001F, .999F)
                }
        }
    }

    private fun setupBottomNavigation() {
        viewModel.getBottomNavigationItemsList(isDoubtFeedAvailable())
        viewModel.sendEvent(EventConstants.CAMERA_BOTTOM_NAVIGATION_VISIBLE, ignoreSnowplow = true)
    }

    private fun setupIconsBottomSheet(icons: List<TopOptionWidgetItem>) {
        binding.iconsBottomSheet.show()
        mIconsBottomSheetBehavior = BottomSheetBehavior.from(binding.iconsBottomSheet)
        mIconsBottomSheetBehavior?.peekHeight = binding.tvIconsBottomSheetTitle.run {
            textSize.ceil() + binding.tvIconsBottomSheetTitle.marginTop * 4
        }
        binding.iconsBottomSheet.updateLayoutParams {
            height =
                binding.layoutCameraInteractionView.height + (
                        mIconsBottomSheetBehavior?.peekHeight
                            ?: 0
                        )
        }

        binding.iconsBottomSheet.setOnClickListener {
            mIconsBottomSheetBehavior?.state =
                if (mIconsBottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED) {
                    BottomSheetBehavior.STATE_EXPANDED
                } else {
                    BottomSheetBehavior.STATE_COLLAPSED
                }
        }

        // Move camera screen buttons up to give space to bottom sheet
        val translationY = -(mIconsBottomSheetBehavior?.peekHeight?.toFloat() ?: 0f)
        binding.layoutCameraInteractionView.animate()
            .translationYBy(translationY)
            .setDuration(1)
            .withEndAction {
                binding.toolTipContainer.translationY = translationY
                setGalleryBottomSheetPeekHeight()
            }
            .start()

        ObjectAnimator.ofFloat(binding.iconsBottomSheet, View.TRANSLATION_Y, -(20f.dpToPx()))
            .setDuration(1000)
            .apply {
                repeatMode = ValueAnimator.REVERSE
                repeatCount = 1
            }
            .start()

        binding.rvIcons.apply {
            layoutManager = GridLayoutManager(this@CameraActivity, 4)
            adapter = TopOptionsAdapter(false, this@CameraActivity).also {
                it.updateOptions(icons)
            }
        }

        mIconsBottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when (newState) {
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        binding.tvIconsBottomSheetTitle.setText(R.string.swipe_down_to_ask_question)
                        binding.ivArrow.animate().rotation(180f).start()
                        viewModel.sendEvent(EventConstants.CAMERA_NAVIGATION_ICONS_SWIPE_UP)
                    }
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.tvIconsBottomSheetTitle.setText(R.string.swipe_up_for_more)
                        binding.ivArrow.animate().rotation(0f).start()
                        viewModel.sendEvent(EventConstants.CAMERA_NAVIGATION_ICONS_SWIPE_DOWN)
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                /* no-op */
            }
        })

        viewModel.sendEvent(
            EventConstants.CAMERA_NAVIGATION_ICONS_BOTTOM_SHEET_VISIBLE,
            ignoreSnowplow = true
        )
    }

    private fun setupBottomNavigationRecyclerView(list: List<BottomNavigationItemData>) {
        binding.bottomNavigationContainer.show()
        binding.rvBottomNavigation.adapter = BottomNavigationAdapter(list, this)
    }

    private fun setUpSampleQuestionRecyclerView(subjectEntityList: List<SubjectEntity?>) {
        binding.sampleQuestionRecyclerView.adapter = sampleQuestionAdapter
        sampleQuestionAdapter.updateData(subjectEntityList.subList(0, 1))
        binding.sampleQuestionRecyclerView.post {
            setGalleryBottomSheetPeekHeight()
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is ShowSampleQuestion -> {
                defaultPrefs().edit {
                    putBoolean(SHOULD_SHOW_SAMPLE_QUESTION_BOTTOM_LAYOUT, false)
                }
                shouldShowSampleQuestionBottomLayout = false
                viewModel.sendEvent(
                    EventConstants.EVENT_DEMO_QUESTION_CLICKED,
                    hashMapOf(),
                    ignoreSnowplow = true
                )
                viewModel.publishOnDemoQuestionClickEvent(
                    action.subjectEntity?.subject.orEmpty(),
                    EventConstants.EVENT_NAME_CAMERA_V2
                )
                trySampleQuestion(action.subjectEntity?.imageUrl.orEmpty())
                binding.layoutBottomListView.hide()
                setupBottomNavigation()
            }

            is PublishEvent -> {
                viewModel.sendEvent(action.event.name, action.event.params)
            }

            is OpenLibraryVideoPlayListScreen -> {
                openPlayListScreen(action)
            }

            is OpenLibraryPlayListActivity -> {
                openLibraryListingActivity(action)
            }

            is GalleryImageClicked -> {
                shortsIntentDisposable.clear()
                if (!shouldProceed) {
                    return
                }
                if (action.isDemoQuestion) {
                    trySampleQuestion(action.uri.toString())
                    viewModel.sendEvent(EventConstants.GALLERY_DEMO_CLICKED, ignoreSnowplow = true)
                } else {
                    viewModel.processGalleryImage(action.uri)
                }
                viewModel.sendEvent(
                    EventConstants.GALLERY_IMAGE_ON_CAMERA_CLICKED,
                    ignoreSnowplow = true
                )
            }

            is GalleryShowMoreClicked -> {
                shortsIntentDisposable.clear()
                openGalleryForImage()
                viewModel.sendEvent(
                    EventConstants.GALLERY_IMAGES_VIEW_ALL_CLICKED,
                    ignoreSnowplow = true
                )
            }

            is BottomNavigationItemClicked -> {
                shortsIntentDisposable.clear()
                deeplinkAction.performAction(this, action.itemData.deeplink.orEmpty())
                viewModel.sendEvent(
                    EventConstants.CAMERA_BOTTOM_NAVIGATION_CLICKED,
                    hashMapOf(
                        Constants.MOVE_TO to action.itemData.id.orEmpty(),
                        Constants.POSITION to action.position
                    )
                )
                finish()
            }

            is TopOptionClicked -> {
                shortsIntentDisposable.clear()
                deeplinkAction.performAction(this, action.data.deepLink)
                viewModel.sendEvent(
                    EventConstants.CAMERA_NAVIGATION_ICON_CLICK,
                    hashMapOf(
                        Constants.ID to action.data.id,
                        Constants.TITLE to action.data.title
                    )
                )
            }
        }
    }

    private fun openGalleryForImage(noImagesFoundInStorage: Boolean = false) {
        if (noImagesFoundInStorage) {
            noImagesToastShown = true
            viewModel.sendEvent(EventConstants.NO_IMAGES_IN_GALLERY, ignoreSnowplow = true)
        }
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_GALLERY)
        }
        sendEvent(EventConstants.PHONE_GALLERY_OPENED)
        sendEventForCameraScreenASKCleverTap(Constants.EVENT_NAME_ASK_QUESTION_FROM_GALLERY)
        viewModel.sendEvent(
            EventConstants.PHONE_GALLERY_OPENED,
            getScreenOrientationEventParams(),
            ignoreSnowplow = true
        )
    }

    private fun showImagesFromGallery() {
        if (noImagesInGallery) {
            openGalleryForImage(noImagesInGallery)
        } else {
            setGalleryBottomSheetPeekHeight()
            binding.galleryBottomSheet.show()
            if (galleryImagesAdapter.itemCount == 0) {
                binding.galleryPlaceholderImagesRecyclerView.show()
            }
            binding.askQueDemoVideoButton.show()
            binding.galleryImagesRecyclerView.show()
            binding.toolTipContainer.hide()
            binding.galleryFragmentContainer.hide()
            noImagesToastShown = false
            //region Code related to showing demo question in gallery
            if (defaultPrefs().getString(Constants.DEMO_QUESTION_URL, null) != null) {
                val galleryDemoQuestionShowCount =
                    defaultPrefs().getInt(GALLERY_DEMO_QUESTION_SHOW_COUNT, 0)
                if (galleryDemoQuestionShowCount <= MAX_GALLERY_DEMO_QUESTION_SHOW_COUNT) {
                    defaultPrefs().edit {
                        putInt(GALLERY_DEMO_QUESTION_SHOW_COUNT, galleryDemoQuestionShowCount + 1)
                    }
                    // If new count > max value, refresh fully expanded gallery images
                    if (galleryDemoQuestionShowCount + 1 > MAX_GALLERY_DEMO_QUESTION_SHOW_COUNT) {
                        viewModel.loadAllImagesInGalleryLiveData.value = true
                    }
                }
            }
            //endregion
            viewModel.sendEvent(
                EventConstants.GALLERY_IMAGES_VIEWED_ON_CAMERA,
                ignoreSnowplow = true
            )
        }
    }

    private fun getDummyGalleryImageItemsList(): List<GalleryImageViewItem> {
        val galleryItem = GalleryImageViewItem(uri = null)
        val dummyItemsCount = 15
        return mutableListOf<GalleryImageViewItem>().apply {
            repeat(dummyItemsCount) {
                add(galleryItem)
            }
        }
    }

    private fun openPlayListScreen(action: OpenLibraryVideoPlayListScreen) {
        Intent(this, FragmentHolderActivity::class.java).also {
            it.action = Constants.NAVIGATE_VIEW_PLAYLIST
            it.putExtra(Constants.PLAYLIST_ID, action.playlistId)
            it.putExtra(Constants.PLAYLIST_TITLE, action.playlistName)
            it.putExtra(Constants.IS_FROM_VIDEO_TAG, false)
            it.putExtra(Constants.QUESTION_ID, "")
            it.putExtra(Constants.VIDEO_TAG_NAME, "")
        }.apply {
            startActivity(this)
        }
    }

    private fun openLibraryListingActivity(action: OpenLibraryPlayListActivity) {
        LibraryListingActivity.getStartIntent(
            this,
            action.playlistId,
            action.playlistName,
            packageDetailsId = action.packageDetailsId.orEmpty(),
            page = "Camera"
        ).apply {
            startActivity(this)
        }
    }

    private fun trySampleQuestion(imageUrl: String) {
        binding.progressBarTwo.show()
        sampleQuestiondisposable.add(
            Single.fromCallable {
                BitmapUtils.getBitmapFromUrl(this, imageUrl)
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ currentBitmap ->
                    binding.progressBarTwo.hide()
                    if (currentBitmap != null) {
                        BitmapUtils.convertBitmapToByteArray(currentBitmap) { byteArray ->
                            byteArray?.let {
                                viewModel.processCameraImage(it)
                            }
                        }
                    }
                }, {
                    binding.progressBarTwo.hide()
                    ToastUtils.makeText(
                        this,
                        getString(R.string.somethingWentWrong),
                        Toast.LENGTH_SHORT
                    ).show()
                })
        )
    }

    /** Returns true if the device has an available back camera. False otherwise */
    private fun hasBackCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) ?: false
    }

    /** Returns true if the device has an available front camera. False otherwise */
    private fun hasFrontCamera(): Boolean {
        return cameraProvider?.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) ?: false
    }

    /** Enabled or disabled a button to switch cameras depending on the available cameras */
    private fun updateCameraSwitchButton() {
        try {
            binding.imgCameraRotate.isEnabled = hasBackCamera() && hasFrontCamera()
        } catch (exception: CameraInfoUnavailableException) {
            binding.imgCameraRotate.isEnabled = false
        }
    }

    /** Declare and bind preview, capture and analysis use cases */
    @SuppressLint("RestrictedApi")
    private fun bindCameraUseCases() {
        lifecycleScope.launchWhenResumed {
            val preview = Preview.Builder().build()

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build()

            val builder = ImageCapture.Builder()

            // Create an Extender object which can be used to apply extension
            // configurations.
            val bokehImageCapture = BokehImageCaptureExtender.create(builder)

            // Query if extension is available (optional).
            if (bokehImageCapture.isExtensionAvailable(cameraSelector)) {
                // Enable the extension if available.
                bokehImageCapture.enableExtension(cameraSelector)
            }

            imageCapture = builder.setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetAspectRatio(getSuitableAspectRatio())
                .setMaxResolution(Size(1920, 1440))
                .setFlashMode(flashOptions[mCurrentFlash])
                .build()

            cameraProvider?.unbindAll()

            try {
                preview.setSurfaceProvider(binding.cameraView.surfaceProvider)
                camera = cameraProvider?.bindToLifecycle(
                    this@CameraActivity, cameraSelector, preview, imageCapture
                )
                pinchToZoom()
            } catch (exc: Exception) {
                Log.e(exc)
                FirebaseCrashlytics.getInstance().recordException(exc)
                toast(R.string.error_starting_camera)
                showImagesFromGallery()
            }
        }
    }

    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private fun swapCamera() {
        lensFacing = if (CameraSelector.LENS_FACING_FRONT == lensFacing) {
            CameraSelector.LENS_FACING_BACK
        } else {
            CameraSelector.LENS_FACING_FRONT
        }
        // Re-bind
        // use cases to update selected camera
        bindCameraUseCases()
    }

    @SuppressLint("RestrictedApi")
    private fun setUpCamera() {

        if (AudioToolTipUtils.isUserBackToCameraPage()) {
            defaultPrefs().edit()
                .putInt(Constants.USER_BACK_TO_CAMERA_PAGE, 0).apply()
            setUpToolTipAudio(Constants.SCREEN_CAMERA_RETURN)
        } else {
            if (!isForStatus) {
                setUpToolTipAudio(Constants.SCREEN_CAMERA)
            }
        }

        binding.cameraView.implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        binding.cameraView.show()
        binding.cameraView.post {
            val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

            cameraProviderFuture.addListener(
                Runnable {
                    try {
                        cameraProvider = cameraProviderFuture.get()
                    } catch (e: java.lang.Exception) {
                        viewModel.sendEvent(
                            EventConstants.EVENT_CAMERA_ERROR,
                            hashMapOf<String, Any>().apply {
                                put(EventConstants.ERROR_MESSAGE, e.message.orEmpty())
                            },
                            ignoreSnowplow = true
                        )
                    }
                    lensFacing = when {
                        hasBackCamera() -> CameraSelector.LENS_FACING_BACK
                        hasFrontCamera() -> CameraSelector.LENS_FACING_FRONT
                        else -> NO_CAMERA_FOUND
                    }
                    // Enable or disable switching between cameras
                    updateCameraSwitchButton()
                    bindCameraUseCases()
                },
                ContextCompat.getMainExecutor(this)
            )
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun pinchToZoom() {
        // Listen to pinch gestures
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                // Get the camera's current zoom ratio
                val currentZoomRatio = camera?.cameraInfo?.zoomState?.value?.zoomRatio ?: 0F

                // Get the pinch gesture's scaling factor
                val delta = detector.scaleFactor

                // Update the camera's zoom ratio. This is an asynchronous operation that returns
                // a ListenableFuture, allowing you to listen to when the operation completes.
                camera?.cameraControl?.setZoomRatio(currentZoomRatio * delta)

                viewModel.sendEvent(EventConstants.PINCH_TO_ZOOM, ignoreSnowplow = true)

                // Return true, as the event was handled
                return true
            }
        }
        val scaleGestureDetector = ScaleGestureDetector(this@CameraActivity, listener)

        // Attach the pinch gesture listener to the viewfinder
        binding.cameraView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            return@setOnTouchListener true
        }
    }

    override fun onResume() {
        super.onResume()
        // Update timer and overlay data each time user visits on camera for D0 users
        viewModel.getCameraSetting(
            PermissionUtils.hasPermissions(
                this@CameraActivity,
                arrayOf(Manifest.permission.CAMERA)
            )
        )

        orientationEventListener?.enable()
        if (startCameraOnResume) {
            startCameraOnResume = false
            permissionAllow()
        } else {
            if (AudioToolTipUtils.isUserBackToCameraPage()) {
                defaultPrefs().edit()
                    .putInt(Constants.USER_BACK_TO_CAMERA_PAGE, 0).apply()
                setUpToolTipAudio(Constants.SCREEN_CAMERA_RETURN)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        orientationEventListener?.disable()
    }

    override fun onStop() {
        shortsIntentDisposable.clear()
        super.onStop()
        // Stop timer if running for D0 users
        day0UserTimer?.cancel()
        contentResolver.unregisterContentObserver(mGalleryImagesContentObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        orientationEventListener?.disable()
        sampleQuestiondisposable.dispose()
        shortsIntentDisposable.dispose()
        countDownTimer?.cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Constants.MY_PERMISSIONS_REQUEST_CAMERA -> {
                var valid = true
                for (grantResult in grantResults) {
                    valid = valid && grantResult == PackageManager.PERMISSION_GRANTED
                }
                if (!valid && !ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.CAMERA
                    )
                ) {
                    doNotAskAgain = true
                    sendEvent(EventConstants.EVENT_NAME_CAMERA_PERMISSION, true, false)
                    viewModel.publishOnCameraPermission(false, EventConstants.EVENT_NAME_CAMERA_V2)
                    sendEventForCameraPermissionCleverTap(
                        EventConstants.EVENT_NAME_CAMERA_PERMISSION,
                        show = true,
                        allow = false
                    )
                    supportFragmentManager.beginTransaction()
                        .add(RequiredPermissionDialog.newInstance(), RequiredPermissionDialog.TAG)
                        .commit()
                } else if (valid) {
                    viewModel.setCameraScreenShownToTrue()
                    setUpCamera()
                    // Refresh overlay after permission granted
                    viewModel.getCameraSetting(
                        PermissionUtils.hasPermissions(
                            this,
                            arrayOf(Manifest.permission.CAMERA)
                        )
                    )
                    viewModel.publishOnCameraPermission(true, EventConstants.EVENT_NAME_CAMERA_V2)
                    sendEvent(EventConstants.EVENT_NAME_CAMERA_PERMISSION, true, true)
                    sendEventForCameraPermissionCleverTap(
                        EventConstants.EVENT_NAME_CAMERA_PERMISSION,
                        true,
                        true
                    )
                } else {
                    onBackPressed()
                }
            }
            Constants.REQUEST_STORAGE_PERMISSION -> {
                if (PackageManager.PERMISSION_GRANTED in grantResults) {
                    showImagesFromGallery()
                    viewModel.refreshGalleryImageItemsList()
                    viewModel.checkIfAnyImagePresentInStorage()
                    viewModel.storageReadPermissionReceivedLiveData.value = true
                    viewModel.sendEvent(
                        EventConstants.GALLERY_READ_STORAGE_PERMISSION_GRANTED,
                        ignoreSnowplow = true
                    )
                } else {
                    openGalleryForImage()
                    defaultPrefs().edit {
                        putBoolean(Constants.READ_STORAGE_DENIED_ON_GALLERY_CLICK, true)
                    }
                    viewModel.sendEvent(
                        EventConstants.GALLERY_READ_STORAGE_PERMISSION_DENIED,
                        ignoreSnowplow = true
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        shortsIntentDisposable.clear()
        if (UserUtil.getIsGuestLogin()) {
            DoubtnutApp.INSTANCE.bus()?.send(RefreshUI())
            super.onBackPressed()
        }
        if (!isForStatus) {
            if (mGalleryBottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
                mGalleryBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            } else if (mIconsBottomSheetBehavior?.state == BottomSheetBehavior.STATE_EXPANDED) {
                mIconsBottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
            } else {
                viewModel.publishEventWith(
                    EventConstants.EVENT_NAME_BACK_FROM_CAMERA,
                    EventConstants.EVENT_NAME_CAMERA_V2, ignoreSnowplow = true
                )
                // Show Sample question dialog on back press for three session
                val shouldShowBackPressSampleQuestion =
                    defaultPrefs().getBoolean(SHOULD_SHOW_BACK_PRESS_SAMPLE_QUESTION, true)
                val isBackPressSampleQuestionShownOnThisSession =
                    defaultPrefs().getBoolean(IS_BACK_PRESS_SAMPLE_QUESTION_SHOWN, false)
                val backPressSampleQuestionShowCount =
                    defaultPrefs().getInt(BACK_PRESS_SAMPLE_QUESTION_DIALOG_COUNT, 0)

                if (!isBackPressSampleQuestionShownOnThisSession &&
                    viewModel.deepLinkForBackPressDialog.isNotNullAndNotEmpty()
                ) {
                    UXCam.logEvent("{$UXCAM_TAG}_${UXCAM_EVENT_BACK_PRESS_BOTTOM_SHEET}")
                    deeplinkAction.performAction(this, viewModel.deepLinkForBackPressDialog)
                    setBackPressDialogCount()
                } else if (!isBackPressSampleQuestionShownOnThisSession &&
                    !viewModel.backPressWidgets.isNullOrEmpty()
                ) {
                    showSampleQuestionDialogOnBackPress(
                        BackPressSampleQuestionFragmentV2.newInstance(true),
                        BackPressSampleQuestionFragmentV2.TAG
                    )
                } else if (!isBackPressSampleQuestionShownOnThisSession &&
                    shouldShowBackPressSampleQuestion &&
                    backPressSampleQuestionShowCount < MAX_BACK_PRESS_SAMPLE_QUESTION_DIALOG_COUNT
                ) {
                    showSampleQuestionDialogOnBackPress(
                        BackPressSampleQuestionFragment.newInstance(true),
                        BackPressSampleQuestionFragment.TAG
                    )
                } else {
                    if (intent.getStringExtra(Constants.SOURCE) == MainActivity.TAG || intent.getStringExtra(
                            Constants.SOURCE
                        ) == HomeFeedFragmentV2.TAG
                    ) {
                        setResultToCallingActivity()
                        super.onBackPressed()
                    } else if (intent.getStringExtra(Constants.SOURCE) == InAppSearchActivity.TAG) {
                        startActivity(
                            MainActivity.getStartIntent(
                                context = this,
                                recreate = false,
                                showCamera = false
                            )
                        )
                    } else {
                        super.onBackPressed()
                    }

                }
            }
        } else {
            if (intent.getStringExtra(Constants.SOURCE) == MainActivity.TAG || intent.getStringExtra(
                    Constants.SOURCE
                ) == HomeFeedFragmentV2.TAG
            ) {
                setResultToCallingActivity()
            }
            super.onBackPressed()
        }
    }

    private fun setResultToCallingActivity() {
        val resultIntent = Intent()
        resultIntent.putExtra(Constants.SCREEN_NAME, TAG)
        resultIntent.putExtra(
            FINISH_CALLING_ACTIVITY,
            viewModel.shouldD0UserExitAppIfPressBackButton
        )
        setResult(Activity.RESULT_OK, resultIntent)
    }

    private fun sendEvent(eventName: String, show: Boolean, allow: Boolean) {
        eventTracker.addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(this).toString())
            .addStudentId(getStudentId())
            .addScreenName(EventConstants.PAGE_MAIN_ASK_PAGE)
            .addEventParameter(EventConstants.PARAM_ALLOW, allow)
            .addEventParameter(EventConstants.PARAM_SHOW, show)
            .track()
    }

    private fun sendEvent(eventName: String) {
        if (!isForStatus) {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_MAIN_ASK_PAGE)
                .track()
        }
    }

    private fun sendEventForCameraScreenCleverTap(eventName: String) {
        if (!isForStatus) {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addEventParameter(EventConstants.PARAM_SUCCESS, true)
                .track()
        }
    }

    private fun sendEventForCameraScreenASKCleverTap(eventName: String) {
        if (!isForStatus) {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addEventParameter(EventConstants.PARAM_SUCCESS, true)
                .addEventParameter(
                    EventConstants.PARAM_CLASS,
                    getStudentClass()
                )
                .addEventParameter(
                    EventConstants.PARAM_LANGUAGE,
                    defaultPrefs(this).getString(Constants.STUDENT_LANGUAGE_CODE, "")
                        .orDefaultValue()
                )
                .addEventParameter(EventConstants.PARAM_TIMESTAMP, Utils.currentTime())
                .cleverTapTrack()
        }
    }

    private fun sendEventForCameraPermissionCleverTap(
        eventName: String,
        show: Boolean,
        allow: Boolean
    ) {
        if (!isForStatus) {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addEventParameter(EventConstants.PARAM_ALLOW, allow)
                .addEventParameter(EventConstants.PARAM_SHOW, show)
                .cleverTapTrack()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun permissionAllow() {
        if (PermissionUtils.hasPermissions(this, arrayOf(Manifest.permission.CAMERA))) {
            binding.cameraView.show()
            setUpCamera()
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.CAMERA),
                Constants.MY_PERMISSIONS_REQUEST_CAMERA
            )
        } else {
            binding.cameraView.show()
            if (binding.cameraView != null) {
                setUpCamera()
            }
        }
    }

    private fun requestReadPermission() {
        val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        ActivityCompat.requestPermissions(this, permissions, Constants.REQUEST_STORAGE_PERMISSION)
    }

    private fun hasExternalStorageReadPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestAgain() {

        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            )
        ) {
            val i = Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + BuildConfig.APPLICATION_ID)
            )
            startActivity(i)
            onBackPressed()
        } else {
            requestPermission()
        }
    }

    override fun onStart() {
        super.onStart()
        contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            mGalleryImagesContentObserver
        )
        viewModel.refreshGalleryImageItemsList()
        viewModel.sendEvent(EventConstants.REACHED_CAMERA_SCREEN, hashMapOf(), true)
        if (FeaturesManager.isFeatureEnabled(
                DoubtnutApp.INSTANCE,
                Features.DOUBT_PAYWALL
            ) && !isForStatus
        ) {
            shouldProceed = false
            viewModel.getPackageStatus()
        } else {
            shouldProceed = true
        }
    }

    private fun getSuitableAspectRatio(): Int {
        val display = binding.cameraView.display
        if (display != null) {
            val metrics = DisplayMetrics().also { display.getRealMetrics(it) }
            val width = metrics.widthPixels
            val height = metrics.heightPixels

            val previewRatio = max(width, height).toDouble() / min(width, height)
            if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
                return AspectRatio.RATIO_4_3
            }
            return AspectRatio.RATIO_16_9
        } else {
            return AspectRatio.RATIO_16_9
        }
    }

    private fun getScreenOrientationEventParams() =
        hashMapOf(
            EventConstants.ORIENTATION to
                    (
                            viewModel.screenOrientationLiveData.value
                                ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                            ) as Any
        )

    private fun setUpOrientationChangedListener() {
        orientationEventListener = object : CameraActivityOrientationEventListener(this) {
            override fun onSimpleOrientationChanged(orientation: Int) {
                imageCapture?.targetRotation = orientation
                currentOrientation = orientation

                when (orientation) {
                    Surface.ROTATION_0 -> {
                        updateCameraScreenUi(0, 1f)
                        viewModel.setScreenOrientationLiveData(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    }
                    Surface.ROTATION_90 -> {
                        updateCameraScreenUi(90)
                        viewModel.setScreenOrientationLiveData(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    }
                    Surface.ROTATION_180 -> {
                        updateCameraScreenUi(0)
                        viewModel.setScreenOrientationLiveData(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                    }
                    Surface.ROTATION_270 -> {
                        updateCameraScreenUi(-90)
                        viewModel.setScreenOrientationLiveData(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
                    }
                }
            }
        }
    }

    private fun updateCameraScreenUi(rotationAngle: Int, alphaValue: Float = 0f) {
        val rotateIconsList =
            listOf<View>(
                binding.imgGallery,
                binding.imgText,
                binding.imgFlash,
                binding.imgQuestionAskedHistory,
                binding.ivVoiceSearch,
                binding.imgSearch
            )
        rotateIconsList.forEach {
            it.animate().rotation(rotationAngle.toFloat()).setDuration(500).start()
        }

        binding.toolTipContainer.animate().alpha(alphaValue).setDuration(500).start()
    }

    private fun onPackageStatusSuccess(packageStatusEntity: PackageStatusEntity) {
        if (packageStatusEntity.subscription?.status == true) {
            shouldProceed = true
            if (packageStatusEntity.subscription?.dialogView == true) {
                showPackageDialog(
                    packageStatusEntity.subscription?.dialogViewInfo?.isCancel
                        ?: false,
                    packageStatusEntity.subscription?.dialogViewInfo?.descriptionOne,
                    packageStatusEntity.subscription?.dialogViewInfo?.descriptionTwo,
                    packageStatusEntity.subscription?.dialogViewInfo?.packageDesc,
                    packageStatusEntity.subscription?.status
                        ?: false
                )
            }

            if (packageStatusEntity.subscription?.alertView == true) {
                viewModel.publishEventWith(EventConstants.EVENT_NAME_CAMERA_MESSAGE_RENEW, "")
                isAlertShowing = true
                binding.layoutBottomListView.hide()
                supportFragmentManager.findFragmentByTag(ToolTipFragment.TAG)?.let {
                    supportFragmentManager.beginTransaction().remove(it).commit()
                }
                binding.alertView.show()
                binding.textViewVipExpiring.show()
                binding.textViewVipExpiring.text =
                    packageStatusEntity.subscription?.alertViewText.orEmpty()
                binding.buttomRenewNow.show()
            }
            val lp =
                binding.captureQuestionPhotoText.layoutParams as ConstraintLayout.LayoutParams
            lp.width = Utils.convertDpToPixel(148F).toInt()
            lp.height = Utils.convertDpToPixel(40F).toInt()
            binding.captureQuestionPhotoText.layoutParams = lp
            binding.captureQuestionPhotoText.loadImage(
                packageStatusEntity.subscription?.imageUrl.orEmpty(),
                R.drawable.ic_logo_camera_page,
                R.drawable.ic_logo_camera_page
            )
            binding.captureQuestionPhotoText.setOnClickListener {
                startActivity(DoubtPackageActivity.getStartIntent(this))
                viewModel.publishVipClickEvent(EventConstants.EVENT_NAME_CAMERA_VIP)
            }
        } else {
            if (packageStatusEntity.question?.dialogView == true) {
                shouldProceed = packageStatusEntity.question?.dialogViewInfo?.isCancel
                    ?: false
                showPackageDialog(
                    shouldProceed,
                    packageStatusEntity.question?.dialogViewInfo?.descriptionOne,
                    packageStatusEntity.question?.dialogViewInfo?.descriptionTwo,
                    packageStatusEntity.question?.dialogViewInfo?.packageDesc,
                    packageStatusEntity.subscription?.status
                        ?: false
                )

                if (shouldProceed) {
                    viewModel.publishEventWith(EventConstants.EVENT_NAME_POP_UP_ONE_TIME, "")
                } else {
                    viewModel.publishEventWith(
                        EventConstants.EVENT_NAME_POP_UP_LOCK,
                        "",
                        ignoreSnowplow = true
                    )
                }
            } else {
                shouldProceed = true
            }

            if (packageStatusEntity.question?.alertView == true) {
                isAlertShowing = true
                binding.layoutBottomListView.hide()
                supportFragmentManager.findFragmentByTag(ToolTipFragment.TAG)?.let {
                    supportFragmentManager.beginTransaction().remove(it).commit()
                }
                binding.alertView.show()
                binding.textViewVipExpiring.show()
                binding.textViewVipExpiring.text =
                    packageStatusEntity.question?.alertViewText.orEmpty()
                binding.buttomRenewNow.show()
                binding.buttomRenewNow.text = getString(R.string.buy_doubt_package)
                viewModel.publishEventWith(EventConstants.EVENT_NAME_CAMERA_MESSAGE_LIMIT, "")
            }
        }
        binding.buttomRenewNow.setOnClickListener {
            startActivity(DoubtPackageActivity.getStartIntent(this))
        }
    }

    private fun showPackageDialog(
        isCancelable: Boolean,
        descriptionOne: String?,
        descriptionTwo: String?,
        packageDesc: PackageDesc?,
        subscriptionStatus: Boolean
    ) {
        DoubtPackageDialog.newInstance(
            isDialogCancelable = isCancelable,
            descriptionOne = descriptionOne,
            descriptionTwo = descriptionTwo,
            packageDesc = packageDesc,
            subscriptionStatus = subscriptionStatus
        ).show(supportFragmentManager, DoubtPackageDialog.TAG)
    }

    private fun updateProgressBarState(state: Boolean) {
        if (state) {
            binding.progressBar.bringToFront()
        }
        binding.progressBar.setVisibleState(state)
    }

    private fun onApiError(e: Throwable) {
        showSnackbar(R.string.api_error, R.string.retry, Snackbar.LENGTH_INDEFINITE, "") {
            viewModel.getPackageStatus()
        }
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this)) {
            showSnackbar(
                R.string.somethingWentWrong,
                R.string.retry,
                Snackbar.LENGTH_INDEFINITE,
                ""
            ) {
                viewModel.getPackageStatus()
            }
        } else {
            showSnackbar(
                R.string.string_noInternetConnection,
                R.string.retry,
                Snackbar.LENGTH_INDEFINITE,
                ""
            ) {
                viewModel.getPackageStatus()
            }
        }
    }

    private fun isDoubtFeedAvailable(): Boolean {
        return FeaturesManager.isFeatureEnabled(this, Features.DOUBT_FEED) &&
                userPreference.isDoubtFeedAvailable()
    }
}
