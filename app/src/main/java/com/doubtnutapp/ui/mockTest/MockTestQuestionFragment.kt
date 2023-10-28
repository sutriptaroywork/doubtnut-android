package com.doubtnutapp.ui.mockTest

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ToastUtils
import com.doubtnut.core.utils.activityViewModelProvider
import com.doubtnut.core.utils.toast
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.MockTestQuestionDataOptions
import com.doubtnutapp.data.remote.models.QuestionwiseResult
import com.doubtnutapp.databinding.FragmentMockTestQuestionItemBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.mathview.MathViewActivity
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.Utils
import com.doubtnutapp.utils.showToast
import com.google.android.exoplayer2.util.Log
import com.instacart.library.truetime.TrueTimeRx
import java.util.*
import javax.inject.Inject


class MockTestQuestionFragment :
    BaseBindingFragment<MockTestListViewModel, FragmentMockTestQuestionItemBinding>() {

    private lateinit var mockTestQuestionViewModel: MockTestQuestionViewModel
    var testQuestionData: MockTestQuestionDataOptions? = null
    private var testReportList: QuestionwiseResult? = null
    private var listener: MockTestQuestionFragment.OnMockTestFragmentInteractionListener? = null

    private var isNextClicked : Boolean = false


    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    var position: Int = 0
    private var flagReport: Boolean = false
    private var inputText: String = ""
    private var checkBoxOptionList: ArrayList<String> = ArrayList()
    private var radioOptionList: ArrayList<String> = ArrayList()
    private var flag: String = Constants.TEST_OVER
    private var testSize: Int = 0
    var testSubscriptionId: Int = 0
    var checkedOptionList: ArrayList<String> = ArrayList()
    var oldCheckedOptionList: ArrayList<String> = ArrayList()
    var selectedOptions: String = ""

    companion object {
        fun newInstance(
            testQuestionData: MockTestQuestionDataOptions,
            position: Int,
            numberOfQuestions: Int,
            testSubscriptionId: Int,
            flag: String?,
            testReportListFlag: Boolean
        ): MockTestQuestionFragment {
            val fragment = MockTestQuestionFragment()
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
        ): MockTestQuestionFragment {
            val fragment = MockTestQuestionFragment()
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
        private const val TAG = "MockTestQuestionFragment"
        private const val IS_REVIEWED = "is_reviewed"
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("inputText", inputText)
        outState.putStringArrayList("checkBoxOptionList", checkBoxOptionList)
        outState.putStringArrayList("radioOptionList", radioOptionList)
    }

    private fun setReviewButton() {
        binding.btnReview.setTextColor(ContextCompat.getColor(requireContext(), R.color.tomato))
        binding.btnReview.background = Utils.getShape("#ffffff", "#ea532c", 4f)
        if (viewModel.reviewQuestionList.contains(position + 1)) {
            binding.btnReview.text = getString(R.string.review_text)
        } else {
            binding.btnReview.text = getString(R.string.mark_review_text)
        }
        binding.btnReview.setOnClickListener {
            submitQuiz(false, true)
            if (viewModel.reviewQuestionList.contains(position + 1)) {
                viewModel.reviewQuestionList.remove(position + 1)
                binding.btnReview.text = getString(R.string.mark_review_text)
                testQuestionData?.isReviewed = false
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.REVIEW_BUTTON_CLICK,
                        hashMapOf(
                            IS_REVIEWED to false,
                            EventConstants.TEST_ID to testSubscriptionId
                        ), ignoreSnowplow = true
                    )
                )
            } else {
                binding.btnReview.text = getString(R.string.review_text)
                viewModel.reviewQuestionList.add(position + 1)
                testQuestionData?.isReviewed = true
                analyticsPublisher.publishEvent(
                    AnalyticsEvent(
                        EventConstants.REVIEW_BUTTON_CLICK,
                        hashMapOf(
                            EventConstants.TEST_ID to testSubscriptionId,
                            IS_REVIEWED to true
                        ), ignoreSnowplow = true
                    )
                )
            }
        }
    }

    private fun setAttemptLimitVisibilty() {
        TooltipCompat.setTooltipText(binding.tvAttempted, "Attempt limit information for this section");
        if (!flagReport && mockTestQuestionViewModel != null && mockTestQuestionViewModel.sectionAttemptLimitMap!!.size > 0
            && mockTestQuestionViewModel.sectionAttemptLimitMap?.get(testQuestionData?.sectionCode!!) != null
        ) {
            binding.tvAttempted.visibility = View.VISIBLE
            binding.tvClear.visibility = View.VISIBLE
            setAttemptedCount()
        } else {
            binding.tvAttempted.visibility = View.GONE
            binding.tvClear.visibility = View.GONE
        }
    }

    private fun initObserver() {
        mockTestQuestionViewModel.answersMapLiveData.observe(viewLifecycleOwner, Observer {
            setAttemptedCount()
        })
    }

    private fun setAttemptedCount() {
        if (testQuestionData != null) {
            val s = SpannableStringBuilder().append("Attempted ")
                .bold {
                    append(
                        "" + mockTestQuestionViewModel.answersMap.filter {
                            it.value.equals(testQuestionData?.sectionCode!!)
                        }.size + " out of " +
                                mockTestQuestionViewModel.sectionAttemptLimitMap?.get(
                                    testQuestionData?.sectionCode!!
                                )
                    )
                }
                .append(" in " + testQuestionData!!.sectionTitle!! + " section")
            binding.tvAttempted.text = s
        } else {
            binding.tvAttempted.visibility = View.GONE
        }
    }

    private fun getFlagOptionFromSavedState(savedInstanceState: Bundle?) {
        inputText = savedInstanceState?.getString("inputText") ?: ""
        checkBoxOptionList = savedInstanceState?.getStringArrayList("checkBoxOptionList")
            ?: ArrayList()
        radioOptionList = savedInstanceState?.getStringArrayList("radioOptionList")
            ?: ArrayList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MockTestQuestionFragment.OnMockTestFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun init() {
        mockTestQuestionViewModel = activity?.run {
            ViewModelProviders.of(this)[MockTestQuestionViewModel::class.java]
        } ?: throw Exception("Invalid Activity")

    }

    private fun initMathView() {
        binding.mockTestMathView.javaInterfaceForMockTest = this
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
        binding.mockTestMathView.questionNumber =
            getString(R.string.string_question_quiz_number, questionNumber)
        if (questionNumber == 1) {
            binding.tvSkip.visibility = View.GONE
        } else {
            binding.tvSkip.show()
        }
        if (flagReport && (!testReportList?.bottomWidgetEntity?.bottomDeeplink.isNullOrEmpty() ||
                        !testReportList?.bottomWidgetEntity?.textSolution.isNullOrEmpty())) {
            showVideoSolution()
        } else {
            binding.tvSolution.hide()
        }
        when {
            flagReport -> {
                binding.mockTestMathView.setIsReport()
                binding.mockTestMathView.question = testReportList!!.text
                binding.mockTestMathView.resultQuestionData = testReportList as QuestionwiseResult
            }
            else -> {
                binding.mockTestMathView.question = testQuestionData!!.text
                binding.mockTestMathView.questionData = testQuestionData as MockTestQuestionDataOptions

            }
        }
    }

    private fun showVideoSolution() {
        binding.tvSolution.show()
        if (!testReportList?.bottomWidgetEntity?.bottomText.isNullOrEmpty()) {
            binding.tvSolution.text = testReportList?.bottomWidgetEntity?.bottomText.orEmpty()
        }
        binding.tvSolution.setOnClickListener {
            if (!testReportList?.bottomWidgetEntity?.bottomDeeplink.isNullOrEmpty()) {
                deeplinkAction.performAction(requireContext(), testReportList?.bottomWidgetEntity?.bottomDeeplink)
            } else if (!testReportList?.bottomWidgetEntity?.textSolution.isNullOrEmpty()) {
                startActivity(MathViewActivity.getStartIntent(requireContext(), testReportList?.bottomWidgetEntity?.textSolution))
            } else {
                showToast(context, getString(R.string.somethingWentWrong))
            }
        }
    }

    private fun setQuestionSingleOrMcq() {
        when {
            flagReport -> when {
                testReportList!!.type.equals(Constants.TEST_SINGLE_ACTION_TYPE_OPTION) ->
                    binding.mockTestMathView.questionType = getString(
                        R.string.string_single_answer_question,
                        testReportList!!.correctReward, testReportList!!.incorrectReward
                    )
                testReportList!!.type.equals(Constants.TEST_MULTIPLE_ACTION_TYPE_OPTION) ->
                    binding.mockTestMathView.questionType = getString(
                        R.string.string_multiple_answer_question,
                        testReportList!!.correctReward, testReportList!!.incorrectReward
                    )
                testReportList!!.type.equals(Constants.TEST_MATRIX_ACTION_TYPE_OPTION) ->
                    binding.mockTestMathView.questionType = getString(
                        R.string.string_matrix_answer_question,
                        testReportList!!.correctReward, testReportList!!.incorrectReward
                    )
                testReportList!!.type.equals(Constants.TEST_TEXT_ACTION_TYPE_OPTION) ->
                    binding.mockTestMathView.questionType = getString(
                        R.string.string_numerical_answer_question,
                        testReportList!!.correctReward, testReportList!!.incorrectReward
                    )

                else -> binding.mockTestMathView.questionType = getString(
                    R.string.string_multiple_answer_question,
                    testReportList!!.correctReward, testReportList!!.incorrectReward
                )
            }
            else -> when {
                testQuestionData!!.type.equals(Constants.TEST_SINGLE_ACTION_TYPE_OPTION) ->
                    binding.mockTestMathView.questionType = getString(
                        R.string.string_single_answer_question,
                        testQuestionData!!.correctReward,
                        testQuestionData!!.incorrectReward
                    )
                testQuestionData!!.type.equals(Constants.TEST_MULTIPLE_ACTION_TYPE_OPTION) ->
                    binding.mockTestMathView.questionType = getString(
                        R.string.string_multiple_answer_question,
                        testQuestionData!!.correctReward,
                        testQuestionData!!.incorrectReward
                    )
                testQuestionData!!.type.equals(Constants.TEST_MATRIX_ACTION_TYPE_OPTION) ->
                    binding.mockTestMathView.questionType = getString(
                        R.string.string_matrix_answer_question,
                        testQuestionData!!.correctReward,
                        testQuestionData!!.incorrectReward
                    )
                testQuestionData!!.type.equals(Constants.TEST_TEXT_ACTION_TYPE_OPTION) ->
                    binding.mockTestMathView.questionType = getString(
                        R.string.string_numerical_answer_question,
                        testQuestionData!!.correctReward,
                        testQuestionData!!.incorrectReward
                    )
                else -> binding.mockTestMathView.questionType = getString(
                    R.string.string_multiple_answer_question,
                    testQuestionData!!.correctReward,
                    testQuestionData!!.incorrectReward
                )
            }
        }
    }

    private fun setQuestionImage() {
        when {
            flagReport -> if (!testReportList!!.imageUrl.isNullOrBlank()) {
                binding.mockTestMathView.setIsImage()
                binding.mockTestMathView.imageLink = testReportList!!.imageUrl
            }
            else -> if (!testQuestionData!!.imageUrl.isNullOrBlank()) {
                binding.mockTestMathView.setIsImage()
                binding.mockTestMathView.imageLink = testQuestionData!!.imageUrl
            }
        }

    }

    private fun setOptionSelected() {
        binding.mockTestMathView.setInputTextFeild(inputText)
        checkBoxOptionList.let {
            binding.mockTestMathView.setMatrixCheckedOptions(it)
        }
        radioOptionList.let {
            binding.mockTestMathView.setRadioCheckedOptions(it)
        }
    }

    private fun clearOptionSelected() {
        if (mockTestQuestionViewModel.answersMap.containsKey(testQuestionData?.questionbankId!!.toInt())) {
            mockTestQuestionViewModel.answersMap.remove(testQuestionData?.questionbankId!!.toInt())
            mockTestQuestionViewModel.answersMapLiveData.postValue(mockTestQuestionViewModel.answersMap)
        }
        checkBoxOptionList.clear()
        radioOptionList.clear()
        binding.mockTestMathView.setMatrixCheckedOptions(checkBoxOptionList)
        binding.mockTestMathView.setRadioCheckedOptions(radioOptionList)
        binding.mockTestMathView.setInputTextFeild("")
        binding.mockTestMathView.post { binding.mockTestMathView.reload() }
        setAttemptedCount()
        clearCall()
    }

    private fun setIsTestReport() {
        if (flagReport) {
            binding.mockTestMathView.setIsReport()
            setReportMarks(testReportList!!)
            // inputText, checkBoxOptionList and radioOptionList fill these with the corrent answer
        } else {
            binding.tvMarks.hide()
            binding.tvMarksHeading.hide()
        }
    }

    fun setReportMarks(result: QuestionwiseResult) {
        binding.tvMarks.visibility = View.VISIBLE
        binding.tvMarksHeading.visibility = View.VISIBLE
        when {
            result!!.isSkipped == 1 -> {
                binding.tvskipped.show()
                binding.mockTestMathView.submitChangeName = getString(R.string.string_count_zero)
                binding.tvMarks.text = getString(R.string.string_count_zero)
                binding.tvMarks.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.tvMarks.isClickable = false
                binding.mockTestMathView.submitChangeColor = getString(R.string.string_small_green_color)
            }
            else -> {
                when {
                    result!!.isCorrect == 0 -> {
                        binding.mockTestMathView.submitChangeName = result!!.marksScored.toString()
                        binding.tvMarks.text = result!!.marksScored.toString()
                        binding.tvMarks.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.color_challenge_option_wrong_answer
                            )
                        )
                        binding.mockTestMathView.submitChangeColor =
                            getString(R.string.string_small_red_color)
                    }
                    else -> {
                        binding.mockTestMathView.submitChangeName = result!!.marksScored.toString()
                        binding.tvMarks.text = result!!.marksScored.toString()
                        binding.tvMarks.setTextColor(
                            ContextCompat.getColor(
                                requireContext(),
                                R.color.color_challenge_option_correct_answer
                            )
                        )
                        binding.mockTestMathView.submitChangeColor =
                            getString(R.string.string_small_green_color)
                    }
                }
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

    fun skipForMockTest(position: Int) {
        val activity = Handler(Looper.getMainLooper())
        activity.post {
            when {
                !flagReport -> {
                    if (checkedOptionList.isNullOrEmpty()) {
                        skipCall(position)
                        checkedOptionList.clear()
                        radioOptionList.clear()
                        checkBoxOptionList.clear()
                    } else {
                        if (testQuestionData?.type.equals("TEXT")) {
                            if (inputText.isEmpty()) {
                                skipCall(position)
                            } else {
                                binding.mockTestMathView.loadUrl("javascript:{var myVal = callFromActivity(); Android.onTextData(myVal.toString());}")
                            }
                        } else {
                            submitQuiz(false)
                        }
                    }
                }
                else -> listener?.onFragmentInteractionReport(position + 1)
            }
        }
    }

    fun submitQuiz(moveForward: Boolean = true,  skipNavigation: Boolean = false) {
        val activityNew = Handler(Looper.getMainLooper())
        activityNew.post {
            when {
                !flagReport -> {
                    selectedOptions = (Arrays.toString(checkedOptionList.toArray()).replace("[", "")
                        .replace("]", "").replace(" ", "")).trim()
                    val timeTaken = calculateTimeTaken()
                    var reviewStatus = if (testQuestionData!!.isReviewed == true) {
                        "REVIEWED"
                    } else {
                        null
                    }
                    oldCheckedOptionList = checkedOptionList
                    listener?.onFragmentInteractionCallSubmit(
                        testQuestionData!!.testId, "ANS", 0,
                        selectedOptions, testQuestionData!!.sectionCode!!,
                        testSubscriptionId.toString(),
                        testQuestionData!!.questionbankId!!.toString(),
                        testQuestionData!!.type!!, getIsEligible(), timeTaken,
                        testQuestionData!!.subjectCode!!, testQuestionData!!.chapterCode
                            ?: "",
                        testQuestionData!!.subtopicCode ?: "",
                        testQuestionData!!.classCode ?: "", testQuestionData!!.mcCode
                            ?: "", position, moveForward, reviewStatus,skipNavigation
                    )
                }
                else -> listener?.onFragmentInteractionReport(position + 1)
            }
        }
    }

    private fun skipCall(position: Int) {
        viewModel.getTestResponse(
            testQuestionData!!.testId,
            "SKIP",
            0,
            "",
            testQuestionData!!.sectionCode!!,
            testSubscriptionId.toString(),
            testQuestionData!!.questionbankId!!.toString(),
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
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        binding.progressBar2.visibility = View.GONE
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        binding.progressBar2.visibility = View.GONE
                        toast(getString(R.string.api_error))
                    }
                    is Outcome.BadRequest -> {
                        binding.progressBar2.visibility = View.GONE
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(requireFragmentManager(), "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        binding.progressBar2.visibility = View.GONE
                        listener?.onFragmentInteraction(position, true)
                    }
                }
            })
    }

    private fun clearCall() {
        viewModel.getTestResponse(
            testQuestionData!!.testId,
            "SKIP",
            0,
            "",
            testQuestionData!!.sectionCode!!,
            testSubscriptionId.toString(),
            testQuestionData!!.questionbankId!!.toString(),
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
                        binding.progressBar2.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        binding.progressBar2.visibility = View.GONE
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        binding.progressBar2.visibility = View.GONE
                        toast(getString(R.string.api_error))
                    }
                    is Outcome.BadRequest -> {
                        binding.progressBar2.visibility = View.GONE
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(requireFragmentManager(), "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        binding.progressBar2.visibility = View.GONE
                    }
                }
            })
    }

    @JavascriptInterface
    fun option(i: Int) {
        if (mockTestQuestionViewModel.sectionAttemptLimitMap?.get(testQuestionData?.sectionCode!!) != null) {
            if (!checkAttemptLimit()) {
                return
            }
        }
        val activity = Handler(Looper.getMainLooper())
        activity.post {
            when {
                !flagReport -> {
                    if (!checkedOptionList.contains(testQuestionData!!.options[i].optionCode.toString())) {
                        checkedOptionList = ArrayList()
                        checkedOptionList.add(testQuestionData!!.options[i].optionCode.toString())
                        radioOptionList = ArrayList()
                        radioOptionList.add(i.toString())
                    } else {
                        checkedOptionList = ArrayList()
                        radioOptionList = ArrayList()
                    }
                }
            }
        }
    }

    private fun checkAttemptLimit(): Boolean {
        var attemptLimitCount =
            mockTestQuestionViewModel.sectionAttemptLimitMap?.get(testQuestionData?.sectionCode!!);
        var dataInMap: Map<Int, String> =
            mockTestQuestionViewModel.answersMap.filter { it.value.equals(testQuestionData?.sectionCode!!) }

        if (dataInMap.size >= attemptLimitCount!!) {
            activity?.let {
                ToastUtils.makeText(
                    it,
                    "Clear some answers in this section to attempt this question",
                    Toast.LENGTH_SHORT
                ).show()
            }
            clearOptionSelected()
            return false
        }

        mockTestQuestionViewModel.answersMap[testQuestionData?.questionbankId!!.toInt()] =
            testQuestionData?.sectionCode!!
        mockTestQuestionViewModel.answersMapLiveData.postValue(mockTestQuestionViewModel.answersMap)
        binding.tvAttempted.post { setAttemptedCount() }
        return true
    }

    @JavascriptInterface
    fun optioncheckbox(i: Int) {
        if (mockTestQuestionViewModel.sectionAttemptLimitMap?.get(testQuestionData?.sectionCode!!) != null) {
            if (!checkAttemptLimit()) {
                return
            }
        }
        Log.d("checkedOptionList", "position - " + i.toString())
        val activity = Handler(Looper.getMainLooper())
        activity.post {

            when {
                !flagReport -> {

                    if (!checkedOptionList.contains(testQuestionData!!.options[i].optionCode.toString())) {
                        checkedOptionList.add(testQuestionData!!.options[i].optionCode.toString())
                        checkBoxOptionList.add(i.toString())
                        Log.d("checkBoxOptionList", "checkBoxOptionList - " + checkBoxOptionList)
                    } else {
                        checkedOptionList.remove(testQuestionData!!.options[i].optionCode.toString())
                        checkBoxOptionList.remove(i.toString())
                        Log.d("checkBoxOptionList", "checkBoxOptionList - " + checkBoxOptionList)
                    }
                }
            }
        }
    }


    @JavascriptInterface
    fun onTextData(value: String) {
        if (value.isNullOrEmpty()) {
            if (mockTestQuestionViewModel.answersMap.containsKey(testQuestionData?.questionbankId!!.toInt())) {
                mockTestQuestionViewModel.answersMap.remove(testQuestionData?.questionbankId!!.toInt())
                mockTestQuestionViewModel.answersMapLiveData.postValue(mockTestQuestionViewModel.answersMap)
            }
            if (isNextClicked) {
                inputText = ""
                skipForMockTest(position + 1)
            } else {
                inputText = ""
                skipForMockTest(position - 1)
            }
            return
        }
        if (mockTestQuestionViewModel.sectionAttemptLimitMap?.get(testQuestionData?.sectionCode!!) != null) {
            if (!checkAttemptLimit()) {
                return
            }
        }
        Log.d("value ::: ", value)
        inputText = value
        checkedOptionList.add(value)
        submitQuiz(isNextClicked)
    }

    interface OnMockTestFragmentInteractionListener {
        fun onFragmentInteraction(position: Int, isSkip: Boolean)
        fun onFragmentInteractionReport(position: Int)
        fun onFragmentInteractionCallSubmit(
            testId: Int, s: String, i1: Int?, selectedOptions: String,
            sectionCode: String, testSubscriptionId: String, toInt: String,
            type: String, isEligible: String, timeTaken: Int?,
            subjectCode: String?, s6: String?, s7: String?, s8: String?,
            s9: String?, position: Int, moveForward: Boolean, reviewStatus: String?,
            skipNavigation: Boolean
        )

        fun onFragmentInteractionGetIsEligible(): Int
        fun onFragmentBackClickedReport(position: Int)
    }

    private fun hideKeyBoard() {
        // Check if no view has focus:
        val view = activity?.currentFocus
        view?.let { v ->
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm?.let { it.hideSoftInputFromWindow(v.windowToken, 0) }
        }
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMockTestQuestionItemBinding {
        return FragmentMockTestQuestionItemBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
       return TAG
    }

    override fun provideViewModel(): MockTestListViewModel {
        return activityViewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        init()
        initMathView()
        setUpDataFromIntent()
        setQuestionToMathView()
        setQuestionSingleOrMcq()
        setQuestionImage()
        getFlagOptionFromSavedState(savedInstanceState)
        setOptionSelected()
        setIsTestReport()
        setAttemptLimitVisibilty()
        initObserver()
        if (flagReport) {
            binding.btnReview.hide()
        } else {
            setReviewButton()
        }

        binding.tvNext.setOnClickListener {
            isNextClicked = true
            when {
                testQuestionData?.type.equals("TEXT") -> {
                    binding.mockTestMathView.loadUrl("javascript:{var myVal = callFromActivity(); Android.onTextData(myVal.toString());}")
                }
                checkedOptionList.isNullOrEmpty() -> {
                    skipForMockTest(position + 1)
                }
                else -> {
                    submitQuiz()
                }
            }
            hideKeyBoard()
        }

        binding.tvClear.setOnClickListener {
            clearOptionSelected()
        }

        binding.tvSkip.setOnClickListener {
            isNextClicked = false
            if (!flagReport) {
                skipForMockTest(position - 1)
            } else {
                listener?.onFragmentBackClickedReport(position - 1)
            }
            hideKeyBoard()

        }
    }
}
