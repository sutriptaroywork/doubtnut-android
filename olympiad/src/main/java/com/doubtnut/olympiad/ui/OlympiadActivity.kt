package com.doubtnut.olympiad.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.doubtnut.core.CoreApplication
import com.doubtnut.core.actions.ActivityAction
import com.doubtnut.core.constant.CoreConstants
import com.doubtnut.core.data.local.defaultPrefs2
import com.doubtnut.core.ui.base.CoreBindingActivity
import com.doubtnut.core.utils.observeNonNull
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnut.olympiad.R
import com.doubtnut.olympiad.databinding.ActivityOlympiadBinding
import com.doubtnut.olympiad.ui.viewmodel.OlympiadActivityVM

class OlympiadActivity : CoreBindingActivity<OlympiadActivityVM, ActivityOlympiadBinding>() {

    private lateinit var navController: NavController

    override fun provideViewBinding(): ActivityOlympiadBinding {
        return ActivityOlympiadBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): OlympiadActivityVM {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.setGraph(R.navigation.nav_graph_olympiad, intent.extras)
        handleIntent(intent)

        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.title.observeNonNull(this) {
            binding.tvTitle.text = it
        }
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.getParcelableExtra<Uri>(INTENT_EXTRA_URI)
        if (uri != null) {
            if (defaultPrefs2().getString(CoreConstants.STUDENT_LOGIN, "false") != "true"
                || !defaultPrefs2().getBoolean(CoreConstants.ON_BOARDING_COMPLETED, false)
            ) {
                CoreApplication.pendingDeeplink = uri.toString()
                startActivity(
                    Intent(ActivityAction.SPLASH_MAIN)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                )
                finish()
                return
            }

            val deeplinkUri = Uri.parse(uri.toString())
            val inclusive = deeplinkUri.toString().startsWith(DEEP_LINK_OLYMPIAD_REGISTER)
                    || deeplinkUri.toString().startsWith(DEEP_LINK_OLYMPIAD_REGISTER2)
                    || deeplinkUri.toString().startsWith(DEEP_LINK_OLYMPIAD_SUCCESS)
                    || deeplinkUri.toString().startsWith(DEEP_LINK_OLYMPIAD_SUCCESS2)
            navController.navigate(
                deeplinkUri,
                navOptions {
                    popUpTo(R.id.olympiadRegisterFragment) {
                        this.inclusive = inclusive
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
        const val TAG = "OlympiadActivity"

        private const val INTENT_EXTRA_URI = "uri"
        private const val DEEP_LINK_OLYMPIAD_REGISTER = "doubtnutapp://olympiad/register"
        private const val DEEP_LINK_OLYMPIAD_REGISTER2 = "doubtnutapp://olympiad-register"
        private const val DEEP_LINK_OLYMPIAD_SUCCESS = "doubtnutapp://olympiad/success"
        private const val DEEP_LINK_OLYMPIAD_SUCCESS2 = "doubtnutapp://olympiad-success"

        fun getStartIntent(
            context: Context
        ): Intent =
            Intent(context, OlympiadActivity::class.java)

        fun getDeeplinkStartIntent(context: Context, uri: Uri): Intent =
            Intent(context, OlympiadActivity::class.java).apply {
                putExtra(INTENT_EXTRA_URI, uri)
            }
    }
}