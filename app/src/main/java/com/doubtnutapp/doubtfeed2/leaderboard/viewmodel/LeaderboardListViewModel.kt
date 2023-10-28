package com.doubtnutapp.doubtfeed2.leaderboard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.LeaderboardStudent
import com.doubtnutapp.doubtfeed2.leaderboard.data.model.StudentData
import com.doubtnutapp.doubtfeed2.leaderboard.data.repository.LeaderboardRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Created by devansh on 12/7/21.
 */

class LeaderboardListViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val leaderboardRepository: LeaderboardRepository,
    private val analyticsPublisher: AnalyticsPublisher,
) : BaseViewModel(compositeDisposable) {

    private val _topThreeLeadersLiveData = MutableLiveData<List<LeaderboardStudent>>()
    val topThreeLeadersLiveData: LiveData<List<LeaderboardStudent>?>
        get() = _topThreeLeadersLiveData

    private val _studentDataLiveData = MutableLiveData<StudentData?>()
    val studentDataLiveData: LiveData<StudentData?>
        get() = _studentDataLiveData

    val leaderboardLiveData: LiveData<PagedList<LeaderboardStudent>> by lazy {
        LeaderboardListDataSourceFactory(
            coroutineScope = viewModelScope,
            repository = leaderboardRepository,
            id = id,
            topThreeLeadersLiveData = _topThreeLeadersLiveData,
            studentDataLiveData = _studentDataLiveData,
        ).toLiveData(
            Config(LeaderboardListDataSource.PAGE_SIZE, enablePlaceholders = false)
        )
    }

    private var id: Int = -1

    fun setListId(id: Int) {
        if (this.id == -1) {
            this.id = id
        }
    }

    fun sendEvent(
        eventName: String,
        params: HashMap<String, Any> = hashMapOf(),
        ignoreSnowplow: Boolean = false
    ) {
        analyticsPublisher.publishEvent(
            AnalyticsEvent(
                eventName,
                params,
                ignoreSnowplow = ignoreSnowplow
            )
        )
    }

    /**
     * Data source related classes to get leaderboard list data using Paging library
     */
    private class LeaderboardListDataSourceFactory(
        private val coroutineScope: CoroutineScope,
        private val repository: LeaderboardRepository,
        private val id: Int,
        private val topThreeLeadersLiveData: MutableLiveData<List<LeaderboardStudent>>,
        private val studentDataLiveData: MutableLiveData<StudentData?>,
    ) : DataSource.Factory<Int, LeaderboardStudent>() {

        override fun create(): DataSource<Int, LeaderboardStudent> =
            LeaderboardListDataSource(
                coroutineScope, repository, id, topThreeLeadersLiveData, studentDataLiveData
            )
    }

    private class LeaderboardListDataSource(
        private val coroutineScope: CoroutineScope,
        private val repository: LeaderboardRepository,
        private val id: Int,
        private val topThreeLeadersLiveData: MutableLiveData<List<LeaderboardStudent>>,
        private val studentDataLiveData: MutableLiveData<StudentData?>,
    ) : PageKeyedDataSource<Int, LeaderboardStudent>() {

        companion object {
            const val PAGE_SIZE = 10
            const val INITIAL_PAGE = 0
        }

        override fun loadInitial(
            params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<Int, LeaderboardStudent>
        ) {
            getData(INITIAL_PAGE) {
                callback.onResult(it, null, INITIAL_PAGE + 1)
            }
        }

        override fun loadBefore(
            params: LoadParams<Int>,
            callback: LoadCallback<Int, LeaderboardStudent>
        ) {
        }

        override fun loadAfter(
            params: LoadParams<Int>,
            callback: LoadCallback<Int, LeaderboardStudent>
        ) {
            getData(params.key) {
                callback.onResult(it, params.key + 1)
            }
        }

        private fun getData(page: Int, callback: (List<LeaderboardStudent>) -> Unit) {
            coroutineScope.launch {
                repository.getLeaderboardList(id, page)
                    .catch { }
                    .collect {
                        if (it.data.leaderboardData != null && it.data.leaderboardData!!.isNotEmpty()) {
                            if (page == INITIAL_PAGE) {
                                topThreeLeadersLiveData.value = it.data.leaderboardData!!.take(3)
                                studentDataLiveData.value = it.data.studentData
                                callback(it.data.leaderboardData!!.drop(3))
                            } else {
                                callback(it.data.leaderboardData!!)
                            }
                        }
                    }
            }
        }
    }
}
