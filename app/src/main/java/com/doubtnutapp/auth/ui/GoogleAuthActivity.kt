package com.doubtnutapp.auth.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.auth.viewmodel.AuthViewModel
import com.doubtnutapp.databinding.ActivityGoogleAuthBinding
import com.doubtnutapp.gamification.settings.settingdetail.ui.SettingDetailActivity
import com.doubtnutapp.screennavigator.PrivacyPolicyScreen
import com.doubtnutapp.screennavigator.TermsAndConditionsScreen
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgets.PointerTextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider
import dagger.Lazy
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class GoogleAuthActivity :
    BaseBindingActivity<AuthViewModel, ActivityGoogleAuthBinding>() {

    companion object {
        private const val TAG = "GoogleAuthActivity"
        private const val RC_GOOGLE_SIGN_IN = 1211
        fun getStartIntent(context: Context) = Intent(context, GoogleAuthActivity::class.java)
    }

    override fun provideViewBinding(): ActivityGoogleAuthBinding =
        ActivityGoogleAuthBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): AuthViewModel =
        viewModelProvider(viewModelFactory)

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var defaultDataStore: Lazy<DefaultDataStore>

    private lateinit var gso: GoogleSignInOptions
    private lateinit var firebaseAuth: FirebaseAuth

    override fun setupView(savedInstanceState: Bundle?) {
        setUpTncAndPrivacyPolicyText()
        setUpFireBaseAuth()
        initGoogleSignIn()
        UserUtil.putAuthPageOpenCount(UserUtil.getAuthPageOpenCount() + 1)
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.GOOGLE_AUTH_PAGE_OPEN,
                ignoreSnowplow = true
            )
        )

        binding.rootView.setOnClickListener {
            onBackPressed()
        }

        binding.ivClose.setOnClickListener {
            onBackPressed()
        }

        binding.mainLayout.setOnClickListener {}

        lifecycleScope.launchWhenResumed {
            val messageText = defaultDataStore.get().gmailVerificationScreenText.firstOrNull()
            val pointers = mutableListOf<String>()
            if (messageText.isNotNullAndNotEmpty()) {
                pointers.addAll(messageText.orEmpty().split("::$$::"))
            } else {
                pointers.addAll(
                    listOf(
                        "Please provide your email id to continue using social features like friends and study group.",
                        "फ्रेंड्स और स्टडी ग्रुप जैसे फीचर्स उपयोग करते रहने के लिए अपना ईमेल ID वेरीफाई करना जरूरी है, अभी वेरीफाई करें!",
                        "Doubtnut collects email ID to enable quality control. This ensures that only verified and like-minded students benefit from the platform.",
                        "Doubtnut App पर आने वाले सभी बच्चो की सुरक्षा के लिए ईमेल ID लिया जाता है । ईमेल ID के द्वारा Doubtnut सुनिश्चित कर पाता है की App पर आने वाले सभी छात्र सत्यापित है और मंच पे सभी चर्चा निरापद है।"
                    )
                )
            }
            binding.layoutPointers.removeAllViews()
            pointers.forEach {
                val textView = PointerTextView(this@GoogleAuthActivity)
                textView.setViews("•", it)
                binding.layoutPointers.addView(textView)
            }
        }

        binding.buttonGoogleLogin.background = Utils.getShape(
            "#ffffff",
            "#e5e5e5",
            42f,
            1
        )
    }

    private fun setUpFireBaseAuth() {
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.useAppLanguage()
    }

    private fun initGoogleSignIn() {
        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestIdToken(getString(R.string.google_web_client_id))
            .build()

        binding.buttonGoogleLogin.setOnClickListener {
            val signIn = GoogleSignIn.getClient(this, gso).signInIntent
            startActivityForResult(signIn, RC_GOOGLE_SIGN_IN)
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.GOOGLE_AUTH_CLICK,
                    ignoreSnowplow = true
                )
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                signInWithAuthCredential(credential)
            } catch (e: ApiException) {
                toast("Error Occurred")
                Log.e(e, TAG)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.GOOGLE_AUTH_FAILURE,
                        ignoreSnowplow = true
                    )
                )
            }
        }
    }

    private fun signInWithAuthCredential(authCredential: AuthCredential?) {
        if (!NetworkUtils.isConnected(this)) return
        authCredential ?: return
        updateProgress(true)
        firebaseAuth.signInWithCredential(authCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    task.result.user?.getIdToken(true)?.addOnCompleteListener {
//                        viewModel.verifyGoogleAuth(it.result.token ?: "")
                        defaultPrefs().edit {
                            putBoolean(Constants.GMAIL_VERIFIED, true)
                        }
                        updateProgress(false)
                        toast("Updated Successfully")
                        setResult(RESULT_OK)
                        finish()
                    }
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        toast(R.string.somethingWentWrong)
                    }
                    updateProgress(false)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.GOOGLE_AUTH_FAILURE,
                            ignoreSnowplow = true
                        )
                    )
                }
            }
            .addOnFailureListener(this) {
                toast("Error Occurred")
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.GOOGLE_AUTH_FAILURE,
                        ignoreSnowplow = true
                    )
                )
            }
    }

    /*override fun setupObservers() {
        super.setupObservers()
        viewModel.googleAuthLiveData.observeK(
            this,
            this::onGoogleAuthResponse,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onGoogleAuthResponse(data: Any) {
        defaultPrefs().edit {
            putBoolean(Constants.GMAIL_VERIFIED, true)
        }
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.GOOGLE_AUTH_SUCCESS,
                ignoreSnowplow = true
            )
        )
        toast("Updated Successfully")
        setResult(RESULT_OK)
        finish()
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.GOOGLE_AUTH_FAILURE,
                ignoreSnowplow = true
            )
        )
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.GOOGLE_AUTH_FAILURE,
                ignoreSnowplow = true
            )
        )
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.GOOGLE_AUTH_FAILURE,
                ignoreSnowplow = true
            )
        )
    }*/

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
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
                val intent =
                    Intent(this@GoogleAuthActivity, SettingDetailActivity::class.java).also {
                        it.putExtra(Constants.PAGE_NAME, TermsAndConditionsScreen.toString())
                    }
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(this@GoogleAuthActivity, R.color.tomato)
            }

        }

        val span2 = object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent =
                    Intent(this@GoogleAuthActivity, SettingDetailActivity::class.java).also {
                        it.putExtra(Constants.PAGE_NAME, PrivacyPolicyScreen.toString())
                    }
                startActivity(intent)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ContextCompat.getColor(this@GoogleAuthActivity, R.color.tomato)
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

        binding.textViewTncPrivacyPolicy.text = builder
        binding.textViewTncPrivacyPolicy.movementMethod = LinkMovementMethod.getInstance()
    }

}
