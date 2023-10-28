package com.doubtnutapp.ui.cameraGuide

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.WebSettings
import android.widget.ImageView
import androidx.lifecycle.Observer
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.databinding.ActivityCameraGuideBinding
import com.doubtnutapp.gamification.settings.profilesetting.ui.ProfileSettingActivity
import com.doubtnutapp.screennavigator.CameraScreen
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.browser.ProgressWebViewClient
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_HLS
import com.doubtnutapp.utils.EmptyCallback
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import dagger.android.AndroidInjector
import javax.inject.Inject


class CameraGuideActivity : BaseBindingActivity<CameraGuideViewModel, ActivityCameraGuideBinding>(),
    ExoPlayerHelper.VideoEngagementStatusListener, ExoPlayerHelper.ExoPlayerStateListener {


    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector


    @Inject
    lateinit var screenNavigator: Navigator

    private lateinit var studentId: String
    private lateinit var eventTracker: Tracker

    private var handler: Handler? = null


    private var animBlink: Animation? = null


    private lateinit var onBoardingVideoURL: String
    private lateinit var onBoardingVideoId: String

    private var player: SimpleExoPlayer? = null
    private var componentListener: ComponentListener? = null


    private var playWhenReady: Boolean = true
    private var playerCurrentPosition: Long = 0
    private var engageTime: Long = 0
    private var maxSeekTime: Long = 0
    private var runningTimeInMillis: Long = 0
    private var playerCurrentWindow: Int = 0
    private lateinit var exoPlayerHelper: ExoPlayerHelper
    override fun provideViewBinding(): ActivityCameraGuideBinding =
        ActivityCameraGuideBinding.inflate(layoutInflater)

    private val TAG = "CameraGuideActivity"

    override fun providePageName(): String = TAG

    override fun provideViewModel(): CameraGuideViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        exoPlayerHelper = ExoPlayerHelper(this, binding.playerViewCameraGuide)
        lifecycle.addObserver(exoPlayerHelper)
        eventTracker = getTracker()
        studentId = getStudentId() ?: ""
        animBlink = AnimationUtils.loadAnimation(
            this,
            R.anim.blink
        )
        handler = Handler(Looper.getMainLooper())

        setUpWebView()

        getCameraGuide()
        init()



        binding.ivCloseGuide.setOnClickListener {
            onBackPressed()
            sendEvent(EventConstants.EVENT_NAME_CLOSE_CAMERA_GUIDE_PAGE)
        }

        binding.askDoubt.setOnClickListener {
            viewModel.publishCameraButtonClickEvent("Demo")
            if (intent?.extras?.containsKey(ProfileSettingActivity.INTENT_EXTRA_SOURCE) == true && intent.getStringExtra(
                    ProfileSettingActivity.INTENT_EXTRA_SOURCE
                ) == ProfileSettingActivity.TAG
            ) {
                screenNavigator.startActivityFromActivity(
                    this,
                    CameraScreen,
                    hashMapOf<String, String>().apply {
                        put(ProfileSettingActivity.INTENT_EXTRA_SOURCE, ProfileSettingActivity.TAG)
                    }.toBundle()
                )
            } else {
                onBackPressed()
            }
            sendEvent(EventConstants.EVENT_NAME_ASK_DOUBT_FROM_CAMERA_GUIDE)
        }
    }

    private fun setUpWebView() {
        binding.apply {
            webViewGuide.settings.javaScriptEnabled = true
            webViewGuide.settings.domStorageEnabled = true
            webViewGuide.settings.databaseEnabled = true
            webViewGuide.settings.setAppCacheEnabled(false)
            if (Build.VERSION.SDK_INT >= 21) {
                webViewGuide.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            }
            webViewGuide.clearCache(true)
            val webViewClient = ProgressWebViewClient(progressBarWebview)
            webViewGuide.webViewClient = webViewClient
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun getCameraGuide() {

        viewModel.cameraGuide().observe(this, Observer { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarWebview.show()
                }
                is Outcome.Failure -> {
                    binding.progressBarWebview.hide()
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    sendEvent(EventConstants.EVENT_NAME_GET_CAMERA_GUIDE_FAILURE)
                }
                is Outcome.ApiError -> {
                    binding.progressBarWebview.hide()
                    apiErrorToast(response.e)
                    sendEvent(EventConstants.EVENT_NAME_GET_CAMERA_GUIDE_API_ERROR)

                }
                is Outcome.BadRequest -> {
                    binding.progressBarWebview.hide()
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                    sendEvent(EventConstants.EVENT_NAME_GET_CAMERA_GUIDE_BAD_REQUEST)

                }
                is Outcome.Success -> {
                    binding.progressBarWebview.hide()
                    binding.webViewGuide.loadDataWithBaseURL(
                        null, response.data.data.toString(),
                        "text/HTML", "UTF-8", null
                    )
                    sendEvent(EventConstants.EVENT_NAME_GET_CAMERA_GUIDE_SUCCESS)


                }
            }
        })
    }


    private fun releasePlayer() {
        if (player != null) {
            playerCurrentPosition = player!!.currentPosition
            playerCurrentWindow = player!!.currentWindowIndex
            playWhenReady = player!!.playWhenReady
            if (componentListener != null) {
                player!!.removeListener(componentListener!!)
            }
            player!!.release()
            player = null
        }
    }


    private fun startTrackingEngagmentTime() {
        runningTimeInMillis = System.currentTimeMillis()
    }

    private fun pauseTrackingEngagmentTime() {

        if (runningTimeInMillis == 0L) return //tracking has not started yet

        engageTime += (System.currentTimeMillis() - runningTimeInMillis)
        runningTimeInMillis = 0
    }


    override fun registerVideoEngagementStatus(videoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {
        sendWatchData(videoEngagementStats)
    }


    fun sendWatchData(userVideoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {

        val engagementTime = userVideoEngagementStats.engagementTime
        val maxSeekTime = userVideoEngagementStats.maxSeekTime

        viewModel.updateAnswerView(onBoardingVideoId,
                maxSeekTime.toString(), Constants.VIRAL_PAGE, engagementTime.toString(), Constants.VIRAL_VIDEO_SOURCE
        ).enqueue(EmptyCallback<ApiResponse<Any>>())
    }

    private inner class ComponentListener : Player.EventListener {

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            when (playbackState) {
                Player.STATE_BUFFERING -> {
                    binding.progressBarExoPlayerBuffering.visibility = View.VISIBLE
                    //Pause tracking because at this point of time player is not playing a video.
                    if (playWhenReady) pauseTrackingEngagmentTime()
                }
                Player.STATE_READY -> {
                    binding.progressBarExoPlayerBuffering.visibility = View.GONE
                    when (playWhenReady) {
                        true -> {
                            startTrackingEngagmentTime()
                        }
                        false -> {
                            pauseTrackingEngagmentTime()
                        }
                    }
                }
                Player.STATE_ENDED -> binding.progressBarExoPlayerBuffering.visibility = View.GONE
            }
        }

        override fun onSeekProcessed() {
            val currentPosition = player?.currentPosition ?: 0
            if (currentPosition >= maxSeekTime) {
                maxSeekTime = currentPosition
            }
        }
    }


    private fun init() {

        onBoardingVideoId =
            defaultPrefs(this!!).getString(Constants.TYPE_INTRO_QID, "").orDefaultValue()
        onBoardingVideoURL =
            defaultPrefs(this!!).getString(Constants.TYPE_INTRO_URL, "").orDefaultValue()
        binding.playerViewCameraGuide.player = player
        binding.playerViewCameraGuide.findViewById<ImageView>(R.id.exo_fullscreen).visibility =
            View.GONE
        exoPlayerHelper.setExoPlayerStateListener(this)
        exoPlayerHelper.setVideoEngagementStatusListener(this)
        exoPlayerHelper.setMediaData(MEDIA_TYPE_HLS)
        exoPlayerHelper.setVideoUrl(onBoardingVideoURL)
        exoPlayerHelper.setFallbackUrl(onBoardingVideoURL)
        exoPlayerHelper.setHlsTimeoutTime(0)
        exoPlayerHelper.startPlayingVideo()


    }


    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    override fun onStop() {
        super.onStop()
        exoPlayerHelper.unSetListeners()
        releasePlayer()
    }

    override fun onPlayerEnd() {
        binding.progressBarExoPlayerBuffering.hide()
        sendEvent(EventConstants.EVENT_NAME_VIDEO_PLAY_END)
    }

    override fun onPlayerStart() {
        binding.progressBarExoPlayerBuffering.hide()
        sendEvent(EventConstants.EVENT_NAME_VIDEO_PLAY_START)

    }

    override fun onPlayerPause() {
        binding.progressBarExoPlayerBuffering.hide()
        sendEvent(EventConstants.EVENT_NAME_VIDEO_PLAY_PAUSE)

    }

    override fun onPlayerBuffering() {
        binding.progressBarExoPlayerBuffering.show()
        sendEvent(EventConstants.EVENT_NAME_VIDEO_PLAY_BUFFERING)

    }

    private fun getTracker(): com.doubtnut.analytics.Tracker {
        val doubtnutApp = this@CameraGuideActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEvent(eventName: String) {
        this@CameraGuideActivity.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@CameraGuideActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_CAMERA_GUIDE_ACTIVITY)
                .track()
        }
    }


}