package com.doubtnutapp.ui.mockTest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.FilterSelectAction
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.mocktest.MockTestAnalysisData
import com.doubtnutapp.databinding.ActivityMockTestAnalysisBinding
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.mockTest.viewmodel.MockTestAnalysisViewModel
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class MockTestAnalysisActivity : BaseBindingActivity<MockTestAnalysisViewModel,
        ActivityMockTestAnalysisBinding>(),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = ""
        const val SOURCE_MOCK_TEST = "mock_test"
        const val SUBJECT: String = "subject"
        fun getStartIntent(
            context: Context,
            testId: String,
            subject: String?,
            source: String?
        ): Intent {
            return Intent(context, MockTestAnalysisActivity::class.java).apply {
                putExtra(Constants.TEST_ID, testId)
                putExtra(SUBJECT, subject)
                putExtra(Constants.SOURCE, source)
            }
        }
    }

    private var testId: String = ""
    private var subject: String? = null
    private var adapter: WidgetLayoutAdapter? = null
    private var source: String = ""

    override fun provideViewBinding(): ActivityMockTestAnalysisBinding =
            ActivityMockTestAnalysisBinding.inflate(layoutInflater)

    override fun provideViewModel(): MockTestAnalysisViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        binding.ivBack.setOnClickListener {
            finish()
        }
        setupObservers()
        setupRecyclerView()
        testId = intent.getStringExtra(Constants.TEST_ID).orEmpty()
        subject = intent.getStringExtra(SUBJECT)
        source = intent.getStringExtra(Constants.SOURCE) ?: SOURCE_MOCK_TEST
        if (subject?.isEmpty() == true) {
            subject = null
        }
        viewModel.getAnalysisData(testId, subject)
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.MOCK_TEST_ANALYSIS_VIEW,
                hashMapOf(
                    EventConstants.TEST_ID to testId,
                    EventConstants.SOURCE to source
                ), ignoreSnowplow = true
            )
        )
    }

    private fun onAnalysisDataSuccess(data: MockTestAnalysisData) {
        binding.tvTitle.text = data.title.orEmpty()
        adapter = WidgetLayoutAdapter(this, this)
        binding.rvWidgets.adapter = adapter
        adapter?.setWidgets(data.widgets.orEmpty())
    }

    private fun setupRecyclerView() {
        binding.rvWidgets.layoutManager = LinearLayoutManager(this)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.analysisData.observeK(this,
                this::onAnalysisDataSuccess,
                this::onApiError,
                this::unAuthorizeUserError,
                this::ioExceptionHandler,
                this::updateProgressBarState)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    override fun providePageName(): String {
        return TAG
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    override fun performAction(action: Any) {
        if (action is FilterSelectAction) {
            subject = action.filterText
            viewModel.getAnalysisData(testId, subject)
        }
    }
}