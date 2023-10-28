package com.doubtnutapp.topicboostergame2.ui

import android.content.Context
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.addCallback
import androidx.annotation.Keep
import androidx.core.os.postDelayed
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.base.*
import com.doubtnutapp.base.extension.getNavigationResult
import com.doubtnutapp.base.extension.mayNavigate
import com.doubtnutapp.data.remote.models.topicboostergame.OptionStatus
import com.doubtnutapp.data.remote.models.topicboostergame2.*
import com.doubtnutapp.databinding.FragmentTbgQuizBinding
import com.doubtnutapp.socket.OnResponseData
import com.doubtnutapp.socket.SocketEventType
import com.doubtnutapp.topicboostergame2.extensions.loadOpponentImage
import com.doubtnutapp.topicboostergame2.extensions.loadUserImage
import com.doubtnutapp.topicboostergame2.ui.adapter.DialogEmojiAdapter
import com.doubtnutapp.topicboostergame2.ui.adapter.TbgQuizOptionAdapter
import com.doubtnutapp.topicboostergame2.viewmodel.TbgGameFlowViewModel
import com.doubtnutapp.ui.mediahelper.ExoPlayerHelper
import com.doubtnutapp.ui.mediahelper.MEDIA_TYPE_BLOB
import com.doubtnutapp.utils.EventObserver
import com.doubtnutapp.utils.extension.observeEvent
import com.google.android.exoplayer2.ui.PlayerView
import com.zhuinden.fragmentviewbindingdelegatekt.viewBinding
import dagger.android.support.AndroidSupportInjection
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Created by devansh on 27/6/21.
 */

class TbgQuizFragment : Fragment(R.layout.fragment_tbg_quiz), ActionPerformer2 {

    companion object {
        const val USER_QUIT_GAME = "user_quit_game"
        const val MESSAGE_DURATION = 3000L
        const val NEXT_QUESTION_DELAY = 2000L
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private val binding by viewBinding(FragmentTbgQuizBinding::bind)
    private val args by navArgs<TbgQuizFragmentArgs>()
    private val viewModel by navGraphViewModels<TbgGameFlowViewModel>(R.id.navGraphGameFlow) { viewModelFactory }
    private val navController by lazy { findNavController() }

    private lateinit var currentQuestionData: CurrentQuestionData
    private lateinit var countDownTimer: CountDownTimer

    private var isQuizOver: Boolean = false
    private var opponentAnswer: Int? = null
    private val userHandler = Handler(Looper.getMainLooper())
    private val opponentHandler = Handler(Looper.getMainLooper())
    private var isQuit: Boolean = false

    private val adapter: TbgQuizOptionAdapter by lazy {
        TbgQuizOptionAdapter(
            this,
            args.opponentImage,
            viewModel.opponentImageBackgroundColor
        )
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.mathViewQuestion.text = "Question"
        setupObservers()
        setupGame()
        setupMusic()
        with(binding) {
            ivUser.loadUserImage(viewModel.userImageUrl)
            ivOpponent.loadOpponentImage(args.opponentImage)
            ivOpponent.setBackgroundColor(viewModel.opponentImageBackgroundColor)
            tvOpponent.text = args.opponentName
            activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner) {
                showQuizExitDialog()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        //Need this call as nav controller won't navigate if app is in background
        if (isQuizOver) {
            showResult()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cancelCountDownTimer()
        userHandler.removeAllCallbacksAndMessages()
        opponentHandler.removeAllCallbacksAndMessages()
    }

    override fun performAction(action: Any) {
        when (action) {
            is TopicBoosterGameQuizOptionSelected -> {
                if (currentQuestionData.isAnswered) return
                currentQuestionData.isAnswered = true
                val currentQuestion = currentQuestionData.question
                val optionStatusList = currentQuestion.optionStatusList ?: return

                val isCorrect = action.optionNumber == currentQuestion.answerInt

                if (isCorrect) {
                    optionStatusList[action.optionNumber] = OptionStatus.CORRECT
                    viewModel.userScore += currentQuestionData.currentScore
                    updateUserScoreText()
                } else {
                    optionStatusList[action.optionNumber] = OptionStatus.INCORRECT
                    optionStatusList[currentQuestion.answerInt] = OptionStatus.CORRECT
                }
                val questionSubmitData = QuestionSubmit(
                    questionNumber = currentQuestionData.position,
                    option = action.optionNumber,
                    score = currentQuestionData.currentScore,
                    isCorrect = isCorrect,
                    gameId = args.gameId,
                    totalScore = viewModel.userScore,
                )
                viewModel.submitAnswer(currentQuestion.questionId, questionSubmitData, isCorrect)
                adapter.updateOptions(currentQuestionData.question.optionStatusPairs)
            }
            is TbgEmojiClicked -> {
                viewModel.apply {
                    sendEvent(EventConstants.TOPIC_BOOSTER_GAME_EMOJI_SENT)
                    sendEmoji(action.emoji)
                }
                hideEmojiSelectionPopup()
                userHandler.removeAllCallbacksAndMessages()
                binding.apply {
                    tvEmoji.text = action.emoji
                    textAnimator.setTransition(R.id.sendEmojiTransition)
                    textAnimator.progress = 0.0F
                    tvEmoji.show()
                    textAnimator.transitionToEnd()
                    userHandler.postDelayed(MESSAGE_DURATION) {
                        tvEmoji.invisible()
                    }
                }
            }
        }
    }

    private fun setupObservers() {
        viewModel.socketMessageLiveData.observe(viewLifecycleOwner, EventObserver {
            onSocketEvent(it)
        })

        viewModel.opponentQuestionSubmittedLiveData.observeEvent(viewLifecycleOwner) { data ->
            //Opponent submitted the answer
            opponentAnswer = data.option
            val currentQuestion = currentQuestionData.question
            if (data.questionNumber == currentQuestionData.position) {
                // data.correctQuestionIds == null means older app version without new socket keys or submission by bot,
                // so we use the old way of adding current question id
                if (data.isCorrect) {
                    if (data.totalScore == null) {
                        // Do it the old way, ignoring totalScore key
                        viewModel.opponentScore += data.score
                    }
                    if (data.correctQuestionIds == null) {
                        viewModel.opponentCorrectQuestions.add(currentQuestion.questionId)
                    }
                }
                opponentAnswer?.let {
                    currentQuestion.optionStatusList?.set(it, OptionStatus.OPPONENT_SELECTED)
                }
            }
            viewModel.answerSubmitted()
        }

        viewModel.isSubmissionDoneForQuestionLiveData.observe(viewLifecycleOwner, EventObserver {
            if (it) {
                lifecycleScope.launch {
                    updateOpponentScoreText()
                    adapter.updateOptions(currentQuestionData.question.optionStatusPairs)
                    adapter.updateOpponentAnswer(opponentAnswer)
                    delay(NEXT_QUESTION_DELAY)
                    updateUiForNextQuestion()
                }
            }
        })

        getNavigationResult<String>(TbgMessageDialogFragment.MESSAGE)?.observe(viewLifecycleOwner) {
            with(binding) {
                viewModel.apply {
                    sendEvent(EventConstants.TOPIC_BOOSTER_GAME_CHAT_MSG_SENT)
                    sendMessage(it)
                }
                userHandler.removeAllCallbacksAndMessages()
                tvMessage.text = it
                textAnimator.setTransition(R.id.sendMessageTransition)
                textAnimator.progress = 0.0F
                tvMessage.show()
                textAnimator.transitionToEnd()
                userHandler.postDelayed(MESSAGE_DURATION) {
                    tvMessage.invisible()
                }
            }
        }

        getNavigationResult<String>(getString(R.string.quit))?.observe(viewLifecycleOwner) {
            isQuit = true
            showResult()
            viewModel.sendEvent(EventConstants.TOPIC_BOOSTER_GAME_QUIT)
        }
    }

    private fun onSocketEvent(event: SocketEventType) {
        when (event) {
            is OnResponseData -> {
                onSocketResponseData(event)
            }
            else -> {
            }
        }
    }

    private fun onSocketResponseData(responseData: OnResponseData) {
        when (val data = responseData.data) {
            is QuestionSubmit -> {
                // Handled in ViewModel and via a separate LiveData exposed from ViewModel
            }
            is GameMessage -> {
                opponentHandler.removeAllCallbacksAndMessages()
                binding.apply {
                    textAnimator.progress = 1.0F
                    tvOpponentMessage.show()
                    tvOpponentMessage.text = data.message
                    textAnimator.setTransition(R.id.opponentSendMessageTransition)
                    textAnimator.progress = 0.0F
                    textAnimator.transitionToEnd()
                    opponentHandler.postDelayed(MESSAGE_DURATION) {
                        tvOpponentMessage.invisible()
                    }
                }
            }
            is GameEmoji -> {
                opponentHandler.removeAllCallbacksAndMessages()
                binding.apply {
                    textAnimator.progress = 1.0F
                    tvOpponentEmoji.show()
                    tvOpponentEmoji.text = data.emoji
                    textAnimator.setTransition(R.id.opponentSendEmojiTransition)
                    textAnimator.progress = 0.0F
                    textAnimator.transitionToEnd()
                    opponentHandler.postDelayed(MESSAGE_DURATION) {
                        tvOpponentEmoji.invisible()
                    }
                }
            }
        }
    }

    private fun setupGame() {
        val firstQuestion = viewModel.questionsList.firstOrNull() ?: return
        with(binding) {
            // Start from -1 as updateUiForNextQuestion() does +1
            currentQuestionData = CurrentQuestionData(
                question = firstQuestion,
                position = -1,
                currentScore = firstQuestion.maxScore,
                fraction = firstQuestion.fraction
            )
            adapter.resetAdapterData()
            rvOptions.adapter = adapter
            updateUiForNextQuestion()

            buttonChat.setOnClickListener {
                mayNavigate {
                    navigate(TbgQuizFragmentDirections.actionShowMessageDialog(viewModel.messages))
                }
            }

            buttonEmoji.setOnClickListener {
                if (containerEmoji.isVisible) {
                    hideEmojiSelectionPopup()
                } else {
                    showEmojiSelectionPopup(viewModel.emojis)
                }
            }

            popupBackground.setOnClickListener {
                hideEmojiSelectionPopup()
            }
        }
    }

    private fun updateUiForNextQuestion() {
        with(binding) {
            viewModel.resetQuestionSubmissionState()

            val newPosition = currentQuestionData.position + 1
            if (newPosition == viewModel.totalQuestions) {
                isQuizOver = true
                showResult()
                return
            }
            val newQuestion = viewModel.questionsList.getOrNull(newPosition) ?: return
            currentQuestionData = CurrentQuestionData(
                question = newQuestion,
                position = newPosition,
                currentScore = newQuestion.maxScore,
                fraction = newQuestion.fraction
            )

            updateUserScoreText()
            updateOpponentScoreText()

            tvProgress.text =
                getString(R.string.quiz_progress, newPosition + 1, viewModel.questionsList.size)
            mathViewQuestion.apply {
                setFontSize(16)
                setTextColor("black")
                text = newQuestion.questionText
            }
            adapter.updateOptions(newQuestion.optionStatusPairs)
            adapter.updateOpponentAnswer(null)

            val totalDurationSeconds = newQuestion.expire.toLong()
            val totalDurationMillis = TimeUnit.SECONDS.toMillis(totalDurationSeconds)

            val progressMax = totalDurationMillis.toInt()
            progressBar.max = progressMax
            progressBarDark.max = progressMax

            tvTimeRemaining.text = getString(R.string.duration_second, totalDurationSeconds)

            cancelCountDownTimer()

            countDownTimer = object : CountDownTimer(totalDurationMillis, 10) {
                private var timeRemainingSeconds: Long =
                    TimeUnit.MILLISECONDS.toSeconds(totalDurationMillis)

                private val botMessagesMap = newQuestion.botMessages.associate { it.time to it.message }
                private val botEmojisMap = newQuestion.botEmoji.associate { it.time to it.message }

                override fun onTick(millisUntilFinished: Long) {
                    val timeElapsed = (totalDurationMillis - millisUntilFinished).toInt()
                    val timeElapsedSeconds = TimeUnit.MILLISECONDS.toSeconds(timeElapsed.toLong()).toInt()

                    progressBar.progress = timeElapsed
                    progressBarDark.progress = timeElapsed

                    val currentTimeRemainingSeconds =
                        TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1

                    if (timeRemainingSeconds != currentTimeRemainingSeconds) {
                        timeRemainingSeconds = currentTimeRemainingSeconds
                        tvTimeRemaining.text =
                            getString(R.string.duration_second, timeRemainingSeconds)

                        val currentScore = calculateCurrentScore(
                            currentQuestionData.question,
                            timeElapsedSeconds,
                        )
                        currentQuestionData.currentScore = currentScore
                        tvScore.text = currentScore.toString()

                        //Bot answer and chat flow
                        if (args.isOpponentBot) {
                            if ((totalDurationSeconds - currentTimeRemainingSeconds) >= newQuestion.botMeta?.responseTime ?: 10
                                && newQuestion.botMeta?.answer.isNullOrBlank().not()
                            ) {
                                if (currentQuestionData.isOpponentAnswered.not()) {
                                    currentQuestionData.isOpponentAnswered = true
                                    newQuestion.botMeta?.let { submitBotAnswer(it) }
                                }
                            }
                            botMessagesMap[timeElapsedSeconds]?.let {
                                sendBotMessage(it)
                            }
                            botEmojisMap[timeElapsedSeconds]?.let {
                                sendBotEmoji(it)
                            }
                        }
                    }
                }

                override fun onFinish() {
                    tvTimeRemaining.text = getString(R.string.duration_second, 0)
                    updateUiForNextQuestion()
                }
            }.start()
        }
    }

    private fun submitBotAnswer(botMeta: BotMeta) {
        val answerInt = botMeta.answerInt ?: return
        val isCorrect = answerInt == currentQuestionData.question.answerInt
        val botTotalScore = viewModel.opponentScore + getScore(isCorrect)
        val questionSubmitData = QuestionSubmit(
            questionNumber = currentQuestionData.position,
            option = answerInt,
            score = getScore(isCorrect),
            isCorrect = isCorrect,
            gameId = args.gameId,
            totalScore = botTotalScore,
        )
        viewModel.handleQuestionSubmitSocketEvent(questionSubmitData)
    }

    private fun sendBotMessage(message: String) {
        onSocketResponseData(OnResponseData(GameMessage(message)))
    }

    private fun sendBotEmoji(emoji: String) {
        onSocketResponseData(OnResponseData(GameEmoji(emoji)))
    }

    private fun getScore(isCorrect: Boolean): Int {
        return if (isCorrect) currentQuestionData.currentScore else 0
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

    private fun setupMusic() {
        val url = viewModel.musicUrl ?: return

        ExoPlayerHelper(requireContext(), PlayerView(requireContext())).apply {
            lifecycle.addObserver(this)
            enableLoopCurrentVideo()
            setMediaData(MEDIA_TYPE_BLOB)
            setVideoUrl(url)
            setFallbackUrl(url)

            binding.buttonMusic.apply {
                isSelected = true
                setOnClickListener {
                    isSelected = if (isMute()) {
                        unMute()
                        setText(R.string.music_on)
                        true
                    } else {
                        mute()
                        setText(R.string.music_off)
                        false
                    }
                }
            }
        }
    }

    private fun showEmojiSelectionPopup(data: List<String>) {
        with(binding) {
            containerEmoji.show()
            popupBackground.show()

            val emojiAdapter = DialogEmojiAdapter(this@TbgQuizFragment)
            rvEmoji.apply {
                layoutManager = GridLayoutManager(requireContext(), 3)
                adapter = emojiAdapter
            }
            emojiAdapter.updateList(data)

            ivClose.setOnClickListener {
                hideEmojiSelectionPopup()
            }
        }
    }

    private fun hideEmojiSelectionPopup() {
        with(binding) {
            containerEmoji.hide()
            popupBackground.hide()
        }
    }

    private fun calculateCurrentScore(question: TbgQuestion, timeRemaining: Int): Int {
        /**
         * currentScore = maxScore - floor(timeRemaining / fraction) * multiplier
         * fraction = totalTime / 6 (for now, comes in API)
         * multiplier (comes in API)
         */
        return question.maxScore - (timeRemaining / question.fraction) * question.multiplier
    }

    private fun showResult() {
        // Adding mayNavigate here doesn't work when we are navigating to result screen from dialog
        val currentDestinationId = navController.currentDestination?.id
        if (currentDestinationId == R.id.tbgQuizFragment
            || currentDestinationId == R.id.tbgPopupDialogFragmentGameFlow) {
            navController.navigate(viewModel.getShowResultAction(args, isQuit))
        }
    }

    private fun showQuizExitDialog() {
        val action = TbgQuizFragmentDirections.actionShowQuizExitDialog(
            PopupDialogData(
                description = getString(R.string.quiz_exit_dialog_info),
                button1 = getString(R.string.quit),
                button2 = getString(R.string.i_will_play),
            )
        )
        mayNavigate {
            navController.navigate(action)
        }
    }

    @Keep
    private data class CurrentQuestionData(
        val question: TbgQuestion,
        val position: Int,
        var currentScore: Int,
        var fraction: Int,
        var isAnswered: Boolean = false,
        var isOpponentAnswered: Boolean = false,
    )
}