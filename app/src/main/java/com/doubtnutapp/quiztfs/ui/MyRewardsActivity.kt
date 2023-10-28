package com.doubtnutapp.quiztfs.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.ScratchCardClicked
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.quiztfs.MyRewardsData
import com.doubtnutapp.databinding.ActivityMyRewardsBinding
import com.doubtnutapp.quiztfs.ScratchCardState.STATE_LOCKED
import com.doubtnutapp.quiztfs.viewmodel.MyRewardsViewModel
import com.doubtnutapp.quiztfs.widgets.DialogData
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.answer.TagsEndlessRecyclerOnScrollListener
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter

class MyRewardsActivity : BaseBindingActivity<MyRewardsViewModel,
        ActivityMyRewardsBinding>(),
    ActionPerformer {

    companion object {
        val TAG = "MyRewardsActivity"
        fun getStartIntent(context: Context, source: String, type: String) =
            Intent(context, MyRewardsActivity::class.java).apply {
                putExtra(Constants.SOURCE, source)
                putExtra(Constants.TYPE, type)
            }
    }

    private lateinit var adapter: WidgetLayoutAdapter
    private lateinit var infiniteScrollListener: TagsEndlessRecyclerOnScrollListener
    private  var type = ""

    override fun provideViewBinding(): ActivityMyRewardsBinding =
        ActivityMyRewardsBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): MyRewardsViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {
        binding.ivBack.setOnClickListener {
            goBack()
        }
        setupRecyclerView()
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.myRewards.observeK(
            this,
            ::onSuccess,
            ::onApiError,
            ::unAuthorizeUserError,
            ::ioExceptionHandler,
            ::updateProgress
        )
    }

    override fun performAction(action: Any) {
        when (action) {
            is ScratchCardClicked -> {
                if (action.dialogData.state == STATE_LOCKED) {
                    ToastUtils.makeText(this, "Unlock to view this reward!", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    openScratchCardDialog(action.dialogData)
                }
            }
        }
    }

    private fun openScratchCardDialog(dialogData: DialogData) {
        val dialog = MyRewardsScratchCardDialogFragment.newInstance(
            dialogData,
            object : MyRewardsScratchCardDialogFragment.ScratchListener {
                override fun onScratched(couponCode: Int?) {
                    viewModel.submitScratchStatus(couponCode)
                }

                override fun onApiSuccess() {
                    initiateRecyclerListenerAndFetchInitialData()
                }
            }
        )
        dialog.show(supportFragmentManager, MyRewardsScratchCardDialogFragment.TAG)
    }

    private fun onSuccess(myRewardsData: MyRewardsData) {
        setData(myRewardsData)
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
        infiniteScrollListener.setDataLoading(state)
        binding.progressBar.setVisibleState(state)
    }

    private fun setData(myRewardsData: MyRewardsData) {
        binding.pageTitle.text = myRewardsData.pageTitle
        if (myRewardsData.widgets.isNullOrEmpty()) {
            infiniteScrollListener.isLastPageReached = true
        } else {
            adapter.addWidgets(myRewardsData.widgets)
        }
    }

    private fun initiateRecyclerListenerAndFetchInitialData() {
        adapter.clearData()
        val startPage = 1
        binding.rvWidgets.clearOnScrollListeners()
        infiniteScrollListener =
            object : TagsEndlessRecyclerOnScrollListener(binding.rvWidgets.layoutManager) {
                override fun onLoadMore(currentPage: Int) {
                    fetchList(currentPage)
                }
            }.also {
                it.setStartPage(startPage)
            }
        binding.rvWidgets.addOnScrollListener(infiniteScrollListener)
        fetchList(startPage)
    }

    private fun fetchList(page: Int) {
        type = intent.getStringExtra(Constants.TYPE).orEmpty()
        viewModel.fetchRewards(page, type)
    }

    private fun setupRecyclerView() {
        adapter = WidgetLayoutAdapter(this, this)
        binding.rvWidgets.layoutManager = LinearLayoutManager(this)
        binding.rvWidgets.adapter = adapter

        initiateRecyclerListenerAndFetchInitialData()
    }
}