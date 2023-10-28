package com.doubtnutapp.feed.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.feed.viewmodel.UnbanActivityViewModel
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.ui.base.BaseActivity
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_unbanned_request.*
import javax.inject.Inject

class UnbannedRequestActivity : BaseActivity() {

    companion object {
        fun getStartIntent(context: Context) = Intent(context, UnbannedRequestActivity::class.java)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: UnbanActivityViewModel

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unbanned_request)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        viewModel = viewModelFactory.create(UnbanActivityViewModel::class.java)

        viewModel.eventLiveData.observe(this) {
            progressBar.hide()
            toast(it)
            setResult(Activity.RESULT_OK)
            finish()
        }

        submitUnbanRequest.setOnClickListener {
            if (checkboxUnbanPolicy.isChecked) {
                progressBar.show()
                analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.SUBMIT_FOR_UNBAN_REVIEW_CLICK, hashMapOf(Pair(EventConstants.SOURCE, "friends")),
                ignoreSnowplow = true))
                viewModel.sendUnbanRequest()
            } else {
                toast("Accept community guideline")
            }
        }

        checkboxUnbanPolicy.setOnCheckedChangeListener { buttonView, isChecked ->
            submitUnbanRequest.isEnabled = isChecked
        }
    }

}