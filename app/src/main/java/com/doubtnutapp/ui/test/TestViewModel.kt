package com.doubtnutapp.ui.test

import android.content.Context
import com.doubtnutapp.authToken
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.data.remote.models.TestResponse
import com.doubtnutapp.toRequestBody
import com.doubtnutapp.utils.Utils
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject

class TestViewModel
@Inject constructor(compositeDisposable: CompositeDisposable) : BaseViewModel(compositeDisposable) {

    fun getTestDetails(applicationContext: Context): RetrofitLiveData<ApiResponse<ArrayList<TestDetails>>> {
        return DataHandler.INSTANCE.testRepository.getTestDetails(authToken(applicationContext))
    }

    fun getTestResponse(
        applicationContext: Context,
        testId: Int,
        actionType: String,
        isReview: Int,
        optionCode: String,
        sectionCode: String,
        testSubcriptionId: Int,
        questionbankId: Int,
        questionType: String,
        isEligible: String,
        timeTake: Int,
        subjectCode: String,
        chapterCode: String,
        subtopicCode: String,
        classCode: String,
        mcCode: String
    ): RetrofitLiveData<ApiResponse<TestResponse>> {
        return DataHandler.INSTANCE.testRepository.getTestResponse(
            authToken(applicationContext),
            Utils.getTestResponseBody(
                testId, actionType, isReview, optionCode, sectionCode, testSubcriptionId,
                questionbankId, questionType, isEligible, timeTake, subjectCode,
                chapterCode, subtopicCode, classCode, mcCode
            ).toRequestBody()
        )
    }

}
