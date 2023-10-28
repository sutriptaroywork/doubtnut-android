package com.doubtnutapp.liveclass.ui.practice_english

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Parcelable
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.AskPermission
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.databinding.WidgetPracticeEnglishBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnut.core.widgets.entities.WidgetLayoutConfig
import com.doubtnutapp.utils.FileUtils
import com.doubtnutapp.utils.setOnDebouncedClickListener
import com.doubtnutapp.utils.showToast
import com.doubtnut.core.widgets.ui.WidgetBindingVH
import com.doubtnutapp.widgetmanager.widgets.BaseBindingWidget
import com.doubtnutapp.widgetmanager.widgets.BaseWidget
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import javax.inject.Inject

import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import com.doubtnut.core.utils.*
import com.doubtnutapp.databinding.EditTextPracticeEnglishBinding
import com.doubtnutapp.databinding.TextViewPracticeEnglishBinding
import com.google.android.flexbox.FlexboxLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics

class PracticeEnglishWidget(context: Context) :
    BaseBindingWidget<PracticeEnglishWidget.PracticeEnglishWidgetHolder,
            PracticeEnglishWidget.PracticeEnglishWidgetModel, WidgetPracticeEnglishBinding>(context) {

    @Inject
    lateinit var practiceEnglishRepository: PracticeEnglishRepository

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    private var recorder: MediaRecorder? = null
    private var audioFileName: String = ""
    private var binding: WidgetPracticeEnglishBinding? = null
    private var questionNumber = 0
    private var mediaPlayer: MediaPlayer? = null
    private var data: PracticeEnglishWidgetData? = null
    private var currentQuestion: PracticeEnglishWidgetItems? = null
    private var currentQuestionType: String = ""
    private var isPermissionGranted: Boolean = false
    private val answerEditText by lazy {
        EditTextPracticeEnglishBinding.inflate(
            LayoutInflater.from(
                context
            )
        )
    }
    private val answerTextViewList = mutableListOf<TextView>()
    private var isAnswerShown = false

    init {
        DoubtnutApp.INSTANCE.daggerAppComponent?.inject(this)
    }

    companion object {
        private const val HOME = "home"
        private const val DELIMINATOR = "_____"
        private const val BLANK_SPACES = "            "
    }

    override fun getViewBinding(): WidgetPracticeEnglishBinding {
        return WidgetPracticeEnglishBinding
            .inflate(LayoutInflater.from(context), this, true)
    }

    override fun setupViewHolder() {
        this.widgetViewHolder = PracticeEnglishWidgetHolder(getViewBinding(), this)
    }

    override fun bindWidget(
        holder: PracticeEnglishWidgetHolder,
        model: PracticeEnglishWidgetModel
    ): PracticeEnglishWidgetHolder {
        super.bindWidget(holder, model.apply {
            layoutConfig = WidgetLayoutConfig(12, 12, 0, 0)
        })
        binding = holder.binding
        currentQuestion = model.data.questions?.getOrNull(questionNumber)
        currentQuestionType = currentQuestion?.questionType.orEmpty()
        data = model.data
        if (data?.questions.isNullOrEmpty())
            return holder
        if (isAnswerShown) {
            showAnswerScreen()
            return holder
        }
        setUpUI()
        binding?.btnSubmitQuestion
            ?.setOnDebouncedClickListener(2000) {
                binding?.progressBar?.show()
                context?.hideKeyboard(this)
                if (currentQuestionType == QuestionType.AUDIO_QUESTION.toString()) {
                    val audioFile = File(audioFileName)
                    val fileBody = ProgressRequestBody(
                        file = audioFile,
                        content_type = "video/3gpp",
                        listener = object : ProgressRequestBody.UploadProgressListener {
                            override fun onProgressUpdate(percentage: Int) {
                                binding?.progressBar?.show()
                            }
                        })
                    practiceEnglishRepository.uploadAttachment(
                        model.data.questions?.getOrNull(questionNumber)?.audioUploadUrl.orEmpty(),
                        fileBody
                    ).applyIoToMainSchedulerOnSingle().subscribeToSingle({
                        onSuccess()
                    }, {
                        binding?.progressBar?.hide()
                        onError(it)
                    })
                } else if (currentQuestionType == QuestionType.TEXT_QUESTION.toString() &&
                    binding?.etWriteAnswer?.text.isNotNullAndNotEmpty()
                ) {
                    val hashmap = hashMapOf<String, Any>()
                    hashmap["session_id"] = data?.sessionId.orEmpty()
                    hashmap["textResponse"] = binding?.etWriteAnswer?.text.toString()
                    checkAnswers(hashmap)
                } else if (currentQuestionType == QuestionType.SINGLE_BLANK_QUESTION.toString()
                    && answerEditText.editText.text.toString()
                        .isNotNullAndNotEmpty()
                ) {
                    val hashmap = hashMapOf<String, Any>()
                    hashmap["session_id"] = data?.sessionId.orEmpty()
                    hashmap["textResponse"] = answerEditText.editText.text.toString()
                    checkAnswers(hashmap)
                } else if (currentQuestionType == QuestionType.MULTI_BLANK_QUESTION.toString()) {
                    var emptyCount = 0
                    getCombinedAnswerList().forEach {
                        if (it == BLANK_SPACES)
                            emptyCount++
                    }
                    if (emptyCount == getCombinedAnswerList().size) {
                        binding?.progressBar?.hide()
                        showToast(
                            context,
                            currentQuestion?.errorToastText.orDefaultValue("Kuch to likho!")
                        )
                    } else {
                        val hashmap = hashMapOf<String, Any>()
                        hashmap["session_id"] = data?.sessionId.orEmpty()
                        hashmap["textResponse"] = getCombinedAnswerList()
                        checkAnswers(hashmap)
                    }
                } else {
                    binding?.progressBar?.hide()
                    showToast(
                        context,
                        currentQuestion?.errorToastText.orDefaultValue("Kuch to likho!")
                    )
                }
            }
        return holder
    }

    private fun setUpUI() {
        binding?.apply {
            tvQuestionNumber.text =
                (questionNumber + 1).toString() + "/" + data?.questions?.size.toString()
            title.text = data?.title.orEmpty()
            practiceEnglishTitle.text = data?.practiceEnglishTitle.orEmpty()
            practiceEnglishCta.text = data?.practiceEnglishCta.orEmpty()
            practiceEnglishLayout.setOnClickListener {
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_RECORD,
                        hashMapOf(
                            EventConstants.AUDIO_ID to currentQuestion?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to currentQuestion?.questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.QUESTION,
                            EventConstants.SOURCE to currentQuestionType,
                            EventConstants.PAGE to HOME
                        ),
                        ignoreMoengage = false
                    )
                )
                deeplinkAction.performAction(context, data?.deeplink.orEmpty())
            }
            tvHindiText.text = currentQuestion?.title
            tvQuestion.text = currentQuestion?.question
            tvQuestion.setOnClickListener {
                setupMediaPlayer(currentQuestion?.question_audio)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to currentQuestion?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to currentQuestion?.questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.QUESTION,
                            EventConstants.SOURCE to currentQuestionType,
                            EventConstants.PAGE to HOME
                        ),
                        ignoreMoengage = false
                    )
                )
            }
            ivSpeakerQuestion.setOnClickListener {
                setupMediaPlayer(currentQuestion?.question_audio)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to currentQuestion?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to currentQuestion?.questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.QUESTION,
                            EventConstants.SOURCE to currentQuestionType,
                            EventConstants.PAGE to HOME
                        ),
                        ignoreMoengage = false
                    )
                )
            }
            btnNextQuestion.setOnClickListener {
                isAnswerShown = false
                when (currentQuestionType) {
                    QuestionType.AUDIO_QUESTION.toString() -> {
                        binding?.recordButton?.visibility = INVISIBLE
                        binding?.tvHintText?.setVisibleState(false)
                    }
                    QuestionType.TEXT_QUESTION.toString() -> {
                        cardViewWriterAnswer.setVisibleState(false)
                    }
                    QuestionType.SINGLE_BLANK_QUESTION.toString() -> {
                        cardViewQuestion.visibility = INVISIBLE
                        cardViewSingleBlankAnswer.setVisibleState(false)
                        binding?.layoutAnswerHint?.setVisibleState(false)
                        binding?.tvAnswerHintText?.setVisibleState(false)
                        binding?.layoutAnswerSpeaker?.setVisibleState(false)
                    }
                    QuestionType.MULTI_BLANK_QUESTION.toString() -> {
                        binding?.cardViewSingleBlankAnswer?.setVisibleState(false)
                        binding?.layoutAnswerHint?.setVisibleState(false)
                        binding?.tvAnswerHintText?.setVisibleState(false)
                        binding?.layoutAnswerSpeaker?.setVisibleState(false)
                    }
                }
                questionNumber++
                if (questionNumber < data?.questions?.size!!) {
                    currentQuestion = data?.questions?.getOrNull(questionNumber)
                    currentQuestionType = currentQuestion?.questionType.orEmpty()
                    binding?.cardViewAnswer?.setVisibleState(false)
                    binding?.btnSubmitQuestion?.setVisibleState(false)
                    binding?.btnNextQuestion?.setVisibleState(false)
                    setUpUI()
                } else {
                    endTest()
                }
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.AUDIO_ID to currentQuestion?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to currentQuestion?.questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.NEXT,
                            EventConstants.SOURCE to currentQuestionType,
                            EventConstants.PAGE to HOME
                        ),
                        ignoreMoengage = false
                    ),
                )
            }
            btnTryAgain.setOnClickListener {
                binding?.recordButton?.setVisibleState(true)
                binding?.cardViewAnswer?.setVisibleState(false)
                binding?.btnSubmitQuestion?.setVisibleState(false)
                binding?.btnNextQuestion?.setVisibleState(false)
                binding?.tvHintText?.setVisibleState(true)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.AUDIO_ID to currentQuestion?.question_audio.orEmpty(),
                            EventConstants.QUESTION to currentQuestion?.questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.TRY_AGAIN,
                            EventConstants.SOURCE to currentQuestionType,
                            EventConstants.PAGE to HOME
                        ),
                        ignoreMoengage = false
                    )
                )
            }

            binding?.btnSubmitQuestion?.text = data?.submitText.orEmpty()
            if (data?.hintText.isNotNullAndNotEmpty()) {
                binding?.tvHintText?.text = data?.hintText
            }
            when (currentQuestionType) {
                QuestionType.AUDIO_QUESTION.toString() -> {
                    binding?.recordButton?.setVisibleState(true)
                    binding?.tvHintText?.setVisibleState(true)
                    binding?.cardViewQuestion?.setVisibleState(true)
                    setUpVoiceNoteRecording()
                }
                QuestionType.TEXT_QUESTION.toString() -> {
                    cardViewQuestion.setVisibleState(true)
                    btnSubmitQuestion.setVisibleState(true)
                    cardViewWriterAnswer.setVisibleState(true)
                    binding?.etWriteAnswer?.setText("")
                }
                QuestionType.SINGLE_BLANK_QUESTION.toString() -> {
                    handleSingleBlankQuestion()
                }
                QuestionType.MULTI_BLANK_QUESTION.toString() -> {
                    handleMultiBlankQuestion()
                }
                else -> {
                    currentQuestion = data?.questions?.getOrNull(questionNumber)
                    currentQuestionType = currentQuestion?.questionType.orEmpty()
                    binding?.btnNextQuestion?.setVisibleState(true)
                }
            }
        }
    }

    private fun handleMultiBlankQuestion() {
        binding?.apply {
            val lpRight = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            binding?.cardViewSingleBlankAnswer?.setVisibleState(true)
            binding?.layoutAnswerHint?.setVisibleState(true)
            binding?.tvAnswerHintText?.setVisibleState(true)
            binding?.layoutAnswerSpeaker?.setVisibleState(true)
            binding?.recordButton?.visibility = INVISIBLE
            binding?.btnSubmitQuestion?.visibility = VISIBLE
            binding?.layoutWriteAnswer?.removeAllViews()
            answerTextViewList.clear()
            currentQuestion?.blankQuestion?.forEachIndexed { i, it ->

                val tvQuestions = TextView(context).apply {
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
                binding?.layoutWriteAnswer?.addView(tvQuestions)
            }
            binding?.layoutAnswerHint?.removeAllViews()
            currentQuestion?.otherOptions?.forEachIndexed { i, it ->
                val textView = TextView(context).apply {
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
                                        EventConstants.AUDIO_ID to currentQuestion?.question_audio.orEmpty(),
                                        EventConstants.TYPE to EventConstants.WORD_BUBBLE,
                                        EventConstants.SOURCE to currentQuestionType,
                                        EventConstants.PAGE to HOME
                                    )
                                )
                            )
                        }

                    }
                }

                val lp = textView.layoutParams as FlexboxLayout.LayoutParams
                lp.setMargins(10, 10, 10, 10)
                textView.layoutParams = lp
                binding?.layoutAnswerHint?.addView(textView)
            }

            binding?.tvAnswer?.text = currentQuestion?.answer_text
            binding?.tvAnswerHintText?.text = currentQuestion?.otherOptionText
            binding?.layoutAnswerSpeaker?.setOnClickListener {
                setupMediaPlayer(currentQuestion?.answer_audio)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to currentQuestion?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to currentQuestion?.questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.QUESTION,
                            EventConstants.SOURCE to currentQuestionType,
                            EventConstants.PAGE to HOME
                        )
                    )
                )
            }
            binding?.refreshMultiBlank?.setVisibleState(true)
            binding?.refreshMultiBlank?.setOnClickListener {
                handleMultiBlankQuestion()
            }
        }
    }

    private fun handleSingleBlankQuestion() {
        binding?.apply {
            cardViewAnswer.visibility = INVISIBLE
            cardViewSingleBlankAnswer.setVisibleState(true)
            cardViewQuestion.visibility = GONE
            btnSubmitQuestion.setVisibleState(true)
            binding?.layoutAnswerSpeaker?.setVisibleState(true)
            answerEditText.editText.setText("")
            answerEditText.editText.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus) {
                    context?.hideKeyboard(view)
                }
            }
            layoutWriteAnswer.removeAllViews()
            currentQuestion?.blankQuestion?.forEach {
                if (it == DELIMINATOR) {
                    layoutWriteAnswer.addView(answerEditText.root)
                } else {
                    val textView =
                        TextViewPracticeEnglishBinding.inflate(
                            LayoutInflater.from(
                                context
                            )
                        )
                    textView.textView.apply {
                        text = it
                    }
                    layoutWriteAnswer.addView(textView.root)
                }
            }
            binding?.tvAnswer?.text = currentQuestion?.answer_text
            binding?.layoutAnswerSpeaker?.setOnClickListener {
                setupMediaPlayer(currentQuestion?.answer_audio)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to currentQuestion?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to currentQuestion?.questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.QUESTION,
                            EventConstants.SOURCE to currentQuestionType,
                            EventConstants.PAGE to HOME
                        )
                    )
                )
            }
        }
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

    private fun endTest() {
        (context as? AppCompatActivity)?.lifecycleScope?.launch {
            practiceEnglishRepository.getEndScreenData(data?.sessionId.orEmpty(), HOME)
                .map { it.data }
                .catch { }
                .collect {
                    showEndScreen(it)
                }
        }
    }

    private fun showEndScreen(endScreenData: PracticeEndScreenData) {
        binding?.contentLayout?.visibility = View.INVISIBLE
        binding?.resultLayout?.visibility = View.VISIBLE
        binding?.tvEnglishPractice?.text = data?.title.orEmpty()
        binding?.tvResultTitle?.setTextFromHtml(endScreenData.display_title_text.orEmpty())
        binding?.ivResult?.loadImageEtx(endScreenData.image_url.orEmpty())
        binding?.tvResultSubTitle?.text = endScreenData.display_subtitle_text.orEmpty()
        binding?.tvShare?.text = data?.shareText.orEmpty()
        binding?.ivShare?.loadImageEtx(data?.shareImageUrl.orEmpty())
        binding?.tvShare?.setOnClickListener {
            deeplinkAction.performAction(context, data?.shareDeeplink.orEmpty())
        }
        if (endScreenData.questions.isNullOrEmpty()) {
            binding?.btnPracticeMore?.visibility = INVISIBLE
        } else {
            binding?.btnPracticeMore?.visibility = VISIBLE
            val btnText: String = if (endScreenData.practice_more_button_text.isNullOrEmpty()) {
                endScreenData.try_again_button_text.orEmpty()
            } else {
                endScreenData.practice_more_button_text.orEmpty()
            }
            binding?.btnPracticeMore?.text = btnText
            binding?.btnPracticeMore?.setOnClickListener {
                showPracticeMoreData(endScreenData)
            }
        }
    }

    private fun showPracticeMoreData(endScreenData: PracticeEndScreenData) {
        binding?.cardViewAnswer?.setVisibleState(false)
        binding?.btnSubmitQuestion?.setVisibleState(false)
        binding?.btnNextQuestion?.setVisibleState(false)
        binding?.contentLayout?.visibility = View.VISIBLE
        binding?.resultLayout?.visibility = View.GONE
        data?.questions = endScreenData.questions
        data?.sessionId = endScreenData.sessionId
        questionNumber = 0
        currentQuestion = endScreenData.questions?.getOrNull(questionNumber)
        currentQuestionType = currentQuestion?.questionType.orEmpty()
        setUpUI()
    }

    private fun onSuccess() {
        FileUtils.deleteCacheDir(context, "tempUploadCache")
        val hashmap = hashMapOf<String, Any>()
        hashmap["session_id"] = data?.sessionId.orEmpty()
        hashmap["audioResponse"] = currentQuestion?.audioUploadUrl.orEmpty()
        checkAnswers(hashmap)
    }

    private fun checkAnswers(hashmap: HashMap<String, Any>) {
        (context as? AppCompatActivity)?.lifecycleScope?.launch {
            practiceEnglishRepository.checkAnswer(
                currentQuestion?.questionId.orEmpty(),
                hashmap.toRequestBody(),
                source = HOME
            ).map {
                it.data
            }.catch { e ->
                showToast(context, e.toString())
                binding?.progressBar?.hide()
            }.collect {
                binding?.progressBar?.hide()
                onAnswerSuccess(it)
            }
        }
    }

    private fun onError(it: Throwable) {
        Toast.makeText(context, it.message.toString(), Toast.LENGTH_LONG).show()
    }

    private fun onAnswerSuccess(answerData: AnswerData) {
        isAnswerShown = true
        currentQuestion?.audioUploadUrl = answerData.tryAgainUploadUrl.orEmpty()
        binding?.apply {
            tvPercentage.text = answerData.matchPercent.toString()
            tvPercentage.setTextColor(Color.parseColor(answerData.percentageTextColor))
            tvCorrectText.text = answerData.correctText
            tvCorrectText.setTextColor(Color.parseColor(answerData.percentageTextColor))
            tvYourAnswerText.text = answerData.yourAnswerText
            tvYourAnswerText.setOnClickListener { _ ->
                setupMediaPlayer(answerData.userAudioUrl)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.QUESTION_ID to currentQuestion?.questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.RESPONSE,
                            EventConstants.SOURCE to currentQuestionType,
                            EventConstants.PAGE to PracticeEnglishWidget.HOME
                        ),
                        ignoreMoengage = false
                    )
                )
            }
            ivYourAnswerSpeaker.setOnClickListener { _ ->
                setupMediaPlayer(answerData.userAudioUrl)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to currentQuestion?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to currentQuestion?.questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.RESPONSE,
                            EventConstants.SOURCE to currentQuestionType,
                            EventConstants.PAGE to PracticeEnglishWidget.HOME
                        ),
                        ignoreMoengage = false
                    )
                )
            }
            TextViewUtils.setTextFromHtml(tvYourAnswer, answerData.userTextDisplay.orEmpty())
            tvCorrectAnswerText.text = answerData.correctAnswerText
            ivCorrectAnswerSpeaker.setOnClickListener { _ ->
                setupMediaPlayer(answerData.answerAudioUrl)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to currentQuestion?.question_audio.orEmpty(),
                            EventConstants.QUESTION_ID to currentQuestion?.questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.CORRECT_ANSWER,
                            EventConstants.SOURCE to currentQuestionType,
                            EventConstants.PAGE to PracticeEnglishWidget.HOME
                        ),
                        ignoreMoengage = false
                    )
                )
            }
            tvCorrectAnswerText.setOnClickListener { _ ->
                setupMediaPlayer(answerData.answerAudioUrl)
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.PE_AUDIO_PLAY,
                        hashMapOf(
                            EventConstants.AUDIO_ID to answerData?.answerAudioUrl.orEmpty(),
                            EventConstants.QUESTION_ID to currentQuestion?.questionId.orEmpty(),
                            EventConstants.TYPE to EventConstants.CORRECT_ANSWER,
                            EventConstants.SOURCE to currentQuestionType,
                            EventConstants.PAGE to PracticeEnglishWidget.HOME
                        ),
                        ignoreMoengage = false
                    )
                )
            }
            TextViewUtils.setTextFromHtml(tvCorrectAnswer, answerData.correctTextDisplay.orEmpty())
            btnTryAgain.text = answerData.tryAgainButtonText
            btnNextQuestion.text = answerData.nextButtonText
        }
        showAnswerScreen()
    }

    private fun showAnswerScreen() {
        when (currentQuestionType) {
            QuestionType.SINGLE_BLANK_QUESTION.toString() -> {
                binding?.cardViewQuestion?.setVisibleState(true)
                binding?.cardViewSingleBlankAnswer?.setVisibleState(false)
                binding?.layoutAnswerHint?.setVisibleState(false)
                binding?.tvAnswerHintText?.setVisibleState(false)
                binding?.layoutAnswerSpeaker?.setVisibleState(false)
            }
            QuestionType.MULTI_BLANK_QUESTION.toString() -> {
                binding?.cardViewQuestion?.setVisibleState(true)
                binding?.cardViewSingleBlankAnswer?.setVisibleState(false)
                binding?.layoutAnswerHint?.setVisibleState(false)
                binding?.tvAnswerHintText?.setVisibleState(false)
                binding?.layoutAnswerSpeaker?.setVisibleState(false)
                binding?.refreshMultiBlank?.setVisibleState(false)
            }
            QuestionType.TEXT_QUESTION.toString() -> {
                binding?.cardViewWriterAnswer?.setVisibleState(false)
            }
            QuestionType.AUDIO_QUESTION.toString() -> {
                binding?.recordButton?.visibility = LinearLayout.INVISIBLE
                binding?.btnTryAgain?.setVisibleState(true)
            }
        }
        binding?.cardViewAnswer?.setVisibleState(true)
        binding?.btnNextQuestion?.setVisibleState(true)
        binding?.btnSubmitQuestion?.setVisibleState(false)
        binding?.tvHintText?.setVisibleState(false)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setUpVoiceNoteRecording() {

        //IMPORTANT
        binding?.recordButton?.animation = null
        var actionDownMillis: Long = 0L
        binding?.recordButton?.setOnTouchListener { view, motionEvent ->
            if (!isPermissionGranted) {
                actionPerformer?.performAction(AskPermission())
            } else {
                when (motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {
                        actionDownMillis = System.currentTimeMillis()
                        binding?.tvHintText?.text =
                            data?.recordPressedText.orDefaultValue("Recording...")
                        onRecord(true)
                        ScaleAnim(binding?.recordButton!!).start()
                    }
                    MotionEvent.ACTION_UP -> {
                        onRecordButtonReleased(actionDownMillis)
                        ScaleAnim(binding?.recordButton!!).stop()
                        binding?.tvHintText?.text =
                            data?.hintText.orDefaultValue("Press and Hold for Recording")
                    }
                    MotionEvent.ACTION_CANCEL -> {
                        onRecordButtonReleased(actionDownMillis)
                        ScaleAnim(binding?.recordButton!!).stop()
                        binding?.tvHintText?.text =
                            data?.hintText.orDefaultValue("Press and Hold for Recording")
                    }
                }
            }
            true
        }

        audioFileName = "${context.externalCacheDir?.absolutePath}/learn_english.3gp"
    }

    private fun onRecordButtonReleased(actionDownMillis: Long) {
        if (System.currentTimeMillis() - actionDownMillis > 1000) {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.PE_AUDIO_RECORD,
                    hashMapOf(
                        EventConstants.AUDIO_ID to currentQuestion?.question_audio.orEmpty(),
                        EventConstants.QUESTION_ID to currentQuestion?.questionId.orEmpty(),
                        EventConstants.TYPE to EventConstants.QUESTION,
                        EventConstants.SOURCE to currentQuestionType,
                        EventConstants.PAGE to HOME
                    ),
                    ignoreMoengage = false
                )
            )
            binding?.btnSubmitQuestion?.setVisibleState(true)
        } else {
            binding?.btnSubmitQuestion?.setVisibleState(false)
            showToast(context, data?.toastText.orDefaultValue("Press and Hold for Recording"))
        }
        onRecord(false)
        ScaleAnim(binding?.recordButton!!).stop()
    }

    private fun onRecord(start: Boolean) {
        if (start) {
            startRecording()
        } else {
            stopRecording()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        releaseMediaPlayer()
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun startRecording() {
        try {
            recorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setOutputFile(audioFileName)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                prepare()
                start()
            }
        } catch (e: IOException) {
            android.util.Log.e(AudioQuestionFragment.TAG, "prepare() failed")
            FirebaseCrashlytics.getInstance().recordException(e)
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

    fun onPermissionFlowCompleted(isGranted: Boolean) {
        when {
            isGranted -> if (isGranted) {
                isPermissionGranted = true
            } else {
                showToast(
                    context,
                    (context as? AppCompatActivity)?.getString(R.string.needs_record_audio_permissions)
                        .orEmpty()
                )
            }
        }
    }

    private fun getCombinedAnswerList(): List<String> {
        val answer = mutableListOf<String>()
        answerTextViewList.forEach { answer.add(it.text.toString()) }
        return answer
    }

    class PracticeEnglishWidgetHolder(
        binding: WidgetPracticeEnglishBinding,
        widget: BaseWidget<*, *>
    ) :
        WidgetBindingVH<WidgetPracticeEnglishBinding>(binding, widget)

    class PracticeEnglishWidgetModel : WidgetEntityModel<PracticeEnglishWidgetData, WidgetAction>()

    @Keep
    data class PracticeEnglishWidgetData(
        @SerializedName("questions") var questions: List<PracticeEnglishWidgetItems>?,
        @SerializedName("submit_button_text") val submitText: String?,
        @SerializedName("title") val title: String?,
        @SerializedName("session_id") var sessionId: String?,
        @SerializedName("practice_english_banner_text") val practiceEnglishTitle: String?,
        @SerializedName("practice_english_cta") val practiceEnglishCta: String?,
        @SerializedName("deeplink") val deeplink: String?,
        @SerializedName("bg_image_url") val bgImageUrl: String?,
        @SerializedName("share_text") val shareText: String?,
        @SerializedName("share_img_url") val shareImageUrl: String?,
        @SerializedName("share_deeplink") val shareDeeplink: String?,
        @SerializedName("display_type") val questionTypeList: List<DisplayType>?,
        @SerializedName("hint_text") val hintText: String?,
        @SerializedName("record_pressed_text") val recordPressedText: String?,
        @SerializedName("record_toast_text") val toastText: String?,
    ) : WidgetData()

    @Keep
    @Parcelize
    data class PracticeEnglishWidgetItems(
        @SerializedName("title") val title: String?,
        @SerializedName("question_audio") val question_audio: String?,
        @SerializedName("question") val question: String?,
        @SerializedName("blank_question") val blankQuestion: List<String>?,
        @SerializedName("question_id") val questionId: String?,
        @SerializedName("answer_audio_upload_url") var audioUploadUrl: String?,
        @SerializedName("write_ans_hint") val write_ans_hint: String?,
        @SerializedName("show_mic") val show_mic: Boolean?,
        @SerializedName("submit_button_text") val submit_text: String?,
        @SerializedName("language") val language: String?,
        @SerializedName("display_type") val questionType: String?,
        @SerializedName("otherOptions") val otherOptions: List<String>?,
        @SerializedName("otherOptionText") val otherOptionText: String?,
        @SerializedName("answer_text") val answer_text: String?,
        @SerializedName("answer_audio") val answer_audio: String?,
        @SerializedName("error_toast_text") val errorToastText: String?,
    ) : WidgetData(), Parcelable

    @Keep
    data class DisplayType(
        @SerializedName("display_type") val displayType: String?
    )
}