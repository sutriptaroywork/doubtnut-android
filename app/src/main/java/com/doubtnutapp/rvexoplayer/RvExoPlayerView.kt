package com.doubtnutapp.rvexoplayer

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ui.PlayerView

/**
 * @author Sachin Saxena
 * RvExoPlayerView is view used to place in recyclerview item.
 */
class RvExoPlayerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    companion object {
        const val ID = 0x11203
    }

    var uniqueViewHolderId: String = ""
    var url: String? = ""
    var mediaType: String = MEDIA_TYPE_BLOB
    var drmScheme: String? = null
    var drmLicenseUrl: String? = null
    var currentPosition: Long? = 0L
    var continueWatching: Boolean = true

    var canResumePlayer: Boolean = true
        set(value) {
            field = value
            (playerView?.tag as? RecyclerViewExoPlayerHelper)?.canResumePlayer = value
        }

    var isMute: Boolean = false
        set(value) {
            field = value
            (playerView?.tag as? RecyclerViewExoPlayerHelper)?.isMute = value
        }

    var playerView: PlayerView? = null
    var imaAdTagUrl: String? = ""
    var imaAdMediaLoadTimeOut: Int = ExoPlayerHelper.DEFAULT_IMA_AD_MEDIA_LOAD_TIME_OUT_IN_MS

    fun addPlayer(playerView: PlayerView) {
        if (this.playerView == null) {
            this.playerView = playerView
            addView(playerView)
        }
    }

    fun removePlayer() {
        if (playerView != null) {
            playerView?.player?.stop()
            if (playerView?.parent != null) {
                removeView(playerView)
            }
            playerView = null
            listener?.onStop()
        }
    }

    fun stopPlayer() {
        if (playerView != null) {
            playerView?.player?.stop()
            removeView(playerView)
            listener?.onPause()
        }
    }

    override fun removeView(view: View?) {
        try {
            view?.parent ?: return
            super.removeView(view)
        } catch (e: Exception) {
        }
        if (view is PlayerView) {
            playerView = null
        }
    }

    var listener: Listener? = null

    interface Listener {
        fun onPlayerReady() {}
        fun onStart() {}
        fun onPause() {}
        fun onStop() {}
        fun onProgress(positionMs: Long) {}
        fun onError(error: ExoPlaybackException?) {}
        fun onBuffering(isBuffering: Boolean) {}
        fun onToggleControllerVisible(isVisible: Boolean) {}
        fun setVideoEngagementStatusListener(videoEngagementStats: ExoPlayerHelper.VideoEngagementStats) {}
    }
}