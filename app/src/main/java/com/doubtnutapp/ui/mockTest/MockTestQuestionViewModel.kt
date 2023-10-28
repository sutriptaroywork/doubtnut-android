package com.doubtnutapp.ui.mockTest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.Constants
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.models.mocktest.MockTestAnalysisData
import com.doubtnutapp.data.remote.repository.MockTestRepository
import com.doubtnutapp.map
import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MockTestQuestionViewModel @Inject constructor(
    private val mockTestRepository: MockTestRepository,
    compositeDisposable: CompositeDisposable
) :
    BaseViewModel(compositeDisposable) {

    private val readFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    val sectionAttemptLimitMap: MutableMap<String, Int>? = mutableMapOf()
    val answersMap: MutableMap<Int, String> = mutableMapOf()
    val answersMapLiveData = MutableLiveData<MutableMap<Int, String>>()

    private val _startTestData
            : MutableLiveData<Outcome<StartTestData>> = MutableLiveData()

    val startTestData: LiveData<Outcome<StartTestData>>
        get() = _startTestData

    fun getTestRules(rulesId: Int): RetrofitLiveData<ApiResponse<TestRules>> {
        return mockTestRepository.getMockTestRules(rulesId)
    }

    fun getTestQuestions(testId: Int): RetrofitLiveData<ApiResponse<MockTestQuestionData>> {
        return mockTestRepository.getMockTestQuestions(testId)
    }

    fun getTestResult(testSubscriptionId: Int): RetrofitLiveData<ApiResponse<MockTestResult>> {
        return mockTestRepository.getMockTestResult(testSubscriptionId)
    }

    fun getTestLeaderboard(testId: Int): RetrofitLiveData<ApiResponse<ArrayList<TestLeaderboard>>> {
        return mockTestRepository.getMockTestLeaderboard(testId)
    }

    fun startTest(testId: String) {
        viewModelScope.launch {
            mockTestRepository.startTest(testId)
                .map {
                    it
                }.catch {
                    val throwable = it
                    Log.d(throwable.message.toString())
                }.collect {
                    _startTestData.value = Outcome.success(it.data)
                }
        }
    }

    fun getTrueTimeDecision(publishTime: String?, unpublishTime: String?, now: Date): String {

        val readFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val trueTime = readFormat.parse(readFormat.format(now.time))
        val startTestTime =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(publishTime)
        val endTestTime =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(
                unpublishTime
            )

        val flag = if (trueTime.after(endTestTime)) {
            Constants.TEST_OVER
        } else if (trueTime.before(startTestTime)) {
            Constants.TEST_UPCOMING
        } else {
            Constants.TEST_ACTIVE
        }
        return flag
    }

    fun getTestStartBeforeTimeDifferenceLong(publishTime: String?): Long {
        val now = if (TrueTimeRx.isInitialized()) {
            TrueTimeRx.now()
        } else {
            Calendar.getInstance().getTime()
        }
        val trueTime = readFormat.parse(readFormat.format(now.time))
        val startTestTime =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).parse(publishTime)
        return startTestTime.time - trueTime.time
    }

    fun getTestStartAfterTimeDifferenceLong(unpublishTime: String?): Long {
        val now = if (TrueTimeRx.isInitialized()) {
            TrueTimeRx.now()
        } else {
            Calendar.getInstance().getTime()
        }
        return readFormat.parse(unpublishTime).time - readFormat.parse(readFormat.format(now.time)).time
    }

}
