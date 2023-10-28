package com.doubtnutapp.notificationmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.apxor.androidsdk.core.Attributes
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.dpToPx
import com.doubtnutapp.ActionHandlerActivity
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.deeplink.AppActions
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.BitmapUtils
import com.doubtnutapp.videoPage.model.VideoStickyNotificationData
import com.google.gson.JsonObject
import kotlin.math.min

/**
 * Created by devansh on 2020-07-25.
 */

object VideoStickyNotificationManager {

    fun handleStickyNotification(context: Context,
                                 notificationManager: NotificationManager?,
                                 videoData: VideoStickyNotificationData) {
        if (notificationManager == null || Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return
        }

        if (defaultPrefs(context).getBoolean(NotificationConstants.NOTIFICATION_USER_DISMISS_VIDEO_STICKY, false)) {
            return
        }

        val notificationLayoutSmall = RemoteViews(context.packageName,
            R.layout.notification_video_sticky
        )
        val notificationLayoutBig = RemoteViews(context.packageName,
            R.layout.notification_video_sticky_big
        )

        val channelId = NotificationConstants.NOTIFICATION_CHANNEL_ID_VIDEO_STICKY

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = NotificationConstants.NOTIFICATION_CHANNEL_ID_VIDEO_STICKY
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(channelId, channelName, importance).apply {
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(mChannel)
        }

        val notificationPriority = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager.IMPORTANCE_HIGH
        } else {
            NotificationCompat.PRIORITY_MAX
        }

        val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.logo)
                .setColorized(true)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                .setVibrate(null)
                .setPriority(notificationPriority)
                .setCustomContentView(notificationLayoutSmall)
                .setCustomBigContentView(notificationLayoutBig)
                .setContentIntent(getVideoPagePendingIntent(context, videoData, NotificationConstants.NOTIFICATION_STICKY_GENERIC_ID))
                .build()

        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notification.color = ContextCompat.getColor(context, R.color.buttonColor)
        }

        setUpUi(context, notificationLayoutSmall, notificationLayoutBig, videoData) {
            notificationManager.notify(NotificationConstants.NOTIFICATION_STICKY_GENERIC_ID, notification)
        }

        ApxorUtils.logAppEvent(EventConstants.VIDEO_STICKY_NOTIFICATION_SHOWN, Attributes().apply {
            putAttribute(Constants.QUESTION_ID, videoData.questionId)
        })
    }


    private fun setUpUi(context: Context,
                        notificationLayoutSmall: RemoteViews,
                        notificationLayoutBig: RemoteViews,
                        videoData: VideoStickyNotificationData,
                        onSetupComplete: () -> Unit) {

        setupSmallNotificationLayout(context, notificationLayoutSmall, videoData)
        setupBigNotificationLayout(context, notificationLayoutBig, videoData)

        getNotificationImageBitmaps(context, videoData.imageUrl) { smallImage, bigImage ->
            notificationLayoutSmall.setImageViewBitmap(R.id.imageViewVideo, smallImage)
            notificationLayoutBig.setImageViewBitmap(R.id.imageViewVideo, bigImage)
            onSetupComplete()
        }
    }


    private fun setupSmallNotificationLayout(context: Context, notificationLayout: RemoteViews,
                                             videoData: VideoStickyNotificationData) {
        notificationLayout.apply {
            setTextViewText(R.id.tvNotificationTitle, videoData.notificationTitle)

            if (videoData.remainingDurationText.isNotBlank()) {
                setTextViewText(R.id.tvTimeRemaining, videoData.remainingDurationText)
            } else {
                setViewVisibility(R.id.tvTimeRemaining, View.GONE)
            }

            setOnClickPendingIntent(R.id.videoStickyNotificationSetting, getSettingsPendingIntent(context, videoData.questionId))
            setOnClickPendingIntent(
                R.id.imageViewVideo, getVideoPagePendingIntent(context, videoData,
                    NotificationConstants.NOTIFICATION_STICKY_GENERIC_ID)
            )
        }
    }


    private fun setupBigNotificationLayout(context: Context, notificationLayout: RemoteViews,
                                           videoData: VideoStickyNotificationData) {
        val watchedTimeText = getTimelineText(videoData.watchedTimeSeconds)
        val totalTimeText = getTimelineText(videoData.totalTimeSeconds)

        notificationLayout.apply {
            setTextViewText(R.id.tvWatchedTime, watchedTimeText)
            setTextViewText(R.id.tvTotalTime, totalTimeText)
            setTextViewText(R.id.tvNotificationTitle, videoData.notificationTitle)
            setTextViewText(R.id.tvTimeRemaining, videoData.remainingDurationText)
            setProgressBar(
                R.id.stickyNotificationProgressBar, videoData.totalTimeSeconds,
                    videoData.watchedTimeSeconds, false)

            setOnClickPendingIntent(R.id.videoStickyNotificationSetting, getSettingsPendingIntent(context, videoData.questionId))
            setOnClickPendingIntent(
                R.id.imageViewVideo, getVideoPagePendingIntent(context, videoData,
                    NotificationConstants.NOTIFICATION_STICKY_GENERIC_ID)
            )
        }
    }


    private fun getTimelineText(seconds: Int): CharSequence {
        return "%02d:%02d".format(seconds / 60, seconds % 60)
    }


    private fun getVideoPagePendingIntent(context: Context, videoData: VideoStickyNotificationData,
                                          requestCode: Int): PendingIntent {
        val intent = Intent(context, ActionHandlerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("data", getVideoPlayData(videoData))
        intent.action = NotificationConstants.ACTION_QUICK_SEARCH
        return PendingIntent.getActivity(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private fun getVideoPlayData(videoData: VideoStickyNotificationData): HashMap<String, String> {
        val dataMap: HashMap<String, String> = HashMap()
        dataMap[Constants.NOTIFICATION_EVENT] = AppActions.VIDEO.name
        dataMap[Constants.SOURCE] = AppActions.VIDEO_STICKY_NOTIFICATION.name
        dataMap["data"] = JsonObject().apply {
            addProperty(Constants.Q_ID, videoData.questionId)
            addProperty(Constants.PAGE, videoData.page)
            addProperty(Constants.VIDEO_START_POSITION, videoData.watchedTimeSeconds)
            addProperty(Constants.PLAYLIST_ID, videoData.playListId)
            addProperty(Constants.IMAGE_URL, videoData.imageUrl)
        }.toString()
        return dataMap
    }


    private fun getSettingsPendingIntent(context: Context, qid: String): PendingIntent {
        val settingsIntent = Intent(context, ActionHandlerActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("data", hashMapOf(
                    Constants.NOTIFICATION_EVENT to AppActions.VIDEO_STICKY_NOTIFICATION.name,
                    "data" to JsonObject().apply {
                        addProperty("type", Constants.QUICK_SEARCH_SETTING)
                        addProperty(Constants.Q_ID, qid)
                    }.toString()))
        }
        return PendingIntent.getActivity(context, 3, settingsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private fun getNotificationImageBitmaps(context: Context, imageUrl: String,
                                            onSuccess: (smallImage: Bitmap, bigImage: Bitmap) -> Unit) {
        BitmapUtils.loadBitmap(context, imageUrl) { originalBitmap ->
            val bigBitmap = getSuitableBigImageBitmap(originalBitmap)
            BitmapUtils.loadBitmap(context, originalBitmap, RoundedCorners(4.dpToPx())) { smallBitmap ->
                onSuccess(smallBitmap, bigBitmap)
            }
        }
    }


    private fun getSuitableBigImageBitmap(toTransform: Bitmap): Bitmap =
            getSuitableImageBitmap(toTransform, 2.0)


    private fun getSuitableImageBitmap(toTransform: Bitmap, requiredAspectRatio: Double): Bitmap {
        val requiredHeight = min((toTransform.width / requiredAspectRatio).toInt(), toTransform.height)
        return Bitmap.createBitmap(toTransform, 0, 0, toTransform.width, requiredHeight)
    }
}