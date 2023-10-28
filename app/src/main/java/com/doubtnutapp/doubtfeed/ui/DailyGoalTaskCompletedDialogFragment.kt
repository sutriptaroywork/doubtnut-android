package com.doubtnutapp.doubtfeed.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.databinding.FragmentDailyGoalTaskCompletedDialogBinding
import com.doubtnutapp.doubtfeed.viewmodel.DoubtFeedViewModel
import com.doubtnutapp.loadImage
import com.doubtnutapp.ui.base.BaseBindingDialogFragment

/**
 * Created by Pankaj on 2021-04-24.
 */

class DailyGoalTaskCompletedDialogFragment :
    BaseBindingDialogFragment<DoubtFeedViewModel, FragmentDailyGoalTaskCompletedDialogBinding>() {

    companion object {
        const val TAG = "DailyGoalTaskCompletedDailyFragment"
        fun newInstance(): DailyGoalTaskCompletedDialogFragment =
            DailyGoalTaskCompletedDialogFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDailyGoalTaskCompletedDialogBinding =
        FragmentDailyGoalTaskCompletedDialogBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DoubtFeedViewModel {
        val doubtFeedViewModel: DoubtFeedViewModel by viewModels(
            ownerProducer = { requireParentFragment() }
        ) { viewModelFactory }
        return doubtFeedViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        val data = viewModel.taskCompletedPopupData ?: return

        binding.ivCongrats.loadImage(data.imageUrl)
        binding.tvCongrats.text = data.headingText
        binding.tvMesssage.text = data.subHeadingText
        binding.buttonStartTask.text = data.buttonText

        binding.buttonStartTask.setOnClickListener {
            with(viewModel) {
                getNextIncompleteDailyGoal()?.let {
                    performDeeplinkAction(view.context, it.data.deeplink)
                    submitDoubtCompletion(it.data.goalId, getString(R.string.daily_goal_done))
                    isNewDailyGoalViewed = true
                    sendEvent(
                        EventConstants.DF_GOAL_COMPLETED,
                        hashMapOf(
                            Constants.GOAL_NUMBER to (it.data.goalNumber),
                            Constants.WIDGET_TYPE to it.data.items?.firstOrNull()?.type.orEmpty(),
                            Constants.TOPIC to viewModel.currentTopic?.title.orEmpty(),
                        )
                    )
                }
            }
            dismiss()
        }

        binding.dailyGoalTaskDialogRootContainer.setOnClickListener { dismiss() }
        binding.dailyGoalTaskCardContainer.setOnClickListener { }
    }
}
