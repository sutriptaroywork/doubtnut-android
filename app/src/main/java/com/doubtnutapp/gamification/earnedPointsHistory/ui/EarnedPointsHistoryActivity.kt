package com.doubtnutapp.gamification.earnedPointsHistory.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.core.base.ActionPerformer

import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.databinding.ActivityEarnedPointsHistoryBinding
import com.doubtnutapp.delegation.networkerror.NetworkErrorHandler
import com.doubtnutapp.gamification.earnedPointsHistory.model.EarnedPointsBaseFeedViewItem
import com.doubtnutapp.gamification.earnedPointsHistory.ui.adapter.EarnedPointsHistoryListAdapter
import com.doubtnutapp.gamification.earnedPointsHistory.ui.viewmodel.EarnedPointsHistoryViewModel
import com.doubtnutapp.setVisibleState
import dagger.android.AndroidInjection
import javax.inject.Inject

class EarnedPointsHistoryActivity : AppCompatActivity(), ActionPerformer {

    override fun performAction(action: Any) {

    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var networkErrorHandler: NetworkErrorHandler

    lateinit var earnedPointsHistoryViewModel: EarnedPointsHistoryViewModel

    private lateinit var earnedPointsListAdapter: EarnedPointsHistoryListAdapter

    private lateinit var binding: ActivityEarnedPointsHistoryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = ActivityEarnedPointsHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        earnedPointsHistoryViewModel = ViewModelProviders.of(this, viewModelFactory).get(EarnedPointsHistoryViewModel::class.java)
        earnedPointsHistoryViewModel.getEarnedPointsHistory()

        setupObserver()

        earnedPointsListAdapter = EarnedPointsHistoryListAdapter(this)
        binding.earnedPointsHistoryListView.adapter = earnedPointsListAdapter

        setUpListener()
    }

    private fun setUpListener() {

        binding.closeButton.setOnClickListener {
            onBackPressed()
        }

    }

    private fun setupObserver() {
        earnedPointsHistoryViewModel.earnedPointsHistoryListDataModel.observeK(this,
                this::onSuccess,
                networkErrorHandler::onApiError,
                networkErrorHandler::unAuthorizeUserError,
                networkErrorHandler::ioExceptionHandler,
                this::updateProgressHandler
        )
    }

    private fun onSuccess(earnedPointsHistoryListDataModel: List<EarnedPointsBaseFeedViewItem>) {
        setupGameMilestoneList(earnedPointsHistoryListDataModel)
    }

    private fun updateProgressHandler(state: Boolean) {
        binding.progress.setVisibleState(state)
    }

    private fun setupGameMilestoneList(earnedPointsHistoryList: List<EarnedPointsBaseFeedViewItem>) {
        earnedPointsListAdapter.updateFeeds(earnedPointsHistoryList)
    }
}
