package com.doubtnutapp.liveclass.ui.practice_english

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.TextViewUtils
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.NextQuestionClicked
import com.doubtnutapp.base.TryAgainClicked
import com.doubtnutapp.utils.setOnDebouncedClickListener
import com.doubtnutapp.databinding.FragmentTextQuestionBinding
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingFragment
import kotlinx.android.synthetic.main.fragment_text_question.*
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 03/12/21.
 */
class TextQuestionFragment :
    BaseBindingFragment<PracticeEnglishViewModel, FragmentTextQuestionBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    private var questionId: String? = null
    private var data: TextQuestionData? = null
    private var actionPerformer: ActionPerformer? = null
    private var mediaPlayer: MediaPlayer? = null

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
        private const val TAG = "TextQuestionFragment"
        private const val QUESTION_ID = "question_id"
        private const val DATA = "data"
        private const val REQUEST_CODE_SPEECH_INPUT = 1001
        fun newInstance(questionId: String, data: TextQuestionData) =
            TextQuestionFragment().apply {
                arguments = Bundle().apply {
                    putString(QUESTION_ID, questionId)
                    putParcelable(DATA, data)
                }
            }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTextQuestionBinding = FragmentTextQuestionBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): PracticeEnglishViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpUi()
    }

    private fun setUpUi() {
        binding.apply {
            btnSubmitQuestion.setVisibleState(true)
            cardViewWriterAnswer.setVisibleState(true)
            btnNextQuestion.setVisibleState(false)
            btnTryAgain.setVisibleState(false)
            cardViewAnswer.setVisibleState(false)

            title.text = data?.title
            tvQuestion.text = data?.question
            ivSpeakerQuestion.setOnClickListener {
                setupMediaPlayer(data?.question_audio)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.QUESTION,
                            EventConstants.SOURCE to QuestionType.TEXT_QUESTION
                        )
                    )
                )
            }
            ivMic.setVisibleState(data?.show_mic ?: false)
            ivMic.setOnClickListener {
                startSpeechToText()
            }
            btnSubmitQuestion.text = data?.submit_text
            btnSubmitQuestion.setOnClickListener {
                viewModel.checkAnswer(
                    questionId.orEmpty(),
                    QuestionType.TEXT_QUESTION,
                    etWriteAnswer.text.toString()
                )
            }
            btnNextQuestion.setOnDebouncedClickListener(2000) {
                actionPerformer?.performAction(NextQuestionClicked())
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                            EventConstants.QUESTION to questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.NEXT,
                            EventConstants.SOURCE to QuestionType.TEXT_QUESTION
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
                            EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                            EventConstants.QUESTION to questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.TRY_AGAIN,
                            EventConstants.SOURCE to QuestionType.TEXT_QUESTION
                        )
                    )
                )
            }
        }
    }

    private fun startSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                this@TextQuestionFragment.data?.language ?: "en"
            )
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to type")
        }
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT)
        } catch (e: Exception) {
            Toast
                .makeText(
                    requireContext(), " " + e.message,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS) ?: return
                etWriteAnswer.setText(result[0])
            }
        }
    }

    override fun setupObservers() {
        viewModel.answerLiveData.observe(this, {
            binding.btnSubmitQuestion.setVisibleState(false)
            binding.cardViewWriterAnswer.setVisibleState(false)
            binding.btnNextQuestion.setVisibleState(true)
//            binding.btnTryAgain.setVisibleState(true)
            binding.cardViewAnswer.setVisibleState(true)

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
                                EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.RESPONSE,
                                EventConstants.SOURCE to QuestionType.TEXT_QUESTION
                            )
                        )
                    )
                }
                tvYourAnswerText.setOnClickListener { _ ->
                    setupMediaPlayer(it.userAudioUrl)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PE_AUDIO_PLAY,
                            hashMapOf(
                                EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.RESPONSE,
                                EventConstants.SOURCE to QuestionType.TEXT_QUESTION
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
                                EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.CORRECT_ANSWER,
                                EventConstants.SOURCE to QuestionType.TEXT_QUESTION
                            )
                        )
                    )
                }
                tvCorrectAnswerText.setOnClickListener { _ ->
                    setupMediaPlayer(it.answerAudioUrl)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PE_AUDIO_PLAY,
                            hashMapOf(
                                EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.CORRECT_ANSWER,
                                EventConstants.SOURCE to QuestionType.TEXT_QUESTION
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