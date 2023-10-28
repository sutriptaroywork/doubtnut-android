package com.doubtnutapp

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.deeplink.AppActions
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.notificationmanager.StickyGenericTimerNotificationManager
import com.doubtnutapp.ui.splash.SplashActivity
import com.doubtnutapp.utils.AppUtils.isImmediateUpdate
import com.doubtnutapp.utils.ApxorUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.android.AndroidInjection
import org.json.JSONObject
import java.lang.reflect.Type
import javax.inject.Inject

class ActionHandlerActivity : Activity() {
    lateinit var eventTracker: Tracker

    var notifSource = Constants.FIREBASE_NOTIFICATION

    @Inject
    lateinit var actionHandlerEventManager: ActionHandlerEventManager

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        eventTracker = getTracker()
        FirebaseCrashlytics.getInstance()
            .setUserId(defaultPrefs(this).getString(Constants.STUDENT_ID, "").orEmpty())
        var data: HashMap<String, String>? = null
        val notificationFrom = intent?.getStringExtra("notification_from")
        if (notificationFrom != null) {
            notifSource = notificationFrom
        }
        if (notificationFrom == Constants.MOENGAGE) {
            val jsonString = intent.getStringExtra("payload")
            try {
                data = Gson().fromJson(
                    jsonString,
                    object : TypeToken<HashMap<String?, String?>?>() {}.type
                )
            } catch (t: Throwable) {
                Log.e(
                    Throwable("Exception In MoEngage Notification$jsonString", t),
                    "ExceptionInMoengageNotification"
                )
            }
        } else {
            try {
                data = intent?.extras?.getSerializable("data") as HashMap<String, String>?
                if (data == null) {
                    Log.e(
                        Throwable("Exception In Notification Null Data"),
                        "ExceptionInNotification"
                    )
                }
            } catch (exception: Exception) {
                Log.e(
                    Throwable(
                        "Exception In Notification" + intent?.extras?.getSerializable("data")
                            .toString(),
                        exception
                    ),
                    "ExceptionInNotification"
                )
            }
        }
        val notificationType = getNotificationType(data)
        ApxorUtils.logAppEvent(
            EventConstants.EVENT_NAME_APP_OPEN_DN,
            Attributes().apply {
                putAttribute(EventConstants.SOURCE, notificationType)
            }
        )

        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_APP_OPEN_DN)
        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_APP_OPEN_DN)

        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(EventConstants.EVENT_NAME_APP_OPEN_DN)
            .addEventParameter(EventConstants.SOURCE, notificationType)
            .addStudentId(UserUtil.getStudentId())
            .track()

        analyticsPublisher.publishEvent(
            StructuredEvent(category = EventConstants.EVENT_NAME_APP_OPEN_DN_CATEGORY,
                action = EventConstants.EVENT_NAME_APP_OPEN_DN,
                eventParams = hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, notificationType)
                    put(EventConstants.PARENT, EventConstants.NOTIFICATION)
                })
        )

//        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishBranchIoEvent(
//            AnalyticsEvent(
//                EventConstants.EVENT_NAME_APP_OPEN_DN,
//                hashMapOf<String, Any>().apply {
//                    put(EventConstants.SOURCE, notificationType)
//                }
//            )
//        )

        val countToSendEvent: Int =
            Utils.getCountToSend(RemoteConfigUtils.getEventInfo(), EventConstants.SESSION_START)
        repeat((0 until countToSendEvent).count()) {
            sendEventForPage(EventConstants.SESSION_START)
        }

        if (isImmediateUpdate()) {
            defaultPrefs(this).edit().putString(Constants.NOTIFICATION_DATA, "").apply()
            finish()
            startActivity(
                Intent(
                    this,
                    SplashActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            return
        }
        if (defaultPrefs(this).getString(
                Constants.STUDENT_LOGIN,
                "false"
            ) != "true" || !defaultPrefs(this).getBoolean(Constants.ONBOARDING_COMPLETED, false)
        ) {
            defaultPrefs(this).edit().putString(Constants.NOTIFICATION_DATA, Gson().toJson(data))
                .apply()
            startActivity(
                Intent(
                    this,
                    SplashActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        } else {
            defaultPrefs(this).edit().putString(Constants.NOTIFICATION_DATA, "").apply()
            handleData(data)
        }
    }

    private fun getNotificationType(data: HashMap<String, String>?): String {
        val source: String
        val firebaseNotificationTypes = AppActions.getActions()

        if (data == null || firebaseNotificationTypes.contains(data[Constants.NOTIFICATION_EVENT])) {
            source = Constants.FIREBASE_NOTIFICATION
        } else if (data[Constants.NOTIFICATION_EVENT] == Constants.STICKY_NOTIFICATION) {
            source = Constants.STICKY_NOTIFICATION
        } else if (data[Constants.NOTIFICATION_EVENT] == Constants.MATCH_NOTIFICATION ||
            data[Constants.NOTIFICATION_EVENT] == Constants.MATCH_OCR_NOTIFICATION
        ) {
            source = Constants.MATCH_PAGE_NOTIFICATION
        } else if (data[Constants.NOTIFICATION_EVENT] == AppActions.VIDEO_STICKY_NOTIFICATION.name) {
            source = AppActions.VIDEO_STICKY_NOTIFICATION.name
        } else if (data[Constants.NOTIFICATION_EVENT] == NotificationConstants.CHANNEL_ID_COURSE) {
            source = NotificationConstants.CHANNEL_ID_COURSE
        } else if (data[Constants.NOTIFICATION_EVENT] == NotificationConstants.GENERIC_STICKY_TIMER) {
            source = NotificationConstants.GENERIC_STICKY_TIMER
        } else {
            source = Constants.FIREBASE_NOTIFICATION
        }
        return source
    }

    private fun handleData(data: HashMap<String, String>?) {
        val extraData: JSONObject?

        if (data == null || data["data"] == null) {
            startActivity(
                Intent(
                    this,
                    SplashActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
            return
        }
        extraData = JSONObject(data["data"])

        val notifSrc =
            if (notifSource == Constants.MOENGAGE) Constants.MOENGAGE_NOTIFICATION else Constants.FIREBASE_NOTIFICATION
        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NOTIFICATION_CLICK,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, notifSrc)
                    if (extraData.has("notification_id")) put(
                        Constants.NOTIFICATION_ID,
                        extraData["notification_id"].toString()
                    ) else put(Constants.NOTIFICATION_ID, "NA")
                }, ignoreFirebase = false, ignoreBranch = false, ignoreMoengage = false
            )
        )
        updateClickedNotificationId(data)
        if (!data["firebase_eventtag"].isNullOrEmpty() && !data["firebase_eventtag"].equals("null")) {
            sendEvent("""${EventConstants.NOTIFICATION_OPEN_TAGGED}${data["firebase_eventtag"]}""")
        }

        // course_sticky_notification
        if (data["event"] == NotificationConstants.COURSE_NOTIFICATION) {
            // if button is clicked on course_sticky_notification
            if (extraData.has(Constants.SOURCE))
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.COURSE_NOTIFICATION_BUTTON_CLICK,
                        hashMapOf(
                            "student_id" to getStudentId(),
                            "push_id" to extraData.getString("id"),
                            "sn_type" to extraData.getString("sn_type")
                        ),
                        ignoreBranch = false,
                        ignoreMoengage = false
                    )
                )
            else {
                // if banner is clicked on course_sticky_notification
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.COURSE_NOTIFICATION_BANNER_CLICK,
                        hashMapOf(
                            "student_id" to getStudentId(),
                            "push_id" to extraData.getString("id"),
                            "sn_type" to extraData.getString("sn_type")
                        ),
                        ignoreBranch = false
                    )
                )

                Utils.sendClassLanguageSpecificEvent(EventConstants.COURSE_NOTIFICATION_BANNER_CLICK)

            }

            if (data["is_vanish"] == "true") {
                NotificationManagerCompat.from(this)
                    .cancel(NotificationConstants.COURSE_NOTIFICATION_REQUEST_CODE)
            }
        }

        if (data["event"] == NotificationConstants.GENERIC_STICKY_TIMER) {
            // if button is clicked on generic_sticky_with_timer_notification
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.STICKY_WITH_TIMER_NOTIFICATION_CLICK,
                    hashMapOf(
                        "student_id" to getStudentId(),
                        "push_id" to extraData.getString("id")
                    ),
                    ignoreBranch = false,
                    ignoreMoengage = false
                )
            )

            StickyGenericTimerNotificationManager.clearNotification()

            deeplinkAction.performAction(this, extraData.getString("deeplink"),
                Bundle().apply {
                    putBoolean(Constants.CLEAR_TASK, true)
                    putString(Constants.SOURCE, data["source"])
                })
            finish()

        }

        val isCommentNotificationAction = data[EventConstants.ACTION] == Constants.COMMENT
        if (!isCommentNotificationAction) {
            sendEvent(EventConstants.NOTIFICATION_OPEN)
            sendSnowPlowEvent(
                EventConstants.NOTIFICATION_OPEN,
                getNotificationType(data),
                data,
                extraData.toString()
            )
        } else {
            applicationContext?.apply {
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

        if (extraData.has("deeplink") && extraData.get("deeplink").toString().isNotEmpty()) {
            val source =
                if (data.containsKey(Constants.SOURCE)) data[Constants.SOURCE] else "notification"
            deeplinkAction.performAction(
                this, extraData.getString("deeplink"),
                Bundle().apply {
                    putBoolean(Constants.CLEAR_TASK, true)
                    putString(Constants.SOURCE, source)
                }
            )
            finish()
            return
        }

        val appAction = AppActions.fromString(data[Constants.NOTIFICATION_EVENT])

        if (appAction != null) {
            var deeplink = AppActions.getDeeplink(appAction)
            val deeplinkUriBuilder = Uri.parse(deeplink).buildUpon()
            data["path"]?.let {
                if (it.isNotBlank()) {
                    deeplinkUriBuilder.appendPath(it)
                }
            }
            extraData.keys().forEach {
                deeplinkUriBuilder.appendQueryParameter(it, extraData.getString(it))
            }
            if (!extraData.has("page"))
                deeplinkUriBuilder.appendQueryParameter("page", "notification")
            deeplink = deeplinkUriBuilder.build().toString()

            val source =
                if (data.containsKey(Constants.SOURCE)) data[Constants.SOURCE] else "notification"
            val deeplinkHandled = deeplinkAction.performAction(
                this, deeplink,
                Bundle().apply {
                    putBoolean(Constants.CLEAR_TASK, true)
                    putString(Constants.SOURCE, source)
                }
            )

            if (deeplinkHandled) {
                finish()
            } else {
                startActivity(
                    Intent(
                        this,
                        SplashActivity::class.java
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
            }
        } else {
            startActivity(
                Intent(
                    this,
                    SplashActivity::class.java
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
            finish()
        }
    }

    private fun sendSnowPlowEvent(
        eventName: String,
        source: String,
        data: java.util.HashMap<String, String>,
        payloadData: String
    ) {

        val params: HashMap<String, Any> = hashMapOf(
            EventConstants.SOURCE to source,
            EventConstants.PARAM_TIMESTAMP to System.currentTimeMillis(),
            EventConstants.PAYLOAD_DATA to payloadData
        )
        // params for feed notifications
        if (data != null && data["data"] != null) {
            val extraData = JSONObject(data["data"])

            if (extraData.has(Constants.FEED_TRIGGER)) {
                var feedTrigger = extraData.getString(Constants.FEED_TRIGGER)
                if (!feedTrigger.isNullOrEmpty()) {
                    params.put(Constants.FEED_TRIGGER, feedTrigger)
                }
            }
            if (extraData.has(Constants.TYPE)) {
                val type = extraData.getString(Constants.TYPE)
                if (!type.isNullOrEmpty()) {
                    params.put(Constants.TYPE, type)
                }
            }
        }
        applicationContext?.apply {
            (applicationContext as DoubtnutApp).analyticsPublisher.get().publishEvent(
                StructuredEvent(
                    category = Constants.NOTIFICATION,
                    action = eventName,
                    eventParams = params
                )
            )
        }
    }

    private fun updateClickedNotificationId(data: Map<String, String>) {
        val id: String = data["id"] ?: return
        val notficationIdString: String =
            defaultPrefs(this@ActionHandlerActivity).getString(
                Constants.SEEN_NOTIFICATIONS_LIST,
                ""
            ).orDefaultValue()
        var notificationList = ArrayList<String>()
        val gson = Gson()
        if (notficationIdString.isNotEmpty()) {
            val type: Type = object : TypeToken<List<String?>?>() {}.type
            notificationList = gson.fromJson(notficationIdString, type)
        }
        notificationList.add(id)
        defaultPrefs(this@ActionHandlerActivity).edit {
            putString(Constants.SEEN_NOTIFICATIONS_LIST, gson.toJson(notificationList))
        }
    }

    override fun onBackPressed() {
        finish()
        startActivity(
            Intent(
                this,
                SplashActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        super.onBackPressed()
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEvent(eventName: String) {
        eventTracker.addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(this@ActionHandlerActivity).toString())
            .addStudentId(getStudentId())
            .addScreenName(EventConstants.PAGE_NOTIFICATION)
            .track()
    }

    private fun sendEventForPage(eventName: String) {
        eventTracker.addEventNames(eventName)
            .addNetworkState(NetworkUtils.isConnected(this@ActionHandlerActivity).toString())
            .addStudentId(getStudentId())
            .addScreenName(EventConstants.PAGE_NOTIFICATION)
            .track()
    }

    private fun getStudentId() =
        defaultPrefs(this@ActionHandlerActivity).getString(Constants.STUDENT_ID, "")
            .orDefaultValue()
}
