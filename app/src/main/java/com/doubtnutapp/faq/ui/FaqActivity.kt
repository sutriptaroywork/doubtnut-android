package com.doubtnutapp.faq.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityFaqBinding
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.faq.viewmodel.FaqViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class FaqActivity : BaseBindingActivity<FaqViewModel, ActivityFaqBinding>() {
    private lateinit var adapter: WidgetLayoutAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private var bucket: String? = null
    private var priority: String? = null

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = "FaqActivity"

        const val BUCKET = "bucket"
        const val PRIORITY = "priority"

        fun startActivity(
            context: Context,
            bucket: String, priority: String
        ): Intent {
            return Intent(context, FaqActivity::class.java).apply {
                putExtra(BUCKET, bucket)
                putExtra(PRIORITY, priority)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        super.onCreate(savedInstanceState)
    }

    override fun provideViewBinding(): ActivityFaqBinding =
        ActivityFaqBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): FaqViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        bucket = intent.getStringExtra(BUCKET).orEmpty()
        priority = intent.getStringExtra(PRIORITY).orEmpty()

        setupRecyclerView()
        setUpListener()
        sendEvent()

        if (bucket!!.isEmpty()) {
            bucket = null
        }

        if (priority!!.isEmpty()) {
            priority = null
        }

        viewModel.fetchFaqData(bucket, priority)
    }

    fun sendEvent() {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.FAQ_PAGE_OPEN,
                hashMapOf(),
                ignoreSnowplow = true
            )
        )
    }

    private fun setupRecyclerView() {
        adapter = WidgetLayoutAdapter(this)
        layoutManager = LinearLayoutManager(this)
        binding.recyclerViewFaq.layoutManager = layoutManager
        binding.recyclerViewFaq.adapter = adapter
    }

    private fun setUpListener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.widgetsLiveData.observeK(
            this,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        viewModel.header.observe(
            this,
            EventObserver {
                binding.tvTitle.text = it
            }
        )
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this)) {
            toast(getString(R.string.somethingWentWrong))
        } else {
            toast(getString(R.string.string_noInternetConnection))
        }
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBarFaq.setVisibleState(state)
    }

    private fun onWidgetListFetched(list: List<WidgetEntityModel<*, *>>) {
        adapter.setWidgets(list)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

}