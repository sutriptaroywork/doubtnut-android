package com.doubtnutapp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.course.widgets.WidgetViewPlanButtonModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class WidgetPlanButtonVM @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _widgetViewPlanButtonModel = MutableLiveData<WidgetViewPlanButtonModel?>()
    val widgetViewPlanButtonModel: LiveData<WidgetViewPlanButtonModel?>
        get() = _widgetViewPlanButtonModel

    private val _widgetViewPlanButtonVisibility = MutableLiveData<Boolean>()
    val widgetViewPlanButtonVisibility: LiveData<Boolean>
        get() = _widgetViewPlanButtonVisibility

    fun updateWidgetViewPlanButtonModel(
        widgetViewPlanButtonModel: WidgetViewPlanButtonModel?
    ) {
        _widgetViewPlanButtonModel.value = widgetViewPlanButtonModel
    }

    fun updateWidgetViewPlanButtonVisibility(
        isWidgetViewPlanButtonVisible: Boolean
    ) {
        _widgetViewPlanButtonVisibility.value = isWidgetViewPlanButtonVisible
    }
}
