package com.doubtnutapp.login.ui.fragment

import android.content.Context
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.LottieAnimDataStore
import com.doubtnut.core.observer.SingleEventObserver
import com.doubtnut.core.utils.LottieAnimationViewUtils.applyAnimationFromUrl
import com.doubtnut.core.utils.PermissionUtils
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.dpToPx
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.base.OpenWhatsapp
import com.doubtnutapp.databinding.FragmentStudentOtpBinding
import com.doubtnutapp.home.mapper.ActionToScreenMapper
import com.doubtnutapp.login.ui.activity.LoginListener
import com.doubtnutapp.login.ui.activity.StudentLoginActivity
import com.doubtnutapp.login.viewmodel.LoginViewModel
import com.doubtnutapp.screennavigator.Navigator
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.onboarding.SMSBroadcastReceiver
import com.doubtnutapp.utils.DebouncedOnClickListener
import com.doubtnutapp.utils.KeyboardUtils
import com.google.android.gms.auth.api.phone.SmsRetriever
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 2020-06-18.
 */
class StudentOtpFragment : BaseBindingFragment<LoginViewModel, FragmentStudentOtpBinding>() {

    companion object {
        const val TAG = "StudentOtpFragment"
        const val REQUEST_CODE_WHATSAPP_LOGIN = 199
        fun newInstance() = StudentOtpFragment()
    }

    private var loginListener: LoginListener? = null

    private var timer: CountDownTimer? = null
    private var bufferTimer: CountDownTimer? = null

    private var counter: Int = 0
    private var otpScreenTime = 60000L

    private var trueCallerLoginType: String? = null
    private var isWhatsappInstalled: Boolean = false

    private var screenWidth: Int? = null

    private var enableMissedCallVerification: Boolean = true

    @Inject
    lateinit var screenNavigator: Navigator

    @Inject
    lateinit var actionToScreenMapper: ActionToScreenMapper

    @Inject
    lateinit var lottieAnimDataStore: LottieAnimDataStore

    private val smsBroadcastReceiver by lazy { SMSBroadcastReceiver() }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentStudentOtpBinding =
        FragmentStudentOtpBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): LoginViewModel = activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpView()
        setUpClickListener()
        addOtpListener()
        viewModel.checkForWhatsApp()
    }

    override fun onResume() {
        super.onResume()
        startCounter()
    }

    private fun setUpView() {
        screenWidth = requireActivity().getScreenWidth()
        enableMissedCallVerification =
            defaultPrefs().getBoolean(Constants.ENABLE_MISSED_CALL_VERIFICATION, true)

        mBinding?.apply {
            lifecycleScope.launchWhenResumed {
                val otpScreenAnimationUrl = lottieAnimDataStore.otpScreenAnimationUrl.firstOrNull()
                otpAnimation.applyAnimationFromUrl(otpScreenAnimationUrl)
            }

            textViewResendCodeTimer.text = String.format(
                getString(R.string.resend_code_in_sec, 15 - counter)
            )
            if (viewModel.countryCode != Constants.COUNTRY_CODE_INDIA) {
                otpView.itemWidth = screenWidth?.let { it / 9 } ?: 48.dpToPx()
                otpView.itemCount = 6
                textViewDescription.setText(R.string.enter_the_6_digit_code_sent_to_you_at)
            } else {
                otpView.itemWidth = screenWidth?.let { width -> width / 5 } ?: 68.dpToPx()
                otpView.itemCount = 4
            }
        }
    }

    fun setUpLoginListener(loginListener: LoginListener) {
        this.loginListener = loginListener
    }

    private fun addOtpListener() {

        if (activity == null) return

        val client = SmsRetriever.getClient(requireActivity())
        val retriever = client.startSmsRetriever()

        retriever.addOnSuccessListener {
            val otpListener = object : SMSBroadcastReceiver.OTPListener {

                override fun onOTPReceived(otp: String) {
                    viewModel.verifyOtp(otp)
                }

                override fun onOTPTimeOut() {}
            }
            smsBroadcastReceiver.injectOTPListener(otpListener)
        }
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        context.registerReceiver(
            smsBroadcastReceiver,
            IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.isFirebaseOtpEnable.observe(viewLifecycleOwner) {
            // Change number of OTP digits and message based on OTP service (firebase - 6 digits and other - 4 digits)
            if (it) {
                mBinding?.otpView?.itemWidth =
                    screenWidth?.let { width -> width / 9 } ?: 48.dpToPx()
                mBinding?.otpView?.itemCount = 6
                mBinding?.otpView?.setText("")
                mBinding?.textViewDescription?.setText(R.string.enter_the_6_digit_code_sent_to_you_at)
            } else {
                mBinding?.otpView?.itemWidth =
                    screenWidth?.let { width -> width / 5 } ?: 68.dpToPx()
                mBinding?.otpView?.itemCount = 4
                mBinding?.otpView?.setText("")
                mBinding?.textViewDescription?.setText(R.string.enter_the_4_digit_code_sent_to_you_at)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            mBinding?.progressBar?.setVisibleState(it)
        }

        viewModel.completeNumberField.observe(viewLifecycleOwner) {
            mBinding?.textViewNumber?.text = it
        }

        viewModel.otpField.observe(viewLifecycleOwner) {
            mBinding?.otpView?.setText(it)
        }

        viewModel.checkForLoginTypeLiveData.observe(viewLifecycleOwner) { loginViewType ->
            trueCallerLoginType = loginViewType
        }

        viewModel.showOtherLoginOption.observe(viewLifecycleOwner) { toShow ->
            mBinding?.textViewResendCode?.isEnabled = !toShow
            setVisibilityOfOtherLoginOptions(toShow)
            setVisibilityOfLoginWithOtpOptions(!toShow)
            setVisibilityOfLoginWithPinOptions(toShow)
        }

        viewModel.languageChangedLiveData.observe(viewLifecycleOwner) {
            mBinding?.apply {
                textViewDescription.setText(R.string.enter_the_4_digit_code_sent_to_you_at)
                textViewResendCode.setText(R.string.resend_code)
                buttonVerifyOtp.setText(R.string.verify_otp)
                tvTryWith.setText(R.string.not_able_to_login_try_with)
                btMissCallVerification.setText(R.string.missed_call_verification)
                tvOr.setText(R.string.fragment_otp_or_text)
                tryWithOtherNumber.setText(R.string.try_with_different_number)
                tvPinOrText.setText(R.string.fragment_otp_or_text)
                tvLoginWithPin.setText(R.string.login_with_your_login_pin)
                timer?.cancel()
                timer?.start()
            }
        }

        viewModel.checkForWhatsapp.observe(viewLifecycleOwner) {
            isWhatsappInstalled = it
        }

        viewModel.isPinExist.observe(viewLifecycleOwner) {
            setVisibilityOfLoginWithPinOptions(it)
        }

        viewModel.startTimerWithBuffer.observe(viewLifecycleOwner, SingleEventObserver {
            if (it.first) {
                it.second?.let { bufferTime ->
                    otpScreenTime -= (counter - 1) * 1000
                    startBufferTimer(bufferTime * 1000L)
                } ?: startCounter()
            }
        })
    }

    private fun setVisibilityOfLoginWithPinOptions(toShow: Boolean) {
        mBinding?.pinLayout?.setVisibleState(toShow && viewModel.countryCode == Constants.COUNTRY_CODE_INDIA)
    }

    private fun setUpClickListener() {

        mBinding?.buttonVerifyOtp?.setOnClickListener(object : DebouncedOnClickListener(300) {
            override fun onDebouncedClick(v: View?) {
                viewModel.otpField.value = mBinding?.otpView?.text.toString()
                viewModel.onVerifyOtpClick()
            }
        })

        mBinding?.tryWithOtherNumber?.setOnClickListener {

            if (activity == null) return@setOnClickListener

            requireActivity().onBackPressed()
            viewModel.resetFields.postValue(true)
            viewModel.publishEventWith(EventConstants.EVENT_NAME_CHANGE_NUMBER_CLICKED)
        }

        mBinding?.tryWithWhatsapp?.setOnClickListener {

            if (activity == null) return@setOnClickListener

            openWhatsappForLogin()
            viewModel.resetFields.postValue(true)
            viewModel.publishEventWith(EventConstants.EVENT_NAME_CHANGE_NUMBER_CLICKED)
        }

        mBinding?.otpView?.doAfterTextChanged {

            if (context == null || it == null) return@doAfterTextChanged
            if (it.toString().length > 1) {
                mBinding?.otpView?.setLineColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.tomato
                    )
                )
                mBinding?.buttonVerifyOtp?.isEnabled = true
            } else {
                mBinding?.otpView?.setLineColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.disable_otp_view_cursor
                    )
                )
                mBinding?.buttonVerifyOtp?.isEnabled = false
            }
        }

        mBinding?.btMissCallVerification?.setOnClickListener {
            mBinding?.otpView?.let {
                KeyboardUtils.hideKeyboard(it)
            }
            checkPermissions()
        }

        mBinding?.textViewResendCode?.setOnClickListener {
            val isOtpOverCall = viewModel.isOtpOverCallEnabled.value ?: false
            if (isOtpOverCall) {
                timer?.cancel()
                OtpOverCallDialogFragment.newInstance()
                    .show(childFragmentManager, OtpOverCallDialogFragment.TAG)
            } else {
                viewModel.resendOtp()
            }
        }

        mBinding?.tvLoginWithPin?.setOnClickListener {
            timer?.cancel()
            LoginPinDialogFragment.newInstance()
                .show(childFragmentManager, LoginPinDialogFragment.TAG)
        }
    }

    private fun startCounter() {

        timer = object : CountDownTimer(otpScreenTime, 1000) {

            override fun onTick(millisUntilFinished: Long) {

                if (context == null) return

                if (viewModel.countryCode == Constants.COUNTRY_CODE_INDIA) {
                    when (counter) {
                        0 -> {
                            mBinding?.textViewResendCodeTimer?.text =
                                getString(R.string.resend_code_in_sec, 15 - counter)
                            mBinding?.textViewResendCode?.isEnabled = false
                            mBinding?.textViewResendCode?.isClickable = false
                        }
                        in 1..15 -> { // till 10th seconds, show timer and disable resend OTP button
                            mBinding?.textViewResendCodeTimer?.text =
                                getString(R.string.resend_code_in_sec, 15 - counter)
                        }
                        16 -> { // At 16th second  hide Resend Code Timer
                            mBinding?.textViewResendCodeTimer?.invisible()
                            mBinding?.textViewResendCode?.isEnabled = true
                            mBinding?.textViewResendCode?.isClickable = true
                        }
                        30 -> { // At 31th second  show Resend Code Timer and send Firebase OTP
                            mBinding?.textViewResendCodeTimer?.show()
                            mBinding?.textViewResendCodeTimer?.text =
                                getString(R.string.resend_code_in_sec, 60 - counter)
                            mBinding?.textViewResendCode?.isEnabled = false
                            mBinding?.textViewResendCode?.isClickable = false

                            viewModel.enableFirebaseOtp(true)
                            toast(R.string.firebase_otp_login_message)
                            viewModel.sendOtp()
                        }
                        in 31..60 -> {
                            mBinding?.textViewResendCodeTimer?.text =
                                getString(R.string.resend_code_in_sec, 60 - counter)

                        }
                    }
                } else {
                    when (counter) {
                        0 -> {
                            mBinding?.textViewResendCodeTimer.let { textView ->
                                textView?.text =
                                    getString(R.string.resend_code_in_sec, 30 - counter)
                            }
                            mBinding?.textViewResendCode?.isEnabled = false
                            mBinding?.textViewResendCode?.isClickable = false
                        }
                        in 1..30 -> { // till 10th seconds, show timer and disable resend OTP button
                            mBinding?.textViewResendCodeTimer?.text =
                                getString(R.string.resend_code_in_sec, 30 - counter)

                        }
                        31 -> { // At 31th second  show Resend Code
                            mBinding?.textViewResendCodeTimer?.hide()
                            mBinding?.textViewResendCode?.isEnabled = true
                            mBinding?.textViewResendCode?.isClickable = true
                        }
                    }
                }
                counter++
            }

            override fun onFinish() {

                if (context == null) return

                // At 60th second Enable Resend OTP button and hide Resend Code Timer
                mBinding?.textViewResendCodeTimer?.invisible()
                mBinding?.textViewResendCode?.isEnabled = true
                mBinding?.textViewResendCode?.isClickable = true

                setVisibilityOfLoginWithOtpOptions(toShow = false)
                setVisibilityOfOtherLoginOptions(toShow = true)
            }
        }
        timer?.start()
    }

    private fun startBufferTimer(bufferTime: Long) {

        bufferTimer?.cancel()

        bufferTimer = object : CountDownTimer(bufferTime, bufferTime) {

            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {

                if (context == null) return

                startCounter()
            }
        }

        bufferTimer?.start()
    }

    private fun setVisibilityOfOtherLoginOptions(toShow: Boolean) {
        mBinding?.otpView?.let { KeyboardUtils.hideKeyboard(it) }
        mBinding?.tvTryWith?.setVisibleState(toShow)
        mBinding?.tryWithOtherNumber?.setVisibleState(toShow)
        mBinding?.tryWithWhatsapp?.setVisibleState(toShow && isWhatsappInstalled)
        val visibleState =
            viewModel.checkForLoginTypeLiveData.value != Constants.SHOW_TRUE_CALLER_LOGIN && enableMissedCallVerification && toShow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mBinding?.btMissCallVerification?.setVisibleState(visibleState)
            mBinding?.tvOr?.setVisibleState(visibleState)
        } else {
            mBinding?.btMissCallVerification?.setVisibleState(false)
            mBinding?.tvOr?.setVisibleState(false)
        }
    }

    private fun setVisibilityOfLoginWithOtpOptions(toShow: Boolean) {
        mBinding?.otpView?.let {
            KeyboardUtils.hideKeyboard(it)
        }
        mBinding?.resendOtpLayout?.setVisibleState(toShow)
        mBinding?.otpAnimation?.setVisibleState(toShow)
        mBinding?.otpView?.setVisibleState(toShow)
        mBinding?.textViewDescription?.setVisibleState(toShow)
        mBinding?.textViewNumber?.setVisibleState(toShow)
        mBinding?.buttonVerifyOtp?.setVisibleState(toShow)
    }

    override fun onStop() {
        timer?.cancel()
        super.onStop()
    }

    override fun onDetach() {
        try {
            context?.unregisterReceiver(smsBroadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDetach()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) requestPermission()
        else loginListener?.startTruecallerVerification()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            arrayOf(
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.ANSWER_PHONE_CALLS,
                android.Manifest.permission.READ_CALL_LOG
            )
        } else {
            arrayOf(
                android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.CALL_PHONE,
                android.Manifest.permission.READ_CALL_LOG
            )
        }

        val remainingPermissions =
            PermissionUtils.getNotGrantedPermissions(requireContext(), permission)
        if (remainingPermissions.isNotEmpty()) {
            requestPermissions(
                remainingPermissions,
                StudentLoginActivity.MY_PERMISSIONS_REQUEST_ALL
            )
        } else {
            loginListener?.startTruecallerVerification()
        }
    }


    private fun openWhatsappForLogin() {
        val externalUrl = "http://bit.ly/2PoQrOc"
        val screen = actionToScreenMapper.map(OpenWhatsapp(externalUrl))
        val args = hashMapOf(Constants.EXTERNAL_URL to externalUrl).toBundle()
        screenNavigator.startActivityForResultFromActivity(
            requireActivity(),
            screen,
            args,
            REQUEST_CODE_WHATSAPP_LOGIN
        )
    }

    /**
     * Take required permission for truecaller,
     * if deny - request otp flow
     * else continue with truecaller flow
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {

        when (requestCode) {
            StudentLoginActivity.MY_PERMISSIONS_REQUEST_ALL -> {
                var allGranted = true
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        allGranted = false
                        break
                    }
                }
                if (allGranted) {
                    loginListener?.startTruecallerVerification()
                    viewModel.publishEventWith(
                        EventConstants.EVENT_NAME_TRUE_CALLER_PERMISSION_ALLOWED,
                        ignoreSnowplow = true
                    )
                } else {
                    viewModel.publishEventWith(
                        EventConstants.EVENT_NAME_TRUE_CALLER_PERMISSION_DENIED,
                        ignoreSnowplow = true
                    )
                    viewModel.resetFields.postValue(true)
                    activity?.onBackPressed()
                    val errorText = context?.getString(R.string.err_truecaller_missed_call)
                    if (context != null && errorText != null) {
                        toast(errorText)
                    }
                }
            }
        }
    }
}