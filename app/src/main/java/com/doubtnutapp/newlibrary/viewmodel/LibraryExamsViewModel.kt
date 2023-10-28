package com.doubtnutapp.newlibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.newlibrary.model.ApiLibraryExamBottomSheetData
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.studygroup.service.LibraryRepository
import com.doubtnutapp.toRequestBody
import com.doubtnutapp.widgets.LibraryExamWidget
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class LibraryExamsViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val libraryRepository: LibraryRepository
) : BaseViewModel(compositeDisposable) {

    private val _examWidgets: MutableLiveData<ApiLibraryExamBottomSheetData> = MutableLiveData()
    val examWidgets: LiveData<ApiLibraryExamBottomSheetData>
        get() = _examWidgets

    val progressLiveData: MutableLiveData<Boolean> = MutableLiveData(false)

    private val _updatedExamWidget: MutableLiveData<WidgetEntityModel<WidgetData, WidgetAction>> =
        MutableLiveData()
    val updatedExamWidget: LiveData<WidgetEntityModel<WidgetData, WidgetAction>>
        get() = _updatedExamWidget

    val selectedExams = mutableSetOf<String>()

    fun getExamWidgets(widgetId: String, commaSeparatedTabIds: String?) {
        progressLiveData.postValue(true)
        compositeDisposable.add(
            libraryRepository.getExamWidgets(widgetId, commaSeparatedTabIds)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        progressLiveData.postValue(false)
                        _examWidgets.postValue(it)
                        it.list.forEach { item ->
                            if ((item as? LibraryExamWidget.Model)?.data?.isChecked == true) {
                                selectedExams.add(item.data.id)
                            }
                        }
                    },
                    {
                        progressLiveData.postValue(false)
                        it.printStackTrace()
                    }
                )
        )
    }

    fun changeExam() {
        if (selectedExams.isEmpty()) return
        progressLiveData.postValue(true)
        val requestBody = hashMapOf<String, Any>(
            "id" to selectedExams
        ).toRequestBody()
        compositeDisposable.add(
            libraryRepository.changeExam(requestBody)
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        progressLiveData.postValue(false)
                        _updatedExamWidget.postValue(it)
                    },
                    {
                        progressLiveData.postValue(false)
                        it.printStackTrace()
                    }
                )
        )
    }

    fun filterSelectedExams(widgets: List<WidgetEntityModel<WidgetData, WidgetAction>>) {
        val selectedExams = widgets.filter {
            (it as? LibraryExamWidget.Model)?.data?.isChecked == true
        }
        selectedExams.forEach {
            val data = it.data as? LibraryExamWidget.Data ?: return
            markSelectedExams(id = data.id, isChecked = true)
        }
    }

    fun markSelectedExams(id: String, isChecked: Boolean) {
        val examWidgets = examWidgets.value?.list ?: return
        val widget: LibraryExamWidget.Model? = examWidgets.find {
            (it as? LibraryExamWidget.Model)?.data?.id == id
        } as? LibraryExamWidget.Model
        widget ?: return
        if (isChecked) {
            selectedExams.add(id)
        } else {
            selectedExams.remove(id)
        }
        widget.data.isChecked = isChecked
    }
}