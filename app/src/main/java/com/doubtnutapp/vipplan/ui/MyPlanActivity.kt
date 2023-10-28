package com.doubtnutapp.vipplan.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityMyPlanBinding
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.vipplan.viewmodel.MyPlanViewModel
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import javax.inject.Inject

class MyPlanActivity : BaseBindingActivity<MyPlanViewModel, ActivityMyPlanBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    companion object {
        const val TAG = "MyPlanActivity"
        fun getStartIntent(context: Context) =
            Intent(context, MyPlanActivity::class.java)
    }

    private var adapter: WidgetLayoutAdapter? = null

    override fun provideViewBinding(): ActivityMyPlanBinding {
        return ActivityMyPlanBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MyPlanViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun getStatusBarColor(): Int {
        return R.color.purple
    }

    override fun setupView(savedInstanceState: Bundle?) {
        setUpObserver()
        setUpRecyclerView()
        viewModel.fetchPlanDetail()
        binding.imageViewBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun setUpRecyclerView() {
        adapter = WidgetLayoutAdapter(this)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun setUpObserver() {
        viewModel.widgetsLiveData.observeK(
            this,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
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
        binding.progressBar.setVisibleState(state)
    }

    private fun onWidgetListFetched(list: List<WidgetEntityModel<*, *>>) {
        adapter?.setWidgets(list)
    }

}