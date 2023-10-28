package com.doubtnutapp.fcm

import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.text.isDigitsOnly
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.utils.showToast
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.schedulers.Schedulers

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            FirebaseCrashlytics.getInstance()
                .setUserId(defaultPrefs(context!!).getString(Constants.STUDENT_ID, "").orEmpty())
            var data: HashMap<String, String>? = null
            val notificationFrom = intent.getStringExtra("notification_from")
            if (notificationFrom == Constants.MOENGAGE) {
                val jsonString = intent.getStringExtra("payload")
                try {
                    data = Gson().fromJson(
                        jsonString,
                        object : TypeToken<HashMap<String?, String?>?>() {}.type
                    )
                } catch (t: Throwable) {
                    Log.e(
                        Throwable(
                            "Exception In MoEngage NotificationActionReceiver $jsonString",
                            t
                        ), "ExceptionInMoengageNotification"
                    )
                }
            } else {
                try {
                    data = intent.extras?.getSerializable("data") as HashMap<String, String>?
                    if (data == null) {
                        Log.e(
                            Throwable("Exception In Notification Null Data"),
                            "ExceptionInNotification"
                        )
                    }
                } catch (exception: Exception) {
                    Log.e(
                        Throwable(
                            "Exception In NotificationActionReceiver" + intent.extras?.getSerializable(
                                "data"
                            ).toString(), exception
                        ), "ExceptionInNotification"
                    )
                }
            }

            if (data != null && data["data"] != null) {
                val appAction = data[Constants.NOTIFICATION_EVENT]
                if (appAction == Constants.MUTE_POST) {
                    if (!data[Constants.POST_ID].isNullOrEmpty()) {
                        showToast(context, "Post muted")
                        DataHandler.INSTANCE.teslaRepository.muteFeedPostNotification(hashMapOf<String, String>().apply {
                            put("entity_id", data[Constants.POST_ID]!!)
                            put("entity_type", "new_feed_type")
                        }.toRequestBody()).subscribeOn(Schedulers.io()).subscribe()
                    }
                    if (!data[Constants.NOTIFICATION_ID].isNullOrEmpty() && data[Constants.NOTIFICATION_ID]!!.isDigitsOnly()) {
                        val notificationId = data[Constants.NOTIFICATION_ID]!!.toInt()
                        val notificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        val channelId = context.getString(R.string.clvertap_notification_channel_id)
                        val notificationBuilder = NotificationCompat.Builder(context, channelId)
                            .setSmallIcon(R.mipmap.logo)
                            .setColorized(true)
                            .setAutoCancel(false)
                            .setVibrate(null)
                            .setContentText("Notification for this post muted successfully")
                            .setContentTitle("")
                        val notification = notificationBuilder.build()
                        notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
                        notificationManager.notify(notificationId, notification)
                    }
                    context.apply {
                        val params = HashMap<String, Any>()
                        params.putAll(data)
                        (applicationContext as DoubtnutApp).analyticsPublisher.get().publishEvent(
                            StructuredEvent(
                                category = Constants.NOTIFICATION,
                                action = EventConstants.NOTIFICATION_ACTION_CLICK,
                                eventParams = params
                            )
                        )
                    }
                }
            }
        }
    }
}

