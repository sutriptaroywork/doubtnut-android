package com.doubtnutapp.liveclass.ui.practice_english

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingFragment
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.TextViewUtils
import com.doubtnutapp.base.NextQuestionClicked
import com.doubtnutapp.base.TryAgainClicked
import com.doubtnutapp.databinding.FragmentMultiBlankQuestionBinding
import com.doubtnut.core.utils.viewModelProvider
import com.google.android.flexbox.FlexboxLayout
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.utils.setOnDebouncedClickListener
import javax.inject.Inject


/**
 * Created by Akshat Jindal on 03/12/21.
 */
class MultiBlankQuestionFragment :
    BaseBindingFragment<PracticeEnglishViewModel, FragmentMultiBlankQuestionBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    private var questionId: String? = null
    private var data: MultiBlankQuestionData? = null
    private var actionPerformer: ActionPerformer? = null
    private var mediaPlayer: MediaPlayer? = null
    private val answerTextViewList = mutableListOf<TextView>()

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
        private const val BLANK_SPACES = "            "
        private const val DELIMINATOR = "_____"
        fun newInstance(questionId: String, data: MultiBlankQuestionData) =
            MultiBlankQuestionFragment().apply {
                arguments = Bundle().apply {
                    putString(QUESTION_ID, questionId)
                    putParcelable(DATA, data)
                }
            }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentMultiBlankQuestionBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): PracticeEnglishViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpUi()
    }

    private fun setUpUi() {
        binding.apply {
            title.text = data?.title
            layoutAnswerSpeaker.setOnClickListener {
                setupMediaPlayer(data?.answer_audio)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.QUESTION,
                            EventConstants.SOURCE to QuestionType.MULTI_BLANK_QUESTION
                        )
                    )
                )
            }

            val lpRight = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )

            data?.question?.forEachIndexed { i, it ->

                val tvQuestions = TextView(requireContext()).apply {
                    tag = Integer.valueOf(i)
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
                    setTextColor(Color.BLACK)
                    gravity = Gravity.CENTER
                    if (it == DELIMINATOR) {
                        text = BLANK_SPACES
                        setBackgroundResource(R.drawable.rounded_corners_stroke_black_padding_10dp)
                        layoutParams = lpRight
                        val lp = this.layoutParams as FlexboxLayout.LayoutParams
                        lp.setMargins(10, 10, 10, 10)
                        this.layoutParams = lp

                        answerTextViewList.add(this)
                    } else {
                        text = it
                        layoutParams = lpRight
                        setPadding(0, 10, 0, 0)
                        val lp = this.layoutParams as FlexboxLayout.LayoutParams
                        lp.setMargins(10, 10, 0, 0)
                        this.layoutParams = lp
                    }
                }

                layoutWriteAnswer.addView(tvQuestions)
            }

            data?.otherOptions?.forEachIndexed { i, it ->
                val textView = TextView(requireContext()).apply {
                    tag = Integer.valueOf(i)
                    gravity = Gravity.CENTER
                    text = it
                    setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
                    setTextColor(Color.BLACK)
                    setBackgroundResource(R.drawable.rounded_corners_stroke_gray)
                    layoutParams = lpRight

                    setOnClickListener {
                        val isValid = addAnswerToFirstBlankAvailable(this)
                        if (isValid) {
                            this.setTextColor(Color.GRAY)
                            analyticsPublisher.publishEvent(
                                AnalyticsEvent(
                                    EventConstants.PE_BUTTON_CLICK,
                                    hashMapOf(
                                        EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                                        EventConstants.TYPE to EventConstants.WORD_BUBBLE,
                                        EventConstants.SOURCE to QuestionType.MULTI_BLANK_QUESTION
                                    )
                                )
                            )
                        }

                    }
                }

                val lp = textView.layoutParams as FlexboxLayout.LayoutParams
                lp.setMargins(10, 10, 10, 10)
                textView.layoutParams = lp
                layoutAnswerHint.addView(textView)
            }

            tvAnswer.text = data?.answer_text
            tvAnswerHintText.text = data?.otherOptionText

            btnSubmitQuestion.text = data?.submit_text
            btnSubmitQuestion.setOnClickListener {
                viewModel.checkAnswer(
                    questionId = questionId.orEmpty(),
                    questionType = QuestionType.MULTI_BLANK_QUESTION,
                    answer = "",
                    multiAnswers = getCombinedAnswerList()
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
                            EventConstants.SOURCE to QuestionType.MULTI_BLANK_QUESTION
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
                            EventConstants.SOURCE to QuestionType.MULTI_BLANK_QUESTION
                        )
                    )
                )
            }
            btnRefresh.text = data?.refresh_text
            btnRefresh.setOnClickListener {
                actionPerformer?.performAction(TryAgainClicked())
            }
        }
    }

    private fun getCombinedAnswerList(): List<String> {
        val answer = mutableListOf<String>()
        answerTextViewList.forEach { answer.add(it.text.toString()) }
        return answer
    }

    private fun addAnswerToFirstBlankAvailable(hintTextView: TextView): Boolean {
        val text = hintTextView.text.toString()
        answerTextViewList.forEach { answerTextView ->
            if (answerTextView.text.toString() == BLANK_SPACES && hintTextView.currentTextColor != Color.GRAY) {
                answerTextView.text = text
                return true
            }
        }
        return false
    }

    override fun setupObservers() {
        viewModel.answerLiveData.observe(this, {
            binding.btnSubmitQuestion.setVisibleState(false)
            binding.btnRefresh.setVisibleState(false)
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
                                EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.RESPONSE,
                                EventConstants.SOURCE to QuestionType.MULTI_BLANK_QUESTION
                            )
                        )
                    )
                }
                TextViewUtils.setTextFromHtml(tvYourAnswer, it.userTextDisplay.orEmpty())
                tvYourAnswer.setOnClickListener { _ ->
                    setupMediaPlayer(it.userAudioUrl)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PE_AUDIO_PLAY,
                            hashMapOf(
                                EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.RESPONSE,
                                EventConstants.SOURCE to QuestionType.MULTI_BLANK_QUESTION
                            )
                        )
                    )
                }
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
                                EventConstants.SOURCE to QuestionType.MULTI_BLANK_QUESTION
                            )
                        )
                    )
                }
                TextViewUtils.setTextFromHtml(tvCorrectAnswer, it.correctTextDisplay.orEmpty())
                tvCorrectAnswer.setOnClickListener { _ ->
                    setupMediaPlayer(it.answerAudioUrl)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PE_AUDIO_PLAY,
                            hashMapOf(
                                EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.CORRECT_ANSWER,
                                EventConstants.SOURCE to QuestionType.MULTI_BLANK_QUESTION
                            )
                        )
                    )
                }
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