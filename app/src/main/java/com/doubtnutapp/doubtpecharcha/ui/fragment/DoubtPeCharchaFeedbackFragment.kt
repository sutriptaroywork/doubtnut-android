package com.doubtnutapp.doubtpecharcha.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.databinding.FragmentDoubtPeCharchaFeedbackBinding
import com.doubtnutapp.doubtpecharcha.viewmodel.DoubtP2pViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment

class DoubtPeCharchaFeedbackFragment :
    BaseBindingDialogFragment<DoubtP2pViewModel, FragmentDoubtPeCharchaFeedbackBinding>() {

    companion object {
        const val TAG = "DoubtPeCharchaFeedbackFragment"
        private const val ROOM_ID = "room_id"
        fun newInstance(roomId: String) = DoubtPeCharchaFeedbackFragment().apply {
            val bundle = Bundle()
            bundle.putString(ROOM_ID, roomId)
            arguments = bundle
        }
    }

    private val roomId: String? by lazy {
        arguments?.getString(ROOM_ID)
    }

    private var feedbackButtonClickListener: FeedbackButtonClickListener? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentDoubtPeCharchaFeedbackBinding =
        FragmentDoubtPeCharchaFeedbackBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DoubtP2pViewModel = activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpClickListeners()
    }

    fun setButtonClickListener(feedbackButtonClickListener: FeedbackButtonClickListener) {
        this.feedbackButtonClickListener = feedbackButtonClickListener
    }

    private fun setUpClickListeners() {
        binding.btYes.setOnClickListener {
            viewModel.sendEvent(EventConstants.P2P_SOLUTION_FEEDBACK_YES)
            feedbackButtonClickListener?.onFeedbackYes()
            dismiss()
        }

        binding.btNo.setOnClickListener {
            viewModel.sendEvent(EventConstants.P2P_SOLUTION_FEEDBACK_NO)
            feedbackButtonClickListener?.onFeedbackNo()
            dismiss()
        }
    }

    interface FeedbackButtonClickListener {
        fun onFeedbackYes()
        fun onFeedbackNo()
    }
}
