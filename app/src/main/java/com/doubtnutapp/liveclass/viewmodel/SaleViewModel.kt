package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.course.widgets.SaleWidgetModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.Widgets
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class SaleViewModel @Inject constructor(
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _widgetsLiveData: MutableLiveData<Outcome<Widgets>> = MutableLiveData()

    val widgetsLiveData: LiveData<Outcome<Widgets>>
        get() = _widgetsLiveData

    var extraParams: HashMap<String, Any> = hashMapOf()

    fun getNudgesData(id: Int) {
        _widgetsLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getNudgesData(id)
                        .applyIoToMainSchedulerOnSingle()
                        .map {
                            it.data.widgets.map { widget ->
                                if (widget != null) {
                                    if (widget.extraParams == null) {
                                        widget.extraParams = hashMapOf()
                                    }
                                    widget.extraParams?.putAll(extraParams)
                                }
                                if (widget is SaleWidgetModel) {
                                    widget.data.items?.forEach { _saleItem ->
                                        _saleItem.responseAtTimeInMillis = System.currentTimeMillis()
                                    }
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