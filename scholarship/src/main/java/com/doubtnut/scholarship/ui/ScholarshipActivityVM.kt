package com.doubtnut.scholarship.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.core.base.CoreViewModel
import com.doubtnut.core.data.remote.Outcome
import com.doubtnut.core.entitiy.BaseResponse
import com.doubtnut.scholarship.data.entity.ScholarshipData
import com.doubtnut.scholarship.data.remote.ScholarshipRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ScholarshipActivityVM @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val scholarshipRepository: ScholarshipRepository
) : CoreViewModel(compositeDisposable) {

    private val _widgetsLiveData: MutableLiveData<Outcome<ScholarshipData>> = MutableLiveData()
    val widgetsLiveData: LiveData<Outcome<ScholarshipData>>
        get() = _widgetsLiveData

    private val _registerTestLiveData: MutableLiveData<Outcome<BaseResponse>> = MutableLiveData()
    val registerTestLiveData: LiveData<Outcome<BaseResponse>>
        get() = _registerTestLiveData

    private fun startLoading() {
        _widgetsLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _widgetsLiveData.value = Outcome.loading(false)
    }

    fun getScholarshipData(
        id: String,
        changeTest: Boolean? = null
    ) {
        startLoading()
        viewModelScope.launch {
            scholarshipRepository.getScholarshipData(id, changeTest)
                .catch {
                    it.printStackTrace()
                    stopLoading()
                }
                .collect {
                    stopLoading()
                    _widgetsLiveData.value = Outcome.success(it)
                }
        }
    }

    fun registerScholarshipTest(
        id: String
    ) {
        _registerTestLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            scholarshipRepository.registerScholarshipTest(id)
                .catch {
                    it.printStackTrace()
                    _registerTestLiveData.value = Outcome.loading(false)
                }
                .collect {
                    _registerTestLiveData.value = Outcome.loading(false)
                    _registerTestLiveData.value = Outcome.success(it)
                }
        }
    }

}