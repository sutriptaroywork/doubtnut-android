package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.doubtnut.core.utils.setStatusBarColor
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.viewBinding
import com.doubtnutapp.databinding.ActivityTopicBoosterGame2Binding
import com.doubtnutapp.ui.base.BaseActivity
import dagger.android.AndroidInjection
import io.branch.referral.Defines
import javax.inject.Inject

class TopicBoosterGameActivity2 : BaseActivity() {

    companion object {
        private const val INTENT_EXTRA_URI = "uri"

        fun getStartIntent(context: Context) = Intent(context, TopicBoosterGameActivity2::class.java)

        fun getDeeplinkStartIntent(context: Context, uri: Uri): Intent =
            Intent(context, TopicBoosterGameActivity2::class.java).apply {
                putExtra(INTENT_EXTRA_URI, uri)
            }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val navController by lazy { findNavController(R.id.nav_host_fragment_topic_booster_game) }
    private val binding by viewBinding(ActivityTopicBoosterGame2Binding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setStatusBarColor(R.color.colorPrimary)
        setContentView(binding.root)

        val uri = intent.getParcelableExtra<Uri>(INTENT_EXTRA_URI)
        if (uri != null && navController.graph.hasDeepLink(uri)) {
            navController.navigate(
                uri,
                navOptions {
                    popUpTo(R.id.tbgHomeFragment) {
                        inclusive = true
                    }
                })
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