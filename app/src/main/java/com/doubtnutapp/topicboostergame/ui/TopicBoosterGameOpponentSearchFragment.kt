package com.doubtnutapp.topicboostergame.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.databinding.FragmentTopicBoosterGameOpponentSearchBinding
import com.doubtnutapp.topicboostergame.extensions.loadOpponentImage
import com.doubtnutapp.topicboostergame.extensions.loadUserImage
import com.doubtnutapp.topicboostergame.extensions.mayNavigate
import com.doubtnutapp.topicboostergame.viewmodel.TopicBoosterGameViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Created by devansh on 24/2/21.
 */

class TopicBoosterGameOpponentSearchFragment :
    BaseBindingFragment<TopicBoosterGameViewModel, FragmentTopicBoosterGameOpponentSearchBinding>() {

    companion object {
        const val TAG = "TopicBoosterGameOpponentSearchFragment"
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTopicBoosterGameOpponentSearchBinding =
        FragmentTopicBoosterGameOpponentSearchBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): TopicBoosterGameViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        getDataIntent()
        binding.ivUser.loadUserImage(viewModel.userImageUrl)
        setupOpponentSearchAnimation()
    }

    private fun getDataIntent() {
        val testUUId = arguments?.getString(TopicBoosterGameActivity.INTENT_EXTRA_TEST_UUID)
        val onlinePlayersCountK = arguments?.getFloat(TopicBoosterGameActivity.INTENT_EXTRA_ONLINE_PLAYERS_COUNT_K) ?: 0f

        binding.ivOpponent.loadOpponentImage(viewModel.opponentImageUrl)
        binding.ivOpponent.setBackgroundColor(viewModel.opponentImageBackgroundColor)

        updateOnlinePlayersCountText(onlinePlayersCountK)

        viewModel.getTopicBoosterGameQuestionsList(viewModel.parentQuestionId, testUUId, viewModel.chapterAlias)
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.gameBannerDataLiveData.observe(viewLifecycleOwner) {
            if (it != null) {
                if (it.isAvailable) {
                    if (it.onlinePlayersCountK != null) {
                        viewModel.opponentImageUrl = it.opponentImage.orEmpty()
                        viewModel.opponentName = it.opponentName.orEmpty()

                        binding.ivOpponent.loadOpponentImage(viewModel.opponentImageUrl)
                        updateOnlinePlayersCountText(it.onlinePlayersCountK)
                    }
                } else {
                    activity?.finish()
                }
            }
        }
    }

    private fun setupOpponentSearchAnimation() {
        lifecycleScope.launch {
            delay(4000)
            //This needs to be observed here to prevent going to the next screen before questions are loaded
            viewModel.questionsListLiveData.observe(viewLifecycleOwner) {
                if (it.isNullOrEmpty().not()) {
                    binding.tvStatus.setText(R.string.found_your_opponent)
                    binding.tvStartingIn.setText(R.string.starting_in)
                    binding.tvOpponentName.text = viewModel.opponentName
                    binding.animationOpponentSearch.apply {
                        cancelAnimation()
                        hide()
                    }
                    binding.ivOpponent.show()
                    startTimerAnimation()
                }
            }
        }
    }

    private fun startTimerAnimation() {
        binding.animationTimer.show()
        binding.tvStartingIn.show()
        binding.animationTimer.addAnimatorEndListener {
            if (mayNavigate()) findNavController().navigate(R.id.actionStartQuiz)
        }
        binding.animationTimer.playAnimation()
    }

    private fun updateOnlinePlayersCountText(onlinePlayersCountK: Float) {
        binding.tvOnlinePlayers.text = getString(R.string.players_online, onlinePlayersCountK)
    }
}