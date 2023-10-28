package com.doubtnut.analytics.debug

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.Keep
import androidx.core.app.NotificationCompat
import com.doubtnut.analytics.BuildConfig
import com.doubtnut.analytics.EventDestinations
import com.doubtnut.analytics.R

@SuppressLint("StaticFieldLeak")
object AnalyticsInterceptor {

    val analyticsEvents = arrayListOf<Event>()

    private const val CHANNEL_ID = "dn-analytics"
    private var builder: NotificationCompat.Builder? = null

    private var applicationContext: Context? = null

    fun init(applicationContext: Context) {
        if (isEnabled) {
            this.applicationContext = applicationContext
            createNotification()
        }
    }

    fun intercept(event: Event) {
        try {
            if (isEnabled) {
                analyticsEvents.add(0, event)
                updateNotification()
            }
        } catch (ignored: Exception) {
            // https://console.firebase.google.com/project/doubtnut-staging/crashlytics/app/android:com.doubtnutapp.staging/issues/bfbef9ba03eef653079b93fad87f285d?time=last-twenty-four-hours&devices=vivo%EF%BF%BFvivo%201951&sessionEventKey=60BE147F03E3000109C0C91C52F9A4A9_1549477981509454416
        }
    }

    val isEnabled = BuildConfig.ENABLE_ANALYTICS_INTERCEPTOR

    private fun createNotification() {
        if (applicationContext == null) return
        val notificationManager =
            applicationContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Doubtnut analytics monitoring",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        builder = NotificationCompat.Builder(applicationContext!!, CHANNEL_ID)
        val intent = Intent(applicationContext, AnalyticsLogActivity::class.java)
        builder!!.setSmallIcon(R.drawable.ic_analytics_notification)
        builder!!.setContentIntent(PendingIntent.getActivity(applicationContext, 0, intent, 0))
        builder!!.setContentTitle("DN analytics")
        notificationManager.notify(999, builder!!.build())
    }

    private fun updateNotification() {
        val notificationManager =
            applicationContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (analyticsEvents.isNotEmpty()) {
            var message = ""
            analyticsEvents.forEachIndexed { index, event ->
                if (index < 5) {
                    message += event.eventName + " " + event.destination.toString() + "\n"
                }
            }
            builder?.setStyle(NotificationCompat.BigTextStyle().bigText(message))
        }
        if (builder != null) {
            notificationManager.notify(999, builder!!.build())
        }
    }

    fun getEventDestination(trackerId: String): String {
        when (trackerId) {
            "firebase_client" -> return EventDestinations.FIREBASE
            "branch_client" -> return EventDestinations.BRANCH
            "clever_tap_client" -> return EventDestinations.CLEVERTAP
        }
        return ""
    }

    @Keep
    data class Event(val destination: ArrayList<String>, val eventName: String, val params: String)
}
