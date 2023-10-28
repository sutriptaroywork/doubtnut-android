package com.doubtnutapp.ui.test

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager.widget.ViewPager
import com.doubtnut.analytics.EventConstants
import com.doubtnut.core.utils.toast
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.*
import com.doubtnutapp.base.extension.observeK
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.databinding.ActivityTestQuestionBinding
import com.doubtnutapp.ui.base.BaseBindingActivity
import com.doubtnutapp.ui.errorRequest.BadRequestDialog
import com.doubtnutapp.ui.errorRequest.NetworkErrorDialog
import com.doubtnutapp.utils.NetworkUtils
import com.doubtnutapp.utils.UserUtil.getStudentId
import com.google.android.flexbox.FlexboxLayout
import com.instacart.library.truetime.TrueTimeRx
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.fragment_test_question_item.*
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by akshaynandwana on
 * 18, December, 2018
 **/
class TestQuestionActivity :
    BaseBindingActivity<TestQuestionViewModel, ActivityTestQuestionBinding>(),
    TestQuestionFragment.OnFragmentInteractionListener,
    TestSummaryReportFragment.OnFragmentInteractionListenerTestSubmission,
    HasAndroidInjector,
    View.OnClickListener {

    private lateinit var testViewModel: TestViewModel
    private lateinit var testQuestionPagerAdapter: TestQuestionPagerAdapter

    private var testQuestionDataList: MutableList<TestQuestionDataOptions> = mutableListOf()

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

    private var startFromLibrary: Boolean = false

    private var isCreated: Boolean = false

    var isEligible: Int = 1

    private var skipQuestionNumber: HashMap<Int, Boolean>? = HashMap()
    private var skipQuestionNumberFlexDraw: HashSet<Int> = HashSet()
    private var correctQuestionNumber: HashSet<Int> = HashSet()
    private var incorrectQuestionNumber: HashSet<Int> = HashSet()

    private var oldPageSelected = 0

    companion object {
        const val TAG = "TestQuestionActivity"

        fun newIntent(
            context: Context, testDetails: TestDetails, flag: String,
            testSubscriptionId: Int,
            fromLibrary: Boolean
        ): Intent {
            val testQuestionIntent = Intent(context, TestQuestionActivity::class.java)
            testQuestionIntent.putExtra(Constants.TEST_DETAILS_OBJECT, testDetails)
            testQuestionIntent.putExtra(Constants.TEST_TRUE_TIME_FLAG, flag)
            testQuestionIntent.putExtra(Constants.TEST_SUBSCRIPTION_ID, testSubscriptionId)
            testQuestionIntent.putExtra(Constants.FROM_LIBRARY, fromLibrary)

            return testQuestionIntent
        }
    }

    override fun provideViewBinding(): ActivityTestQuestionBinding {
        return ActivityTestQuestionBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): TestQuestionViewModel {
        testViewModel = viewModelProvider(viewModelFactory)
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        init()
        setupDataFromIntent()
        setupListener()
        setupLivedataObservers()
        if (testSubscriptionId != 0) {
            decideVisibleLayout()
        } else {
            viewModel.subscribeForTest(testDetails.testId)
        }
    }

    override fun finish() {
        if (startFromLibrary) {
            this.setResult(Activity.RESULT_OK)
        }
        super.finish()
    }

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
            (binding.viewPagerTest.visibility == View.VISIBLE) && (flagTestReport == 0)
                    && (flag == Constants.TEST_OVER) -> {
                val alertDialog: AlertDialog.Builder =
                    showAlertDialog(
                        getString(R.string.string_leaving_test),
                        getString(R.string.string_test_over_end_back_press_dialog_msg)
                    )
                alertDialog.setPositiveButton(getString(R.string.string_yes)) { dialog, _ ->
                    if (countDownTestDurationTimerBool) {
                        countDownTestDurationTimer?.cancel()
                    }
                    super.onBackPressed()
                    dialog.dismiss()

                }
                alertDialog.setNegativeButton(getString(R.string.string_no)) { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
            }
            (binding.viewPagerTest.visibility == View.VISIBLE) && (flagTestReport == 0) -> {
                val alertDialog: AlertDialog.Builder =
                    showAlertDialog(
                        getString(R.string.string_leaving_test),
                        getString(R.string.string_test_over_end_back_press_dialog_msg)
                    )
                alertDialog.setPositiveButton(getString(R.string.string_yes)) { dialog, _ ->
                    if (countDownTestDurationTimerBool) countDownTestDurationTimer?.cancel()
                    if (countDownTimerTestNotifyBool) countDownTimerTestNotify?.cancel()
                    super.onBackPressed()
                }
                alertDialog.setNegativeButton(getString(R.string.string_no)) { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()

            }
            (binding.viewPagerTest.visibility == View.VISIBLE) && (flagTestReport == 1) -> {
                val alertDialog: AlertDialog.Builder =
                    showAlertDialog(
                        getString(R.string.string_close_report),
                        getString(R.string.string_close_report_dialog_msg)
                    )
                alertDialog.setPositiveButton(getString(R.string.string_yes)) { dialog, _ ->
                    dialog.dismiss()
                    binding.viewPagerTest.hide()
                    binding.layoutTestCheckScore.root.visibility = View.VISIBLE
                }
                alertDialog.setNegativeButton(getString(R.string.string_no)) { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()

            }
            else -> super.onBackPressed()
        }
    }

    override fun androidInjector(): AndroidInjector<Any> = dispatchingAndroidInjector

    override fun onFragmentInteractionCallSubmit(
        testId: Int,
        actionType: String,
        isReview: Int,
        optionCode: String,
        sectionCode: String,
        testSubcriptionId: Int,
        questionbankId: Int,
        questionType: String,
        isEligible: String,
        timeTaken: Int,
        subjectCode: String,
        chapterCode: String,
        subtopicCode: String,
        classCode: String,
        mcCode: String,
        position: Int
    ) {
        testViewModel.getTestResponse(
            applicationContext,
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
                        progressBar2.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        progressBar2.visibility = View.GONE
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        progressBar2.visibility = View.GONE
                        apiErrorToast(response.e)
                    }
                    is Outcome.BadRequest -> {
                        progressBar2.visibility = View.GONE
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(supportFragmentManager, "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        progressBar2.visibility = View.GONE
                        onFragmentInteraction(position + 1, false)
                    }
                }

            })

    }

    fun onFragmentInteractionCallSubmitNoNext(
        testId: Int, actionType: String, isReview: Int,
        optionCode: String, sectionCode: String,
        testSubcriptionId: Int, questionbankId: Int, questionType: String,
        isEligible: String, timeTaken: Int,
        subjectCode: String, chapterCode: String, subtopicCode: String,
        classCode: String, mcCode: String, position: Int
    ) {
        testViewModel.getTestResponse(
            applicationContext,
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
                        progressBar2.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        progressBar2.visibility = View.GONE
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        progressBar2.visibility = View.GONE
                        toast(getString(R.string.api_error))
                    }
                    is Outcome.BadRequest -> {
                        progressBar2.visibility = View.GONE
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(supportFragmentManager, "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        progressBar2.visibility = View.GONE
                        skipQuestionNumber?.put(position + 1, false)
                    }
                }

            })

    }

    override fun onFragmentInteraction(position: Int, isSkip: Boolean) {
        skipQuestionNumber?.put(position, isSkip)
        when {
            position >= testQuestionDataList.size -> {
                binding.viewPagerTest.currentItem = position + 1

            }
            else -> binding.viewPagerTest.currentItem = position
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
                alertDialog.setPositiveButton(getString(R.string.string_ok)) { dialog, _ ->
                    dialog.dismiss()
                    binding.viewPagerTest.hide()
                    binding.tvTestTimer.hide()
                    binding.view6.hide()
                    displayTestReportUI()
                }
                alertDialog.setNegativeButton(getString(R.string.string_notificationEducation_cancel)) { dialog, _ ->
                    dialog.dismiss()
                }
                alertDialog.show()
            }
            else -> binding.viewPagerTest.currentItem = position
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
        binding.viewPagerTest.hide()
        binding.tvTestTimer.hide()
        binding.progressBarTestDurationTimer.hide()
        binding.view6.hide()
        displayTestReportUI()
    }

    override fun onFragmentInteractionGetIsEligible(): Int {
        return isEligible
    }

    override fun getSkipQuestionNumber(): HashMap<Int, Boolean>? {
        return skipQuestionNumber
    }

    override fun summaryDotClickChangeViewPagerPosisiton(position: Int) {
        binding.viewPagerTest.currentItem = position
    }

    private fun init() {
        testQuestionPagerAdapter = TestQuestionPagerAdapter(supportFragmentManager)
    }

    private fun setupListener() {
        binding.layoutTestStarted.btnStartQuiz.setOnClickListener {
            when {
                !NetworkUtils.isConnected(this) -> showNoInternetAlertDialog()
                else -> {
                    if (defaultPrefs(this@TestQuestionActivity).getString(
                            Constants.STUDENT_LOGIN,
                            ""
                        ) == ""
                    ) {
                        val dialog = LoginDialog.newInstance()
                        dialog.show(supportFragmentManager, "LoginDialog")
                    } else {
                        getTestQuestionsandOptions()
                        sendEvent(EventConstants.EVENT_NAME_START_QUIZ)

                    }
                    if (countDownTestTimerBool) countDownTestTimer?.cancel()
                }
            }
        }
        binding.layoutTestOver.btnQuizOverStart.setOnClickListener {
            when {
                !NetworkUtils.isConnected(this) -> showNoInternetAlertDialog()
                else -> {
                    getTestQuestionsandOptions()
                    sendEvent(EventConstants.EVENT_NAME_START_QUIZ)

                }

            }
        }

        binding.layoutTestOver.imageViewDailyquizBackOver.setOnClickListener { this.onBackPressed() }
        binding.layoutTestNotStarted.imageViewDailyquizBackNotStarted.setOnClickListener {
            checkCountDownTestTimeandBackPress()
        }
        binding.layoutTestStarted.imageViewDailyquizBackStarted.setOnClickListener {
            checkCountDownTestTimeandBackPress()
        }
        binding.layoutTestCheckScore.imageViewDailytestBackReport.setOnClickListener { this.onBackPressed() }

        binding.layoutTestCheckScore.btnViewSolution.setOnClickListener {
            viewModel.publishQuizViewAnswerEvent(testDetails.testId.toString())
            val now = when {
                TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                else -> Calendar.getInstance().time
            }
            when {
                viewModel.getTrueTimeDecision(
                    testDetails.publishTime, testDetails.unpublishTime, now
                ) ==
                        Constants.TEST_OVER -> checkDetailTestReport(0)
                else -> toast(getString(R.string.test_not_start_till_time_end))
            }
        }

        binding.layoutTestCheckScore.includeScore.itemLeftScrollToday.setOnClickListener {

            val recyclerView = binding.layoutTestCheckScore.includeScore.recyclerViewWinningNow
            val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
            when {
                linearLayoutManager.findLastVisibleItemPosition() > 0 ->
                    recyclerView.smoothScrollToPosition(
                        linearLayoutManager.findLastVisibleItemPosition() - 1
                    )
                else -> binding.layoutTestCheckScore.includeScore.recyclerViewWinningNow.smoothScrollToPosition(
                    0
                )
            }
        }
        binding.layoutTestCheckScore.includeScore.itemRightScrollToday.setOnClickListener {
            val recyclerView = binding.layoutTestCheckScore.includeScore.recyclerViewWinningNow
            val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
            recyclerView.smoothScrollToPosition(
                linearLayoutManager.findLastVisibleItemPosition() + 1
            )
        }

        binding.viewPagerTest.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
                        val fragmentInstance = testQuestionPagerAdapter.instantiateItem(
                            binding.viewPagerTest,
                            oldPageSelected
                        ) as? TestQuestionFragment

                        if (fragmentInstance != null && fragmentInstance.oldCheckedOptionList != fragmentInstance.checkedOptionList) {

                            val selectedOptions =
                                (Arrays.toString(fragmentInstance.checkedOptionList.toArray())
                                    .replace("[", "")
                                    .replace("]", "").replace(" ", "")).trim()

                            fragmentInstance.oldCheckedOptionList =
                                fragmentInstance.checkedOptionList

                            val isEligibleGet = when (flag) {
                                Constants.TEST_OVER -> 0.toString()
                                else -> isEligible.toString()
                            }

                            val timeTake = when {
                                TrueTimeRx.isInitialized() -> TrueTimeRx.now()
                                else -> Calendar.getInstance().time
                            }
                            val timeTaken = (timeTake.time / 1000).toInt()

                            onFragmentInteractionCallSubmitNoNext(
                                fragmentInstance.testQuestionData!!.testId,
                                "ANS", 0,
                                selectedOptions, fragmentInstance.testQuestionData!!.sectionCode!!,
                                fragmentInstance.testSubscriptionId,
                                fragmentInstance.testQuestionData!!.questionbankId!!.toInt(),
                                fragmentInstance.testQuestionData!!.type!!,
                                isEligibleGet,
                                timeTaken,
                                fragmentInstance.testQuestionData!!.subjectCode!!,
                                fragmentInstance.testQuestionData!!.chapterCode ?: "",
                                fragmentInstance.testQuestionData!!.subtopicCode ?: "",
                                fragmentInstance.testQuestionData!!.classCode ?: "",
                                fragmentInstance.testQuestionData!!.mcCode
                                    ?: "", fragmentInstance.position
                            )

                        }
                    }

                    oldPageSelected = position
                }

                //Log.d("CheckData", " skipQuestionNumber - "+skipQuestionNumber.toString())
            }
        })
    }

    private fun setupDataFromIntent() {
        intent?.let {
            testDetails = it.getParcelableExtra<TestDetails>(Constants.TEST_DETAILS_OBJECT)!!
            flag = it.getStringExtra(Constants.TEST_TRUE_TIME_FLAG).orEmpty()
            testSubscriptionId = it.getIntExtra(Constants.TEST_SUBSCRIPTION_ID, 0)
            startFromLibrary = it.getBooleanExtra(Constants.FROM_LIBRARY, false)
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
        binding.layoutTestOver.root.visibility = View.VISIBLE
        showTestRules(binding.layoutTestOver.root)
    }

    private fun displayTestUpcomingUI() {
        binding.layoutTestNotStarted.root.visibility = View.VISIBLE
        showTestRules(binding.layoutTestNotStarted.root)
        binding.layoutTestNotStarted.layoutQuizBottomTimer.tvQuizStartOverHeader.text =
            getString(R.string.string_next_test_starts)
        setupTestTimer(
            viewModel.getTestStartBeforeTimeDifferenceLong(testDetails.publishTime),
            binding.layoutTestNotStarted.root,
            binding.layoutTestNotStarted.layoutQuizBottomTimer.tvQuizStartsInTimer
        )
    }

    private fun displayTestActiveUI() {
        binding.layoutTestStarted.root.visibility = View.VISIBLE
        showTestRules(binding.layoutTestStarted.root)
        binding.layoutTestStarted.include2.tvQuizStartOverHeader.text = getString(R.string.string_test_ends)
        setupTestTimer(
            viewModel.getTestStartAfterTimeDifferenceLong(testDetails.unpublishTime),
            binding.layoutTestStarted.root,
            binding.layoutTestStarted.include2.tvQuizStartsInTimer
        )
    }

    private fun displayTestReportUI() {
        viewModel.getTestResult(testSubscriptionId)
    }

    private fun showTestRules(layout_view: View) {
        viewModel.getTestRules(
            testDetails.ruleId
                ?: 0
        ).observe(this, { response ->
            when (response) {
                is Outcome.Progress -> {
                    binding.progressBarTestQuestion.visibility = View.VISIBLE
                }
                is Outcome.Failure -> {
                    binding.progressBarTestQuestion.hide()
                    val dialog = NetworkErrorDialog.newInstance()
                    dialog.show(supportFragmentManager, "NetworkErrorDialog")
                }
                is Outcome.ApiError -> {
                    binding.progressBarTestQuestion.hide()
                    toast(getString(R.string.api_error))
                }
                is Outcome.BadRequest -> {
                    binding.progressBarTestQuestion.hide()
                    val dialog = BadRequestDialog.newInstance("unauthorized")
                    dialog.show(supportFragmentManager, "BadRequestDialog")
                }
                is Outcome.Success -> {
                    binding.progressBarTestQuestion.hide()

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
                        binding.layoutTestOver.root -> {
                            binding.layoutTestOver.btnQuizOverStart.text =
                                getString(R.string.string_start_test)
                            binding.layoutTestOver.tvRulesOver.text = builder
                        }
                        binding.layoutTestNotStarted.root -> {
                            binding.layoutTestNotStarted.tvRules.text = builder
                        }
                        binding.layoutTestStarted.root -> {
                            binding.layoutTestStarted.btnStartQuiz.text = getString(R.string.string_start_test)
                            binding.layoutTestStarted.tvRulesStarted.text = builder
                        }
                    }
                }
            }
        })
    }

    private fun setupTestTimer(time: Long, view: View, textView: TextView) {
        countDownTestTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTestTimerBool = true
                textView.text = getString(
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
            }

            override fun onFinish() {
                countDownTestTimerBool = false
                textView.text = getString(R.string.string_quiz_time_over)
                when (view) {
                    binding.layoutTestNotStarted.root -> {
                        binding.layoutTestNotStarted.root.hide()
                        displayTestActiveUI()
                    }
                    else -> {
                        val alertDialog = showAlertDialog(
                            getString(R.string.string_test_over_dialog_title),
                            getString(R.string.string_test_over_no_points_dialog_msg)
                        )
                        alertDialog.setPositiveButton(getString(R.string.string_ok)) { dialog, _ ->
                            dialog.dismiss()
                            displayTestOverUI()
                        }
                        alertDialog.show()
                    }
                }
            }
        }
        countDownTestTimer?.start()
    }

    private fun setupTestDurationTimer() {
        val time: Long = TimeUnit.MINUTES.toMillis(testDetails.durationInMin?.toLong() ?: 0L)
        binding.progressBarTestDurationTimer.max = time.toInt()
        countDownTestDurationTimer = object : CountDownTimer(time, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                countDownTestDurationTimerBool = true
                binding.tvTestTimer.text = getString(
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
                binding.progressBarTestDurationTimer.progress = millisUntilFinished.toInt()
            }

            override fun onFinish() {
                countDownTestDurationTimerBool = false
                binding.tvTestTimer.text = getString(R.string.string_test_duration_timer_finish)
                binding.progressBarTestDurationTimer.progress = 0
                val alertDialog = showAlertDialog(
                    getString(R.string.string_test_over_dialog_title),
                    getString(R.string.string_test_over_no_points_dialog_msg)
                )
                alertDialog.setPositiveButton(getString(R.string.string_ok)) { dialog, _ -> dialog.dismiss() }
                if (!isFinishing) alertDialog.show()
                isEligible = 0
            }
        }
        countDownTestDurationTimer?.start()
    }

    private fun getTestQuestionsandOptions() {
        viewModel.publishDailyQuizTopicSelectionEvent(testDetails.testId.toString())
        viewModel.getTestQuestions(testDetails.testId)
            .observe(this, { response ->
                when (response) {
                    is Outcome.Progress -> {
                        binding.progressBarTestQuestion.visibility = View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        Log.d("QuestionsCheck", response.toString())
                        binding.progressBarTestQuestion.hide()
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        binding.progressBarTestQuestion.hide()
                        toast(getString(R.string.api_error))
                    }
                    is Outcome.BadRequest -> {
                        binding.progressBarTestQuestion.hide()
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(supportFragmentManager, "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        binding.progressBarTestQuestion.hide()
                        testQuestionDataList.addAll(response.data.data)
                        setupTestQuestionPagerAdapter()
                    }
                }
            })
    }

    private fun setupTestQuestionPagerAdapter() {
        (0..testQuestionDataList.size).forEach { position ->
            when {
                position != testQuestionDataList.size -> testQuestionPagerAdapter.addFragment(
                    TestQuestionFragment.newInstance(
                        testQuestionDataList[position],
                        position,
                        testQuestionDataList.size,
                        testSubscriptionId, flag,
                        false
                    ),
                    (position + 1).toString()
                )
                else -> testQuestionPagerAdapter.addFragment(
                    TestSummaryReportFragment.newInstance(
                        testQuestionDataList.size,
                        testSubscriptionId,
                        testDetails.testId
                    ),
                    getString(R.string.string_test_summary_tab)
                )
            }
        }
        binding.viewPagerTest.adapter = testQuestionPagerAdapter
        binding.testTabs.setupWithViewPager(binding.viewPagerTest)

        when {
            binding.layoutTestOver.root.visibility == View.VISIBLE -> binding.layoutTestOver.root.hide()
            else -> {
                binding.layoutTestStarted.root.hide()
                if (viewModel.getTestStartAfterTimeDifferenceLong(testDetails.unpublishTime) <
                    TimeUnit.MINUTES.toMillis(testDetails.durationInMin?.toLong() ?: 0L)
                ) {
                    setupTestUnpublishNotifyTimer(
                        viewModel
                            .getTestStartAfterTimeDifferenceLong(testDetails.unpublishTime)
                    )
                }
            }
        }
        binding.tvTestTimer.visibility = View.VISIBLE
        binding.view6.visibility = View.VISIBLE
        binding.progressBarTestDurationTimer.visibility = View.VISIBLE
        binding.viewPagerTest.visibility = View.VISIBLE
        binding.testTabs.visibility = View.VISIBLE
        setupTestDurationTimer()
    }

    private fun checkCountDownTestTimeandBackPress() {
        if (countDownTestTimerBool) {
            countDownTestTimer?.cancel()
        }
        this.onBackPressed()
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
                    alertDialog.setPositiveButton(getString(R.string.string_ok)) { dialog, _ ->
                        dialog.dismiss()
                    }
                    alertDialog.show()
                }
                isEligible = 0
            }
        }
        countDownTimerTestNotify?.start()
    }

    private fun checkDetailTestReport(viewPagerPosition: Int) {
        if (!NetworkUtils.isConnected(this)) {
            val alertDialog = showAlertDialog(
                getString(R.string.string_quiz_no_report),
                getString(R.string.string_networkUtils_noInternetConnection)
            )
            alertDialog.show()
            alertDialog.setPositiveButton(getString(R.string.string_ok)) { dialog, _ ->
                dialog.dismiss()
                finish()

            }
        } else {
            flagTestReport = 1
            setupTestReportQuestionPagerAdapter(viewPagerPosition)
            if (countDownTestDurationTimerBool) {
                countDownTestDurationTimer?.cancel()
            }
        }
    }

    private fun setupTestReportQuestionPagerAdapter(viewPagerPosition: Int) {
        testQuestionPagerAdapter = TestQuestionPagerAdapter(supportFragmentManager)
        (0..testReportList.size).forEach { position ->
            when {
                position != testReportList.size -> testQuestionPagerAdapter.addFragment(
                    TestQuestionFragment.newInstanceReport(
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
        binding.viewPagerTest.adapter = testQuestionPagerAdapter
        binding.testTabs.setupWithViewPager(binding.viewPagerTest)

        binding.layoutTestCheckScore.root.hide()
        binding.progressBarTestDurationTimer.hide()
        binding.tvTestTimer.visibility = View.VISIBLE
        binding.view6.visibility = View.VISIBLE
        binding.tvTestTimer.text = getString(R.string.string_test_report_heading)

        binding.viewPagerTest.visibility = View.VISIBLE
        binding.testTabs.visibility = View.VISIBLE
        binding.viewPagerTest.currentItem = viewPagerPosition
    }

    private fun setUpRecyclerView(quizLeaderList: List<TestLeaderboard>) {
        binding.layoutTestCheckScore.includeScore.recyclerViewWinningNow.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.HORIZONTAL, false)
        binding.layoutTestCheckScore.includeScore.recyclerViewWinningNow.adapter = TestTopWinnersAdapter(this, quizLeaderList)
        binding.layoutTestCheckScore.includeScore.recyclerViewWinningNow.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
    }

    private fun setTextViewInFlexbox(test_size: Int) {
        if (!isCreated) {
            isCreated = true
            (0 until test_size).forEach { i ->
                val tvQuestions = TextView(this)
                tvQuestions.setOnClickListener(this)
                tvQuestions.tag = Integer.valueOf(i)
                tvQuestions.text = (i + 1).toString()
                tvQuestions.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16.toFloat())
                tvQuestions.gravity = Gravity.CENTER
                if (skipQuestionNumberFlexDraw.contains(testReportList[i].questionbankId!!.toInt())) {
                    tvQuestions.background =
                        resources.getDrawable(R.drawable.circle_test_question_attempted)
                    tvQuestions.setTextColor(resources.getColor(R.color.black))
                }
                if (correctQuestionNumber.contains(testReportList[i].questionbankId!!.toInt())) {
                    tvQuestions.background =
                        resources.getDrawable(R.drawable.circle_test_question_correct)
                    tvQuestions.setTextColor(resources.getColor(R.color.white))
                }
                if (incorrectQuestionNumber.contains(testReportList[i].questionbankId!!.toInt())) {
                    tvQuestions.background =
                        resources.getDrawable(R.drawable.circle_test_question_incorrect)
                    tvQuestions.setTextColor(resources.getColor(R.color.white))
                }
                val lpRight = FlexboxLayout.LayoutParams(
                    FlexboxLayout.LayoutParams.WRAP_CONTENT,
                    FlexboxLayout.LayoutParams.WRAP_CONTENT
                )
                tvQuestions.layoutParams = lpRight
                val lp = tvQuestions.layoutParams as FlexboxLayout.LayoutParams
                lp.setMargins(10, 10, 10, 10)
                tvQuestions.layoutParams = lp
                binding.layoutTestCheckScore.flexboxlayoutReport.addView(tvQuestions)
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
            viewModel.getTrueTimeDecision(
                testDetails.publishTime, testDetails.unpublishTime,
                now
            ) == Constants.TEST_OVER -> checkDetailTestReport(position)
            else -> toast(getString(R.string.test_not_start_till_time_end))
        }
    }

    private fun setupLivedataObservers() {
        viewModel.testResultLiveData.observeK(
            this,
            this::onTestResultSuccess,
            this::onApiError,
            this::onBadReqError,
            this::onReqFailure,
            this::updateProgressState
        )

        viewModel.testSubscriptionIdLiveData.observeK(
            this,
            this::onTestSubscriptionSuccess,
            this::onApiError,
            this::onBadReqError,
            this::onReqFailure,
            this::updateProgressState
        )

    }

    fun onTestSubscriptionSuccess(testSubscriptionId: Int?) {
        if (testSubscriptionId != null) {
            this.testSubscriptionId = testSubscriptionId
            decideVisibleLayout()
        } else {
            toast(getString(R.string.error_failedToSubscribe))
        }
    }

    fun onTestResultSuccess(response: ApiResponse<TestResult>) {
        binding.progressBarTestQuestion.hide()
        testReportList.addAll(response.data.questionwiseResult)
        binding.layoutTestCheckScore.root.visibility = View.VISIBLE

        skipQuestionNumberFlexDraw.clear()
        response.data.reportCard.skipped?.let {
            if (it.isNotEmpty()) {
                skipQuestionNumberFlexDraw = it.split(",").map { it.trim() }
                    .map { it.toInt() }.toHashSet()
            }
        }

        response.data.reportCard.correct?.let {
            if (it.isNotEmpty()) {
                correctQuestionNumber = it.split(",").map { it.trim() }
                    .map { it.toInt() }.toHashSet()
            }
        }

        response.data.reportCard.incorrect?.let {
            if (it.isNotEmpty()) {
                incorrectQuestionNumber = it.split(",").map { it.trim() }
                    .map { it.toInt() }.toHashSet()
            }
        }

        binding.layoutTestCheckScore.tvIncorrect.text = incorrectQuestionNumber.size.toString()
        binding.layoutTestCheckScore.tvCorrect.text = correctQuestionNumber.size.toString()
        binding.layoutTestCheckScore.tvSkipped.text = skipQuestionNumberFlexDraw.size.toString()
        binding.layoutTestCheckScore.tvScore.text = response.data.reportCard.totalScore.toString()
        setTextViewInFlexbox(response.data.questionwiseResult.size)

        viewModel.getTestLeaderboard(testDetails.testId)
            .observe(this, { responseLeaderboard ->
                when (responseLeaderboard) {
                    is Outcome.Progress -> {
                        binding.layoutTestCheckScore.includeScore.progressBarQuizTopWinnersLoadingScore.visibility =
                            View.VISIBLE
                    }
                    is Outcome.Failure -> {
                        binding.layoutTestCheckScore.includeScore.progressBarQuizTopWinnersLoadingScore.hide()
                        val dialog = NetworkErrorDialog.newInstance()
                        dialog.show(supportFragmentManager, "NetworkErrorDialog")
                    }
                    is Outcome.ApiError -> {
                        binding.layoutTestCheckScore.includeScore.progressBarQuizTopWinnersLoadingScore.hide()
                        toast(getString(R.string.api_error))
                    }
                    is Outcome.BadRequest -> {
                        binding.layoutTestCheckScore.includeScore.progressBarQuizTopWinnersLoadingScore.hide()
                        val dialog = BadRequestDialog.newInstance("unauthorized")
                        dialog.show(supportFragmentManager, "BadRequestDialog")
                    }
                    is Outcome.Success -> {
                        binding.layoutTestCheckScore.includeScore.progressBarQuizTopWinnersLoadingScore.hide()
                        if (responseLeaderboard.data.data.isNotEmpty()) {
                            binding.layoutTestCheckScore.includeScore.root.visibility = View.VISIBLE
                            setUpRecyclerView(
                                responseLeaderboard.data.data
                            )
                        }
                    }

                }
            })
    }

    fun onBadReqError() {
        binding.progressBarTestQuestion.hide()
        val dialog = BadRequestDialog.newInstance("unauthorized")
        dialog.show(supportFragmentManager, "BadRequestDialog")
    }

    private fun onApiError(e: Throwable) {
        binding.progressBarTestQuestion.hide()
        apiErrorToast(e)
    }

    fun onReqFailure() {
        binding.progressBarTestQuestion.hide()
        val dialog = NetworkErrorDialog.newInstance()
        dialog.show(supportFragmentManager, "NetworkErrorDialog")
    }

    fun updateProgressState(loading: Boolean) {
        binding.progressBarTestQuestion.setVisibleState(loading)
    }

    private fun showAlertDialog(title: String, msg: String): AlertDialog.Builder {

        val alertDialog = AlertDialog.Builder(this@TestQuestionActivity)
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
        alertDialog.setPositiveButton(getString(R.string.string_ok)) { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        alertDialog.show()
    }

    fun onLoginDone() {
        getTestQuestionsandOptions()
        sendEvent(EventConstants.EVENT_NAME_START_QUIZ)
    }

    private fun sendEvent(eventName: String) {
        this@TestQuestionActivity.apply {
            (this@TestQuestionActivity.applicationContext as DoubtnutApp).getEventTracker()
                .addEventNames(eventName)
                .addNetworkState(NetworkUtils.isConnected(this@TestQuestionActivity).toString())
                .addStudentId(getStudentId())
                .addScreenName(EventConstants.PAGE_QUIZ)
                .track()
        }
    }

}
