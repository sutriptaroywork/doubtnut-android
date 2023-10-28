package com.doubtnutapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import androidx.core.content.edit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.core.worker.ISyncConfigDataWorker
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.camerascreen.datasource.LocalConfigDataSource
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.domain.homefeed.interactor.ConfigData
import com.doubtnutapp.login.ui.activity.LanguageActivity
import com.doubtnutapp.login.ui.activity.StudentLoginActivity
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.packageInstallerCheck.CheckForPackageInstall
import com.doubtnutapp.ui.base.BaseActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.onboarding.OnBoardingStepsActivity
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.facebook.appevents.AppEventsConstants
import dagger.android.AndroidInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@SuppressLint("HardwareIds")
class SplashActivity : BaseActivity() {

    private var launchDelay: Long = 750 /*500 msec*/

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var userPreference: UserPreference

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var defaultDatStore: DefaultDataStore

    @Inject
    lateinit var checkForPackageInstall: CheckForPackageInstall

    private lateinit var viewModel: SplashViewModel
    lateinit var eventTracker: Tracker
    private val handler: Handler = Handler()

    private var deeplink: String? = null

    private val runnable: Runnable = Runnable {
        moveForward()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        if (UserUtil.getIsAnonymousLogin()) {
            userPreference.logOutUser()
        }
        viewModel = viewModelProvider(viewModelFactory)
        viewModel.getTrueCallerLoginWaitTime()

        if (userPreference.getUserLoggedIn()) {
            getConfigData()
            incrementSessionCount()
        } else {
            ISyncConfigDataWorker.syncConfigData = true
        }

        initializeAppStatusToMoEngage()
        initializeSessionForAudio()
        CountingManager.updateAppOpenCount(applicationContext)
        FeaturesManager.updateFeatureConfig()

        eventTracker = getTracker()
        DoubtnutApp.INSTANCE.handleStickyNotification()
        val studentId = defaultPrefs(this@SplashActivity).getString(Constants.STUDENT_ID, "")
        if (!studentId.isNullOrBlank()) {
            //it will update the Fcm reg iff it was not updated
            viewModel.updateFCMRegId()

            BranchIOUtils.clearReferringParam(applicationContext)
        }

        // Reset Ignore for StudyDostWidget
        defaultPrefs().edit {
            putBoolean(Constants.IGNORE_STUDY_DOST, false)
        }

        defaultPrefs().edit().putBoolean(Constants.IS_COURSE_SELECTION_SHOWN, false).apply()
        defaultPrefs().edit().putString(Constants.SELECTED_CATEGORY_ID, "").apply()
        defaultPrefs().edit().putString(Constants.SELECTED_ASSORTMENT_ID, "").apply()

        viewModel.updateConfigValues()

        if (!studentId.isNullOrBlank()) {
            viewModel.updateNotificationCount()
        }

        viewModel.registerDailyAttendanceEvent()

        defaultPrefs(this).edit {
            putString(Constants.APP_VERSION, BuildConfig.VERSION_NAME)
        }

        UXCamUtil.init()

        Utils.sendClassLangEvents(EventConstants.EVENT_NAME_APP_OPEN_DN)
        Utils.sendClassLanguageSpecificEvent(EventConstants.EVENT_NAME_APP_OPEN_DN)

        defaultPrefs(this).edit().putBoolean(Constants.SHOW_BOTTOM_SHEET, true).apply()
        ApxorUtils.logAppEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, Attributes().apply {
            putAttribute(EventConstants.SOURCE, EventConstants.EVENT_NAME_DEFAULT)
        })

        DoubtnutApp.INSTANCE.getEventTracker()
            .addEventNames(EventConstants.EVENT_NAME_APP_OPEN_DN)
            .addEventParameter(EventConstants.SOURCE, EventConstants.EVENT_NAME_DEFAULT)
            .addStudentId(getStudentId())
            .track()

//        DoubtnutApp.INSTANCE.analyticsPublisher.get().publishBranchIoEvent(
//            AnalyticsEvent(EventConstants.EVENT_NAME_APP_OPEN_DN, hashMapOf<String, Any>().apply {
//                put(EventConstants.SOURCE, EventConstants.EVENT_NAME_DEFAULT)
//            })
//        )

        // Send to snowplow
        viewModel.publishEvent(StructuredEvent(
            category = EventConstants.EVENT_NAME_APP_OPEN_DN_CATEGORY,
            action = EventConstants.EVENT_NAME_APP_OPEN_DN,
            eventParams = hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, EventConstants.EVENT_NAME_DEFAULT)
            }
        ))

        lifecycleScope.launchWhenStarted {
            if (defaultDatStore.isNewUser.firstOrNull() == true) {
                sendNewBranchEvent()
            }
        }
        setUpObservers()
        handleDeeplink()
        if (userPreference.getUserLoggedIn()) {
            viewModel.storeAppOpenCountOnBackend()
            // DNR region start
            viewModel.markAppOpenForDnrRewards()
            // DNR region end
        }
        userPreference.setAppExitDialogShownInCurrentSession(false)
        userPreference.setCameraScreenNavigationDataFetchedInCurrentSession(false)

        getHomePageData()
    }

    private fun initializeSessionForAudio() {
        defaultPrefs().edit()
            .putInt(Constants.SCREEN_CAMERA_SESSION, 0).commit()
        defaultPrefs().edit()
            .putInt(Constants.SCREEN_CAMERA_RETURN_SESSION, 0).commit()
        defaultPrefs().edit()
            .putInt(Constants.SCREEN_LOADING_SESSION, 0).commit()
        defaultPrefs().edit()
            .putInt(Constants.SCREEN_CROP_SESSION, 0).commit()
        defaultPrefs().edit()
            .putInt(Constants.SCREEN_MATCH_PAGE_SESSION, 0).commit()
    }

    private fun getHomePageData() {
        // Clear cached home page data if app updated
        if (defaultPrefs().getInt(
                Constants.LAST_BUILD_VERSION_CODE,
                -1
            ) != BuildConfig.VERSION_CODE
        ) {
            defaultPrefs().edit {
//                putBoolean(Constants.HOME_PAGE_DATA_INVALIDATED, true)
//                putString(Constants.HOME_PAGE_DATA, "")
                putString(Constants.FIRST_PAGE_DEEPLINK, "")
            }
        }
        defaultPrefs().edit {
            putInt(Constants.LAST_BUILD_VERSION_CODE, BuildConfig.VERSION_CODE)
        }

//        if (userPreference.getUserLoggedIn()
//            && defaultPrefs().getBoolean(Constants.ONBOARDING_COMPLETED, false)
//        ) {
//            viewModel.getDoubtFeedStatus()
//            viewModel.fetchHomeFeed()
//        }
        defaultPrefs().edit {
            remove(Constants.HOME_PAGE_DATA)
        }
    }

    private fun incrementSessionCount() {
        var sessionCount = defaultPrefs().getInt(Constants.SESSION_COUNT, 1)
        if (sessionCount < 1000000) {
            defaultPrefs().edit().putInt(Constants.SESSION_COUNT, ++sessionCount).apply()
        }
    }

    private fun getConfigData() {
        val postPurchaseSessionCount =
            defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0)
        val sessionCount = defaultPrefs().getInt(Constants.SESSION_COUNT, 1)
        viewModel.getConfigData(sessionCount, postPurchaseSessionCount)
    }

    private fun handleDeeplink() {
        deeplink = intent?.data?.toString()
        if (TextUtils.isEmpty(deeplink)) deeplink = intent.getStringExtra(Constants.DEEPLINK_URI)
        if (deeplink != null) {
            if (deeplinkAction.performAction(
                    this,
                    deeplink!!,
                    Bundle().apply { putBoolean(Constants.CLEAR_TASK, true) })
            ) {
                finish()
            }
        }
        Utils.saveIsEmulatorAndSafetyNetResponseToPref()
    }

    private fun setUpObservers() {

        viewModel.onBoardingStatus.observeK(this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            {})

        viewModel.configLiveData.observeK(this,
            ::onConfigDataSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            {})
    }

    private fun onConfigDataSuccess(data: ConfigData) {
        var postPurchaseSessionCount =
            defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0)
        if (postPurchaseSessionCount in 1..1000000) {
            defaultPrefs().edit()
                .putInt(Constants.POST_PURCHASE_SESSION_COUNT, ++postPurchaseSessionCount).apply()
        }
        DoubtnutApp.INSTANCE.onboardingList = data.onboardingList
        DoubtnutApp.INSTANCE.isOnboardingStarted = data.onboardingList?.isNotEmpty() ?: false
        ConfigUtils.saveToPref(data)
        if (data.appDataCollect == 1) {
            sendAllInstalledData()
        }
    }

    private fun sendNewBranchEvent() {
        val timeDiff = Utils.getInstallationDays(baseContext)
        if (timeDiff in 3..7 && userPreference.getThreeVideosWatchedInTwoDays() && userPreference.getLastDayBranchEventSend() !in 3..7) {
//            analyticsPublisher.publishBranchIoEvent(
//                AnalyticsEvent(
//                    EventConstants.DV3D3_APP_OPEN_AFTER_DAY2,
//                    hashMapOf()
//                )
//            )
            userPreference.putLastDayBranchEventSend(timeDiff)
        }
    }

    private fun onSuccess(status: Boolean) {
        defaultPrefs(this).edit {
            putBoolean(Constants.ONBOARDING_COMPLETED, status)
        }
        handler.postDelayed(runnable, launchDelay)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
        handler.postDelayed(runnable, launchDelay)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun initializeAppStatusToMoEngage() {
        MoEngageUtils.initializeAppStatusToMoEngage(
            applicationContext,
            CountingManager.getAppOpenCount(this) == 0
        )
        if (MoEngageUtils.isUserInstallingUpdate(applicationContext)) {
            MoEngageUtils.sendPushToken(
                applicationContext, defaultPrefs(this).getString(Constants.GCM_REG_ID, "")
                    ?: ""
            )
        }
        MoEngageUtils.updateVersionCodeInPref(applicationContext)
    }

    override fun onStart() {
        super.onStart()
        BranchIOUtils.initSession(this@SplashActivity, this.intent.data)
        LocaleManager.setLocale(this)
        if (defaultPrefs(this).getString(Constants.DEVICE_NAME, "").isNullOrBlank()) {
            val deviceName = Utils.getDeviceName()
            defaultPrefs(this).edit {
                putString(Constants.DEVICE_NAME, deviceName)
            }
        }

        val studentId = defaultPrefs(this@SplashActivity).getString(Constants.STUDENT_ID, "")

        if (!studentId.isNullOrBlank()) {
            launchDelay = 250
        }

        BranchIOUtils
            .setIdentity(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))

        handleInstallEvent()
        val countToSendEvent: Int =
            Utils.getCountToSend(RemoteConfigUtils.getEventInfo(), EventConstants.SESSION_START)
        repeat((0 until countToSendEvent).count()) {
            sendEvent(EventConstants.SESSION_START)
        }

        val countToSendSplashStateEvent: Int =
            Utils.getCountToSend(
                RemoteConfigUtils.getEventInfo(),
                EventConstants.EVENT_NAME_SCREEN_STATE_SPLASH
            )
        repeat((0 until countToSendSplashStateEvent).count()) {
            sendEventForFirstSession(EventConstants.EVENT_NAME_SCREEN_STATE_SPLASH)
        }

        sendEventForFirstSessionCleverTap(EventConstants.EVENT_NAME_SCREEN_STATE_SPLASH)
        updateUserDetails()
    }

    public override fun onResume() {
        super.onResume()
        sendPreLoginOnboardData()
        sendApxorCustomAttributes()
        if ((defaultPrefs(this).getString(
                Constants.STUDENT_LOGIN,
                "false"
            ) == "true") && !defaultPrefs(this).getBoolean(Constants.ONBOARDING_COMPLETED, false)
        ) {
            viewModel.getOnBoardingStatus()
        } else {
            handler.postDelayed(runnable, launchDelay)
        }
        defaultPrefs().edit {
            putBoolean(Constants.HAS_UPI, hasAnyUpiApp())
        }
    }

    private fun sendApxorCustomAttributes() {
        val sessionInfo = Attributes()
        sessionInfo.putAttribute("network", NetworkUtils.getNetworkClass(this))
        sessionInfo.putAttribute(
            "ncert_re_entry",
            defaultPrefs(this).getBoolean(
                LocalConfigDataSource.PREF_KEY_IS_NCERT_RE_ENTRY_ENABLED,
                false
            )
        )
        sessionInfo.putAttribute(
            "bounty_enabled",
            defaultPrefs(this).getBoolean(LocalConfigDataSource.PREF_KEY_IS_BOUNTY_ENABLED, false)
        )
        ApxorUtils.setSessionCustomInfo(sessionInfo)
    }

    private fun moveForward() {
        if (defaultPrefs(this).getString(Constants.STUDENT_LOGIN, "false") != "true") {
            val languageCode = defaultPrefs().getString(Constants.STUDENT_LANGUAGE_CODE, null)
            viewModel.publishEvent(
                AnalyticsEvent(
                    EventConstants.EXPERIMENT_LOGIN_V3,
                    hashMapOf<String, Any>().apply {
                        put(Constants.LOGIN_VARIANT, LOGIN_VARIANT)
                    },
                    ignoreSnowplow = true
                )
            )
            defaultPrefs(this).edit {
                putInt(Constants.LOGIN_VARIANT, LOGIN_VARIANT)
            }

            if (languageCode != null) {
                StudentLoginActivity.getStartIntent(this)
            } else {
                LanguageActivity.getStartIntent(this)
            }.apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
            finish()
        } else if (!defaultPrefs(this).getBoolean(Constants.ONBOARDING_COMPLETED, false)) {
            OnBoardingStepsActivity.getStartIntent(this).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this)
            }
            finish()
        } else {

            viewModel.getAppWideBottomSheetTabsData()

            startActivity(MainActivity.getStartIntent(this))

            if (!defaultPrefs(this).getBoolean(Constants.IS_MOENGAGE_UNIQUE_ID_SET, false)) {
                MoEngageUtils.setUniqueId(applicationContext, getStudentId())
            }

            updateUserDetails()
            finish()
        }
    }

    private fun getTracker(): Tracker {
        val doubtnutApp = this@SplashActivity.applicationContext as DoubtnutApp
        return doubtnutApp.getEventTracker()
    }

    private fun sendEvent(eventName: String) {
        this@SplashActivity.apply {
            eventTracker.addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@SplashActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_SPLASH)
                .track()
        }
    }

    private fun sendEventForFirstSession(@Suppress("SameParameterValue") eventName: String) {
        this@SplashActivity.apply {
            eventTracker.addEventNames(eventName)
                .addScreenName(EventConstants.STATE_OPEN)
                .addNetworkState(NetworkUtils.isConnected(this@SplashActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_SPLASH)
                .track()

        }
    }

    private fun sendEventForFirstSessionCleverTap(@Suppress("SameParameterValue") eventName: String) {
        this@SplashActivity.apply {
            eventTracker.addEventNames(eventName)
                .addScreenName(EventConstants.STATE_OPEN)
                .addNetworkState(NetworkUtils.isConnected(this@SplashActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_SPLASH)
                .track()

        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
        Log.v(this.javaClass.simpleName, "onPause()")
    }

    @SuppressLint("CheckResult")
    private fun updateUserDetails() {
        viewModel
            .userDetailsData(applicationContext)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()
    }

    private fun sendPreLoginOnboardData() {
        if (defaultPrefs(this).getString(Constants.STUDENT_LOGIN, "false") != "true") {
            viewModel.sendPreLoginOnboardData(applicationContext)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe()
        }
    }

    @SuppressLint("CheckResult")
    private fun sendAllInstalledData() {
        CoreApplication.INSTANCE.runOnDifferentThread {
            viewModel
                .getAllInstalledApps(applicationContext)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe()
        }
    }

    private fun handleInstallEvent() {
        if (!defaultPrefs(this).getBoolean(Constants.IS_INSTALL_EVENT_SENT, false)) {
            defaultPrefs(this).edit().putBoolean(Constants.IS_INSTALL_EVENT_SENT, true).apply()
            val countToSendEvent: Int =
                Utils.getCountToSend(RemoteConfigUtils.getEventInfo(), EventConstants.APP_INSTALL)
            repeat((0 until countToSendEvent).count()) {
                sendEvent(EventConstants.APP_INSTALL)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        AppEventsConstants.EVENT_NAME_CONTACT,
                        hashMapOf()
                    )
                )
            }
        }
    }

    companion object {
        private const val LOGIN_VARIANT: Int = 2
    }

    private fun hasAnyUpiApp(): Boolean {
        val upiAppsPackages = listOf(
            "net.one97.paytm",
            "com.phonepe.app",
            "com.google.android.apps.nbu.paisa.user",
            "in.org.npci.upiapp",
            "in.amazon.mShop.android.shopping"
        )
        upiAppsPackages.forEach {
            if (checkForPackageInstall.appInstalled(it)) {
                return true
            }
        }
        return false
    }
}