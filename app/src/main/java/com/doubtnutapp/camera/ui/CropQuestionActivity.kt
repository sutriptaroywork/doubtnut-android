package com.doubtnutapp.camera.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.TypedValue
import android.view.OrientationEventListener
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toRectF
import androidx.core.os.bundleOf
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.databinding.ActivityCropQuestionBinding
import com.doubtnutapp.matchquestion.ui.fragment.bottomsheet.MatchBottomSheetFragment
import com.doubtnutapp.matchquestion.viewmodel.CropQuestionViewModel
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.utils.*
import com.doubtnutapp.widgets.viewpagerbottomsheetbehavior.ViewPagerBottomSheetBehavior
import com.facebook.appevents.AppEventsConstants
import com.theartofdev.edmodo.cropper.CropConstant
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageFragment
import com.theartofdev.edmodo.cropper.CropImageView
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import java.io.File
import javax.inject.Inject

class CropQuestionActivity :
    BaseActivity(),
    HasAndroidInjector,
    CropImageFragment.GetCropResultListener,
    MatchBottomSheetFragment.MatchBottomSheetFragmentCallbacks {

    companion object {

        const val TAG = "CropQuestionActivity"
        private const val IMAGE_URI = "IMAGE_URI"
        private const val CURRENT_ORIENTATION = "CURRENT_ORIENTATION"
        private const val CROP_EXP_VARIANT = "CROP_EXP_VARIANT"

        fun getStartIntent(context: Context, imageUri: String?, currentOrientation: Int, cropExpVariant: Int) =
            Intent(context, CropQuestionActivity::class.java).apply {
                putExtra(IMAGE_URI, imageUri)
                putExtra(CURRENT_ORIENTATION, currentOrientation)
                putExtra(CROP_EXP_VARIANT, cropExpVariant)
            }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var networkUtil: NetworkUtil

    private lateinit var eventTracker: Tracker

    private var imageUri: String? = null

    private lateinit var cropQuestionViewModel: CropQuestionViewModel

    private var isTakingPicture: Boolean = false

    private var currentOrientation: Int? = null

    private var isToastShown = false

    private lateinit var binding: ActivityCropQuestionBinding

    // Properties related to question image scroll animation
    private var mBottomSheetBehavior: ViewPagerBottomSheetBehavior<View>? = null
    private val mCropWindowRect: RectF = RectF()
    private var mCropRectBottomToParentTopDist: Float = 0f
    private var mBottomSheetExpandedOffset: Int = 0
    private var mQuestionImageInitialTranslation: Float = 0f
    private var mCropImageView: CropImageView? = null
    private var cropExpVariant: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        requestFullScreen()
        binding = ActivityCropQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        getIntentData()
        cropQuestionViewModel.detectFaceInImage(Uri.parse(imageUri))
        cropQuestionViewModel.getCropScreenConfig(imageUri = Uri.parse(imageUri))
        setUpObservers()
    }

    private fun init() {
        eventTracker = DoubtnutApp.INSTANCE.getEventTracker()
        cropQuestionViewModel = ViewModelProvider(this, viewModelFactory).get(CropQuestionViewModel::class.java)
        cropQuestionViewModel.sendEvent(EventConstants.REACHED_CROP_SCREEN, hashMapOf(), true)
    }

    private fun getIntentData() {
        imageUri = intent.getStringExtra(IMAGE_URI)
        currentOrientation = intent.getIntExtra(CURRENT_ORIENTATION, OrientationEventListener.ORIENTATION_UNKNOWN)
        cropExpVariant = intent.getIntExtra(CROP_EXP_VARIANT, -1)
    }

    private fun setUpObservers() {
        cropQuestionViewModel.cropConfig.observe(
            this,
            EventObserver {
                val cropConfigValue = with(it.first) {
                    binding.tvCropTitle.text = cropScreenTitle
                    bundleOf(
                        CropConstant.INTENT_EXTRA_KEY_TITLE to cropScreenTitle,
                        CropConstant.INTENT_EXTRA_KEY_FIND_SOLUTION_BUTTON_TEXT to this.findSolutionButtonText
                    )
                }

                val imageUri = if (it.second == null) Uri.fromFile(File(it.first.sampleImageUri)) else it.second

                openCropImageFragment(cropConfigValue, imageUri)

                cropQuestionViewModel.setTrickQuestionShownToTrue()
                Handler().postDelayed({
                    isTakingPicture = false
                }, 500)
            }
        )
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private fun openCropImageFragment(cropConfigValue: Bundle, imageUri: Uri?) {
        CropImage.fragment(imageUri).run {
            setBorderLineColor(ContextCompat.getColor(this@CropQuestionActivity, R.color.colorAccent))
            setBorderCornerColor(ContextCompat.getColor(this@CropQuestionActivity, R.color.colorAccent))
            val rect = Rect()
            binding.seeThroughView.getGlobalVisibleRect(rect)
            setGuidelines(CropImageView.Guidelines.ON)
            setMinCropWindowSize(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, Resources.getSystem().displayMetrics).toInt(),
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, Resources.getSystem().displayMetrics).toInt()
            )
            setAutoZoomEnabled(true)
            setCropWindow(rect.toRectF())
            add(this@CropQuestionActivity, cropConfigValue, R.id.fragmentContainer)
        }
    }

    override fun setIntentForResult(intent: Intent?, resultCode: Int) {
        when (resultCode) {
            Activity.RESULT_OK -> openMatchResultScreen(intent)

            Activity.RESULT_CANCELED -> onCancelFromCrop()

            CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> onCropError(intent)
        }
    }

    override fun onCropOverlayMoved(rect: Rect, cropWindowRect: RectF) {
        with(cropWindowRect) {
            mCropWindowRect.set(this)
            setQuestionImageInitialTranslation()
            mBottomSheetExpandedOffset = height().toInt().coerceIn(50.dpToPx(), 100.dpToPx()).also {
                mCropRectBottomToParentTopDist = bottom - binding.layoutMain.top.toFloat() - it - mQuestionImageInitialTranslation
            }
            // Calling here so that it is executed if image rotates as a result of SelectImageDialog.
            keepQuestionImageAboveMatchPage()
        }
    }

    override fun onCropWindowInit(cropWindowRect: RectF) {
        with(cropWindowRect) {
            cropQuestionViewModel.sendEvent(
                EventConstants.CROP_WINDOW_RECT_INIT,
                hashMapOf(
                    "crop_window_rect" to toString(),
                    "left" to left,
                    "top" to top,
                    "right" to right,
                    "bottom" to bottom
                )
            )
        }
    }

    override fun onMatchBottomSheetCreated(bottomSheetBehavior: ViewPagerBottomSheetBehavior<View>) {
        binding.tvCropTitle.hide()
        mBottomSheetBehavior = bottomSheetBehavior
        mBottomSheetBehavior?.apply {
            expandedOffset = mBottomSheetExpandedOffset
            addBottomSheetCallback(object : ViewPagerBottomSheetBehavior.BottomSheetCallback() {
                private var maxCropSlideOffset = 0f
                private var cropSlideOffsetStartValue = 0f
                private var translationOffset = 0f

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == ViewPagerBottomSheetBehavior.STATE_COLLAPSED) {
                        maxCropSlideOffset = 0f
                        keepQuestionImageAboveMatchPage()
                    }
                }

                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    mCropImageView?.setIsBorderTransparent(slideOffset >= MatchBottomSheetFragment.BOTTOM_SHEET_SLIDE_THRESHOLD)

                    if (bottomSheet.top <= mCropWindowRect.bottom.toInt() - mQuestionImageInitialTranslation) {
                        if (maxCropSlideOffset == 0f) {
                            maxCropSlideOffset = 1 - slideOffset
                            cropSlideOffsetStartValue = slideOffset
                            translationOffset = -mQuestionImageInitialTranslation
                        }
                        val cropScreenSlideOffset = (slideOffset - cropSlideOffsetStartValue) / maxCropSlideOffset
                        // Do not translate below the original position of the cropper layout
                        binding.fragmentContainer.translationY = (
                            translationOffset -
                                (mCropRectBottomToParentTopDist * cropScreenSlideOffset)
                            )
                            .coerceAtMost(translationOffset)
                    } else {
                        // Fixes bug where cropScreenSlideOffset does not become 0 inside the above if condition
                        // Because of it, the question image gets displaced from its original position
                        // when the bottom sheet collapses
                        if (maxCropSlideOffset != 0f) {
                            val cropScreenSlideOffset = (slideOffset - cropSlideOffsetStartValue) / maxCropSlideOffset
                            if (cropScreenSlideOffset != 0f) {
                                binding.fragmentContainer.translationY = -mQuestionImageInitialTranslation
                            }
                            // Set it to 0 to prevent executing the above if conditional code again
                            maxCropSlideOffset = 0f
                        }
                    }
                }
            })

            setQuestionImageInitialTranslation()
            // must be executed only once
            mCropRectBottomToParentTopDist -= mQuestionImageInitialTranslation
            keepQuestionImageAboveMatchPage()
        }
    }

    override fun onMatchBottomSheetFragmentClosed() {
        mBottomSheetBehavior = null
        // Undo the effect of subtracting translation value from dist. when fragment was created
        mCropRectBottomToParentTopDist += mQuestionImageInitialTranslation
        translateCropScreen(0f)
        binding.tvCropTitle.show()
    }

    override fun onImageSelected(selectedBitmap: Bitmap, rotationAngle: Int) {
        mCropImageView?.rotateImage(rotationAngle)
    }

    private fun setQuestionImageInitialTranslation() {
        mBottomSheetBehavior?.let {
            val peekHeightFromBottom = binding.layoutMain.height - it.peekHeight
            if (peekHeightFromBottom < mCropWindowRect.bottom) {
                mQuestionImageInitialTranslation = mCropWindowRect.bottom - peekHeightFromBottom
            }
        }
    }

    private fun keepQuestionImageAboveMatchPage() {
        mBottomSheetBehavior?.let {
            val peekHeightFromBottom = binding.layoutMain.height - it.peekHeight
            if (peekHeightFromBottom < mCropWindowRect.bottom) {
                translateCropScreen(-mQuestionImageInitialTranslation)
            }
        }
    }

    private fun translateCropScreen(translationY: Float) {
        binding.fragmentContainer.animate()
            .translationY(translationY)
            .setDuration(100)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    private fun openMatchResultScreen(data: Intent?) {
        val result = CropImage.getActivityResult(data)
        if (cropQuestionViewModel.anyFaceExists) {
            showInvalidImageDialog()
            cropQuestionViewModel.sendEvent(EventConstants.EVENT_SELFIE_IMAGE, hashMapOf("source" to "Gallery"), ignoreSnowplow = true)
        } else {

            // Save Ocr locally if network is not available and show notification when internet will be available.
            processOcrFromImage(result.uri)

            val countToSendEvent: Int = Utils.getCountToSend(RemoteConfigUtils.getEventInfo(), EventConstants.QUESTION_ASK)
            repeat((0 until countToSendEvent).count()) {
                sendEvent(EventConstants.QUESTION_ASK)
                cropQuestionViewModel.sendEvent(AppEventsConstants.EVENT_NAME_SUBMIT_APPLICATION, hashMapOf())
            }

            val cropImageFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as CropImageFragment
            mCropImageView = cropImageFragment.getCropImageView()

            binding.matchBottomSheet.show()
            val matchBottomSheetFragment = MatchBottomSheetFragment.newInstance(
                questionImageUri = result.uri.path.orEmpty(),
                questionText = "",
                source = intent.getStringExtra(Constants.SOURCE).orEmpty(),
                bottomNavigationBottomMargin = mBottomSheetExpandedOffset
            ).apply {
                setMatchBottomSheetCreatedCallback(this@CropQuestionActivity)
            }
            supportFragmentManager.commit(allowStateLoss = true) {
                add(R.id.matchBottomSheet, matchBottomSheetFragment, MatchBottomSheetFragment.TAG)
            }
        }
        val cropWindowAdjusted =
            data?.getBooleanExtra(CropConstant.RESULT_INTENT_EXTRA_KEY_CROP_WINDOW_ADJUSTED, false)
        if (cropWindowAdjusted != null) {
            sendEvent(EventConstants.EVENT_NAME_CROPPER_USED)
        }
        sendEvent(EventConstants.EVENT_NAME_SUBMIT_FROM_CROP)

        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK)
        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK)

        val countToSendEvent: Int =
            Utils.getCountToSend(
                RemoteConfigUtils.getEventInfo(),
                EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK
            )
        repeat((0 until countToSendEvent).count()) {
            sendEvent(EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK)
        }

        cropQuestionViewModel.sendEvent(
            EventConstants.EVENT_NAME_FIND_SOLUTION_CLICK,
            hashMapOf<String, Any>().apply {
                put(EventConstants.TIME_STAMP, System.currentTimeMillis())
                put(
                    EventConstants.ORIENTATION,
                    currentOrientation
                        ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                )
            },
            true
        )

        cropQuestionViewModel.storeQuestionAskCoreAction()
    }

    private fun processOcrFromImage(uri: Uri) {
        if (NetworkUtil(this).isConnected()) return
        cropQuestionViewModel.runTextRecognition(uri)
        cropQuestionViewModel.sendEvent(EventConstants.OCR_FROM_IMAGE_STORED_IN_DB, hashMapOf(), ignoreSnowplow = true)
    }

    private fun onCropError(data: Intent?) {
        val result = CropImage.getActivityResult(data)
        val cropError: Throwable? = result.error
        if (cropError != null) {
            ToastUtils.makeText(this, cropError.message.orEmpty(), Toast.LENGTH_LONG).show()
        } else {
            ToastUtils.makeText(this, "Unexpected error", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onCancelFromCrop() {
        val params = Bundle()
        params.putString("student_id", UserUtil.getStudentId())
        sendEvent(EventConstants.EVENT_NAME_CLOSE_FROM_CROP)
        cropQuestionViewModel.sendEvent(EventConstants.EVENT_NAME_CLOSE_FROM_CROP, hashMapOf())
        finish()
    }

    private fun showInvalidImageDialog() {
        val dialog = InvalidImageDialogFragment.newInstance(TAG)
        dialog.show(supportFragmentManager, CameraActivity.INVALID_IMAGE_DIALOG)
    }

    private fun sendEvent(eventName: String) {
        eventTracker.addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(this).toString())
            .addStudentId(UserUtil.getStudentId())
            .addScreenName(EventConstants.PAGE_MAIN_ASK_PAGE)
            .track()
    }

    override fun onBackPressed() {
        if (isToastShown.not()) {
            toast(R.string.back_press_message)
            isToastShown = !isToastShown
        } else {
            super.onBackPressed()
        }
    }
}
