package com.doubtnutapp.ui.mockTest

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isEmpty
import androidx.fragment.app.viewModels
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.toast
import com.doubtnutapp.Constants
import com.doubtnutapp.Constants.MOCK_TEST_SECTION_LIST
import com.doubtnutapp.R
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.MockTestConfigData
import com.doubtnutapp.data.remote.models.MockTestReviewData
import com.doubtnutapp.data.remote.models.MockTestSectionData
import com.doubtnutapp.data.remote.models.MockTestSummary
import com.doubtnutapp.databinding.FragmentMockTestSummaryReportSubmissionBinding
import com.doubtnutapp.ui.base.BaseBindingFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.mockTest.dialog.ReviewQuestionDialogFragment
import com.doubtnutapp.utils.Utils
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent

class MockTestSummaryReportFragment : BaseBindingFragment<MockTestListViewModel, FragmentMockTestSummaryReportSubmissionBinding>(), View.OnClickListener {

    private var testSize: Int = 0
    private var testSubscriptionId: Int? = null
    private var mockTestSectionList: ArrayList<MockTestSectionData> = arrayListOf()
    private var listener: OnFragmentInteractionListenerTestSubmission? = null
    private var optionsData: ArrayList<MockTestSummary> = arrayListOf()
    private var viewVisible: Boolean = false
    private var attemptedCount: Int = 0
    private var skippedCount: Int = 0
    private var isCreated: Boolean = false
    private var reviewQuestionList: ArrayList<MockTestSummary> = arrayListOf()
    private var reviewData: MockTestReviewData? = null
    private var configData: MockTestConfigData? = null

    private val source: String by lazy {
        arguments?.getString(Constants.SOURCE).orEmpty()
    }

    private val isRevisionCornerSource: Boolean
        get() = source == Constants.REVISION_CORNER

    companion object {
        const val TAG = "MockTestSummaryReportFragment"

        fun newInstance(
            numberOfQuestions: Int,
            testSubscriptionId: Int,
            mockTestSectionList: ArrayList<MockTestSectionData>,
            source: String?,
            examType: String?,
        ): MockTestSummaryReportFragment {
            val fragment = MockTestSummaryReportFragment()
            val args = Bundle()
            args.putInt(Constants.TEST_SIZE, numberOfQuestions)
            args.putInt(Constants.TEST_SUBSCRIPTION_ID, testSubscriptionId)
            args.putParcelableArrayList(MOCK_TEST_SECTION_LIST, mockTestSectionList)
            args.putString(Constants.SOURCE, source)
            args.putString(Constants.EXAM_TYPE, examType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun providePageName(): String = TAG

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentMockTestSummaryReportSubmissionBinding =
        FragmentMockTestSummaryReportSubmissionBinding.inflate(layoutInflater)

    override fun provideViewModel(): MockTestListViewModel {
        val mockTestListViewModel by viewModels<MockTestListViewModel>(
            ownerProducer = { requireActivity() }
        ) { viewModelFactory }
        return mockTestListViewModel
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        setUpDataFromIntent()
        setuplistener()
        if (viewVisible) {
            getSummery()
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            viewVisible = true
            view ?: return
            if (isViewModelInitialized) getSummery()
        }
    }

    private fun getSummery() {
        reviewQuestionList.clear()
        viewModel.getSummary(testSubscriptionId ?: 0).observe(viewLifecycleOwner, { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarTestReportSubmission.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarTestReportSubmission.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    binding.progressBarTestReportSubmission.visibility = View.GONE
                    toast(getString(R.string.api_error))
                }
                is Outcome.BadRequest -> {
                    binding.progressBarTestReportSubmission.visibility = View.GONE
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(requireFragmentManager(), "BadRequestDialog")
                }
                is Outcome.Success -> {
                    binding.progressBarTestReportSubmission.visibility = View.GONE
                    binding.layoutQuestionCount.visibility = View.VISIBLE
                    optionsData.clear()
                    configData = response.data.data.configData
                    optionsData = response.data.data.summaryList ?: ArrayList()
                    reviewData = response.data.data.reviewData
                    if(!response.data.data.summaryList.isNullOrEmpty()) {
                        createSections()
                    }
                }
            }
        })
    }

    override fun onClick(v: View?) {
        val position = v!!.tag as Int
        listener?.summaryDotClickChangeViewPagerPosisiton(position)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = when (context) {
            is OnFragmentInteractionListenerTestSubmission -> context
            else -> throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
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
            mockTestSectionList = requireArguments().getParcelableArrayList(MOCK_TEST_SECTION_LIST)!!
        }
    }

    private fun setuplistener() {
        binding.btnSubmitTest.setOnClickListener {
            if (viewModel.reviewQuestionList.isEmpty()) {
                getTestSubmit()
                if (isRevisionCornerSource) {
                    viewModel.sendEvent(
                        EventConstants.RC_TEST_SUBMITED, hashMapOf(
                            EventConstants.TYPE to EventConstants.RC_FULL_LENGHT_TEST
                        ), ignoreSnowplow = true
                    )
                }
            } else {
                val fragment = ReviewQuestionDialogFragment.newInstance(
                    mockTestSectionList,
                    reviewQuestionList, testSubscriptionId, reviewData
                )
                fragment.setTargetFragment(this, 0)
                fragment.show(requireFragmentManager(), "")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == ReviewQuestionDialogFragment.SUBMIT_TEST_RESULT_CODE) {
            listener?.onFragmentInteractionTestSubmission()
        } else {
            listener?.summaryDotClickChangeViewPagerPosisiton(data?.getIntExtra(ReviewQuestionDialogFragment.QUESTION_INDEX, 0)
                    ?: 0)

        }
    }

    private fun getTestSubmit() {
        viewModel.getTestSubmit(testSubscriptionId ?: 0).observe(viewLifecycleOwner, { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarTestReportSubmission.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarTestReportSubmission.visibility = View.GONE
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(requireFragmentManager(), "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    binding.progressBarTestReportSubmission.visibility = View.GONE
                    toast(getString(R.string.api_error))
                }
                is Outcome.BadRequest -> {
                    binding.progressBarTestReportSubmission.visibility = View.GONE
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(requireFragmentManager(), "BadRequestDialog")
                }
                is Outcome.Success -> {
                    binding.progressBarTestReportSubmission.visibility = View.GONE

                    listener?.onFragmentInteractionTestSubmission()

                    if (arguments?.getString(Constants.SOURCE) == Constants.REVISION_CORNER) {
                        with(response.data.data) {
                            viewModel.submitRevisionCornerStats(
                                testId = testId,
                                totalScore = totalScore,
                                totalMarks = totalMarks,
                                examType = arguments?.getString(Constants.EXAM_TYPE).orEmpty()
                            )
                        }
                    }
                }
            }
        })
    }

    /**
     * Creates Sections according to the starting index and ending index in MockTestSectionData
     * and adds the sections in a FlexBoxLayout Dynamically.
     *
     * Map of Section position and list is created and then added to FlexBoxLayout to show skipped
     * and attempted questions with different colors.
     *
     */

    private fun createSections() {
        if (isCreated) {
            binding.flexLayout.removeAllViews()
            attemptedCount = 0
            skippedCount = 0
            isCreated = false
        }
        val sectionMap = HashMap<Int, ArrayList<MockTestSummary>>()
        var questionList = ArrayList<MockTestSummary>()
        for (i in 0 until mockTestSectionList.size - 1) {
            questionList = ArrayList<MockTestSummary>()
            for (j in mockTestSectionList[i].sectionStartingIndex until mockTestSectionList[i].sectionStartingIndex + mockTestSectionList[i].sectionEndingIndex + 1) {
                questionList.add(optionsData[j])
            }
            sectionMap[i] = questionList
        }
        for (i in 0 until mockTestSectionList.size - 1) {
            val sectionHeadingTv: TextView = TextView(context)
            val headingMargin = ViewUtils.dpToPx(32f, context).toInt()
            val headingTopMargin = ViewUtils.dpToPx(8f, context).toInt()
            val flexBox: FlexboxLayout = FlexboxLayout(context)
            sectionHeadingTv.text = mockTestSectionList[i].sectionTitle
            sectionHeadingTv.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            try {
                sectionHeadingTv.typeface = ResourcesCompat.getFont(requireContext(), R.font.lato_bold)
            } catch (e: Exception) {

            }
            binding.flexLayout.addView(sectionHeadingTv)
            val params: LinearLayout.LayoutParams =
                sectionHeadingTv.layoutParams as LinearLayout.LayoutParams
            params.leftMargin = headingMargin
            params.topMargin = headingTopMargin
            sectionHeadingTv.layoutParams = params
            flexBox.flexDirection = FlexDirection.ROW
            flexBox.flexWrap = FlexWrap.WRAP
            flexBox.justifyContent = JustifyContent.CENTER
            if (sectionMap.containsKey(i)) {
                var startingIndex = 0
                if (i != 0) {
                    startingIndex =
                        mockTestSectionList[i - 1].sectionStartingIndex + mockTestSectionList[i - 1].sectionEndingIndex + 1
                }
                setTextViewInFlexbox(flexBox, sectionMap[i]!!, startingIndex)
                binding.flexLayout.addView(flexBox)
                val params: LinearLayout.LayoutParams =
                    flexBox.layoutParams as LinearLayout.LayoutParams
                val margin = ViewUtils.dpToPx(8f, context).toInt()
                val bottomMargin = ViewUtils.dpToPx(16f, context).toInt()
                params.leftMargin = margin
                params.rightMargin = margin
                params.bottomMargin = bottomMargin
                params.topMargin = margin
                flexBox.layoutParams = params
            }
        }
        isCreated = true;
    }

    private fun setTextViewInFlexbox(
        flexBox: FlexboxLayout,
        mockTestSummary: ArrayList<MockTestSummary>,
        startingIndex: Int
    ) {
        if (!flexBox.isEmpty()) flexBox.removeAllViews()
        for (i in 0 until mockTestSummary.size) {
            val tvQuestions = TextView(context)
            tvQuestions.setOnClickListener(this)
            tvQuestions.tag = Integer.valueOf(startingIndex + i)
            tvQuestions.text = (startingIndex + i + 1).toString()
            tvQuestions.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
            tvQuestions.gravity = Gravity.CENTER

            if (viewModel.reviewQuestionList.contains(startingIndex + i + 1)) {
                tvQuestions.setPadding(
                    Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(8f).toInt(),
                    Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(8f).toInt()
                )
                tvQuestions.background = Utils.getShape(
                    configData?.reviewColor ?: "#ffffff",
                    configData?.reviewColor ?: "#ffffff",
                    5f
                )
                mockTestSummary[i].isReviewed = true
                tvQuestions.setTextColor(resources.getColor(R.color.white))
            } else {
                mockTestSummary[i].let {
                    when {

                        it.mockTestActionType == "ANS" && !it.mockTestOptionCode.isNullOrBlank() -> {
                            attemptedCount += 1
                            tvQuestions.background = Utils.getShape(
                                configData?.attemptedColor ?: "#ffffff",
                                configData?.attemptedColor ?: "#ffffff", 5f
                            )
                            tvQuestions.setPadding(
                                Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(8f).toInt(),
                                Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(8f).toInt()
                            )
                            tvQuestions.setTextColor(resources.getColor(R.color.white))
                        }

                        it.mockTestActionType == "SKIP" && it.mockTestOptionCode.isNullOrBlank() -> {
                            skippedCount += 1
                            tvQuestions.background =
                                Utils.getShape(
                                    configData?.skippedColor ?: "#ffffff", "#dcdcdc", 5f
                                )
                            tvQuestions.setPadding(
                                Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(8f).toInt(),
                                Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(8f).toInt()
                            )
                            tvQuestions.setTextColor(resources.getColor(R.color.black))
                        }
                        else -> {
                            skippedCount += 1
                            tvQuestions.background =
                                Utils.getShape(
                                    configData?.skippedColor ?: "#ffffff", "#dcdcdc", 5f
                                )
                            tvQuestions.setPadding(
                                Utils.convertDpToPixel(15f).toInt(),
                                Utils.convertDpToPixel(15f).toInt(),
                                Utils.convertDpToPixel(8f).toInt(),
                                Utils.convertDpToPixel(8f).toInt()
                            )
                            tvQuestions.setTextColor(resources.getColor(R.color.black))
                        }
                    }
                }
            }
            reviewQuestionList.add(mockTestSummary[i])
            val lpRight = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            tvQuestions.layoutParams = lpRight
            val lp = tvQuestions.layoutParams as FlexboxLayout.LayoutParams
            lp.setMargins(6, 6, 6, 6)
            tvQuestions.layoutParams = lp
            flexBox.addView(tvQuestions)
            binding.tvAttemptCount.text = attemptedCount.toString()
            binding.tvSkippedCount.text = skippedCount.toString()
            binding.tvReviewCount.text = reviewData?.reviewCount.orEmpty()
            binding.tvAttemptCount.setTextColor(Utils.parseColor(configData?.attemptedColor))
            binding.tvSkippedCount.setTextColor(Utils.parseColor("#225cdd"))
            binding.tvReviewCount.setTextColor(Utils.parseColor(configData?.reviewColor))
        }
    }

    interface OnFragmentInteractionListenerTestSubmission {
        fun onFragmentInteractionTestSubmission()
        fun summaryDotClickChangeViewPagerPosisiton(position: Int)
    }
}