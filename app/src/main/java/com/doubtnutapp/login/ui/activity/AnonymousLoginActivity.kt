package com.doubtnutapp.login.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.observer.SingleEventObserver
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.LocaleManager
import com.doubtnutapp.MainActivity
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ActivityAnonymousLoginBinding
import com.doubtnutapp.login.LoginNavigation
import com.doubtnutapp.login.viewmodel.LoginViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.KeyboardUtils
import com.doubtnutapp.utils.UserUtil
import javax.inject.Inject

class AnonymousLoginActivity :
    BaseBindingActivity<LoginViewModel, ActivityAnonymousLoginBinding>() {

    companion object {
        private const val TAG = "AnonymousLoginActivity"
        private const val LOCALE = "locale"
        fun getStartIntent(context: Context, locale: String) =
            Intent(context, AnonymousLoginActivity::class.java).apply {
                putExtra(LOCALE, locale)
            }
    }

    override fun provideViewBinding(): ActivityAnonymousLoginBinding =
        ActivityAnonymousLoginBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): LoginViewModel = viewModelProvider(viewModelFactory)

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun setupView(savedInstanceState: Bundle?) {
        viewModel.addAnonymousUser(intent.getStringExtra(LOCALE) ?: "en")
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ANONYMOUS_LOGIN_PAGE_OPEN))
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.navigationLiveData.observe(this) {
            it?.getContentIfNotHandled()?.let { navigation ->
                when (navigation) {
                    is LoginNavigation.MainScreen -> showMainScreen()
                    is LoginNavigation.OnBoardingScreen -> showMainScreen()
                    else -> {

                    }
                }
            }
        }

        viewModel.errorAnonymousLoginLiveData.observe(this, SingleEventObserver {
            if (it) {
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ANONYMOUS_LOGIN_PAGE_FAILURE))
                startWalkThroughActivity()
                finish()
            }
        })
    }

    private fun showMainScreen() {
        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.ANONYMOUS_LOGIN_PAGE_SUCCESS))
        LocaleManager.setLocale(this)
        KeyboardUtils.hideKeyboard(currentFocus ?: View(this))
        startActivity(Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            .apply {
                putExtra("hasToShowCamera", true)
            })
        UserUtil.putAnonymousLogin()
        finish()
    }

    private fun startWalkThroughActivity() {
        StudentLoginActivity.getStartIntent(this, LanguageActivity.TAG)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            .apply {
                startActivity(this)
            }
    }

}