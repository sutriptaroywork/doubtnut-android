package com.doubtnutapp.notificationmanager

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.doubtnutapp.*
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.utils.BitmapUtils
import com.google.gson.JsonObject
import java.util.*

object StudyGroupNotificationManager {

    @SuppressLint("UnspecifiedImmutableFlag")
    fun handleStudyGroupNotification(
        data: Map<String, String>,
    ) {

        val notificationId = data["notification_id"]?.toInt() ?: Random().nextInt(10000)
        val context = DoubtnutApp.INSTANCE

        val title = data["title"]
        val imageUrl = data["image"]
        val message = data["message"]
        val bigContentTitle = data["big_content_title"]
        val summaryText = data["summary_text"]
        val summaryDeeplink = data["summary_deeplink"]
        val autoDownloadImage: Boolean? = data["auto_download_image"]?.toBoolean()

        val channelId = NotificationConstants.CHANNEL_ID_STUDY_GROUP
        val summaryId = NotificationConstants.STUDY_GROUP_SUMMARY_NOTIFICATION_ID
        val groupKey = NotificationConstants.GROUP_KEY_STUDY_GROUP

        val notificationManager = NotificationManagerCompat.from(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(channelId, channelId, importance)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification =
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.logo)
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(groupKey)

        val isAutoImageDownloadEnabled =
            defaultPrefs().getBoolean(Constants.SG_IMAGE_AUTO_DOWNLOAD, false)
        if (isAutoImageDownloadEnabled || autoDownloadImage == true) {
            if (imageUrl.isNullOrEmpty().not()) {
                val bitmap = BitmapUtils.getBitmapFromUrl(context, imageUrl!!)
                if (bitmap != null)
                    notification.setStyle(
                        NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                    )
            } else {
                notification.setStyle(NotificationCompat.BigTextStyle().bigText(message))
            }
        }

        val summaryNotification =
            NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.logo)
                .setStyle(
                    NotificationCompat.InboxStyle()
                        .addLine("$title $message")
                        .setBigContentTitle(bigContentTitle)
                        .setSummaryText(summaryText)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setGroup(groupKey)
                .setAutoCancel(true)
                .setGroupAlertBehavior(NotificationCompat.GROUP_ALERT_CHILDREN)
                .setGroupSummary(true)

        val dataMap: HashMap<String, String> = HashMap(data)
        val actionHandlerIntent = Intent(context, ActionHandlerActivity::class.java)
        actionHandlerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        actionHandlerIntent.putExtra(Constants.DATA, dataMap)

        val pendingIntent = PendingIntent.getActivity(
            context, notificationId /* Request code */, actionHandlerIntent,
            PendingIntent.FLAG_ONE_SHOT
        )

        notification.setContentIntent(pendingIntent)

        val summaryMap: HashMap<String, String> = HashMap()
        summaryMap["data"] = JsonObject().apply {
            addProperty("deeplink", summaryDeeplink)
        }.toString()

        val summaryActionHandlerIntent = Intent(context, ActionHandlerActivity::class.java)
        summaryActionHandlerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        summaryActionHandlerIntent.putExtra(Constants.DATA, HashMap(summaryMap.toMap()))

        val summatPendingIntent = PendingIntent.getActivity(
            context, notificationId /* Request code */, summaryActionHandlerIntent,
            PendingIntent.FLAG_ONE_SHOT
        )
        summaryNotification.setContentIntent(summatPendingIntent)

        notificationManager.apply {
            notify(notificationId, notification.build())
            notify(summaryId, summaryNotification.build())
        }
    }

    fun dismissNotification(notificationId: Int) {
        val notificationManager = NotificationManagerCompat.from(DoubtnutApp.INSTANCE)
        notificationManager.cancel(notificationId)
    }
}