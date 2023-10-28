package com.doubtnutapp.ui.mediahelper

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.doubtnutapp.R
import com.doubtnutapp.downloadedVideos.DownloadedVideosActivity
import com.google.android.exoplayer2.ext.workmanager.WorkManagerScheduler
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.scheduler.Requirements
import com.google.android.exoplayer2.scheduler.Scheduler
import com.google.android.exoplayer2.ui.DownloadNotificationHelper
import com.google.android.exoplayer2.util.NotificationUtil
import com.google.android.exoplayer2.util.Util

const val EXO_DOWNLOAD_FOREGROUND_NOTIFICATION_ID = 1
const val EXO_DOWNLOAD_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL = 1000L
const val EXO_DOWNLOAD_NOTIFICATION_CHANNEL = "video_downloader"

class ExoDownloadService : DownloadService(EXO_DOWNLOAD_FOREGROUND_NOTIFICATION_ID,
        EXO_DOWNLOAD_FOREGROUND_NOTIFICATION_UPDATE_INTERVAL,
        EXO_DOWNLOAD_NOTIFICATION_CHANNEL,
        R.string.exo_download_notification_channel_name, 0) {

    override fun getDownloadManager(): DownloadManager {
        val utils = ExoUtils.getInstance(this)
        val downloadManager = utils.getDownloadManager()
        downloadManager.maxParallelDownloads = 2
        downloadManager.requirements = Requirements(Requirements.NETWORK)
        downloadManager.addListener(TerminalStateNotificationHelper(this, utils.downloadNotificationHelper, EXO_DOWNLOAD_FOREGROUND_NOTIFICATION_ID + 1))
        return downloadManager
    }

    override fun getScheduler(): Scheduler? {
        return WorkManagerScheduler(applicationContext, "exoWorker")
    }

    override fun getForegroundNotification(downloads: MutableList<Download>): Notification {
        val utils = ExoUtils.getInstance(this)
        val intent = Intent(applicationContext, DownloadedVideosActivity::class.java)
        val pendingIntent = PendingIntent.getActivities(applicationContext, 121, arrayOf(intent), PendingIntent.FLAG_ONE_SHOT)
        return utils.downloadNotificationHelper.buildProgressNotification(applicationContext, R.mipmap.logo, pendingIntent, "Taking video offline", downloads)
    }

    private class TerminalStateNotificationHelper(
            val context: Context, val notificationHelper: DownloadNotificationHelper, firstNotificationId: Int) : DownloadManager.Listener {
        var nextNotificationId: Int = firstNotificationId
        override fun onDownloadChanged(downloadManager: DownloadManager, download: Download, finalException: Exception?) {
            val intent = Intent(context, DownloadedVideosActivity::class.java)
            val pendingIntent = PendingIntent.getActivities(context, 121, arrayOf(intent), PendingIntent.FLAG_ONE_SHOT)
            val notification: Notification = when (download.state) {
                Download.STATE_COMPLETED -> {
                    notificationHelper.buildDownloadCompletedNotification(
                            context,
                            R.drawable.ic_done,  /* contentIntent= */
                            pendingIntent,
                            Util.fromUtf8Bytes(download.request.data))
                }
                Download.STATE_FAILED -> {
                    notificationHelper.buildDownloadFailedNotification(
                            context,
                            R.drawable.ic_cross,  /* contentIntent= */
                            pendingIntent,
                            Util.fromUtf8Bytes(download.request.data))
                }

                else -> {
                    return
                }
            }
            NotificationUtil.setNotification(context, nextNotificationId++, notification)
        }

    }

}