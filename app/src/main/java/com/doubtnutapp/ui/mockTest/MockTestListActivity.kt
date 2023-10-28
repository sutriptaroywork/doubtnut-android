package com.doubtnutapp.ui.mockTest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.mocktest.MockTestListData
import com.doubtnutapp.databinding.ActivityMockTestListBinding
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.mockTest.event.RefreshMockTestList
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import io.reactivex.disposables.Disposable

class MockTestListActivity :
    BaseBindingActivity<MockTestListViewModel, ActivityMockTestListBinding>(), HasAndroidInjector {

    private lateinit var adapter: WidgetLayoutAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager
    private var mockTestTitle: String = ""
    private var courseName: String = ""

    companion object {
        private const val TAG = "MockTestListActivity"
        private const val COURSE_NAME = "course_name"

        fun getStartIntent(context: Context, courseName: String): Intent {
            return Intent(context, MockTestListActivity::class.java).apply {
                putExtra(COURSE_NAME, courseName)
            }
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    private var appStateObserver: Disposable? = null

    private fun setToolbar() {
        binding.tvTitle.text = mockTestTitle
    }

    private fun init() {
        viewModel = ViewModelProviders.of(this, viewModelFactory)[MockTestListViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = WidgetLayoutAdapter(this)
        layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMockTestList.layoutManager = layoutManager
        binding.recyclerViewMockTestList.adapter = adapter
    }

    private fun setUpListener() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }

        appStateObserver = DoubtnutApp.INSTANCE
            .bus()?.toObservable()?.subscribe {
                when (it) {
                    is RefreshMockTestList -> {
                        finish()
                    }
                }
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun setMockTestData(widgets: List<WidgetEntityModel<*, *>>?) {
        binding.progressBarMockTestList.hide()
        if (!widgets.isNullOrEmpty()) {
            adapter.setWidgets(widgets)
        } else {
            binding.recyclerViewMockTestList.visibility = View.GONE
            binding.tvNoTest.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appStateObserver?.dispose()
    }

    override fun provideViewBinding(): ActivityMockTestListBinding {
        return ActivityMockTestListBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MockTestListViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.testListData.observeK(
            this,
            this::onTestListSuccess,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )
    }

    private fun ioExceptionHandler() {
        if (NetworkUtils.isConnected(this).not()) {
            toast(getString(R.string.string_noInternetConnection))
        } else {
            toast(getString(R.string.somethingWentWrong))
        }
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBarMockTestList.setVisibleState(state)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onTestListSuccess(data: MockTestListData) {
        binding.tvTitle.text = data.title.orEmpty()
        setMockTestData(data.widgets)
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        statusbarColor(this, R.color.white_20)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        init()
        setupObservers()
        setToolbar()
        setupRecyclerView()
        setUpListener()
        getMockTestData()
    }

    private fun getMockTestData() {
        courseName = intent.getStringExtra(COURSE_NAME).orEmpty()
        viewModel.getTestList(courseName)
    }
}