package com.doubtnutapp.ui.mockTest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.databinding.ActivityMockTestSubscriptionBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector

/**
 * Created by Anand Gaurav on 23/06/20.
 */
class MockTestSubscriptionActivity :
    BaseBindingActivity<MockTestSubscriptionViewModel, ActivityMockTestSubscriptionBinding>(),
    HasAndroidInjector {

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    companion object {
        const val TAG = "MockTestSubscriptionActivity"
        const val TEST_ID = "test_id"
        private const val IS_RETRY_ENABLED = "is_retry_enabled"

        fun getStartIntent(
            context: Context,
            testId: Int,
            isRetryEnabled: Boolean,
            source: String? = null,
            examType: String? = null,
            ruleId: Int? = null,
        ) = Intent(context, MockTestSubscriptionActivity::class.java)
            .apply {
                putExtra(TEST_ID, testId)
                putExtra(IS_RETRY_ENABLED, isRetryEnabled)
                putExtra(Constants.SOURCE, source)
                putExtra(Constants.EXAM_TYPE, examType)
                putExtra(Constants.RULE_ID, ruleId)
            }
    }

    var testId: Int? = null
    private var isRetryEnabled: Boolean? = false

    fun getMockTestData() {
        if (testId == null) return
        binding.buttonRetry.hide()
        viewModel.getMockTestData(testId!!)
    }

    private fun setUpObserver() {
        viewModel.mockTestLiveData.observeK(
            this,
            this::onMockDataSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )
    }

    private fun onMockDataSuccess(testDetails: TestDetails) {
        val intent = MockTestSectionActivity.liveClassIntent(
            fragmentActivity = this,
            testDetails = testDetails,
            source = intent?.getStringExtra(Constants.SOURCE),
            examType = intent?.getStringExtra(Constants.EXAM_TYPE),
            ruleId = intent?.getIntExtra(Constants.RULE_ID, Constants.DEFAULT_RULE_ID)
        )
        startActivity(intent)
        finish()
    }

    private fun unAuthorizeUserError() {
        if (isRetryEnabled == true) {
            binding.buttonRetry.show()
        }
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)

        if (isRetryEnabled == true) {
            binding.buttonRetry.show()
        } else {
            finish()
        }
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
        if (isRetryEnabled == true) {
            binding.buttonRetry.show()
        } else {
            finish()
        }
    }

    private fun updateProgress(state: Boolean) {
        binding.progress.setVisibleState(state)
    }

    override fun provideViewBinding(): ActivityMockTestSubscriptionBinding {
        return ActivityMockTestSubscriptionBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MockTestSubscriptionViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        statusbarColor(this, R.color.grey_statusbar_color)
        testId = intent.getIntExtra(TEST_ID, 0)
        isRetryEnabled = intent.getBooleanExtra(IS_RETRY_ENABLED, false)
        viewModel = viewModelProvider(viewModelFactory)
        setUpObserver()
        binding.buttonRetry.setOnClickListener {
            getMockTestData()
        }
        getMockTestData()
    }

}