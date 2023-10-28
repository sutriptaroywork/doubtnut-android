package com.doubtnutapp.fcm

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.content.edit
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.apxor.androidsdk.core.Attributes
import com.apxor.androidsdk.plugins.push.ApxorPushAPI
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.constant.ErrorConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ThreadUtils
import com.doubtnutapp.*
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.fcm.notification.NotificationBuilderFactory
import com.doubtnutapp.fcm.notification.NotificationConstants
import com.doubtnutapp.fcm.notification.NotificationItemResolver
import com.doubtnutapp.fcm.notification.PushNotification
import com.doubtnutapp.liveclass.ui.LiveClassActivity
import com.doubtnutapp.liveclassreminder.LiveClassReminderActivity
import com.doubtnutapp.matchquestion.ui.activity.MatchQuestionActivity
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.notificationmanager.NotificationTimer
import com.doubtnutapp.notificationmanager.StickyCourseNotificationManager
import com.doubtnutapp.notificationmanager.StickyGenericTimerNotificationManager
import com.doubtnutapp.notificationmanager.StudyGroupNotificationManager
import com.doubtnutapp.ui.quiz.QuizNotificationActivity
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.videoPage.ui.VideoPageActivity
import com.freshchat.consumer.sdk.Freshchat
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.pushbase.MoEPushHelper
import com.theartofdev.edmodo.cropper.CropImageActivity
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import java.util.*
import javax.inject.Inject

class FCMMessagingService : FirebaseMessagingService() {

    private var uniqueId = 45373734
    private var notificationManager: NotificationManager? = null

    @Inject
    lateinit var userPreference: UserPreference

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
    }

    override fun onNewToken(refreshedToken: String) {
        super.onNewToken(refreshedToken)
        defaultPrefs(this.application as DoubtnutApp).edit {
            putString(Constants.GCM_REG_ID, refreshedToken)
            putBoolean(Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER, false)
        }
        refreshedToken.let {
            sendRegistrationToServer(this, it)
            ChatUtil.sendToken(this, it)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            if (MoEPushHelper.getInstance().isFromMoEngagePlatform(remoteMessage.data)) {
                ApxorUtils.logAppEvent(EventConstants.NOTIFICATION_RECEIVED, Attributes().apply {
                    putAttribute(EventConstants.SOURCE, Constants.MOENGAGE_NOTIFICATION)
                })
                sendNotificationReceivedEvent(remoteMessage.data, Constants.MOENGAGE_NOTIFICATION)
                MoEFireBaseHelper.getInstance()
                    .passPushPayload(applicationContext, remoteMessage.data)
            } else if (Freshchat.isFreshchatNotification(remoteMessage)) {
                ChatUtil.initializeFreshWorks(applicationContext)
                Freshchat.handleFcmMessage(this.application as DoubtnutApp, remoteMessage)
            } else {
                // your app's business logic to show notification
                ApxorUtils.logAppEvent(EventConstants.NOTIFICATION_RECEIVED, Attributes().apply {
                    putAttribute(EventConstants.SOURCE, Constants.FIREBASE_NOTIFICATION)
                })
                sendNotificationReceivedEvent(remoteMessage.data, Constants.FIREBASE_NOTIFICATION)
                if (remoteMessage.data.isNotEmpty()) {
                    val extras = Bundle()
                    for (entry in remoteMessage.data.entries) {
                        extras.putString(entry.key, entry.value)
                    }
                    val eventTracker = getTracker()
                    if (ApxorPushAPI.isApxorNotification(remoteMessage)) {
                        ThreadUtils.runOnAnalyticsThread {
                            ApxorPushAPI.handleNotification(remoteMessage, applicationContext)
                        }
                    } else {
                        if (extras.getString("notification_type") == "SILENT_QUIZ_NOTIFICATION") {
                            val quizIntent = Intent(this, QuizNotificationActivity::class.java)
                            quizIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            this.startActivity(quizIntent)
                        } else if (extras.getString("notification_type") == "SILENT_GAMIFICATION") {
                            (application as DoubtnutApp).showPopup(extras)
                        } else if (extras.getString("event") == "live_class_reminder_pop_up") {
                            var shouldShow = true
                            try {
                                val manager: ActivityManager =
                                    getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                                val taskList: List<ActivityManager.RunningTaskInfo> =
                                    manager.getRunningTasks(10)
                                val list = listOf<String>(
                                    CameraActivity::class.java.name,
                                    MatchQuestionActivity::class.java.name,
                                    CropImageActivity::class.java.name,
                                    VideoPageActivity::class.java.name,
                                    LiveClassActivity::class.java.name,
                                    LiveClassReminderActivity::class.java.name
                                )
                                if (list.contains(taskList[0].topActivity?.className)) {
                                    shouldShow = false
                                }
                            } catch (e: Exception) {

                            }
                            if (shouldShow) {
                                val liveClassReminderActivityIntent = Intent(
                                    this,
                                    LiveClassReminderActivity::class.java
                                )
                                liveClassReminderActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                liveClassReminderActivityIntent.putExtra(
                                    Constants.DATA,
                                    HashMap(remoteMessage.data)
                                )
                                startActivity(liveClassReminderActivityIntent)
                            }
                        } else if (extras.getString("event") == "force_logout") {
                            userPreference.logOutUser()
                            if ((extras.get("silent") as? String).toBoolean()) {
                                //do nothing
                            } else {
                                sendNotification(remoteMessage.data, eventTracker)
                                sendEventForNotificationCreation()
                            }
                        } else {
                            sendNotification(remoteMessage.data, eventTracker)
                            sendEventForNotificationCreation()
                        }
                    }


                    if (!remoteMessage.data["firebase_eventtag"].isNullOrEmpty() && !remoteMessage.data["firebase_eventtag"].equals(
                            "null"
                        )
                    ) {
                        applicationContext?.apply {
                            eventTracker.addEventNames("""${EventConstants.NOTIFICATION_RECEIVED_TAGGED}${remoteMessage.data["firebase_eventtag"].toString()}""")
                                .addNetworkState(
                                    NetworkUtils.isConnected(this@FCMMessagingService).toString()
                                )
                                .addStudentId(getStudentId())
                                .addScreenName(EventConstants.PAGE_NOTIFICATION)
                                .track()
                        }

                    }
                }
            }
        } catch (t: Throwable) {
//          Fatal Exception: java.lang.OutOfMemoryError
//          Failed to allocate a 1804212 byte allocation with 1544840 free bytes and 1508KB until OOM
            with(FirebaseCrashlytics.getInstance()) {
                log("Notification data: ${remoteMessage.data}")
                setCustomKey(ErrorConstants.DN_FATAL, true)
                recordException(t)
            }
        }
    }

    companion object {
        private const val TAG = "FirebaseMessagingService"

        @SuppressLint("CheckResult")
        fun sendRegistrationToServer(context: Context, refreshedToken: String) {

            // Send Push Token to MoEngage
            MoEngageUtils.sendPushToken(context, refreshedToken)

            val params: HashMap<String, Any> = HashMap()
            params[Constants.KEY_GCM_REG_ID] = refreshedToken
            params[Constants.KEY_STUDENT_ID] = getStudentId()

            if (!defaultPrefs(context).getString(Constants.XAUTH_HEADER_TOKEN, "")
                    .isNullOrBlank()
            ) {

                DataHandler.INSTANCE.studentsRepositoryv2.updateUserProfileObservable(params.toRequestBody())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        if (DoubtnutApp.INSTANCE.fcmRegId.isEmpty()) return@subscribe
                        defaultPrefs(context).edit {
                            putBoolean(Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER, true)
                        }
                    }, {})

            }
        }
    }

    private fun sendNotification(data: Map<String, String>, eventTracker: Tracker) {
        val channelId = getString(R.string.clvertap_notification_channel_id)

        val title = data["title"]
        val imageUrl = data["image"]
        val body = data["message"]
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //local broadcast
        val broadcaster = LocalBroadcastManager.getInstance(baseContext)
        val intent = Intent(Constants.LOCAL_BROADCAST_REQUEST_ACCEPT)
        val hashMap: HashMap<String, String> = HashMap()
        convertArrayMapToHashMap(data, hashMap)
        intent.putExtra(Constants.LOCAL_BROADCAST_REQUEST_DATA, hashMap)
        broadcaster.sendBroadcast(intent)

        /*
        * We need to send `id` as key with value in RemoteMessage.data so as to show the notification we want to.
        * `id` will be used as identifier for the different notifications.
        * */
        when {
            data["event"].equals(NotificationConstants.VIDEO_NOTIFICATION) -> {
                if (data["data"] != null
                    && JSONObject(data["data"]).has("doubt_comment_id")
                    && Utils.isInForeGround()
                    && Utils.isActivityOnTop(listOf(LiveClassActivity::class.java.name))
                ) {
                    try {
                        val qid = JSONObject(data["data"]).getString("qid")
                        val commentId = JSONObject(data["data"]).getString("doubt_comment_id")
                        if (!qid.isNullOrBlank() && !commentId.isNullOrBlank()) {
                            if (DoubtnutApp.INSTANCE.onLiveClassDoubtAnswered(qid, commentId)) {
                                return
                            }
                        }
                    } catch (e: Exception) {
                        //ignore exception
                    }
                }
                val immutableNotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                PushNotification(
                    immutableNotificationManager,
                    NotificationItemResolver(),
                    NotificationBuilderFactory()
                )
                    .show(applicationContext, data)
                return
            }
            data["event"].equals(NotificationConstants.COURSE_NOTIFICATION) -> {
                when (data["sn_type"]) {
                    "image", "text", "banner" -> {
                        val immutableNotificationManager =
                            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        StickyCourseNotificationManager.handleCourseStickyNotification(
                            data,
                            this,
                            immutableNotificationManager
                        )
                        return
                    }
                    else -> {
                        //default flow
                    }
                }
            }
            data["event"].equals(NotificationConstants.STUDY_GROUP_NOTIFICATION) || data["event"].equals(
                NotificationConstants.STUDY_GROUP
            ) && data["notification_id"] != null -> {
                val notificationId = data["notification_id"]
                notificationId?.let {
                    StudyGroupNotificationManager.handleStudyGroupNotification(data)
                    return
                }
            }
            data["event"].equals(NotificationConstants.GENERIC_STICKY_TIMER) -> {
                val immutableNotificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                StickyGenericTimerNotificationManager.removePreviousNotification(data)

                StickyGenericTimerNotificationManager.handleStickyNotification(
                    data,
                    this,
                    immutableNotificationManager
                )
                return
            }

        }

        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelName = "Channel Name"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val mChannel = NotificationChannel(
                    channelId, channelName, importance
                )
                notificationManager!!.createNotificationChannel(mChannel)
            }
            val notificationBuilder = NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.logo)
                .setColorized(true)
                .setAutoCancel(false)
                .setVibrate(null)
                .setSound(defaultSoundUri)

            if (body != null) {
                notificationBuilder.setContentText(body)
            }
            if (title != null) {
                notificationBuilder.setContentTitle(title)
            } else notificationBuilder.setContentTitle("DoubtNut")

            val dataMap: HashMap<String, String> = HashMap(data)
            val actionHandlerIntent = Intent(this, ActionHandlerActivity::class.java)
            actionHandlerIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            actionHandlerIntent.putExtra(Constants.DATA, dataMap)
            uniqueId = Random().nextInt(10000)

            val pendingIntent = PendingIntent.getActivity(
                this, uniqueId /* Request code */, actionHandlerIntent,
                PendingIntent.FLAG_ONE_SHOT
            )
            notificationBuilder.setContentIntent(pendingIntent)

            val clubAction = getClubAction(data)
            if (FeaturesManager.isFeatureEnabled(
                    this,
                    Features.CLUB_FEED_NOTIFICATION_V2,
                    false
                ) && !clubAction.isNullOrEmpty()
            ) {
                addNotificationAction(notificationBuilder, data, clubAction.hashCode())
            }

            if (imageUrl != null && imageUrl != "") {
                val bitmap = BitmapUtils.getBitmapFromUrl(this, imageUrl)
                if (bitmap != null)
                    notificationBuilder.setStyle(
                        NotificationCompat.BigPictureStyle().bigPicture(bitmap)
                    )
            } else {
                notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
            }

            val notification = notificationBuilder.build()
            notification.flags = notification.flags or Notification.FLAG_AUTO_CANCEL
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notification.color =
                    resources.getColor(R.color.buttonColor, applicationContext.theme)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                notification.color = resources.getColor(R.color.buttonColor)
            }

            var notificationID = uniqueId
            if (FeaturesManager.isFeatureEnabled(
                    this,
                    Features.CLUB_FEED_NOTIFICATION_V2,
                    false
                ) && !clubAction.isNullOrEmpty()
            ) {
                notificationID = clubAction.hashCode()
            }

            try {
                notificationManager!!.notify(notificationID, notification)
            } catch (e: Throwable) {
                // https://console.firebase.google.com/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/c7527ad15b9b3839f533eb29c5848515?time=last-seven-days&sessionEventKey=60ED5A4D02500001406244445ECAC3F3_1562784238762132481
                // Fatal Exception: java.lang.OutOfMemoryError: Failed to allocate a 12975016 byte allocation with 1007077 free bytes and 983KB until OOM
                with(FirebaseCrashlytics.getInstance()) {
                    log("Notification Title: $title")
                    log("Notification body: $body")
                    log("Notification data: $data")
                    setCustomKey(ErrorConstants.DN_FATAL, true)
                    recordException(e)
                }
            }
        }
    }

    private fun addNotificationAction(
        notificationBuilder: NotificationCompat.Builder,
        data: Map<String, String>,
        notificationId: Int
    ) {
        if (data != null && data[Constants.DATA] != null) {
            val extraData = JSONObject(data[Constants.DATA])

            val dataMap: HashMap<String, String?> = HashMap(data)
            dataMap[Constants.NOTIFICATION_ID] = "$notificationId"
            if (extraData.has(Constants.POST_ID)) {
                dataMap[Constants.POST_ID] = extraData.getString(Constants.POST_ID)
            }
            if (extraData.has(Constants.COMMENT_COUNT)) {
                dataMap[Constants.COMMENT_COUNT] = extraData.getString(Constants.COMMENT_COUNT)
            }
            if (extraData.has(Constants.FEED_TRIGGER)) {
                dataMap[Constants.FEED_TRIGGER] = extraData.getString(Constants.FEED_TRIGGER)
            }
            if (extraData.has(Constants.CLUB_ACTION)) {
                dataMap[Constants.CLUB_ACTION] = extraData.getString(Constants.CLUB_ACTION)
            }
            if (extraData.has(Constants.COMMENT_ID)) {
                dataMap[Constants.COMMENT_ID] = extraData.getString(Constants.COMMENT_ID)
            }
            val contentActionEvent = dataMap[Constants.NOTIFICATION_EVENT]
            //Mute Action
            dataMap[Constants.NOTIFICATION_EVENT] = Constants.MUTE_POST
            dataMap[EventConstants.ACTION] = Constants.MUTE_POST
            val muteIntent = Intent(this, NotificationActionReceiver::class.java)
            muteIntent.putExtra(Constants.DATA, dataMap)
            uniqueId = Random().nextInt(10000)
            val pendingIntent =
                PendingIntent.getBroadcast(this, uniqueId, muteIntent, PendingIntent.FLAG_ONE_SHOT)
            notificationBuilder.addAction(
                NotificationCompat.Action(
                    0,
                    Constants.MUTE,
                    pendingIntent
                )
            )

            ///Comment Action
            dataMap[Constants.NOTIFICATION_EVENT] = contentActionEvent
            dataMap[EventConstants.ACTION] = Constants.COMMENT
            val intent = Intent(this, ActionHandlerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.putExtra(Constants.DATA, dataMap)
            uniqueId = Random().nextInt(10000)
            val commentPendingIntent = PendingIntent.getActivity(
                this,
                uniqueId /* Request code */,
                intent,
                PendingIntent.FLAG_ONE_SHOT
            )
            notificationBuilder.addAction(
                NotificationCompat.Action(
                    0,
                    Constants.COMMENT,
                    commentPendingIntent
                )
            )
        }
    }

    private fun convertArrayMapToHashMap(
        data: Map<String, String>,
        hashMap: HashMap<String, String>
    ) {
        data.forEach {
            hashMap[it.key] = it.value
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendNotificationReceivedEvent(data: Map<String, String>, source: String) {

        var feedTrigger: String? = null
        val params: HashMap<String, Any> = hashMapOf(
            EventConstants.SOURCE to source,
            EventConstants.PARAM_TIMESTAMP to System.currentTimeMillis()
        )
        var notifId: String = "NA"
        if (source == Constants.MOENGAGE_NOTIFICATION) {
            notifId = data.getOrDefault("dn_notif_id", notifId)
        }
        //params for feed notifications
        if (data != null && data[Constants.DATA] != null) {
            val extraData = JSONObject(data[Constants.DATA])
            if (extraData.has("notification_id")) notifId = extraData["notification_id"].toString()
            if (extraData.has(Constants.FEED_TRIGGER)) {
                feedTrigger = extraData.getString(Constants.FEED_TRIGGER)
                if (!feedTrigger.isNullOrEmpty()) {
                    params[Constants.FEED_TRIGGER] = feedTrigger
                }
            }
            if (extraData.has(Constants.TYPE)) {
                val type = extraData.getString(Constants.TYPE)
                if (!type.isNullOrEmpty()) {
                    params[Constants.TYPE] = type
                }
            }
        }
        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishEvent(
            AnalyticsEvent(
                EventConstants.EVENT_NOTIFICATION_VIEW,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, source)
                    put(Constants.NOTIFICATION_ID, notifId)
                }, ignoreFirebase = false, ignoreBranch = true, ignoreMoengage = false
            )
        )
        sendNotificationReceivedSnowplowEvent(EventConstants.NOTIFICATION_RECEIVED, params)
        sendMessageReceivedFirebaseEvent(
            EventConstants.NOTIFICATION_RECEIVED, source, feedTrigger
                ?: "NA"
        )
    }

    private fun sendNotificationReceivedSnowplowEvent(
        eventName: String,
        params: HashMap<String, Any>
    ) {
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

    private fun sendMessageReceivedFirebaseEvent(
        eventName: String,
        source: String,
        feedTrigger: String
    ) {
        applicationContext?.apply {
            getTracker().addEventNames(eventName)
                .addStudentId(
                    defaultPrefs(this@FCMMessagingService).getString(
                        Constants.STUDENT_ID,
                        ""
                    ) as String
                )
                .addEventParameter(EventConstants.SOURCE, source)
                .addEventParameter(Constants.FEED_TRIGGER, feedTrigger)
                .track()
        }
    }

    private fun sendEventForNotificationCreation() {
        applicationContext?.apply {
            getTracker().addEventNames(EventConstants.NOTIFICATION_RECEIVED)
                .addNetworkState(NetworkUtils.isConnected(this@FCMMessagingService).toString())
                .addStudentId(
                    defaultPrefs(this@FCMMessagingService).getString(
                        Constants.STUDENT_ID,
                        ""
                    ) as String
                )
                .addScreenName(EventConstants.PAGE_NOTIFICATION)
                .track()
        }
    }

    private fun getClubAction(data: Map<String, String>): String? {
        if (data != null && data[Constants.DATA] != null) {
            val extraData = JSONObject(data[Constants.DATA])
            return Utils.getStringFormJsonObject(extraData, Constants.CLUB_ACTION)
        }
        return null
    }
}
