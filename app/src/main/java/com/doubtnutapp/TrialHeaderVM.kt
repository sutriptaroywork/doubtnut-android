package com.doubtnutapp

import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.course.widgets.MyCourseWidgetItem
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class TrialHeaderVM @Inject constructor(
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    private val _showTrialHeader =
        MutableStateFlow<Pair<Boolean, MyCourseWidgetItem?>>(Pair(false, null))
    val showTrialHeader: StateFlow<Pair<Boolean, MyCourseWidgetItem?>>
        get() = _showTrialHeader

    fun showTrialHeader(showTrialHeader: Boolean, trialMyCourseWidgetItem: MyCourseWidgetItem?) {
        _showTrialHeader.value = Pair(showTrialHeader, trialMyCourseWidgetItem)
    }
}
