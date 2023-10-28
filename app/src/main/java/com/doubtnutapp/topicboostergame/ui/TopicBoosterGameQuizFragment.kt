package com.doubtnutapp.topicboostergame.ui

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.annotation.Keep
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.doubtnutapp.R
import com.doubtnut.core.utils.activityViewModelProvider

import com.doubtnutapp.base.ActionPerformer2
import com.doubtnutapp.base.TopicBoosterGameQuizOptionSelected
import com.doubtnutapp.data.remote.models.topicboostergame.OptionStatus
import com.doubtnutapp.data.remote.models.topicboostergame.TopicBoosterGameQuestion
import com.doubtnutapp.databinding.FragmentTopicBoosterGameQuizBinding
import com.doubtnutapp.topicboostergame.extensions.loadOpponentImage
import com.doubtnutapp.topicboostergame.extensions.loadUserImage
import com.doubtnutapp.topicboostergame.ui.adapter.TopicBoosterGameQuizOptionAdapter
import com.doubtnutapp.topicboostergame.viewmodel.TopicBoosterGameViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Created by devansh on 25/2/21.
 */

class TopicBoosterGameQuizFragment : BaseBindingFragment<TopicBoosterGameViewModel, FragmentTopicBoosterGameQuizBinding>(), ActionPerformer2 {

    companion object {
        const val TAG = "TopicBoosterGameQuizFragment"
    }

    private val NEXT_QUESTION_DELAY = 2000L

    private lateinit var currentQuestionData: CurrentQuestionData
    private lateinit var countDownTimer: CountDownTimer

    private var isQuizOver: Boolean = false

    private val adapter: TopicBoosterGameQuizOptionAdapter by lazy {
        TopicBoosterGameQuizOptionAdapter(this, viewModel.opponentImageUrl, viewModel.opponentImageBackgroundColor)
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTopicBoosterGameQuizBinding =
        FragmentTopicBoosterGameQuizBinding.inflate(layoutInflater)

    override fun provideViewModel(): TopicBoosterGameViewModel =
        activityViewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        binding.ivUser.loadUserImage(viewModel.userImageUrl)
        binding.ivOpponent.loadOpponentImage(viewModel.opponentImageUrl)
        binding.ivOpponent.setBackgroundColor(viewModel.opponentImageBackgroundColor)
        binding.tvOpponent.text = viewModel.opponentName
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
            findNavController().navigate(R.id.actionShowExitDialog)
        }
    }

    override fun onStart() {
        super.onStart()
        //Need this call as nav controller won't navigate if app is in background
        if (isQuizOver) {
            findNavController().navigate(R.id.actionShowResult)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelCountDownTimer()
    }

    override fun performAction(action: Any) {
        when (action) {
            is TopicBoosterGameQuizOptionSelected -> {
                if (currentQuestionData.isAnswered) return
                currentQuestionData.isAnswered = true
                val currentQuestion = currentQuestionData.question
                val optionStatusList = currentQuestion.optionStatusList ?: return

                viewModel.userAttemptedCount += 1
                val isCorrect = action.optionNumber == currentQuestion.answerInt

                if (isCorrect) {
                    optionStatusList[action.optionNumber] = OptionStatus.CORRECT
                    viewModel.userScore += currentQuestion.score
                    viewModel.userCorrectCount += 1
                    updateUserScoreText()
                } else {
                    optionStatusList[action.optionNumber] = OptionStatus.INCORRECT
                    optionStatusList[currentQuestion.answerInt] = OptionStatus.CORRECT
                }
                viewModel.saveUserResponse(currentQuestion, isCorrect, viewModel.parentQuestionId, currentQuestionData.position)

                adapter.updateOptions(currentQuestionData.question.optionStatusPairs)
                if (currentQuestionData.isOpponentAnswered) {
                    lifecycleScope.launch {
                        updateOpponentScoreText()
                        adapter.updateOpponentAnswer(currentQuestion.botMeta.answerInt)
                        delay(NEXT_QUESTION_DELAY)
                        updateUiForNextQuestion()
                    }
                }
            }
        }
    }

    override fun setupObservers() {
        viewModel.questionsListLiveData.observe(viewLifecycleOwner) {
            if (it.isNullOrEmpty()) return@observe
            // Start from -1 as updateQuizUi() does +1
            currentQuestionData = CurrentQuestionData(it.first(), -1)
            adapter.resetAdapterData()
            binding.rvOptions.adapter = adapter
            updateUiForNextQuestion()
        }
    }

    private fun updateUiForNextQuestion() {
        val newPosition = currentQuestionData.position + 1
        if (newPosition == viewModel.totalQuestions) {
            isQuizOver = true
            findNavController().navigate(R.id.actionShowResult)
            return
        }
        val newQuestion = viewModel.questionsListLiveData.value?.getOrNull(newPosition) ?: return
        currentQuestionData = CurrentQuestionData(newQuestion, newPosition)

        updateUserScoreText()
        updateOpponentScoreText()

        binding.tvProgress.text = getString(R.string.quiz_progress, newPosition + 1, viewModel.questionsListLiveData.value?.size)
        binding.mathViewQuestion.apply {
            setFontSize(16)
            setTextColor("black")
            text = newQuestion.questionText
        }
        adapter.updateOptions(newQuestion.optionStatusPairs)
        adapter.updateOpponentAnswer(null)

        val totalDurationSeconds = newQuestion.expire.toLong()
        val totalDurationMillis = TimeUnit.SECONDS.toMillis(totalDurationSeconds)

        binding.progressBar.max = totalDurationMillis.toInt()
        binding.tvTimeRemaining.text = getString(R.string.duration_second, totalDurationSeconds)

        cancelCountDownTimer()
        countDownTimer = object : CountDownTimer(totalDurationMillis, 10) {

            private var timeRemainingSeconds: Long = TimeUnit.MILLISECONDS.toSeconds(totalDurationMillis)

            override fun onTick(millisUntilFinished: Long) {
                binding.progressBar.progress = (totalDurationMillis - millisUntilFinished).toInt()
                val currentTimeRemainingSeconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1
                if (timeRemainingSeconds != currentTimeRemainingSeconds) {
                    timeRemainingSeconds = currentTimeRemainingSeconds
                    binding.tvTimeRemaining.text = getString(R.string.duration_second, timeRemainingSeconds)

                    if ((totalDurationSeconds - currentTimeRemainingSeconds) >= newQuestion.botMeta.responseTime
                            && newQuestion.botMeta.answer.isNullOrBlank().not()) {
                        if (currentQuestionData.isOpponentAnswered.not()) {
                            currentQuestionData.isOpponentAnswered = true
                            newQuestion.botMeta.answerInt?.let {
                                viewModel.opponentAttemptedCount += 1
                                if (it == newQuestion.answerInt) {
                                    viewModel.opponentCorrectCount += 1
                                    viewModel.opponentScore += newQuestion.score
                                }
                                newQuestion.optionStatusList?.set(it, OptionStatus.OPPONENT_SELECTED)
                            }

                            if (currentQuestionData.isAnswered) {
                                lifecycleScope.launch {
                                    updateOpponentScoreText()
                                    adapter.updateOptions(currentQuestionData.question.optionStatusPairs)
                                    adapter.updateOpponentAnswer(newQuestion.botMeta.answerInt)
                                    delay(NEXT_QUESTION_DELAY)
                                    updateUiForNextQuestion()
                                }
                            }
                        }
                    }
                }
            }

            override fun onFinish() {
                binding.tvTimeRemaining.text = getString(R.string.duration_second, 0)
                updateUiForNextQuestion()
            }
        }.start()
    }

    private fun updateUserScoreText() {
        binding.tvUserScore.text = getString(R.string.quiz_score, viewModel.userScore)
    }

    private fun updateOpponentScoreText() {
        binding.tvOpponentScore.text = getString(R.string.quiz_score, viewModel.opponentScore)
    }

    private fun cancelCountDownTimer() {
        if (::countDownTimer.isInitialized) {
            countDownTimer.cancel()
        }
    }

    @Keep
    private data class CurrentQuestionData(
            val question: TopicBoosterGameQuestion,
            val position: Int,
            var isAnswered: Boolean = false,
            var isOpponentAnswered: Boolean = false
    )
}