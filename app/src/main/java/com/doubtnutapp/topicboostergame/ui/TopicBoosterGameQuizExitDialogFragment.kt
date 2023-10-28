package com.doubtnutapp.topicboostergame.ui

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.R
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.databinding.FragmentTopicBoosterGameQuizExitDialogBinding
import com.doubtnutapp.topicboostergame.viewmodel.TopicBoosterGameViewModel
import com.doubtnutapp.ui.base.BaseBindingDialogFragment

/**
 * Created by devansh on 2/3/21.
 */

class TopicBoosterGameQuizExitDialogFragment : BaseBindingDialogFragment<TopicBoosterGameViewModel, FragmentTopicBoosterGameQuizExitDialogBinding>() {

    companion object {
        const val TAG = "TopicBoosterGameQuizExitDialogFragment"
        const val USER_QUIT_GAME = "user_quit_game"
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTopicBoosterGameQuizExitDialogBinding =
        FragmentTopicBoosterGameQuizExitDialogBinding.inflate(layoutInflater)

    override fun provideViewModel(): TopicBoosterGameViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }

        binding.buttonQuit.setOnClickListener {
            findNavController().navigate(R.id.actionShowResult, bundleOf(USER_QUIT_GAME to true))
            viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_QUIT)
        }

        binding.buttonContinue.setOnClickListener {
            dismiss()
        }

        binding.ivClose.setOnClickListener {
            dialog?.cancel()
        }
    }
}