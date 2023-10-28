package com.doubtnut.core.view.mediaplayer

import android.media.MediaPlayer
import android.media.MediaPlayer.SEEK_PREVIOUS_SYNC
import android.os.Build
import androidx.lifecycle.Lifecycle
import com.doubtnut.core.view.ViewActionHandler
import com.doubtnut.core.view.audiotooltipview.ViewLifecycleObserver
import java.io.IOException

/**
Created by Sachin Saxena on 14/07/22.
 */
class MediaPlayerManager(
    private val mediaPlayerVolumeStateListener: MediaPlayerVolumeStateListener? = null,
    private val mediaPlayerStateListener: MediaPlayerStateListener? = null,
    private val mediaPlayerErrorListener: MediaPlayerErrorListener? = null,
) : AbstractMediaPlayerManager(), ViewActionHandler {

    companion object {
        private const val MIN_VOLUME = 0.0F
        private const val MAX_VOLUME = 1.0F
    }

    private val audioTooltipLifecycleObserver by lazy {
        ViewLifecycleObserver()
    }

    init {
        audioTooltipLifecycleObserver.registerActionHandler(this)
    }

    // media url to play
    private var mediaUrl: String? = null
        set(value) {
            field = value
            releaseMediaPlayer()
            playMediaPlayer()
        }

    /**
     * represent current state of media player volume (mute/unmute)
     * @see MediaPlayerVolumeState
     */
    private var mediaPlayerVolumeState = MediaPlayerVolumeState.INVALID

    private var mediaPlayer: MediaPlayer? = null

    // current position of playing url, set when onStop() lifecycle method will be called
    // this variable is use when onStart() is called after onStop() to set current seek position
    private var lastSeekPosition: Long? = 0L

    // tells whether playback has been completed or not
    private var isMediaPlaybackCompleted = false

    // tells whether onStop() lifecycle method has been called or not
    private var hasScreenStopped = false

    // represent whether media player is mute or not
    private var isMute: Boolean = false
        set(value) {
            when (value) {
                true -> {
                    mediaPlayerVolumeState = MediaPlayerVolumeState.MUTED
                    mediaPlayerVolumeStateListener?.onVolumeChange(MediaPlayerVolumeState.MUTED)
                }
                false -> {
                    mediaPlayerVolumeState = MediaPlayerVolumeState.UNMUTED
                    mediaPlayerVolumeStateListener?.onVolumeChange(MediaPlayerVolumeState.UNMUTED)
                }
            }
            field = value
        }

    /**
     * represent current state of media player
     * @see MediaPlayerState
     */
    private var mediaPlayerState: MediaPlayerState = MediaPlayerState.UNINITIALIZED
        set(value) {
            mediaPlayerStateListener?.onStateChange(value)
            field = value
        }

    private val onPreparedListener = MediaPlayer.OnPreparedListener {
        mediaPlayerState = MediaPlayerState.PREPARED
        startPlaying()
    }

    private val onCompletionListener = MediaPlayer.OnCompletionListener {
        isMediaPlaybackCompleted = true
        mediaPlayerState = MediaPlayerState.COMPLETED
    }

    private val onErrorListener = MediaPlayer.OnErrorListener { _, what, extra ->
        mediaPlayerErrorListener?.onError(what.toString(), extra.toString())
        return@OnErrorListener true
    }

    private val onBufferingUpdateListener = MediaPlayer.OnBufferingUpdateListener { _, _ ->
        // ToDo - update buffer percentage or track buffering at user end
    }

    override fun registerLifecycle(lifecycle: Lifecycle) {
        audioTooltipLifecycleObserver.registerLifecycle(lifecycle)
    }

    override fun setMedia(url: String) {
        mediaUrl = url
    }

    override fun getMedia(): String? = mediaUrl

    // region start - Media player methods
    override fun playMedia() = playMediaPlayer()

    override fun onResume() = resumeMediaPlayer()

    override fun pauseMedia() = pauseMediaPlayer()

    override fun stopMedia() = releaseMediaPlayer()

    override fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    override fun setVolume(leftVolume: Float, rightVolume: Float) {
        mediaPlayer?.setVolume(leftVolume, rightVolume)
    }

    override fun toggleVolume() {
        if (isMute) {
            unMutePlayer()
        } else {
            mutePlayer()
        }
    }

    override fun mutePlayer() {
        setVolume(MIN_VOLUME, MIN_VOLUME)
        isMute = true
    }

    override fun unMutePlayer() {
        setVolume(MAX_VOLUME, MAX_VOLUME)
        isMute = false
    }

    override fun isPlayerMuted(): Boolean = isMute

    override fun getPlayerVolumeState(): MediaPlayerVolumeState = mediaPlayerVolumeState

    override fun getMediaPlayerState(): MediaPlayerState = mediaPlayerState
    // region end - Media player methods

    // region start - view lifecycle methods
    override fun onStart() {
        if (hasScreenStopped && isMediaPlaybackCompleted.not()) {
            playMediaPlayer()
        }
    }

    override fun onPause() = pauseMediaPlayer()

    override fun onStop() {
        hasScreenStopped = true
        lastSeekPosition = mediaPlayer?.currentPosition?.toLong()
        releaseMediaPlayer()
    }
    // region end - view lifecycle methods

    // region start - private methods
    private fun initializeMediaPlayer() {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer()
            mediaPlayerState = MediaPlayerState.IDLE
        }
    }

    private fun prepareMediaPlayer() {
        mediaPlayer?.apply {
            // set listeners
            setUpListeners()
            try {
                setDataSource(mediaUrl) // set data resource
                mediaPlayerState = MediaPlayerState.INITIALIZED
            } catch (e: IllegalStateException) {
                mediaPlayerErrorListener?.onError(
                    "IllegalStateException-MediaPlayer-setDataSource()",
                    e.message.orEmpty()
                )
            }
            unMutePlayer() // set max volume
            try {
                prepareAsync() // prepare asynchronously
                mediaPlayerState = MediaPlayerState.PREPARING
            } catch (e: IllegalStateException) {
                mediaPlayerErrorListener?.onError(
                    "IllegalStateException-MediaPlayer-prepareAsync()",
                    e.message.orEmpty()
                )
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    seekTo(
                        lastSeekPosition ?: 0L,
                        SEEK_PREVIOUS_SYNC
                    ) // set last seek position if set in onStop() method
                } catch (e: IllegalStateException) {
                    mediaPlayerErrorListener?.onError(
                        "IllegalStateException-MediaPlayer-seekTo()",
                        e.message.orEmpty()
                    )
                } catch (e: IllegalArgumentException) {
                    mediaPlayerErrorListener?.onError(
                        "IllegalArgumentException-MediaPlayer-seekTo()",
                        e.message.orEmpty()
                    )
                }
            }
        }
    }

    private fun setUpListeners() {
        mediaPlayer?.apply {
            setOnPreparedListener(onPreparedListener)
            setOnBufferingUpdateListener(onBufferingUpdateListener)
            setOnCompletionListener(onCompletionListener)
            setOnErrorListener(onErrorListener)
        }
    }

    private fun playMediaPlayer() {
        if (mediaPlayerState == MediaPlayerState.UNINITIALIZED) {
            initializeMediaPlayer()
        }
        if (mediaPlayerState == MediaPlayerState.STOPPED || mediaPlayerState == MediaPlayerState.IDLE) {
            prepareMediaPlayer()
        } else {
            startPlaying()
        }
    }

    private fun startPlaying() {
        mediaPlayer?.apply {
            val validStatesToPlay =
                listOf(
                    MediaPlayerState.PREPARED,
                    MediaPlayerState.PAUSED,
                    MediaPlayerState.COMPLETED
                )
            if (mediaPlayerState in validStatesToPlay) {
                try {
                    start()
                    mediaPlayerState = MediaPlayerState.STARTED
                } catch (e: IllegalStateException) {
                    mediaPlayerErrorListener?.onError(
                        "IllegalStateException-MediaPlayer-start()",
                        e.message.orEmpty()
                    )
                }
            }
        }
    }

    private fun pauseMediaPlayer() {
        mediaPlayer?.apply {
            val validStatesToPause = listOf(MediaPlayerState.STARTED)
            if (getMediaPlayerState() in validStatesToPause) {
                try {
                    pause() // pause playback, call start() to resume
                    mediaPlayerState = MediaPlayerState.PAUSED
                } catch (e: IllegalStateException) {
                    mediaPlayerErrorListener?.onError(
                        "IllegalStateException-MediaPlayer-pause()",
                        e.message.orEmpty()
                    )
                }
            }
        }
    }

    private fun resumeMediaPlayer() {
        mediaPlayer?.apply {
            val validStatesToResume = listOf(MediaPlayerState.PAUSED)
            if (getMediaPlayerState() in validStatesToResume) {
                try {
                    start()
                    mediaPlayerState = MediaPlayerState.STARTED
                } catch (e: IllegalStateException) {
                    mediaPlayerErrorListener?.onError(
                        "IllegalStateException-MediaPlayer-start()",
                        e.message.orEmpty()
                    )
                }
            }
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.apply {
            val validStatesToStop = listOf(MediaPlayerState.STARTED, MediaPlayerState.PAUSED)
            if (mediaPlayerState in validStatesToStop) {
                try {
                    stop() // Stops playback after playback has been started or paused
                    mediaPlayerState = MediaPlayerState.STOPPED
                } catch (e: IllegalStateException) {
                    mediaPlayerErrorListener?.onError(
                        "IllegalStateException-MediaPlayer-stop()",
                        e.message.orEmpty()
                    )
                }
            }
        }
    }

    private fun resetMediaPlayer() {
        mediaPlayer?.apply {
            try {
                reset() // player will be in uninitialized state again
                mediaPlayerState = MediaPlayerState.IDLE
            } catch (e: IOException) {
                mediaPlayerErrorListener?.onError(
                    "IOException-MediaPlayer-reset()",
                    e.message.orEmpty()
                )
            }
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.apply {
            lastSeekPosition = currentPosition.toLong()
            try {
                release()
                mediaPlayerState = MediaPlayerState.UNINITIALIZED
            } catch (e: Exception) {
                mediaPlayerErrorListener?.onError(
                    "Exception-MediaPlayer-release()",
                    e.message.orEmpty()
                )
            }
        }
        mediaPlayer = null
    }
    // region end - private methods

    interface MediaPlayerStateListener {
        fun onStateChange(state: MediaPlayerState, data: Any? = null)
    }

    interface MediaPlayerVolumeStateListener {
        fun onVolumeChange(state: MediaPlayerVolumeState)
    }

    interface MediaPlayerErrorListener {
        fun onError(errorType: String?, errorCode: String?)
    }
}