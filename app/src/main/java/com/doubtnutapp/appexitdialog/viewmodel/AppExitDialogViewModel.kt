package com.doubtnutapp.appexitdialog.viewmodel

import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.repository.AppExitRepository
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by devansh on 9/1/21.
 */

class AppExitDialogViewModel @Inject constructor(
    private val analyticsPublisher: AnalyticsPublisher,
    private val appExitRepository: AppExitRepository,
    compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    fun setAppExitDialogShownInCurrentSession(shown: Boolean) {
        appExitRepository.setAppExitDialogShownInCurrentSession(shown)
    }

    fun sendEvent(name: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(AnalyticsEvent(name, params))
    }
}
