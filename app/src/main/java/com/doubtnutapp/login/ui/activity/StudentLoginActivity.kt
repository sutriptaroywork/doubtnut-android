package com.doubtnutapp.login.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.core.view.ViewCompat.animate
import androidx.core.view.ViewPropertyAnimatorListener
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import com.apxor.androidsdk.core.Attributes
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.LottieAnimDataStore
import com.doubtnut.core.data.LottieAnimDatastoreImpl
import com.doubtnut.core.entitiy.SingleEvent
import com.doubtnut.core.observer.SingleEventObserver
import com.doubtnut.core.utils.LottieAnimationViewUtils.applyAnimationFromAsset
import com.doubtnut.core.utils.ThreadUtils
import com.doubtnut.core.utils.setMargins
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.Constants.IS_LOGIN_BACK_PRESS_DIALOG_SHOWN
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.AnimateBottomSheet
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.ActivityStudentLoginBinding
import com.doubtnutapp.deeplink.AppActions
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.login.LoginNavigation
import com.doubtnutapp.login.ui.fragment.LoginBackPressDialogFragment
import com.doubtnutapp.login.ui.fragment.MissCallVerificationFragment
import com.doubtnutapp.login.ui.fragment.StudentOtpFragment
import com.doubtnutapp.login.ui.fragment.StudentPhoneFragment
import com.doubtnutapp.login.viewmodel.LoginViewModel
import com.doubtnutapp.moengage.MoEngageUtils
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.onboarding.OnBoardingStepsActivity
import com.doubtnutapp.utils.*
import com.doubtnutapp.utils.UserUtil.getStudentClass
import com.doubtnutapp.widgets.LockableBottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.snowplowanalytics.snowplow.Snowplow
import com.truecaller.android.sdk.*
import com.truecaller.android.sdk.clients.VerificationCallback
import com.truecaller.android.sdk.clients.VerificationDataBundle
import com.uxcam.UXCam
import dagger.Lazy
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 2020-06-17.
 */
class StudentLoginActivity : BaseBindingActivity<LoginViewModel, ActivityStudentLoginBinding>(),
    LoginListener,
    VerificationCallback, ITrueCallback, ActionPerformer2,
    LoginBackPressDialogFragment.CallBackFromDialog {

    companion object {
        const val TAG = "StudentLoginActivity"
        const val FROM_SCREEN = "from_screen"
        const val MY_PERMISSIONS_REQUEST_ALL = 1001
        fun getStartIntent(context: Context, fromScreen: String? = null) =
            Intent(context, StudentLoginActivity::class.java).apply {
                putExtra(FROM_SCREEN, fromScreen)
            }
    }

    @Inject
    lateinit var mUserPreference: Lazy<UserPreference>

    @Inject
    lateinit var lottieAnimDataStore: Lazy<LottieAnimDataStore>

    @Inject
    lateinit var deeplinkAction: Lazy<DeeplinkAction>

    @Inject
    lateinit var analyticsPublisher: Lazy<AnalyticsPublisher>

    private val firebaseAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance().apply {
            useAppLanguage()
        }
    }
    private lateinit var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var loginBottomSheetBehaviour: BottomSheetBehavior<*>

    private var bottomSheetPeekHeight: Int = 0

    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var verificationId: String? = null

    private var alertDialogShown = false
    private var timer: CountDownTimer? = null
    private var isTimeActive = false

    private val trueCallerWaitTime: Long by lazy {
        defaultPrefs(this).getLong(Constants.TRUE_CALLER_WAIT_TIME, 15) * 1000
    }

    private var isBottomSheetAnimating = false
    private var trueCallerLoginType: String? = null

    private val fromScreen: String? by lazy {
        intent.getStringExtra(FROM_SCREEN)
    }
    private var loginBackPressDialogFragment: LoginBackPressDialogFragment? = null

    // region Truecaller flags
    private val enableTrueCaller: Boolean by lazy {
        defaultPrefs().getBoolean(Constants.ENABLE_TRUECALLER_VERIFICATION, true)
    }
    private val enableMissedCallVerification: Boolean by lazy {
        defaultPrefs().getBoolean(Constants.ENABLE_MISSED_CALL_VERIFICATION, true)
    }
    // end region

    override fun provideViewBinding(): ActivityStudentLoginBinding =
        ActivityStudentLoginBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): LoginViewModel = viewModelProvider(viewModelFactory)

    override fun onCreate(savedInstanceState: Bundle?) {
        requestFullScreen()
        enforceLtrLayout()
        super.onCreate(savedInstanceState)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        if (enableTrueCaller || enableMissedCallVerification) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                initTrueCallerSdk()
            }
        }

        initPhoneAuthCallback()
        setupUi()

        viewModel.handleDeepLink()
        viewModel.checkForTrueCallerApp()
        viewModel.publishEventWith(EventConstants.WALK_THROUGH_PAGE_OPENED, hashMapOf(), true)

        // add phone fragment as first screen
        val studentPhoneFragment = StudentPhoneFragment.newInstance()
        studentPhoneFragment.setUpLoginListener(this)
        addFragment(studentPhoneFragment, StudentPhoneFragment.TAG)
    }

    private fun setupUi() {
        binding.ivLogoCenter.isInvisible = true
        binding.ivLogo.show()
        binding.buttonLanguage.apply {
            show()
            text = mUserPreference.get().getSelectedDisplayLanguage()
            setOnClickListener {
                viewModel.publishEventWith(
                    EventConstants.LANGUAGE_CHANGE_BUTTON_CLICKED,
                    ignoreSnowplow = true
                )
                startActivityForResult(
                    LanguageActivity.getStartIntent(this@StudentLoginActivity, TAG),
                    LanguageActivity.REQUEST_CODE
                )
            }
        }
        setUpOnboardingAnimation()
        setUpLoginBottomSheet()
        setUpClickListeners()
        handleBranchDeepLinkForReferral()
    }

    override fun onResume() {
        super.onResume()
        binding.lottieAnimation.apply {
            if (isAnimating.not()) {
                resumeAnimation()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.lottieAnimation.apply {
            if (isAnimating) {
                pauseAnimation()
            }
        }
    }

    private fun handleBranchDeepLinkForReferral() {
        if (AppUtils.isImmediateUpdate()) {
            return
        }

        extractDeeplinkFromBranchIOAndLaunch()
    }

    private fun extractDeeplinkFromBranchIOAndLaunch() {
        val referringParams = BranchIOUtils.getReferringParam(applicationContext)
        if (!referringParams.isNullOrBlank()) {
            if (!AppUtils.isImmediateUpdate()) {
                val deeplink = BranchDeeplinkHandler.getParsedDeeplink(
                    applicationContext,
                    referringParams,
                    (applicationContext as DoubtnutApp).getEventTracker(),
                    analyticsPublisher.get()
                )
                when {
                    deeplink != null -> {
                        // only launch refer a friend deeplink through Student login page
                        // because rest of deeplinks will be launched from MainActivity page after login
                        if (deeplink.contains(AppActions.REFERRAL_CODE_SHARE.toString())) {
                            deeplinkAction.get().performAction(
                                this,
                                deeplink,
                                EventConstants.PAGE_DEEPLINK_CLICK
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        stopAnimation()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            LanguageActivity.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    LocaleManager.updateResources(this, mUserPreference.get().getSelectedLanguage())
                    viewModel.languageChangedLiveData.value = true
                    viewModel.publishEventWith(
                        EventConstants.LANGUAGE_CHANGE_POP_UP_CLOSE,
                        ignoreSnowplow = true
                    )
                }
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.checkForLoginTypeLiveData.observe(this) { loginViewType ->
            trueCallerLoginType = loginViewType
        }

        viewModel.navigationLiveData.observe(this, SingleEventObserver { navigation ->
            when (navigation) {
                is LoginNavigation.OtpScreen -> {
                    showOtpFragment()
                }
                is LoginNavigation.MainScreen -> showMainScreen()
                is LoginNavigation.OnBoardingScreen -> showOnBoardingScreen()
                else -> {}
            }
        })

        viewModel.messageLiveData.observe(this, SingleEventObserver { message ->
            toast(message)
        })

        viewModel.requestOtpLiveData.observe(this, SingleEventObserver { phoneNumber ->
            viewModel.enableFirebaseOtp(true)
            startPhoneNumberVerification(phoneNumber)
            viewModel.publishEventWith(EventConstants.EVENT_NAME_FIREBASE_OTP_TRIGGERED)
        })

        viewModel.verificationCodeLiveData.observe(this, SingleEventObserver { verificationCode ->
            verifyPhoneNumberWithCode(verificationCode)
        })

        viewModel.studentIdLiveData.observe(this, SingleEventObserver { studentId ->
            ApxorUtils.setUserIdentifier(studentId)
            val userInfo = Attributes()
            userInfo.putAttribute("class", getStudentClass())
            userInfo.putAttribute("studentId", studentId)
            userInfo.putAttribute(
                "language",
                defaultPrefs(this).getString(Constants.STUDENT_LANGUAGE_NAME, "")
            )
            ApxorUtils.setUserCustomInfo(userInfo)

            ThreadUtils.runOnAnalyticsThread {
                Snowplow.getDefaultTracker()?.subject?.userId = studentId
            }

            if (UXCamUtil.shouldStart(Constants.UXCAM_PERCENT, this)) {
                UXCam.setUserIdentity(studentId)
                UXCam.setUserProperty(
                    Constants.LANG_CODE,
                    defaultPrefs(this).getString(Constants.STUDENT_LANGUAGE_CODE, "")
                )
                UXCam.setUserProperty(Constants.STUDENT_CLASS, getStudentClass())
            }

            MoEngageUtils.setUniqueId(applicationContext, studentId)
            MoEngageUtils.sendPushToken(
                applicationContext,
                defaultPrefs(this).getString(Constants.GCM_REG_ID, "") ?: ""
            )
        })

        viewModel.nameEntryForTrueCaller.observe(this) {
            verifyUserWithTruecaller(it.first, it.second)
        }

        viewModel.messageStringIdLiveData.observe(this, SingleEventObserver { messageStringId ->
            toast(messageStringId)
        })

        viewModel.isFirebaseOtpEnable.observe(this) {
            if (it) loginBackPressDialogFragment?.dismiss()
        }

        viewModel.languageChangedLiveData.observe(this) {
            if (it) {
                binding.buttonLanguage.text = mUserPreference.get().getSelectedDisplayLanguage()
                binding.tvTitle.setText(R.string.click_crop_solve_your_doubt)
            }
        }
    }

    private fun initTruecallerVerification() {
        startTrueCallerTimer()
        TruecallerSDK.getInstance().requestVerification("IN", viewModel.phoneNumber, this, this)
    }

    private fun setUpClickListeners() {

        binding.rootLayout.setOnClickListener(object : DebouncedOnClickListener(600) {
            override fun onDebouncedClick(v: View?) {
                animateBottomSheet()
            }
        })

        binding.loginLayout.loginBottomSheet.setOnClickListener {
            loginBottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is AnimateBottomSheet -> {
                animateBottomSheet()
                viewModel.publishEventWith(EventConstants.ASK_FIRST_DOUBT_CLICK, hashMapOf(), true)
            }
            else -> {
            }
        }
    }

    private fun animateBottomSheet() {

        if (isBottomSheetAnimating || loginBottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) return

        isBottomSheetAnimating = true

        animate(binding.loginLayout.loginBottomSheet)
            .translationYBy(-60f)
            .setDuration(300)
            .setListener(object : ViewPropertyAnimatorListener {

                override fun onAnimationEnd(view: View?) {

                    if (view == null) return

                    animate(view)
                        .translationYBy(60f)
                        .setDuration(200)
                        .setListener(null)

                    isBottomSheetAnimating = false

                }

                override fun onAnimationCancel(view: View?) {}

                override fun onAnimationStart(view: View?) {}

            })
    }

    private fun setUpLoginBottomSheet() {
        loginBottomSheetBehaviour =
            LockableBottomSheetBehavior.from(binding.loginLayout.loginBottomSheet)
        loginBottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED

        bottomSheetPeekHeight = loginBottomSheetBehaviour.peekHeight

        loginBottomSheetBehaviour.setBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {

            override fun onSlide(p0: View, slideOffset: Float) {}

            override fun onStateChanged(p0: View, p1: Int) {
                when (p1) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        binding.loginLayout.loginBottomSheet.setMargins(0, 0, 0, 0)
                        viewModel.resetFields.postValue(true)
                        viewModel.isBottomSheetExpanded.postValue(false)
                        moveToPhoneFragment()
                    }

                    BottomSheetBehavior.STATE_DRAGGING -> {
                        binding.loginLayout.loginBottomSheet.setMargins(0, 70, 0, 0)
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        viewModel.isBottomSheetExpanded.postValue(true)
                        binding.loginLayout.loginBottomSheet.setMargins(0, 70, 0, 0)

                        val phoneFragment =
                            supportFragmentManager.findFragmentByTag(StudentPhoneFragment.TAG)
                        if (phoneFragment != null && phoneFragment.isVisible) {
                            (loginBottomSheetBehaviour as LockableBottomSheetBehavior).setLocked(
                                true
                            )
                        } else {
                            (loginBottomSheetBehaviour as LockableBottomSheetBehavior).setLocked(
                                false
                            )
                        }
                        viewModel.resetFields.postValue(false)
                        val showTrueCallerPopUp =
                            trueCallerLoginType == Constants.SHOW_TRUE_CALLER_LOGIN
                                    && enableTrueCaller
                                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
                        viewModel.showTrueCallerPopUp.postValue(SingleEvent(showTrueCallerPopUp))
                    }

                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                    }

                    BottomSheetBehavior.STATE_HIDDEN -> {
                    }

                    BottomSheetBehavior.STATE_SETTLING -> {
                    }
                }
            }
        })
    }

    private fun setUpOnboardingAnimation() {
        binding.apply {
            tvTitle.text = getString(R.string.click_crop_solve_your_doubt)
            lottieAnimation.apply {
                applyAnimationFromAsset(
                    animationFile = "lottie_login_screen_animation.zip",
                    repeatCount = 0,
                    onAnimationEnd = { expandBottomSheet(true) })

                setOnClickListener(object : DebouncedOnClickListener(600) {
                    override fun onDebouncedClick(v: View?) {
                        performAction(AnimateBottomSheet)
                    }
                })
            }
        }
    }

    private fun stopAnimation() {
        if (!binding.lottieAnimation.isAnimating) {
            binding.lottieAnimation.clearAnimation()
        }
    }

    private fun addFragment(fragment: Fragment, tag: String, backStack: Boolean = true) {

        if (supportFragmentManager.isDestroyed) return

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragmentContainer, fragment, tag)
            .apply { if (backStack) addToBackStack(tag) }
            .commitAllowingStateLoss()
        supportFragmentManager.beginTransaction()
    }

    private fun moveToPhoneFragment() {

        if (supportFragmentManager.isDestroyed) return

        viewModel.enableFirebaseOtp(false)
        viewModel.showOtherLoginOption.postValue(false)
        viewModel.enableFirebaseOtp(false)

        // Remove all the fragments from back stack up to StudentPhoneFragment
        try {
            supportFragmentManager.popBackStackImmediate(StudentPhoneFragment.TAG, 0)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }

        if (loginBottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
            (loginBottomSheetBehaviour as LockableBottomSheetBehavior).setLocked(true)
        } else {
            (loginBottomSheetBehaviour as LockableBottomSheetBehavior).setLocked(false)
        }
    }

    private fun dragLoginBottomSheet(expand: Boolean) {
        if (expand) {
            binding.loginLayout.loginBottomSheet.setMargins(0, 70, 0, 0)
            loginBottomSheetBehaviour.state = BottomSheetBehavior.STATE_EXPANDED
            viewModel.publishEventWith(EventConstants.LOGIN_PAGE_BOTTOM_SHEET_OPENED, true)
        } else {
            binding.loginLayout.loginBottomSheet.setMargins(0, 0, 0, 0)
            loginBottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
            viewModel.publishEventWith(
                EventConstants.LOGIN_PAGE_BOTTOM_SHEET_CLOSED,
                ignoreSnowplow = true
            )
        }
    }

    private fun showOtpFragment() {
        val currentFragment = supportFragmentManager.findFragmentByTag(StudentOtpFragment.TAG)
        if (currentFragment == null || !currentFragment.isVisible) {
            viewModel.showOtherLoginOption.postValue(false)
            val studentOtpFragment = StudentOtpFragment.newInstance()
            studentOtpFragment.setUpLoginListener(this)
            replaceFragment(studentOtpFragment, StudentOtpFragment.TAG)
        }
    }

    private fun showMainScreen() {
        // Update Localisation before entering into app

        viewModel.fetchAppWideNavIcons()

        LocaleManager.setLocale(this)
        KeyboardUtils.hideKeyboard(currentFocus ?: View(this))
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .apply {
                putExtra("hasToShowCamera", true)
            })
        finish()
    }

    private fun showOnBoardingScreen() {
        KeyboardUtils.hideKeyboard(currentFocus ?: View(this))
        if (intent.action != null && intent.action == Constants.NAVIGATE_ON_BOARDING) {
            setResult(Activity.RESULT_OK)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            finish()
        } else {
            val intent = Intent(
                this,
                OnBoardingStepsActivity::class.java
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

            startActivity(intent)
            finish()
        }
    }

    override fun replaceFragment(fragment: Fragment, tag: String) {
        addFragment(fragment, tag)
    }

    override fun expandBottomSheet(expand: Boolean) {
        dragLoginBottomSheet(expand)
    }

    override fun startTruecallerVerification() {
        initTruecallerVerification()
    }

    override fun getBottomSheetState(): Int = loginBottomSheetBehaviour.state

    private fun openBackPressDialog(
        animationDatastoreKey: String,
        @StringRes title: Int,
        @StringRes subtitle: Int?,
        @StringRes cta1Text: Int,
        @StringRes cta2Text: Int?,
        fromScreen: String,
    ) {

        loginBackPressDialogFragment = LoginBackPressDialogFragment
            .newInstance(
                animationDatastoreKey = animationDatastoreKey,
                title = title,
                subTitle = subtitle,
                cta1Text = cta1Text,
                cta2Text = cta2Text,
                fromScreen = fromScreen
            ).apply {
                setCallBackListener(this@StudentLoginActivity)
            }
        loginBackPressDialogFragment?.show(supportFragmentManager, LoginBackPressDialogFragment.TAG)
    }

    override fun onBackPressed() {

        val isBackPressDialogShown =
            defaultPrefs().getBoolean(IS_LOGIN_BACK_PRESS_DIALOG_SHOWN, false)

        when (getCurrentFragment()) {

            is MissCallVerificationFragment -> {
                if (viewModel.getMissedCallState() != null
                    && viewModel.getMissedCallState() == VerificationCallback.TYPE_MISSED_CALL_RECEIVED
                    && !alertDialogShown
                ) {
                    showAlert()
                    return
                } else {
                    stopTrueCallerTimer()
                }
            }

            is StudentOtpFragment -> {
                if (!isBackPressDialogShown && viewModel.isFirebaseOtpEnable.value != true) {

                    openBackPressDialog(
                        animationDatastoreKey = LottieAnimDatastoreImpl.KEY_OTP_BACK_PRESS_ANIMATION,
                        title = R.string.fragment_otp_back_press_title,
                        subtitle = R.string.fragment_otp_back_press_sub_title,
                        cta1Text = R.string.resend_otp,
                        cta2Text = null,
                        fromScreen = StudentOtpFragment.TAG
                    )

                    defaultPrefs().edit {
                        putBoolean(IS_LOGIN_BACK_PRESS_DIALOG_SHOWN, true)
                    }
                    return
                }
            }

            is StudentPhoneFragment -> {
                if (loginBottomSheetBehaviour.state == BottomSheetBehavior.STATE_EXPANDED) {
                    if (!isBackPressDialogShown) {

                        openBackPressDialog(
                            animationDatastoreKey = LottieAnimDatastoreImpl.KEY_PHONE_BACK_PRESS_ANIMATION,
                            title = R.string.fragment_phone_back_press_title,
                            subtitle = null,
                            cta1Text = R.string.fragment_phone_back_press_button_text,
                            cta2Text = R.string.join_as_a_guest,
                            fromScreen = StudentPhoneFragment.TAG
                        )

                        defaultPrefs().edit {
                            putBoolean(IS_LOGIN_BACK_PRESS_DIALOG_SHOWN, true)
                        }
                    } else {
                        loginBottomSheetBehaviour.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                    return
                } else {
                    if (fromScreen == null) {
                        defaultPrefs().edit {
                            putBoolean(IS_LOGIN_BACK_PRESS_DIALOG_SHOWN, false)
                        }
                    } else {
                        LanguageActivity.getStartIntent(this)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                            .apply {
                                startActivity(this)
                            }
                    }
                    finish()
                }
            }

            else -> {
                super.onBackPressed()
            }
        }

        super.onBackPressed()
        moveToPhoneFragment()
    }

    private fun showAlert() {

        viewModel.publishEventWith(
            EventConstants.EVENT_NAME_TRUE_CALLER_NAME_BACK_PRESS,
            ignoreSnowplow = true
        )

        alertDialogShown = true

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.true_caller_dialog_message))
        builder.setCancelable(false)
        builder.setPositiveButton(getString(R.string.true_caller_dialog_yes)) { dialog, which ->
            toast(R.string.err_msg_fail_truecaller_verification)
            viewModel.resetFields.postValue(true)
            moveToPhoneFragment()
        }
        builder.setNegativeButton(getString(R.string.true_caller_dialog_no)) { dialog, which -> // If user click no
            dialog?.cancel()
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun initTrueCallerSdk() {
        val trueScope: TruecallerSdkScope = TruecallerSdkScope.Builder(this, this)
            .consentMode(TruecallerSdkScope.CONSENT_MODE_POPUP)
            .consentTitleOption(TruecallerSdkScope.SDK_CONSENT_TITLE_VERIFY)
            .footerType(TruecallerSdkScope.FOOTER_TYPE_SKIP)
            .sdkOptions(TruecallerSdkScope.SDK_OPTION_WITH_OTP)
            .build()

        TruecallerSDK.init(trueScope)
    }

    private fun initPhoneAuthCallback() {
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                if (!p0.smsCode.isNullOrBlank()) {
                    viewModel.otpField.value = p0.smsCode
                }
                signInWithPhoneAuthCredential(p0)
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                viewModel.isLoading.postValue(false)
                if (p0 is FirebaseAuthInvalidCredentialsException) {
                    toast(R.string.somethingWentWrong)
                } else if (p0 is FirebaseTooManyRequestsException) {
                    toast(R.string.loginLimitExceeded)
                }
                viewModel.showOtherLoginOption.postValue(true)
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationId = p0
                resendToken = p1
                viewModel.showOtherLoginOption.postValue(false)
                viewModel.isLoading.postValue(false)
                if (viewModel.countryCode != Constants.COUNTRY_CODE_INDIA) {
                    viewModel.startTimerWithBuffer.postValue(SingleEvent(Pair(true, null)))
                    showOtpFragment()
                }
            }
        }
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        if (resendToken != null) {
            resendVerificationCode(phoneNumber)
        } else {
            sendVerificationCode(phoneNumber)
        }
    }

    private fun sendVerificationCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            30,
            TimeUnit.SECONDS,
            this,
            callback,
            resendToken
        )
    }

    private fun resendVerificationCode(phoneNumber: String) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber,
            30,
            TimeUnit.SECONDS,
            this,
            callback,
            resendToken
        )
    }

    private fun verifyPhoneNumberWithCode(code: String) {

        if (verificationId == null) {
            toast(R.string.somethingWentWrong)
            viewModel.showOtherLoginOption.postValue(true)
            return
        }

        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(authCredential: PhoneAuthCredential?) {

        viewModel.isLoading.postValue(true)

        if (authCredential == null) {
            return
        }

        firebaseAuth.signInWithCredential(authCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    task.result?.user?.getIdToken(true)?.addOnCompleteListener {
                        viewModel.requestFireBaseLoginToServer(it.result?.token ?: "")
                    }
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        toast(R.string.invalidate_otp)
                    }
                    viewModel.isLoading.postValue(false)
                    viewModel.otpField.postValue("")
                }
            }
            .addOnFailureListener(this) {
                it.message?.let { toast(it) }
            }
    }

    /**
     * Timer for truecaller verification
     * If it finishes - stop timer and continue with OTP flow
     */
    private fun startTrueCallerTimer() {
        isTimeActive = true

        timer = object : CountDownTimer(trueCallerWaitTime, trueCallerWaitTime) {

            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                toast(R.string.err_msg_fail_truecaller_verification)
                viewModel.resetFields.postValue(true)
                viewModel.publishEventWith(
                    EventConstants.EVENT_NAME_TRUE_CALLER_CALL_TIME_OUT,
                    ignoreSnowplow = true
                )
                moveToPhoneFragment()
            }
        }

        timer?.start()
    }

    /**
     * This Method stop timer and
     * change active state of timer to false
     */
    private fun stopTrueCallerTimer() {
        timer?.cancel()
        isTimeActive = false
    }

    /**
     * pass first and last name to truecaller to verify,
     * if it fails, continue to OTP flow
     */
    private fun verifyUserWithTruecaller(firstName: String, lastName: String) {
        val profile: TrueProfile = TrueProfile.Builder(firstName, lastName).build()
        TruecallerSDK.getInstance().verifyMissedCall(profile, this)
        viewModel.publishEventWith(
            EventConstants.EVENT_NAME_TRUECALLER_NAME_VERIFICATION_STARTED,
            ignoreSnowplow = true
        )
    }

    override fun onRequestFailure(p0: Int, p1: TrueException) {
        viewModel.isLoading.postValue(false)
        viewModel.publishEventWith(
            EventConstants.EVENT_NAME_TRUECALLER_FAIL_OTP,
            ignoreSnowplow = true
        )
        toast(R.string.err_msg_fail_truecaller_verification)

        stopTrueCallerTimer()

        viewModel.resetFields.postValue(true)

        moveToPhoneFragment()
    }

    override fun onRequestSuccess(requestCode: Int, extras: VerificationDataBundle?) {

        viewModel.isLoading.postValue(false)

        when (requestCode) {

            // miss call initiated
            VerificationCallback.TYPE_MISSED_CALL_INITIATED -> {
                replaceFragment(
                    MissCallVerificationFragment.newInstance(),
                    MissCallVerificationFragment.TAG
                )
                (loginBottomSheetBehaviour as LockableBottomSheetBehavior).setLocked(true)
                KeyboardUtils.hideKeyboard(currentFocus ?: View(this))
                viewModel.setMissedCallState(VerificationCallback.TYPE_MISSED_CALL_INITIATED)
                viewModel.publishEventWith(
                    EventConstants.EVENT_NAME_TRUECALLER_VERIFICATION_STATUS,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.STATUS, "TYPE_MISSED_CALL_INITIATED")
                    }, ignoreSnowplow = true
                )
            }

            // miss call received - stop timer
            VerificationCallback.TYPE_MISSED_CALL_RECEIVED -> {

                stopTrueCallerTimer()

                viewModel.setMissedCallState(VerificationCallback.TYPE_MISSED_CALL_RECEIVED)
                viewModel.publishEventWith(
                    EventConstants.EVENT_NAME_TRUECALLER_VERIFICATION_STATUS,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.STATUS, "TYPE_MISSED_CALL_RECEIVED")
                    }, ignoreSnowplow = true
                )
            }

            // miss call canceled
            VerificationCallback.TYPE_OTP_INITIATED -> {
                stopTrueCallerTimer()
            }

            VerificationCallback.TYPE_OTP_RECEIVED -> {
            }

            // user verification completed
            VerificationCallback.TYPE_VERIFICATION_COMPLETE -> {

                val accessToken = extras?.getString(VerificationDataBundle.KEY_ACCESS_TOKEN)

                viewModel.verifyTrueCallerLogin("", "", true, accessToken)

                viewModel.publishEventWith(
                    EventConstants.EVENT_NAME_TRUECALLER_VERIFICATION_STATUS,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.STATUS, "TYPE_VERIFICATION_COMPLETE")
                    }, ignoreSnowplow = true
                )
            }

            // user already verified on truecaller
            VerificationCallback.TYPE_PROFILE_VERIFIED_BEFORE -> {

                viewModel.publishEventWith(
                    EventConstants.EVENT_NAME_TRUECALLER_VERIFICATION_STATUS,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.STATUS, "TYPE_PROFILE_VERIFIED_BEFORE")
                    }, ignoreSnowplow = true
                )

                val accessToken = extras?.profile?.accessToken

                viewModel.verifyTrueCallerLogin("", "", true, accessToken)
            }
        }
    }

    override fun onFailureProfileShared(p0: TrueError) {
        viewModel.isLoading.postValue(false)
        viewModel.publishEventWith(
            EventConstants.EVENT_NAME_TRUE_CALLER_PROFILE_SHARED_ERROR,
            hashMapOf<String, Any>().apply {
                put(EventConstants.ERROR_TYPE, p0.errorType)
            }, ignoreSnowplow = true
        )
        viewModel.resetFields.postValue(true)
        moveToPhoneFragment()
        toast(R.string.err_msg_fail_truecaller_verification)
    }

    override fun onSuccessProfileShared(p0: TrueProfile) {
        viewModel.isLoading.postValue(false)
        viewModel.publishEventWith(
            EventConstants.EVENT_NAME_TRUE_CALLER_PROFILE_SHARED_SUCCESS,
            hashMapOf()
        )
        viewModel.verifyTrueCallerLogin(p0.payload, p0.signature, false, null)
    }

    override fun onVerificationRequired(p0: TrueError?) {
        viewModel.isLoading.postValue(false)
        viewModel.publishEventWith(
            EventConstants.EVENT_NAME_TRUE_CALLER_PROFILE_SHARED_ERROR,
            hashMapOf(),
            ignoreSnowplow = true
        )
        viewModel.resetFields.postValue(true)
        moveToPhoneFragment()
    }

    override fun resendOtp() {
        viewModel.resendOtp()
    }

    override fun guestLogin() {
        viewModel.addAnonymousUser(viewModel.getSelectedLocale(), "login")
        viewModel.publishEventWith(
            EventConstants.EVENT_NAME_GUEST_LOGIN_INITIATED
        )
    }
}

interface LoginListener {
    fun replaceFragment(fragment: Fragment, tag: String)
    fun expandBottomSheet(expand: Boolean)
    fun startTruecallerVerification()
    fun getBottomSheetState(): Int
}