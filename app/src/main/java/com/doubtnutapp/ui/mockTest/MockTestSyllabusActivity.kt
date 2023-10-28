package com.doubtnutapp.ui.mockTest

import android.content.Intent
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.doubtnut.core.utils.viewModelProvider
import com.doubtnutapp.Constants
import com.doubtnutapp.R
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.databinding.ActivityMockTestsSyllabusBinding
import com.doubtnutapp.libraryhome.mocktest.viewmodel.MockTestSyllabusViewModel
import com.doubtnutapp.ui.base.BaseBindingActivity
import dagger.android.AndroidInjector
import dagger.android.HasAndroidInjector


class MockTestSyllabusActivity :
    BaseBindingActivity<MockTestSyllabusViewModel, ActivityMockTestsSyllabusBinding>(),
    HasAndroidInjector {

    private lateinit var testDetails: TestDetails
    private var flag: String = Constants.TEST_OVER
    private var mTitle: String = ""
    private var testSubscriptionId: Int = 0

    private fun getDataFromViewModel() {
        val description: String = testDetails.description ?: ""
        viewModel.getSyllabusLiveData(description).observe(this, Observer {
            setSyllabus(it.map, it.marksList, it.subjectList)
        })
    }

    private fun initUI() {
        binding.tvTestName.text = testDetails.title
        binding.tvTitle.text = mTitle

        if (testDetails.durationInMin != 0) {
            binding.tvDuration.text =
                testDetails.durationInMin.toString() + getString(R.string.minutes_text)
        } else {
            binding.tvDuration.visibility = INVISIBLE
        }
        if (testDetails.totalQuestions != 0) {
            binding.tvQuestionsValue.text = testDetails.totalQuestions?.toString()
        } else {
            binding.tvQuestionsValue.visibility = INVISIBLE
        }
    }

    private fun setupDataFromIntent() {
        intent?.let {
            testDetails = it.getParcelableExtra<TestDetails>(Constants.TEST_DETAILS_OBJECT)!!
            flag = it.getStringExtra(Constants.TEST_TRUE_TIME_FLAG).orEmpty()
            testSubscriptionId = it.getIntExtra(Constants.TEST_SUBSCRIPTION_ID, 0)
            mTitle = it.getStringExtra(Constants.SYLLABUS_TITLE).orEmpty()
        }
    }

    private fun initListeners() {
        binding.btnAttempt.setOnClickListener(OnClickListener {
            //viewModel
            startTestQuestionActivity()
        })
        binding.ivBack.setOnClickListener(OnClickListener {
            onBackPressed()
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun setSyllabus(map: HashMap<Int, List<String>>, marksList: ArrayList<String>, subjectList: ArrayList<String>) {
        var totalMarks = 0
        for (i in subjectList.indices) {
            val linearLayout: ConstraintLayout = layoutInflater.inflate(R.layout.item_mock_test_syllabus_topic, null) as ConstraintLayout
            val subjectTv: TextView = linearLayout.findViewById(R.id.tv_subject)
            val marksTv: TextView = linearLayout.findViewById(R.id.tv_marks)

            subjectTv.text = subjectList[i]
            if (i <= marksList.size && marksList[i].toInt() != 0) {
                marksTv.text = marksList[i] + getString(R.string.marks_text_syllabus)
                totalMarks += marksList[i].toInt()
            }
            if (i == 0) {
                val params = subjectTv.layoutParams as ConstraintLayout.LayoutParams
                params.topMargin = 0
                params.leftMargin = 16
                linearLayout.layoutParams = params
            }
            binding.llSyllabus.addView(linearLayout)
            if (map.containsKey(i)) {
                val topicList = map.get(i)
                if (!topicList.isNullOrEmpty()) {
                    for (j in 1 until topicList.size) {
                        val layout: ConstraintLayout = layoutInflater.inflate(
                            R.layout.item_mock_test_syllabus_subject,
                            null
                        ) as ConstraintLayout
                        val topicTv: TextView = layout.findViewById(R.id.tv_topic)
                        topicTv.text = topicList[j]
                        binding.llSyllabus.addView(layout)
                    }
                }
            }
        }
        if (totalMarks != 0) {
            binding.tvMarksValue.text = totalMarks.toString()
        } else {
            binding.tvMarks.visibility = INVISIBLE
        }
    }

    companion object {
        private const val TAG = "MockTestSyllabusActivity"

        fun newIntent(
            listActivity: MockTestListActivity, testDetails: TestDetails, flag: String,
            testSubscriptionId: Int, title: String
        ): Intent {
            val testQuestionIntent = Intent(listActivity, MockTestSyllabusActivity::class.java)
            testQuestionIntent.putExtra(Constants.TEST_DETAILS_OBJECT, testDetails)
            testQuestionIntent.putExtra(Constants.TEST_TRUE_TIME_FLAG, flag)
            testQuestionIntent.putExtra(Constants.TEST_SUBSCRIPTION_ID, testSubscriptionId)
            testQuestionIntent.putExtra(Constants.SYLLABUS_TITLE, title)
            return testQuestionIntent
        }
    }

    private fun startTestQuestionActivity() {
        val questionIntentObject = MockTestSectionActivity.newIntent(
            this@MockTestSyllabusActivity,
            testDetails, flag, testSubscriptionId
        )
        finish()
        startActivity(questionIntentObject)
    }

    override fun androidInjector(): AndroidInjector<Any> {
        return dispatchingAndroidInjector
    }

    override fun provideViewBinding(): ActivityMockTestsSyllabusBinding {
        return ActivityMockTestsSyllabusBinding.inflate(layoutInflater)
    }

    override fun providePageName(): String {
        return TAG
    }

    override fun provideViewModel(): MockTestSyllabusViewModel {
        return viewModelProvider(viewModelFactory)
    }

    override fun setupView(savedInstanceState: Bundle?) {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        viewModel = viewModelProvider(viewModelFactory)
        initListeners()
        setupDataFromIntent()
        getDataFromViewModel()
        initUI()
    }
}