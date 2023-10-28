package com.doubtnut.core.view.mediaplayer

import androidx.lifecycle.Lifecycle

/**
Created by Sachin Saxena on 15/07/22.
 */
abstract class AbstractMediaPlayerManager {
    abstract fun registerLifecycle(lifecycle: Lifecycle)
    abstract fun setMedia(url: String)
    abstract fun getMedia() : String?
    abstract fun playMedia()
    abstract fun pauseMedia()
    abstract fun stopMedia()
    abstract fun isPlaying(): Boolean
    abstract fun setVolume(leftVolume: Float, rightVolume: Float)
    abstract fun toggleVolume()
    abstract fun mutePlayer()
    abstract fun unMutePlayer()
    abstract fun getMediaPlayerState(): MediaPlayerState
    abstract fun isPlayerMuted(): Boolean
    abstract fun getPlayerVolumeState(): MediaPlayerVolumeState
}