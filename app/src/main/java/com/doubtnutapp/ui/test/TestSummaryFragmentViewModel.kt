package com.doubtnutapp.ui.test

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnCompletable
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.TestSubmit
import com.doubtnutapp.domain.common.interactor.AddQuizIdToAttemptedList
import com.doubtnutapp.plus
import com.doubtnutapp.ui.test.event.TestEventManager
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class TestSummaryFragmentViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val addQuizIdToAttemptedList: AddQuizIdToAttemptedList,
    private val testEventManager: TestEventManager
) : BaseViewModel(compositeDisposable) {

    fun getTestSubmit(testSubscriptionId: Int): RetrofitLiveData<ApiResponse<TestSubmit>> {
        return DataHandler.INSTANCE.testRepository.getTestSubmit(testSubscriptionId)
    }

    fun addTestIdToAttemptedList(testId: Int, testSubscriptionId: Int?) {
        testSubscriptionId?.let {
            compositeDisposable + addQuizIdToAttemptedList.execute(
                AddQuizIdToAttemptedList.Param(
                    testId,
                    testSubscriptionId
                )
            )
                .applyIoToMainSchedulerOnCompletable()
                .subscribe()
        }
    }

    fun publishOnQuizSubmitEvent(testId: String) {
        testEventManager.onQuizSubmit(testId)
    }
}