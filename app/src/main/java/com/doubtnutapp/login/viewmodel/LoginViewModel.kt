package com.doubtnutapp.login.viewmodel

import android.nfc.FormatException
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.analytics.Tracker
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnut.referral.data.remote.ReferralRepository
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.bottomnavigation.repository.BottomNavRepository
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.repository.DoubtFeedRepository
import com.doubtnutapp.data.remote.repository.OtpOverCallRepository
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.domain.login.entity.IntroEntity
import com.doubtnutapp.feed.view.FeedFragment
import com.doubtnutapp.login.LoginNavigation
import com.doubtnutapp.login.event.LoginEventManager
import com.doubtnutapp.login.model.ApiGetOTP
import com.doubtnutapp.login.model.ApiVerifyUser
import com.doubtnutapp.login.model.OtpOverCall
import com.doubtnutapp.login.repository.LoginRepository
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.packageInstallerCheck.CheckForPackageInstall
import com.doubtnutapp.studygroup.service.DnrRepository
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.splash.SplashRepository
import com.doubtnutapp.utils.*
import com.facebook.appevents.AppEventsConstants
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 2019-08-16.
 */
class LoginViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val loginRepository: LoginRepository,
    private val loginEventManager: LoginEventManager,
    private val eventTracker: Tracker,
    private val networkUtil: NetworkUtil,
    private val userPreference: UserPreference,
    private val splashRepository: SplashRepository,
    private val checkForPackageInstall: CheckForPackageInstall,
    private val otpOverCallRepository: OtpOverCallRepository,
    private val gson: Gson,
    private val doubtFeedRepository: DoubtFeedRepository,
    private val analyticsPublisher: AnalyticsPublisher,
    private val dnrRepository: DnrRepository,
    private val defaultDataStore: DefaultDataStore,
    private val referralRepository: ReferralRepository,
    private val bottomNavRepository: BottomNavRepository,
    /*private val trackerController: TrackerController*/
) : BaseViewModel(compositeDisposable) {

    private val teslaRepository = DataHandler.INSTANCE.teslaRepository

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()

    val completeNumberField: MutableLiveData<String> = MutableLiveData()

    val otpField: MutableLiveData<String> = MutableLiveData()

    var phoneNumber = ""
    var countryCode = UserUtil.countryCode.removePrefix("+").ifBlank {
        Constants.COUNTRY_CODE_INDIA
    }

    var sessionId: String = ""
    var callWithoutOtp: Boolean? = null

    var isOptin = true
    var userConsent = 1

    var isAnonymousLogin = false
    var guestUser = false

    val isPinExist: MutableLiveData<Boolean> = MutableLiveData()
    val isOtpOverCallEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val startTimerWithBuffer: MutableLiveData<SingleEvent<Pair<Boolean, Int?>>> =
        MutableLiveData(SingleEvent(Pair(false, null)))

    private val _navigationLiveData: MutableLiveData<SingleEvent<LoginNavigation>> = MutableLiveData()
    val navigationLiveData: LiveData<SingleEvent<LoginNavigation>>
        get() = _navigationLiveData

    private val _studentIdLiveData: MutableLiveData<SingleEvent<String>> = MutableLiveData()
    val studentIdLiveData: LiveData<SingleEvent<String>>
        get() = _studentIdLiveData

    val languageChangedLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private val _nameEntryForTrueCaller: MutableLiveData<Pair<String, String>> = MutableLiveData()
    val nameEntryForTrueCaller: LiveData<Pair<String, String>>
        get() = _nameEntryForTrueCaller

    fun verifyNameOnTrueCaller(firstName: String, lastName: String) {
        _nameEntryForTrueCaller.postValue(Pair(firstName, lastName))
    }

    private val _errorAnonymousLoginLiveData: MutableLiveData<SingleEvent<Boolean>> = MutableLiveData()
    val errorAnonymousLoginLiveData: LiveData<SingleEvent<Boolean>>
        get() = _errorAnonymousLoginLiveData

    var isBottomSheetExpanded: MutableLiveData<Boolean> = MutableLiveData()

    var resetFields: MutableLiveData<Boolean> = MutableLiveData()

    private val _missedCallState: MutableLiveData<Int> = MutableLiveData()
    val missedCallState: LiveData<Int>
        get() = _missedCallState

    fun setMissedCallState(state: Int) {
        _missedCallState.postValue(state)
    }

    fun getMissedCallState(): Int? {
        return _missedCallState.value
    }

    private val _messageStringIdLiveData: MutableLiveData<SingleEvent<Int>> = MutableLiveData()
    val messageStringIdLiveData: LiveData<SingleEvent<Int>>
        get() = _messageStringIdLiveData

    private val _messageLiveData: MutableLiveData<SingleEvent<String>> = MutableLiveData()
    val messageLiveData: LiveData<SingleEvent<String>>
        get() = _messageLiveData

    private val _requestOtpLiveData: MutableLiveData<SingleEvent<String>> = MutableLiveData()

    val requestOtpLiveData: LiveData<SingleEvent<String>>
        get() = _requestOtpLiveData

    private val _verificationCodeLiveData: MutableLiveData<SingleEvent<String>> = MutableLiveData()
    val verificationCodeLiveData: LiveData<SingleEvent<String>>
        get() = _verificationCodeLiveData

    var sendOtpThroughFirebase = false

    private val _isFirebaseOtpEnabled: MutableLiveData<Boolean> = MutableLiveData()
    val isFirebaseOtpEnable: LiveData<Boolean>
        get() = _isFirebaseOtpEnabled

    fun enableFirebaseOtp(enable: Boolean) {
        sendOtpThroughFirebase = enable
        _isFirebaseOtpEnabled.postValue(enable)
    }

    private val _checkForLoginTypeLiveData = MutableLiveData<String>()
    val checkForLoginTypeLiveData: LiveData<String>
        get() = _checkForLoginTypeLiveData

    private val _checkForWhatsapp = MutableLiveData<Boolean>()
    val checkForWhatsapp: LiveData<Boolean>
        get() = _checkForWhatsapp

    val showTrueCallerPopUp: MutableLiveData<SingleEvent<Boolean>> = MutableLiveData()

    private var sendOtpTime: Long? = null

    var showOtherLoginOption = MutableLiveData<Boolean>()

    private val _hasPinChanged = MutableLiveData<Pair<Boolean, String?>>()
    val hasPinChanged: LiveData<Pair<Boolean, String?>>
        get() = _hasPinChanged

    private val _otpOverCall = MutableLiveData<SingleEvent<OtpOverCall>>()
    val otpOverCall: LiveData<SingleEvent<OtpOverCall>>
        get() = _otpOverCall

    fun sendOtp() {
        sendOtpTime = System.currentTimeMillis()

        if (networkUtil.isConnectedWithMessage()) {
            publishEventWith(EventConstants.EVENT_NAME_BTN_SEND_OTP_CLICK, true)
            if (phoneNumber.isBlank()) {
                _messageStringIdLiveData.value = SingleEvent(R.string.enter_valid_number)
            } else if (countryCode.isBlank()) {
                _messageStringIdLiveData.value = SingleEvent(R.string.enter_valid_country_code)
            } else {
                if (countryCode == Constants.COUNTRY_CODE_INDIA && !sendOtpThroughFirebase) {
                    if (phoneNumber.length > 10) {
                        _messageStringIdLiveData.value = SingleEvent(R.string.enter_valid_indian_number)
                    } else if (phoneNumber.length < 10) {
                        _messageStringIdLiveData.value = SingleEvent(R.string.enter_valid_10_digit_number)
                    } else {
                        val numberWithCountryCode = "+$countryCode$phoneNumber"
                        completeNumberField.value = numberWithCountryCode
                        requestOtpFromServer()
                    }
                } else {
                    isLoading.postValue(true)
                    val numberWithCountryCode = "+$countryCode$phoneNumber"
                    _requestOtpLiveData.value = SingleEvent(numberWithCountryCode)
                    completeNumberField.value = numberWithCountryCode
                }
            }
        }
    }

    fun resendOtp() {
        if (networkUtil.isConnectedWithMessage()) {
            publishEventWith(EventConstants.RESEND_OTP_CLICK, hashMapOf())
            sendOtpTime?.let {
                val difference = System.currentTimeMillis() - it
                publishEventWith(
                    EventConstants.DURATION_BETWEEN_SEND_AND_RESEND_OTP,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.SCREEN_TIME, difference)
                    }

                )
            }
            resetOtpField()
            sendOtp()
        }
    }

    private fun requestOtpFromServer() {
        compositeDisposable.add(
            loginRepository.requestOtp(phoneNumber)
                .doOnSubscribe {
                    isLoading.postValue(true)
                }
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(this::onOtpRequestSuccess, this::onVerificationError)
        )
    }

    fun fetchAppWideNavIcons() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                bottomNavRepository.fetchAndStoreNavIcons()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun resetOtpField() {
        otpField.value = ""
    }

    private fun onOtpRequestSuccess(apiGetOTP: ApiGetOTP) {
        sessionId = apiGetOTP.sessionId
        callWithoutOtp = apiGetOTP.callWithoutOtp
        isLoading.postValue(false)
        when (apiGetOTP.callWithoutOtp) {
            true -> {
                verifyOtp()
            }
            else -> {
                isPinExist.postValue(apiGetOTP.pinExists)
                isOtpOverCallEnabled.postValue(apiGetOTP.otpOverCall)
                _navigationLiveData.value = SingleEvent(LoginNavigation.OtpScreen)
            }
        }
    }

    fun verifyOtp(
        otp: String = "",
        pinInserted: Boolean = false,
        pin: String? = null
    ) {
        if (networkUtil.isConnectedWithMessage()) {
            publishEventWith(EventConstants.EVENT_NAME_BTN_VERIFY_OTP_CLICK, true)
            compositeDisposable + loginRepository.verifyOtp(
                sessionId = sessionId,
                otp = otp,
                isOptin = isOptin,
                pinInserted = pinInserted,
                pin = pin,
                userConsent = userConsent,
                callWithoutOtp = callWithoutOtp
            )
                .doOnSubscribe {
                    isLoading.postValue(true)
                }
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({ userEntity ->
                    onLoginVerificationSuccess(
                        apiVerifyUser = userEntity,
                        source = if (pinInserted) Constants.PIN else EventConstants.MOBILE_NO
                    )
                }) {
                    publishEventWith(EventConstants.LOGIN_FAIL, hashMapOf<String, Any>().apply {
                        put(
                            EventConstants.SOURCE,
                            if (pinInserted) Constants.PIN else EventConstants.MOBILE_NO
                        )
                    })
                    onVerificationError(it)
                }
        }
    }

    fun storePin(pin: String) {
        if (networkUtil.isConnectedWithMessage()) {
            compositeDisposable + loginRepository.storePin(pin)
                .doOnSubscribe {
                    isLoading.postValue(true)
                }
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    publishEventWith(EventConstants.LOGIN_PIN_SET)
                    _hasPinChanged.postValue(Pair(true, it.message))
                }) {
                    onVerificationError(it)
                }
        }
    }

    fun onVerifyOtpClick() {
        if (countryCode == Constants.COUNTRY_CODE_INDIA && !sendOtpThroughFirebase) {
            val otp = otpField.value ?: ""
            if (otp.length < 4) {
                _messageStringIdLiveData.value = SingleEvent(R.string.enter_valid_otp)
            } else {
                verifyOtp(otp)
            }
        } else if (networkUtil.isConnectedWithMessage()) {
            val otp = otpField.value ?: ""
            if (otp.length < 6) {
                _messageStringIdLiveData.value = SingleEvent(R.string.enter_valid_otp)
            } else {
                publishEventWith(EventConstants.EVENT_NAME_BTN_VERIFY_OTP_CLICK, true)
                _verificationCodeLiveData.postValue(SingleEvent(otp))
            }
        }
    }

    fun requestFireBaseLoginToServer(token: String) {
        if (networkUtil.isConnectedWithMessage()) {
            compositeDisposable + loginRepository.requestFirebaseLogin(
                countryCode = countryCode,
                isOptin = isOptin,
                token = token,
                userConsent = userConsent
            )
                .doOnSubscribe {
                    isLoading.postValue(true)
                }
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    onLoginVerificationSuccess(it, EventConstants.FIREBASE)
                }

                ) {
                    publishEventWith(EventConstants.LOGIN_FAIL, hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, EventConstants.FIREBASE)
                    })
                    onVerificationError(it)
                }
        }
    }

    private fun onLoginVerificationSuccess(apiVerifyUser: ApiVerifyUser, source: String) {
//        fetchHomeFeed()
        isLoading.postValue(false)
        UserUtil.setPhoneNumber(phoneNumber)

        // set user id for conviva analytics tracker
        /*viewModelScope.launch {
            trackerController.subject.userId = apiVerifyUser.studentId
        }*/
        if (apiVerifyUser.isNewUser) {
            Utils.sendClassLangEvents(EventConstants.NEW_USER_REGISTERED)
            Utils.sendClassLanguageSpecificEvent(EventConstants.NEW_USER_REGISTERED)

            publishEventWith(EventConstants.NEW_USER_REGISTERED)
            // Send this event to Branch
            viewModelScope.launch {
                defaultDataStore.set(DefaultDataStoreImpl.IS_NEW_USER, true)
            }
            val language = userPreference.getSelectedLanguage()
            if (language.isNotBlank() &&
//                (language.contains("en", true)
//                        || language.contains("hi", true)) //commented to avoid sending event new_user_registered_language_en to branch
                (language.contains("hi", true))
            ) {
                analyticsPublisher.publishBranchIoEvent(
                    AnalyticsEvent(
                        EventConstants.NEW_USER_REGISTERED_LANGUAGE
                                + EventConstants.UNDERSCORE
                                + language,
                        hashMapOf()
                    )
                )
            }
            analyticsPublisher.publishBranchIoEvent(
                AnalyticsEvent(
                    EventConstants.NEW_USER_REGISTERED,
                    hashMapOf()
                )
            )

            sendReferralId()

        } else {
            viewModelScope.launch {
                defaultDataStore.set(DefaultDataStoreImpl.IS_NEW_USER, false)
            }
        }

        _studentIdLiveData.value = SingleEvent(apiVerifyUser.studentId)
        updateUserDataToPreferences(apiVerifyUser)
        val countToSendEvent: Int = Utils.getCountToSend(
            RemoteConfigUtils.getEventInfo(),
            EventConstants.REGISTRATION_LOGIN_COMPLETE
        )
        repeat((0 until countToSendEvent).count()) {
            sendFirebaseEvent(EventConstants.REGISTRATION_LOGIN_COMPLETE)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL,
                    hashMapOf()
                )
            )
        }

        analyticsPublisher.publishBranchIoEvent(
            AnalyticsEvent(
                EventConstants.REGISTRATION_LOGIN_COMPLETION,
                hashMapOf()
            )
        )

        sendFirebaseEvent(EventConstants.EVENT_NAME_LOGIN_DONE)

        publishEventWith(EventConstants.LOGIN_SUCCESSFUL, hashMapOf<String, Any>().apply {
            put(EventConstants.SOURCE, source)
        })
        sendMoengageSignInEvent(apiVerifyUser.isNewUser)

        sendUserRegistrationEvent(apiVerifyUser)
        loginEventManager.sendMoEngageLoginEvent(apiVerifyUser)

    }

    /**
     * Send referral id extracted from branch deeplink
     * When user downloads app from a referral code
     */
    private fun sendReferralId() {
        viewModelScope.launch {
            val referralId = defaultDataStore.referralId.firstOrNull()
            if (referralId.isNotNullAndNotEmpty()) {
                val map = HashMap<String, String>()
                map["referrer_id"] = referralId!!
                val requestBody = map.toRequestBody()
                try {
                    val response = referralRepository.sendReferralCode(requestBody)
                    clearReferralIdFromPref()
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }


    private fun clearReferralIdFromPref(){
        viewModelScope.launch {
            defaultDataStore.set(DefaultDataStoreImpl.PREF_KEY_REFERRAL_ID,"")
        }
    }

    private fun onVerificationError(error: Throwable) {
        try {
            if (error is HttpException) {
                val networkException = GsonBuilder()
                    .excludeFieldsWithoutExposeAnnotation()
                    .create()
                    .fromJson(
                        error.response()?.errorBody()?.string(),
                        DoubtnutNetworkException::class.java
                    )
                if (networkException.meta.message.isBlank().not()) {
                    _messageLiveData.value = SingleEvent(networkException.meta.message)
                    resetOtpField()
                } else {
                    _messageStringIdLiveData.value = SingleEvent(R.string.something_went_wrong)
                }
            } else {
                _messageStringIdLiveData.value = SingleEvent(R.string.something_went_wrong)
            }
        } catch (e: Exception) {
            _messageStringIdLiveData.value = SingleEvent(R.string.something_went_wrong)
        }
        isLoading.postValue(false)
        logException(error)
        if (isAnonymousLogin) {
            _errorAnonymousLoginLiveData.postValue(SingleEvent(true))
        }
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                || error is NullPointerException
                || error is ClassCastException
                || error is FormatException
                || error is IllegalArgumentException
            ) {
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    private fun updateUserDataToPreferences(apiVerifyUser: ApiVerifyUser) {
        compositeDisposable + loginRepository.updateUserData(
            apiVerifyUser.studentId, apiVerifyUser.onboardingVideo,
            apiVerifyUser.intro.map {
                IntroEntity(
                    it.type,
                    it.video,
                    it.questionId
                )
            }, apiVerifyUser.studentUsername.orEmpty()
        )
            .doOnSubscribe {
                isLoading.postValue(true)
            }.flatMap {
                loginRepository.getUserProfileDataAndOldStudentId()
            }
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                isLoading.postValue(false)
                updateFcmToken()
                getOnboardingStatus()
                UserUtil.putGuestLogin(guestUser)
                // update config again when we have student id
                updateConfig()
                FirebaseCrashlytics.getInstance()
                    .setUserId(
                        defaultPrefs().getString(Constants.STUDENT_ID, "")
                            .orDefaultValue("")
                    )
            }, {
                isLoading.postValue(false)
                _messageStringIdLiveData.value = SingleEvent(R.string.something_went_wrong)
                if (isAnonymousLogin) {
                    _errorAnonymousLoginLiveData.postValue(SingleEvent(true))
                }
            })
    }

    private fun getAppConfigSetting() {
        val sessionCount = defaultPrefs().getInt(Constants.SESSION_COUNT, 1)
        val postPurchaseSessionCount =
            defaultPrefs().getInt(Constants.POST_PURCHASE_SESSION_COUNT, 0)
        compositeDisposable.add(
            DataHandler.INSTANCE.appConfigRepository.getConfigData(
                sessionCount = sessionCount,
                postPurchaseSessionCount = postPurchaseSessionCount
            )
                .applyIoToMainSchedulerOnSingle()
                .map {
                    ConfigUtils.saveToPref(it.data)
                }
                .subscribeToSingle({
                    getCameraScreenConfig()
                })
        )
    }

    private fun updateFcmToken() {
        compositeDisposable.add(
            loginRepository.updateFcmToken()
                .applyIoToMainSchedulerOnCompletable()
                .subscribeToCompletable({
                })
        )
    }

    /**
     * This method update app and camera screen configuration after flagr call
     * configuration api needs some flagr data process.
     */
    private fun updateConfig() {
        compositeDisposable.add(
            FeaturesManager.getFeatureConfig()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        FeaturesManager.saveFeatureConfig(DoubtnutApp.INSTANCE, it)
                        getAppConfigSetting()
                    },
                    {
                        it.printStackTrace()
                        getAppConfigSetting()
                    }
                )
        )
    }

    private fun getCameraScreenConfig() {
        compositeDisposable.add(
            splashRepository.saveCameraScreenConfig()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle({
                    UXCamUtil.init()
                    ExoPlayerHelper.warmupClient()
                })
        )
    }

    private fun getOnboardingStatus() {
        compositeDisposable + loginRepository.getOnBoardingStatus()
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                saveOnBoardingDataToPreference(it)
                getDoubtFeedStatus()
            }, this::onVerificationError)
    }

    private fun saveOnBoardingDataToPreference(apiOnBoardingStatus: ApiOnBoardingStatus) {
        compositeDisposable + Observable.fromCallable {
            defaultPrefs().edit().putString(
                Constants.DEFAULT_ONBOARDING_DEEPLINK,
                apiOnBoardingStatus.defaultOnboardingDeeplink
            )
                .apply()
            userPreference.updateOnBoardingData(apiOnBoardingStatus)
            userPreference.putUserHasWatchedVideo(apiOnBoardingStatus.isVideoWatched)
            userPreference.putUserSelectedExams(Utils.getAllSelectedExams(apiOnBoardingStatus))
            userPreference.putUserSelectedBoard(Utils.getSelectedBoard(apiOnBoardingStatus))
            apiOnBoardingStatus.selectedExamBoards?.forEach { examBoard ->
                MoEngageUtils.setUserAttribute(
                    DoubtnutApp.INSTANCE.applicationContext,
                    EventConstants.CCM_ID, examBoard.ccmId.toString()
                )
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (apiOnBoardingStatus.isOnboardingCompleted) {
                    // DNR region start
                    markAppOpenForDnrRewards()
                    // DNR region end
                    _navigationLiveData.value = SingleEvent(LoginNavigation.MainScreen)
                } else {
                    _navigationLiveData.value = SingleEvent(LoginNavigation.OnBoardingScreen)
                }
                val userSelectedExamsList = userPreference.getUserSelectedExams().split(",")
                val userClass = userPreference.getUserClass()
                viewModelScope.launch {
                    if (defaultDataStore.isNewUser.firstOrNull() == true) {
                        Utils.sendRegistrationBranchEvents(
                            analyticsPublisher,
                            userSelectedExamsList,
                            userClass
                        )
                    } else {
                        Utils.sendLoginBranchEvents(
                            analyticsPublisher,
                            userSelectedExamsList,
                            userClass
                        )
                    }
                }
            }, this::onVerificationError)
    }

    private fun markAppOpenForDnrRewards() {
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

    fun publishEventWith(eventName: String, ignoreSnowplow: Boolean = false) {
        loginEventManager.eventWith(eventName, ignoreSnowplow)
        eventTracker.addEventNames(eventName)
            .addScreenName(EventConstants.PAGE_LOGIN)
            .track()

    }

    fun publishEventWith(
        eventName: String,
        params: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false
    ) =
        loginEventManager.eventWith(eventName, params, ignoreSnowplow)

    private fun sendFirebaseEvent(eventName: String) {
        eventTracker.addEventNames(eventName)
            .addScreenName(EventConstants.PAGE_LOGIN)
            .track()
    }

    private fun sendUserRegistrationEvent(apiVerifyUser: ApiVerifyUser) {

        loginEventManager.sendUserRegistrationEvent(apiVerifyUser)

        eventTracker.addEventNames(AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION)
            .addScreenName(EventConstants.PAGE_LOGIN)
            .addStudentId(apiVerifyUser.studentId)
            .track()
    }

    fun verifyTrueCallerLogin(
        payload: String?,
        signature: String?,
        status: Boolean,
        accessToken: String?
    ) {
        publishEventWith(EventConstants.RECEIVED_TRUECALLER_TOKEN)
        sendFirebaseEvent(EventConstants.RECEIVED_TRUECALLER_TOKEN)
        compositeDisposable + loginRepository.verifyTrueCallerLogin(
            payload = payload.orEmpty(),
            signature = signature.orEmpty(),
            isOptin = isOptin,
            status = status,
            accessToken = accessToken,
            phone = phoneNumber,
            name = _nameEntryForTrueCaller.value?.first + " " + _nameEntryForTrueCaller.value?.second,
            countryCode = countryCode,
            userConsent = userConsent
        )
            .doOnSubscribe {
                isLoading.postValue(true)
            }
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    onSuccess(it, EventConstants.TRUECALLER)
                },
                {
                    publishEventWith(EventConstants.LOGIN_FAIL, hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, EventConstants.TRUECALLER)
                    })
                    onError(it)
                })
    }

    private fun onSuccess(apiVerifyUser: ApiVerifyUser, source: String) {
//        fetchHomeFeed()
        UserUtil.setPhoneNumber(phoneNumber)

        // set user id for conviva analytics tracker
        /*viewModelScope.launch {
            trackerController.subject.userId = apiVerifyUser.studentId
        }*/
        publishEventWith(EventConstants.LOGIN_SUCCESSFUL, hashMapOf<String, Any>().apply {
            put(EventConstants.SOURCE, source)
        }, false)

        isLoading.postValue(false)

        if (apiVerifyUser.isNewUser) {
            Utils.sendClassLangEvents(EventConstants.NEW_USER_REGISTERED)
            Utils.sendClassLanguageSpecificEvent(EventConstants.NEW_USER_REGISTERED)

            publishEventWith(EventConstants.NEW_USER_REGISTERED, hashMapOf())
            viewModelScope.launch {
                defaultDataStore.set(DefaultDataStoreImpl.IS_NEW_USER, true)
            }
            val language = userPreference.getSelectedLanguage()
            if (language.isNotBlank() &&
                (language.contains("en", true)
                        || language.contains("hi", true))
            ) {
                analyticsPublisher.publishBranchIoEvent(
                    AnalyticsEvent(
                        EventConstants.NEW_USER_REGISTERED_LANGUAGE
                                + EventConstants.UNDERSCORE
                                + language,
                        hashMapOf()
                    )
                )
            }

            sendReferralId()

            // Send this event to Branch
            BranchIOUtils.userCompletedAction(EventConstants.NEW_USER_REGISTERED, JSONObject())
        } else {
            viewModelScope.launch {
                defaultDataStore.set(DefaultDataStoreImpl.IS_NEW_USER, false)
            }
        }

        _studentIdLiveData.value = SingleEvent(apiVerifyUser.studentId)
        updateUserDataToPreferences(apiVerifyUser)
        val countToSendEvent: Int = Utils.getCountToSend(
            RemoteConfigUtils.getEventInfo(),
            EventConstants.REGISTRATION_LOGIN_COMPLETE
        )
        repeat((0 until countToSendEvent).count()) {
            sendFirebaseEvent(EventConstants.REGISTRATION_LOGIN_COMPLETE)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    AppEventsConstants.EVENT_NAME_COMPLETED_TUTORIAL,
                    hashMapOf()
                )
            )
        }
        analyticsPublisher.publishBranchIoEvent(
            AnalyticsEvent(
                EventConstants.REGISTRATION_LOGIN_COMPLETION,
                hashMapOf()
            )
        )
        sendFirebaseEvent(EventConstants.EVENT_NAME_LOGIN_DONE)
        loginEventManager.sendMoEngageLoginEvent(apiVerifyUser)
        publishEventWith(EventConstants.EVENT_NAME_LOGIN_DONE)
        sendMoengageSignInEvent(apiVerifyUser.isNewUser)
    }

    private fun onError(error: Throwable) {
        _messageStringIdLiveData.value = SingleEvent(R.string.something_went_wrong)
        isLoading.postValue(false)
        logException(error)
    }

    fun checkForTrueCallerApp() {
        if (checkForPackageInstall.appInstalled(Constants.TRUECALLER_PACKAGE_NAME)) {
            _checkForLoginTypeLiveData.value = Constants.SHOW_TRUE_CALLER_LOGIN
        } else {
            _checkForLoginTypeLiveData.value = Constants.SHOW_TRUE_CALLER_MISSED_CALLED_LOGIN
        }
    }

    fun publishOnWhatsappOptinClick(source: String) {
        loginEventManager.onWhatsappOptinClick(source)
        eventTracker.addEventNames(EventConstants.WHATSAPP_OPTIN_CLICK + "_" + source)
            .addScreenName(EventConstants.PAGE_LOGIN)
            .track()
    }

    private fun sendMoengageSignInEvent(isNewUser: Boolean) {
        analyticsPublisher.publishMoEngageEvent(
            AnalyticsEvent(
                EventConstants.SIGN_IN, hashMapOf(
                    Constants.SIGN_UP to isNewUser
                )
            )
        )
    }

    fun handleDeepLink() {
        compositeDisposable.add(Observable.intervalRange(
            0,
            15,
            0,
            1, TimeUnit.SECONDS, AndroidSchedulers.mainThread()
        )
            .subscribe {
                checkForLoginDeepLink()
            })
    }

    private fun checkForLoginDeepLink() {
        val referringParams = BranchIOUtils.getReferringParam(DoubtnutApp.INSTANCE)
        if (!referringParams.isNullOrBlank()) {
            val referringParamsJSON = JSONObject(referringParams)
            if (referringParamsJSON.optBoolean("+clicked_branch_link", false)) {
                when (referringParamsJSON.getString("~feature")) {
                    Constants.WHATSAPP_LOGIN -> {
                        val token = referringParamsJSON.getString("access_token")
                        verifyWhatsAppLogin(token)
                        BranchIOUtils.clearReferringParam(DoubtnutApp.INSTANCE)
                    }
                }
            }
        }
    }

    fun checkForWhatsApp() {
        _checkForWhatsapp.value =
            checkForPackageInstall.appInstalled(Constants.WHATSAPP_PACKAGE_NAME)
    }

    private fun verifyWhatsAppLogin(verficationToken: String) {
        publishEventWith(EventConstants.RECEIVED_WHATSAPP_TOKEN)
        sendFirebaseEvent(EventConstants.RECEIVED_WHATSAPP_TOKEN)
        compositeDisposable + loginRepository.verifyWhatsAppLogin(
            verificationToken = verficationToken,
            isOptin = isOptin,
            userConsent = userConsent
        )
            .doOnSubscribe {
                isLoading.postValue(true)
            }
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    onSuccess(it, EventConstants.WHATSAPP)
                },
                {
                    publishEventWith(EventConstants.LOGIN_FAIL, hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, EventConstants.WHATSAPP)
                    })
                    onError(it)
                }
            )
    }

    fun otpOverCall() {
        compositeDisposable + otpOverCallRepository.otpOverCall(
            getOtpOverCallRequestBody(
                phoneNumber
            ).toRequestBody()
        )
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    _otpOverCall.postValue(SingleEvent(it))
                },
                {
                    onVerificationError(it)
                }
            )
    }

    private fun getOtpOverCallRequestBody(phone: String)
            : HashMap<String, Any> {
        return HashMap<String, Any>().apply {
            this["phone"] = phone
            this["locale"] =
                defaultPrefs().getString(Constants.STUDENT_LANGUAGE_CODE, "en") as String
        }
    }

    private fun fetchHomeFeed() {
        if (defaultPrefs().getBoolean(Constants.ONBOARDING_COMPLETED, false)) {
            defaultPrefs().edit {
                putBoolean(Constants.HOME_PAGE_DATA_INVALIDATED, false)
            }
            GlobalScope.launch {
                val source = FeedFragment.SOURCE_HOME
                val assortmentId = defaultPrefs().getString(Constants.SELECTED_ASSORTMENT_ID, "")
                teslaRepository.getFeed(1, source, "", assortmentId = assortmentId.orEmpty())
                    .catch {
                        defaultPrefs().edit {
                            putBoolean(Constants.HOME_PAGE_DATA_INVALIDATED, true)
                        }
                    }
                    .collect {
                        if (it.meta.code == HttpURLConnection.HTTP_OK) {
                            defaultPrefs().edit {
                                putString(Constants.HOME_PAGE_DATA, gson.toJson(it.data))
                            }
                        }
                    }
            }
        }
    }

    private fun getDoubtFeedStatus() {
        //Launch with GlobalScope as app may have navigated to other screen
        GlobalScope.launch {
            doubtFeedRepository.getDoubtFeedStatus()
                .catch { }
                .collect()
        }
    }

    fun addAnonymousUser(locale: String, source: String? = null) {
        isAnonymousLogin = true
        compositeDisposable + loginRepository.addAnonymousUser(locale = locale, source = source)
            .doOnSubscribe {
                isLoading.postValue(true)
            }
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                publishEventWith(EventConstants.LOGIN_SUCCESSFUL, hashMapOf<String, Any>().apply {
                    put(key = EventConstants.SOURCE, value = EventConstants.ANONYMOUS_LOGIN)
                })
                guestUser = it.guestUser
                onLoginVerificationSuccess(it, EventConstants.ANONYMOUS_LOGIN)
            }
            ) {
                publishEventWith(EventConstants.LOGIN_FAIL, hashMapOf<String, Any>().apply {
                    put(key = EventConstants.SOURCE, value = EventConstants.ANONYMOUS_LOGIN)
                })
                onVerificationError(it)
            }
    }

    fun getSelectedLocale() = userPreference.getSelectedLanguage()
}
