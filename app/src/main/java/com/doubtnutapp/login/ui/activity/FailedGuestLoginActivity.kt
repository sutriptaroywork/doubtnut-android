package com.doubtnutapp.login.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.data.remote.PopupDetails
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.camera.ui.CameraActivity
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.databinding.ActivityFailedGuestLoginBinding
import com.doubtnutapp.isNotNullAndNotEmpty
import com.doubtnutapp.loadImage
import com.doubtnutapp.login.viewmodel.LoginViewModel
import com.doubtnutapp.orDefaultValue
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.splash.SplashActivity
import javax.inject.Inject


class FailedGuestLoginActivity :
    BaseBindingActivity<LoginViewModel, ActivityFailedGuestLoginBinding>() {

    companion object {
        private const val TAG = "FailedGuestLoginActivity"
        private const val EVENT_TAG = "guest_login"
        private const val POPUP_DETAILS = "popup_details"
        private const val SOURCE = "source"
        fun getStartIntent(context: Context, popupDetails: PopupDetails?, source: String?) =
            Intent(context, FailedGuestLoginActivity::class.java).apply {
                putExtra(POPUP_DETAILS, popupDetails)
                putExtra(SOURCE, source)
            }
    }

    private val popupDetails: PopupDetails? by lazy {
        intent.getParcelableExtra(
            POPUP_DETAILS
        )
    }

    private val source: String? by lazy {
        intent.getStringExtra(SOURCE)
    }

    @Inject
    lateinit var userPreference: UserPreference

    override fun providePageName(): String = TAG

    override fun provideViewBinding(): ActivityFailedGuestLoginBinding =
        ActivityFailedGuestLoginBinding.inflate(layoutInflater)

    override fun provideViewModel(): LoginViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        binding.apply {
            viewModel.publishEventWith(
                "${EVENT_TAG}_${EventConstants.VIEWED}",
                hashMapOf<String, Any>().apply {
                    put(EventConstants.SOURCE, source.orDefaultValue())
                }
            )

            val backgroundColor =
                ContextCompat.getColor(this@FailedGuestLoginActivity, R.color.colorTransparent)
            rootView.setBackgroundColor(backgroundColor)

            ivIcon.apply {
                popupDetails?.imageUrl?.let {
                    loadImage(it)
                }
            }

            tvTitle.apply {
                isVisible = popupDetails?.title.isNotNullAndNotEmpty()
                text = popupDetails?.title
            }

            tvSubtitle.apply {
                isVisible = popupDetails?.subtitle.isNotNullAndNotEmpty()
                text = popupDetails?.subtitle
            }

            ivClose.setOnClickListener {
                viewModel.publishEventWith(
                    "${EVENT_TAG}_${EventConstants.CROSS_CLICKED}",
                    hashMapOf<String, Any>().apply {
                        put(EventConstants.SOURCE, source.orDefaultValue())
                    }
                )
                moveToCameraScreen()
            }

            btLogin.apply {
                isVisible = popupDetails?.ctaText.isNotNullAndNotEmpty()
                text = popupDetails?.ctaText
                setOnClickListener {
                    viewModel.publishEventWith(
                        "${EVENT_TAG}_${EventConstants.CTA_CLICKED}",
                        hashMapOf<String, Any>().apply {
                            put(EventConstants.CTA_TEXT, popupDetails?.ctaText.orEmpty())
                            put(EventConstants.SOURCE, source.orDefaultValue())
                        }
                    )
                    moveToLoginScreen()
                }
            }
        }
    }

    private fun moveToLoginScreen() {
        finish()
        userPreference.logOutUser()
        val intent =
            Intent(this@FailedGuestLoginActivity, SplashActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        startActivity(intent)
    }

    override fun onBackPressed() {
        viewModel.publishEventWith(
            "${EVENT_TAG}_${EventConstants.BACK_CLICKED}",
            hashMapOf<String, Any>().apply {
                put(EventConstants.SOURCE, source.orDefaultValue())
            }
        )
    }

    private fun moveToCameraScreen() {
        finish()
        CameraActivity.getStartIntent(
            context = this@FailedGuestLoginActivity,
            source = TAG
        ).also { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }
}
