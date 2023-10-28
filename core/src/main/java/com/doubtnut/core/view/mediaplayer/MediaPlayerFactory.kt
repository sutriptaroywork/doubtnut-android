package com.doubtnut.core.view.mediaplayer

/**
Created by Sachin Saxena on 15/07/22.
 */
object MediaPlayerFactory {

    fun getMediaPlayerManager(
        mediaPlayerVolumeStateListener: MediaPlayerManager.MediaPlayerVolumeStateListener? = null,
        mediaPlayerStateListener: MediaPlayerManager.MediaPlayerStateListener? = null,
        mediaPlayerErrorListener: MediaPlayerManager.MediaPlayerErrorListener? = null
    ): AbstractMediaPlayerManager {
        return MediaPlayerManager(
            mediaPlayerVolumeStateListener,
            mediaPlayerStateListener,
            mediaPlayerErrorListener
        )
    }
}