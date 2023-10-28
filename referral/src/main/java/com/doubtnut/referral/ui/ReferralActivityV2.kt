package com.doubtnut.referral.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.doubtnut.core.analytics.IAnalyticsPublisher
import com.doubtnut.core.constant.CoreEventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.ui.base.CoreBindingActivity
import com.doubtnut.core.utils.CoreUserUtils
import com.doubtnut.core.utils.isNotNullAndNotEmpty2
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.referral.R
import com.doubtnut.referral.databinding.ActivityReferralV2Binding
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject

class ReferralActivityV2 : CoreBindingActivity<ReferralActivityVM, ActivityReferralV2Binding>() {

    private lateinit var navController: NavController

    @Inject
    lateinit var analyticsPublisher: IAnalyticsPublisher

    override fun provideViewBinding(): ActivityReferralV2Binding {
        return ActivityReferralV2Binding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return ""
    }

    override fun provideViewModel(): ReferralActivityVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.setGraph(R.navigation.nav_graph_referral, intent.extras)
        handleIntent(intent)

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        binding.viewActionClickHandler.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    "${EVENT_TAG}_${CoreEventConstants.CTA_CLICKED}",
                    hashMapOf(
                        CoreEventConstants.STUDENT_ID to CoreUserUtils.getStudentId(),
                        CoreEventConstants.CTA_TEXT to binding.tvAction.text
                    ), ignoreMoengage = false
                )
            )

            if (viewModel.mobile.value.isNotNullAndNotEmpty2()) {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + viewModel.mobile.value))
                startActivity(intent)
            }
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        lifecycleScope.launchWhenResumed {
            viewModel.mobile
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .collectLatest {
                    binding.ivAction.isVisible = it.isNotNullAndNotEmpty2()
                    binding.tvAction.isVisible = it.isNotNullAndNotEmpty2()
                    binding.viewActionClickHandler.isVisible = it.isNotNullAndNotEmpty2()
                }
        }

        lifecycleScope.launchWhenResumed {
            viewModel.title
                .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
                .collectLatest {
                    binding.tvTitle.text = it
                }
        }
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.getParcelableExtra<Uri>(INTENT_EXTRA_URI)
        if (uri != null) {
            val deeplinkUri = Uri.parse(uri.toString())
            navController.navigate(
                deeplinkUri,
                navOptions {
                    popUpTo(R.id.referralHomeFragment) {
                        inclusive = true
                    }
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent ?: return
        handleIntent(intent)
    }

    companion object {
        const val TAG = "ReferralActivityV2"
        const val EVENT_TAG = "referral_activity_v2"

        private const val INTENT_EXTRA_URI = "uri"

        fun getStartIntent(
            context: Context
        ): Intent =
            Intent(context, ReferralActivityV2::class.java)

        fun getDeeplinkStartIntent(context: Context, uri: Uri): Intent =
            Intent(context, ReferralActivityV2::class.java).apply {
                putExtra(INTENT_EXTRA_URI, uri)
            }
    }
}