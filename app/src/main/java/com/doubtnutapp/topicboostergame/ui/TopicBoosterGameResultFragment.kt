package com.doubtnutapp.topicboostergame.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R

import com.doubtnutapp.base.PlayVideo
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.topicboostergame.TopicBoosterGameResult
import com.doubtnutapp.databinding.FragmentTopicBoosterGameResultBinding
import com.doubtnutapp.resourcelisting.ui.adapter.ResourcePlaylistAdapter
import com.doubtnutapp.show
import com.doubtnutapp.toBundle
import com.doubtnutapp.topicboostergame.extensions.loadUserImage
import com.doubtnutapp.topicboostergame.viewmodel.TopicBoosterGameViewModel
import com.doubtnutapp.topicboostergame2.ui.TbgQuizFragment
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.EventObserver

/**
 * Created by devansh on 2/3/21.
 */

class TopicBoosterGameResultFragment : BaseBindingFragment<TopicBoosterGameViewModel, FragmentTopicBoosterGameResultBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "TopicBoosterGameResultFragment"
    }

    private var userWon: Boolean = false
    private val adapter: ResourcePlaylistAdapter by lazy {
        ResourcePlaylistAdapter(childFragmentManager, solutionVideoPage, this, false)
    }
    private val solutionVideoPage = Constants.PAGE_TOPIC_BOOSTER_GAME

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTopicBoosterGameResultBinding =
        FragmentTopicBoosterGameResultBinding.inflate(layoutInflater)

    override fun provideViewModel(): TopicBoosterGameViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        updateUi()
        setOnClickListeners()

        viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_PLAYED, hashMapOf(
            Constants.QUESTIONS_ATTEMPTED to viewModel.userAttemptedCount,
            Constants.TOTAL_QUESTIONS to viewModel.totalQuestions
        ))
    }

    private fun updateUi() {
        binding.ivUser.loadUserImage(viewModel.userImageUrl)

        val isQuit = arguments?.getBoolean(TbgQuizFragment.USER_QUIT_GAME) == true
        val result: Int

        if (isQuit) {
            binding.tvResult.setText(R.string.oops_you_lost)
            binding.tvSubtitle.setText(R.string.you_quit_the_game)
            userWon = false
            result = TopicBoosterGameResult.LOST
        } else if (viewModel.userScore > viewModel.opponentScore) {
            binding.tvResult.setText(R.string.you_won)
            binding.tvSubtitle.setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow_db9f00))
            userWon = true
            result = TopicBoosterGameResult.WON
            viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_WON)
        } else if (viewModel.userScore == viewModel.opponentScore) {
            binding.tvResult.setText(R.string.quiz_draw)
            binding.tvSubtitle.setText(R.string.both_of_you_scored_equal_points)
            userWon = false
            result = TopicBoosterGameResult.TIED
        } else {
            binding.tvResult.setText(R.string.oops_you_lost)
            binding.tvSubtitle.setText(R.string.better_luck_next_time)
            userWon = false
            result = TopicBoosterGameResult.LOST
        }

        binding.progressBarAccuracyUser.progressMax = viewModel.totalQuestions.toFloat()
        binding.progressBarAccuracyUser.progress = viewModel.userCorrectCount.toFloat()
        binding.tvAccuracyUser.text = getString(R.string.quiz_accuracy_stats, viewModel.userCorrectCount, viewModel.totalQuestions)

        binding.tvStatsTitleOpponent.text = getString(R.string.opponent_score, viewModel.opponentName)

        binding.progressBarAccuracyOpponent.progressMax = viewModel.totalQuestions.toFloat()
        binding.progressBarAccuracyOpponent.progress = viewModel.opponentCorrectCount.toFloat()
        binding.tvAccuracyOpponent.text = getString(R.string.quiz_accuracy_stats, viewModel.opponentCorrectCount, viewModel.totalQuestions)

        binding.rvSolutions.adapter = adapter

        viewModel.submitResult(result)
        viewModel.getQuizSolutions(requireContext(), viewModel.solutionsPlaylistId, viewModel.questionIds, isQuit)
    }

    private fun setOnClickListeners() {
        binding.buttonPlayAgain.setOnClickListener {
            findNavController().navigate(R.id.actionPlayAgain)
            viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_PLAY_AGAIN_CLICKED)
        }

        binding.buttonAskQuestion.setOnClickListener {
            viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_QUESTION_ASK_CLICKED)
            viewModel.deeplinkAction.performAction(requireContext(), "doubtnutapp://camera")
            activity?.finish()
        }

        binding.buttonHome.setOnClickListener {
            viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_GO_HOME_CLICKED)
            viewModel.deeplinkAction.performAction(requireContext(), "doubtnutapp://home")
            activity?.finish()
        }

        binding.viewUserStatsClickInterceptor.setOnClickListener {
            viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_USER_STATS_CLICK, hashMapOf(
                    Constants.USER_WON to userWon,
                    Constants.USER_CORRECT_ANSWERS to viewModel.userCorrectCount
            ))
        }
    }

    override fun setupObservers() {
        viewModel.quizSolutionsLiveData.observe(viewLifecycleOwner) {
            when (it) {
                is Outcome.Success -> {
                    binding.tvSolutionsTitle.show()
                    adapter.updateList(it.data.playlist.orEmpty(), viewModel.solutionsPlaylistId)
                }
            }
        }

        viewModel.navigateScreenLiveData.observe(viewLifecycleOwner, EventObserver {
            with(viewModel) {
                val args: Bundle? = it.second?.toBundle()
                screenNavigator.startActivityFromActivity(requireActivity(), it.first, args)
                val playlistId = args?.get(Constants.QUESTION_ID)
                sendEventByQid(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK, playlistId.toString())
                sendEventByEventTracker(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK + playlistId)
                sendCleverTapEvent(EventConstants.EVENT_NAME_PLAY_VIDEO_CLICK)
            }
        })

        viewModel.resultLiveData.observe(viewLifecycleOwner) {
            if (it?.rewardMessage != null) {
                binding.tvSubtitle.text = it.rewardMessage
            }
        }
    }

    override fun performAction(action: Any) {
        when (action) {
            is PlayVideo -> viewModel.openVideoScreen(action, solutionVideoPage)
        }
    }
}