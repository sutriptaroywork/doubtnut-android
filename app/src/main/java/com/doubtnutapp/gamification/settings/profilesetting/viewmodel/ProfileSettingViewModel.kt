package com.doubtnutapp.gamification.settings.profilesetting.viewmodel

import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.OnSettingOptionClicked
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToCompletable
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.domain.settings.interactor.IsUserLoggedIn
import com.doubtnutapp.domain.settings.interactor.LogOutUser
import com.doubtnutapp.downloadedVideos.ExoDownloadTracker
import com.doubtnutapp.gamification.event.SettingEventManager
import com.doubtnutapp.gamification.settings.SettingsConstansts
import com.doubtnutapp.gamification.settings.profilesetting.ui.ProfileSettingActivity
import com.doubtnutapp.gamification.settings.profilesetting.ui.ProfileSettingsItems
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.utils.ChatUtil
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by shrreya on 29/6/19.
 */
class ProfileSettingViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val isUserLoggedIn: IsUserLoggedIn,
    private val logOutUser: LogOutUser,
    private val settingEventManager: SettingEventManager,
    private val analyticsPublisher: AnalyticsPublisher
) : BaseViewModel(compositeDisposable) {

    companion object {
        const val LOGIN_REQUEST_CODE = 100
        const val PIP_SETTINGS_REQUEST_CODE = 101
        const val MILLIS_ONE_DAY = 86400000L
    }

    private val _settingOptionsLiveData = MutableLiveData<List<ProfileSettingsItems>>()

    val settingOptionsLiveData: LiveData<List<ProfileSettingsItems>>
        get() = _settingOptionsLiveData

    private val _navigateForResultLiveData = MutableLiveData<Event<NavigationModelForResult>>()

    val navigateForResultLiveData: LiveData<Event<NavigationModelForResult>>
        get() = _navigateForResultLiveData

    private val _logOutUserLiveData = MutableLiveData<Event<Boolean>>()
    val logOutUserLiveData: LiveData<Event<Boolean>>
        get() = _logOutUserLiveData

    private val _adminOptionsLiveData = MutableLiveData<Event<Boolean>>()
    val adminOptionsLiveData: LiveData<Event<Boolean>>
        get() = _adminOptionsLiveData

    private val _shouldShowChangePinDialog = MutableLiveData<Boolean>()
    val shouldShowChangePinDialog: LiveData<Boolean>
        get() = _shouldShowChangePinDialog

    private val _createStudyGroupForChatSupport = MutableLiveData<SingleEvent<Boolean>>()
    val createStudyGroupForChatSupport: LiveData<SingleEvent<Boolean>>
        get() = _createStudyGroupForChatSupport

    private val settingsOptions = arrayListOf(
        ProfileSettingsItems(SettingsConstansts.LOGIN, R.drawable.ic_login, R.string.login),
        ProfileSettingsItems(SettingsConstansts.LOGOUT, R.drawable.ic_logout, R.string.logout),
        ProfileSettingsItems(
            SettingsConstansts.TERMS_AND_CONDITIOND,
            R.drawable.ic_tnc,
            R.string.tnc
        ),
        ProfileSettingsItems(
            SettingsConstansts.PRIVACY_POLICY,
            R.drawable.ic_privacy_policy,
            R.string.privacy_policy
        ),
        ProfileSettingsItems(
            SettingsConstansts.CONTACT_US,
            R.drawable.ic_language,
            R.string.contactus
        ),
        ProfileSettingsItems(SettingsConstansts.ABOUT_US, R.drawable.ic_about_us, R.string.aboutus),
        ProfileSettingsItems(
            SettingsConstansts.RATE_US,
            R.drawable.ic_stars_circle,
            R.string.rateus
        ),
        ProfileSettingsItems(
            SettingsConstansts.HOW_TO_USE_DOUBTNUT,
            R.drawable.ic_how_to_use_doubtnut,
            R.string.howtousedoubtnut
        ),
        ProfileSettingsItems(
            SettingsConstansts.NOTIFICATION_SETTINGS,
            R.drawable.ic_notification_setting,
            R.string.notification_settings
        )
    ).apply {
        when {
            (System.currentTimeMillis() - defaultPrefs().getLong(Constants.PREF_CHAT_START_TIME, 0)
                .or(0)
                    ) in 1 until MILLIS_ONE_DAY -> {
                this.add(
                    ProfileSettingsItems(
                        SettingsConstansts.CHAT_WITH_US,
                        R.drawable.ic_language,
                        R.string.chat_with_us
                    )
                )
            }
            ChatUtil.isChatEnabledForHamburger(DoubtnutApp.INSTANCE.applicationContext) -> {
                defaultPrefs().edit().putLong(Constants.PREF_CHAT_START_TIME, 0).apply()
                this.add(
                    ProfileSettingsItems(
                        SettingsConstansts.CHAT_WITH_US,
                        R.drawable.ic_language,
                        R.string.chat_with_us
                    )
                )
            }
            else -> {
                defaultPrefs().edit().putLong(Constants.PREF_CHAT_START_TIME, 0).apply()
            }
        }
        if (DoubtnutApp.INSTANCE.deviceSupportsPipMode()) {
            this.add(
                ProfileSettingsItems(
                    SettingsConstansts.PIP_MODE_SETTINGS,
                    R.drawable.ic_picture_in_picture_outline,
                    R.string.pip_mode_settings
                )
            )
        }
        if (BuildConfig.ENABLE_ADMIN_OPTIONS) {
            this.add(
                ProfileSettingsItems(
                    SettingsConstansts.ADMIN_OPTIONS,
                    R.drawable.ic_setting_black,
                    R.string.admin_options
                )
            )
        }
    }

    fun handleAction(action: Any) {
        when (action) {
            is OnSettingOptionClicked -> handleOption(action.settingOptionType)
            else -> {
            }
        }
    }

    fun getSettingOptions() {
        isUserLoggedIn
            .execute(Unit)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(::onSuccess)
    }

    private fun handleOption(settingOptionType: String) {
        when (settingOptionType) {
            SettingsConstansts.LOGOUT -> {
                val showChangePinDialog = shouldShowChangePinDialog()
                when (showChangePinDialog.first) {
                    true -> {
                        defaultPrefs().edit {
                            putInt(
                                Constants.NO_OF_CHANGE_PIN_DIALOG_SHOWN,
                                showChangePinDialog.second + 1
                            )
                        }
                        _shouldShowChangePinDialog.postValue(true)
                    }
                    false -> logoutUser()
                }
            }
            SettingsConstansts.ADMIN_OPTIONS -> showAdminOptions()
            SettingsConstansts.CHAT_WITH_US -> {
                if (FeaturesManager.isFeatureEnabled(
                        DoubtnutApp.INSTANCE,
                        Features.STUDY_GROUP_AS_FRESH_CHAT
                    )
                ) {
                    _createStudyGroupForChatSupport.postValue(SingleEvent(true))
                } else {
                    val eventMap = hashMapOf<String, Any>()
                    eventMap[EventConstants.SOURCE] = EventConstants.HAMBURGER
                    eventMap[EventConstants.FEATURE] = EventConstants.FEATURE_FRESHWORKS
                    ChatUtil.setUser(
                        DoubtnutApp.INSTANCE.applicationContext,
                        "", "", "", "", EventConstants.HAMBURGER
                    )
                    ChatUtil.startConversation(DoubtnutApp.INSTANCE.applicationContext)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.CHAT_STARTED,
                            eventMap, ignoreSnowplow = true
                        )
                    )
                }
            }
            else -> openScreen(settingOptionType)
        }
    }

    private fun onSuccess(isLoggedIn: Boolean) {

        //if expression will be return lambda function, this will filter out the option
        val filterFunction: (ProfileSettingsItems) -> Boolean = if (isLoggedIn) {
            { it.settingOptionType != SettingsConstansts.LOGIN }
        } else {
            { it.settingOptionType != SettingsConstansts.LOGOUT }
        }

        _settingOptionsLiveData.value = settingsOptions.filter(filterFunction)

    }

    private fun openScreen(featureType: String) {
        when (val screen = mapScreen(featureType)) {
            is LoginScreen -> {
                _navigateForResultLiveData.value =
                    Event(NavigationModelForResult(screen, null, LOGIN_REQUEST_CODE))
            }
            is PipModeSettingsScreen -> {
                _navigateForResultLiveData.value = Event(
                    NavigationModelForResult(
                        screen, null,
                        PIP_SETTINGS_REQUEST_CODE
                    )
                )
                settingEventManager.eventWith(
                    EventConstants.PIP_MODE_MAIN_SETTINGS_CLICKED,
                    ignoreSnowplow = true
                )
                publishSettingMenuItemClickEvent(
                    EventConstants.PIP_MODE_MAIN_SETTINGS_CLICKED,
                    ignoreSnowplow = true
                )
            }
            else -> {
                _navigateLiveData.value =
                    Event(NavigationModel(screen, if (screen is LearnMoreScreen) {
                        hashMapOf<String, String>().apply {
                            put(
                                ProfileSettingActivity.INTENT_EXTRA_SOURCE,
                                ProfileSettingActivity.TAG
                            )
                        }
                    } else {
                        null
                    }))
            }
        }
    }

    private fun mapScreen(option: String): Screen = when (option) {
        SettingsConstansts.LOGIN -> LoginScreen
        SettingsConstansts.TERMS_AND_CONDITIOND -> TermsAndConditionsScreen
        SettingsConstansts.PRIVACY_POLICY -> PrivacyPolicyScreen
        SettingsConstansts.ABOUT_US -> AboutUsScreen
        SettingsConstansts.RATE_US -> RateUsScreen
        SettingsConstansts.CONTACT_US -> ContactUsScreen
        SettingsConstansts.HOW_TO_USE_DOUBTNUT -> LearnMoreScreen
        SettingsConstansts.NOTIFICATION_SETTINGS -> QuickSearchSetting
        SettingsConstansts.PIP_MODE_SETTINGS -> PipModeSettingsScreen
        else -> NoScreen
    }

    private fun shouldShowChangePinDialog(): Pair<Boolean, Int> {
        val noOfPinChangeDialogShown =
            defaultPrefs().getInt(Constants.NO_OF_CHANGE_PIN_DIALOG_SHOWN, 0)
        val isPinExist = defaultPrefs().getBoolean(Constants.IS_PIN_SET, false)
        return Pair(
            isPinExist.not() && noOfPinChangeDialogShown < Constants.MAX_NO_OF_CHANGE_PIN_DIALOG,
            noOfPinChangeDialogShown
        )
    }

    private fun logoutUser() {
        ExoDownloadTracker.getInstance(DoubtnutApp.INSTANCE.applicationContext).clearAllDownloads()
        logOutUser.execute(Unit)
            .applyIoToMainSchedulerOnCompletable().subscribeToCompletable(::onComplete)
    }

    private fun onComplete() {
        getSettingOptions()
        _logOutUserLiveData.value = Event(true)
    }

    fun publishSettingMenuItemClickEvent(item: String, ignoreSnowplow: Boolean = false) {
        settingEventManager.settingMenuItemClick(item, ignoreSnowplow)
    }

    fun publishLogoutEvent() {
        settingEventManager.settingLogoutClick()
    }

    fun publishEvent(eventName: String, ignoreSnowplow: Boolean = false) {
        settingEventManager.eventWith(eventName, ignoreSnowplow = ignoreSnowplow)
    }

    private fun showAdminOptions() {
        _adminOptionsLiveData.postValue(Event(true))
    }

}