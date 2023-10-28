package com.doubtnutapp.ui.onboarding

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.SimpleItemAnimator
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.ui.helpers.LinearLayoutManagerWithSmoothScroller
import com.doubtnut.core.utils.setVisibleState2
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.OnBoardingLanguageButtonPressed
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.onboarding.model.ApiOnBoardingStep
import com.doubtnutapp.databinding.ActivityOnboardingStepsBinding
import com.doubtnutapp.login.ui.activity.LanguageActivity
import com.doubtnutapp.screennavigator.CameraScreen
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.onboarding.actions.OnBoardingStepClick
import com.doubtnutapp.ui.onboarding.actions.SubmitOnBoardingStepItem
import com.doubtnutapp.ui.onboarding.adapter.OnBoardingStepsAdapter
import com.doubtnutapp.ui.onboarding.viewmodel.OnBoardingStepsViewModel
import com.doubtnutapp.utils.KeyboardUtils
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_onboarding_steps.*
import javax.inject.Inject

class OnBoardingStepsActivity :
    BaseBindingActivity<OnBoardingStepsViewModel, ActivityOnboardingStepsBinding>() {

    companion object {
        const val TAG = "OnBoardingStepsActivity"

        fun getStartIntent(context: Activity) =
            Intent(context, OnBoardingStepsActivity::class.java)
    }

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var mUserPreference: UserPreference

    private var isAlertShown = false

    private var onBoardingStepClickObserver: Disposable? = null

    private val onboardingStepAdapter: OnBoardingStepsAdapter by lazy { OnBoardingStepsAdapter() }

    private var askButtonMessage: Pair<String?, String?>? = null

    override fun provideViewBinding(): ActivityOnboardingStepsBinding {
        return ActivityOnboardingStepsBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): OnBoardingStepsViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.redTomato
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        requestFullScreen()
        enforceLtrLayout()
        super.onCreate(savedInstanceState)
    }

    override fun setupView(savedInstanceState: Bundle?) {

        viewModel.loginVariant = defaultPrefs().getInt(Constants.LOGIN_VARIANT, 1)
        viewModel.languageCode =
            defaultPrefs().getString(Constants.STUDENT_LANGUAGE_CODE, "en") ?: "en"

        postponeEnterTransition()
        KeyboardUtils.hideKeyboard(currentFocus ?: View(this))

        viewModel.getOnBoardingSteps(null, null)
        setClickListeners()

        setUpRecyclerView()
        Utils.saveIsEmulatorAndSafetyNetResponseToPref()
    }

    private fun setClickListeners() {
        binding.askQuestion.setOnClickListener {
            viewModel.submitMultiSelectItems()
        }
    }

    private fun setUpRecyclerView() {
        binding.onboardingStepsList.setHasFixedSize(true)
        (onboardingStepsList.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        onboardingStepsList.layoutManager = LinearLayoutManagerWithSmoothScroller(this)
        onboardingStepsList.adapter = onboardingStepAdapter
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.askButtonText.observe(this) {
            binding.askButtonTextView.text = it
        }

        viewModel.askQuestion.observe(this) {
            toggleVisibilityOfAskQuestion(it)
        }

        viewModel.errorMessage.observe(this) {
            if (!it.isNullOrEmpty()) {
                showToast(this, it, Gravity.BOTTOM, 0, 180)
            }
        }

        viewModel.askButtonMessage.observe(this) {
            askButtonMessage = it
            toggleVisibilityOfAskQuestion(false)
        }

        viewModel.scrollToPosition.observe(this) {
            onboardingStepsList.smoothScrollToPosition(it)
        }

        viewModel.onBoardingStepsLiveDataEntity.observe(this) {
            updateOnBoardingSteps(it)
        }

        viewModel.navigateLiveData.observe(this) {
            val navigationData = it.getContentIfNotHandled()
            if (navigationData != null && navigationData.screen == CameraScreen) {
                // Update Localisation before entering into app
                LocaleManager.setLocale(this)
                defaultPrefs(this).edit().putBoolean(Constants.ONBOARDING_COMPLETED, true).apply()
                viewModel.sendEvent(EventConstants.EVENT_NAME_ONBOARDING_COMPLETED, hashMapOf())
                showCameraPageFirst()
                finish()
            }
        }

        viewModel.progressLiveData.observe(this) { singleEvent ->
            singleEvent.getContentIfNotHandled()?.let { visible ->
                binding.progressBar.setVisibleState2(visible)
            }
        }

        onBoardingStepClickObserver =
            DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { onBoardingObserver ->
                if (onBoardingObserver is OnBoardingStepClick) {
                    val itemPosition = onBoardingObserver.position
                    viewModel.onStepClick(itemPosition)
                }
                if (onBoardingObserver is SubmitOnBoardingStepItem) {
                    val position = onBoardingObserver.position
                    val onboardingStepItem = onBoardingObserver.onBoardingStepItem
                    viewModel.submitOnBoardingStep(
                        position,
                        onboardingStepItem,
                        onBoardingObserver.isMultiSelect
                    )
                }
                if (onBoardingObserver is OnBoardingLanguageButtonPressed) {
                    viewModel.sendEvent(
                        EventConstants.LANGUAGE_CHANGE_BUTTON_CLICKED,
                        hashMapOf(),
                        ignoreSnowplow = true
                    )
                    startActivityForResult(
                        LanguageActivity.getStartIntent(this, TAG),
                        LanguageActivity.REQUEST_CODE
                    )
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            LanguageActivity.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    toggleVisibilityOfAskQuestion(enable = false)
                    LocaleManager.updateResources(this, mUserPreference.getSelectedLanguage())
                    viewModel.languageCode = mUserPreference.getSelectedLanguage()
                    viewModel.getOnBoardingSteps(null, null)
                    viewModel.sendEvent(
                        EventConstants.LANGUAGE_CHANGE_POP_UP_CLOSE,
                        hashMapOf(),
                        ignoreSnowplow = true
                    )
                }
            }
        }
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgressBarState(@Suppress("UNUSED_PARAMETER") state: Boolean) {}

    private fun updateOnBoardingSteps(steps: List<ApiOnBoardingStep>) {
        onboardingStepAdapter.updateFeeds(steps)
    }

    private fun toggleVisibilityOfAskQuestion(enable: Boolean) {
        if (enable) {
            binding.askQuestion.setBackgroundColor(ContextCompat.getColor(this, R.color.tomato))
            binding.tvAskButtonSutTitle.text = askButtonMessage?.first

        } else {
            binding.askQuestion.setBackgroundColor(
                ContextCompat.getColor(
                    this,
                    R.color.onboarding_ask_button_inactive
                )
            )
            binding.tvAskButtonSutTitle.text = askButtonMessage?.second
        }
    }

    private fun showCameraPageFirst() {
        val intent = Intent(this, MainActivity::class.java)
        if (CoreApplication.pendingDeeplink.isNullOrEmpty()) {
            intent.action = Constants.NAVIGATE_CAMERA_SCREEN
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        this.startActivity(intent)
    }

    override fun onBackPressed() {
        viewModel.sendEvent(
            EventConstants.BACK_PRESS_FROM_ONBOARDING,
            hashMapOf<String, Any>().apply {
                put(EventConstants.ONBOARDING_TYPE, "b")
            }, ignoreSnowplow = true
        )
        if (viewModel.currentExpandedItem > 1) {
            viewModel.moveToPreviousCard()
        } else {
            if (!isAlertShown) {
                showToast(this, getString(R.string.back_press_message), Gravity.CENTER)
                isAlertShown = true
            } else {
                super.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        onBoardingStepClickObserver?.dispose()
    }

}