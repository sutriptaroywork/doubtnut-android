package com.doubtnutapp.video

import com.google.android.exoplayer2.ui.TimeBar

interface VideoFragmentListener {

    fun onFullscreenRequested() {}
    fun onPortraitRequested() {}

    fun onVideoCompleted() {}

    /*
    Used to save the published viewid (used in recyclerviews, where we need to save the published view
    id in the adapter models so that a new viewid is not created when view is recycled and binded again)
     */
    fun onViewIdPublished(viewId: String) {}

    fun onVideoStart() {}

    fun onVideoPause() {}

    fun addLandscapeSimilarFragment() {}

    fun removeLandscapeSimilarFragment() {}

    fun hideLandscapeSimilarFragment() {}

    fun showLandscapeSimilarFragment() {}

    fun changeLandscapeSimilarFragmentState(toExpand: Boolean) {}

    fun singleTapOnPlayerView() {}

    fun showSuggestions() {}

    fun hideSuggestions() {}

    fun onSeekPositionChange(position: Long) {}

    fun onShowPlayerControls() {}

    fun onHidePlayerControls() {}

    fun onPictureInPictureModeRequested() {}

    fun onExoPlayerTimeBarSrubStart(timeBar: TimeBar, position: Long) {}

    /**
     * Callback when user seeks the video using scrub or does forward / rewind
     */
    fun onExoPlayerPositionDiscontinuityReasonSeek() {}

    fun onExoPlayerProgress(positionMs: Long) {}
}