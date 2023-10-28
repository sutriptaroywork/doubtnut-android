package com.doubtnutapp.profile.social

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.databinding.ActivityReportUserBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.google.gson.internal.LinkedTreeMap
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ReportUserActivity : BaseBindingActivity<DummyViewModel, ActivityReportUserBinding>() {

    companion object {

        private const val TAG = "ReportUserActivity"

        fun startActivity(context: Context, userId: String, username: String) {
            Intent(context, ReportUserActivity::class.java).apply {
                putExtra(Constants.STUDENT_ID, userId)
                putExtra(Constants.STUDENT_USER_NAME, username)
            }.also {
                context.startActivity(it)
            }
        }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun provideViewBinding(): ActivityReportUserBinding {
        return ActivityReportUserBinding.inflate(layoutInflater)
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

    @SuppressLint("CheckResult", "SetTextI18n")
    override fun setupView(savedInstanceState: Bundle?) {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val userId = intent.getStringExtra(Constants.STUDENT_ID).orEmpty()
        val username = intent.getStringExtra(Constants.STUDENT_USER_NAME).orEmpty()

        binding.tvBlockUserTitle.text = "Report $username"

        binding.viewBlockUser.hide()
        binding.btnReportUser.hide()
        binding.progressBar.show()
        DataHandler.INSTANCE.socialRepository.userReportStatus(userId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                binding.progressBar.hide()
                if (it.error == null && it.data is LinkedTreeMap<*, *>) {
                    val alreadyReported = it.data["isReported"] as Boolean
                    if (alreadyReported) {
                        toast("You have already reported this user")
                        finish()
                    } else {
                        binding.viewBlockUser.show()
                        binding.btnReportUser.show()
                    }
                }
            }, {
                binding.progressBar.hide()
                binding.viewBlockUser.show()
                binding.btnReportUser.show()
            })

        binding.btnReportUser.setOnClickListener {
            binding.progressBar.show()
            DataHandler.INSTANCE.socialRepository.reportUser(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    toast("Reported $username")
                    finish()
                }, {
                    binding.progressBar.hide()
                    toast("Error reporting user")
                })
            analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_REPORT_USER_CONFIRM_CLICK, ignoreSnowplow = true))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            super.onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

}