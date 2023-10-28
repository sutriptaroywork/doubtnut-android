package com.doubtnutapp.newlibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.studygroup.service.LibraryRepository
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject

class LibraryViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val libraryRepository: LibraryRepository,
    private val analyticsPublisher: AnalyticsPublisher,
) : BaseViewModel(compositeDisposable) {

    private val _libraryWidgets: MutableLiveData<Outcome<List<WidgetEntityModel<WidgetData, WidgetAction>>>> =
        MutableLiveData()
    val libraryWidgets: LiveData<Outcome<List<WidgetEntityModel<WidgetData, WidgetAction>>>>
        get() = _libraryWidgets

    fun getLibraryWidgets() {
        _libraryWidgets.value = Outcome.loading(true)
        compositeDisposable.add(
            libraryRepository.getLibraryWidgets()
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    {
                        _libraryWidgets.value = Outcome.loading(false)
                        _libraryWidgets.value = Outcome.success(it)
                    },
                    {
                        _libraryWidgets.value = Outcome.loading(false)
                        it.printStackTrace()
                    }
                )
        )
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }
}
