package com.doubtnutapp.liveclass.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.models.ResourceListData
import com.doubtnutapp.plus
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class ResourceListViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _widgetsLiveData: MutableLiveData<Outcome<ResourceListData>> = MutableLiveData()

    val widgetsLiveData: LiveData<Outcome<ResourceListData>>
        get() = _widgetsLiveData

    var extraParams: HashMap<String, Any> = hashMapOf()
    var itemsAdded = 0

    fun getResourceList(page: Int, id: String, subject: String) {
        _widgetsLiveData.value = Outcome.loading(true)
        compositeDisposable +
                DataHandler.INSTANCE.courseRepository.getResourceList(page, id, subject)
                    .applyIoToMainSchedulerOnSingle()
                    .map {
                        it.data.widgets.mapIndexed { index, widget ->
                            if (widget.extraParams == null) {
                                widget.extraParams = hashMapOf()
                            }
                            widget.extraParams?.putAll(extraParams)
                            widget.extraParams?.put(
                                EventConstants.ITEM_PARENT_POSITION,
                                itemsAdded + index
                            )
                        }
                        it.data
                    }
                    .subscribeToSingle({
                        if (!it.widgets.isNullOrEmpty()) {
                            itemsAdded.plus(it.widgets.size)
                        }
                        _widgetsLiveData.value = Outcome.success(it)
                        _widgetsLiveData.value = Outcome.loading(false)
                    }, {
                        _widgetsLiveData.value = Outcome.loading(false)
                    })
    }
}