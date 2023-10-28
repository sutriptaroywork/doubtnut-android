package com.doubtnutapp.rvexoplayer

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.os.CountDownTimer
import android.os.Handler
import android.view.View
import androidx.annotation.FloatRange
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.doubtnutapp.getVisibleItemsCount
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerView
import java.lang.ref.WeakReference

/**
 * @author Sachin Saxena
 * RecyclerViewExoPlayerHelper lightweight utility for playing video using ExoPlayer inside RecyclerView,
 * With this you can set
 * @param id Id of RecyclerViewExoPlayer which is placed inside RecyclerView Item
 * @param playStrategy Used to decide when video will play, this will be value between 0 to 1, if 0.5 set means when view has 50% visibility it will start play. Default is PlayStrategy.DEFAULT i.e. 0.75
 * @param stopStrategy Used to decide when video will stop, this will be value between 0 to 1, if 0.5 set means when view has 50% visibility it will stop playing. Default is PlayStrategy.DEFAULT i.e. 0.0  - when view is completely hidden
 * @param autoPause Used to automatically pause the video at first run. As RecyclerViewExoPlayerHelper is only added when autoPlay is true
 *      thus, to add the player but play only in case of the click, autoPause is used.
 * @param autoPlay Used to device we need to autoplay video or not., Default value is true
 * @param autoPlayInitiation Used to start playing video after this time
 * @param muteStrategy Used to decide whether mute one player affects other player also or not, values may be MuteStrategy.ALL, MuteStrategy.INDIVIDUAL, if individual user need to manage isMute flag with there own
 * @param defaultMute Used to decide whether player is mute by default or not, Default Value is false
 * @param loop Used whether need to play video in looping or not, if 0 then no looping will be there, Default is Int.MAX_VALUE
 * @param progressRequired Used to get media playback update
 * @param progressDelay Time in milli second after which we need progress update
 * @param defaultMinBufferMs Min Time in milli second to buffer
 * @param defaultMaxBufferMs Max Time in milli second to buffer
 * @param reBufferDuration reBuffer duration in milli second
 */
class RecyclerViewExoPlayerHelper(
    mContext: Context,
    private val id: Int,
    private val autoPlay: Boolean = true,
    private var autoPause: Boolean = false,
    private val autoPlayInitiation: Long = 1000L,
    @FloatRange(from = 0.0, to = 1.0) val playStrategy: Float = RvPlayStrategy.DEFAULT,
    @FloatRange(from = 0.0, to = 1.0) val stopStrategy: Float = RvStopStrategy.DEFAULT,
    @RvMuteStrategy.Values val muteStrategy: Int = RvMuteStrategy.ALL,
    private val defaultMute: Boolean = false,
    private val useController: Boolean = false,
    private val loop: Int = Int.MAX_VALUE,
    private val progressRequired: Boolean = false,
    private val progressDelay: Long = 100L,
    private val defaultMinBufferMs: Int = DEFAULT_MIN_BUFFER_MS,
    private val defaultMaxBufferMs: Int = DEFAULT_MAX_BUFFER_MS,
    private val reBufferDuration: Int?
) {
    private val playerView: WeakReference<PlayerView> = WeakReference(PlayerView(mContext))

    val rvPlayerHelper: ExoPlayerHelper
    var rVExoPlayerView: RvExoPlayerView? = null
    var currentlyPlayingId: String? = null

    companion object {
        const val DEFAULT_MIN_BUFFER_MS = DefaultLoadControl.DEFAULT_MIN_BUFFER_MS
        const val DEFAULT_MAX_BUFFER_MS = DefaultLoadControl.DEFAULT_MAX_BUFFER_MS
    }

    var lifecycle: Lifecycle? = null

    var isMute = defaultMute
        set(value) {
            field = value
            if (value) rvPlayerHelper.mute() else rvPlayerHelper.unMute()
        }

    var canResumePlayer = true
        set(value) {
            field = value
            rvPlayerHelper.canResumePlayerOnLifecycleResume = value
        }

    var canPlay = true
        set(value) {
            field = value
            if (value) rvPlayerHelper.resumePlayer() else rvPlayerHelper.pausePlayer()
        }

    init {
        playerView.get()?.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
        playerView.get()?.useController = useController
        playerView.get()?.tag = this

        rvPlayerHelper = ExoPlayerHelper(
            mContext,
            playerView.get()!!,
            defaultMinBufferMs,
            defaultMaxBufferMs,
            reBufferDuration
        )

        rvPlayerHelper.setExoPlayerStateListener(object : ExoPlayerHelper.ExoPlayerStateListener {
            override fun onPlayerStart() {
                canResumePlayer = true
                playerView.get()?.getPlayerParent()?.listener?.onPlayerReady()
                playerView.get()?.getPlayerParent()?.listener?.onStart()

                // Leveraging Kotlin's setter syntax by reassigning the same value in order to
                // play / pause player depending on condition
                canPlay = canPlay

                if (lifecycle?.currentState != Lifecycle.State.RESUMED) {
                    rvPlayerHelper.pausePlayer()
                }
                if (autoPause) {
                    rvPlayerHelper.pausePlayer()
                    autoPause = false
                }
                updateMuteStatus(rVExoPlayerView ?: return, isMute)
            }

            override fun onPlayerError(error: ExoPlaybackException) {
                playerView.get()?.getPlayerParent()?.listener?.onError(error)
            }

            override fun onPlayerPause() {
                currentlyPlayingId = ""
                playerView.get()?.getPlayerParent()?.listener?.onPause()
            }

            override fun onPlayerEnd() {
                playerView.get()?.getPlayerParent()?.listener?.onStop()
                rvPlayerHelper.stop()
            }

            override fun onPlayerBuffering() {
                playerView.get()?.getPlayerParent()?.listener?.onBuffering(true)
            }

        })

        if (progressRequired) {
            rvPlayerHelper.setProgressListener(
                progressDelay,
                object : ExoPlayerHelper.ProgressListener {
                    override fun onProgress(positionMs: Long) {
                        playerView.get()?.getPlayerParent()?.listener?.onProgress(positionMs)
                    }
                })
        }

        rvPlayerHelper.setVideoEngagementStatusListener(object :
            ExoPlayerHelper.VideoEngagementStatusListener {
            override fun registerVideoEngagementStatus(videoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {
                playerView.get()?.getPlayerParent()?.listener?.setVideoEngagementStatusListener(
                    videoEngagementStats
                )
            }
        })

        rvPlayerHelper.playerView.setControllerVisibilityListener {
            playerView.get()
                ?.getPlayerParent()?.listener?.onToggleControllerVisible(it == View.VISIBLE)
        }
    }

    /**
     * Make this helper lifecycle aware so it will stop player when activity goes to background.
     */
    fun makeLifeCycleAware(activity: FragmentActivity) {
        lifecycle = activity.lifecycle
        activity.lifecycle.addObserver(rvPlayerHelper)
    }

    /**
     * Make this helper lifecycle aware so it will stop player when fragment goes to background.
     */
    fun makeLifeCycleAware(lifecycleOwner: LifecycleOwner) {
        lifecycle = lifecycleOwner.lifecycle
        lifecycleOwner.lifecycle.addObserver(rvPlayerHelper)
    }

    private fun getViewRect(view: View): Rect {
        val rect = Rect()
        val offset = Point()
        view.getGlobalVisibleRect(rect, offset)
        return rect
    }

//    private fun visibleAreaOffset(playerView: RvExoPlayerView, parent: View): Float {
//        val videoRect = getViewRect(playerView)
//        val parentRect = getViewRect(parent)
//
//        return if ((parentRect.contains(videoRect) || parentRect.intersect(videoRect))) {
//            val visibleArea = (videoRect.height() * videoRect.width()).toFloat()
//            val viewArea = playerView.width * playerView.height
//            if (viewArea <= 0f) 1f else visibleArea / viewArea
//        } else {
//            0f
//        }
//    }

    /**
     * Return visible area of view
     */
    private fun visibleAreaOffset(parent: View): Float {
        val videoRect = getViewRect(parent)
        val parentRect = getViewRect(parent)

        return if ((parentRect.contains(videoRect) || parentRect.intersect(videoRect))) {
            val visibleArea = (videoRect.height() * videoRect.width()).toFloat()
            val viewArea = parent.width * parent.height
            if (viewArea <= 0f) 1f else visibleArea / viewArea
        } else {
            0f
        }
    }

    fun playCurrent(recyclerView: RecyclerView) {
        onScrollListener.onScrollStateChanged(
            recyclerView = recyclerView,
            newState = RecyclerView.SCROLL_STATE_IDLE
        )
    }

    fun stopCurrent() {
        currentlyPlayingId = ""
        rVExoPlayerView?.stopPlayer()
    }

    val playVideoTimer = PlayVideoTimer(autoPlayInitiation, autoPlayInitiation)

    val stopVideoTimer = StopVideoPlayTimer(100, 100)

    private val onScrollListener = object : RecyclerView.OnScrollListener() {

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            when (newState) {
                RecyclerView.SCROLL_STATE_IDLE -> {
                    playVideoTimer.apply {
                        cancel()
                        attachedRecyclerView = recyclerView
                        start()
                    }
                }

                RecyclerView.SCROLL_STATE_DRAGGING -> {
                    playVideoTimer.cancel()
                    stopVideoTimer.apply {
                        cancel()
                        attachedRecyclerView = recyclerView
                        start()
                    }
                }
            }
        }

    }

    private fun stopVideo(recyclerView: RecyclerView, visibleCount: Int) {
        for (i in 0 until visibleCount) {

            val view = recyclerView.getChildAt(i) ?: continue
            val rVExoPlayer = view.findViewById<View>(id) as? RvExoPlayerView ?: continue

            if (visibleAreaOffset(view) <= stopStrategy && rVExoPlayer.uniqueViewHolderId == currentlyPlayingId) {
                playerView.get()?.getPlayerParent()?.removePlayer()
                currentlyPlayingId = ""
                break
            }
        }
    }

    private fun playVideo(recyclerView: RecyclerView, visibleCount: Int) {
        // Post to message queue so that action is performed after layout
        recyclerView.post {
            for (i in 0 until visibleCount) {
                val view = recyclerView.getChildAt(i) ?: continue
                val rVExoPlayer = view.findViewById<View>(id) as? RvExoPlayerView ?: continue
                if (!isPlayerReadyToPlay(rVExoPlayer)) continue

                if (visibleAreaOffset(view) >= playStrategy) {
                    if (rVExoPlayer.uniqueViewHolderId == currentlyPlayingId) break
                    playerView.get()?.getPlayerParent()?.removePlayer()
                    play(rVExoPlayer)
                    break
                } else {
                    releasePlayer(view)
                }
            }
        }
    }

    private fun isPlayerReadyToPlay(rVExoPlayer: RvExoPlayerView): Boolean {

        if (rVExoPlayer.playerView != null) return false

        if (rVExoPlayer.url.isNullOrBlank() ||
            !rVExoPlayer.continueWatching
        )
            return false

        return true
    }

    private val childAttachHandler = Handler()
    private val childAttachRunnable = object : Runnable {
        var attachedRecyclerView: RecyclerView? = null
        override fun run() {
            if (attachedRecyclerView != null)
                onScrollListener.onScrollStateChanged(
                    recyclerView = attachedRecyclerView ?: return,
                    newState = RecyclerView.SCROLL_STATE_IDLE
                )
        }
    }

    private val onChildAttachStateChangeListener =
        object : RecyclerView.OnChildAttachStateChangeListener {
            var attachedRecyclerView: RecyclerView? = null
            override fun onChildViewDetachedFromWindow(view: View) {
                releasePlayer(view)
            }

            override fun onChildViewAttachedToWindow(view: View) {
                childAttachHandler.removeCallbacks(childAttachRunnable)
                childAttachHandler.postDelayed(childAttachRunnable.apply {
                }, 100)
            }
        }

    /**
     * Used to attach this helper to recycler view. make call to this after setting LayoutManager to your recycler view
     */
    fun attachToRecyclerView(recyclerView: RecyclerView) {
        if (autoPlay) {
            if (recyclerView.layoutManager != null) {
                recyclerView.removeOnScrollListener(onScrollListener)
                recyclerView.removeOnChildAttachStateChangeListener(onChildAttachStateChangeListener)
                recyclerView.addOnScrollListener(onScrollListener)
                recyclerView.addOnChildAttachStateChangeListener(onChildAttachStateChangeListener.apply {
                    attachedRecyclerView = recyclerView
                })
            } else {
                throw(RuntimeException("call attachToRecyclerView() after setting RecyclerView.layoutManager"))
            }
        }
    }

    fun detachFromRecyclerView(recyclerView: RecyclerView) {
        recyclerView.removeOnScrollListener(onScrollListener)
        recyclerView.removeOnChildAttachStateChangeListener(onChildAttachStateChangeListener)
    }

    /**
     * add PlayerView and attach video url to it
     * @param rVExoPlayer - view attached to Recyclerview ViewHolder
     */
    private fun play(rVExoPlayer: RvExoPlayerView) {

        rVExoPlayer.apply {
            addPlayer(this@RecyclerViewExoPlayerHelper.playerView.get() ?: return)

            rVExoPlayerView = this
            currentlyPlayingId = uniqueViewHolderId
            rvPlayerHelper.setMediaData(
                mediaType = mediaType,
                drmScheme = drmScheme, drmLicenseUrl = drmLicenseUrl,
                useFallback = false
            )
            rvPlayerHelper.setVideoUrl(url ?: "")
            if (imaAdTagUrl.isNotNullAndNotEmpty()) {
                rvPlayerHelper.setImaAdTagUrl(imaAdTagUrl ?: "")
            }
            if (imaAdMediaLoadTimeOut > 0) {
                rvPlayerHelper.setImaAdMediaLoadTimeout(imaAdMediaLoadTimeOut)
            }
            rvPlayerHelper.setFallbackUrl(url ?: "")
            rvPlayerHelper.setPlayerCurrentPosition(currentPosition ?: 0)
            rvPlayerHelper.resetEngagementTime()
            rvPlayerHelper.preparePlayer()
            updateMuteStatus(this, this@RecyclerViewExoPlayerHelper.isMute)
        }
    }

    private fun releasePlayer(view: View) {

        val rVExoPlayer = view.findViewById<View>(id) as? RvExoPlayerView ?: return

        if (rVExoPlayer.playerView == null) return

        rVExoPlayer.removePlayer()
    }

    private fun updateMuteStatus(rvExoPlayer: RvExoPlayerView, isMute: Boolean) {
        when (muteStrategy) {
            RvMuteStrategy.ALL -> {
                rvExoPlayer.isMute = isMute
            }
            RvMuteStrategy.INDIVIDUAL -> {
                /* no-op */
            }
        }
    }

    private fun PlayerView.getPlayerParent(): RvExoPlayerView? = parent as? RvExoPlayerView

    fun getPlayerView(): PlayerView? = playerView.get()

    inner class PlayVideoTimer(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {

        var attachedRecyclerView: RecyclerView? = null

        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            val visibleItemCount = attachedRecyclerView?.getVisibleItemsCount() ?: 0

            if (attachedRecyclerView == null || visibleItemCount <= 0) return

            if (lifecycle?.currentState != Lifecycle.State.RESUMED
                && lifecycle?.currentState != Lifecycle.State.CREATED
            ) return

            playVideo(attachedRecyclerView ?: return, visibleItemCount)
        }
    }

    inner class StopVideoPlayTimer(millisInFuture: Long, countDownInterval: Long) :
        CountDownTimer(millisInFuture, countDownInterval) {

        var attachedRecyclerView: RecyclerView? = null

        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            val visibleItemCount = attachedRecyclerView?.getVisibleItemsCount() ?: 0

            if (attachedRecyclerView == null || visibleItemCount <= 0) return

            stopVideo(attachedRecyclerView ?: return, visibleItemCount)
        }
    }
}