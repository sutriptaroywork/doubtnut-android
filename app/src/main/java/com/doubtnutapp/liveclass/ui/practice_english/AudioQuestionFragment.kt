package com.doubtnutapp.liveclass.ui.practice_english

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.TextViewUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.NextQuestionClicked
import com.doubtnutapp.base.Status
import com.doubtnutapp.base.TryAgainClicked
import com.doubtnutapp.base.extension.hasAudioRecordingPermission
import com.doubtnutapp.databinding.FragmentAudioQuestionBinding
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.FileUtils
import com.doubtnutapp.utils.setOnDebouncedClickListener
import com.doubtnutapp.utils.showToast
import java.io.IOException
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 03/12/21.
 */
class AudioQuestionFragment :
    BaseBindingFragment<PracticeEnglishViewModel, FragmentAudioQuestionBinding>() {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher
    private var questionId: String? = null
    private var data: AudioQuestionData? = null
    private var actionPerformer: ActionPerformer? = null
    private var recorder: MediaRecorder? = null
    private var audioFileName: String = ""
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
        const val TAG = "AudioQuestionFragment"
        private const val QUESTION_ID = "question_id"
        private const val DATA = "data"
        fun newInstance(questionId: String, data: AudioQuestionData) =
            AudioQuestionFragment().apply {
                arguments = Bundle().apply {
                    putString(QUESTION_ID, questionId)
                    putParcelable(DATA, data)
                }
            }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentAudioQuestionBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): PracticeEnglishViewModel =
        viewModelProvider(viewModelFactory)

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpVoiceNoteRecording()
        setUpUI()
    }

    private fun setUpUI() {
        binding.apply {
            title.text = data?.title
            tvQuestion.text = data?.question
            tvQuestion.setOnClickListener {
                setupMediaPlayer(data?.question_audio)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.QUESTION,
                            EventConstants.SOURCE to QuestionType.AUDIO_QUESTION
                        )
                    )
                )
            }
            ivSpeakerQuestion.setOnClickListener {
                setupMediaPlayer(data?.question_audio)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.QUESTION,
                            EventConstants.SOURCE to QuestionType.AUDIO_QUESTION
                        )
                    )
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
                            EventConstants.SOURCE to QuestionType.AUDIO_QUESTION
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
                            EventConstants.SOURCE to QuestionType.AUDIO_QUESTION
                        )
                    )
                )
            }

            btnSubmitQuestion.text = data?.submit_text
            btnSubmitQuestion.setOnDebouncedClickListener(2000) {
                viewModel.uploadAttachment(
                    filePath = audioFileName,
                    audioUploadUrl = data?.answerAudioUploadUrl.orEmpty()
                )
            }
        }
    }

    private val requestAudioPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            when {
                isGranted -> { }
                else -> toast(getString(R.string.needs_record_audio_permissions), Toast.LENGTH_SHORT)
            }
        }

    override fun setupObservers() {
        viewModel.stateLiveData.observe(requireActivity()) {
            when (it.status) {
                Status.SUCCESS -> {
                    FileUtils.deleteCacheDir(requireContext(), "tempUploadCache")
                    binding.fileUploadProgress.hide()
                    viewModel.checkAnswer(
                        questionId.orEmpty(),
                        QuestionType.AUDIO_QUESTION,
                        data?.answerAudioUploadUrl.orEmpty()
                    )
                }
                Status.NONE -> {
                    binding.fileUploadProgress.hide()
                }
                Status.LOADING -> {
                    binding.fileUploadProgress.show()
                    it.message?.toIntOrNull()?.let { progress ->
                        binding.fileUploadProgress.setProgressCompat(progress, true)
                    }
                }
                Status.ERROR -> {
                    FileUtils.deleteCacheDir(requireContext(), "tempUploadCache")
                    binding.fileUploadProgress.hide()
                    showToast(requireContext(), it.message!!)
                }
            }
        }

        viewModel.answerLiveData.observe(this, {

            binding.recordButton.setVisibleState(false)
            binding.cardViewAnswer.setVisibleState(true)
            binding.btnTryAgain.setVisibleState(true)
            binding.btnNextQuestion.setVisibleState(true)

            binding.apply {
                tvPercentage.text = it.matchPercent.toString()
                tvPercentage.setTextColor(Color.parseColor(it.percentageTextColor))
                tvCorrectText.text = it.correctText
                tvCorrectText.setTextColor(Color.parseColor(it.percentageTextColor))
                tvYourAnswerText.text = it.yourAnswerText
                tvYourAnswerText.setOnClickListener { _ ->
                    setupMediaPlayer(it.userAudioUrl)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PE_AUDIO_PLAY,
                            hashMapOf(
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.RESPONSE,
                                EventConstants.SOURCE to QuestionType.AUDIO_QUESTION
                            )
                        )
                    )
                }
                ivYourAnswerSpeaker.setOnClickListener { _ ->
                    setupMediaPlayer(it.userAudioUrl)
                    analyticsPublisher.publishEvent(
                        AnalyticsEvent(
                            EventConstants.PE_AUDIO_PLAY,
                            hashMapOf(
                                EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                                EventConstants.QUESTION_ID to questionId.orEmpty(),
                                EventConstants.TYPE to EventConstants.RESPONSE,
                                EventConstants.SOURCE to QuestionType.AUDIO_QUESTION
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
                                EventConstants.SOURCE to QuestionType.AUDIO_QUESTION
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
                                EventConstants.SOURCE to QuestionType.AUDIO_QUESTION
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

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpVoiceNoteRecording() {

        //IMPORTANT
        binding.recordButton.animation = null
        var actionDownMillis = 0L
        binding.recordButton.setOnTouchListener { view, motionEvent ->
            if (!hasAudioRecordingPermission()) {
                requestAudioPermission.launch(Manifest.permission.RECORD_AUDIO)
            } else {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        actionDownMillis = System.currentTimeMillis()
                        binding.tvHintText.text = ("Recording...")
                        onRecord(true)
                        ScaleAnim(binding.recordButton).start()
                    }
                    MotionEvent.ACTION_UP -> {
                        onRecordButtonReleased(actionDownMillis)
                        ScaleAnim(binding.recordButton).stop()
                        binding.tvHintText.text = ("Press and Hold for Recording")
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        onRecordButtonReleased(actionDownMillis)
                        ScaleAnim(binding.recordButton).stop()
                        binding.tvHintText.text = ("Press and Hold for Recording")
                    }
                }
            }
            true
        }

        audioFileName = "${requireActivity().externalCacheDir?.absolutePath}/learn_english.3gp"
    }

    private fun onRecordButtonReleased(actionDownMillis: Long) {
        if (System.currentTimeMillis() - actionDownMillis > 1000) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.PE_AUDIO_RECORD,
                    hashMapOf(
                        EventConstants.AUDIO_ID to data?.question_audio.orEmpty(),
                        EventConstants.QUESTION_ID to questionId.orEmpty(),
                        EventConstants.TYPE to EventConstants.QUESTION,
                        EventConstants.SOURCE to QuestionType.AUDIO_QUESTION
                    ),
                    ignoreMoengage = false
                )
            )
            binding.btnSubmitQuestion.setVisibleState(true)
        } else {
            binding.btnSubmitQuestion.setVisibleState(false)
            showToast(context, ("Press and Hold for Recording"))
        }
        onRecord(false)
        ScaleAnim(binding.recordButton).stop()
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
        releaseMediaPlayer()
    }

    private fun onRecord(start: Boolean) {
        if (start) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    private fun startRecording() {
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(audioFileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            try {
                prepare()
            } catch (e: IOException) {
                android.util.Log.e(TAG, "prepare() failed")
            }
            start()
        }
    }

    private fun stopRecording() {
        recorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        recorder = null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseMediaPlayer()
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

}

