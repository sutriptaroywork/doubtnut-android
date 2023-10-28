package com.doubtnutapp

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.apxor.androidsdk.core.ApxorSDK
import com.doubtnut.analytics.Tracker
import com.doubtnut.analytics.debug.AnalyticsInterceptor
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.data.QuizNotificationDataStore
import com.doubtnut.core.data.QuizNotificationDatastoreImpl
import com.doubtnut.core.utils.ThreadUtils
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.widgets.IWidgetLayoutAdapter
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.clients.FirebaseTrackerClient
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.di.component.DaggerDoubtnutAppComponent
import com.doubtnutapp.di.component.DoubtnutAppComponent
import com.doubtnutapp.domain.homefeed.interactor.OnboardingData
import com.doubtnutapp.gamification.popactivity.GamificationPopupManager
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.notificationmanager.StickyNotificationManager
import com.doubtnutapp.notificationmanager.VideoStickyNotificationManager
import com.doubtnutapp.ui.quiz.QuizJobCreator
import com.doubtnutapp.utils.BranchIOUtils
import com.doubtnutapp.videoPage.model.VideoStickyNotificationData
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.evernote.android.job.JobManager
import com.freshchat.consumer.sdk.Freshchat
import com.google.firebase.messaging.FirebaseMessaging
import com.instacart.library.truetime.TrueTimeRx
import com.snowplowanalytics.snowplow.Snowplow
import com.snowplowanalytics.snowplow.configuration.NetworkConfiguration
import com.snowplowanalytics.snowplow.configuration.SubjectConfiguration
import com.snowplowanalytics.snowplow.configuration.TrackerConfiguration
import com.snowplowanalytics.snowplow.tracker.LogLevel
import dagger.Lazy
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val BUILD_TYPE_RELEASE = "release"
const val BUILD_TYPE_ALPHA = "alpha"

class DoubtnutApp :
    CoreApplication(),
    androidx.work.Configuration.Provider {

    override fun getWorkManagerConfiguration() =
        androidx.work.Configuration.Builder()
            .build()

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    @Inject
    lateinit var gamificationPopupManager: GamificationPopupManager

    var daggerAppComponent: DoubtnutAppComponent? = null

    @Inject
    lateinit var tracker: com.doubtnut.analytics.Tracker

    @Inject
    lateinit var analyticsPublisher: Lazy<AnalyticsPublisher>

    private var notificationManager: NotificationManager? = null

    @Inject
    lateinit var lifecycleHandler: LifecycleHandler

    var isRatingDialogStarted = false
    var isInAppDialogShowing = false
    var onboardingList: List<OnboardingData>? = listOf()
    var isOnboardingStarted: Boolean = false
    var isOnboardingCompleted: Boolean = false

    @Inject
    lateinit var quizNotificationDatastore: QuizNotificationDataStore

    private val lifecycleListener: LifecycleListener by lazy {
        LifecycleListener(applicationContext)
    }

    companion object {
        lateinit var INSTANCE: DoubtnutApp
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this

        initialiseDaggerComponent()
        setupLifecycleListener()

        RxJavaPlugins.setErrorHandler { }

        StethoUtils.install(this)

        // https://stackoverflow.com/a/45710089/2437655
        if (0 != (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        ThreadUtils.runOnAnalyticsThread {
            initApxor()
        }
        BranchIOUtils.init(this)
        feedGCM()
        initTrueTime()
        addTrackerClients()

        setQuizNotificationAndroidJob()

        registerActivityLifecycleCallbacks(gamificationPopupManager)
        FirebaseLibUtils.init()
        initializeMoEngage()
        registerBroadcastReceiver()
        LocaleManager.setLocale(this)
        ThreadUtils.runOnAnalyticsThread {
            initSnowplowTracker()
        }
        AnalyticsInterceptor.init(this)

        registerActivityLifecycleCallbacks(lifecycleHandler)
        isRatingDialogStarted = false
        isInAppDialogShowing = false
    }

    override fun getWidgetLayoutAdapter(context: Context): IWidgetLayoutAdapter {
        return WidgetLayoutAdapter(context)
    }

    override fun getWidgetLayoutAdapter(
        context: Context,
        actionPerformer: ActionPerformer?,
        source: String?
    ): IWidgetLayoutAdapter {
        return WidgetLayoutAdapter(context, actionPerformer, source)
    }

    private fun initSnowplowTracker() {
        val networkConfig = if (BuildConfig.DEBUG) {
            NetworkConfiguration("http://13.234.119.169:8080")
        } else {
            NetworkConfiguration("https://sp.doubtnut.com")
        }

        val trackerConfig = TrackerConfiguration("student")
            .sessionContext(true)
            .lifecycleAutotracking(true)
            .logLevel(if (BuildConfig.DEBUG) LogLevel.VERBOSE else LogLevel.OFF)
            .platformContext(true)
            .diagnosticAutotracking(BuildConfig.DEBUG)

        val subjectConfiguration = SubjectConfiguration().apply {
            userId = defaultPrefs(applicationContext).getString(Constants.STUDENT_ID, "")
        }

        Snowplow.createTracker(
            applicationContext,
            "doubtnut",
            networkConfig,
            trackerConfig,
            subjectConfiguration
        )
    }

    private fun initializeMoEngage() {
        MoEngageUtils.init(this)
    }

    private fun registerBroadcastReceiver() {
        val intentFilterRestoreID = IntentFilter(Freshchat.FRESHCHAT_USER_RESTORE_ID_GENERATED)
        getLocalBroadcastManager().registerReceiver(restoreIdReceiver, intentFilterRestoreID)
    }

    private var restoreIdReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // TODO: Save this restoreId to app's backend for restoring across platforms and session
            val restoreId = Freshchat.getInstance(applicationContext).user.restoreId
            defaultPrefs().edit {
                putString(Constants.FRESHCHAT_RESTORE_ID, restoreId)
            }
        }
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun getLocalBroadcastManager(): LocalBroadcastManager {
        return LocalBroadcastManager.getInstance(applicationContext)
    }

    fun handleStickyNotification() {
        StickyNotificationManager.handleStickyNotification(this, getNotificationManager())
    }

    fun handleVideoStickyNotification(videoData: VideoStickyNotificationData) {
        VideoStickyNotificationManager.handleStickyNotification(
            this,
            getNotificationManager(),
            videoData
        )
    }

    private fun getNotificationManager(): NotificationManager? {
        if (notificationManager == null) {
            notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        return notificationManager
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleManager.setLocale(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        base?.let { LocaleManager.setLocale(base) }
    }

    private fun initialiseDaggerComponent() {
        daggerAppComponent = DaggerDoubtnutAppComponent
            .builder()
            .application(this)
            .build()
            .also { it.inject(this) }
    }

    fun showPopup(extras: Bundle) {
        gamificationPopupManager.showPopup(extras)
    }

    fun onLiveClassDoubtAnswered(qid: String, commentId: String): Boolean {
        return gamificationPopupManager.onLiveClassDoubtAnswered(qid, commentId)
    }

    private fun initApxor() {
        ApxorSDK.initialize(getString(R.string.apxor_id), this.applicationContext)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    /**
     * Moving initialisation to IO thread and skip running it if called again in 10minutes.
     */
    private fun setQuizNotificationAndroidJob() {
        launch {
            val quizNotificationAndroidJobLastKnownTimestamp =
                quizNotificationDatastore.quizNotificationAndroidJobLastKnownTimestamp.firstOrNull()
            if (quizNotificationAndroidJobLastKnownTimestamp == null ||
                System.currentTimeMillis() - quizNotificationAndroidJobLastKnownTimestamp > TimeUnit.MINUTES.toMillis(
                    10
                )
            ) {
                quizNotificationDatastore.set(
                    QuizNotificationDatastoreImpl.QUIZ_NOTIFICATION_ANDROID_JOB_LAST_KNOWN_TIMESTAMP,
                    System.currentTimeMillis()
                )
                JobManager.create(this@DoubtnutApp).addJobCreator(QuizJobCreator())
            }
        }
    }

    private fun addTrackerClients() {
        tracker.addClient(FirebaseTrackerClient(this))
    }

    override fun getEventTracker(): Tracker {
        return tracker
    }

    override fun getDatabase(): DoubtnutDatabase? = daggerAppComponent?.getRoomDb()

    // true-time for quiz
    @SuppressLint("CheckResult")
    private fun initTrueTime() {
        TrueTimeRx.build()
            .withConnectionTimeout(31428)
            .withLoggingEnabled(true)
            .initializeRx("17.253.84.251")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : DisposableSingleObserver<Date>() {
                override fun onSuccess(date: Date) {
                    Log.d("TrueTimeTag", "Success initialized TrueTime :$date")
                }

                override fun onError(e: Throwable) {
                    Log.e(
                        "TrueTimeTag",
                        "something went wrong when trying to initializeRx TrueTime",
                        e
                    )
                }
            })
    }

    var fcmRegId: String
        get() = defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID, "").orEmpty()
        set(value) = defaultPrefs(applicationContext).edit {
            putString(Constants.GCM_REG_ID, value)
        }

    private fun feedGCM() {
        try {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                if (token.isNullOrEmpty().not()) {
                    fcmRegId = token
                }
            }
        } catch (e: Exception) {
            // Error in feeding GCM, Skipping firebase non-fatal logging as well for the same.
            // https://play.google.com/console/u/0/developers/8879759867892395282/app/4973323575198231399/vitals/crashes/74c08ea7/details?days=7
        }
    }

    fun isRelease(): Boolean {
        return BuildConfig.BUILD_TYPE == BUILD_TYPE_RELEASE || BuildConfig.BUILD_TYPE == BUILD_TYPE_ALPHA
    }

    fun getBaseApiUrl(): String {
        if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {
            return defaultPrefs(this).getString(Constants.ADMIN_API_URL, BuildConfig.BASE_URL_API)!!
        }
        return BuildConfig.BASE_URL_API
    }

    fun getBaseSocketUrl(): String {
        if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {
            return defaultPrefs(this).getString(
                Constants.SOCKET_BASE_URL,
                BuildConfig.BASE_URL_SOCKET
            )!!
        }
        return BuildConfig.BASE_URL_SOCKET
    }

    fun getBaseMicroApiUrl(): String {
        if (BuildConfig.DEBUG || BuildConfig.ENABLE_ADMIN_OPTIONS) {
            return defaultPrefs(this).getString(
                Constants.ADMIN_API_MICRO_URL,
                BuildConfig.BASE_URL_MICRO_API
            )!!
        }
        return BuildConfig.BASE_URL_MICRO_API
    }

    fun publishEvent(event: StructuredEvent) {
        analyticsPublisher.get().publishEvent(event)
    }

    override fun onTerminate() {
        super.onTerminate()
        getLocalBroadcastManager().unregisterReceiver(restoreIdReceiver)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        ToastUtils.makeTextInDev(
            context = this,
            message = "Low Memory",
            duration = Toast.LENGTH_SHORT,
            isAdminEnabled = BuildConfig.ENABLE_ADMIN_OPTIONS
        )?.show()
    }

    private fun setupLifecycleListener() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(lifecycleListener)
    }
}
