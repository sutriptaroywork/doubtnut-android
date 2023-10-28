package com.doubtnutapp

/**
 * Created by devansh on 11/1/21.
 */

object CoreActions {
    const val QUESTION_ASK = "question_ask"
    const val NCERT_VIDEO_WATCH = "ncert_video_watch"
    const val LIVE_CLASS_VIDEO_WATCH = "live_class_video_watch"
    const val GAME_OPEN = "game_open"
    const val FEED_SEEN = "feed_seen"

    /**
     * Core actions that need to be checked for app exit dialog
     */
    val appExitCoreActions =
        setOf(QUESTION_ASK, NCERT_VIDEO_WATCH, LIVE_CLASS_VIDEO_WATCH, GAME_OPEN, FEED_SEEN)
}
