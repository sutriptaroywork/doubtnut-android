package com.doubtnutapp.matchquestion.viewmodel

import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

/**
 * Created by devansh on 08/07/20.
 */

class BlurQuestionImageDialogFragmentViewModel @Inject constructor(
        private val analyticsPublisher: AnalyticsPublisher,
        compositeDisposable: CompositeDisposable
) : BaseViewModel(compositeDisposable) {

    fun sendEvent(eventName: String, params: HashMap<String, Any>) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params))
    }

}