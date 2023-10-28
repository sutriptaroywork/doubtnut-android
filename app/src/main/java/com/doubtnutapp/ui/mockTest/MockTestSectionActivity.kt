package com.doubtnutapp.ui.mockTest

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.databinding.ActivityMockTestQuestionBinding
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnutapp.mathview.MathViewActivity
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.ui.mockTest.event.RefreshMockTestList
import com.doubtnutapp.ui.pdfviewer.PdfViewerActivity
import com.doubtnutapp.utils.TestUtils
import com.doubtnutapp.utils.Utils
import com.doubtnut.core.utils.ViewUtils
import com.doubtnutapp.widgetmanager.ui.WidgetLayoutAdapter
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.google.android.material.tabs.TabLayout
import com.instacart.library.truetime.TrueTimeRx
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.mock_test_not_started.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MockTestSectionActivity :
    BaseBindingActivity<MockTestQuestionViewModel, ActivityMockTestQuestionBinding>(),
    MockTestQuestionFragment.OnMockTestFragmentInteractionListener,
    MockTestSummaryReportFragment.OnFragmentInteractionListenerTestSubmission, View.OnClickListener,
    HasAndroidInjector {

    private lateinit var mockTestQuestionViewModel: MockTestQuestionViewModel
    private lateinit var mockTestListViewModel: MockTestListViewModel
    private lateinit var MockTestQuestionPagerAdapter: MockTestQuestionPagerAdapter

    private var testQuestionDataList: MutableList<MockTestQuestionDataOptions> = mutableListOf()
    private var mockTestSectionDataList: MutableList<MockTestSectionData> = mutableListOf()

    private var testReportList: MutableList<QuestionwiseResult> = mutableListOf()
    private lateinit var testDetails: TestDetails

    private var flag: String = Constants.TEST_OVER
    private var testSubscriptionId: Int = 0
    private var flagTestReport: Int = 0

    private var countDownTestTimer: CountDownTimer? = null
    private var countDownTestDurationTimer: CountDownTimer? = null
    private var countDownTimerTestNotify: CountDownTimer? = null

    private var countDownTestTimerBool: Boolean = false
    private var countDownTestDurationTimerBool: Boolean = false
    private var countDownTimerTestNotifyBool: Boolean = false

    private var isCreated: Boolean = false
    private var fromSkipped: Boolean = true

    var isEligible: Int = 1

    private var skipQuestionNumberFlexDraw: HashSet<Int> = HashSet()
    private var correctQuestionNumber: HashSet<Int> = HashSet()
    private var incorrectQuestionNumber: HashSet<Int> = HashSet()

    private val networkConnectivityIntentFilter: IntentFilter =
        IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")

    private var oldPageSelected = 0
    private var filterData: HashMap<String, Int>? = HashMap()

    private var flagTestSubmitted: Boolean = false
    private var configData: MockTestConfigData? = null

    private val source: String? by lazy { intent.getStringExtra(Constants.SOURCE) }

    private val isRevisionCornerSource: Boolean
        get() = source == Constants.REVISION_CORNER

    @Inject
    lateinit var deeplinkAction: DeeplinkAction

    companion object {
        fun newIntent(
            testActivity: Activity, testDetails: TestDetails, flag: String,
            testSubscriptionId: Int
        ): Intent {
            val testQuestionIntent = Intent(testActivity, MockTestSectionActivity::class.java)
            testQuestionIntent.putExtra(Constants.TEST_DETAILS_OBJECT, testDetails)
            testQuestionIntent.putExtra(Constants.TEST_TRUE_TIME_FLAG, flag)
            testQuestionIntent.putExtra(Constants.TEST_SUBSCRIPTION_ID, testSubscriptionId)
            return testQuestionIntent
        }

        fun liveClassIntent(
            fragmentActivity: FragmentActivity,
            testDetails: TestDetails,
            source: String?,
            examType: String?,
            ruleId: Int?,
        ): Intent {
            val testQuestionIntent = Intent(fragmentActivity, MockTestSectionActivity::class.java)
            testQuestionIntent.putExtra(Constants.TEST_DETAILS_OBJECT, testDetails)
            testQuestionIntent.putExtra(
                Constants.TEST_TRUE_TIME_FLAG, getTimeFlag(
                    testDetails.publishTime,
                    testDetails.unpublishTime, testDetails.attemptCount
                )
            )
            testQuestionIntent.putExtra(
                Constants.TEST_SUBSCRIPTION_ID,
                testDetails.testSubscriptionId?.toInt()
            )
            testQuestionIntent.putExtra(Constants.SOURCE, source)
            testQuestionIntent.putExtra(Constants.EXAM_TYPE, examType)
            testQuestionIntent.putExtra(
                Constants.RULE_ID,
                if (ruleId == Constants.DEFAULT_RULE_ID) null else ruleId
            )
            return testQuestionIntent
        }

        private fun getTimeFlag(
            publishTime: String?,
            unpublishTime: String?,
            attemptCount: Int?
        ): String {
            return when {
                attemptCount != 0 -> Constants.USER_CANNOT_ATTEMPT_TEST
                else -> TestUtils.getTrueTimeDecision(
                    publishTime,
                    unpublishTime,
                    now = when {
                        TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                        else -> Calendar.getInstance().time
                    }
                )
            }
        }

        private const val TAG = "MockTestSectionActivity"
    }

    @Inject
    lateinit var analyticsPublisher: AnalyticsPublisher

    override fun onDestroy() {
        super.onDestroy()
        if (countDownTestTimerBool) {
            countDownTestTimer?.cancel()
        }
        if (countDownTestDurationTimerBool) {
            countDownTestDurationTimer?.cancel()
        }
        if (countDownTimerTestNotifyBool) {
            countDownTimerTestNotify?.cancel()
        }
    }

    override fun onBackPressed() {
        when {
            (binding.viewPagerMockTest.visibility == View.VISIBLE) && (flagTestReport == 0)
                    && (flag == Constants.TEST_OVER) -> {
                val alertDialog: AlertDialog.Builder =
                    showAlertDialog(
                        getString(R.string.string_leaving_test),
                        getString(R.string.string_test_over_end_back_press_dialog_msg)
                    )
                alertDialog.setPositiveButton(getString(R.string.string_yes)) { _, _ ->
                    if (countDownTestDurationTimerBool) {
                        countDownTestDurationTimer?.cancel()
                    }
                    super.onBackPressed()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                alertDialog.setNegativeButton(getString(R.string.string_no)) { _, _ -> }
                alertDialog.show()

            }
            (binding.viewPagerMockTest.visibility == View.VISIBLE) && (flagTestReport == 0) -> {
                val alertDialog: AlertDialog.Builder =
                    showAlertDialog(
                        getString(R.string.string_leaving_test),
                        getString(R.string.string_test_over_end_back_press_dialog_msg)
                    )
                alertDialog.setPositiveButton(getString(R.string.string_yes)) { _, _ ->
                    if (countDownTestDurationTimerBool) countDownTestDurationTimer?.cancel()
                    if (countDownTimerTestNotifyBool) countDownTimerTestNotify?.cancel()

                    super.onBackPressed()
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
                alertDialog.setNegativeButton(getString(R.string.string_no)) { _, _ -> }
                alertDialog.show()

            }
            (binding.viewPagerMockTest.visibility == View.VISIBLE) && (flagTestReport == 1) -> {
                binding.viewPagerBackground.hide()
                binding.viewPagerMockTest.hide()
                binding.reportTitle.hide()
                binding.tvMarks.hide()
                binding.tvMarksHeading.hide()
                binding.llTopicFilters.hide()
                binding.layoutMockTestCheckScore.root.visibility = View.VISIBLE
            }

            flagTestSubmitted -> {
                DoubtnutApp.INSTANCE.bus()?.send(RefreshMockTestList())
                super.onBackPressed()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }

            else -> {
                super.onBackPressed()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    override fun onFragmentInteractionCallSubmit(
        testId: Int, actionType: String, isReview: Int?,
        optionCode: String, sectionCode: String,
        testSubcriptionId: String,
        questionbankId: String,
        questionType: String,
        isEligible: String,
        timeTaken: Int?,
        subjectCode: String?,
        chapterCode: String?,
        subtopicCode: String?,
        classCode: String?,
        mcCode: String?,
        position: Int,
        moveForward: Boolean,
        reviewStatus: String?,
        skipNavigation: Boolean
    ) {
        mockTestListViewModel.getTestResponse(
            testId,
            actionType,
            isReview,
            optionCode,
            sectionCode,
            testSubcriptionId,
            questionbankId,
            questionType,
            isEligible,
            timeTaken,
            subjectCode,
            chapterCode,
            subtopicCode,
            classCode, mcCode,
            reviewStatus
        )
            .observe(this, { response ->
                when (response) {
                    is Outcome.Progress -> {
                        binding.progressBarMockTestQuestion.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        binding.progressBarMockTestQuestion.visibility = View.GONE
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        binding.progressBarMockTestQuestion.visibility = View.GONE
                        toast(getString(R.string.api_error))
                    }
                    is Outcome.BadRequest -> {
                        binding.progressBarMockTestQuestion.visibility = View.GONE
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(supportFragmentManager, "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        binding.progressBarMockTestQuestion.visibility = View.GONE
                        if (!skipNavigation) {
                            onFragmentInteraction(
                                if (moveForward) {
                                    position + 1
                                } else {
                                    position - 1
                                }, false
                            )
                        }
                    }
                }

            })

    }

    fun onFragmentInteractionCallSubmitNoNext(
        testId: Int, actionType: String, isReview: Int?,
        optionCode: String, sectionCode: String,
        testSubcriptionId: String, questionbankId: String, questionType: String,
        isEligible: String, timeTaken: Int?,
        subjectCode: String?, chapterCode: String?, subtopicCode: String?,
        classCode: String?, mcCode: String?, position: Int
    ) {
        mockTestListViewModel.getTestResponse(
            testId,
            actionType,
            isReview,
            optionCode,
            sectionCode,
            testSubcriptionId,
            questionbankId,
            questionType,
            isEligible,
            timeTaken,
            subjectCode,
            chapterCode,
            subtopicCode,
            classCode, mcCode
        )
            .observe(this, { response ->
                when (response) {
                    is Outcome.Progress -> {
                        binding.progressBarMockTestQuestion.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        binding.progressBarMockTestQuestion.visibility = View.GONE
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        binding.progressBarMockTestQuestion.visibility = View.GONE
                        toast(getString(R.string.api_error))
                    }
                    is Outcome.BadRequest -> {
                        binding.progressBarMockTestQuestion.visibility = View.GONE
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(supportFragmentManager, "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        binding.progressBarMockTestQuestion.visibility = View.GONE

                    }
                }

            })

    }

    override fun onFragmentBackClickedReport(position: Int) {
        binding.viewPagerMockTest.currentItem = position
    }

    override fun onFragmentInteraction(position: Int, isSkip: Boolean) {
        when {
            position >= testQuestionDataList.size -> {
                binding.viewPagerMockTest.currentItem = position + 1
                fromSkipped = false

            }
            else -> binding.viewPagerMockTest.currentItem = position
        }
    }

    override fun onFragmentInteractionReport(position: Int) {
        when {
            position >= testReportList.size -> {
                val alertDialog: AlertDialog.Builder =
                    showAlertDialog(
                        getString(R.string.string_close_report),
                        getString(R.string.string_close_report)
                    )
                alertDialog.setPositiveButton(getString(R.string.string_ok)) { _, _ ->
                    binding.viewPagerBackground.hide()
                    binding.viewPagerMockTest.hide()
                    binding.tvMockTestTimer.hide()
                    binding.viewMock.hide()
                    binding.reportTitle.hide()
                    displayTestReportUI()
                }
                alertDialog.setNegativeButton(getString(R.string.string_notificationEducation_cancel)) { _, _ ->
                }
                alertDialog.show()
            }
            else -> binding.viewPagerMockTest.currentItem = position
        }
    }

    override fun onFragmentInteractionTestSubmission() {
        when {
            flag != Constants.TEST_OVER -> {
                if (countDownTestDurationTimerBool) countDownTestDurationTimer?.cancel()
                if (countDownTestTimerBool) countDownTestTimer?.cancel()
            }
        }
        if (countDownTestTimerBool) countDownTestTimer?.cancel()
        binding.viewPagerMockTest.hide()
        binding.viewPagerBackground.hide()
        binding.tvMockTestTimer.hide()
        binding.progressBarMockTestDurationTimer.hide()
        binding.llTopicFilters.hide()
        binding.progressLayout.hide()
        binding.viewMock.hide()
        displayTestReportUI()

        flagTestSubmitted = true
    }

    override fun onFragmentInteractionGetIsEligible(): Int {
        return isEligible
    }

    override fun summaryDotClickChangeViewPagerPosisiton(position: Int) {
        binding.viewPagerMockTest.currentItem = position
    }

    private fun init() {
        MockTestQuestionPagerAdapter = MockTestQuestionPagerAdapter(supportFragmentManager)
        mockTestQuestionViewModel =
            ViewModelProviders.of(this, viewModelFactory)[MockTestQuestionViewModel::class.java]
        mockTestListViewModel =
            ViewModelProviders.of(this, viewModelFactory)[MockTestListViewModel::class.java]
    }

    private fun startTest() {
        when {
            !com.doubtnutapp.utils.NetworkUtils.isConnected(this) -> showNoInternetAlertDialog()
            else -> {
                viewModel.startTest(testSubscriptionId.toString())
                analyticsPublisher.publishMoEngageEvent(AnalyticsEvent(EventConstants.MOCK_TEST_STARTED))
                getTestQuestionsandOptions()
                if (countDownTestTimerBool) countDownTestTimer?.cancel()
            }
        }
    }

    private fun setupListener() {
        binding.layoutMockTestStarted.btnStartQuiz.setOnClickListener {
            startTest()
        }
        binding.layoutMockTestOver.btnQuizOverStart.setOnClickListener {
            when {
                !com.doubtnutapp.utils.NetworkUtils.isConnected(this) -> showNoInternetAlertDialog()
                else -> {
                    analyticsPublisher.publishMoEngageEvent(AnalyticsEvent(EventConstants.MOCK_TEST_STARTED))
                    if (isRevisionCornerSource) {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.RC_START_TEST_CLICK, hashMapOf(
                                    EventConstants.TYPE to EventConstants.RC_FULL_LENGHT_TEST
                                ), ignoreSnowplow = true
                            )
                        )
                    }
                    viewModel.startTest(testSubscriptionId.toString())
                    getTestQuestionsandOptions()
                }
            }
        }

        binding.ivBack.setOnClickListener {
            this.onBackPressed()
        }

        binding.layoutMockTestCheckScore.tvViewAnswers.setOnClickListener {
            val position = 0
            val now = when {
                TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                else -> Calendar.getInstance().time
            }
            when {
                mockTestQuestionViewModel.getTrueTimeDecision(
                    testDetails.publishTime, testDetails.unpublishTime,
                    now
                ) == Constants.TEST_OVER -> {
                    checkDetailTestReport(position)
                }
                else -> toast(getString(R.string.test_not_start_till_time_end))
            }
        }


        binding.viewPagerMockTest.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {

                if (position != oldPageSelected && oldPageSelected != (testQuestionDataList.size + 1)) {

                    if (oldPageSelected != testQuestionDataList.size) {

                        val fragmentInstance = MockTestQuestionPagerAdapter.instantiateItem(
                            binding.viewPagerMockTest,
                            oldPageSelected
                        ) as? MockTestQuestionFragment
                        if (fragmentInstance != null && fragmentInstance.oldCheckedOptionList != fragmentInstance.checkedOptionList) {

                            val selectedOptions =
                                (Arrays.toString(fragmentInstance.checkedOptionList.toArray())
                                    .replace("[", "")
                                    .replace("]", "").replace(" ", "")).trim()

                            fragmentInstance.oldCheckedOptionList =
                                fragmentInstance.checkedOptionList

                            val actionType = if (selectedOptions.isNullOrEmpty()) "SKIP" else "ANS"

                            val isEligibleGet = when (flag) {
                                Constants.TEST_OVER -> 0.toString()
                                else -> isEligible.toString()
                            }

                            val timeTake = when {
                                TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                                else -> Calendar.getInstance().time
                            }
                            val timeTaken = (timeTake.time / 1000).toInt()


                            if (flagTestReport == 0 && fromSkipped) {

                                onFragmentInteractionCallSubmitNoNext(
                                    fragmentInstance.testQuestionData!!.testId,
                                    actionType,
                                    0,
                                    selectedOptions,
                                    fragmentInstance.testQuestionData!!.sectionCode,
                                    fragmentInstance.testSubscriptionId.toString(),
                                    fragmentInstance.testQuestionData?.questionbankId.toString(),
                                    fragmentInstance.testQuestionData!!.type!!,
                                    isEligibleGet,
                                    timeTaken,
                                    fragmentInstance.testQuestionData?.subjectCode ?: "",
                                    fragmentInstance.testQuestionData?.chapterCode ?: "",
                                    fragmentInstance.testQuestionData?.subtopicCode ?: "",
                                    fragmentInstance.testQuestionData?.classCode ?: "",
                                    fragmentInstance.testQuestionData?.mcCode
                                        ?: "",
                                    fragmentInstance.position
                                )

                            }
                        }
                    }

                    oldPageSelected = position


                    if (testReportList.isEmpty() && position < testQuestionDataList.size) {
                        val index = filterData?.get(testQuestionDataList[position].sectionTitle)
                        if (index != null && index != binding.tbTopicFilters.selectedTabPosition) {
                            index.apply { binding.tbTopicFilters.getTabAt(index!!)?.select() }
                        }
                    } else if (testReportList.isEmpty() && position == testQuestionDataList.size) {
                        binding.tbTopicFilters.getTabAt(mockTestSectionDataList.lastIndex)?.select()
                    } else if (testQuestionDataList.isEmpty() && position < testReportList.size) {
                        val index = filterData?.get(testReportList[position].sectionTitle)
                        if (index != binding.tbTopicFilters.selectedTabPosition) {
                            index.apply { binding.tbTopicFilters.getTabAt(index!!)?.select() }
                        }
                    } else if (testQuestionDataList.isEmpty() && position == testReportList.size) {
                        binding.tbTopicFilters.getTabAt(mockTestSectionDataList.lastIndex)?.select()
                    }
                }
            }

        })
    }

    private fun setupDataFromIntent() {
        intent?.let {
            testDetails = it.getParcelableExtra<TestDetails>(Constants.TEST_DETAILS_OBJECT)!!
            flag = it.getStringExtra(Constants.TEST_TRUE_TIME_FLAG).orEmpty()
            testSubscriptionId = it.getIntExtra(Constants.TEST_SUBSCRIPTION_ID, 0)
        }
    }

    private fun decideVisibleLayout() {
        when (flag) {
            Constants.TEST_OVER -> {
                displayTestOverUI()
            }
            Constants.TEST_UPCOMING -> {
                displayTestUpcomingUI()
            }
            Constants.TEST_ACTIVE -> {
                displayTestActiveUI()
            }
            Constants.USER_CANNOT_ATTEMPT_TEST -> {
                displayTestReportUI()
            }
        }
    }

    private fun displayTestOverUI() {
        binding.layoutMockTestOver.root.visibility = View.VISIBLE
        showTestRules(binding.layoutMockTestOver.root)
    }

    private fun displayTestUpcomingUI() {
        binding.layoutMockTestNotStarted.root.visibility = View.VISIBLE
        showTestRules(binding.layoutMockTestNotStarted.root)
    }

    private fun displayTestActiveUI() {
        binding.layoutMockTestStarted.root.visibility = View.VISIBLE
        showTestRules(binding.layoutMockTestStarted.root)
    }

    private fun displayTestReportUI() {
        binding.tvTitle.text = getString(R.string.string_test_report)
        mockTestQuestionViewModel.getTestResult(
            testSubscriptionId
        ).observe(this, { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarMockTestQuestion.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarMockTestQuestion.hide()
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    binding.progressBarMockTestQuestion.hide()
                    toast(getString(R.string.api_error))
                }
                is Outcome.BadRequest -> {
                    binding.progressBarMockTestQuestion.hide()
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }
                is Outcome.Success -> {
                    setupIndexData(
                        response.data.data.indexData,
                        response.data.data.showResult ?: true
                    )
                    binding.progressBarMockTestQuestion.hide()
                    configData = response.data.data.configData
                    val adapter = WidgetLayoutAdapter(this)
                    binding.layoutMockTestCheckScore.rvWidgets.adapter = adapter
                    binding.layoutMockTestCheckScore.rvWidgets.layoutManager =
                        LinearLayoutManager(this, RecyclerView.VERTICAL, false)
                    adapter.setWidgets(response.data.data.widgets.orEmpty())
                    testReportList.clear()
                    testReportList.addAll(response.data.data.questionwiseResult)
                    var params = binding.guideline8.layoutParams as ConstraintLayout.LayoutParams
                    params.guidePercent = 0.05f
                    params.guideBegin = ViewUtils.dpToPx(150f, this).toInt()
                    binding.guideline8.layoutParams = params
                    binding.layoutMockTestCheckScore.root.visibility = View.VISIBLE
                    //tbTopicFilters.hide()
                    binding.llTopicFilters.hide()
                    binding.progressLayout.hide()

                    skipQuestionNumberFlexDraw.clear()
                    response.data.data.reportCard.skipped?.let {
                        if (it.isNotEmpty()) {
                            skipQuestionNumberFlexDraw = it.split(",").map { it.trim() }
                                .map { it.toInt() }.toHashSet()
                        }
                    }

                    response.data.data.reportCard.correct?.let {
                        if (it.isNotEmpty()) {
                            correctQuestionNumber = it.split(",").map { it.trim() }
                                .map { it.toInt() }.toHashSet()
                        }
                    }

                    response.data.data.reportCard.incorrect?.let {
                        if (it.isNotEmpty()) {
                            incorrectQuestionNumber = it.split(",").map { it.trim() }
                                .map { it.toInt() }.toHashSet()
                        }
                    }
                    val sectionData = response.data.data.sectionData
                    if(response.data.data.showResult == true){
                                 sectionData.add(
                        0,
                        MockTestSectionData(
                            0,
                            "",
                            getString(R.string.total),
                            "",
                            "",
                            "",
                            0,
                            0,
                            "",
                            "",
                            "",
                            "",
                            "",
                            0,
                            0,
                            response.data.data.reportCard.totalScore.toString(),
                            correctQuestionNumber.size.toString(),
                            skipQuestionNumberFlexDraw.size.toString(),
                            incorrectQuestionNumber.size.toString(),
                            0,
                            false)
                                 )
                    } else {
                        sectionData.add(
                            0,
                            MockTestSectionData(
                                0,
                                "",
                                getString(R.string.total),
                                "",
                                "",
                                "",
                                0,
                                0,
                                "",
                                "",
                                "",
                                "",
                                "",
                                0,
                                0,
                                response.data.data.reportCard.totalScore.toString(),
                                response.data.data.attempted,
                                "--",
                                response.data.data.unattempted,
                                0,
                                false)
                        )
                    }

                    val bottomWidget = response.data.data.bottomWidgetEntity
                    createFlexSections(sectionData, response.data.data.questionwiseResult)
                    if ((testDetails.solutionPdf.isNullOrEmpty()
                                && bottomWidget?.bottomDeeplink.isNullOrEmpty()
                                && bottomWidget?.textSolution.isNullOrEmpty())
                        || intent.getStringExtra(Constants.SOURCE) == Constants.REVISION_CORNER
                    ) {
                        binding.layoutMockTestCheckScore.tvPdfDownload.visibility = View.GONE
                    } else {
                        binding.layoutMockTestCheckScore.tvPdfDownload.show()
                        if (!bottomWidget?.bottomText.isNullOrEmpty()) {
                            binding.layoutMockTestCheckScore.tvPdfDownload.text =
                                bottomWidget?.bottomText
                        }
                        binding.layoutMockTestCheckScore.tvPdfDownload.setOnClickListener {
                            if (!bottomWidget?.bottomDeeplink.isNullOrEmpty()) {
                                deeplinkAction.performAction(this, bottomWidget?.bottomDeeplink)
                            } else if (!bottomWidget?.textSolution.isNullOrEmpty()) {
                                startActivity(
                                    MathViewActivity.getStartIntent(
                                        this,
                                        bottomWidget?.textSolution
                                    )
                                )
                            } else if (!testDetails.solutionPdf.isNullOrEmpty()) {
                                PdfViewerActivity.previewPdfFromTheUrl(
                                    this@MockTestSectionActivity,
                                    testDetails.solutionPdf.orEmpty()
                                )
                            } else {
                                toast(getString(R.string.somethingWentWrong))
                            }
                        }
                    }
                    setUpRecyclerView(
                        response.data.data.sectionData,
                        response.data.data.showResult ?: true
                    )
                    for (i in 0 until sectionData.size) {
//                        tbTopicFilters.addTab(tbTopicFilters.newTab().setText(sectionData[i].sectionTitle))
                        filterData?.put(sectionData[i].sectionTitle, i)
                    }

                }
            }
        })
    }

    private fun setupIndexData(indexData: List<MockTestResult.IndexData>?, showResult: Boolean) {
        binding.layoutMockTestCheckScore.indexCorrect.text =
            indexData?.getOrNull(0)?.text ?: getString(R.string.correct_index_text)
        binding.layoutMockTestCheckScore.indexIncorrect.text =
            indexData?.getOrNull(1)?.text ?: getString(R.string.incorrect_index_text)
        if (indexData?.getOrNull(2)?.text == null) {
            binding.layoutMockTestCheckScore.indexSkip.hide()
        } else {
            binding.layoutMockTestCheckScore.indexSkip.text =
                indexData.getOrNull(2)?.text ?: getString(R.string.skip_index_text)
        }
        binding.layoutMockTestCheckScore.colorCorrect.background = Utils.getShape(
            indexData?.getOrNull(
                0
            )?.color ?: "#ffffff",
            indexData?.getOrNull(
                0
            )?.color ?: "#ffffff",
            5f,
            1
        )
        if (showResult) {
            binding.layoutMockTestCheckScore.colorIncorrect.background = Utils.getShape(
                indexData?.getOrNull(
                    1
                )?.color ?: "#ffffff",
                indexData?.getOrNull(
                    1
                )?.color ?: "#ffffff",
                5f,
                1
            )
        } else {
            binding.layoutMockTestCheckScore.colorIncorrect.background = Utils.getShape(
                indexData?.getOrNull(
                    1
                )?.color ?: "#ffffff",
                "#dcdcdc",
                5f,
                1
            )
        }


        if (indexData?.getOrNull(2) == null) {
            binding.layoutMockTestCheckScore.colorSkip.hide()
        } else {
            binding.layoutMockTestCheckScore.colorSkip.background = Utils.getShape(
                indexData.getOrNull(
                    2
                )?.color ?: "#ffffff", "#dcdcdc",
                5f,
                1
            )
        }
    }

    /**
     * Creates Sections according to the starting index and ending index in MockTestSectionData
     * and adds the sections in a FlexBoxLayout Dynamically.
     *
     * Map of Section position and list is created and then added to FlexBoxLayout to show correct,
     * incorrect and attempted questions with different colors.
     *
     */

    private fun createFlexSections(
        sectionData: ArrayList<MockTestSectionData>,
        resultList: ArrayList<QuestionwiseResult>
    ) {
        if (!isCreated) {
            val sectionMap = HashMap<Int, ArrayList<QuestionwiseResult>>()
            var questionList = ArrayList<QuestionwiseResult>()
            for (i in 1 until sectionData.size) {
                questionList = ArrayList<QuestionwiseResult>()
                for (j in sectionData[i].sectionStartingIndex until sectionData[i].sectionStartingIndex + sectionData[i].sectionEndingIndex + 1) {
                    questionList.add(resultList[j])
                }
                sectionMap[i] = questionList
            }
            for (i in 1 until sectionData.size) {
                val sectionHeadingTv: TextView = TextView(this)
                val headingMargin = ViewUtils.dpToPx(32f, this@MockTestSectionActivity).toInt()
                val headingTopMargin = ViewUtils.dpToPx(8f, this@MockTestSectionActivity).toInt()
                val flexBox: FlexboxLayout = FlexboxLayout(this)
                sectionHeadingTv.text = sectionData[i].sectionTitle
                sectionHeadingTv.setTextColor(
                    ContextCompat.getColor(
                        this@MockTestSectionActivity,
                        R.color.black
                    )
                )
                try {
                    sectionHeadingTv.typeface =
                        ResourcesCompat.getFont(this@MockTestSectionActivity, R.font.lato_bold)
                } catch (e: Exception) {

                }
                binding.layoutMockTestCheckScore.flexboxLayout.addView(sectionHeadingTv)
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
                    if (i != 1) {
                        startingIndex =
                            sectionData[i - 1].sectionStartingIndex + sectionData[i - 1].sectionEndingIndex + 1
                    }
                    setTextViewInFlexbox(flexBox, sectionMap[i]!!, startingIndex)
                    binding.layoutMockTestCheckScore.flexboxLayout.addView(flexBox)
                    val params: LinearLayout.LayoutParams =
                        flexBox.layoutParams as LinearLayout.LayoutParams
                    val margin = ViewUtils.dpToPx(8f, this@MockTestSectionActivity).toInt()
                    val bottomMargin = ViewUtils.dpToPx(16f, this@MockTestSectionActivity).toInt()
                    params.leftMargin = margin
                    params.rightMargin = margin
                    params.bottomMargin = bottomMargin
                    params.topMargin = margin
                    flexBox.layoutParams = params
                }
            }
            isCreated = true
        }
    }

    private fun showTestRules(layout_view: View) {
        binding.tvTitle.text = testDetails.title
        val ruleId = intent?.getIntExtra(Constants.RULE_ID, testDetails.ruleId ?: 0) ?: 0
        mockTestQuestionViewModel.getTestRules(ruleId).observe(this, { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarMockTestQuestion.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarMockTestQuestion.hide()
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    binding.progressBarMockTestQuestion.hide()
                    toast(getString(R.string.api_error))
                }
                is Outcome.BadRequest -> {
                    binding.progressBarMockTestQuestion.hide()
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }
                is Outcome.Success -> {
                    binding.progressBarMockTestQuestion.hide()

                    val builder: String?
                    builder = when {
                        response.data.data.rules.orEmpty()
                            .isEmpty() -> getString(R.string.string_test_rules)
                        else -> {
                            val stringBuilder = StringBuilder()
                            for (rules in response.data.data.rules.orEmpty()) {
                                stringBuilder.append("\u2022 $rules \n")
                            }
                            stringBuilder.toString()
                        }
                    }

                    when (layout_view) {
                        binding.layoutMockTestOver.root -> {
                            binding.layoutMockTestOver.btnQuizOverStart.text =
                                getString(R.string.string_start_test)
                            binding.layoutMockTestOver.tvRulesOver.text = builder
                        }
                        binding.layoutMockTestNotStarted.root -> {
                            binding.layoutMockTestNotStarted.tvRuleNotStarted.text = builder
                            showStartTestTimer()
                        }
                        binding.layoutMockTestStarted.root -> {
                            binding.layoutMockTestStarted.btnStartQuiz.text =
                                getString(R.string.string_start_test)
                            binding.layoutMockTestStarted.tvRulesStarted.text = builder
                        }
                    }

                    if (isRevisionCornerSource) {
                        analyticsPublisher.publishEvent(
                            AnalyticsEvent(
                                EventConstants.RC_TEST_INSTRUCTION_PAGE_SHOWN, hashMapOf(
                                    EventConstants.TYPE to EventConstants.RC_FULL_LENGHT_TEST
                                ), ignoreSnowplow = true
                            )
                        )
                    }
                }
            }
        })
    }

    private fun showStartTestTimer() {
        val readFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val currentTime = when {
            TrueTimeRx.isInitialized() -> TrueTimeRx.now()
            else -> Calendar.getInstance().time
        }
        val trueTime = readFormat.parse(readFormat.format(currentTime.time))
        val startTestTime = SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            Locale.getDefault()
        ).parse(testDetails.publishTime)
        if (trueTime.before(startTestTime)) {
            //in Milliseconds
            val time: Long = testDetails.testWaitTimeMillis ?: 0L
            val startTestTimer = object : CountDownTimer(time, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    binding.layoutMockTestNotStarted.btnNotStartedStart.text = getString(
                        R.string.starting_in_text,
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                        (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                                TimeUnit.HOURS.toMinutes(
                                    TimeUnit.MILLISECONDS.toHours(
                                        millisUntilFinished
                                    )
                                )),
                        (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                                TimeUnit.MINUTES.toSeconds(
                                    TimeUnit.MILLISECONDS.toMinutes(
                                        millisUntilFinished
                                    )
                                ))
                    )
                }

                override fun onFinish() {
                    binding.layoutMockTestNotStarted.btnNotStartedStart.text =
                        getString(R.string.string_start_test)
                    binding.layoutMockTestNotStarted.btnNotStartedStart.setOnClickListener {
                        startTest()
                    }
                }
            }
            startTestTimer.start()
        }
    }

    private fun setupTestDurationTimer() {
        val time: Long = TimeUnit.MINUTES.toMillis(testDetails.durationInMin?.toLong() ?: 0L)
        binding.progressBarMockTestDurationTimer.max = time.toInt()
        countDownTestDurationTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTestDurationTimerBool = true
                binding.tvMockTestTimer.text = getString(
                    R.string.string_quiz_question_timer,
                    TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                    (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) -
                            TimeUnit.HOURS.toMinutes(
                                TimeUnit.MILLISECONDS.toHours(
                                    millisUntilFinished
                                )
                            )),
                    (TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    millisUntilFinished
                                )
                            ))
                )
                binding.progressBarMockTestDurationTimer.progress = millisUntilFinished.toInt()
            }

            override fun onFinish() {
                countDownTestDurationTimerBool = false
                binding.tvMockTestTimer.text = getString(R.string.string_test_duration_timer_finish)
                binding.progressBarMockTestDurationTimer.progress = 0
                val alertDialog = showAlertDialog(
                    getString(R.string.string_test_over_dialog_title),
                    getString(R.string.string_test_over_no_points_dialog_msg)
                )
                alertDialog.setPositiveButton(getString(R.string.string_ok)) { _, _ -> }
                if (!isFinishing) alertDialog.show()
                isEligible = 0
            }
        }
        countDownTestDurationTimer?.start()
    }

    private fun getTestQuestionsandOptions() {
        mockTestQuestionViewModel.getTestQuestions(testDetails.testId)
            .observe(this, { response ->
                when (response) {
                    is Outcome.Progress -> {
                        binding.progressBarMockTestQuestion.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        Log.d("QuestionsCheck", response.toString())
                        binding.progressBarMockTestQuestion.hide()
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        binding.progressBarMockTestQuestion.hide()
                        toast(getString(R.string.api_error))
                    }
                    is Outcome.BadRequest -> {
                        binding.progressBarMockTestQuestion.hide()
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(supportFragmentManager, "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        binding.progressBarMockTestQuestion.hide()
                        if (response.data.data.mockTestQuestionDataList.isNotEmpty()) {
                            testQuestionDataList.addAll(response.data.data.mockTestQuestionDataList)
                            if (response.data.data.mockTestSectionDataList.isNotEmpty()) {
                                mockTestSectionDataList = response.data.data.mockTestSectionDataList
                                setSectionAttemptLimit(mockTestSectionDataList as ArrayList<MockTestSectionData>)
                            }
                            setupMockTestQuestionPagerAdapter()
                            binding.tvSummary.setOnClickListener {
                                setSummaryTabBackground()
                                removeBackground()
                                binding.tbTopicFilters.getTabAt(mockTestSectionDataList.lastIndex)
                                    ?.select()
                            }

                        }
                        if (mockTestSectionDataList.isNotEmpty()) {
                            mockTestSectionDataList.add(
                                MockTestSectionData(
                                    1,
                                    "",
                                    getString(R.string.string_test_summary_tab),
                                    "",
                                    "",
                                    "",
                                    0,
                                    0,
                                    "",
                                    "",
                                    "",
                                    "",
                                    "",
                                    testQuestionDataList.size + 1,
                                    testQuestionDataList.size + 1,
                                    "",
                                    "",
                                    "",
                                    "",
                                    0,
                                    false)
                            )
                            for (i in 0 until response.data.data.mockTestSectionDataList.size) {
                                if (i == response.data.data.mockTestSectionDataList.size - 1) {
                                    binding.tbTopicFilters.addTab(
                                        binding.tbTopicFilters.newTab().setText("")
                                    )
                                } else {
                                    binding.tbTopicFilters.addTab(
                                        binding.tbTopicFilters.newTab()
                                            .setText(mockTestSectionDataList[i].sectionTitle)
                                    )
                                }
                                filterData?.put(mockTestSectionDataList[i].sectionTitle, i)
                            }
                        }

                    }
                }
            })
    }

    private fun setSectionAttemptLimit(mockTestSectionDataList: ArrayList<MockTestSectionData>) {
        for (items in mockTestSectionDataList) {
            if (items.attemptLimit != null)
                mockTestQuestionViewModel.sectionAttemptLimitMap!!.put(
                    items.sectionCode,
                    items.attemptLimit
                )
        }
    }

    private fun setSummaryTabBackground() {
        binding.tvSummary.gravity = Gravity.CENTER
        val topPadding: Int = ViewUtils.dpToPx(4f, this@MockTestSectionActivity).toInt()
        val leftPadding: Int = ViewUtils.dpToPx(6f, this@MockTestSectionActivity).toInt()
        binding.tvSummary.setPadding(leftPadding, topPadding, leftPadding, topPadding)
        binding.tvSummary.background = getDrawable(R.drawable.bg_mock_test_tab_selected)
        binding.tvSummary.setTextColor(
            ContextCompat.getColor(
                this@MockTestSectionActivity,
                R.color.white
            )
        )
    }

    private fun setupMockTestQuestionPagerAdapter() {
        (0..testQuestionDataList.size).forEach { position ->
            when {
                position != testQuestionDataList.size -> MockTestQuestionPagerAdapter.addFragment(
                    MockTestQuestionFragment.newInstance(
                        testQuestionDataList[position],
                        position,
                        testQuestionDataList.size,
                        testSubscriptionId, flag,
                        false
                    ),
                    (position + 1).toString()
                )
                else -> MockTestQuestionPagerAdapter.addFragment(
                    MockTestSummaryReportFragment.newInstance(
                        testQuestionDataList.size,
                        testSubscriptionId,
                        mockTestSectionDataList as ArrayList<MockTestSectionData>,
                        intent?.getStringExtra(Constants.SOURCE),
                        intent?.getStringExtra(Constants.EXAM_TYPE),
                    ),
                    getString(R.string.string_test_report_your_answer)
                )
            }
        }
        binding.viewPagerMockTest.adapter = MockTestQuestionPagerAdapter
        binding.mockTestTabs.setupWithViewPager(binding.viewPagerMockTest)

        when {
            binding.layoutMockTestOver.root.visibility == View.VISIBLE -> binding.layoutMockTestOver.root.hide()
            else -> {
                binding.layoutMockTestStarted.root.hide()
                binding.layoutMockTestNotStarted.root.hide()
                if (mockTestQuestionViewModel.getTestStartAfterTimeDifferenceLong(testDetails.unpublishTime) <
                    TimeUnit.MINUTES.toMillis(testDetails.durationInMin?.toLong() ?: 0L)
                ) {
                    setupTestUnpublishNotifyTimer(
                        mockTestQuestionViewModel
                            .getTestStartAfterTimeDifferenceLong(testDetails.unpublishTime)
                    )
                }
            }
        }
        binding.tvMockTestTimer.visibility = View.VISIBLE
        binding.tvSummary.visibility = View.VISIBLE
        binding.llTopicFilters.show()
        binding.viewMock.visibility = View.VISIBLE
        binding.progressBarMockTestDurationTimer.visibility = View.VISIBLE
        binding.progressLayout.visibility = View.VISIBLE
        binding.viewPagerBackground.show()
        binding.viewPagerMockTest.visibility = View.VISIBLE
        binding.mockTestTabs.visibility = View.VISIBLE
        setupTestDurationTimer()
    }

    private fun setupTestUnpublishNotifyTimer(testUnpublishDurationDifference: Long) {
        countDownTimerTestNotify = object : CountDownTimer(testUnpublishDurationDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTimerTestNotifyBool = true
            }

            override fun onFinish() {
                countDownTimerTestNotifyBool = false
                if (isFinishing.not() && isDestroyed.not()) {
                    val alertDialog = showAlertDialog(
                        getString(R.string.string_test_over_dialog_title),
                        getString(R.string.string_test_over_no_points_dialog_msg)
                    )
                    alertDialog.setPositiveButton(getString(R.string.string_ok)) { _, _ -> }
                    alertDialog.show()
                }
                isEligible = 0
            }
        }
        countDownTimerTestNotify?.start()
    }

    private fun checkDetailTestReport(viewPagerPosition: Int) {
        if (!com.doubtnutapp.utils.NetworkUtils.isConnected(this)) {
            val alertDialog = showAlertDialog(
                getString(R.string.string_quiz_no_report),
                getString(R.string.string_networkUtils_noInternetConnection)
            )
            alertDialog.setPositiveButton(getString(R.string.string_ok)) { _, _ -> finish() }
            alertDialog.show()
        } else {
            flagTestReport = 1
            setupTestReportQuestionPagerAdapter(viewPagerPosition)
            if (countDownTestDurationTimerBool) {
                countDownTestDurationTimer?.cancel()
            }
        }
    }

    private fun setupTestReportQuestionPagerAdapter(viewPagerPosition: Int) {
        MockTestQuestionPagerAdapter = MockTestQuestionPagerAdapter(supportFragmentManager)
        (0..testReportList.size).forEach { position ->
            when {
                position != testReportList.size -> MockTestQuestionPagerAdapter.addFragment(
                    MockTestQuestionFragment.newInstanceReport(
                        testReportList[position],
                        position,
                        testReportList.size,
                        testSubscriptionId, flag,
                        true
                    ),
                    (position + 1).toString()
                )
            }

        }
        binding.viewPagerMockTest.adapter = MockTestQuestionPagerAdapter
        binding.mockTestTabs.setupWithViewPager(binding.viewPagerMockTest)

        binding.layoutMockTestCheckScore.root.hide()
        binding.progressBarMockTestDurationTimer.hide()
        //tbTopicFilters.hide()
        binding.llTopicFilters.hide()
        binding.tvSummary.hide()
        binding.progressLayout.hide()
        binding.viewMock.visibility = View.VISIBLE
        binding.reportTitle.show()
        binding.viewPagerBackground.show()
        binding.viewPagerMockTest.visibility = View.VISIBLE
        binding.mockTestTabs.visibility = View.VISIBLE
        binding.viewPagerMockTest.currentItem = viewPagerPosition
    }

    private fun setUpRecyclerView(
        mockTestSection: ArrayList<MockTestSectionData>,
        showResult: Boolean
    ) {
        binding.layoutMockTestCheckScore.recyclerViewSectionwiseData.layoutManager =
            LinearLayoutManager(this)
        binding.layoutMockTestCheckScore.recyclerViewSectionwiseData.adapter =
            MockTestSectionAdapter()
        //view.recyclerViewSectionwiseData.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        (binding.layoutMockTestCheckScore.recyclerViewSectionwiseData.adapter as MockTestSectionAdapter).updateData(
            mockTestSection, showResult
        )
        if (!showResult) {
            binding.layoutMockTestCheckScore.tvCorrectText.text = "Attempted"
            binding.layoutMockTestCheckScore.tvIncorrectText.text = "Unattempted"
            binding.layoutMockTestCheckScore.tvScoreText.text = ""
            binding.layoutMockTestCheckScore.tvSkippedText.text = ""
            (binding.layoutMockTestCheckScore.tvScoreText.layoutParams as? LinearLayout.LayoutParams)?.weight = 0f
            (binding.layoutMockTestCheckScore.tvScoreText.layoutParams as? LinearLayout.LayoutParams)?.weight = 0f
            binding.layoutMockTestCheckScore.llKey.weightSum = 3.5f
        }
    }

    private fun setTextViewInFlexbox(
        flexBox: FlexboxLayout,
        questionList: ArrayList<QuestionwiseResult>,
        startingIndex: Int
    ) {
        if (!isCreated) {
            (0 until questionList.size).forEach { i ->
                val tvQuestions = TextView(this)
                tvQuestions.setOnClickListener(this)
                tvQuestions.tag = Integer.valueOf(startingIndex + i)
                tvQuestions.text = (startingIndex + i + 1).toString()
                tvQuestions.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
                tvQuestions.gravity = Gravity.CENTER
                if (skipQuestionNumberFlexDraw.contains(questionList[i].questionbankId!!.toInt())) {
                    tvQuestions.background = Utils.getShape(
                        configData?.skippedColor ?: "#ffffff",
                        "#dcdcdc", 5f
                    )
                    tvQuestions.setPadding(
                        Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(9f).toInt(),
                        Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(9f).toInt()
                    )
                    tvQuestions.setTextColor(resources.getColor(R.color.black))
                }
                if (correctQuestionNumber.contains(questionList[i].questionbankId!!.toInt())) {
                    tvQuestions.background = Utils.getShape(
                        configData?.correctColor ?: "#ffffff",
                        configData?.correctColor ?: "#ffffff", 5f
                    )
                    tvQuestions.setPadding(
                        Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(9f).toInt(),
                        Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(9f).toInt()
                    )
                    tvQuestions.setTextColor(resources.getColor(R.color.black))
                }
                if (incorrectQuestionNumber.contains(questionList[i].questionbankId!!.toInt())) {
                    tvQuestions.background = Utils.getShape(
                        configData?.incorrectColor ?: "#ffffff",
                        configData?.incorrectColor ?: "#ffffff", 5f
                    )
                    tvQuestions.setPadding(
                        Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(9f).toInt(),
                        Utils.convertDpToPixel(15f).toInt(), Utils.convertDpToPixel(9f).toInt()
                    )
                    tvQuestions.setTextColor(resources.getColor(R.color.black))
                }
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
    }

    override fun onClick(v: View?) {
        val position = v!!.tag as Int
        val now = when {
            TrueTimeRx.isInitialized() -> TrueTimeRx.now()
            else -> Calendar.getInstance().time
        }
        when {
            mockTestQuestionViewModel.getTrueTimeDecision(
                testDetails.publishTime, testDetails.unpublishTime,
                now
            ) == Constants.TEST_OVER -> checkDetailTestReport(position)
            else -> toast(getString(R.string.test_not_start_till_time_end))
        }
    }

    private fun showAlertDialog(title: String, msg: String): AlertDialog.Builder {
        val alertDialog = AlertDialog.Builder(this@MockTestSectionActivity)
        alertDialog.setCancelable(false)
        alertDialog.setTitle(title)
        alertDialog.setMessage(msg)
        return alertDialog
    }

    private fun showNoInternetAlertDialog() {
        val alertDialog = showAlertDialog(
            getString(R.string.string_noInternetConnection),
            getString(R.string.string_networkUtils_noInternetConnection)
        )
        alertDialog.setPositiveButton(getString(R.string.string_ok)) { _, _ -> finish() }
        alertDialog.show()
    }

    private fun setupMockTestTopicFilterRecyclerView() {

        binding.tbTopicFilters.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                setTabBackgroud(tab, true)
                for (i in 0 until mockTestSectionDataList.size) {

                    val sectionStartingIndex = mockTestSectionDataList[i].sectionStartingIndex
                    if (binding.tbTopicFilters.selectedTabPosition == i) {
                        when {
                            sectionStartingIndex == testQuestionDataList.size + 1 -> {
                                binding.viewPagerMockTest.currentItem = sectionStartingIndex + 1

                            }
                            sectionStartingIndex <= testQuestionDataList.size -> binding.viewPagerMockTest.currentItem =
                                sectionStartingIndex
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

    }

    private fun setTabBackgroud(tab: TabLayout.Tab?, isSelected: Boolean) {
        for (i in 0 until mockTestSectionDataList.size - 1) {
            var tv = binding.tbTopicFilters.getTabAt(i)?.customView
            tv = if (tv == null) {
                TextView(this@MockTestSectionActivity)
            } else {
                tv as TextView
            }
//            tv.typeface = ResourcesCompat.getFont(this, R.font.lato)
            tv.text = mockTestSectionDataList[i].sectionTitle
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            tv.gravity = Gravity.CENTER
            val topPadding: Int = ViewUtils.dpToPx(4f, this@MockTestSectionActivity).toInt()
            val leftPadding: Int = ViewUtils.dpToPx(6f, this@MockTestSectionActivity).toInt()
            tv.setPadding(leftPadding, topPadding, leftPadding, topPadding)
            if (binding.tbTopicFilters.selectedTabPosition == i) {
                tv.background = getDrawable(R.drawable.bg_mock_test_tab_selected)
                tv.setTextColor(ContextCompat.getColor(this@MockTestSectionActivity, R.color.white))
            } else {
                tv.background = getDrawable(R.drawable.bg_mock_test_tab_unselected)
                tv.setTextColor(ContextCompat.getColor(this@MockTestSectionActivity, R.color.black))
                tv.setTextSize(
                    TypedValue.COMPLEX_UNIT_PX,
                    ViewUtils.dpToPx(12f, this@MockTestSectionActivity)
                )
            }
            binding.tbTopicFilters.getTabAt(i)?.customView = tv
        }
        if (binding.tbTopicFilters.selectedTabPosition != mockTestSectionDataList.size - 1) {
            binding.tvSummary.background = null
            binding.tvSummary.setTextColor(
                ContextCompat.getColor(
                    this@MockTestSectionActivity,
                    R.color.black
                )
            )
        } else {
            setSummaryTabBackground()
        }
    }

    fun removeBackground() {
        for (i in 0 until mockTestSectionDataList.size - 1) {
            var tv = binding.tbTopicFilters.getTabAt(i)?.customView
            tv = if (tv == null) {
                TextView(this@MockTestSectionActivity)
            } else {
                tv as TextView
            }
            tv.background = getDrawable(R.drawable.bg_mock_test_tab_unselected)
            tv.setTextColor(ContextCompat.getColor(this@MockTestSectionActivity, R.color.black))
            binding.tbTopicFilters.getTabAt(i)?.customView = tv
        }
    }

    override fun provideViewBinding(): ActivityMockTestQuestionBinding {
        return ActivityMockTestQuestionBinding.inflate(LayoutInflater.from(this))
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MockTestQuestionViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        statusbarColor(this, R.color.white_20)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        init()
        setupDataFromIntent()
        setupListener()
        decideVisibleLayout()
        setupMockTestTopicFilterRecyclerView()
    }

}
