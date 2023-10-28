package com.doubtnutapp.inappupdate.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.doubtnutapp.databinding.ActivityImmediateUpdateBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnut.core.utils.viewModelProvider

class ImmediateUpdateActivity :
    BaseBindingActivity<DummyViewModel, ActivityImmediateUpdateBinding>() {

    companion object {
        private const val TAG = "ImmediateUpdateActivity"
        const val IMMEDIATE_UPDATE_REQUEST_CODE = 102
        fun startActivity(activity: Activity, start: Boolean = true) =
            Intent(activity, ImmediateUpdateActivity::class.java).apply {
                if (start)
                    activity.startActivityForResult(this, IMMEDIATE_UPDATE_REQUEST_CODE)
            }
    }

    private fun initUi() {
        binding.ivClose.setOnClickListener {
            onBackPressed()
        }
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        if (intent.resolveActivity(packageManager) != null) {
            binding.updateButton.visibility = View.VISIBLE
            setClickListener(intent)
        } else {
            binding.updateButton.visibility = View.GONE
        }
    }

    private fun setClickListener(intent: Intent) {
        binding.updateButton.setOnClickListener {
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
    }

    override fun provideViewBinding(): ActivityImmediateUpdateBinding {
        return ActivityImmediateUpdateBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): DummyViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        initUi()
    }
}