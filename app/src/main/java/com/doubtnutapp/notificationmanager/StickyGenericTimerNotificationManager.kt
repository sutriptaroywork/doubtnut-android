package com.doubtnutapp.notificationmanager

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.ActionHandlerActivity
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.fcm.notification.NotificationConstants.CHANNEL_NAME_STICKY_TIMER
import com.doubtnutapp.getTestStartBeforeTimeDifferenceLong
import com.doubtnutapp.isNotNullAndNotEmpty
import com.google.gson.Gson
import com.google.gson.JsonObject
import org.json.JSONObject

object StickyGenericTimerNotificationManager {

    private var notificationManager: NotificationManager? = null
    private var dataObject: JSONObject = JSONObject()
    private var firebaseTag: String? = null

    private var notificationEndTime: String? = null

    private var timer: NotificationTimer.Builder? = null

    fun handleStickyNotification(
        data: Map<String, String>,
        context: Context,
        _notificationManager: NotificationManager?
    ) {
        notificationManager = _notificationManager
        if (notificationManager == null || Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            return
        }

        val channelId = NotificationConstants.CHANNEL_ID_GENERIC_STICKY_TIMER

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(
                channelId, CHANNEL_NAME_STICKY_TIMER, importance
            )
            notificationManager!!.createNotificationChannel(mChannel)
        }

        dataObject = JSONObject(data["data"])

        firebaseTag =
            if (data["firebase_eventtag"].isNotNullAndNotEmpty() && !data["firebase_eventtag"].equals(
                    "null"
                )
            ) data["firebase_eventtag"].orEmpty()
            else ""


        notificationEndTime = dataObject.get("end_time").toString()
        val millisTillFinish = getTestStartBeforeTimeDifferenceLong(notificationEndTime)

        //check to avoid past time notifications
        if (millisTillFinish <= 0) return

        val pendingIntent = getPendingIntent(
            context,
            NotificationConstants.GENERIC_STICKY_TIMER_BANNER_REQUEST_CODE,
            dataObject["deeplink_banner"].toString()
        )

        timer = NotificationTimer.Builder(context)
            .setContentIntent(pendingIntent)
            .setOnFinishListener {}
            .setData(
                Gson().fromJson(
                    dataObject.toString(),
                    HashMap::class.java
                ) as HashMap<String, String>
            )
            .setNotificationManager(notificationManager ?: return)

        timer?.play(millisTillFinish)

        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(
            AnalyticsEvent(
                EventConstants.STICKY_WITH_TIMER_NOTIFICATION_DISPLAY, hashMapOf(
                    "source" to NotificationConstants.GENERIC_STICKY_TIMER_NOTIFICATION,
                    "id" to dataObject.getString("id")
                )
            )
        )
    }

    fun clearNotification() {
        timer?.terminate()
        timer = null
    }

    fun removePreviousNotification(data: Map<String, String>) {
        val tempDataObject = JSONObject(data["data"])
        val notificationEndTime = tempDataObject.get("end_time").toString()
        val timeToFinish = getTestStartBeforeTimeDifferenceLong(notificationEndTime)
        if (timeToFinish > 0)
            clearNotification()
    }

    private fun getPendingIntent(
        context: Context,
        requestCode: Int,
        deeplink: String,
    ): PendingIntent {

        val intent = Intent(context, ActionHandlerActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra("data", getData(deeplink))
        intent.action = NotificationConstants.GENERIC_STICKY_TIMER
        return PendingIntent.getActivity(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private fun getData(deeplink: String): HashMap<String, String> {

        val dataMap: HashMap<String, String> = HashMap()
        dataMap["event"] = NotificationConstants.GENERIC_STICKY_TIMER
        dataMap["source"] = NotificationConstants.GENERIC_STICKY_TIMER

        if (!firebaseTag.isNullOrBlank()) {
            dataMap["firebase_eventtag"] = firebaseTag.orEmpty()
        }

        dataMap["data"] = JsonObject().apply {
            addProperty("deeplink", deeplink)
            addProperty("id", dataObject.getString("id"))
        }.toString()
        return dataMap
    }
}