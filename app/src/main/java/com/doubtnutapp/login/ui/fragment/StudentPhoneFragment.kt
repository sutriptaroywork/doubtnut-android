package com.doubtnutapp.login.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.constant.ErrorConstants
import com.doubtnut.core.observer.SingleEventObserver
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentPhoneBinding
import com.doubtnutapp.gamification.settings.settingdetail.ui.SettingDetailActivity
import com.doubtnutapp.login.ui.activity.LoginListener
import com.doubtnutapp.login.viewmodel.LoginViewModel
import com.doubtnutapp.screennavigator.PrivacyPolicyScreen
import com.doubtnutapp.screennavigator.TermsAndConditionsScreen
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.DebouncedOnClickListener
import com.doubtnutapp.utils.KeyboardUtils
import com.doubtnutapp.widgets.countrycodepicker.CountryCodePickerDialogFragment
import com.doubtnutapp.widgets.countrycodepicker.model.CountryCode
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsOptions
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.truecaller.android.sdk.*

/**
 * Created by Sachin Saxena on 2020-06-17.
 */
class StudentPhoneFragment : BaseBindingFragment<LoginViewModel, FragmentPhoneBinding>(),
    ITrueCallback,
    CountryCodePickerDialogFragment.OnCountryCodePickedListener {

    companion object {
        const val TAG = "StudentPhoneFragment"

        const val PHONE_SELECTOR_REQUEST = 1001
        const val TRUECALLER_REQUEST = 100

        fun newInstance() = StudentPhoneFragment()
    }

    private var loginListener: LoginListener? = null

    private var enableTrueCaller: Boolean = true

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentPhoneBinding =
        FragmentPhoneBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): LoginViewModel = activityViewModelProvider(viewModelFactory)

    @SuppressLint("SetTextI18n")
    override fun setupView(view: View, savedInstanceState: Bundle?) {
        enableTrueCaller = defaultPrefs().getBoolean(Constants.ENABLE_TRUECALLER_VERIFICATION, true)
        if (enableTrueCaller && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            initTrueCallerSdk()
        }
        setUpTncAndPrivacyPolicyText()
        setUpClickListeners()
        if (viewModel.countryCode != Constants.COUNTRY_CODE_INDIA) {
            mBinding?.countryCodePicker?.text = "+${viewModel.countryCode}"
        }

        val studentImages = defaultPrefs().getString(Constants.LOGIN_STUDENT_IMAGES, "")
        if (!studentImages.isNullOrEmpty()) {
            val listOfImages = studentImages.split(",")
            try {
                mBinding?.ivStudent1?.loadImage(listOfImages[0])
                mBinding?.ivStudent2?.loadImage(listOfImages[1])
                mBinding?.ivStudent3?.loadImage(listOfImages[2])
            } catch (e: ArrayIndexOutOfBoundsException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        mBinding?.smsUserConsent?.setOnCheckedChangeListener { _, state ->
            viewModel.userConsent = if (state) 1 else 0
            viewModel.isOptin = state
            viewModel.publishEventWith(EventConstants.USER_CONSENT_CLICK,
                hashMapOf<String, Any>().apply {
                    put(EventConstants.STATE, state)
                }, ignoreSnowplow = true)
        }
    }

    fun setUpLoginListener(loginListener: LoginListener) {
        this.loginListener = loginListener
    }

    private fun setUpTncAndPrivacyPolicyText() {
        val completeText = "By continuing you agree to our T&C and Privacy Policy."

        val builder = SpannableStringBuilder(completeText)
        val tncText = "T&C"
        val privacyPolicyText = "Privacy Policy"
        val firstIndexTncText = builder.toString().indexOf(tncText)
        val lastIndexTncText = firstIndexTncText + tncText.length

        val firstIndexPrivacyPolicyText = builder.toString().indexOf(privacyPolicyText)
        val lastIndexPrivacyPolicyText = firstIndexPrivacyPolicyText + privacyPolicyText.length

        val span1 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(context, SettingDetailActivity::class.java).also {
                    it.putExtra(Constants.PAGE_NAME, TermsAndConditionsScreen.toString())
                }
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(context!!, R.color.tomato)
            }

        }

        val span2 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(context, SettingDetailActivity::class.java).also {
                    it.putExtra(Constants.PAGE_NAME, PrivacyPolicyScreen.toString())
                }
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(context!!, R.color.tomato)
            }

        }
        builder.setSpan(
            span1,
            firstIndexTncText,
            lastIndexTncText,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.setSpan(
            span2,
            firstIndexPrivacyPolicyText,
            lastIndexPrivacyPolicyText,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        mBinding?.textViewTncPrivacyPolicy?.text = builder
        mBinding?.textViewTncPrivacyPolicy?.movementMethod = LinkMovementMethod.getInstance()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun initTrueCallerSdk() {
        val trueScope: TruecallerSdkScope = TruecallerSdkScope.Builder(requireContext(), this)
            .consentMode(TruecallerSdkScope.CONSENT_MODE_POPUP)
            .consentTitleOption(TruecallerSdkScope.SDK_CONSENT_TITLE_VERIFY)
            .footerType(TruecallerSdkScope.FOOTER_TYPE_SKIP)
            .sdkOptions(TruecallerSdkScope.SDK_OPTION_WITH_OTP)
            .build()

        TruecallerSDK.init(trueScope)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.isBottomSheetExpanded.observe(viewLifecycleOwner) {
            updateUi(it)
        }

        viewModel.resetFields.observe(viewLifecycleOwner) {
            if (it) {
                mBinding?.etPhone?.setText("")
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) {
            mBinding?.progressBar?.setVisibleState(it)
        }

        // If bottom sheet expanded, and truecaller app is installed, show truecaller pop up
        // Else show keyboard for mobile number input
        viewModel.showTrueCallerPopUp.observe(viewLifecycleOwner, SingleEventObserver {
            try {
                if (it && TruecallerSDK.getInstance().isUsable) {
                    TruecallerSDK.getInstance().getUserProfile(this)
                    viewModel.publishEventWith(
                        EventConstants.EVENT_NAME_TRUECALLER_LOGIN_POPUP,
                        hashMapOf(),
                        ignoreSnowplow = true
                    )
                } else {
                    handleWithoutTrueCaller()
                }
            } catch (e: Exception) {
                // https://console.firebase.google.com/project/doubtnut-e000a/crashlytics/app/android:com.doubtnutapp/issues/707a72a228b91f18389e1c4f02bd0ecb?time=last-seven-days&sessionEventKey=60C77B2D027A000148F4E334A5470361_1552123622731111748
                handleWithoutTrueCaller()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        })

        viewModel.languageChangedLiveData.observe(viewLifecycleOwner) {
            mBinding?.tvTitle?.setText(R.string.login_with_phone_number)
            mBinding?.smsUserConsent?.setText(R.string.sms_user_consent)
            mBinding?.etPhone?.setHint(R.string.enter_your_phone_number)
            mBinding?.msgToStudent?.setText(R.string._20m_students_are_learning_on_doubtnut)
            mBinding?.tvErrorPhone?.setText(R.string.only_enter_10_digit_mobile_number)
            mBinding?.btNext?.setText(R.string.click_to_continue)
        }
    }

    private fun handleWithoutTrueCaller() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // ToDo - Need to handle for international number
            requestPhoneSelector()
        } else {
            showKeyboard()
        }
    }

    private fun afterTextChange(phoneNumber: String?) {

        if (phoneNumber.isNullOrEmpty()) {
            mBinding?.btNext?.isEnabled = false
            mBinding?.tvErrorPhone?.hide()
        } else {
            when (viewModel.countryCode) {

                Constants.COUNTRY_CODE_INDIA -> {
                    mBinding?.tvErrorPhone?.setVisibleState(phoneNumber.length > 10)
                    mBinding?.btNext?.isEnabled = phoneNumber.length == 10
                    setPhoneNumberColor(phoneNumber.length, 10)
                }

                else -> {
                    mBinding?.tvErrorPhone?.setVisibleState(phoneNumber.isEmpty())
                    mBinding?.btNext?.isEnabled = phoneNumber.isNotEmpty() == true
                    setPhoneNumberColor(1, phoneNumber.length)
                }
            }
        }
    }

    private fun setPhoneNumberColor(currentLength: Int, requireLength: Int) =
        when {
            currentLength > requireLength -> mBinding?.etPhone?.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.red
                )
            )

            else -> mBinding?.etPhone?.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.greyBlack
                )
            )
        }

    private fun setUpClickListeners() {
        mBinding?.countryCodePicker?.setOnClickListener {
            if (viewModel.isBottomSheetExpanded.value == true) {
                CountryCodePickerDialogFragment().apply {
                    setOnCountryCodePickedListener(this@StudentPhoneFragment)
                }.show(childFragmentManager, CountryCodePickerDialogFragment.TAG)
            } else {
                loginListener?.expandBottomSheet(true)
            }
        }

        mBinding?.etPhone?.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                viewModel.phoneNumber = s.toString()
                afterTextChange(s?.toString())
            }

        })

        mBinding?.btNext?.setOnClickListener(object : DebouncedOnClickListener(300) {
            override fun onDebouncedClick(v: View?) {
                mBinding?.etPhone?.let {
                    KeyboardUtils.hideKeyboard(it)
                }
                viewModel.sendOtp()
                viewModel.publishEventWith(
                    EventConstants.EVENT_NAME_LOGIN_INITIATED,
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, EventConstants.MOBILE_NO)
                    },
                    true
                )
            }
        })

        mBinding?.phoneLayout?.setOnClickListener {
            loginListener?.expandBottomSheet(true)
        }

        mBinding?.etPhone?.setOnClickListener {
            loginListener?.expandBottomSheet(true)
        }
    }

    fun updateUi(isExpanded: Boolean) {
        if (isExpanded) {
            mBinding?.etPhone?.enable()
            mBinding?.msgLayout?.invisible()
            mBinding?.btNext?.show()
            mBinding?.countryCodePicker?.isClickable = true
            mBinding?.countryCodePicker?.isEnabled = true
            afterTextChange(mBinding?.etPhone?.text?.toString())
        } else {
            mBinding?.etPhone?.isFocusable = false
            mBinding?.countryCodePicker?.isClickable = false
            mBinding?.countryCodePicker?.isEnabled = false
            mBinding?.msgLayout?.show()
            mBinding?.btNext?.invisible()
            mBinding?.tvErrorPhone?.hide()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PHONE_SELECTOR_REQUEST -> {
                    val credential: Credential? = data.getParcelableExtra(Credential.EXTRA_KEY)
                    val phoneNumber = credential?.id
                    if (!phoneNumber.isNullOrEmpty()) {
                        mBinding?.etPhone?.setText(phoneNumber.takeLast(10))
                        mBinding?.etPhone?.setSelection(mBinding?.etPhone?.text?.length ?: 0)
                    }
                }

                TRUECALLER_REQUEST -> {
                    try {
                        TruecallerSDK.getInstance()
                            .onActivityResultObtained(
                                requireActivity(),
                                TRUECALLER_REQUEST,
                                resultCode,
                                data
                            )
                    } catch (e: Exception) {
                        FirebaseCrashlytics.getInstance()
                            .setCustomKey(ErrorConstants.DN_FATAL, true)
                        FirebaseCrashlytics.getInstance().recordException(e)
                        ToastUtils.makeTextInDev(
                            requireContext(),
                            e.message ?: "SWW with Truecaller",
                            Toast.LENGTH_SHORT
                        )?.show()
                    }
                }
                else -> showKeyboard()
            }
        } else {
            showKeyboard()
        }
    }

    // Construct a request for phone numbers and show the picker
    private fun requestPhoneSelector() {
        val hintRequest = HintRequest.Builder()
            .setPhoneNumberIdentifierSupported(true)
            .build()
        val options = CredentialsOptions.Builder()
            .forceEnableSaveDialog()
            .build()
        val pendingIntent =
            Credentials.getClient(requireContext(), options).getHintPickerIntent(hintRequest)
        try {
            startIntentSenderForResult(
                pendingIntent.intentSender,
                PHONE_SELECTOR_REQUEST,
                null,
                0,
                0,
                0,
                Bundle()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onSuccessProfileShared(p0: TrueProfile) {
        viewModel.verifyTrueCallerLogin(p0.payload, p0.signature, false, null)
    }

    override fun onFailureProfileShared(p0: TrueError) {
        showKeyboard()
        viewModel.publishEventWith(
            EventConstants.EVENT_NAME_TRUE_CALLER_PROFILE_SHARED_ERROR,
            hashMapOf<String, Any>().apply {
                put(EventConstants.ERROR_TYPE, p0.errorType)
            }, ignoreSnowplow = true)
    }

    override fun onVerificationRequired(p0: TrueError?) {
        showKeyboard()
        viewModel.publishEventWith(
            EventConstants.EVENT_NAME_TRUE_CALLER_PROFILE_SHARED_ERROR,
            hashMapOf(),
            ignoreSnowplow = true
        )
    }

    override fun onCountryCodePicked(countryCodeData: CountryCode) {
        mBinding?.countryCodePicker?.text = countryCodeData.plusAppendedPhoneCode

        val phoneCode = countryCodeData.phoneCode
        viewModel.countryCode = phoneCode
        viewModel.enableFirebaseOtp(phoneCode != Constants.COUNTRY_CODE_INDIA)
        viewModel.showOtherLoginOption.postValue(false)
        loginListener?.expandBottomSheet(true)

        afterTextChange(mBinding?.etPhone?.text?.toString())
    }

    private fun showKeyboard() {
        mBinding?.etPhone?.let { KeyboardUtils.showKeyboard(it) }
    }
}