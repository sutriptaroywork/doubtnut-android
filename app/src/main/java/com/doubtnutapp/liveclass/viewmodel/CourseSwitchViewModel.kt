package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.CallbackData
import com.doubtnutapp.data.remote.models.CourseListData
import com.doubtnutapp.data.remote.models.CourseSelectionData
import com.doubtnutapp.liveclass.ui.dialog.CourseChangeData
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class CourseSwitchViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _courseChangeLiveData: MutableLiveData<Outcome<CourseChangeData>> = MutableLiveData()

    private val _courseSelectionLiveData: MutableLiveData<Outcome<CourseSelectionData>> = MutableLiveData()

    private val _courseListLiveData: MutableLiveData<Outcome<CourseListData>> = MutableLiveData()

    private val _callbackLiveData: MutableLiveData<Outcome<CallbackData>> = MutableLiveData()

    val courseChangeLiveData: LiveData<Outcome<CourseChangeData>>
        get() = _courseChangeLiveData

    val courseListLiveData: LiveData<Outcome<CourseListData>>
        get() = _courseListLiveData

    val courseSelectionLiveData: LiveData<Outcome<CourseSelectionData>>
        get() = _courseSelectionLiveData

    val callbackLiveData: LiveData<Outcome<CallbackData>>
        get() = _callbackLiveData

    fun getCourseChangeData(popupType: String, selectedAssortment: String, assortmentId: String) {
        _courseChangeLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getCourseChangData(popupType, selectedAssortment, assortmentId)
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _courseChangeLiveData.value = Outcome.success(it)
                            _courseChangeLiveData.value = Outcome.loading(false)
                        }, {
                            _courseChangeLiveData.value = Outcome.loading(false)
                        })
    }

    fun getCourseSelectionData() {
        _courseSelectionLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getCourseSelectionData()
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _courseSelectionLiveData.value = Outcome.success(it)
                            _courseSelectionLiveData.value = Outcome.loading(false)
                        }, {
                            _courseSelectionLiveData.value = Outcome.loading(false)
                        })
    }


    fun requestCallback(assortmentId: String, selectedAssortmentId: String, subscriptionId: String) {
        _callbackLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.requestCallback(assortmentId, selectedAssortmentId, subscriptionId)
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _callbackLiveData.value = Outcome.success(it)
                            _callbackLiveData.value = Outcome.loading(false)
                        }, {
                            _callbackLiveData.value = Outcome.loading(false)
                        })
    }

    fun getCourseListData(selectedClass: String, selectedExam: String, selectedExamYear: String, medium: String, assortmentId: String) {
        _courseListLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getCourseListData(selectedClass, selectedExam, selectedExamYear, medium, assortmentId)
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data
                        }
                        .subscribeToSingle({
                            _courseListLiveData.value = Outcome.success(it)
                            _courseListLiveData.value = Outcome.loading(false)
                        }, {
                            _courseListLiveData.value = Outcome.loading(false)
                        })
    }
}