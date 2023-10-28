package com.doubtnutapp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.doubtnutapp.utils.KeyboardUtils
import com.doubtnutapp.video.VideoFragment
import com.doubtnutapp.video.VideoFragmentListener
import com.github.nisrulz.sensey.OrientationDetector
import com.github.nisrulz.sensey.Sensey
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_video_test.*
import kotlinx.android.synthetic.main.dialog_play_video.*

/**
 * Created by Anand Gaurav on 26/03/20.
 */
class VideoTestActivity : AppCompatActivity(), OrientationDetector.OrientationListener, VideoFragmentListener {

    private val handler: Handler = Handler()
    private val ROTAION_DELAY: Long = 500

    private var userSelectedState: Int = 90
    private var isFullScreen = false

    private var videoFragment: VideoFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        statusbarColor(this, R.color.grey_statusbar_color)
        setContentView(R.layout.activity_video_test)
        imageViewPLay.setOnClickListener {
            showDialog()
        }
    }

    private fun showDialog() {
        Dialog(this).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.dialog_play_video)
            window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#cc000000")))
            window?.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            show()
            setCancelable(true)
            dialogParentView.setOnClickListener {
                KeyboardUtils.hideKeyboard(currentFocus ?: View(context))
                dismiss()
            }
            buttonSubmit.setOnClickListener {
                if (editTextUrl.text.toString().isNullOrBlank()){
                    return@setOnClickListener
                }
                playVideo(editTextUrl.text.toString(), "DASH", editTextLicense.text.toString())
                dismiss()
            }
            imageViewClose.setOnClickListener {
                KeyboardUtils.hideKeyboard(currentFocus ?: View(context))
                dismiss()
            }


            setOnDismissListener {
                KeyboardUtils.hideKeyboard(currentFocus ?: View(context))
            }
        }
    }

    private fun playVideo(
            videoUrl: String,
            mediaType: String,
            drmLicenseUrl: String, hlsTimeoutTime: Long = 0) {
        val videoData = VideoFragment.Companion.VideoData(questionId = "",
                videoUrl = videoUrl,
                fallbackVideoUrl = videoUrl,
                hlsTimeoutTime = hlsTimeoutTime,
                aspectRatio = "16:9",
                autoPlay = false,
                show_fullscreen = true,
                page = "",
                isPlayFromBackStack = false,
                mediaType = mediaType,
                drmScheme = "widevine",
                drmLicenseUrl = drmLicenseUrl,
                useFallBack = false,
                lockUnlockLogs = ""
        )
        videoFragment = VideoFragment.newInstance(videoData, null,this)
        supportFragmentManager.beginTransaction().replace(R.id.videoContainer, videoFragment!!).commit()
    }


    override fun onVideoCompleted() {

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
            }, ROTAION_DELAY)
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
            }, ROTAION_DELAY)
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
            }, ROTAION_DELAY)
        }
    }

    private fun updateFullScreenUI() {
        hideStatusBar()
        videoFragment?.enterFullscreen()
    }

    private fun updatePortraitScreenUI() {
        showStatusBar()
        videoFragment?.exitFullscreen()
    }

    private fun hideStatusBar() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN xor View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
    }

    private fun showStatusBar() {
        val decorView = window.decorView
        val uiOptions = View.SYSTEM_UI_FLAG_VISIBLE
        decorView.systemUiVisibility = uiOptions
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            updatePortraitScreenUI()
        } else {
            updateFullScreenUI()
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

    }

    override fun singleTapOnPlayerView() {
        if (videoFragment != null) {
            if (videoFragment?.isPlayerControllerVisible() == true) {
                videoFragment?.hidePlayerController()
            } else {
                videoFragment?.showPlayerController()
            }
        }
    }

}