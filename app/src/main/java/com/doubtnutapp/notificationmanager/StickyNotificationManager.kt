package com.doubtnutapp.notificationmanager

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.doubtnutapp.ActionHandlerActivity
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.getColorRes
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.google.gson.JsonObject

/**
 * Created by Anand Gaurav on 2020-01-29.
 */
object StickyNotificationManager {

    fun handleStickyNotification(context: Context, notificationManager: NotificationManager?) {
        if (notificationManager == null || Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return
        }
        if (defaultPrefs(context).getBoolean(NotificationConstants.NOTIFICATION_USER_DISMISS_QUICK_SEARCH, false)) {
            dismissNotification(notificationManager)
            return
        }

        val notificationLayout = RemoteViews(context.packageName, R.layout.notification_sticky)
        val channelId = "Quick Search"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "Quick Search"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(
                channelId,
                channelName,
                importance
            ).apply {
                setSound(null, null)
            }
            notificationManager.createNotificationChannel(mChannel)
        }

        notificationLayout.setOnClickPendingIntent(R.id.textViewAskDoubt, getPendingIntent(context, Constants.CAMERA, 2))
        notificationLayout.setOnClickPendingIntent(R.id.imageViewSearch, getPendingIntent(context, Constants.IN_APP_SEARCH, 1))
        notificationLayout.setOnClickPendingIntent(R.id.imageViewCamera, getPendingIntent(context, Constants.CAMERA, 2))
        notificationLayout.setOnClickPendingIntent(R.id.imageViewSetting, getPendingIntent(context, Constants.QUICK_SEARCH_SETTING, 3))

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.logo)
                .setColorized(true)
                .setAutoCancel(false)
                .setDefaults(Notification.DEFAULT_LIGHTS or Notification.DEFAULT_SOUND)
                .setVibrate(longArrayOf(0L))
                .setCustomContentView(notificationLayout)
                .setCustomBigContentView(notificationLayout)

        val notification = notificationBuilder.build()
        notification.flags = notification.flags or Notification.FLAG_NO_CLEAR
        notification.color = context.getColorRes(R.color.buttonColor)

        notificationManager.notify(NotificationConstants.NOTIFICATION_STICKY_GENERIC_ID, notification)
    }

    private fun getPendingIntent(context: Context, type: String, requestCode: Int): PendingIntent {
        val intent = Intent(context, ActionHandlerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("data", getData(type))
        intent.action = NotificationConstants.ACTION_QUICK_SEARCH
        return PendingIntent.getActivity(context, requestCode, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun getData(type: String): HashMap<String, String> {
        val dataMap: HashMap<String, String> = HashMap()
        dataMap["event"] = "sticky_notification"
        dataMap["data"] = JsonObject().apply {
            addProperty("type", type)
        }.toString()
        return dataMap
    }

    fun dismissNotification(notificationManager: NotificationManager?) {
        notificationManager?.cancel(NotificationConstants.NOTIFICATION_STICKY_GENERIC_ID)
    }
}