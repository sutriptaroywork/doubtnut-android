package com.doubtnutapp.videoscreen

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.core.view.doOnLayout
import androidx.lifecycle.Lifecycle
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.TapBackwardEvent

import com.doubtnutapp.EventBus.TapForwardEvent

import com.doubtnutapp.databinding.FragmentYoutubeBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.video.VideoFragmentListener
import com.doubtnutapp.youtubeVideoPage.FullScreenHelper
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerFullScreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * Created by Anand Gaurav on 2019-12-10.
 */
class YoutubeFragment : BaseBindingFragment<DummyViewModel, FragmentYoutubeBinding>(),
    ActionPerformer {

    companion object {
        private const val FEATURE_TYPE = "video"
        private const val TAG = "YoutubeFragment"

        private const val UNSTARTED = "UNSTARTED"
        private const val ENDED = "ENDED"
        private const val PLAYING = "PLAYING"
        private const val PAUSED = "PAUSED"
        private const val BUFFERING = "BUFFERING"
        private const val YOUTUBE_ID = "YOUTUBE_ID"
        private const val TIME_MOVE_FORWARD_BACKWARD = 10

        fun newInstance(
            id: String,
            startSeconds: Float,
            videoFragmentListener: VideoFragmentListener,
            youtubeFragmentListener: YoutubeFragmentListener,
            shouldShowEngagementTime: Boolean? = true,
            shouldShowFullScreen: Boolean
        ): YoutubeFragment {
            return YoutubeFragment().apply {
                arguments = bundleOf(YOUTUBE_ID to id)
                this.startSeconds = startSeconds
                this.videoFragmentListener = videoFragmentListener
                this.listener = youtubeFragmentListener
                this.shouldShowFullScreen = shouldShowFullScreen
                this.showEngamentTime = shouldShowEngagementTime
            }
        }
    }

    private var runningTimeInMillis: Long = 0

    private lateinit var tracker: YouTubePlayerTracker

    private lateinit var myYouTubePlayer: YouTubePlayer

    private var fullScreenHelper: FullScreenHelper? = null
    private var videoViewHeight: Int = -1
    private var engageTime: Long = 0

    private var isFullScreen = false
    private var shouldShowFullScreen = true
    private var startSeconds: Float = 0f
    private var showEngamentTime: Boolean? = true;

    private var youtubeVideoId: String = ""

    private var listener: YoutubeFragmentListener? = null

    private var videoFragmentListener: VideoFragmentListener? = null

    override fun providePageName(): String = TAG

    private var currentSecond: Float = 0f

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentYoutubeBinding =
        FragmentYoutubeBinding.inflate(layoutInflater)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        youtubeVideoId = arguments?.getString(YOUTUBE_ID) ?: ""
        if (youtubeVideoId.isNotBlank()) {
            initViews()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            fullScreenHelper = FullScreenHelper(it)
        }
    }

    private fun initViews() {
        tracker = YouTubePlayerTracker()
        initYouTubePlayerView()
        initVideoRotation()

        mBinding?.ivBackFromVideo?.setOnClickListener {
            activity?.onBackPressed()
        }

        mBinding?.youTubePlayerView?.setOnClickListener {
            myYouTubePlayer.seekTo(currentSecond + 10)
        }


        DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe {
            if (it is TapForwardEvent) {
                myYouTubePlayer.seekTo(currentSecond + TIME_MOVE_FORWARD_BACKWARD)
            } else if (it is TapBackwardEvent) {
                if (currentSecond > TIME_MOVE_FORWARD_BACKWARD) {
                    myYouTubePlayer.seekTo(currentSecond - TIME_MOVE_FORWARD_BACKWARD)
                } else {
                    myYouTubePlayer.seekTo(0F)
                }
            }
        }

        setDimensions()
    }

    fun handleFullScreenOnBack() {
        if (isFullScreen) {
            updatePortraitScreenUI()
        }
    }

    fun getCurrentSecond() =
        tracker.currentSecond

    fun getVideoDuration() =
        tracker.videoDuration

    //Returns engagement time in millis, without pausing the engagement time tracking
    fun getCurrentEngagementTime() =
        if (runningTimeInMillis == 0L) {
            engageTime
        } else {
            engageTime + (System.currentTimeMillis() - runningTimeInMillis)
        }

    private fun initVideoRotation() {
        mBinding?.videoContainer?.doOnLayout {
            videoViewHeight = it.height
        }
    }

    override fun performAction(action: Any) {
//        viewModel.handleAction(action)
    }

    private fun showToast(@StringRes resId: Int) {
        showToastMessage(getString(resId))
    }

    private fun showToastMessage(message: String) {
        toast(message)
    }

    override fun onStop() {
        sendWatchData()
        super.onStop()
    }

    private fun sendWatchData() {
        try {
            if (::tracker.isInitialized) {
                val maxSeekTime = tracker.currentSecond.roundToInt()
                var engagementTime = getEngagementTime().toDouble().roundToInt()
                if (showEngamentTime != null && showEngamentTime == false) {
                    engagementTime = 0
                }
                listener?.updateYoutubeEngagementTime(
                    maxSeekTime.toString(),
                    engagementTime.toString()
                )
            }
        } catch (e: Exception) {

        }
    }

    private fun initYouTubePlayerView() {
        mBinding?.youTubePlayerView?.let { lifecycle.addObserver(it) }
        mBinding?.youTubePlayerView?.getPlayerUiController()
            ?.showFullscreenButton(shouldShowFullScreen)
            ?.showYouTubeButton(false)
        mBinding?.youTubePlayerView?.addYouTubePlayerListener(object :
            AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadOrCueVideo(
                    lifecycle,
                    youtubeVideoId,
                    startSeconds
                )
                youTubePlayer.addListener(tracker)
                addFullScreenListenerToPlayer()
                getPlayerInstance(youTubePlayer)
                startTrackingEngagementTime()
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                currentSecond = second
            }

            override fun onStateChange(
                youTubePlayer: YouTubePlayer,
                state: PlayerConstants.PlayerState
            ) {
                super.onStateChange(youTubePlayer, state)
                onNewState(state)
                if (lifecycle.currentState == Lifecycle.State.RESUMED && state == PlayerConstants.PlayerState.PAUSED) {
                    listener?.onYoutubeVideoPauseInResumedState()
                }
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                listener?.onYoutubeVideoPlayFailed(youtubeVideoId, error)
            }
        })
    }

    private fun onNewState(state: PlayerConstants.PlayerState) {
        when (Utils.playerStateToString(state)) {
            PLAYING -> {
                startTrackingEngagementTime()
                listener?.onYoutubePlayerStarted()
            }
            PAUSED -> {
                pauseTrackingEngagementTime()
            }
            UNSTARTED -> {
            }
            BUFFERING -> {
                pauseTrackingEngagementTime()
            }
            ENDED -> {
                listener?.onYoutubePlayerEnd()
                pauseTrackingEngagementTime()
            }
        }
    }

    private fun startTrackingEngagementTime() {
        runningTimeInMillis = System.currentTimeMillis()
    }

    private fun pauseTrackingEngagementTime() {

        if (runningTimeInMillis == 0L) return //tracking has not started yet

        engageTime += (System.currentTimeMillis() - runningTimeInMillis)
        runningTimeInMillis = 0
    }

    private fun getEngagementTime(): Long {
        pauseTrackingEngagementTime()
        return TimeUnit.MILLISECONDS.toSeconds(engageTime)
    }

    private fun getPlayerInstance(youTubePlayer: YouTubePlayer) {
        this.myYouTubePlayer = youTubePlayer
    }

    private fun addFullScreenListenerToPlayer() {
        mBinding?.youTubePlayerView?.addFullScreenListener(object :
            YouTubePlayerFullScreenListener {
            override fun onYouTubePlayerEnterFullScreen() {
                updateFullScreenUI()
            }

            override fun onYouTubePlayerExitFullScreen() {
                updatePortraitScreenUI()
            }
        })
    }

    private fun getScreenWidth(): Int {
        val displayMetrics = DisplayMetrics()
        activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (newConfig.orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            updatePortraitScreenUI()
        } else {
            updateFullScreenUI()
        }
    }

    private fun updateFullScreenUI() {
        if (fullScreenHelper != null) {
            isFullScreen = true
            videoFragmentListener?.onFullscreenRequested()
            fullScreenHelper?.enterFullScreen()
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
            setDimensions()
        }
    }

    private fun updatePortraitScreenUI() {
        if (fullScreenHelper != null) {
            isFullScreen = false
            videoFragmentListener?.onPortraitRequested()
            fullScreenHelper?.exitFullScreen()
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
            setDimensions()
        }
    }

    private fun setDimensions() {
        if (activity != null) {
            val displayMetrics = DisplayMetrics()
            activity?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
            val height = displayMetrics.heightPixels
            val width = displayMetrics.widthPixels
            if (activity?.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                mBinding?.videoContainer?.layoutParams?.height = height
                mBinding?.videoContainer?.layoutParams?.width = width
            } else {
                mBinding?.videoContainer?.layoutParams?.height = width * 9 / 16
                mBinding?.videoContainer?.layoutParams?.width = width
            }
        }
    }

}