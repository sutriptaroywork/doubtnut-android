package com.doubtnutapp.ui.mockTest.dialog

import android.app.Activity
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
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.utils.ViewUtils
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.R
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.MockTestReviewData
import com.doubtnutapp.data.remote.models.MockTestSectionData
import com.doubtnutapp.data.remote.models.MockTestSummary
import com.doubtnutapp.databinding.DialogReviewQuestionBinding
import com.doubtnutapp.getScreenWidth
import com.doubtnutapp.ui.base.BaseBindingDialogFragment
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.mockTest.MockTestListViewModel
import com.doubtnutapp.utils.Utils
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import javax.inject.Inject

class ReviewQuestionDialogFragment :
    BaseBindingDialogFragment<MockTestListViewModel, DialogReviewQuestionBinding>() {

    companion object {
        private const val TAG = "ReviewQuestionDialogFragment"
        private const val SECTION_LIST = "section_list"
        private const val QUESTION_LIST = "question_list"
        private const val SUBSCRIPTION_ID = "subscription_id"
        const val SUBMIT_TEST_RESULT_CODE = 33
        const val QUESTION_INDEX = "index"
        const val DATA = "data"

        fun newInstance(
            sectionList: ArrayList<MockTestSectionData>,
            questionList: ArrayList<MockTestSummary>,
            testSubscriptionId: Int?,
            data: MockTestReviewData?
        ): ReviewQuestionDialogFragment {
            val fragment = ReviewQuestionDialogFragment()
            val args = Bundle()
            args.putParcelableArrayList(SECTION_LIST, sectionList)
            args.putParcelableArrayList(QUESTION_LIST, questionList)
            args.putInt(SUBSCRIPTION_ID, testSubscriptionId ?: 0)
            args.putParcelable(DATA, data)
            fragment.arguments = args
            return fragment
        }
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    private var isCreated = false
    private var mockTestSectionList: ArrayList<MockTestSectionData> = arrayListOf()
    private var optionsData: ArrayList<MockTestSummary> = arrayListOf()
    private var testSubscriptionId = 0
    private var data: MockTestReviewData? = null
    private var startingQuestion = -1

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): DialogReviewQuestionBinding {
        return DialogReviewQuestionBinding.inflate(inflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MockTestListViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(view: View, savedInstanceState: Bundle?) {
        mockTestSectionList = arguments?.getParcelableArrayList(SECTION_LIST) ?: ArrayList()
        optionsData = arguments?.getParcelableArrayList(QUESTION_LIST) ?: ArrayList()
        testSubscriptionId = arguments?.getInt(SUBSCRIPTION_ID) ?: 0
        data = arguments?.getParcelable(DATA)
        createReviewSection()
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                EventConstants.TEST_REVIEW_POPUP_VIEW,
                hashMapOf(
                    EventConstants.TEST_ID to testSubscriptionId
                ), ignoreSnowplow = true
            )
        )
        binding.ivClose.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.TEST_REVIEW_POPUP_CLICK,
                    hashMapOf(
                        EventConstants.BUTTON to "close",
                        EventConstants.TEST_ID to testSubscriptionId
                    ), ignoreSnowplow = true
                )
            )
            dialog?.dismiss()
        }
        binding.btnSubmit.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.TEST_REVIEW_POPUP_CLICK,
                    hashMapOf(
                        EventConstants.BUTTON to "submit",
                        EventConstants.TEST_ID to testSubscriptionId
                    ), ignoreSnowplow = true
                )
            )
            getTestSubmit()
        }
        binding.tvReview.setOnClickListener {
            analyticsPublisher.publishEvent(
                AnalyticsEvent(
                    EventConstants.TEST_REVIEW_POPUP_CLICK,
                    hashMapOf(
                        EventConstants.BUTTON to "review",
                        EventConstants.TEST_ID to testSubscriptionId
                    ), ignoreSnowplow = true
                )
            )
            navigateToQuestion(startingQuestion)
        }
        if (data != null) {
            binding.tvReview.text = data?.buttonOneText.orEmpty()
            binding.btnSubmit.text = data?.buttonTwoText.orEmpty()
            binding.tvTitle.text = data?.title.orEmpty()
        }
    }

    private fun getTestSubmit() {
        viewModel.getTestSubmit(testSubscriptionId).observe(viewLifecycleOwner, { response ->
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

                    targetFragment?.onActivityResult(0, SUBMIT_TEST_RESULT_CODE, null)
                    dialog?.dismiss()
                }
            }
        })
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        fullScreenMode()
    }

    private fun fullScreenMode() {
        val width = requireActivity().getScreenWidth() - 50
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    private fun createReviewSection() {
        if (isCreated) {
            binding.flexLayout.removeAllViews()
            isCreated = false
        }
        val sectionMap = HashMap<Int, ArrayList<MockTestSummary>>()
        var questionList: java.util.ArrayList<MockTestSummary>
        for (i in 0 until mockTestSectionList.size - 1) {
            questionList = ArrayList()
            for (j in mockTestSectionList[i].sectionStartingIndex until
                    mockTestSectionList[i].sectionStartingIndex + mockTestSectionList[i].sectionEndingIndex + 1) {
                if (optionsData.getOrNull(j) != null && optionsData[j].isReviewed) {
                    optionsData[j].questionNumber = j + 1
                    questionList.add(optionsData[j])
                    if (startingQuestion == -1) {
                        startingQuestion = j
                    }
                }
            }
            if (questionList.size > 0) {
                sectionMap[i] = questionList
            }
        }
        for (i in 0 until mockTestSectionList.size - 1) {
            if (sectionMap.containsKey(i)) {
                val sectionHeadingTv = TextView(context)
                val headingMargin = ViewUtils.dpToPx(32f, context).toInt()
                val headingTopMargin = ViewUtils.dpToPx(8f, context).toInt()
                val flexBox = FlexboxLayout(context)
                sectionHeadingTv.text = mockTestSectionList[i].sectionTitle
                sectionHeadingTv.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                try {
                    sectionHeadingTv.typeface =
                        ResourcesCompat.getFont(requireContext(), R.font.lato_bold)
                } catch (e: Exception) {

                }
                binding.flexLayout.addView(sectionHeadingTv)
                val layoutParams: LinearLayout.LayoutParams =
                    sectionHeadingTv.layoutParams as LinearLayout.LayoutParams
                layoutParams.leftMargin = headingMargin
                layoutParams.topMargin = headingTopMargin
                sectionHeadingTv.layoutParams = layoutParams
                flexBox.flexDirection = FlexDirection.ROW
                flexBox.flexWrap = FlexWrap.WRAP
                flexBox.justifyContent = JustifyContent.CENTER
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
            isCreated = true
        }
    }

    private fun setTextViewInFlexbox(
        flexBox: FlexboxLayout,
        mockTestSummary: ArrayList<MockTestSummary>,
        startingIndex: Int
    ) {
        if (!flexBox.isEmpty()) flexBox.removeAllViews()
        for (i in 0 until mockTestSummary.size) {
            val tvQuestions = TextView(context)
            tvQuestions.setOnClickListener {
                val questionNumber = startingIndex + i
                navigateToQuestion(
                    (mockTestSummary.getOrNull(i)?.questionNumber?.minus(1)) ?: 0
                )
            }
            tvQuestions.tag = Integer.valueOf(startingIndex + i)
            tvQuestions.text = (mockTestSummary.getOrNull(i)?.questionNumber ?: 0).toString()
            tvQuestions.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
            tvQuestions.gravity = Gravity.CENTER
            tvQuestions.setPadding(
                Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(8f).toInt(),
                Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(8f).toInt()
            )
            tvQuestions.background = Utils.getShape("#751bad", "#751bad", 5f)
            tvQuestions.setTextColor(resources.getColor(R.color.white))

            val lpRight = FlexboxLayout.LayoutParams(
                FlexboxLayout.LayoutParams.WRAP_CONTENT,
                FlexboxLayout.LayoutParams.WRAP_CONTENT
            )
            tvQuestions.layoutParams = lpRight
            val lp = tvQuestions.layoutParams as FlexboxLayout.LayoutParams
            lp.setMargins(10, 10, 10, 10)
            tvQuestions.layoutParams = lp
            flexBox.addView(tvQuestions)
        }
    }

    private fun navigateToQuestion(questionNumber: Int) {
        dialog?.dismiss()
        val intent = Intent()
        intent.putExtra(QUESTION_INDEX, questionNumber)
        targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
    }

}