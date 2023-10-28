package com.doubtnutapp.libraryhome.mocktest.viewmodel

import android.nfc.FormatException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.Log

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.OpenMockTestListActivity
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.mocktest.MockTestCourseData
import com.doubtnutapp.data.remote.repository.MockTestRepository
import com.doubtnutapp.libraryhome.event.LibraryEventManager
import com.doubtnutapp.screennavigator.MockTestListScreen
import com.doubtnutapp.screennavigator.Screen
import com.google.gson.JsonSyntaxException
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class MockTestViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val libraryEventManager: LibraryEventManager,
    private val mockTestRepository: MockTestRepository
) : BaseViewModel(compositeDisposable) {

    private val _mockTestLiveData: MutableLiveData<Outcome<MockTestCourseData>> =
        MutableLiveData()
    val mockTestLiveData: LiveData<Outcome<MockTestCourseData>>
        get() = _mockTestLiveData

    private val _navigateMockTestScreenLiveData = MutableLiveData<Pair<Screen, Int>>()

    val navigateMockTestScreenLiveData: LiveData<Pair<Screen, Int>>
        get() = _navigateMockTestScreenLiveData

    fun getMockTestData() {
        _mockTestLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            mockTestRepository.getMockTestCourseData()
                .map {
                    it.data
                }.catch { e ->
                    Outcome.failure<Exception>(e)
                    _mockTestLiveData.value = Outcome.loading(false)
                }.collect {
                    onLibraryMockTestDataSuccess(it)
                }
        }
    }

    private fun onLibraryMockTestDataSuccess(data: MockTestCourseData) {
        _mockTestLiveData.value = Outcome.loading(false)
        _mockTestLiveData.value = Outcome.success(data)
    }

    private fun onLibraryMockTestDataError(error: Throwable) {
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
                Log.e(error)
            }
        } catch (e: Exception) {

        }
    }

    fun handleAction(action: Any) {
        when (action) {
            is OpenMockTestListActivity -> openScreen(action)
        }
    }

    private fun openScreen(action: OpenMockTestListActivity) {
        _navigateMockTestScreenLiveData.value = Pair(MockTestListScreen, action.position)
    }

    fun publishLibraryTabSelectedEvent(tab: String) {
        libraryEventManager.onLibraryTabSelected(tab)
    }
}