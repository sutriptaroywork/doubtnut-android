package com.doubtnutapp.ui.splash

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.core.content.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnut.core.data.LottieAnimDataStore
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.bottomnavigation.repository.BottomNavRepository
import com.doubtnutapp.common.AdIdRetreiver
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.base.di.qualifier.AppVersion
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.repository.UserActivityRepository
import com.doubtnutapp.domain.homefeed.interactor.ConfigData
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.studygroup.service.DnrRepository
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.onboarding.model.ApiLoginTimer
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentId
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject
import kotlin.collections.set

class SplashViewModel @Inject constructor(
    app: Application,
    private val adIdRetreiver: AdIdRetreiver,
    private val userPreference: UserPreference,
    private val compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val networkUtil: NetworkUtil,
    private val splashRepository: SplashRepository,
    private val userActivityRepository: UserActivityRepository,
    @AppVersion private val appVersion: String,
    private val dnrRepository: DnrRepository,
    private val defaultDataStore: DefaultDataStore,
    private val lottieAnimDataStore: LottieAnimDataStore,
    private val bottomNavRepository: BottomNavRepository
) : AndroidViewModel(app) {

    init {
        getId()
    }

    private val _onBoardingStatus: MutableLiveData<Outcome<Boolean>> = MutableLiveData()
    private val _configLiveData: MutableLiveData<Outcome<ConfigData>> = MutableLiveData()

    val onBoardingStatus: LiveData<Outcome<Boolean>>
        get() = _onBoardingStatus

    private val _message: MutableLiveData<String> = MutableLiveData()

    val message: LiveData<String>
        get() = _message

    val configLiveData: LiveData<Outcome<ConfigData>>
        get() = _configLiveData

    @SuppressLint("CheckResult")
    fun updateFCMRegId() {

        if (isFcmRegIdUpdated()) return

        val applicationContext = getApplication<DoubtnutApp>().applicationContext

        val params: HashMap<String, Any> = HashMap()
        params[Constants.KEY_GCM_REG_ID] =
            defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID, "").orDefaultValue()
        params[Constants.KEY_STUDENT_ID] = getStudentId()

        if (!defaultPrefs(applicationContext).getString(Constants.XAUTH_HEADER_TOKEN, "")
                .isNullOrBlank()
        ) {
            DataHandler.INSTANCE.studentsRepositoryv2.updateUserProfileObservable(params.toRequestBody())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    if (DoubtnutApp.INSTANCE.fcmRegId.isEmpty()) return@subscribe
                    defaultPrefs(applicationContext).edit {
                        putBoolean(Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER, true)
                    }
                }, {})

        }
    }

    private fun isFcmRegIdUpdated() = defaultPrefs(getApplication()).getBoolean(
        Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER,
        false
    )

    fun getConfigData(sessionCount: Int, postPurchaseSessionCount: Int) {
        _configLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.appConfigRepository.getConfigData(
                    sessionCount,
                    postPurchaseSessionCount
                )
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _configLiveData.value = Outcome.loading(false)
                        _configLiveData.value = Outcome.success(it)
                    }, {
                        _configLiveData.value = Outcome.loading(false)
                    })
    }

    /**
     * save lottie animation file urls into datastore
     * @param lottieUrls - map contains key - screen and value - url
     */
    private fun saveLottieUrls(lottieUrls: Map<String, String>?) {
        viewModelScope.launch(Dispatchers.IO) {
            lottieUrls?.forEach {
                val key = it.key
                val value = it.value
                lottieAnimDataStore.set(stringPreferencesKey(key), value)
            }
        }
    }

    @SuppressLint("HardwareIds")
    fun userDetailsData(context: Context): Completable {
        val params: HashMap<String, Any> = HashMap()
        params["device"] =
            defaultPrefs(context).getString(Constants.DEVICE_NAME, "").orDefaultValue()
        params["latitude"] =
            defaultPrefs(context).getString(Constants.DEVICE_LATITUDE, "0").orDefaultValue("0")
        params["longitude"] =
            defaultPrefs(context).getString(Constants.DEVICE_LONGITUDE, "0").orDefaultValue("0")
        params["udid"] = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ).toString()
        return DataHandler.INSTANCE.studentsRepositoryv2.userDetailsData(params.toRequestBody())
    }

    @SuppressLint("HardwareIds")
    fun sendPreLoginOnboardData(context: Context): Completable {
        val params: HashMap<String, Any> = HashMap()
        params[Constants.KEY_GCM_REG_ID] =
            defaultPrefs(context).getString(Constants.GCM_REG_ID, "").orDefaultValue()
        params["udid"] = android.provider.Settings.Secure.getString(
            context.contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ).toString()
        params["app_version"] = appVersion
        params["aaid"] = userPreference.getGaid()
        return DataHandler.INSTANCE.studentsRepositoryv2.sendPreLoginOnboardData(params.toRequestBody())
    }

    fun getAllInstalledApps(context: Context): Completable {
        val params: HashMap<String, Any> = HashMap()
        params["app_list"] = Utils.getAllInstalledAppsAndSendToServer(context)
        return DataHandler.INSTANCE.studentsRepositoryv2.getAllInstalledApps(
            authToken(
                getApplication()
            ), params.toRequestBody()
        )
    }

    fun updateConfigValues() {
        compositeDisposable.add(
            splashRepository.saveCameraScreenConfig()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    if (it.containsKey("user_property")) {
                        ApxorUtils.addUserProperty(it["user_property"] as Map<String, Any>)
                    }
                    if (it.containsKey("moengage_user_property")) {
                        val applicationContext = getApplication<DoubtnutApp>().applicationContext
                        MoEngageUtils.setUserAttribute(
                            applicationContext,
                            it["moengage_user_property"] as Map<String, Any>
                        )
                    }
                    UXCamUtil.init()
                    ExoPlayerHelper.warmupClient()
                })
        )
    }

    fun updateNotificationCount() {
        compositeDisposable.add(
            DataHandler.INSTANCE.notificationCenterRepository.getUnreadNotifications()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    defaultPrefs().edit {
                        putString(Constants.UNREAD_NOTIFICATION_COUNT, it.data.count)
                    }
                }, {
                    it.printStackTrace()
                })
        )
    }

    fun registerDailyAttendanceEvent() {
        compositeDisposable.add(
            splashRepository.registerDailyStreakEvent()
                .applyIoToMainSchedulerOnCompletable()
                .subscribe()
        )
    }

    fun getOnBoardingStatus() {
        compositeDisposable.add(
            splashRepository.getOnBoardingStatus()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    saveOnBoardingDataToPreference(it)
                }, {
                    onOnBoardingErr(it)
                })
        )
    }

    private fun saveOnBoardingDataToPreference(apiOnBoardingStatus: ApiOnBoardingStatus) {
        compositeDisposable + Observable.fromCallable {
            defaultPrefs().edit().putString(
                Constants.DEFAULT_ONBOARDING_DEEPLINK,
                apiOnBoardingStatus.defaultOnboardingDeeplink
            ).apply()
            userPreference.updateOnBoardingData(apiOnBoardingStatus)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _onBoardingStatus.value = Outcome.success(apiOnBoardingStatus.isOnboardingCompleted)
            }, {})

    }

    fun getId() {
        compositeDisposable + Single.fromCallable {
            adIdRetreiver.getId()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ gaid ->
                if (!gaid.isNullOrBlank()) {
                    defaultPrefs(getApplication()).edit {
                        putString(Constants.GAID, gaid)
                    }
                }

            }, {
                onOnBoardingErr(it)
            })

    }

    fun publishEvent(structuredEvent: StructuredEvent) {
        analyticsPublisher.publishEvent(structuredEvent)
    }

    fun publishEvent(analyticsEvent: AnalyticsEvent) {
        analyticsPublisher.publishEvent(analyticsEvent)
    }

    private fun onOnBoardingErr(error: Throwable) {
        _onBoardingStatus.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        Log.e(error)
    }

    override fun onCleared() {
        compositeDisposable.dispose()
        super.onCleared()
    }

    fun getTrueCallerLoginWaitTime() {
        if (networkUtil.isConnectedWithMessage()) {
            compositeDisposable + splashRepository.getLoginTimer()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    this::onLoginTimerSuccess
                ) {
                    onOnBoardingErr(it)
                }
        }
    }

    private fun onLoginTimerSuccess(apiLoginTimer: ApiLoginTimer) {
        defaultPrefs().edit {
            putBoolean(Constants.ENABLE_TRUECALLER_VERIFICATION, apiLoginTimer.enableTrueCaller)
            putBoolean(
                Constants.ENABLE_MISSED_CALL_VERIFICATION,
                apiLoginTimer.enableMissedCallVerification
            )
            putLong(Constants.TRUE_CALLER_WAIT_TIME, apiLoginTimer.time)
            putString(Constants.LOGIN_STUDENT_IMAGES, apiLoginTimer.studentImages)
            putBoolean(
                Constants.ENABLE_DEEPLINK_GUEST_LOGIN,
                apiLoginTimer.enableDeeplinkForGuestLogin
            )
            UserUtil.countryCode = apiLoginTimer.countryCode.orEmpty()
        }

        saveLottieUrls(apiLoginTimer.lottieUrls)

        viewModelScope.launch {
            // this key is used to show Guest login while back press from StudentPhoneFragment
            defaultDataStore.set(
                key = DefaultDataStoreImpl.ENABLE_GUEST_LOGIN,
                value = apiLoginTimer.enableGuestLogin
            )

            // this key is used to show message while user is prompted for gmail verification before accessing social features
            defaultDataStore.set(
                key = DefaultDataStoreImpl.GMAIL_VERIFICATION_SCREEN_TEXT,
                value = apiLoginTimer.gmailVerificationScreenText.orDefaultValue()
            )
        }
    }

    fun storeAppOpenCountOnBackend() {
        viewModelScope.launch {
            userActivityRepository.storeAppOpenActivity().catch { }.collect()
        }
    }

    fun markAppOpenForDnrRewards() {
        compositeDisposable.add(
            dnrRepository.markAppOpen()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        viewModelScope.launch {
                            defaultDataStore.set(
                                DefaultDataStoreImpl.SHOW_DNR_REWARD_POPUP,
                                it.showRewardPopUp
                            )
                        }
                    },
                    {
                        it.printStackTrace()
                    }
                )
        )
    }

    fun getAppWideBottomSheetTabsData() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                bottomNavRepository.fetchAndStoreNavIcons()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}