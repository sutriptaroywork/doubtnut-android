package com.doubtnutapp.liveclass.ui.practice_english

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.base.*
import com.doubtnutapp.databinding.FragmentSingleChoiceQuestionBinding
import com.doubtnutapp.utils.setOnDebouncedClickListener
import com.doubtnut.core.utils.viewModelProvider
import java.util.*
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 03/12/21.
 */
class SingleChoiceQuestionFragment :
    BaseBindingFragment<PracticeEnglishViewModel, FragmentSingleChoiceQuestionBinding>(),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    private var questionId: String? = null
    private var data: SingleChoiceQuestionData? = null
    private var actionPerformer: ActionPerformer? = null
    private var mediaPlayer: MediaPlayer? = null

    private var answer: String? = null
    private val list: MutableList<MCQQuestionDataItem> = mutableListOf()

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
        private const val TAG = "SingleChoiceQuestionFra"
        private const val QUESTION_ID = "question_id"
        private const val DATA = "data"
        fun newInstance(questionId: String, data: SingleChoiceQuestionData) =
            SingleChoiceQuestionFragment().apply {
                arguments = Bundle().apply {
                    putString(QUESTION_ID, questionId)
                    putParcelable(DATA, data)
                }
            }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSingleChoiceQuestionBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): PracticeEnglishViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        binding.apply {
            title.text = data?.title
            tvQuestion.text = data?.question
            ivSpeakerQuestion.setVisibleState(data?.question_audio.isNullOrEmpty().not())
            ivSpeakerQuestion.setOnClickListener {
                setupMediaPlayer(data?.question_audio)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.QUESTION,
                            EventConstants.SOURCE to QuestionType.SINGLE_CHOICE_QUESTION
                        )
                    )
                )
            }
            btnSubmitQuestion.text = data?.submit_text
            btnSubmitQuestion.setOnClickListener {
                viewModel.checkAnswer(
                    questionId.orEmpty(),
                    QuestionType.TEXT_QUESTION,
                    if (answer.isNullOrEmpty()) return@setOnClickListener else answer.toString()
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
                            EventConstants.SOURCE to QuestionType.SINGLE_CHOICE_QUESTION
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
                            EventConstants.SOURCE to QuestionType.SINGLE_CHOICE_QUESTION
                        )
                    )
                )
            }
            list.clear()
            data?.options?.forEach { list.add(MCQQuestionDataItem(false, null, it)) }
            rvOptions.adapter =
                OptionsAdapter(
                    questionId.orEmpty(),
                    list,
                    this@SingleChoiceQuestionFragment,
                    analyticsPublisher
                )
            tvOptionsText.text = data?.otherOptionText
        }
    }

    override fun setupObservers() {
        viewModel.answerLiveData.observe(this, {
            binding.btnSubmitQuestion.setVisibleState(false)
            binding.btnNextQuestion.setVisibleState(true)
//            binding.btnTryAgain.setVisibleState(true)
            binding.btnNextQuestion.text = it.nextButtonText
            binding.btnTryAgain.text = it.tryAgainButtonText

            binding.apply {
                rvOptions.adapter =
                    OptionsAdapter(
                        questionId.orEmpty(),
                        it.options.orEmpty(),
                        this@SingleChoiceQuestionFragment,
                        analyticsPublisher
                    )
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

    override fun performAction(action: Any) {
        when (action) {
            is OptionSelected -> {
                if (answer == list[action.position].text) {
                    //Deselect logic
                    answer = null
                    list.forEach { it.isSelected = false }
                } else {
                    //Select Logic
                    answer = list[action.position].text
                    list.forEach { it.isSelected = false }
                    list[action.position].isSelected = true
                }
                binding.rvOptions.adapter =
                    OptionsAdapter(questionId.orEmpty(), list, this, analyticsPublisher)
            }
        }
    }
}