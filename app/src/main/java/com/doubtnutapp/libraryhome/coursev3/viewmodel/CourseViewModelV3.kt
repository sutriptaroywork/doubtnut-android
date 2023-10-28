package com.doubtnutapp.libraryhome.coursev3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.Log
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.course.widgets.SaleWidgetModel
import com.doubtnutapp.course.widgets.TrialTimerWidget
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.*
import com.doubtnutapp.data.remote.repository.SnackBarRepositoryImpl
import com.doubtnutapp.domain.homefeed.interactor.ConfigData
import com.doubtnutapp.domain.payment.entities.PurchasedCourseDetail
import com.doubtnutapp.plus
import com.doubtnutapp.toMapOfStringVString
import com.doubtnutapp.utils.Event
import com.doubtnutapp.utils.FileUtils
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class CourseViewModelV3 @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val snackBarRepository: SnackBarRepositoryImpl
) : BaseViewModel(compositeDisposable) {

    var extraParams: HashMap<String, Any> = hashMapOf()

    private val _resourceLiveData: MutableLiveData<Outcome<ApiResourceData>> = MutableLiveData()

    private val _courseLiveData: MutableLiveData<Outcome<ApiCourseDataV3>> = MutableLiveData()

    private val _subjectLiveData: MutableLiveData<Outcome<ApiSubjectDetailData>> = MutableLiveData()

    val courseLiveData: LiveData<Outcome<ApiCourseDataV3>>
        get() = _courseLiveData

    val subjectDetailData: LiveData<Outcome<ApiSubjectDetailData>>
        get() = _subjectLiveData

    private val _callData: MutableLiveData<Event<CallData?>> = MutableLiveData()

    val callData: LiveData<Event<CallData?>>
        get() = _callData

    private val _widgetsLiveData: MutableLiveData<Outcome<Widgets>> = MutableLiveData()

    val widgetsLiveData: LiveData<Outcome<Widgets>>
        get() = _widgetsLiveData

    private val _purchasedCourseLiveData: MutableLiveData<Outcome<PurchasedCourseDetail>> =
        MutableLiveData()

    val purchasedCourseLiveData: LiveData<Outcome<PurchasedCourseDetail>>
        get() = _purchasedCourseLiveData

    private val _activateVipLiveData: MutableLiveData<Outcome<ActivateTrialData>> =
        MutableLiveData()

    val activateVipLiveData: LiveData<Outcome<ActivateTrialData>>
        get() = _activateVipLiveData

    private val _configLiveData: MutableLiveData<Outcome<ConfigData>> = MutableLiveData()

    val configLiveData: LiveData<Outcome<ConfigData>>
        get() = _configLiveData

    private val _snackBarData = MutableLiveData<SnackBarData>()
    val snackBarData: LiveData<SnackBarData>
        get() = _snackBarData

    fun getResourceData(courseDetailId: String, recorded: Int) {
        _resourceLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getResourceData(courseDetailId, recorded)
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
                        _resourceLiveData.value = Outcome.success(it)
                        _resourceLiveData.value = Outcome.loading(false)
                    }, {
                        _resourceLiveData.value = Outcome.loading(false)
                    })
    }

    fun getPurchasedCourses(page: String? = null) {
        _purchasedCourseLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getPurchasedCourses(page)
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
                        _purchasedCourseLiveData.value = Outcome.success(it)
                        _purchasedCourseLiveData.value = Outcome.loading(false)
                    }, {
                        _purchasedCourseLiveData.value = Outcome.loading(false)
                    })
    }

    fun getConfigData(sessionCount: Int, postPurchaseSessionCount: Int) {
        _configLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.appConfigRepository.getConfigData(
                    sessionCount,
                    postPurchaseSessionCount
                )
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _configLiveData.value = Outcome.success(it)
                        _configLiveData.value = Outcome.loading(false)
                    }, {
                        _configLiveData.value = Outcome.loading(false)
                    })
    }

    fun getAllCoursesData(
        assortmentId: String,
        subject: String,
        studentClass: String?,
        page: Int,
        source: String? = null,
        bottomSheet: Boolean = false,
        filtersMap: HashMap<String, MutableList<String>> = hashMapOf(),
        tabId: String? = null
    ) {
        _courseLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getAllCoursesData(
                    assortmentId,
                    subject,
                    studentClass,
                    source,
                    page,
                    bottomSheet,
                    filtersMap.toMapOfStringVString(),
                    tabId
                )
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data.widgets?.mapIndexed { index, widget ->
                            if (widget != null) {
                                if (widget.extraParams == null) {
                                    widget.extraParams = hashMapOf()
                                }
                                widget.extraParams?.putAll(extraParams)
                                widget.extraParams?.put(EventConstants.CARD_ORDER, index)

                                if (widget is SaleWidgetModel) {
                                    widget.data.items?.forEach { _saleItem ->
                                        _saleItem.responseAtTimeInMillis =
                                            System.currentTimeMillis()
                                    }
                                }
                            }
                        }
                        it.data.extraWidgets?.map { widget ->
                            if (widget != null) {
                                if (widget.extraParams == null) {
                                    widget.extraParams = hashMapOf()
                                }
                                widget.extraParams?.putAll(extraParams)
                            }
                        }
                        it.data.stickyWidgets?.map { widget ->
                            if (widget != null) {
                                if (widget.extraParams == null) {
                                    widget.extraParams = hashMapOf()
                                }
                                widget.extraParams?.putAll(extraParams)
                                if (widget is TrialTimerWidget.TrialTimerModel) {
                                    widget.data.responseAtTimeInMillis = System.currentTimeMillis()
                                }
                            }
                        }
                        it.data
                    }
                    .subscribeToSingle({
                        _callData.value = Event(it.callData)
                        _courseLiveData.value = Outcome.success(it)
                        _courseLiveData.value = Outcome.loading(false)
                    }, {
                        _courseLiveData.value = Outcome.loading(false)
                    })
    }

    fun getStories() {
        _widgetsLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getStories()
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data.widgets?.map { widget ->
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

    fun getSubjectDetailData(subject: String, assortmentId: String, page: Int) {
        _subjectLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getSubjectDetailData(
                    subject,
                    assortmentId,
                    page
                )
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data.widgets?.map { widget ->
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
                        _subjectLiveData.value = Outcome.success(it)
                        _subjectLiveData.value = Outcome.loading(false)
                    }, {
                        _subjectLiveData.value = Outcome.loading(false)
                    })
    }

    fun getCourseTabsData(
        pageNumber: Int,
        assortmentId: String,
        tabId: String,
        contentType: String?
    ) {
        _widgetsLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getCourseTabsData(
                    pageNumber,
                    assortmentId,
                    tabId,
                    contentType
                )
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

    fun activateTrial(assortmentId: String?) {
        _activateVipLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.activateTrial(assortmentId.orEmpty())
                    .applyIoToMainSchedulerOnSingle()

                    .map {
                        it.data
                    }
                    .subscribeToSingle({
                        _activateVipLiveData.value = Outcome.success(it)
                        _activateVipLiveData.value = Outcome.loading(false)
                    }, {
                        _activateVipLiveData.value = Outcome.loading(false)
                    })
    }

    var pdfUriLiveData = MutableLiveData<Triple<File, String, String>>()

    fun getPdfFilePath(url: String, type: String) {
        val filepath = FileUtils.getPdfFileDestinationPath(url)
        if (FileUtils.isFilePresent(filepath)) {
            pdfUriLiveData.value = Triple(File(filepath), type, url)
        } else {
            compositeDisposable.add(
                DataHandler.INSTANCE.pdfRepository.downloadPdf(url, filepath)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribe({
                        pdfUriLiveData.value = Triple(File(filepath), type, url)
                    }, {
                        pdfUriLiveData.value = null
                    })
            )
        }
    }

    fun getVideoTabsData(id: String?, qid: String?) {
        _widgetsLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            DataHandler.INSTANCE.courseRepository.getVideoTabsData(id, qid)
                .map {
                    it.data
                }.catch { e ->
                    _widgetsLiveData.value = Outcome.Failure(e)
                    _widgetsLiveData.value = Outcome.loading(false)
                }.collect {
                    _widgetsLiveData.value = Outcome.success(it)
                    _widgetsLiveData.value = Outcome.loading(false)
                }
        }
    }

    fun getSnackBar(
        assortmentId: String?
    ) {
        viewModelScope.launch {
            snackBarRepository.getSnackBarData(
                source = "course_page",
                page = purchasePopupSnackbarPage++,
                assortmentId = assortmentId,
            )
                .catch { exception ->
                    Log.e(exception)
                }
                .collect {
                    if (it.data?.title.isNullOrEmpty()) return@collect
                    _snackBarData.value = it.data ?: return@collect
                }
        }
    }

    companion object {
        var purchasePopupSnackbarPage = 1
    }

}