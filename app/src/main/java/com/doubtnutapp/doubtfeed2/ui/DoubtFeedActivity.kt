package com.doubtnutapp.doubtfeed2.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.navOptions
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.viewBinding
import com.doubtnutapp.databinding.ActivityDoubtFeedBinding
import com.doubtnut.core.utils.setStatusBarColor
import com.doubtnutapp.ui.base.BaseActivity
import io.branch.referral.Defines

/**
 * Created by devansh on 8/7/21.
 */

class DoubtFeedActivity : BaseActivity() {

    companion object {
        private const val INTENT_EXTRA_URI = "uri"

        fun getStartIntent(context: Context) = Intent(context, DoubtFeedActivity::class.java)

        fun getDeeplinkStartIntent(context: Context, uri: Uri): Intent =
            Intent(context, DoubtFeedActivity::class.java).apply {
                putExtra(INTENT_EXTRA_URI, uri)
            }
    }

    private val binding by viewBinding(ActivityDoubtFeedBinding::inflate)
    private val navController by lazy { findNavController(R.id.navHostDoubtFeed) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(R.color.grey_statusbar_color)
        setContentView(binding.root)

        val uri = intent.getParcelableExtra<Uri>(INTENT_EXTRA_URI)
        if (uri != null && navController.graph.hasDeepLink(uri)) {
            navController.navigate(
                uri,
                navOptions {
                    popUpTo(R.id.doubtFeedFragment2) {
                        inclusive = true
                    }
                }
            )
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
