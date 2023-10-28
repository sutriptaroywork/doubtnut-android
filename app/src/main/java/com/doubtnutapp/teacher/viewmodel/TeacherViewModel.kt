package com.doubtnutapp.teacher.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.teacher.model.TeacherListData
import com.doubtnutapp.teacher.repository.TeacherRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class TeacherViewModel @Inject constructor(
    private val teacherRepository: TeacherRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    var extraParams: HashMap<String, Any> = hashMapOf()

    var itemsAdded = 0

    private val _teacherLiveData: MutableLiveData<Outcome<TeacherListData>> = MutableLiveData()
    val teacherLiveData: LiveData<Outcome<TeacherListData>>
        get() = _teacherLiveData

    private fun startLoading() {
        _teacherLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _teacherLiveData.value = Outcome.loading(false)
    }

    fun fetchTeacherList(page: Int) {
        startLoading()
        viewModelScope.launch {
            teacherRepository.fetchTeacherList(page)
                .map {
                    it.data.widgets.mapIndexedNotNull { index, widget ->
                        if (widget != null) {
                            if (widget.extraParams == null) {
                                widget.extraParams = hashMapOf()
                            }
                            widget.extraParams?.putAll(extraParams)
                            widget.extraParams?.put(
                                EventConstants.ITEM_PARENT_POSITION,
                                itemsAdded + index
                            )
                        }
                    }
                    it.data
                }.catch {
                    stopLoading()
                }.collect {
                    if (!it.widgets.isNullOrEmpty()) {
                        itemsAdded.plus(it.widgets.size)
                    }
                    stopLoading()
                    _teacherLiveData.value = Outcome.success(it)
                }
        }
    }


}