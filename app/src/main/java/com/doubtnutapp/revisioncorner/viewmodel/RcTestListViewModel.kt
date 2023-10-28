package com.doubtnutapp.revisioncorner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.models.revisioncorner.*
import com.doubtnutapp.data.remote.repository.RevisionCornerRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.HashMap
import javax.inject.Inject

class RcTestListViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val repository: RevisionCornerRepository,
) : BaseViewModel(compositeDisposable) {

    private val _testMetaDataLiveData = MutableLiveData<TestMetaData>()
    val testMetaDataLiveData: LiveData<TestMetaData?>
        get() = _testMetaDataLiveData

    private var examType: String? = null

    private val _testListLiveData = MutableLiveData<List<WidgetEntityModel<*, *>>>()
    val testListLiveData: MutableLiveData<List<WidgetEntityModel<*, *>>>
        get() = _testListLiveData

    fun setExamType(examType: String) {
        if (this.examType.isNullOrEmpty()) {
            this.examType = examType
        }
    }


    fun getTestListData(page: Int, tabId: String) {
        viewModelScope.launch {
            repository.getTestListData(examType.orEmpty(), page, tabId)
                .catch { }
                .collect {
                    if (page == 0) {
                        //to avoid title updation for every page
                        _testMetaDataLiveData.value = it.data.testMetaData
                    }
                    _testListLiveData.postValue(it.data.widgets.orEmpty())
                }
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}