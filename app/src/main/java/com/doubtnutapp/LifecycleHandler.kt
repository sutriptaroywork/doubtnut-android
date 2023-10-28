package com.doubtnutapp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.isRunning
import com.doubtnutapp.EventBus.ApplicationStateEvent
import com.doubtnutapp.EventBus.PipWindowCloseEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.deeplink.DeeplinkActionHelper
import com.doubtnutapp.utils.SupportsPictureInPictureMode
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.Lazy
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by Anand Gaurav on 16/08/20.
 */
@Singleton
class LifecycleHandler @Inject constructor() : Application.ActivityLifecycleCallbacks {

    private var checkApplicationState: Handler? = null

    private var applicationStateRunnable: Runnable? = null

    private var foregroundActivities = 0

    private var isApplicationInForeground = false

    private var UNIQUE_LOG_TAG = ""

    private var wasAnyActivityStarted = false

    private var enteredTimeStamp: Long = System.currentTimeMillis()

    var activityResumed: WeakReference<Activity>? = null

    @Inject
    lateinit var analyticsPublisher: Lazy<AnalyticsPublisher>

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var deeplinkActionHelper: DeeplinkActionHelper

    private val activityResumedMap: HashMap<String, WeakReference<Activity>> = hashMapOf()

    private val setFirebaseCustomKeys by lazy {
        FirebaseCrashlytics.getInstance().apply {
            setCustomKey("selected_board", UserUtil.getUserBoard())
            setCustomKey("selected_exam", UserUtil.getUserExams())
            setCustomKey("student_id", UserUtil.getStudentId())
            setCustomKey("student_class", UserUtil.getStudentClass())
            setCustomKey("student_language", UserUtil.getUserLanguage())
        }
        true
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        FirebaseCrashlytics.getInstance().setCustomKey(
            Constants.LIFECYCLE_ON_CREATE, activity.localClassName
        )
        Log.d("setFirebaseCustomKeys: $setFirebaseCustomKeys")
    }

    override fun onActivityStarted(activity: Activity) {
        FirebaseCrashlytics.getInstance()
            .setCustomKey(Constants.LIFECYCLE_ON_START, activity.localClassName)
        wasAnyActivityStarted = true
        if (activity !is SupportsPictureInPictureMode) {
            // We don't want our PIP window inside our app. There's no easy way to close a PIP window.
            // We'll close it by sending a local broadcast and listen for it in the PIP activity.
            DoubtnutApp.INSTANCE.bus()?.send(PipWindowCloseEvent)
        }
    }

    override fun onActivityResumed(activity: Activity) {
        activityResumed = WeakReference(activity)
        activityResumedMap[activity.javaClass.simpleName] = WeakReference(activity)

        deeplinkActionHelper.inAppPopupResponsePair?.let {
            deeplinkActionHelper.handleData(deeplinkAction)
        }

        FirebaseCrashlytics.getInstance().setCustomKey(
            Constants.LIFECYCLE_ON_RESUME, activity.localClassName
        )
        if (!isApplicationInForeground) {
            isApplicationInForeground = true
            val udid = Utils.getUDID()
            UNIQUE_LOG_TAG = "id_" + udid + "_" + System.currentTimeMillis().toString()
            val timeStamp = System.currentTimeMillis()
            val params = hashMapOf<String, Any>().apply {
                put(EventConstants.STATUS, EventConstants.APPLICATION_ENTERED_FOREGROUND)
                put(EventConstants.EVENT_NAME_ID, UNIQUE_LOG_TAG)
                put(EventConstants.EVENT_UDID, udid)
                put(EventConstants.TIME_STAMP, timeStamp)
            }
            analyticsPublisher.get().publishEvent(
                StructuredEvent(
                    EventConstants.APPLICATION_STATE,
                    EventConstants.APPLICATION_ENTERED_FOREGROUND_ACTION,
                    eventParams = params
                )
            )
            DoubtnutApp.INSTANCE.bus()?.send(ApplicationStateEvent(true))
            enteredTimeStamp = timeStamp
        }
        foregroundActivities++
    }

    override fun onActivityPaused(activity: Activity) {
        FirebaseCrashlytics.getInstance()
            .setCustomKey(Constants.LIFECYCLE_ON_PAUSE, activity.localClassName)
        activityResumedMap.remove(activity.javaClass.simpleName)

        foregroundActivities--
        wasAnyActivityStarted = false
        checkApplicationState = Handler()
        val source = activity.localClassName
        applicationStateRunnable = object : Runnable {
            override fun run() {
                try {
                    if (!wasAnyActivityStarted && foregroundActivities == 0) {
                        val timeStamp = System.currentTimeMillis()
                        val params = hashMapOf<String, Any>().apply {
                            put(
                                EventConstants.STATUS,
                                EventConstants.APPLICATION_ENTERED_BACKGROUND
                            )
                            put(EventConstants.EVENT_NAME_ID, UNIQUE_LOG_TAG)
                            put(EventConstants.EVENT_UDID, Utils.getUDID())
                            put(EventConstants.TIME_STAMP, timeStamp)
                        }
                        analyticsPublisher.get().publishEvent(
                            StructuredEvent(
                                EventConstants.APPLICATION_STATE,
                                EventConstants.APPLICATION_ENTERED_BACKGROUND_ACTION,
                                eventParams = params
                            )
                        )
                        analyticsPublisher.get().publishEvent(
                            AnalyticsEvent(
                                EventConstants.EVENT_NAME_APP_EXIT_DN,
                                hashMapOf(EventConstants.SOURCE to source),
                                ignoreSnowplow = true
                            )
                        )
                        UNIQUE_LOG_TAG = ""
                        isApplicationInForeground = false
                        DoubtnutApp.INSTANCE.bus()?.send(ApplicationStateEvent(false))
                        val timeSpentInMillis = timeStamp - enteredTimeStamp
                        var totalTimeSpentInMinute = UserUtil.getTotalTimeSpentInMinute()
                        if (timeSpentInMillis > 0) {
                            val timeSpentInMin = TimeUnit.MILLISECONDS.toMinutes(timeSpentInMillis)
                            if (timeSpentInMin < 500) {
                                totalTimeSpentInMinute += TimeUnit.MILLISECONDS.toMinutes(
                                    timeSpentInMillis
                                )
                                if (totalTimeSpentInMinute != 0L && totalTimeSpentInMinute < 9999999999999999)
                                    UserUtil.putTimeSpentInMinute(totalTimeSpentInMinute)
                            }
                        }
                        val installationDays = Utils.getInstallationDays()
                        if (installationDays <= 3 && totalTimeSpentInMinute >= 50) {
//                            analyticsPublisher.get()
//                                .publishBranchIoEvent(AnalyticsEvent(EventConstants.D3E40))
                        }
                        if (installationDays <= 5 && totalTimeSpentInMinute >= 80) {
//                            analyticsPublisher.get()
//                                .publishBranchIoEvent(AnalyticsEvent(EventConstants.D5E80))
                        }
                    }
                } finally {
                    checkApplicationState?.removeCallbacks(this)
                }
            }
        }
        checkApplicationState?.postDelayed(applicationStateRunnable ?: return, 100)
    }

    override fun onActivityStopped(activity: Activity) {
        FirebaseCrashlytics.getInstance().setCustomKey(
            Constants.LIFECYCLE_ON_STOP, activity.localClassName
        )
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {
        FirebaseCrashlytics.getInstance().setCustomKey(
            Constants.LIFECYCLE_ON_DESTROY, activity.localClassName
        )
    }

    fun getActivity(page: String?): Activity? {
        val activity = when (page) {
            "HOME_PAGE" -> activityResumedMap["MainActivity"]?.get()
            "CAMERA_PAGE" -> activityResumedMap["CameraActivity"]?.get()
            "PAYMENT_PAGE" -> activityResumedMap["PaymentActivity"]?.get()
            "COURSE_PAGE" -> activityResumedMap["CourseActivityV3"]?.get()
            else -> activityResumedMap[page]?.get()
        }
        if (activity.isRunning()) return activity
        return null
    }

}