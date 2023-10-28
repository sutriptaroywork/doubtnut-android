package com.doubtnutapp.videoPage.model

/**
 * Created by devansh on 28/07/20.
 */

data class VideoStickyNotificationData(
        val questionId: String,
        val page: String,
        val watchedTimeSeconds: Int,
        val totalTimeSeconds: Int,
        val imageUrl: String,
        val playListId: String,
        val notificationTitle: String,
        val remainingDurationText: String
)