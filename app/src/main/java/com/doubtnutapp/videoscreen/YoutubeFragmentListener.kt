package com.doubtnutapp.videoscreen

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants

/**
 * Created by Anand Gaurav on 2019-12-11.
 */
interface YoutubeFragmentListener {
    fun onYoutubePlayerEnd()

    /**
     * Call this method as a way to identify if a video has been paused because of user interaction.
     * Assumption has been made that if video is pausing when fragment is in RESUMED state, it is
     * a result of user interaction.
     * A sure-shot way is to add a custom controller UI and add a click listener on play/pause button,
     * but that will require significant code changes. If that happens in future, remove this method.
     */
    fun onYoutubeVideoPauseInResumedState() {/* no-op */
    }

    fun updateYoutubeEngagementTime(maxSeekTime: String, engagementTime: String)

    fun onYoutubeVideoPlayFailed(youtubeId: String, error: PlayerConstants.PlayerError) {}

    fun onYoutubePlayerStarted()
}