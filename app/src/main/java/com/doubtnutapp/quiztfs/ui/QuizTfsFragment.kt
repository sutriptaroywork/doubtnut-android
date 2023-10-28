package com.doubtnutapp.quiztfs.ui

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieDrawable
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.base.QuizTFSMultiOptionSelected
import com.doubtnutapp.base.QuizTFSSingleOptionSelected
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.remote.models.quiztfs.QuizTfsData
import com.doubtnutapp.data.remote.models.quiztfs.QuizTfsSubmitResponse
import com.doubtnutapp.databinding.FragmentQuizTfsBinding
import com.doubtnutapp.quiztfs.viewmodel.QuizTfsFragmentViewModel
import com.doubtnutapp.setVisibleState
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableObserver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class QuizTfsFragment : BaseBindingFragment<QuizTfsFragmentViewModel, FragmentQuizTfsBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "QuizTfsFragment"
        const val QUIZ_DATA = "quiz_data"
        private const val TIME_TO_NEXT = "time_to_next"
        private const val WAIT_FOR_NEXT = "wait_for_next"
        fun newInstance(
            quizTfsData: QuizTfsData,
            timeToNext: Long,
            waitForNext: Long
        ): QuizTfsFragment {
            return QuizTfsFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(QUIZ_DATA, quizTfsData)
                    putLong(TIME_TO_NEXT, timeToNext)
                    putLong(WAIT_FOR_NEXT, waitForNext)
                }
            }
        }
    }

    private val quizTfsData: QuizTfsData
        get() = arguments?.getParcelable(QUIZ_DATA)!!

    private val timeToNext: Long
        get() = arguments?.getLong(TIME_TO_NEXT)!!

    private val waitForNext: Long
        get() = arguments?.getLong(WAIT_FOR_NEXT)!!

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    @Inject
    lateinit var networkUtil: NetworkUtil

    private var shouldShowAnswer = false
    private var isAnswerSynced = false

    private var quizTfsSubmitResponse: QuizTfsSubmitResponse? = null
    private var submittedAnswer: List<String>? = null

    private var listener: QuizTfsListener? = null

    private val adapter: QuizQnaOptionAdapter by lazy {
        QuizQnaOptionAdapter(
            this,
            quizTfsData.type
        )
    }


    @Inject
    lateinit var disposable: CompositeDisposable

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentQuizTfsBinding =
        FragmentQuizTfsBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): QuizTfsFragmentViewModel =
        viewModelProvider(viewModelFactory)

    override fun performAction(action: Any) {
        if (action is QuizTFSSingleOptionSelected) {
            if (isAnswerSynced || shouldShowAnswer)
                return
            adapter.questionOptions.mapIndexed { index, option ->
                if (index == action.position) {
                    option.status = QuizTfsOption.STATUS_SELECTED
                } else {
                    option.status = QuizTfsOption.STATUS_UNSELECTED
                }
            }
            adapter.notifyDataSetChanged()
        } else if (action is QuizTFSMultiOptionSelected) {
            if (isAnswerSynced || shouldShowAnswer)
                return
            adapter.questionOptions.mapIndexed { index, option ->
                if (index == action.position) {
                    if (option.status == QuizTfsOption.STATUS_SELECTED) {
                        option.status = QuizTfsOption.STATUS_UNSELECTED
                    } else {
                        option.status = QuizTfsOption.STATUS_SELECTED
                    }
                }
            }
            adapter.notifyDataSetChanged()
        }
    }

    private fun getStreakAnimationAssetName(color: String?) =
        when (color) {
            "green" -> {
                "lottie_tfs_streak_green.zip"
            }
            "orange" -> {
                "lottie_tfs_streak_orange.zip"
            }
            "pink" -> {
                "lottie_tfs_streak_pink.zip"
            }
            else -> {
                "lottie_tfs_streak_yellow.zip"
            }
        }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.QUIZ_TFS_QUESTION_SHOWN,
                hashMapOf(
                    EventConstants.QUESTION_ID to quizTfsData.questionDetails.questionID
                ), ignoreSnowplow = true
            )
        )

        binding.mathViewQuestion.apply {
            setFontSize(16)
            setTextColor("black")
            text = quizTfsData.questionDetails.questionText
        }

        binding.tvPoints.text = quizTfsData.point
        binding.tvQuestionType.text = quizTfsData.questionTypeTitle

        if (quizTfsData.streakText.isNullOrBlank()) {
            binding.layoutStreak.isVisible = false
        } else {
            binding.layoutStreak.isVisible = true
            binding.layoutStreak.background = Utils.getShape(
                quizTfsData.progressColor,
                quizTfsData.progressColor,
                4f
            )
            binding.tvStreak.text = quizTfsData.streakText.orEmpty() + " "
            binding.streakAnimation.setAnimation(getStreakAnimationAssetName(quizTfsData.streakColor))
            binding.streakAnimation.repeatCount = LottieDrawable.INFINITE
            binding.streakAnimation.playAnimation()
        }

        lifecycleScope.launch {
            delay(1600)
            binding.progressLayout.isVisible = false
            binding.progressBarCenter.isVisible = false
        }

        binding.rvOptions.adapter = adapter
        val options = mutableListOf<QuizTfsOption>()
        options.add(
            QuizTfsOption(
                quizTfsData.questionDetails.optionA,
                QuizTfsOption.STATUS_UNSELECTED,
                "A"
            )
        )
        options.add(
            QuizTfsOption(
                quizTfsData.questionDetails.optionB,
                QuizTfsOption.STATUS_UNSELECTED,
                "B"
            )
        )
        options.add(
            QuizTfsOption(
                quizTfsData.questionDetails.optionC,
                QuizTfsOption.STATUS_UNSELECTED,
                "C"
            )
        )
        options.add(
            QuizTfsOption(
                quizTfsData.questionDetails.optionD,
                QuizTfsOption.STATUS_UNSELECTED,
                "D"
            )
        )
        adapter.updateOptions(options)
        setProgressColor(quizTfsData.progressColor, quizTfsData.progressBgColor)
        handleTimer(TimeUnit.SECONDS.toMillis(timeToNext), TimeUnit.SECONDS.toMillis(waitForNext))

        binding.tvSubmit.setOnClickListener(object : DebouncedOnClickListener(1000) {
            override fun onDebouncedClick(v: View?) {
                if (networkUtil.isConnectedWithMessage()) {
                    submitAnswer(true)
                }
            }
        })
    }

    private fun setProgressColor(progressColor: String, backgroundColor: String) {
        val progressBarDrawable: LayerDrawable =
            binding.progressBar.progressDrawable as LayerDrawable
        val backgroundDrawable: Drawable = progressBarDrawable.getDrawable(0)
        val progressDrawable: Drawable = progressBarDrawable.getDrawable(1)

        backgroundDrawable.setColorFilter(
            Utils.parseColor(backgroundColor, R.color.red_ffadad),
            PorterDuff.Mode.SRC_IN
        )
        progressDrawable.setColorFilter(
            Utils.parseColor(progressColor, R.color.red_e34c4c),
            PorterDuff.Mode.SRC_IN
        )
    }

    private fun handleTimer(count: Long, next: Long?) {
        val progressMax = count.toInt()
        binding.progressBar.max = progressMax
        disposable.add(
            getTimerObservable(count)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableObserver<Long>() {
                    override fun onComplete() {
                        disposable.clear()
                        shouldShowAnswer = true
                        binding.tvSubmit.isVisible = false
                        if (next != null) {
                            if (quizTfsSubmitResponse != null) {
                                updateAnswerAndPoint()
                            } else {
                                submitAnswer(false)
                            }
                            handleTimer(next, null)
                        }
                    }

                    override fun onNext(t: Long) {
                        val timerLeft = (count - t)
                        binding.progressBar.progress = timerLeft.toInt()
                        var timeLeftText =
                            TimeUnit.MILLISECONDS.toSeconds(timerLeft).toString() + "s"
                        if (next == null) {
                            timeLeftText = "Next Question in $timeLeftText"
                        }
                        binding.tvTimeRemaining.text = timeLeftText
                    }

                    override fun onError(e: Throwable) {
                        disposable.clear()
                    }
                })
        )
    }

    private fun getTimerObservable(count: Long) =
        Observable.interval(0, 1, TimeUnit.MILLISECONDS).take(count)

    private fun submitAnswer(isUserOpted: Boolean) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.QUIZ_TFS_ANSWER_SUBMITTED,
                hashMapOf(
                    EventConstants.QUESTION_ID to quizTfsData.questionDetails.questionID,
                    EventConstants.IS_USER_CLICK to isUserOpted
                ), ignoreSnowplow = true
            )
        )
        val answer = mutableListOf<String>()
        adapter.questionOptions.forEach {
            if (it.status == QuizTfsOption.STATUS_SELECTED) {
                answer.add(it.key)
            }
        }
        if (isUserOpted && answer.isEmpty()) {
            toast("Select option to continue")
            return
        }
        viewModel.submitQuiz(
            hashMapOf(
                "studentClass" to quizTfsData.studentClass,
                "language" to quizTfsData.language,
                "subject" to quizTfsData.subject,
                "sessionId" to "",
                "sessionType" to "session",
                "testQuestionId" to quizTfsData.questionDetails.questionID,
            ), answer
        )
    }

    override fun setupObservers() {
        super.setupObservers()
        viewModel.quizSubmitLiveData.observeK(
            viewLifecycleOwner,
            this::onSubmitQuizResponse,
            this::onApiError,
            this::unAuthorizeUserError,
            this::ioExceptionHandler,
            this::updateProgressBarState
        )

        viewModel.messageLiveData.observe(viewLifecycleOwner, EventObserver {
            toast(it)
        })

    }

    private fun onSubmitQuizResponse(response: Pair<QuizTfsSubmitResponse, List<String>>) {
        isAnswerSynced = true
        toast("Submitted Successfully")
        binding.tvSubmit.isVisible = false
        quizTfsSubmitResponse = response.first
        submittedAnswer = response.second
        if (shouldShowAnswer) {
            updateAnswerAndPoint()
        } else {
            listener?.updateRedDotVisibility(true)
        }
    }

    private fun updateAnswerAndPoint() {
        val response = quizTfsSubmitResponse ?: return
        showAnswer(response.correctAnswers.orEmpty())
        binding.tvPoints.text = response.totalPoints
    }

    private fun showAnswer(correctAnswer: List<String>) {
        adapter.questionOptions.mapIndexed { index, quizTfsOption ->
            if (submittedAnswer.orEmpty().contains(quizTfsOption.key)) {
                if (correctAnswer.contains(quizTfsOption.key)) {
                    quizTfsOption.status = QuizTfsOption.STATUS_CORRECT
                } else {
                    quizTfsOption.status = QuizTfsOption.STATUS_INCORRECT
                }
            } else {
                if (correctAnswer.contains(quizTfsOption.key)) {
                    quizTfsOption.status = QuizTfsOption.STATUS_CORRECT_UNSELECTED
                } else {
                    quizTfsOption.status = QuizTfsOption.STATUS_UNSELECTED
                }
            }
        }
        adapter.notifyDataSetChanged()
        listener?.showChat()
    }

    private fun updateProgressBarState(state: Boolean) {
        binding.progressBarSubmit.setVisibleState(state)
    }

    private fun unAuthorizeUserError() {
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(parentFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        apiErrorToast(e)
    }

    private fun ioExceptionHandler() {
        context?.let {
            if (NetworkUtils.isConnected(it).not()) {
                toast(it.getString(R.string.string_noInternetConnection))
            } else {
                toast(it.getString(R.string.somethingWentWrong))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    fun setListener(quizTfsListener: QuizTfsListener) {
        listener = quizTfsListener
    }

}
