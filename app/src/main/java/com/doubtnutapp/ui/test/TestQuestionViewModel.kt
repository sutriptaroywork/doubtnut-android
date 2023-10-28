package com.doubtnutapp.ui.test

import android.nfc.FormatException
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.Constants
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.authToken
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.ui.test.event.TestEventManager
import com.google.android.exoplayer2.util.Log
import com.google.gson.JsonSyntaxException
import com.instacart.library.truetime.TrueTimeRx
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

/**
 * Created by akshaynandwana on
 * 18, December, 2018
 **/
class TestQuestionViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val testEventManager: TestEventManager
) : BaseViewModel(compositeDisposable) {

    val testResultLiveData: MutableLiveData<Outcome<ApiResponse<TestResult>>> = MutableLiveData()
    val testSubscriptionIdLiveData: MutableLiveData<Outcome<Int?>> = MutableLiveData()

    private val readFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

    fun getTestRules(rulesId: Int): RetrofitLiveData<ApiResponse<TestRules>> {
        return DataHandler.INSTANCE.testRepository.getTestRules(
            authToken(DoubtnutApp.INSTANCE.applicationContext),
            rulesId
        )
    }

    fun getTestQuestions(testId: Int): RetrofitLiveData<ApiResponse<ArrayList<TestQuestionDataOptions>>> {
        return DataHandler.INSTANCE.testRepository.getTestQuestions(
            authToken(DoubtnutApp.INSTANCE.applicationContext),
            testId
        )
    }

    fun getTestResult(testSubscriptionId: Int) {
        DataHandler.INSTANCE.testRepository.getTestResult(
            authToken(DoubtnutApp.INSTANCE.applicationContext),
            testSubscriptionId
        )
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onTestResultResSuccess, this::onError)
    }

    fun subscribeForTest(testId: Int) {
        DataHandler.INSTANCE.testRepository.getTestSubscribe(authToken(DoubtnutApp.INSTANCE.applicationContext), testId)
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(this::onTestSubscriptionIdResSuccess, this::onError)
    }

    fun getTestLeaderboard(testId: Int): RetrofitLiveData<ApiResponse<ArrayList<TestLeaderboard>>> {
        return DataHandler.INSTANCE.testRepository.getTestLeaderboard(
            authToken(DoubtnutApp.INSTANCE.applicationContext),
            testId
        )
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

    private fun onTestResultResSuccess(response: ApiResponse<TestResult>) {
        testResultLiveData.value = Outcome.loading(false)
        testResultLiveData.value = Outcome.success(response)
    }

    private fun onTestSubscriptionIdResSuccess(response: ApiResponse<TestSubscribe>) {
        testSubscriptionIdLiveData.value = Outcome.loading(false)
        testSubscriptionIdLiveData.value = Outcome.success(response.data.testSubscriptionId)
    }

    private fun onError(error: Throwable) {
        Log.d("errorRequest", error.toString())
        testResultLiveData.value = Outcome.loading(false)
        testResultLiveData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_FORBIDDEN -> Outcome.ApiError(error)
                HttpURLConnection.HTTP_INTERNAL_ERROR -> Outcome.loading(false)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
        logException(error)
    }

    private fun logException(error: Throwable) {
        try {
            if (error is JsonSyntaxException
                || error is NullPointerException
                || error is ClassCastException
                || error is FormatException
                || error is IllegalArgumentException
            ) {
                com.doubtnutapp.Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    fun getData(): ArrayList<MockTestTopicFilter> {
        return arrayListOf(
            MockTestTopicFilter("Math", "2"),
            MockTestTopicFilter("Physics", "4"),
            MockTestTopicFilter("Chemistry", "6"),
            MockTestTopicFilter("Biology", "8")
        )
    }

    fun publishDailyQuizTopicSelectionEvent(testId: String) {
        testEventManager.onDailyQuizTopicSelection(testId)
    }

    fun publishQuizViewAnswerEvent(testId: String) {
        testEventManager.onViewAnswer(testId)
    }

}
