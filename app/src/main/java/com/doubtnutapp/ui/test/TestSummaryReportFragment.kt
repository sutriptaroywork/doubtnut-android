package com.doubtnutapp.ui.test

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.apiErrorToast
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.databinding.FragmentTestSummaryReportSubmissionBinding
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.google.android.flexbox.FlexboxLayout
import dagger.android.support.AndroidSupportInjection
import java.util.*

/**
 * Created by akshaynandwana on
 * 20, December, 2018
 **/
class TestSummaryReportFragment :
    BaseBindingFragment<TestSummaryFragmentViewModel, FragmentTestSummaryReportSubmissionBinding>(),
    View.OnClickListener {

    private var testSize: Int = 0
    private var testSubscriptionId: Int? = null
    private var listener: OnFragmentInteractionListenerTestSubmission? =
        null
    private var skipQuestionNumber: HashMap<Int, Boolean>? = HashMap()

    private var isVisibilee = false
    private var isStarted = false

    private val NOT_AN_TEST_ID = 0
    private var testId: Int = NOT_AN_TEST_ID

    companion object {
        const val TAG = "TestSummaryReportFragment"
        fun newInstance(
            numberOfQuestions: Int,
            testSubscriptionId: Int,
            testId: Int
        ): TestSummaryReportFragment {
            val fragment = TestSummaryReportFragment()
            val args = Bundle()
            args.putInt(Constants.TEST_SIZE, numberOfQuestions)
            args.putInt(Constants.TEST_SUBSCRIPTION_ID, testSubscriptionId)
            args.putInt(Constants.TEST_ID, testId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setUpDataFromIntent()
        setuplistener()
    }

    override fun onStart() {
        super.onStart()
        isStarted = true
        if (isVisibilee) {
            skipQuestionNumber = listener?.getSkipQuestionNumber()
            setTextViewInFlexbox(false)
        }
    }

    override fun onStop() {
        super.onStop()
        isStarted = false
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        isVisibilee = isVisibleToUser
        if (isVisibilee && isStarted) {
            skipQuestionNumber = listener?.getSkipQuestionNumber()
            setTextViewInFlexbox(true)
        }
    }

    override fun onClick(v: View?) {
        val position = v!!.tag as Int
        listener?.summaryDotClickChangeViewPagerPosisiton(position)
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        listener = when (context) {
            is OnFragmentInteractionListenerTestSubmission -> context
            else -> throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun setUpDataFromIntent() {
        arguments?.let {
            testSize = requireArguments().getInt(Constants.TEST_SIZE)
            testSubscriptionId = requireArguments().getInt(Constants.TEST_SUBSCRIPTION_ID)
            testId = arguments?.getInt(Constants.TEST_ID) ?: NOT_AN_TEST_ID
        }
    }

    private fun setuplistener() {
        mBinding?.btnSubmitTest?.setOnClickListener {
            getTestSubmit()
        }
    }

    private fun getTestSubmit() {
        viewModel.publishOnQuizSubmitEvent(testId.toString())
        viewModel.getTestSubmit(
            testSubscriptionId
                ?: 0
        ).observe(viewLifecycleOwner, { response ->
            when (response) {
                is Outcome.Progress -> {
                    mBinding?.progressBarTestReportSubmission?.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    mBinding?.progressBarTestReportSubmission?.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    mBinding?.progressBarTestReportSubmission?.visibility = View.GONE
                    apiErrorToast(response.e)
                }
                is Outcome.BadRequest -> {
                    mBinding?.progressBarTestReportSubmission?.visibility = View.GONE
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(requireFragmentManager(), "BadRequestDialog")
                }
                is Outcome.Success -> {
                    mBinding?.progressBarTestReportSubmission?.visibility = View.GONE
                    listener?.onFragmentInteractionTestSubmission()
                    viewModel.addTestIdToAttemptedList(
                        testId,
                        testSubscriptionId
                    )
                }
            }
        })
    }

    private fun setTextViewInFlexbox(clearBool: Boolean) {
        if (clearBool) {
            mBinding?.flexboxlayout?.removeAllViews()
        }
        for (i in 1..testSize) {
            val tvQuestions = TextView(context)
            tvQuestions.setOnClickListener(this)
            tvQuestions.tag = Integer.valueOf(i - 1)
            tvQuestions.text = i.toString()
            tvQuestions.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
            tvQuestions.gravity = Gravity.CENTER

            skipQuestionNumber?.let {
                when {
                    it.size == 0 -> {
                        tvQuestions.background =
                            resources.getDrawable(R.drawable.circle_test_question_unattempted)
                        tvQuestions.setTextColor(resources.getColor(R.color.greyBlackDark))
                    }
                    it.containsKey(i) -> when {
                        it.getValue(i) -> {
                            tvQuestions.background =
                                resources.getDrawable(R.drawable.circle_test_question_attempted)
                            tvQuestions.setTextColor(resources.getColor(R.color.white))
                        }
                        else -> {
                            tvQuestions.background =
                                resources.getDrawable(R.drawable.circle_test_question_attempted_not_submit)
                            tvQuestions.setTextColor(resources.getColor(R.color.white))
                        }
                    }
                    else -> {
                        tvQuestions.background =
                            resources.getDrawable(R.drawable.circle_test_question_unattempted)
                        tvQuestions.setTextColor(resources.getColor(R.color.greyBlackDark))
                    }
                }
            }

            val lpRight = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            tvQuestions.layoutParams = lpRight
            val lp = tvQuestions.layoutParams as FlexboxLayout.LayoutParams
            lp.setMargins(10, 10, 10, 10)
            tvQuestions.layoutParams = lp
            mBinding?.flexboxlayout?.addView(tvQuestions)
        }
    }

    interface OnFragmentInteractionListenerTestSubmission {
        fun onFragmentInteractionTestSubmission()
        fun getSkipQuestionNumber(): HashMap<Int, Boolean>?
        fun summaryDotClickChangeViewPagerPosisiton(position: Int)
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentTestSummaryReportSubmissionBinding {
        return FragmentTestSummaryReportSubmissionBinding.inflate(inflater, container, false)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): TestSummaryFragmentViewModel {
        return ViewModelProviders.of(this, viewModelFactory)
            .get(TestSummaryFragmentViewModel::class.java)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {

    }
}
