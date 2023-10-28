package com.doubtnutapp.dnr.ui.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.databinding.ActivityDnrBinding
import com.doubtnutapp.dnr.viewmodel.DnrActivityViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import io.branch.referral.Defines

class DnrActivity :
    BaseBindingActivity<DnrActivityViewModel, ActivityDnrBinding>() {

    companion object {
        private const val TAG = "DnrActivity"
        private const val INTENT_EXTRA_URI = "uri"
        private const val HOME_SCREEN_DEEPLINK_PREFIX = "doubtnutapp://dnr/home"

        fun getStartIntent(
            context: Context
        ): Intent =
            Intent(context, DnrActivity::class.java)

        fun getDeeplinkStartIntent(context: Context, uri: Uri): Intent =
            Intent(context, DnrActivity::class.java).apply {
                putExtra(INTENT_EXTRA_URI, uri)
            }
    }

    private val navController by lazy { findNavController(R.id.nav_host_fragment_dnr) }

    override fun provideViewBinding(): ActivityDnrBinding =
        ActivityDnrBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DnrActivityViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        window.statusBarColor = ContextCompat.getColor(this, R.color.tomato)
        navController.setGraph(R.navigation.nav_graph_dnr, intent.extras)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            val uri = it.getParcelableExtra<Uri>(INTENT_EXTRA_URI)
            if (uri != null) {
                val deeplinkUri = Uri.parse(uri.toString())
                if (navController.graph.hasDeepLink(deeplinkUri)) {
                    navController.navigate(deeplinkUri)
                }
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.getParcelableExtra<Uri>(INTENT_EXTRA_URI)
        if (uri != null) {
            val deeplinkUri = Uri.parse(uri.toString())
            if (navController.graph.hasDeepLink(deeplinkUri)) {
                if (deeplinkUri.toString().startsWith(HOME_SCREEN_DEEPLINK_PREFIX)) {
                    navController.navigate(
                        deeplinkUri,
                        navOptions {
                            popUpTo(R.id.dnrHomeFragment) {
                                inclusive = true
                            }
                        }
                    )
                } else {
                    navController.navigate(
                        deeplinkUri,
                        navOptions {
                            popUpTo(R.id.dnrHomeFragment) {
                                inclusive = false
                            }
                        }
                    )
                }
            }
        }
    }

    override fun startActivity(intent: Intent?) {
        val isBranchLink = intent?.data?.host.equals(Constants.BRANCH_HOST)
        if (isBranchLink) {
            intent?.putExtra(Defines.IntentKeys.ForceNewBranchSession.key, true)
        }
        super.startActivity(intent)
    }
}
