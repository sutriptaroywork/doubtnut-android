package com.doubtnutapp.gamification.settings.profilesetting.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.observer.SingleEventObserver
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ActivityProfileSettingssBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.gamification.settings.profilesetting.ui.adapter.ProfileSettingAdapter
import com.doubtnutapp.gamification.settings.profilesetting.viewmodel.ProfileSettingViewModel
import com.doubtnutapp.login.ui.activity.LanguageActivity
import com.doubtnutapp.login.ui.activity.StudentLoginActivity
import com.doubtnutapp.login.ui.fragment.ChangeLoginPinDialogFragment
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.networkstats.ui.NetworkStatsActivity
import com.doubtnutapp.screennavigator.*
import com.doubtnutapp.studygroup.viewmodel.SgCreateViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.AdminOptionsDialog
import com.doubtnutapp.utils.ChatUtil
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.extension.observeEvent
import javax.inject.Inject

/**
 * Created by shrreya on 29/6/19.
 */
class ProfileSettingActivity :
    BaseBindingActivity<ProfileSettingViewModel, ActivityProfileSettingssBinding>(),
    ActionPerformer {

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private lateinit var createStudyGroupViewModel: SgCreateViewModel

    private lateinit var adapter: ProfileSettingAdapter

    companion object {
        const val INTENT_EXTRA_SOURCE = "source"

        const val TAG = "ProfileSettingActivity"

        var networkStatsClickCount = 0
    }

    override fun provideViewBinding(): ActivityProfileSettingssBinding {
        return ActivityProfileSettingssBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ProfileSettingViewModel {
        createStudyGroupViewModel = viewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setClickListener()
        setRecyclerView()
        viewModel.getSettingOptions()
        binding.tvAppVersion.text = defaultPrefs(this).getString(Constants.APP_VERSION, "")
        binding.tvAppVersion.setOnClickListener {
            if (FeaturesManager.isFeatureEnabled(
                    this@ProfileSettingActivity,
                    Features.NETWORK_STATS_OPTION
                )
            ) {
                networkStatsClickCount += 1
                if (networkStatsClickCount == 5) {

                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.NETWORK_STATS_ACCESSED
                        )
                    )

                    Toast.makeText(
                        this@ProfileSettingActivity,
                        "Opening Network Usage Statistics",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(
                        Intent(
                            this@ProfileSettingActivity,
                            NetworkStatsActivity::class.java
                        )
                    )
                    networkStatsClickCount = 0
                }
            }
        }
    }

    override fun getStatusBarColor(): Int {
        return R.color.grey_statusbar_color
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_OK == resultCode && ProfileSettingViewModel.LOGIN_REQUEST_CODE == requestCode) {
            viewModel.getSettingOptions()
            setResult(Activity.RESULT_OK)
        } else if (resultCode == Activity.RESULT_CANCELED) {
            if (requestCode == ProfileSettingViewModel.PIP_SETTINGS_REQUEST_CODE) {
                // Need to check here as we are using the PiP settings provided bu system
                // and there are no callbacks provided when it is changed
                viewModel.publishEvent(
                    if (Utils.hasPipModePermission()) {
                        EventConstants.PIP_MODE_MAIN_SETTING_REENABLED
                    } else {
                        EventConstants.PIP_MODE_MAIN_SETTING_DISABLED
                    }, ignoreSnowplow = true
                )
            }
        }
    }

    override fun performAction(action: Any) {
        viewModel.handleAction(action)
    }

    private fun setClickListener() {
        binding.ivBack.setOnClickListener {
            this.onBackPressed()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.navigateLiveData.observe(this) {
            val value = it.getContentIfNotHandled()
            if (value != null) {
                val args: Bundle? = value.hashMap?.toBundle()
                screenNavigator.startActivityFromActivity(this, value.screen, args)
                sendEventByPosition(value.screen)
            }
        }

        viewModel.settingOptionsLiveData.observe(this) {
            updateSettingOptionList(it)
        }

        viewModel.navigateForResultLiveData.observe(this) {
            val value = it.getContentIfNotHandled()
            if (value != null) {
                val args: Bundle? = value.hashMap?.toBundle()
                screenNavigator.startActivityForResultFromActivity(
                    this,
                    value.screen,
                    args,
                    value.requestCode
                )
                sendEventByPosition(value.screen)
            }
        }

        viewModel.logOutUserLiveData.observe(this) {
            it.getContentIfNotHandled()?.let { isUserLoggedOut ->
                if (isUserLoggedOut) {
                    viewModel.publishLogoutEvent()
                    setResult(Activity.RESULT_OK)
                    clearPreferencesForSession()
                    openLoginScreen()

                    MoEngageUtils.logoutUser(applicationContext)
                    ChatUtil.resetUser(applicationContext)
                }
            }
        }

        viewModel.adminOptionsLiveData.observe(this) { event ->
            event.getContentIfNotHandled()?.let {
                if (it) {
                    AdminOptionsDialog().show(supportFragmentManager, "")
                }
            }
        }

        viewModel.shouldShowChangePinDialog.observe(this) {
            ChangeLoginPinDialogFragment.newInstance(Constants.NAVIGATE_LOGOUT)
                .show(supportFragmentManager, ChangeLoginPinDialogFragment.TAG)
        }

        viewModel.createStudyGroupForChatSupport.observe(this, SingleEventObserver {
            if (it) {
                createStudyGroupViewModel.createGroup(
                    groupName = null,
                    groupImage = null,
                    isSupport = true
                )
            }
        })

        createStudyGroupViewModel.groupCreatedLiveData.observeEvent(this) {
            val deeplink = it.groupChatDeeplink ?: return@observeEvent
            deeplinkAction.performAction(this, deeplink)

            val eventMap = hashMapOf<String, Any>()
            eventMap[EventConstants.SOURCE] = EventConstants.PAYMENT
            eventMap[EventConstants.FEATURE] = EventConstants.FEATURE_STUDY_GROUP
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.CHAT_STARTED,
                    eventMap
                )
            )
        }
    }

    private fun openLoginScreen() {
        val languageCode = defaultPrefs().getString(Constants.STUDENT_LANGUAGE_CODE, null)
        val intent: Intent = if (languageCode != null) {
            StudentLoginActivity.getStartIntent(this)
        } else {
            LanguageActivity.getStartIntent(this)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun clearPreferencesForSession() {
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

        defaultPrefs().edit()
            .putInt(Constants.SCREEN_CAMERA, 0).commit()
        defaultPrefs().edit()
            .putInt(Constants.SCREEN_CAMERA_RETURN, 0).commit()
        defaultPrefs().edit()
            .putInt(Constants.SCREEN_LOADING, 0).commit()
        defaultPrefs().edit()
            .putInt(Constants.SCREEN_CROP, 0).commit()
        defaultPrefs().edit()
            .putInt(Constants.SCREEN_MATCH_PAGE, 0).commit()

        defaultPrefs().edit()
            .putString(Constants.CAMERA_AUDIO_TOOL_TIP_DATA, "").commit()
    }

    private fun sendEventByPosition(screen: Screen) {
        var ignoreSnowPlow = false
        val eventName = when (screen) {
            LoginScreen -> EventConstants.EVENT_NAME_LOGIN
            TermsAndConditionsScreen -> EventConstants.EVENT_NAME_TERMS_N_CONDITION
            PrivacyPolicyScreen -> EventConstants.EVENT_NAME_PRIVACY_POLICY
            ContactUsScreen -> EventConstants.EVENT_NAME_CONTACT_US
            AboutUsScreen -> EventConstants.EVENT_NAME_ABOUT_US
            RateUsScreen -> EventConstants.EVENT_NAME_RATE_US
            HowToUseDoubtNutScreen -> EventConstants.EVENT_NAME_HOW_TO_USE_DOUBTNUT_YOUTUBE
            LearnMoreScreen -> EventConstants.EVENT_NAME_HOW_TO_USE_DOUBTNUT_YOUTUBE
            else -> ""
        }
        if (screen == LoginScreen)
            ignoreSnowPlow = true
        viewModel.publishSettingMenuItemClickEvent(eventName, ignoreSnowPlow)

        this.apply {
            (this.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.EVENT_NAME_SETTING_PAGE)
                .track()
        }
    }

    private fun setRecyclerView() {
        adapter = ProfileSettingAdapter(this)
        binding.settingRecyclerView.adapter = adapter
    }

    private fun updateSettingOptionList(settingsOptions: List<ProfileSettingsItems>) {
        adapter.updateData(settingsOptions)
    }
}