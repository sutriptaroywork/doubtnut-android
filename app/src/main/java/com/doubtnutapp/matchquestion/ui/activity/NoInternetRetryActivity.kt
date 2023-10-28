package com.doubtnutapp.matchquestion.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.airbnb.lottie.LottieDrawable
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.databinding.ActivityNoInternetRetryBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import javax.inject.Inject

/**
 * Created by devansh on 05/09/2020
 */
class NoInternetRetryActivity :
    BaseBindingActivity<DummyViewModel, ActivityNoInternetRetryBinding>() {

    companion object {
        private const val TAG = "NoInternetRetryActivity"

        const val INTENT_EXTRA_FROM_SCREEN = "from_screen"

        fun getStartIntent(context: Context, fromScreen: String? = null) =
            Intent(context, NoInternetRetryActivity::class.java).apply {
                putExtra(INTENT_EXTRA_FROM_SCREEN, fromScreen)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var fromScreen: String? = null

    override fun provideViewBinding(): ActivityNoInternetRetryBinding {
        return ActivityNoInternetRetryBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.redTomato
    }

    override fun setupView(savedInstanceState: Bundle?) {
        fromScreen = intent.getStringExtra(INTENT_EXTRA_FROM_SCREEN)

        if (fromScreen == Constants.LANGUAGE_SCREEN) {
            binding.toolbarTitle.text = getString(R.string.select_app_language)
        } else if (fromScreen == Constants.DN_SHORTS_SCREEN) {
            binding.toolbarTitle.text = ""
        }

        binding.animation.repeatCount = LottieDrawable.INFINITE
        binding.animation.playAnimation()

        binding.buttonRetry.setOnClickListener {
            sendEvent(EventConstants.EVENT_NO_INTERNET_RETRY, hashMapOf(), ignoreSnowplow = true)
            setResultForCallingActivity(Activity.RESULT_OK)
            finish()
        }

        binding.ivBack.setOnClickListener {
            sendEvent(EventConstants.EVENT_NO_INTERNET_BACK_PRESS, hashMapOf(), ignoreSnowplow = true)
            setResultForCallingActivity(Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onBackPressed() {
        sendEvent(EventConstants.EVENT_NO_INTERNET_BACK_PRESS, hashMapOf(), ignoreSnowplow = true)
        setResultForCallingActivity(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    private fun setResultForCallingActivity(result: Int) {
        binding.animation.clearAnimation()
        setResult(result, Intent())
    }

    private fun sendEvent(eventName: String, params: HashMap<String, Any>, ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}