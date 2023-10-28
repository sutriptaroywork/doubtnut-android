package com.doubtnutapp.login.ui.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.DefaultDataStore
import com.doubtnut.core.data.LottieAnimDataStore
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.LottieAnimationViewUtils.applyAnimationFromUrl
import com.doubtnut.core.utils.gone
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.core.utils.visible
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.FragmentLoginBackPressBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.hide
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.uxcam.UXCam
import dagger.Lazy
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Created by Sachin Saxena on 2020-09-29.
 */

class LoginBackPressDialogFragment :
    BaseBindingDialogFragment<DummyViewModel, FragmentLoginBackPressBinding>() {

    private val animationDatastoreKey: String? by lazy {
        arguments?.getString(ANIMATION_DATASTORE_KEY)
    }

    @get:StringRes
    private val title: Int? by lazy { arguments?.getInt(TITLE) }

    @get:StringRes
    private val subtitle: Int? by lazy { arguments?.getInt(SUB_TITLE) }

    @get:StringRes
    private val cta1Text: Int? by lazy { arguments?.getInt(CTA1_TEXT) }

    @get:StringRes
    private val cta2Text: Int? by lazy { arguments?.getInt(CTA2_TEXT) }

    private val fromScreen: String? = arguments?.getString(FROM_SCREEN)

    private var callBackFromDialog: CallBackFromDialog? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var defaultDataSource: Lazy<DefaultDataStore>

    @Inject
    lateinit var lottieAnimDataStore: Lazy<LottieAnimDataStore>

    private var enableGuestLogin: Boolean = true

    companion object {
        const val TAG = "LoginBackPressDialogFragment"
        private const val ANIMATION_DATASTORE_KEY = "animation_datastore_key"
        private const val TITLE = "title"
        private const val SUB_TITLE = "sub_title"
        private const val CTA1_TEXT = "cta1_text"
        private const val CTA2_TEXT = "cta2_text"
        private const val FROM_SCREEN = "from_screen"

        private const val UXCAM_TAG = "login"
        const val UXCAM_EVENT_GUEST_LOGIN = "guest_login_cta"

        fun newInstance(
            animationDatastoreKey: String,
            @StringRes title: Int,
            @StringRes subTitle: Int?,
            @StringRes cta1Text: Int,
            @StringRes cta2Text: Int?,
            fromScreen: String
        ) =
            LoginBackPressDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ANIMATION_DATASTORE_KEY, animationDatastoreKey)
                    putInt(TITLE, title)
                    subTitle?.let { putInt(SUB_TITLE, it) }
                    putInt(CTA1_TEXT, cta1Text)
                    cta2Text?.let { putInt(CTA2_TEXT, it) }
                    putString(FROM_SCREEN, fromScreen)
                }
            }
    }

    override fun providePageName(): String = TAG

    override fun getTheme(): Int = android.R.style.Theme_Black_NoTitleBar

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentLoginBackPressBinding =
        FragmentLoginBackPressBinding.inflate(layoutInflater)

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        lifecycleScope.launchWhenResumed {
            enableGuestLogin = defaultDataSource.get().enableGuestLogin.firstOrNull() ?: true
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            mBinding?.apply {
                animationDatastoreKey?.let { key ->
                    val animationUrl =
                        lottieAnimDataStore.get().get(stringPreferencesKey(key)).firstOrNull()
                    loginAnimation.applyAnimationFromUrl(animationUrl)
                }

                tvTitle.apply {
                    if (title != null && title != 0) {
                        visible()
                        text = getString(title!!)
                    } else {
                        hide()
                    }
                }

                tvSubTitle.apply {
                    if (subtitle != null && subtitle != 0) {
                        visible()
                        text = getString(subtitle!!)
                    } else {
                        gone()
                    }
                }

                btCta1.apply {
                    if (cta1Text != null && cta1Text != 0) {
                        visible()
                        text = getString(cta1Text!!)
                        setOnClickListener {
                            if (activity == null) return@setOnClickListener
                            loginAnimation.clearAnimation()
                            when (fromScreen) {
                                StudentOtpFragment.TAG -> callBackFromDialog?.resendOtp()
                                else -> dismiss()
                            }
                            dismiss()
                        }
                    } else {
                        hide()
                    }
                }

                btCta2.apply {
                    if (cta2Text != null && cta2Text != 0 && enableGuestLogin) {
                        UXCam.logEvent("${UXCAM_TAG}_$UXCAM_EVENT_GUEST_LOGIN")
                        visible()
                        tvOr.visible()
                        text = getString(cta2Text!!)
                        setOnClickListener {
                            if (activity == null) return@setOnClickListener
                            loginAnimation.clearAnimation()
                            callBackFromDialog?.guestLogin()
                            dismiss()
                        }
                    } else {
                        tvOr.gone()
                        gone()
                    }
                }

                ivClose.setOnClickListener {
                    loginAnimation.clearAnimation()
                    dismiss()
                }

                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.EVENT_LOGIN_BACK_PRESS_DIALOG_OPEN,
                        EventConstants.SOURCE, fromScreen.orEmpty(), ignoreSnowplow = true
                    )
                )
            }
        }
    }

    fun setCallBackListener(listener: CallBackFromDialog) {
        callBackFromDialog = listener
    }

    interface CallBackFromDialog {
        fun resendOtp() {}
        fun guestLogin() {}
    }
}