package com.doubtnutapp.quiztfs.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.airbnb.lottie.LottieDrawable
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.remote.models.quiztfs.QuizTfsData
import com.doubtnutapp.databinding.FragmentQuizTfsSolutionBinding
import com.doubtnutapp.dummy.DummyViewModel
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.utils.Utils
import javax.inject.Inject


class QuizTfsSolutionFragment :
    BaseBindingFragment<DummyViewModel, FragmentQuizTfsSolutionBinding>(),
    ActionPerformer {

    companion object {
        const val TAG = "QuizTfsSolutionFragment"
        const val QUIZ_DATA = "quiz_data"
        fun newInstance(
            quizTfsData: QuizTfsData
        ): QuizTfsSolutionFragment {
            return QuizTfsSolutionFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(QUIZ_DATA, quizTfsData)
                }
            }
        }
    }

    private val quizTfsData: QuizTfsData
        get() = arguments?.getParcelable(QUIZ_DATA)!!

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var isSubmitted = false

    private val adapter: QuizQnaOptionAdapter by lazy {
        QuizQnaOptionAdapter(
            this,
            quizTfsData.type
        )
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentQuizTfsSolutionBinding =
        FragmentQuizTfsSolutionBinding.inflate(inflater, container, false)

    override fun providePageName(): String = TAG

    override fun provideViewModel(): DummyViewModel =
        viewModelProvider(viewModelFactory)

    override fun performAction(action: Any) {

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
        binding.mathViewQuestion.apply {
            setFontSize(16)
            setTextColor("black")
            text = quizTfsData.questionDetails.questionText
        }

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

        options.map { quizTfsOption ->
            if (quizTfsData.questionDetails.answerSelected.orEmpty().contains(quizTfsOption.key)) {
                if (quizTfsData.questionDetails.actualAnswer.orEmpty()
                        .contains(quizTfsOption.key)
                ) {
                    quizTfsOption.status = QuizTfsOption.STATUS_CORRECT
                } else {
                    quizTfsOption.status = QuizTfsOption.STATUS_INCORRECT
                }
            } else {
                if (quizTfsData.questionDetails.actualAnswer.orEmpty()
                        .contains(quizTfsOption.key)
                ) {
                    quizTfsOption.status = QuizTfsOption.STATUS_CORRECT_UNSELECTED
                } else {
                    quizTfsOption.status = QuizTfsOption.STATUS_UNSELECTED
                }
            }
        }
        adapter.updateOptions(options)
    }

}
