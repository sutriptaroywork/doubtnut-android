package com.doubtnutapp.ui.mockTest

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.TestDetails
import com.doubtnutapp.domain.liveclasseslibrary.interactor.GetMockDataUseCase
import com.doubtnutapp.domain.mockTestLibrary.entities.MockTestDetailsEntity
import com.doubtnutapp.libraryhome.liveclasses.mapper.MockTestDataMapper
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

/**
 * Created by Anand Gaurav on 23/06/20.
 */
class MockTestSubscriptionViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val getMockDataUseCase: GetMockDataUseCase,
    private val mockTestMapper: MockTestDataMapper
) : BaseViewModel(compositeDisposable) {

    private val _mockTestLiveData: MutableLiveData<Outcome<TestDetails>> = MutableLiveData()
    val mockTestLiveData: LiveData<Outcome<TestDetails>>
        get() = _mockTestLiveData

    fun getMockTestData(mockTestId: Int) {
        _mockTestLiveData.value = Outcome.loading(true)
        compositeDisposable + getMockDataUseCase
            .execute(GetMockDataUseCase.Param(mockTestId))
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle({ onMockDataSuccess(it) }, this::onMockDataFailure)
    }

    private fun onMockDataSuccess(mockTestDetailsEntity: MockTestDetailsEntity) {
        _mockTestLiveData.value = Outcome.success(mockTestMapper.map(mockTestDetailsEntity))
        _mockTestLiveData.value = Outcome.loading(false)
    }

    private fun onMockDataFailure(error: Throwable) {
        _mockTestLiveData.value = Outcome.loading(false)
        _mockTestLiveData.value = if (error is HttpException) {
            val errorCode = error.response()?.code()
            when (errorCode) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(error.message ?: "")
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

}