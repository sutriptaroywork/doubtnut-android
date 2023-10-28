package com.doubtnutapp.ui.test

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.QuestionwiseResult
import com.doubtnutapp.data.remote.models.TestQuestionDataOptions
import com.doubtnutapp.databinding.FragmentTestQuestionItemBinding
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.instacart.library.truetime.TrueTimeRx
import dagger.android.support.AndroidSupportInjection
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by akshaynandwana on
 * 18, December, 2018
 **/
class TestQuestionFragment : BaseBindingFragment<TestViewModel, FragmentTestQuestionItemBinding>() {

    var testQuestionData: TestQuestionDataOptions? = null
    private var testReportList: QuestionwiseResult? = null
    private var listener: OnFragmentInteractionListener? = null

    var position: Int = 0
    private var flagReport: Boolean = false
    private var flagOption1: Boolean = false
    private var flagOption2: Boolean = false
    private var flagOption3: Boolean = false
    private var flagOption4: Boolean = false
    private var flag: String = Constants.TEST_OVER
    private var testSize: Int = 0
    var testSubscriptionId: Int = 0
    var checkedOptionList: ArrayList<String> = ArrayList()
    var oldCheckedOptionList: ArrayList<String> = ArrayList()
    var selectedOptions: String = ""

    companion object {
        const val TAG = "TestQuestionFragment"
        fun newInstance(
            testQuestionData: TestQuestionDataOptions,
            position: Int,
            numberOfQuestions: Int,
            testSubscriptionId: Int,
            flag: String?,
            testReportListFlag: Boolean
        ): TestQuestionFragment {
            val fragment = TestQuestionFragment()
            val args = Bundle()
            args.putParcelable(Constants.TEST_QUESTION_LIST, testQuestionData)
            args.putInt(Constants.TEST_QUESTION_INDEX, position)
            args.putBoolean(Constants.TEST_REPORT_LIST_FLAG, testReportListFlag)
            args.putInt(Constants.TEST_SIZE, numberOfQuestions)
            args.putInt(Constants.TEST_SUBSCRIPTION_ID, testSubscriptionId)
            args.putString(Constants.TEST_TRUE_TIME_FLAG, flag)
            fragment.arguments = args
            return fragment
        }

        fun newInstanceReport(
            questionwiseResult: QuestionwiseResult,
            position: Int,
            numberOfQuestions: Int,
            testSubscriptionId: Int,
            flag: String?,
            testReportListFlag: Boolean
        ): TestQuestionFragment {
            val fragment = TestQuestionFragment()
            val args = Bundle()
            args.putParcelable(Constants.TEST_REPORT_LIST, questionwiseResult)
            args.putInt(Constants.TEST_QUESTION_INDEX, position)
            args.putBoolean(Constants.TEST_REPORT_LIST_FLAG, testReportListFlag)
            args.putInt(Constants.TEST_SIZE, numberOfQuestions)
            args.putInt(Constants.TEST_SUBSCRIPTION_ID, testSubscriptionId)
            args.putString(Constants.TEST_TRUE_TIME_FLAG, flag)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("flagOption1", flagOption1)
        outState.putBoolean("flagOption2", flagOption2)
        outState.putBoolean("flagOption3", flagOption3)
        outState.putBoolean("flagOption4", flagOption4)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initMathView()
        setUpDataFromIntent()
        setQuestionToMathView()
        setOptionsToMathView()
        setQuestionSingleOrMcq()
        setButtonText()
        setQuestionImage()
        getFlagOptionFromSavedState(savedInstanceState)
        setOptionSelected()
        setIsTestReport()
    }

    private fun getFlagOptionFromSavedState(savedInstanceState: Bundle?) {
        flagOption1 = savedInstanceState?.getBoolean("flagOption1") ?: false
        flagOption2 = savedInstanceState?.getBoolean("flagOption2") ?: false
        flagOption3 = savedInstanceState?.getBoolean("flagOption3") ?: false
        flagOption4 = savedInstanceState?.getBoolean("flagOption4") ?: false
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun initMathView() {
        mBinding?.mathView?.javaInterfaceForTest = this
    }

    private fun setUpDataFromIntent() {
        arguments?.let {
            flagReport = requireArguments().getBoolean(Constants.TEST_REPORT_LIST_FLAG)
            position = requireArguments().getInt(Constants.TEST_QUESTION_INDEX)
            testSize = requireArguments().getInt(Constants.TEST_SIZE)
            testSubscriptionId = requireArguments().getInt(Constants.TEST_SUBSCRIPTION_ID)
            flag = requireArguments().getString(Constants.TEST_TRUE_TIME_FLAG).orDefaultValue()
            when {
                flagReport -> testReportList =
                    requireArguments().getParcelable(Constants.TEST_REPORT_LIST)
                else -> testQuestionData =
                    requireArguments().getParcelable(Constants.TEST_QUESTION_LIST)
            }
        }
    }

    private fun setQuestionToMathView() {
        val questionNumber = position + 1
        mBinding?.mathView?.questionNumber =
            getString(R.string.string_question_quiz_number, questionNumber)
        when {
            flagReport ->   mBinding?.mathView?.question = testReportList!!.text
            else ->   mBinding?.mathView?.question = testQuestionData!!.text
        }
    }

    private fun setOptionsToMathView() {
        when {
            flagReport -> {
                mBinding?.mathView?.option1 = testReportList!!.options[0].title
                mBinding?.mathView?.option2 = testReportList!!.options[1].title
                mBinding?.mathView?.option3 = testReportList!!.options[2].title
                mBinding?.mathView?.option4 = testReportList!!.options[3].title
            }
            else -> {
                mBinding?.mathView?.option1 = testQuestionData!!.options[0].title
                mBinding?.mathView?.option2 = testQuestionData!!.options[1].title
                mBinding?.mathView?.option3 = testQuestionData!!.options[2].title
                mBinding?.mathView?.option4 = testQuestionData!!.options[3].title
            }
        }
    }

    private fun setQuestionSingleOrMcq() {
        when {
            flagReport -> when {
                testReportList!!.type.equals(Constants.TEST_SINGLE_ACTION_TYPE_OPTION) ->
                    mBinding?.mathView?.questionType = getString(R.string.string_single_answer_question,
                            testReportList!!.correctReward, testReportList!!.incorrectReward)
                else ->   mBinding?.mathView?.questionType = getString(R.string.string_multiple_answer_question,
                        testReportList!!.correctReward, testReportList!!.incorrectReward)
            }
            else -> when {
                testQuestionData!!.type.equals(Constants.TEST_SINGLE_ACTION_TYPE_OPTION) ->
                    mBinding?.mathView?.questionType = getString(R.string.string_single_answer_question, testQuestionData!!.correctReward, testQuestionData!!.incorrectReward)
                else ->   mBinding?.mathView?.questionType = getString(R.string.string_multiple_answer_question, testQuestionData!!.correctReward, testQuestionData!!.incorrectReward)
            }
        }
    }

    private fun setButtonText() {
        when {
            flagReport -> {
                mBinding?.mathView?.skipChangeName = getString(R.string.string_next_button)
            }
            else -> {
                mBinding?.mathView?.skipChangeName = getString(R.string.string_skip)
            }
        }
        mBinding?.mathView?.submitChangeName = getString(R.string.string_submit_next)
    }

    private fun setQuestionImage() {
        when {
            flagReport -> if (!testReportList!!.imageUrl.isNullOrBlank()) {
                mBinding?.mathView?.setIsImage()
                mBinding?.mathView?.imageLink = testReportList!!.imageUrl
            }
            else -> if (!testQuestionData!!.imageUrl.isNullOrBlank()) {
                mBinding?.mathView?.setIsImage()
                mBinding?.mathView?.imageLink = testQuestionData!!.imageUrl
            }
        }

    }

    private fun setOptionSelected() {
        when {
            flagOption1 ->   mBinding?.mathView?.setOneOptionGreenCorrectActiveQuiz()
        }
        when {
            flagOption2 ->   mBinding?.mathView?.setTwoOptionGreenCorrectActiveQuiz()
        }
        when {
            flagOption3 ->   mBinding?.mathView?.setThreeOptionGreenCorrectActiveQuiz()
        }
        when {
            flagOption4 ->   mBinding?.mathView?.setFourOptionGreenCorrectActiveQuiz()
        }
    }

    private fun setIsTestReport() {
        if (flagReport) {
            mBinding?.mathView?.setIsReport()

            when {
                testReportList!!.isSkipped == 1 -> {
                    mBinding?.mathView?.submitChangeName = getString(R.string.string_count_zero)
                    mBinding?.mathView?.submitChangeColor = getString(R.string.string_small_green_color)
                }
                else -> {
                    when {
                        testReportList!!.isCorrect == 0 -> {
                            mBinding?.mathView?.submitChangeName = testReportList!!.marksScored.toString()
                            mBinding?.mathView?.submitChangeColor = getString(R.string.string_small_red_color)
                        }
                        else -> {
                            mBinding?.mathView?.submitChangeName = testReportList!!.marksScored.toString()
                            mBinding?.mathView?.submitChangeColor = getString(R.string.string_small_green_color)
                        }
                    }
                }
            }
            setOption1BgTick()
            setOption2BgTick()
            setOption3BgTick()
            setOption4BgTick()
        }
    }

    private fun setOption1BgTick() {
        when {
            testReportList!!.options[0].isSelected == 1 && testReportList!!.options[0].isAnswer == "1" -> {
                mBinding?.mathView?.setOption1ChangeColorCorrect()
                if (testReportList!!.isCorrect != 2)
                    mBinding?.mathView?.tickOne = true
            }
        }
        when {
            testReportList!!.options[0].isSelected == 0 && testReportList!!.options[0].isAnswer == "1" -> {
                mBinding?.mathView?.setOption1ChangeColorDefRight()
            }
        }
        when {
            testReportList!!.options[0].isSelected == 1 && testReportList!!.options[0].isAnswer == "0" -> {
                mBinding?.mathView?.setOption1ChangeColor()
                mBinding?.mathView?.wrongOne = true
            }
        }
    }

    private fun setOption2BgTick() {
        when {
            testReportList!!.options[1].isSelected == 1 && testReportList!!.options[1].isAnswer == "1" -> {
                mBinding?.mathView?.setOption2ChangeColorCorrect()
                if (testReportList!!.isCorrect != 2)
                    mBinding?.mathView?.tickTwo = true
            }
        }
        when {
            testReportList!!.options[1].isSelected == 0 && testReportList!!.options[1].isAnswer == "1" -> {
                mBinding?.mathView?.setOption2ChangeColorDefRight()
            }
        }
        when {
            testReportList!!.options[1].isSelected == 1 && testReportList!!.options[1].isAnswer == "0" -> {
                mBinding?.mathView?.setOption2ChangeColor()
                mBinding?.mathView?.wrongTwo = true
            }
        }
    }

    private fun setOption3BgTick() {
        when {
            testReportList!!.options[2].isSelected == 1 && testReportList!!.options[2].isAnswer == "1" -> {
                mBinding?.mathView?.setOption3ChangeColorCorrect()
                if (testReportList!!.isCorrect != 2)
                    mBinding?.mathView?.tickThree = true
            }
        }
        when {
            testReportList!!.options[2].isSelected == 0 && testReportList!!.options[2].isAnswer == "1" -> {
                mBinding?.mathView?.setOption3ChangeColorDefRight()
            }
        }
        when {
            testReportList!!.options[2].isSelected == 1 && testReportList!!.options[2].isAnswer == "0" -> {
                mBinding?.mathView?.setOption3ChangeColor()
                mBinding?.mathView?.wrongThree = true
            }
        }
    }

    private fun setOption4BgTick() {
        when {
            testReportList!!.options[3].isSelected == 1 && testReportList!!.options[3].isAnswer == "1" -> {
                mBinding?.mathView?.setOption4ChangeColorCorrect()
                if (testReportList!!.isCorrect != 2)
                    mBinding?.mathView?.tickFour = true
            }
        }
        when {
            testReportList!!.options[3].isSelected == 0 && testReportList!!.options[3].isAnswer == "1" -> {
                mBinding?.mathView?.setOption4ChangeColorDefRight()
            }
        }
        when {
            testReportList!!.options[3].isSelected == 1 && testReportList!!.options[3].isAnswer == "0" -> {
                mBinding?.mathView?.setOption4ChangeColor()
                mBinding?.mathView?.wrongFour = true
            }
        }
    }

    private fun calculateTimeTaken(): Int {
        val timeTake = when {
            TrueTimeRx.isInitialized() -> TrueTimeRx.now()
            else -> Calendar.getInstance().time
        }
        return (timeTake.time / 1000).toInt()
    }

    private fun getIsEligible(): String {
        return when (flag) {
            Constants.TEST_OVER -> 0.toString()
            else -> listener?.onFragmentInteractionGetIsEligible().toString()
        }
    }

    @JavascriptInterface
    fun skip() {
        val activity = Handler(Looper.getMainLooper())
        activity.post {
            when {
                !flagReport -> skipCall()
                else -> listener?.onFragmentInteractionReport(position + 1)
            }
        }
    }

    @JavascriptInterface
    fun submitQuiz() {
        val activityNew = Handler(Looper.getMainLooper())
        activityNew.post {
            when {
                !flagReport ->
                    when {
                        checkedOptionList.isEmpty() -> context?.let {
                            ToastUtils.makeText(
                                it,
                                getString(R.string.string_quiz_select_answer_else_skip_toast),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> {
                            selectedOptions =
                                (Arrays.toString(checkedOptionList.toArray()).replace("[", "")
                                    .replace("]", "").replace(" ", "")).trim()
                            val timeTaken = calculateTimeTaken()

                            oldCheckedOptionList = checkedOptionList
                            listener?.onFragmentInteractionCallSubmit(
                                testQuestionData!!.testId, "ANS", 0,
                                selectedOptions, testQuestionData!!.sectionCode!!,
                                testSubscriptionId,
                                testQuestionData!!.questionbankId!!.toInt(),
                                testQuestionData!!.type!!, getIsEligible(), timeTaken,
                                testQuestionData!!.subjectCode!!, testQuestionData!!.chapterCode
                                    ?: "",
                                testQuestionData!!.subtopicCode ?: "",
                                testQuestionData!!.classCode ?: "", testQuestionData!!.mcCode
                                    ?: "", position
                            )
                        }
                    }
                else -> listener?.onFragmentInteractionReport(position + 1)
            }
        }
    }

    private fun skipCall() {
        viewModel.getTestResponse(
            requireActivity().applicationContext,
            testQuestionData!!.testId,
            "SKIP",
            0,
            "",
            testQuestionData!!.sectionCode!!,
            testSubscriptionId,
            testQuestionData!!.questionbankId!!.toInt(),
            testQuestionData!!.type!!,
            getIsEligible(),
            calculateTimeTaken(),
            testQuestionData!!.subjectCode!!,
            testQuestionData!!.chapterCode ?: "",
            testQuestionData!!.subtopicCode
                ?: "",
            testQuestionData!!.classCode ?: "",
            testQuestionData!!.mcCode ?: ""
        )
            .observe(this, Observer { response ->
                when (response) {
                    is Outcome.Progress -> {
                        mBinding?.progressBar2?.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        mBinding?.progressBar2?.visibility = View.GONE
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        mBinding?.progressBar2?.visibility = View.GONE
                        toast(getString(R.string.api_error))
                    }
                    is Outcome.BadRequest -> {
                        mBinding?.progressBar2?.visibility = View.GONE
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(requireFragmentManager(), "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        mBinding?.progressBar2?.visibility = View.GONE
                        listener?.onFragmentInteraction(position + 1, true)
                    }
                }
            })
    }

    @JavascriptInterface
    fun optionone() {
        val activity = Handler(Looper.getMainLooper())
        activity.post {

            when {
                !flagReport -> when {
                    !flagOption1 -> {
                        flagOption1 = true
                        checkedOptionList.add(testQuestionData!!.options[0].optionCode.toString())
                        mBinding?.mathView?.setOneOptionGreenCorrectActiveQuiz()
                        when {
                            testQuestionData!!.type == Constants.TEST_SINGLE_ACTION_TYPE_OPTION -> {
                                checkedOptionList.remove(testQuestionData!!.options[1].optionCode.toString())
                                checkedOptionList.remove(testQuestionData!!.options[2].optionCode.toString())
                                checkedOptionList.remove(testQuestionData!!.options[3].optionCode.toString())

                                mBinding?.mathView?.setTwoOptionBlueCorrectActiveQuiz()
                                mBinding?.mathView?.setThreeOptionBlueCorrectActiveQuiz()
                                mBinding?.mathView?.setFourOptionBlueCorrectActiveQuiz()
                            }
                        }
                    }
                    else -> {
                        flagOption1 = false
                        checkedOptionList.remove(testQuestionData!!.options[0].optionCode.toString())
                        mBinding?.mathView?.setOneOptionBlueCorrectActiveQuiz()
                    }
                }
            }
        }
    }

    @JavascriptInterface
    fun optiontwo() {
        val activity = Handler(Looper.getMainLooper())
        activity.post {

            when {
                !flagReport -> when {
                    !flagOption2 -> {
                        flagOption2 = true
                        checkedOptionList.add(testQuestionData!!.options[1].optionCode.toString())
                        mBinding?.mathView?.setTwoOptionGreenCorrectActiveQuiz()
                        when {
                            testQuestionData!!.type == Constants.TEST_SINGLE_ACTION_TYPE_OPTION -> {
                                checkedOptionList.remove(testQuestionData!!.options[0].optionCode.toString())
                                checkedOptionList.remove(testQuestionData!!.options[2].optionCode.toString())
                                checkedOptionList.remove(testQuestionData!!.options[3].optionCode.toString())

                                mBinding?.mathView?.setOneOptionBlueCorrectActiveQuiz()
                                mBinding?.mathView?.setThreeOptionBlueCorrectActiveQuiz()
                                mBinding?.mathView?.setFourOptionBlueCorrectActiveQuiz()
                            }
                        }
                    }
                    else -> {
                        flagOption2 = false
                        checkedOptionList.remove(testQuestionData!!.options[1].optionCode.toString())
                        mBinding?.mathView?.setTwoOptionBlueCorrectActiveQuiz()
                    }
                }
            }
        }
    }

    @JavascriptInterface
    fun optionthree() {
        val activity = Handler(Looper.getMainLooper())
        activity.post {

            when {
                !flagReport -> when {
                    !flagOption3 -> {
                        flagOption3 = true
                        checkedOptionList.add(testQuestionData!!.options[2].optionCode.toString())
                        mBinding?.mathView?.setThreeOptionGreenCorrectActiveQuiz()
                        when {
                            testQuestionData!!.type == Constants.TEST_SINGLE_ACTION_TYPE_OPTION -> {
                                checkedOptionList.remove(testQuestionData!!.options[1].optionCode.toString())
                                checkedOptionList.remove(testQuestionData!!.options[0].optionCode.toString())
                                checkedOptionList.remove(testQuestionData!!.options[3].optionCode.toString())

                                mBinding?.mathView?.setTwoOptionBlueCorrectActiveQuiz()
                                mBinding?.mathView?.setOneOptionBlueCorrectActiveQuiz()
                                mBinding?.mathView?.setFourOptionBlueCorrectActiveQuiz()
                            }
                        }
                    }
                    else -> {
                        flagOption3 = false
                        checkedOptionList.remove(testQuestionData!!.options[2].optionCode.toString())
                        mBinding?.mathView?.setThreeOptionBlueCorrectActiveQuiz()
                    }
                }
            }
        }
    }

    @JavascriptInterface
    fun optionfour() {
        val activity = Handler(Looper.getMainLooper())
        activity.post {

            when {
                !flagReport -> when {
                    !flagOption4 -> {
                        flagOption4 = true
                        checkedOptionList.add(testQuestionData!!.options[3].optionCode.toString())
                        mBinding?.mathView?.setFourOptionGreenCorrectActiveQuiz()
                        when {
                            testQuestionData!!.type == Constants.TEST_SINGLE_ACTION_TYPE_OPTION -> {
                                checkedOptionList.remove(testQuestionData!!.options[1].optionCode.toString())
                                checkedOptionList.remove(testQuestionData!!.options[2].optionCode.toString())
                                checkedOptionList.remove(testQuestionData!!.options[0].optionCode.toString())

                                mBinding?.mathView?.setTwoOptionBlueCorrectActiveQuiz()
                                mBinding?.mathView?.setThreeOptionBlueCorrectActiveQuiz()
                                mBinding?.mathView?.setOneOptionBlueCorrectActiveQuiz()
                            }
                        }
                    }
                    else -> {
                        flagOption4 = false
                        checkedOptionList.remove(testQuestionData!!.options[3].optionCode.toString())
                        mBinding?.mathView?.setFourOptionBlueCorrectActiveQuiz()
                    }
                }
            }
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(position: Int, isSkip: Boolean)
        fun onFragmentInteractionReport(position: Int)
        fun onFragmentInteractionCallSubmit(
            testId: Int, s: String, i1: Int, selectedOptions: String,
            sectionCode: String, testSubscriptionId: Int, toInt: Int,
            type: String, isEligible: String, timeTaken: Int,
            subjectCode: String, s6: String, s7: String, s8: String,
            s9: String, position: Int
        )

        fun onFragmentInteractionGetIsEligible(): Int
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTestQuestionItemBinding {
        return FragmentTestQuestionItemBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): TestViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }
}
