package com.doubtnutapp.studygroup.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.doubtnut.core.common.data.entity.SgWidgetListData
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.DoubtnutApp
import com.doubtnutapp.Features
import com.doubtnutapp.FeaturesManager
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.plus
import com.doubtnutapp.studygroup.model.*
import com.doubtnutapp.studygroup.service.StudyGroupRepository
import com.doubtnutapp.studygroup.ui.activity.StudyGroupActivity
import com.doubtnutapp.studygroup.ui.fragment.SgMyChatsFragment
import com.doubtnutapp.studygroup.ui.fragment.SgMyGroupsFragment
import com.doubtnutapp.utils.Event
import io.reactivex.disposables.CompositeDisposable
import java.util.*
import javax.inject.Inject

class SgHomeViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val userPreference: UserPreference,
    private val analyticsPublisher: AnalyticsPublisher,
    private val studyGroupRepository: StudyGroupRepository
) : BaseViewModel(compositeDisposable) {

    private val _sgWidgetListLiveData: MutableLiveData<Event<Outcome<SgWidgetListData>>> =
        MutableLiveData()
    val sgWidgetListLiveData: LiveData<Event<Outcome<SgWidgetListData>>>
        get() = _sgWidgetListLiveData

    private val _sgWidgetSearchResultLiveData: MutableLiveData<Event<Outcome<SgWidgetListData>>> =
        MutableLiveData()
    val sgWidgetSearchResultLiveData: LiveData<Event<Outcome<SgWidgetListData>>>
        get() = _sgWidgetSearchResultLiveData

    private val _settingLiveData: MutableLiveData<SgSetting> = MutableLiveData()
    val settingLiveData: LiveData<SgSetting>
        get() = _settingLiveData

    private val _metaDataLiveData: MutableLiveData<SgMetaData> = MutableLiveData()
    val metaDataLiveData: LiveData<SgMetaData>
        get() = _metaDataLiveData

    private var initialWidgetList: MutableList<WidgetEntityModel<*, *>> = mutableListOf()
    val messageLiveData: MutableLiveData<String> = MutableLiveData()

    fun getSgGroupList(page: Int, pageType: String) {
        val showJoinGroupWidget: Boolean = getJoinNewGroupFlaggerVariant() == 2
        _sgWidgetListLiveData.value = Event(Outcome.loading(true))
        compositeDisposable +
                studyGroupRepository.getGroupChatList(page, pageType, showJoinGroupWidget)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            val data = it.data
                            if (page == SgMyGroupsFragment.INITIAL_PAGE) {
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
                                initialWidgetList.clear()
                            }
                            _sgWidgetListLiveData.value = Event(Outcome.success(data))
                            initialWidgetList.addAll(it.data.widgets.orEmpty().toList())
                            _sgWidgetListLiveData.value = Event(Outcome.loading(false))
                        },
                        {
                            _sgWidgetListLiveData.value = Event(Outcome.loading(false))
                            _sgWidgetListLiveData.value = Event(Outcome.apiError(it))
                            it.printStackTrace()
                        }
                    )
    }

    fun getSgChatList(page: Int) {
        _sgWidgetListLiveData.value = Event(Outcome.loading(true))
        compositeDisposable +
                studyGroupRepository.getPersonalChatList(page)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            val data = it.data
                            _sgWidgetListLiveData.value = Event(Outcome.success(data))
                            if (page == SgMyChatsFragment.INITIAL_PAGE) {
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
                                initialWidgetList.clear()
                            }
                            initialWidgetList.addAll(it.data.widgets.orEmpty().toList())
                            _sgWidgetListLiveData.value = Event(Outcome.loading(false))
                        },
                        {
                            _sgWidgetListLiveData.value = Event(Outcome.loading(false))
                            _sgWidgetListLiveData.value = Event(Outcome.apiError(it))
                            it.printStackTrace()
                        }
                    )
    }

    fun getSearchResultList(keyword: String, page: Int) {
        _sgWidgetSearchResultLiveData.value = Event(Outcome.loading(true))
        compositeDisposable +
                studyGroupRepository.getSearchResultWidgetList(
                    keyword,
                    page,
                    _metaDataLiveData.value?.search?.source.orEmpty()
                )
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            _sgWidgetSearchResultLiveData.value = Event(Outcome.success(it.data))
                            _sgWidgetSearchResultLiveData.value = Event(Outcome.loading(false))
                        },
                        {
                            _sgWidgetSearchResultLiveData.value = Event(Outcome.loading(false))
                            _sgWidgetSearchResultLiveData.value = Event(Outcome.apiError(it))
                            it.printStackTrace()
                        }
                    )
    }

    fun getSgSetting() {
        compositeDisposable + studyGroupRepository.getSgSetting()
            .applyIoToMainSchedulerOnSingle()
            .subscribeToSingle(
                {
                    _settingLiveData.value = it.data
                },
                {
                    it.printStackTrace()
                }
            )
    }

    fun muteNotification(type: Int, action: StudyGroupActivity.ActionSource) {
        compositeDisposable +
                studyGroupRepository.muteNotification(type = type, action = action)
                    .applyIoToMainSchedulerOnSingle()
                    .subscribeToSingle(
                        {
                            messageLiveData.postValue(it.message)
                            setStudyGroupNotificationMuteStatus(type == 0)
                        },
                        {
                            it.printStackTrace()
                        }
                    )
    }

    fun getInitialWidgetList() = initialWidgetList

    fun updateInitialWidgetList(updatedWidgetList: List<WidgetEntityModel<*, *>>) {
        initialWidgetList.clear()
        initialWidgetList.addAll(updatedWidgetList)
    }

    private fun setStudyGroupNotificationMuteStatus(isMute: Boolean) =
        userPreference.setStudyGroupNotificationMuteStatus(isMute)

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }

    fun getJoinNewGroupFlaggerVariant(): Int {
        return FeaturesManager.getVariantNumber(
            DoubtnutApp.INSTANCE,
            Features.JOIN_NEW_STUDY_GROUP
        )
    }
}