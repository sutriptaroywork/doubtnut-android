package com.doubtnutapp.ui.mockTest.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.models.mocktest.MockTestAnalysisData
import com.doubtnutapp.data.remote.repository.MockTestRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class MockTestAnalysisViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable,
        val mockTestRepository: MockTestRepository) : BaseViewModel(compositeDisposable) {

    private val _analysisData
            : MutableLiveData<Outcome<MockTestAnalysisData>> = MutableLiveData()

    val analysisData: LiveData<Outcome<MockTestAnalysisData>>
        get() = _analysisData


    fun startLoading() {
        _analysisData.value = Outcome.loading(true)
    }

    fun stopLoading() {
        _analysisData.value = Outcome.loading(false)
    }

    fun getAnalysisData(testId: String, subject: String?) {
        startLoading()
        viewModelScope.launch {
            mockTestRepository.getAnalysisData(testId, subject)
                    .map {
                        it.data
                    }.catch {
                        stopLoading()
                    }.collect {
                        _analysisData.value = Outcome.success(it)
                        stopLoading()
                    }
        }
    }

}