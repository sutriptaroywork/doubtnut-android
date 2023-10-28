package com.doubtnutapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.nfc.FormatException
import android.text.TextUtils
import androidx.core.content.edit
import androidx.core.os.bundleOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.DefaultDataStoreImpl
import com.doubtnut.core.data.LottieAnimDataStore
import com.doubtnut.noticeboard.data.entity.UnreadNoticeCountUpdate
import com.doubtnut.noticeboard.data.remote.NoticeBoardRepository
import com.doubtnutapp.EventBus.UpdateStudyDostWidget
import com.doubtnutapp.appexitdialog.ui.AppExitDialogFragment
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.common.UserProfileData
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.base.di.qualifier.Udid
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.common.UserPreferenceImpl
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStatus
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.models.reward.MarkAttendanceModel
import com.doubtnutapp.data.remote.models.reward.RewardNotificationData
import com.doubtnutapp.data.remote.models.reward.RewardPopupModel
import com.doubtnutapp.data.remote.repository.*
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnutapp.db.entity.MatchWithQuestions
import com.doubtnutapp.domain.camerascreen.entity.AudioTooltipResponse
import com.doubtnutapp.domain.gamification.userProfile.interactor.GetUserProfile
import com.doubtnutapp.domain.gamification.userProfile.model.UserProfileEntity
import com.doubtnutapp.domain.homefeed.entites.model.WebViewData
import com.doubtnutapp.domain.homefeed.interactor.ConfigData
import com.doubtnutapp.domain.homefeed.interactor.GetWebViewBottomSheet
import com.doubtnutapp.domain.mainscrren.interactor.SetBottomNavigationButtonClicked
import com.doubtnutapp.domain.mainscrren.interactor.SetBottomNavigationButtonClickedStateToTrue
import com.doubtnutapp.domain.survey.entities.ApiCheckSurvey
import com.doubtnutapp.domain.survey.interactor.CheckSurveyUseCase
import com.doubtnutapp.home.WebViewBottomSheetFragment
import com.doubtnutapp.model.ActiveSlotApiModel
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.screennavigator.NavigationModel
import com.doubtnutapp.screennavigator.WebViewBottomSheet
import com.doubtnutapp.ui.mediahelper.ExoPlayerCacheManager
import com.doubtnutapp.ui.onboarding.repository.OnBoardingRepository
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.internal.LinkedTreeMap
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.lang.reflect.Type
import java.net.HttpURLConnection
import javax.inject.Inject
import kotlin.collections.set

class MainViewModel @Inject constructor(
    private val setBottomNavigationButtonClickedStateToTrue: SetBottomNavigationButtonClickedStateToTrue,
    private val setBottomNavigationButtonClicked: SetBottomNavigationButtonClicked,
    compositeDisposable: CompositeDisposable,
    private val mainViewEventManager: MainViewEventManager,
    private val userPreference: UserPreference,
    @Udid private val udid: String,
    private val getUserProfileUseCase: GetUserProfile,
    private val getWebViewBottomSheet: GetWebViewBottomSheet,
    private val onBoardingRepository: OnBoardingRepository,
    private val inviteRepository: InviteRepository,
    private val vipInfoRepository: VipInfoRepository,
    private val profileRepository: ProfileRepository,
    private val checkSurveyUseCase: CheckSurveyUseCase,
    private val appExitRepository: AppExitRepository,
    private val userActivityRepository: UserActivityRepository,
    private val database: DoubtnutDatabase,
    private val rewardRepository: RewardRepository,
    val gson: Gson,
    private val noticeBoardRepository: NoticeBoardRepository,
    private val defaultDataStore: DefaultDataStore,
    private val lottieAnimDataStore: LottieAnimDataStore
) : BaseViewModel(compositeDisposable) {

    var askMeButtonObserver: MutableLiveData<Boolean> = MutableLiveData()

    private val _isProfileClickedLiveData = MutableLiveData<Boolean>()
    val isProfileClickedLiveData: LiveData<Boolean>
        get() = _isProfileClickedLiveData

    private val _onProfileButtonClicked = MutableLiveData<Boolean>()
    val onProfileButtonClicked: LiveData<Boolean>
        get() = _onProfileButtonClicked

    private val _isLibraryClickedLiveData = MutableLiveData<Boolean>()
    val isLibraryClickedLiveData: LiveData<Boolean>
        get() = _isLibraryClickedLiveData

    private val _onLibraryButtonClicked = MutableLiveData<Boolean>()
    val onLibraryButtonClicked: LiveData<Boolean>
        get() = _onLibraryButtonClicked

    private val _isForumClickedLiveData = MutableLiveData<Boolean>()
    val isForumClickedLiveData: LiveData<Boolean>
        get() = _isForumClickedLiveData

    private val _onForumButtonClicked = MutableLiveData<Boolean>()
    val onForumButtonClicked: LiveData<Boolean>
        get() = _onForumButtonClicked

    private val _eventName = MutableLiveData<String>()
    val eventNameData: LiveData<String>
        get() = _eventName

    private val _latestMatchResultFromDb = MutableLiveData<MatchWithQuestions>()
    val latestMatchResultFromDb: LiveData<MatchWithQuestions>
        get() = _latestMatchResultFromDb

    private val _loginPinSetUp = MutableLiveData<Pair<Boolean?, String?>>()
    val loginPinSetUp: LiveData<Pair<Boolean?, String?>>
        get() = _loginPinSetUp

    private val _studyDost = MutableLiveData<ApiOnBoardingStatus.ApiStudyDost?>()
    val studyDost: LiveData<ApiOnBoardingStatus.ApiStudyDost?>
        get() = _studyDost

    private val _studyGroup = MutableLiveData<ApiOnBoardingStatus.ApiStudyGroup?>()
    val studyGroup: LiveData<ApiOnBoardingStatus.ApiStudyGroup?>
        get() = _studyGroup

    private val _doubtP2p = MutableLiveData<ApiOnBoardingStatus.ApiDoubtP2p?>()
    val doubtP2p: LiveData<ApiOnBoardingStatus.ApiDoubtP2p?>
        get() = _doubtP2p

    private val _kheloAurJeeto = MutableLiveData<ApiOnBoardingStatus.ApiKheloAurJeeto?>()
    val kheloAurJeeto: LiveData<ApiOnBoardingStatus.ApiKheloAurJeeto?>
        get() = _kheloAurJeeto

    private val _doubtFeed2 = MutableLiveData<ApiOnBoardingStatus.ApiDoubtFeed2?>()
    val doubtFeed2: LiveData<ApiOnBoardingStatus.ApiDoubtFeed2?>
        get() = _doubtFeed2

    private val _revisionCorner = MutableLiveData<ApiOnBoardingStatus.ApiRevisionCorner?>()
    val revisionCorner: LiveData<ApiOnBoardingStatus.ApiRevisionCorner?>
        get() = _revisionCorner

    private val _dnrData = MutableLiveData<ApiOnBoardingStatus.ApiDnr?>()
    val dnrData: LiveData<ApiOnBoardingStatus.ApiDnr?>
        get() = _dnrData

    private val _message = MutableLiveData<String>()
    val message: LiveData<String>
        get() = _message

    private val _checkUserSurvey = MutableLiveData<ApiCheckSurvey>()
    val checkUserSurvey: LiveData<ApiCheckSurvey>
        get() = _checkUserSurvey

    private var _showAppExitDialog: Boolean = false
    val showAppExitDialog: Boolean
        get() = _showAppExitDialog
                && Utils.anyAppExitCoreActionNotDone()
                && appExitRepository.getAppExitDialogShownInCurrentSession().not()

    private var _appExitDialogData: AppExitDialogData? = null
    val appExitDialogData: AppExitDialogData?
        get() = _appExitDialogData

    var maxAppExitDialogItems: Int = AppExitDialogFragment.DIALOG_ITEMS_DEFAULT_VALUE

    var appExitDialogExperiment: Int = AppExitDialogFragment.EXPERIMENT_NOT_ENABLED

    val cameraScreenAutoOpenParams = bundleOf(CameraActivity.INTENT_EXTRA_IS_USER_OPENED to false)

    private val _isCourseActive = MutableLiveData<Boolean>()
    val isCourseActive: LiveData<Boolean>
        get() = _isCourseActive

    private val _popupLiveData = MutableLiveData<RewardPopupModel>()
    val popupLiveData: LiveData<RewardPopupModel>
        get() = _popupLiveData

    private val _attendanceLiveData = MutableLiveData<MarkAttendanceModel>()
    val attendanceLiveData: LiveData<MarkAttendanceModel>
        get() = _attendanceLiveData

    private val _configLiveData: MutableLiveData<Outcome<ConfigData>> = MutableLiveData()
    val configLiveData: LiveData<Outcome<ConfigData>>
        get() = _configLiveData

    private val _showDnrRewardLiveData = MutableLiveData<Boolean>()
    val showDnrRewardLiveData: LiveData<Boolean>
        get() = _showDnrRewardLiveData

    fun shouldShowDnrRewardData(showDnrReward: Boolean) {
        _showDnrRewardLiveData.postValue(showDnrReward)
    }

    private val application = DoubtnutApp.INSTANCE.applicationContext

    fun getNotices() {
        viewModelScope.launch {
            noticeBoardRepository.getNotices(NoticeBoardRepository.TYPE_TODAY_ALL)
                .catch { it.printStackTrace() }
                .collect {
                    NoticeBoardRepository.data = it
                    DoubtnutApp.INSTANCE.runOnDifferentThread {
                        it.items.orEmpty().map { it.id }.forEach { id ->
                            if (!id.isNullOrEmpty()) {
                                if (database.noticeBoardDao().getCount(id) <= 0) {
                                    NoticeBoardRepository.unreadNoticeIds.add(id)
                                }
                            }
                        }
                    }
                    DoubtnutApp.INSTANCE.bus()?.send(UnreadNoticeCountUpdate())
                }
        }
    }

    fun checkDataForWebViewBottomSheet(studentClass: String) {
        compositeDisposable.add(
            getWebViewBottomSheet.execute(GetWebViewBottomSheet.Param(studentClass))
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    this::onWebViewDataSuccess, this::onError
                )
        )
    }

    private fun onWebViewDataSuccess(webViewData: WebViewData) {

        webViewData.url?.let {
            if (it.isNotEmpty()) {
                _navigateLiveData.value =
                    Event(NavigationModel(WebViewBottomSheet, hashMapOf<String, Any>().apply {
                        put(WebViewBottomSheetFragment.WEB_VIEW_URL, it)
                        put(WebViewBottomSheetFragment.COUNT, webViewData.count)
                    }))
            }
        }
    }

    @SuppressLint("CheckResult")
    fun sendInviteFriendData(invitorId: String?, inviteeId: String?) {
        if (isInviteRequestDataInValid(
                invitorId,
                inviteeId
            ) || isInvitationDataSent() || isInvitationDataForFirst()
        ) return
        inviteRepository
            .sendAppInvitationData(getInviteApiRequestBody(invitorId!!).toRequestBody())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onSuccess()
            }, {
                onError(it)
            })
    }

    fun isLibraryButtonClicked() {
        compositeDisposable + setBottomNavigationButtonClicked.execute(UserPreferenceImpl.PREF_KEY_LIBRARY_BUTTON_CLICKED)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _isLibraryClickedLiveData.value = it
            })
    }

    fun isForumButtonClicked() {
        compositeDisposable + setBottomNavigationButtonClicked.execute(UserPreferenceImpl.PREF_KEY_FORUM_BUTTON_CLICKED)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _isForumClickedLiveData.value = it
            })
    }

    fun isProfileButtonClicked() {
        compositeDisposable + setBottomNavigationButtonClicked.execute(UserPreferenceImpl.PREF_KEY_PROFILE_BUTTON_CLICKED)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                _isProfileClickedLiveData.value = it
            })
    }

    fun setLibraryButtonClicked() {
        compositeDisposable + setBottomNavigationButtonClickedStateToTrue.execute(UserPreferenceImpl.PREF_KEY_LIBRARY_BUTTON_CLICKED)
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({
                _onLibraryButtonClicked.value = true
            })
    }

    fun setForumButtonClicked() {
        compositeDisposable + setBottomNavigationButtonClickedStateToTrue.execute(UserPreferenceImpl.PREF_KEY_FORUM_BUTTON_CLICKED)
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({
                _onForumButtonClicked.value = true
            })
    }

    fun setProfileButtonClicked() {
        compositeDisposable + setBottomNavigationButtonClickedStateToTrue.execute(UserPreferenceImpl.PREF_KEY_PROFILE_BUTTON_CLICKED)
            .applyIoToMainSchedulerOnCompletable()
            .subscribeToCompletable({
                _onProfileButtonClicked.value = true
            })
    }

    fun clearExoPlayerCaches() {
        ExoPlayerCacheManager.getInstance(application).clearAllCache()
    }

    private fun getInviteApiRequestBody(invitorIdParam: String): HashMap<String, Any> {
        return hashMapOf("referred_id" to invitorIdParam)
    }

    private fun isInviteRequestDataInValid(invitorId: String?, inviteeId: String?) =
        TextUtils.isEmpty(invitorId) || TextUtils.isEmpty(inviteeId) || (invitorId.equals(inviteeId))

    private fun isInvitationDataSent(): Boolean =
        defaultPrefs()
            .getBoolean(Constants.SHARED_PREF_EXTRA_INVITATION_DATA_SENT, false)

    private fun isInvitationDataForFirst(): Boolean =
        !(defaultPrefs(application).getBoolean(Constants.SESSION_FROM_INVITE, false))

    private fun onSuccess() {
        setInvitationSentStatus(true)
    }

    private fun onError(it: Throwable?) {
        if (it is HttpException) {

            val errorString = getErrorAsString(it) ?: ""
            val isUserAlreadyReferred = errorString.contains("USER IS ALREADY REFERRED")

            if (isUserAlreadyReferred) {
                setInvitationSentStatus(true)
            }

        }
    }

    private fun setInvitationSentStatus(status: Boolean) {
        defaultPrefs(application).edit {
            putBoolean(Constants.SHARED_PREF_EXTRA_INVITATION_DATA_SENT, status)
        }
    }

    private fun getErrorAsString(it: HttpException) =
        it.response()?.errorBody()?.source()?.readUtf8()

    fun updateFCMRegId() {

        val applicationContext = DoubtnutApp.INSTANCE.applicationContext

        try {
            val pInfo =
                applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            val appVersion = pInfo.versionName
            if (isNewVersion(applicationContext, appVersion) || isFcmRegIdUpdated()) {
                updateAppVersion(appVersion)
                updateRegId()
                defaultPrefs(applicationContext).edit {
                    putString(Constants.APP_VERSION_NCERT, appVersion)
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
    }

    private fun isNewVersion(context: Context, appVersion: String?) =
        if (appVersion.isNullOrBlank()) {
            false
        } else {
            !TextUtils.equals(
                defaultPrefs(context).getString(Constants.APP_VERSION, ""),
                appVersion
            )
        }

    @SuppressLint("CheckResult")
    private fun updateRegId() {

        val applicationContext = DoubtnutApp.INSTANCE.applicationContext

        if (!defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID, "").isNullOrBlank()
            && !defaultPrefs(applicationContext).getString(Constants.STUDENT_ID, "").isNullOrBlank()
        ) {
            ChatUtil.sendToken(
                applicationContext,
                defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID, "") ?: ""
            )
            val params: HashMap<String, Any> = HashMap()
            params[Constants.KEY_GCM_REG_ID] =
                defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID, "") ?: ""
            params[Constants.KEY_STUDENT_ID] = getStudentId()
            params[Constants.APP_VERSION] =
                defaultPrefs(applicationContext).getString(Constants.APP_VERSION, "")
                    .orDefaultValue()
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
    }

    private fun updateAppVersion(appVersion: String) {
        val applicationContext = DoubtnutApp.INSTANCE.applicationContext

        defaultPrefs(applicationContext).edit {
            putString(Constants.APP_VERSION, appVersion)
        }
    }

    private fun isFcmRegIdUpdated() = defaultPrefs().getBoolean(
        Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER,
        false
    )

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

    fun checkForLevel(label: String) {
        when (label) {
            "fragment_user_profile" -> {
                setProfileButtonClicked()
                mainViewEventManager.onBottomItemClick(EventConstants.EVENT_NAME_PROFILE_IN_BOTTOM_CLICK)
                _eventName.value = EventConstants.EVENT_NAME_PROFILE_IN_BOTTOM_CLICK
            }
            "fragment_library" -> {
                setLibraryButtonClicked()
                mainViewEventManager.onBottomItemClick(EventConstants.EVENT_NAME_LIBRARY_IN_BOTTOM_CLICK)
                _eventName.value = EventConstants.EVENT_NAME_LIBRARY_IN_BOTTOM_CLICK
            }
            "fragment_home" -> {
                mainViewEventManager.onBottomItemClick(EventConstants.EVENT_NAME_HOME_IN_BOTTOM_CLICK)
                _eventName.value = EventConstants.EVENT_NAME_HOME_IN_BOTTOM_CLICK
            }

            "fragment_forum" -> {
                setForumButtonClicked()
                mainViewEventManager.onBottomItemClick(
                    EventConstants.EVENT_NAME_FORUM_IN_BOTTOM_CLICK,
                    ignoreSnowplow = true
                )
                _eventName.value = EventConstants.EVENT_NAME_FORUM_IN_BOTTOM_CLICK
            }
        }
    }

    fun publishCameraClickEvent(viewSource: String) {
        mainViewEventManager.onCameraClick(viewSource)
    }

    fun updateUserprofile() {
        val applicationContext = DoubtnutApp.INSTANCE.applicationContext
        val params: HashMap<String, Any> = HashMap()
        params[Constants.KEY_UDID] = udid
        params[Constants.KEY_APP_VERSION] = Utils.getVersionName()
        params[Constants.KEY_EMAIL] = Utils.getEmail(applicationContext)
        params["student_class"] = userPreference.getUserClass()
        params["language"] = userPreference.getSelectedLanguage()
        params[Constants.KEY_IS_VERFIED] = ""
        params[Constants.KEY_GCM_REG_ID] =
            defaultPrefs(applicationContext).getString(Constants.GCM_REG_ID, "") ?: ""
        params[Constants.GAID] =
            defaultPrefs().getString(Constants.GAID, "").orDefaultValue()

        val fName = Utils.getAccountUsername(applicationContext)
        if (fName != "") {
            params[Constants.KEY_NAME] = fName
        }

        compositeDisposable.add(
            DataHandler.INSTANCE.studentsRepositoryv2.updateUserProfileFromUpdate(params = params.toRequestBody())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    defaultPrefs().edit {
                        putString(Constants.APP_VERSION, Utils.getVersionName())
                    }
                    if (DoubtnutApp.INSTANCE.fcmRegId.isEmpty()) return@subscribe
                    defaultPrefs().edit {
                        putBoolean(Constants.KEY_FIREBASE_REG_ID_UPDATED_ON_SERVER, true)
                    }
                }, {})
        )
    }

    private val _userProfileLiveData = MutableLiveData<Outcome<UserProfileData>>()
    val userProfileLiveData: LiveData<Outcome<UserProfileData>>
        get() = _userProfileLiveData

    val _updateClassOnHomeFragment = MutableLiveData<Boolean>()
    val updateClassOnHomeFragment: LiveData<Boolean>
        get() = _updateClassOnHomeFragment

    fun getUserProfile() {
        _userProfileLiveData.value = Outcome.loading(true)
        compositeDisposable + getUserProfileUseCase
            .execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onSuccess, this::onProfileError)
    }

    fun getDailyPrize(contestId: String): RetrofitLiveData<ApiResponse<DailyPrize>> {
        return DataHandler.INSTANCE.dailyPrizeRepository.getDailyPrize(
            authToken(application),
            contestId
        )
    }

    private fun onSuccess(userProfileEntity: UserProfileEntity) {
        UserUtil.setPhoneNumber(userProfileEntity.mobileNumber.orEmpty())
        UserUtil.countryCode = userProfileEntity.countryCode.orEmpty()
        userPreference.putUserSelectedBoard(userProfileEntity.board.orEmpty())
        userPreference.putUserSelectedExams(userProfileEntity.exams?.joinToString().orEmpty())

        _userProfileLiveData.value = Outcome.loading(false)
        val userProfileData = UserProfileData(
            name = userProfileEntity.userName.orEmpty(),
            level = userProfileEntity.userLevel,
            profileImage = userProfileEntity.profileImage.orEmpty(),
            bannerImage = userProfileEntity.bannerImage.orEmpty(),
            subscriptionStatus = userProfileEntity.subscriptionStatus,
            subscriptionImageUrl = userProfileEntity.subscriptionImageUrl
        )
        _userProfileLiveData.value = Outcome.success(userProfileData)
    }

    private fun onProfileError(error: Throwable) {
        _userProfileLiveData.value = Outcome.loading(false)
        _userProfileLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                //TODO socket time out exception msg should be "Timeout while fetching the data"
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        logException(error)
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

    fun publishEventWith(eventName: String) {
        mainViewEventManager.eventWith(eventName)
    }

    fun publishEventWith(
        eventName: String,
        params: HashMap<String, Any>,
        ignoreSnowplow: Boolean = false
    ) {
        mainViewEventManager.eventWith(eventName, params, ignoreSnowplow)
    }

    fun publishRewardSystemEvent(
        eventName: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        mainViewEventManager.publishRewardSystemEvent(
            eventName,
            params,
            ignoreSnowplow = ignoreSnowplow
        )
    }

    fun publishTransactionEventClick(eventName: String) {
        mainViewEventManager.onTransactionViewClick(eventName)
    }

    fun updateClassAndLanguage() {
        compositeDisposable + onBoardingRepository.getClassAndLanguage()
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                saveClassAndLanguageToPreference(it)
            }, this::onError)
    }

    private fun saveClassAndLanguageToPreference(apiOnBoardingStatus: ApiOnBoardingStatus) {

        compositeDisposable + Observable.fromCallable {
            userPreference.updateClassAndLanguage(apiOnBoardingStatus)
            userPreference.updateStudyDostData(apiOnBoardingStatus.studyDost)
            userPreference.updateStudyGroupData(apiOnBoardingStatus.studyGroup)
            userPreference.updateDoubtP2pData(apiOnBoardingStatus.doubtP2p)
            userPreference.updateKheloAurJeetoData(apiOnBoardingStatus.kheloAurJeeto)
            userPreference.updateDoubtFeed2Data(apiOnBoardingStatus.doubtFeed2)
            userPreference.updateRevisionCornerData(apiOnBoardingStatus.revisionCorner)
            userPreference.updateDnrData(apiOnBoardingStatus.dnr)
            updateDictionaryData(apiOnBoardingStatus.dictionary)
            _loginPinSetUp.postValue(Pair(apiOnBoardingStatus.pinExist, apiOnBoardingStatus.pin))
            _studyDost.postValue(apiOnBoardingStatus.studyDost)
            _studyGroup.postValue(apiOnBoardingStatus.studyGroup)
            _doubtP2p.postValue(apiOnBoardingStatus.doubtP2p)
            _kheloAurJeeto.postValue(apiOnBoardingStatus.kheloAurJeeto)
            _doubtFeed2.postValue(apiOnBoardingStatus.doubtFeed2)
            _revisionCorner.postValue(apiOnBoardingStatus.revisionCorner)
            _dnrData.postValue(apiOnBoardingStatus.dnr)

            // set value if user is verified with email authentication.
//            defaultPrefs().edit {
//                putBoolean(
//                    Constants.GMAIL_VERIFIED,
//                    apiOnBoardingStatus.gmailVerified ?: false
//                )
//            }
            // Store engagement time after which user can claim DNR reward while watching video
            saveEngagementTimeToClaimDnrReward(apiOnBoardingStatus.dnr)
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                _updateClassOnHomeFragment.value = true
            }, this::onError)

    }

    /**
     * This method stores engagement time which will used on Video Page
     * after which user can claim DNR reward.
     * @param apiDnr - DNR data fetched from api
     */
    private fun saveEngagementTimeToClaimDnrReward(apiDnr: ApiOnBoardingStatus.ApiDnr?) {
        if (apiDnr == null) return
        viewModelScope.launch {
            defaultDataStore.set(
                key = DefaultDataStoreImpl.SRP_SF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD,
                value = apiDnr.srpSfEngagementTime
            )
            defaultDataStore.set(
                key = DefaultDataStoreImpl.NON_SRP_SF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD,
                value = apiDnr.nonSrpSfEngagementTime
            )
            defaultDataStore.set(
                key = DefaultDataStoreImpl.LF_ENGAGEMENT_TIME_TO_CLAIM_DNR_REWARD,
                value = apiDnr.lfEngagementTime
            )
        }
    }

    private fun updateDictionaryData(dictionaryData: ApiOnBoardingStatus.DictionaryData?) {
        defaultPrefs().edit().putString(
            Constants.DICTIONARY_TEXT,
            dictionaryData?.text ?: Constants.DEFAULT_DICTIONARY_TEXT
        ).apply()

        defaultPrefs().edit().putString(
            Constants.DICTIONARY_ICON_URL,
            dictionaryData?.iconUrl ?: Constants.DEFAULT_DICTIONARY_ICON_URL
        ).apply()

        defaultPrefs().edit().putString(
            Constants.DICTIONARY_DEEPLINK,
            dictionaryData?.deeplink ?: Constants.DEFAULT_DICTIONARY_DEEPLINK
        ).apply()
    }

    fun updateClickedNotifications() {
        val seenNotificationString: String =
            defaultPrefs().getString(Constants.SEEN_NOTIFICATIONS_LIST, "").orDefaultValue()
        if (seenNotificationString.isEmpty()) {
            return
        }
        val type: Type = object : TypeToken<List<String>?>() {}.type
        val gson = Gson()
        val notificationList: List<String>? = gson.fromJson(seenNotificationString, type)
        if (notificationList.isNullOrEmpty()) {
            return
        }
        DataHandler.INSTANCE.notificationCenterRepository
            .updateSeenNotifications(getSeenNotificationsRequestBody(notificationList).toRequestBody())
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                //clear preference
                defaultPrefs().edit {
                    putString(Constants.SEEN_NOTIFICATIONS_LIST, "")
                }
            }, {
                it.printStackTrace()
            })
    }

    fun updateNotificationCount() {
        DataHandler.INSTANCE.notificationCenterRepository.getUnreadNotifications()
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                defaultPrefs().edit {
                    putString(Constants.UNREAD_NOTIFICATION_COUNT, it.data.count)
                }
            }, {
                it.printStackTrace()
            })
    }

    private fun getSeenNotificationsRequestBody(notificationList: List<String>)
            : HashMap<String, Any> {
        return HashMap<String, Any>().apply {
            this["list"] = notificationList
        }
    }

    fun getLatestMatchResultFromDb() {
        compositeDisposable + database.matchQuestionDao().getLatestMatchesQuestion()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    _latestMatchResultFromDb.postValue(it)
                },
                { it.printStackTrace() }
            )
    }

    fun setMoEngageInfo() {
        compositeDisposable + vipInfoRepository
            .getVipPurchaseInfo()
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                if (!it.purchasedAssortmentIds.isNullOrEmpty()) {
                    UXCamUtil.setUserProperty(Constants.IS_VIP, true)
                } else {
                    UXCamUtil.setUserProperty(Constants.IS_VIP, false)
                }
                updatePurchasedAssortmentIdsToApxor(it.purchasedAssortmentIds)
                updateMoEngageUserInfo(it)
                updatePreferences(it)
            }, {})

        compositeDisposable + profileRepository
            .getActiveSlots()
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({
                updateMoEngageUserActiveSlotInfo(it)
            }, {})
    }

    private fun updatePreferences(it: VipInfoData?) {
        defaultPrefs().edit {
            putBoolean(Constants.IS_VIP, it?.isVip ?: false)
            putBoolean(Constants.IS_TRIAL, it?.isTrial ?: false)
        }
    }

    private fun updatePurchasedAssortmentIdsToApxor(purchasedAssortmentIds: List<String>?) {
        val assortmentMap: HashMap<String, Any> = hashMapOf()
        for (i in 0..2) {
            assortmentMap[EventConstants.PAID_ASSORTMENT_ID_PREFIX + i] =
                purchasedAssortmentIds?.getOrNull(i)
                    ?: "NA"
        }
        ApxorUtils.addUserProperty(assortmentMap)
    }

    private fun updateMoEngageUserInfo(vipInfoData: VipInfoData) {
        MoEngageUtils.setUserAttribute(
            application,
            EventConstants.PURCHASE_COUNT,
            vipInfoData.purchaseCount.toString()
        )
        MoEngageUtils.setUserAttribute(
            application,
            EventConstants.TRAIL_STATUS,
            vipInfoData.isTrial.toString()
        )
        MoEngageUtils.setUserAttribute(
            application,
            EventConstants.TRAIL_ACTIVE_STATUS,
            vipInfoData.isTrialExpired.toString()
        )
        vipInfoData.isCourseActive?.let {
            _isCourseActive.postValue(it)
        }

        val list = mutableListOf<String>()
        vipInfoData.purchasedAssortmentIds?.forEach {
            list.add("*$it*")
        }

        val purchasedAssortmentIds = list.joinToString(" || ")
        if (purchasedAssortmentIds.isNotBlank()) {
            MoEngageUtils.setUserAttribute(
                application,
                Constants.PAID_ASSORTMENT_IDS,
                purchasedAssortmentIds
            )
        }

    }

    private fun updateMoEngageUserActiveSlotInfo(activeSlotData: ActiveSlotApiModel) {
        if (!activeSlotData.flags.isNullOrEmpty()) {
            for (activeSlotFlag in activeSlotData.flags) {
                MoEngageUtils.setUserAttribute(
                    application,
                    activeSlotFlag.flagName,
                    activeSlotFlag.value.toString()
                )
            }
        }
    }

    fun getUserBanStatus() {
        compositeDisposable + DataHandler.INSTANCE.socialRepository
            .getUserBanStatus()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.error == null && it.data is LinkedTreeMap<*, *>) {
                    val banStatus = it.data["banStatus"] as Boolean
                    defaultPrefs().edit {
                        putBoolean(Constants.USER_COMMUNITY_BAN, banStatus)
                    }
                    if (banStatus) {
                        fetchUnbanRequestStatus()
                    }
                }
            }, {

            })
    }

    private fun fetchUnbanRequestStatus() {
        compositeDisposable + DataHandler.INSTANCE.socialRepository
            .getUserUnBanRequestStatus(getStudentId())
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                if (it.error == null && it.data is LinkedTreeMap<*, *>) {
                    val unBanRequestStatus = it.data["Request Status"] as String
                    defaultPrefs().edit {
                        putString(Constants.USER_UNABN_REQUEST_STATE, unBanRequestStatus)
                    }
                }
            }, {

            })
    }

    fun checkUserSurvey(page: String?, type: String?) {
        compositeDisposable + checkSurveyUseCase.execute(CheckSurveyUseCase.Param(page, type))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    _checkUserSurvey.postValue(it)
                },
                {

                }
            )
    }

    fun askIfCanShowDialogOnAppExit() {
        viewModelScope.launch {
            if (appExitDialogExperiment != AppExitDialogFragment.EXPERIMENT_NOT_ENABLED) {
                appExitRepository.askIfCanShowAppExitDialog(
                    appExitDialogExperiment,
                    maxAppExitDialogItems
                )
                    .map { it.data }
                    .catch {
                        _showAppExitDialog = false
                    }
                    .collect {
                        it.coreActionsDoneList?.forEach {
                            userActivityRepository.setCoreActionStatusInPref(it, true)
                        }
                        _showAppExitDialog = it.showOnBackpress == true
                        if (showAppExitDialog) {
                            getAppExitDialogData(appExitDialogExperiment, maxAppExitDialogItems)
                        }
                    }
            }
        }
    }

    fun getAppExitDialogData(experiment: Int, maxItemCount: Int) {
        viewModelScope.launch {
            appExitRepository.getAppExitDialogData(experiment, maxItemCount)
                .map { it.data }
                .catch {
                    _appExitDialogData = null
                }
                .collect {
                    _appExitDialogData = it
                }
        }
    }

    fun resetCoreActions(onSuccess: (StoreActivityResponse) -> Unit = {}) {
        viewModelScope.launch {
            userActivityRepository.resetCoreActions()
                .map { it.data }
                .catch {
                    android.util.Log.e("User_Activity", it.toString())
                }.collect {
                    onSuccess(it)
                }
        }
    }

    fun resetAppOpenCount(onSuccess: (StoreActivityResponse) -> Unit = {}) {
        viewModelScope.launch {
            userActivityRepository.resetAppOpenCountOnBackend()
                .map { it.data }
                .catch {
                    android.util.Log.e("User_Activity", it.toString())
                }.collect {
                    onSuccess(it)
                }
        }
    }

    fun markDailyAttendance() {
        viewModelScope.launch {
            rewardRepository.markAutoDailyAttendance()
                .catch { }
                .collect {
                    /*
                    store API call timestamp
                     */
                    defaultPrefs().edit {
                        putLong(Constants.LAST_MARKED_DAY, System.currentTimeMillis())
                    }
                    //Reset flag for shown unscratched notification
                    defaultPrefs().edit {
                        putBoolean(Constants.IS_FIRST_UNSCRATCHED_SHOWN, false)
                    }
                    _attendanceLiveData.value = it.data
                    it.data.popupData?.let { popupData ->
                        if (popupData.popupHeading != null && popupData.popupDescription != null) {
                            _popupLiveData.value = popupData.apply {
                                isRewardPresent = it.data.isRewardPresent == true
                                isStreakBreak = it.data.isStreakBreak == true
                                isDataOnly = false
                            }
                        }
                        defaultPrefs().edit {
                            putString(
                                Constants.REWARD_POPUP_DATA_AFTER_MARK_ATTENDANCE,
                                gson.toJson(popupData)
                            )
                            putBoolean(Constants.ATTENDANCE_WIDGET_USER_INTERACTION_DONE, false)
                        }
                    }
                    //Save notification data
                    val notificationData: RewardNotificationData? = it.data.notificationData
                    if (notificationData != null) {
                        defaultPrefs().edit {
                            putString(
                                Constants.REWARD_NOTIFICATION_TITLE,
                                notificationData.notificationHeading
                            )
                        }
                        defaultPrefs().edit {
                            putString(
                                Constants.REWARD_NOTIFICATION_DESCRIPTION,
                                notificationData.notificationDescription
                            )
                        }
                    }
                    if (it.data.isStreakBreak) {
                        publishRewardSystemEvent(
                            EventConstants.STREAK_BREAK_ATTENDANCE_MISSED,
                            ignoreSnowplow = true
                        )
                    } else {
                        publishRewardSystemEvent(
                            EventConstants.STREAK_CONTINUE_ACHIEVED,
                            ignoreSnowplow = true
                        )
                    }
                }
        }
    }

    fun getManualAttendancePopupData() {
        viewModelScope.launch {
            rewardRepository.getManualDailyAttendancePopup()
                .catch { }
                .collect {
                    _popupLiveData.value = it.data.popupData?.apply {
                        isAttendanceMarked = it.data.isAttendanceMarked == true
                        isRewardPresent = it.data.isRewardPresent == true
                        isDataOnly = true
                    }
                    if (it.data.isAttendanceMarked == true) {
                        defaultPrefs().edit {
                            putLong(Constants.LAST_MARKED_DAY, System.currentTimeMillis())
                        }
                    }
                }
        }
    }

    fun requestForStudyDost() {
        compositeDisposable.add(
            DataHandler.INSTANCE.studyDostRepository
                .requestForStudyDost()
                .applyIoToMainSchedulerOnSingle()
                .subscribe(
                    {
                        userPreference.updateStudyDostData(it.data)
                        _message.postValue(it.data.description)
                        _studyDost.postValue(it.data)
                        DoubtnutApp.INSTANCE.bus()?.send(UpdateStudyDostWidget())
                    },
                    {
                        it.printStackTrace()
                    }
                ))
    }

    fun isDoubtFeedAvailable() = userPreference.isDoubtFeedAvailable()

    fun getDoubtP2pData() = userPreference.getDoubtP2pData()

    fun getDnrData() = userPreference.getDnrData()

    fun getKheloAurJeetoData() = userPreference.getKheloAurJeetoData()

    fun getDoubtFeed2Data() = userPreference.getDoubtFeed2Data()

    fun getRevisionCornerData() = userPreference.getRevisionCornerData()

    fun getConfigData(sessionCount: Int, postPurchaseSessionCount: Int) {
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
                        _configLiveData.value = Outcome.success(it)
                    }, {
                        _configLiveData.value = Outcome.failure(it)
                    })
    }

    fun getAudioToolTipList() {
        compositeDisposable + DataHandler.INSTANCE.appConfigRepository.getAudioToolTipData()
            .applyIoToMainSchedulerOnSingle().map {
                it.data
            }
            .subscribeToSingle({
                it?.let { putDataToPreferences(it) }
            }, {
                print("")
            })
    }

    private fun putDataToPreferences(audioToolTipList: AudioTooltipResponse) {
        val gson = Gson()
        defaultPrefs().edit()
            .putString(Constants.CAMERA_AUDIO_TOOL_TIP_DATA, gson.toJson(audioToolTipList)).commit()
    }
}
