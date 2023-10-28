package com.doubtnutapp.studygroup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.common.data.entity.SgWidgetListData
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.plus
import com.doubtnutapp.studygroup.model.*
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import com.doubtnutapp.studygroup.ui.fragment.SgExtraInfoFragment
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import java.util.HashMap
import javax.inject.Inject

class SgExtraInfoViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val studyGroupRepository: StudyGroupRepository,
) : BaseViewModel(compositeDisposable) {

    private val _widgetListLiveData: MutableLiveData<Event<Outcome<SgWidgetListData>>> = MutableLiveData()
    val widgetListLiveData: LiveData<Event<Outcome<SgWidgetListData>>>
        get() = _widgetListLiveData

    private val _searchResultList: MutableLiveData<Event<Outcome<SgWidgetListData>>> = MutableLiveData()
    val searchResultList: LiveData<Event<Outcome<SgWidgetListData>>>
        get() = _searchResultList

    private val _metaDataLiveData: MutableLiveData<SgMetaData> = MutableLiveData()
    val metaDataLiveData: LiveData<SgMetaData>
        get() = _metaDataLiveData

    private val _requestRequest: MutableLiveData<Outcome<AcceptStudyGroupInvitation>> =
        MutableLiveData()
    val requestRequest: LiveData<Outcome<AcceptStudyGroupInvitation>>
        get() = _requestRequest

    private var myWidgetList: MutableList<WidgetEntityModel<*, *>> = mutableListOf()

    fun getRequiredWidgetData(page: Int, source: String) {
        _widgetListLiveData.value = Event(Outcome.loading(true))
        compositeDisposable +
                studyGroupRepository.getWidgetList(page, source)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            val data = it.data
                            if (page == SgExtraInfoFragment.INITIAL_PAGE) {
                                _metaDataLiveData.postValue(
                                    SgMetaData(
                                        toolbar = SgToolbarData(
                                            title = data.title,
                                            cta = TopCta(
                                                title = data.newGroupContainer?.title,
                                                image = data.newGroupContainer?.image,
                                                deeplink = data.newGroupContainer?.deeplink
                                            )
                                        ),
                                        bottomCta = data.cta,
                                        search = SgSearch(
                                            source = data.source,
                                            searchText = data.searchText,
                                            isEnabled = data.isSearchEnabled
                                        )
                                    )
                                )
                                myWidgetList.clear()
                            }
                            _widgetListLiveData.value = Event(Outcome.success(data))
                            myWidgetList.addAll(it.data.widgets.orEmpty().toList())
                            _widgetListLiveData.value = Event(Outcome.loading(false))
                        },
                        {
                            _widgetListLiveData.value = Event(Outcome.loading(false))
                            _widgetListLiveData.value = Event(Outcome.apiError(it))
                            it.printStackTrace()
                        }
                    )
    }

    fun getSearchResultList(keyword: String, page: Int) {
        _searchResultList.value = Event(Outcome.loading(true))
        compositeDisposable +
                studyGroupRepository.getSearchResultWidgetList(
                    keyword,
                    page,
                    _metaDataLiveData.value?.search?.source.orEmpty()
                )
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _searchResultList.value = Event(Outcome.success(it.data))
                            _searchResultList.value = Event(Outcome.loading(false))
                        },
                        {
                            _searchResultList.value = Event(Outcome.loading(false))
                            _searchResultList.value = Event(Outcome.apiError(it))
                            it.printStackTrace()
                        }
                    )
    }

    fun getMyGroupChatList() = myWidgetList

    fun ignoreRequest(inviter: String, groupId: String) {
        _requestRequest.value = Outcome.loading(true)
        compositeDisposable +
                studyGroupRepository.rejectRequest(inviter, groupId)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _requestRequest.value = Outcome.success(it.data)
                            _requestRequest.value = Outcome.loading(false)
                        },
                        {
                            _requestRequest.value = Outcome.loading(false)
                            _requestRequest.value = Outcome.apiError(it)
                            it.printStackTrace()
                        }
                    )
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }

}