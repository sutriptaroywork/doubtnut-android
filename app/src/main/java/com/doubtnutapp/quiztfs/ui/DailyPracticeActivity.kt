package com.doubtnutapp.quiztfs.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.quiztfs.DailyPracticeData
import com.doubtnutapp.databinding.ActivityDailyPracticeBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.quiztfs.viewmodel.DailyPracticeViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class DailyPracticeActivity :
    BaseBindingActivity<DailyPracticeViewModel, ActivityDailyPracticeBinding>() {

    @Inject
    lateinit var deeplinkAction: DeeplinkAction


    companion object {
        private const val TAG = "DailyPracticeActivity"
        fun getStartIntent(context: Context, source: String, type: String) =
            Intent(context, DailyPracticeActivity::class.java).apply {
                putExtra(Constants.SOURCE, source)
                putExtra(Constants.TYPE, type)
            }
    }

    private lateinit var adapter: WidgetLayoutAdapter

    override fun provideViewBinding(): ActivityDailyPracticeBinding =
        ActivityDailyPracticeBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DailyPracticeViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        binding.ivBack.setOnClickListener {
            goBack()
        }
        setupRecyclerView()
    }

    override fun onResume() {
        super.onResume()

        initiateRecyclerListenerAndFetchInitialData()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.dailyPracticeData.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )
    }

    private fun onSuccess(data: DailyPracticeData) {

        binding.pageTitle.text = data.pageTitle
        binding.tvFaq.text = data.toolbarTitle
        binding.tvFaq.setOnClickListener {
            deeplinkAction.performAction(this, data.toolbarDeeplink)
        }
        if (!data.widgets.isNullOrEmpty()) {
            adapter.setWidgets(data.widgets)
        }
        binding.rvWidgets.setBackgroundColor(Utils.parseColor(data.bgColor))
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgress(state: Boolean) {
        binding.progressBar.setVisibleState(state)
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {

        fetchList()
    }

    private fun setupRecyclerView() {

        adapter =
            WidgetLayoutAdapter(this)
        binding.rvWidgets.layoutManager = LinearLayoutManager(this)
        binding.rvWidgets.adapter = adapter

        initiateRecyclerListenerAndFetchInitialData()
    }

    private fun fetchList() {
        viewModel.fetch(intent?.getStringExtra("type").orEmpty())
    }
}