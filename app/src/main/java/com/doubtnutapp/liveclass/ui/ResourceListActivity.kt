package com.doubtnutapp.liveclass.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.EventBus.VipStateEvent
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.ResourceListData
import com.doubtnutapp.databinding.ActivityResourceListBinding
import com.doubtnutapp.liveclass.viewmodel.ResourceListViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.showApiErrorToast
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import io.reactivex.disposables.Disposable

class ResourceListActivity :
    BaseBindingActivity<ResourceListViewModel, ActivityResourceListBinding>() {

    companion object {
        private const val TAG = "ResourceListActivity"
        const val TITLE = "title"
        const val ID = "id"
        const val SUBJECT = "subject"
        fun startActivity(context: Context, id: String, subject: String): Intent {
            return Intent(context, ResourceListActivity::class.java).apply {
                putExtra(ID, id)
                putExtra(SUBJECT, subject)
            }
        }
    }

    private var id: String = "0"
    private var subject: String = ""
    private lateinit var adapter: WidgetLayoutAdapter
    private var vipObserver: Disposable? = null
    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener

    override fun provideViewBinding(): ActivityResourceListBinding {
        return ActivityResourceListBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): ResourceListViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        initUi()
        setupListeners()
        setUpObserver()
    }

    private fun setupListeners() {
        binding.ivBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initUi() {
        id = intent.getStringExtra(ID).orEmpty()
        subject = intent.getStringExtra(SUBJECT).orEmpty()
        val startPage = 1
        binding.rvWidgets.clearOnScrollListeners()
        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvWidgets.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    viewModel.getResourceList(currentPage, id, subject)
                }
            }.also {
                it.setStartPage(startPage)
            }
        binding.rvWidgets.addOnScrollListener(infiniteScrollListener)
        adapter = WidgetLayoutAdapter(this)
        binding.rvWidgets.adapter = adapter
        viewModel.extraParams.apply {
            put(EventConstants.EVENT_SCREEN_PREFIX + EventConstants.NAME, TAG)
        }
        viewModel.getResourceList(startPage, id, subject)
    }

    private fun setUpObserver() {
        viewModel.widgetsLiveData.observeK(
            this@ResourceListActivity,
            this::onWidgetListFetched,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgress
        )

        vipObserver = DoubtnutApp.INSTANCE.bus()?.toObservable()?.subscribe { event ->
            if (event is VipStateEvent) {
                if (event.state) {
                    initUi()
                }
            }
        }
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        showApiErrorToast(this)
    }

    private fun unAuthorizeUserError() {
        showApiErrorToast(this)
    }

    private fun updateProgress(state: Boolean) {
        infiniteScrollListener.setDataLoading(state)
        binding.progressBar.setVisibleState(state)
    }

    override fun onDestroy() {
        super.onDestroy()
        vipObserver?.dispose()
    }

    private fun onWidgetListFetched(data: ResourceListData) {
        if (data.widgets.isEmpty()) {
            infiniteScrollListener.isLastPageReached = true
        }
        if (infiniteScrollListener.currentPage == 1) {
            adapter.setWidgets(data.widgets)
        } else {
            adapter.addWidgets(data.widgets)
        }
        binding.tvToolbarTitle.text = data.title.orEmpty()
    }

}