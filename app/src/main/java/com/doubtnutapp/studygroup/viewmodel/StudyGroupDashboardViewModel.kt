package com.doubtnutapp.studygroup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.Constants
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.analytics.model.StructuredEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.ViewState
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.toRequestBody
import com.doubtnutapp.plus
import com.doubtnutapp.studygroup.model.StudyGroupDashboardMessage
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

class StudyGroupDashboardViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
) : BaseViewModel(compositeDisposable) {

    val stateLiveData: MutableLiveData<ViewState> = MutableLiveData(ViewState.none())

    private val _adminDashboardMessageLiveData: MutableLiveData<Outcome<StudyGroupDashboardMessage>> =
        MutableLiveData()
    val adminDashboardMessageLiveData: LiveData<Outcome<StudyGroupDashboardMessage>>
        get() = _adminDashboardMessageLiveData

    private val _studentReportedMessageLiveData: MutableLiveData<Outcome<StudyGroupDashboardMessage>> =
        MutableLiveData()
    val studentReportedMessageLiveData: LiveData<Outcome<StudyGroupDashboardMessage>>
        get() = _studentReportedMessageLiveData

    fun getAdminDashboardMessages(groupId: String, page: Int) {
        _adminDashboardMessageLiveData.value = Outcome.loading(true)
        val params = mapOf(
            "room_id" to groupId,
            "room_type" to Constants.STUDY_GROUP,
            "page" to page
        )
        compositeDisposable +
                DataHandler.INSTANCE.microService.get().getSgAdminDashboard(params.toRequestBody())
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _adminDashboardMessageLiveData.value = Outcome.loading(false)
                            _adminDashboardMessageLiveData.value = Outcome.success(it.data)
                        },
                        {
                            _adminDashboardMessageLiveData.value = Outcome.loading(false)
                            it.printStackTrace()
                        }
                    )
    }

    fun getStudentReportedMessages(groupId: String, reported_student_id: String, page: Int) {
        _studentReportedMessageLiveData.value = Outcome.loading(true)
        val params = mapOf(
            "room_id" to groupId,
            "reported_student_id" to reported_student_id,
            "page" to page
        )
        compositeDisposable +
                DataHandler.INSTANCE.microService.get()
                    .getStudentReportedMessages(params.toRequestBody())
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _studentReportedMessageLiveData.value = Outcome.loading(false)
                            _studentReportedMessageLiveData.value = Outcome.success(it.data)
                        },
                        {
                            _studentReportedMessageLiveData.value = Outcome.loading(false)
                            it.printStackTrace()
                        }
                    )
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf()) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                name = eventName,
                params = params,
                ignoreSnowplow = false,
                ignoreFirebase = false,
                ignoreMoengage = false
            )
        )
    }

    fun publishTimeSpentEvent(category: String, action: String, params: HashMap<String, Any>) {
        analyticsPublisher.publishEvent(
            StructuredEvent(
                category = category,
                action = action,
                eventParams = params
            )
        )
    }
}