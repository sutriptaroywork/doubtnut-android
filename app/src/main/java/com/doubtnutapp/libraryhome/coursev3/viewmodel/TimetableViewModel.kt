package com.doubtnutapp.libraryhome.coursev3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ApiTimetableData
import com.doubtnutapp.data.remote.models.Widgets
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class TimetableViewModel @Inject constructor(compositeDisposable: CompositeDisposable) :
        BaseViewModel(compositeDisposable) {

    private val _widgetsLiveData: MutableLiveData<Outcome<ApiTimetableData>> = MutableLiveData()
    private var extraParams: HashMap<String, Any> = hashMapOf()

    val widgetsLiveData: LiveData<Outcome<ApiTimetableData>>
        get() = _widgetsLiveData


    fun getTimetable(courseId: String, studentClass: String) {
        _widgetsLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getTimetable(courseId, studentClass)
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data.widgets.map { widget ->
                                if (widget != null) {
                                    if (widget.extraParams == null) {
                                        widget.extraParams = hashMapOf()
                                    }
                                    widget.extraParams?.putAll(extraParams)
                                }
                            }
                            it.data
                        }
                        .subscribeToSingle({
                            _widgetsLiveData.value = Outcome.success(it)
                            _widgetsLiveData.value = Outcome.loading(false)
                        }, {
                            _widgetsLiveData.value = Outcome.loading(false)
                        })
    }
}