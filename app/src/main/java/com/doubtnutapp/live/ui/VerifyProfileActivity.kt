package com.doubtnutapp.live.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.hide
import com.doubtnutapp.show
import com.doubtnutapp.utils.Utils
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.fragment_verify_profile.*
import javax.inject.Inject

class VerifyProfileActivity : AppCompatActivity() {

    companion object {

        fun getStartIntent(context: Context, canVerify: Boolean?, title: String?, subtitle: String?): Intent {
            return Intent(context, VerifyProfileActivity::class.java).apply {
                putExtra("title", title)
                putExtra("subtitle", subtitle)
                putExtra("can_verify", canVerify)
            }
        }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_verify_profile)

        analyticsPublisher.publishEvent(AnalyticsEvent(EventConstants.EVENT_NAME_VERIFY_PROFILE_VISIT, ignoreSnowplow = true))
        intent.getStringExtra("title")?.let {
            tvVerifyTitle.text = it
        }

        intent.getStringExtra("subtitle")?.let {
            tvVerifySubtitle.text = it
        }

        Utils.setMaxLinesEditText(etVerifyReason, 5)

        if (!intent.getBooleanExtra("can_verify", true)) {
            etVerifyReason.hide()
            btnSubmit.hide()
            tvCharacterCount.hide()
            btnCancel.text = "Cancel"
        }

        etVerifyReason.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(e: Editable?) {
                tvCharacterCount.text = "${e.toString().length}/200"
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }
        })

        btnClose.setOnClickListener {
            finish()
        }

        btnCancel.setOnClickListener {
            finish()
        }

        btnSubmit.setOnClickListener {
            if (etVerifyReason.text.isEmpty()) {
                toast("Please enter your reason for verification request")
                return@setOnClickListener
            }
            if (etVerifyReason.text.length < 10) {
                toast("Please describe your reason for verification request in detail")
                return@setOnClickListener
            }
            DataHandler.INSTANCE.teslaRepository
                    .requestUserVerification(etVerifyReason.text.toString())
                    .observe(this, Observer {
                        when (it) {
                            is Outcome.Progress -> {
                                progressBar.show()
                            }
                            is Outcome.Failure -> {
                                progressBar.hide()
                                toast("Failed to request verification")
                            }
                            is Outcome.ApiError -> {
                                progressBar.hide()
                                toast("Failed to request verification")
                            }
                            is Outcome.Success -> {
                                progressBar.hide()
                                toast("Verification request submitted")
                                analyticsPublisher.publishEvent(AnalyticsEvent(
                                        EventConstants.EVENT_NAME_VERIFY_PROFILE_SUBMITTED,
                                    ignoreSnowplow = true
                                ))
                                finish()
                            }
                        }
                    })
        }
    }
}