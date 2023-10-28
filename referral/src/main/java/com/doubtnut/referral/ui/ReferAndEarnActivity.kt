package com.doubtnut.referral.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.navigation.IDeeplinkAction
import com.doubtnut.core.ui.base.CoreBindingActivity
import com.doubtnut.core.utils.*
import com.doubtnut.core.widgets.IWidgetLayoutAdapter
import com.doubtnut.referral.databinding.ActivityReferAndEarnBinding
import java.net.URLEncoder
import javax.inject.Inject
import android.content.Intent
import android.graphics.Color

import android.view.WindowManager
import androidx.navigation.fragment.NavHostFragment
import com.doubtnut.referral.R

class ReferAndEarnActivity :
    CoreBindingActivity<ReferAndEarnViewModel, ActivityReferAndEarnBinding>() {

    @Inject
    lateinit var deeplinkAction: IDeeplinkAction

    companion object {
        const val TAG = "ReferAndEarnActivity"
        const val ACTION = "action"
        const val NAVIGATE_HOME = "navigate_home"
        const val NAVIGATE_FAQ = "navigate_faq"

        fun getStartIntent(context: Context, action: String): Intent {
            return Intent(context, ReferAndEarnActivity::class.java).apply {
                putExtra(ACTION, action)
            }
        }
    }

    val action: String? by lazy {
        intent.getStringExtra(ACTION).orEmpty()
    }

    override fun provideViewBinding(): ActivityReferAndEarnBinding {
        return ActivityReferAndEarnBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ReferAndEarnViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setStatusBarColor()
        setUpNavigationComponent()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setUpNavigationComponent()
    }

    private fun setUpNavigationComponent() {
        val fragment =
            supportFragmentManager.findFragmentById(R.id.navHostFragmentContainer) as NavHostFragment
        val navController = fragment.navController
        navController.setGraph(R.navigation.nav_graph_refer_and_earn_landing_page)
        when (action) {
            NAVIGATE_FAQ -> {
                navController.navigate(R.id.referAndEarnFAQFragment)
            }
        }
    }

    private fun setStatusBarColor() {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.parseColor("#007aff")
    }

    private fun launchWhatsappIntent(messageStr: String) {
        val message = URLEncoder.encode(
            messageStr,
            "UTF-8"
        )
        val deeplink =
            "doubtnutapp://whatsapp?external_url=https://api.whatsapp.com/send?text=$message"
        deeplinkAction.performAction(this@ReferAndEarnActivity, deeplink)
    }

}