package com.doubtnutapp.liveclass.ui.practice_english

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.base.ActionPerformer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent

import com.doubtnutapp.base.NextQuestionClicked
import com.doubtnutapp.base.TryAgainClicked
import com.doubtnutapp.databinding.ActivityPracticeEnglishBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.utils.UserUtil
import javax.inject.Inject

/**
 * Created by Akshat Jindal on 03/12/21.
 */
class PracticeEnglishActivity :
    BaseBindingActivity<PracticeEnglishViewModel, ActivityPracticeEnglishBinding>(),
    ActionPerformer {

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var counter = 0
    private val questionIdList: MutableList<Question> = mutableListOf()

    companion object {
        private const val TAG = "PracticeEnglishActivity"
        fun getStartIntent(context: Context, sessionId: String? = null) =
            Intent(context, PracticeEnglishActivity::class.java).apply {
                putExtra(Constants.SESSION_ID, sessionId)
            }
    }

    override fun provideViewBinding(): ActivityPracticeEnglishBinding =
        ActivityPracticeEnglishBinding.inflate(layoutInflater)

    override fun providePageName(): String = TAG
    override fun provideViewModel(): PracticeEnglishViewModel = viewModelProvider(viewModelFactory)

    override fun setupView(savedInstanceState: Bundle?) {

        //for deeplink handling
        val sessionId = intent.getStringExtra(Constants.SESSION_ID).orEmpty()
        viewModel.getInitData(sessionId)
    }

    override fun setupObservers() {
        viewModel.questionIdsListLiveData.observe(this, {
            questionIdList.clear()
            questionIdList.addAll(it.question.orEmpty())
            initUI(it.title.orEmpty())
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.PE_PAGE_VIEW,
                    hashMapOf(
                        EventConstants.TYPE to EventConstants.SESSION_START,
                    )
                )
            )
        })

        viewModel.questionLiveData.observe(this, {
            val fragment = when (it) {
                is AudioQuestionData -> {
                    AudioQuestionFragment.newInstance(
                        questionId = questionIdList[counter].questionId.orEmpty(),
                        data = it
                    ).also { frag ->
                        frag.setUpActionPerformer(this)
                    }
                }
                is TextQuestionData -> {
                    TextQuestionFragment.newInstance(
                        questionId = questionIdList[counter].questionId.orEmpty(),
                        data = it
                    ).also { frag ->
                        frag.setUpActionPerformer(this)
                    }
                }
                is SingleBlankQuestionData -> {
                    SingleBlankQuestionFragment.newInstance(
                        questionId = questionIdList[counter].questionId.orEmpty(),
                        data = it
                    ).also { frag ->
                        frag.setUpActionPerformer(this)
                    }
                }
                is MultiBlankQuestionData -> {
                    MultiBlankQuestionFragment.newInstance(
                        questionId = questionIdList[counter].questionId.orEmpty(),
                        data = it
                    ).also { frag ->
                        frag.setUpActionPerformer(this)
                    }
                }
                is SingleChoiceQuestionData -> {
                    SingleChoiceQuestionFragment.newInstance(
                        questionId = questionIdList[counter].questionId.orEmpty(),
                        data = it
                    ).also { frag ->
                        frag.setUpActionPerformer(this)
                    }
                }
                is ImageQuestionData -> {
                    ImageQuestionFragment.newInstance(
                        questionId = questionIdList[counter].questionId.orEmpty(),
                        data = it
                    ).also { frag ->
                        frag.setUpActionPerformer(this)
                    }
                }
                else -> Fragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.practiceEnglishContainer, fragment)
                .commit()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.PE_PAGE_VIEW,
                    hashMapOf(
                        EventConstants.QUESTION to questionIdList[counter].questionId.orEmpty(),
                        EventConstants.TYPE to EventConstants.QUESTION,
                        EventConstants.SOURCE to questionIdList[counter].question_type.orEmpty()
                    )
                )
            )
        })

        viewModel.endScreenData.observe(this, {
            val fragment = EndSessionFragment.newInstance(it)
            supportFragmentManager.beginTransaction()
                .replace(R.id.practiceEnglishContainer, fragment)
                .commit()
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.PE_PAGE_VIEW,
                    hashMapOf(
                        EventConstants.TYPE to EventConstants.SESSION_END
                    )
                )
            )
        })
    }

    private fun initUI(title: String) {
        binding.backButton.setOnClickListener { onBackPressed() }
        binding.tvToolbarTitle.text = title

        if (questionIdList.size == 0) {
            binding.tvQuestionNumber.text = ""
            showEndScreen()
        } else {
            binding.tvQuestionNumber.text = "${counter + 1} / ${questionIdList.size}"
            fetchQuestionData(questionIdList[counter])
        }
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                name = EventConstants.PRACTICE_ENGLISH_PAGE_VIEW,
                params = hashMapOf(
                    EventConstants.SOURCE to EventConstants.PROFILE,
                    EventConstants.STUDENT_ID to UserUtil.getStudentId(),
                    EventConstants.STUDENT_CLASS to UserUtil.getStudentClass(),
                    EventConstants.BOARD to UserUtil.getUserBoard(),
                ),
                ignoreFacebook = true
            )
        )
    }

    private fun fetchQuestionData(question: Question) {
        viewModel.getQuestionData(
            question.questionId.orEmpty(),
            QuestionType.getQuestionType(question.question_type.orEmpty())
        )
    }

    private fun showEndScreen() {
        viewModel.getEndScreenData()
    }

    override fun performAction(action: Any) {
        when (action) {
            is TryAgainClicked -> {
                fetchQuestionData(questionIdList[counter])
            }
            is NextQuestionClicked -> {
                if (counter == questionIdList.size - 1)
                    showEndScreen()
                else {
                    incrementQuestionNumber()
                    fetchQuestionData(questionIdList[counter])
                }
            }
            else -> {
            }
        }
    }

    private fun incrementQuestionNumber() {
        counter += 1
        binding.tvQuestionNumber.text = "${counter + 1} / ${questionIdList.size}"
    }

    override fun onBackPressed() {

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Confirmation")
            .setMessage("Are you sure you want to end this session?")
            .setPositiveButton("Yes, End Session") { _, _ ->
                finish()
            }
            .setNegativeButton("No") { _, _ -> }
        val dialog = builder.create()
        dialog.show()
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).isAllCaps = false
        dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE).isAllCaps = false
    }

}