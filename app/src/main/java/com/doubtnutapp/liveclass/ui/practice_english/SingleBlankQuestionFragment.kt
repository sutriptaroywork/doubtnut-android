package com.doubtnutapp.liveclass.ui.practice_english

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.TextViewUtils
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.NextQuestionClicked
import com.doubtnutapp.base.TryAgainClicked
import com.doubtnutapp.databinding.EditTextPracticeEnglishBinding
import com.doubtnutapp.databinding.FragmentSingleBlankQuestionBinding
import com.doubtnutapp.databinding.TextViewPracticeEnglishBinding
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.utils.setOnDebouncedClickListener
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.showToast
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 03/12/21.
 */
class SingleBlankQuestionFragment :
    BaseBindingFragment<PracticeEnglishViewModel, FragmentSingleBlankQuestionBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    private var questionId: String? = null
    private var data: SingleBlankQuestionData? = null
    private var actionPerformer: ActionPerformer? = null
    private var mediaPlayer: MediaPlayer? = null
    private val answerEditText by lazy {
        EditTextPracticeEnglishBinding.inflate(
            LayoutInflater.from(
                requireContext()
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            questionId = it.getString(QUESTION_ID)
            data = it.getParcelable(DATA)
        }
    }

    fun setUpActionPerformer(actionPerformer: ActionPerformer) {
        this.actionPerformer = actionPerformer
    }

    companion object {

        private const val TAG = "SingleBlankQuestionFrag"
        private const val QUESTION_ID = "question_id"
        private const val DATA = "data"

        private const val DELIMINATOR = "_____"

        fun newInstance(questionId: String, data: SingleBlankQuestionData) =
            SingleBlankQuestionFragment().apply {
                arguments = Bundle().apply {
                    putString(QUESTION_ID, questionId)
                    putParcelable(DATA, data)
                }
            }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSingleBlankQuestionBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): PracticeEnglishViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        openKeyboard()
        binding.apply {
            title.text = data?.title
            layoutAnswerSpeaker.setOnClickListener {
                setupMediaPlayer(data?.answer_audio)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to data?.answer_audio.orEmpty(),
                            EventConstants.QUESTION_ID to questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.QUESTION,
                            EventConstants.SOURCE to QuestionType.SINGLE_BLANK_QUESTION
                        )
                    )
                )
            }

            data?.question?.forEach {
                if (it == DELIMINATOR) {
                    layoutWriteAnswer.addView(answerEditText.root)
                } else {
                    val textView =
                        TextViewPracticeEnglishBinding.inflate(LayoutInflater.from(requireContext()))
                    textView.textView.apply {
                        text = it
                    }
                    layoutWriteAnswer.addView(textView.root)
                }
            }
            tvAnswer.text = data?.answer_text

            btnSubmitQuestion.text = data?.submit_text
            btnSubmitQuestion.setOnClickListener {
                if (answerEditText.editText.text.isNullOrEmpty())
                    showToast(requireContext(), "Kuch to likho!")
                else
                    viewModel.checkAnswer(
                        questionId.orEmpty(),
                        QuestionType.SINGLE_BLANK_QUESTION,
                        answerEditText.editText.text.toString()
                    )
            }
            btnNextQuestion.setOnDebouncedClickListener(2000) {
                actionPerformer?.performAction(NextQuestionClicked())
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.AUDIO_ID to data?.answer_audio.orEmpty(),
                            EventConstants.QUESTION to questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.NEXT,
                            EventConstants.SOURCE to QuestionType.SINGLE_BLANK_QUESTION
                        )
                    )
                )
            }
            btnTryAgain.setOnClickListener {
                actionPerformer?.performAction(TryAgainClicked())
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.AUDIO_ID to data?.answer_audio.orEmpty(),
                            EventConstants.QUESTION to questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.TRY_AGAIN,
                            EventConstants.SOURCE to QuestionType.SINGLE_BLANK_QUESTION
                        )
                    )
                )
            }
        }
    }

    override fun setupObservers() {
        viewModel.answerLiveData.observe(this, {
            binding.btnSubmitQuestion.setVisibleState(false)
            binding.cardViewWriterAnswer.setVisibleState(false)
            binding.layoutAnswerSpeaker.setVisibleState(false)
            binding.cardViewAnswer.setVisibleState(true)
            binding.btnNextQuestion.setVisibleState(true)
//            binding.btnTryAgain.setVisibleState(true)

            binding.apply {
                tvPercentage.text = it.matchPercent.toString()
                tvPercentage.setTextColor(Color.parseColor(it.percentageTextColor))
                tvCorrectText.text = it.correctText
                tvCorrectText.setTextColor(Color.parseColor(it.percentageTextColor))
                tvYourAnswerText.text = it.yourAnswerText
                ivYourAnswerSpeaker.setOnClickListener { _ ->
                    setupMediaPlayer(it.userAudioUrl)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PE_AUDIO_PLAY,
                            hashMapOf(
                                EventConstants.AUDIO_ID to data?.answer_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.RESPONSE,
                                EventConstants.SOURCE to QuestionType.SINGLE_BLANK_QUESTION
                            )
                        )
                    )
                }
                tvYourAnswer.setOnClickListener { _ ->
                    setupMediaPlayer(it.userAudioUrl)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PE_AUDIO_PLAY,
                            hashMapOf(
                                EventConstants.AUDIO_ID to data?.answer_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.RESPONSE,
                                EventConstants.SOURCE to QuestionType.SINGLE_BLANK_QUESTION
                            )
                        )
                    )
                }
                TextViewUtils.setTextFromHtml(tvYourAnswer, it.userTextDisplay.orEmpty())
                tvCorrectAnswerText.text = it.correctAnswerText
                ivCorrectAnswerSpeaker.setOnClickListener { _ ->
                    setupMediaPlayer(it.answerAudioUrl)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PE_AUDIO_PLAY,
                            hashMapOf(
                                EventConstants.AUDIO_ID to data?.answer_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.CORRECT_ANSWER,
                                EventConstants.SOURCE to QuestionType.SINGLE_BLANK_QUESTION
                            )
                        )
                    )
                }
                tvCorrectAnswer.setOnClickListener { _ ->
                    setupMediaPlayer(it.answerAudioUrl)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PE_AUDIO_PLAY,
                            hashMapOf(
                                EventConstants.AUDIO_ID to data?.answer_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.CORRECT_ANSWER,
                                EventConstants.SOURCE to QuestionType.SINGLE_BLANK_QUESTION
                            )
                        )
                    )
                }
                TextViewUtils.setTextFromHtml(tvCorrectAnswer, it.correctTextDisplay.orEmpty())
                btnTryAgain.text = it.tryAgainButtonText
                btnNextQuestion.text = it.nextButtonText
            }
        })
    }

    private fun setupMediaPlayer(url: String?) {

        if (url.isNullOrBlank())
            return

        releaseMediaPlayer()
        mediaPlayer = null
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
        }

        mediaPlayer?.setOnPreparedListener {
            if (mediaPlayer != null) {
                mediaPlayer?.start()
            }
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
    }

    override fun onStop() {
        super.onStop()
        releaseMediaPlayer()
    }
}