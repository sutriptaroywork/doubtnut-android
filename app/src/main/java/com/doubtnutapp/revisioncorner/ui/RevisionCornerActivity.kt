package com.doubtnutapp.revisioncorner.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.navigation.findNavController
import com.doubtnut.core.utils.setStatusBarColor
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.base.extension.viewBinding
import com.doubtnutapp.databinding.ActivityRevisionCornerBinding
import com.doubtnutapp.ui.base.BaseActivity
import io.branch.referral.Defines

/**
 * Created by devansh on 12/08/21.
 */

class RevisionCornerActivity : BaseActivity() {

    companion object {
        private const val INTENT_EXTRA_URI = "uri"

        fun getDeeplinkStartIntent(context: Context, uri: Uri): Intent =
            Intent(context, RevisionCornerActivity::class.java).apply {
                putExtra(INTENT_EXTRA_URI, uri)
            }
    }

    private val navController by lazy { findNavController(R.id.navHostRevisionCorner) }
    private val binding by viewBinding(ActivityRevisionCornerBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStatusBarColor(R.color.colorPrimary)
        setContentView(binding.root)
        navigateDeeplink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateDeeplink(intent)
    }

    override fun startActivity(intent: Intent?) {
        val isBranchLink = intent?.data?.host.equals(Constants.BRANCH_HOST)
        if (isBranchLink) {
            intent?.putExtra(Defines.IntentKeys.ForceNewBranchSession.key, true)
        }
        super.startActivity(intent)
    }

    private fun navigateDeeplink(intent: Intent?) {
        val uri = intent?.getParcelableExtra<Uri>(INTENT_EXTRA_URI) ?: return
        if (navController.graph.hasDeepLink(uri)) {
            navController.navigate(uri)
        }
    }
}