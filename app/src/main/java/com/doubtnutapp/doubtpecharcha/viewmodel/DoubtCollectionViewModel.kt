package com.doubtnutapp.doubtpecharcha.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.base.extension.applyIoToMainSchedulerOnSingle
import com.doubtnutapp.base.extension.subscribeToSingle
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.deeplink.DeeplinkAction
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.doubtpecharcha.model.*
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.launch
import javax.inject.Inject

class DoubtCollectionViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    val deeplinkAction: DeeplinkAction,
) : BaseViewModel(compositeDisposable) {

    companion object {
        const val DOUBT_TYPE = "doubt_type"
        const val QUESTION_ID = "question_id"
    }

    private val doubtPeCharchaRepository = DataHandler.INSTANCE.doubtPeCharchaRepository

    private val _userDoubtsLiveData = MutableLiveData<Outcome<Pair<Int, P2PDoubtTypes>>>()
    val userDoubtsLiveData: LiveData<Outcome<Pair<Int, P2PDoubtTypes>>>
        get() = _userDoubtsLiveData

    private val _paginatedDoubtsLiveData = MutableLiveData<Outcome<Pair<Int, P2PDoubtTypes>>>()
    val paginatedUserDoubtsLiveData: LiveData<Outcome<Pair<Int, P2PDoubtTypes>>>
        get() = _paginatedDoubtsLiveData

    private val _doubtTypesLiveData = MutableLiveData<Outcome<P2PDoubtTypes>>()
    val doubtTypesLiveData: LiveData<Outcome<P2PDoubtTypes>>
        get() = _doubtTypesLiveData

    var doubtsCollection = hashMapOf<Int, List<WidgetEntityModel<*, *>>?>()
        private set

    // doubtType -> cursor mapping
    private var cursorMapping = hashMapOf<Int, Int>()

    var lastSelectedPage = 1

    fun getUserDoubts(
        primaryTabId: Int, secondaryTabId: Int = 0,
        subjects: ArrayList<Filter>, classes: ArrayList<Filter>,
        languages: ArrayList<Filter>
    ) {
        _userDoubtsLiveData.value = Outcome.loading(true)


        val subjectFilters = getFilters(subjects)
        val classesFilters = getFilters(classes)
        val languageFilters = getFilters(languages)

        val classesList = ArrayList<Int>()
        classesFilters.forEach {
            classesList.add(it.toInt())
        }

        viewModelScope.launch {
            doubtPeCharchaRepository.getDoubtData(
                primaryTabId,
                secondaryTabId,subjectFilters, classesList, languageFilters,
                cursorMapping[primaryTabId] ?: 0
            )
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    { data ->
                        val doubtList = data.doubtData.orEmpty()
                        val previousDoubtList = doubtsCollection[primaryTabId]
                        if (previousDoubtList != null) {
                            doubtsCollection[primaryTabId] = doubtList
                        } else {
                            doubtsCollection[primaryTabId] = doubtList
                        }
                        _userDoubtsLiveData.value = Outcome.loading(false)
                        _userDoubtsLiveData.postValue(Outcome.success(Pair(primaryTabId, data)))
                    },
                    {
                        _userDoubtsLiveData.value = Outcome.loading(false)
                        _userDoubtsLiveData.postValue(Outcome.failure(it))
                    }
                )
        }
    }

    private fun getFilters(
        listFilters: ArrayList<Filter>
    ): ArrayList<String> {
        return listFilters.map {
            it.id
        } as ArrayList<String>
    }

    fun getDoubtsPagination(
        primaryTabId: Int, secondaryTabId: Int = 0,
        subjects: ArrayList<Filter>, classes: ArrayList<Filter>,
        languages: ArrayList<Filter>, page: Int
    ) {

        val subjectFilters = getFilters(subjects)
        val classesFilters = getFilters(classes)
        val languageFilters = getFilters(languages)


        _doubtTypesLiveData.value = Outcome.loading(true)
        viewModelScope.launch {
            doubtPeCharchaRepository.getDoubtsForPagination(
                primaryTabId = primaryTabId,
                secondaryTabId = secondaryTabId,
                subjects = subjectFilters,
                questionClasses = classesFilters,
                questionLanguages = languageFilters,
                page = page
            )
                .applyIoToMainSchedulerOnSingle()
                .subscribeToSingle(
                    { p2pDoubts ->
                        val previousDoubtList = doubtsCollection[primaryTabId]
                        _paginatedDoubtsLiveData.value = Outcome.loading(false)
                        _paginatedDoubtsLiveData.value =
                            Outcome.success(Pair(primaryTabId, p2pDoubts))
//                        cursorMapping[p2pDoubts.activeTab] = p2pDoubts.offsetCursor
                    },
                    {
                        _paginatedDoubtsLiveData.value = Outcome.loading(false)
                        _paginatedDoubtsLiveData.value = Outcome.failure(it)
                    }
                )
        }
    }

    fun sendEvent(
        event: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(AnalyticsEvent(event, params, ignoreSnowplow))
    }
}
