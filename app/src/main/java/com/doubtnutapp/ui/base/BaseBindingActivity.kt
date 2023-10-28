package com.doubtnutapp.ui.base

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewbinding.ViewBinding
import com.doubtnut.core.ui.base.CoreBindingActivity
import com.doubtnutapp.Constants
import com.doubtnutapp.auth.ui.GoogleAuthActivity
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.defaultPrefs

abstract class BaseBindingActivity<VM : BaseViewModel, VB : ViewBinding>
    : CoreBindingActivity<VM, VB>() {

    /**
     * Finish current activity if gmail verification failed, otherwise continue with the feature.
     */
    private val googleAuthResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != Activity.RESULT_OK) {
                finish()
            }
        }

    /**
     * Verify user with google auth as per google restriction to use social feature
     */
    fun verifyUserWithGoogle() {
        val isGmailVerified = defaultPrefs().getBoolean(Constants.GMAIL_VERIFIED, false)
        if (isGmailVerified) return
        googleAuthResultLauncher.launch(getGoogleAuthIntent())
    }

    private fun getGoogleAuthIntent() = GoogleAuthActivity.getStartIntent(binding.root.context)
}