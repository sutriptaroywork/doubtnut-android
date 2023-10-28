package com.doubtnutapp.ui.mediahelper

import android.media.MediaPlayer
import android.os.Handler
import java.io.IOException

object MediaPlayerHelper {

    const val DEFAULT_PULSE_RATE = 1000L

    private var mediaPlayer: MediaPlayer? = null
    private var onCompleteListener: OnCompleteListener? = null
    private var onCurrentPositionListener: MediaCurrentPositionListener? = null

    private val handler: Handler = Handler()
    private val runnable: Runnable = object : Runnable {
        override fun run() {
            mediaPlayer?.let {
                onCurrentPositionListener?.onCurrentPositionChange(it.currentPosition)
                handler.postDelayed(this, DEFAULT_PULSE_RATE)
            }
        }
    }

    private val onCompletionListener: MediaPlayer.OnCompletionListener =
        MediaPlayer.OnCompletionListener {
            onCompleteListener?.let {
                it.onComplete()
                stop()
            }
        }

    @Throws(
        IOException::class,
        IllegalArgumentException::class,
        SecurityException::class,
        IllegalStateException::class
    )
    fun start(mediaFilePath: String) {

        onCompleteListener?.let {
            it.onComplete()
            stop()
        }

        requireNotNull(mediaFilePath)
        initialiseMediaPlayer(mediaFilePath)

        requireNotNull(mediaPlayer)
        mediaPlayer?.start()

        startTimer()
    }

    fun stop() {
        releaseListener()
        pauseTimer()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun releaseListener() {
        onCompleteListener = null
        onCurrentPositionListener = null
    }

    fun seekTo(msec: Int) {
        mediaPlayer?.seekTo(msec)
    }

    fun getMediaDuration(): Int {
        requireNotNull(mediaPlayer)
        return mediaPlayer?.duration ?: 0
    }

    /**
     * You need to call this method before starting the MediaPlayer.
     *
     *@param onCompletionListener instance to receive the callback from MediaPlayer
     * */
    fun setOnCompleteListener(onCompleteListener: OnCompleteListener) {
        this.onCompleteListener = onCompleteListener
    }

    fun setOnCurrentPositionChangeListener(onCurrentPositionListener: MediaCurrentPositionListener) {
        this.onCurrentPositionListener = onCurrentPositionListener
    }

    @Throws(
        IOException::class,
        IllegalArgumentException::class,
        SecurityException::class,
        IllegalStateException::class
    )
    private fun initialiseMediaPlayer(mediaFilePath: String) {
        mediaPlayer = MediaPlayer().apply {
            setDataSource(mediaFilePath)
            prepare()
            setOnCompletionListener(onCompletionListener)
        }
    }

    private fun startTimer() {
        handler.postDelayed(runnable, DEFAULT_PULSE_RATE)
    }

    private fun pauseTimer() {
        handler.removeCallbacksAndMessages(null)
    }

    interface MediaCurrentPositionListener {
        fun onCurrentPositionChange(currentPosition: Int)
    }

    interface OnCompleteListener {
        fun onComplete()
    }

}