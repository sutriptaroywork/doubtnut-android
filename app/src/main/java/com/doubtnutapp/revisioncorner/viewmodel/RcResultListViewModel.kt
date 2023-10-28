package com.doubtnutapp.revisioncorner.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.doubtnutapp.analytics.AnalyticsPublisher
import com.doubtnut.core.entitiy.AnalyticsEvent
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.remote.models.revisioncorner.Result
import com.doubtnutapp.data.remote.models.revisioncorner.ResultInfo
import com.doubtnutapp.data.remote.repository.RevisionCornerRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.HashMap
import javax.inject.Inject

class RcResultListViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
    private val analyticsPublisher: AnalyticsPublisher,
    private val revisionCornerRepository: RevisionCornerRepository,
) : BaseViewModel(compositeDisposable) {

    val resultListLiveData: LiveData<PagedList<Result>> by lazy {
        ResultListDataSourceFactory(
            coroutineScope = viewModelScope,
            repository = revisionCornerRepository,
            tabId = tabId,
            widgetId = widgetId.orEmpty()
        ).toLiveData(
            Config(ResultListDataSource.PAGE_SIZE, enablePlaceholders = false)
        )
    }

    private var tabId: Int = -1
    private var widgetId: String? = null

    fun setListId(widgetId: String, tabId: Int) {
        if (this.widgetId.isNullOrEmpty()) {
            this.widgetId = widgetId
        }
        if (this.tabId == -1) {
            this.tabId = tabId
        }
    }

    private class ResultListDataSourceFactory(
        private val coroutineScope: CoroutineScope,
        private val repository: RevisionCornerRepository,
        private val tabId: Int,
        private val widgetId: String,
    ) : DataSource.Factory<Int, Result>() {

        override fun create(): DataSource<Int, Result> =
            ResultListDataSource(
                coroutineScope, repository, tabId, widgetId
            )
    }

    private class ResultListDataSource(
        private val coroutineScope: CoroutineScope,
        private val repository: RevisionCornerRepository,
        private val tabId: Int,
        private val widgetId: String,
    ) : PageKeyedDataSource<Int, Result>() {

        companion object {
            const val PAGE_SIZE = 10
            const val INITIAL_PAGE = 0
        }

        override fun loadInitial(
            params: LoadInitialParams<Int>,
            callback: LoadInitialCallback<Int, Result>
        ) {
            getData(INITIAL_PAGE) {
                callback.onResult(it.results.orEmpty(), null, it.nextPage)
            }
        }

        override fun loadBefore(params: LoadParams<Int>, callback: LoadCallback<Int, Result>) {
        }

        override fun loadAfter(params: LoadParams<Int>, callback: LoadCallback<Int, Result>) {
            getData(params.key) {
                callback.onResult(it.results.orEmpty(), it.nextPage)
            }
        }

        private fun getData(page: Int, callback: (ResultInfo) -> Unit) {
            coroutineScope.launch {
                repository.getResultListData(widgetId, tabId, page)
                    .catch { }
                    .collect {
                        callback(it.data)
                    }
            }
        }
    }

    fun sendEvent(eventName: String, params: HashMap<String, Any> = hashMapOf(), ignoreSnowplow: Boolean = false) {
        analyticsPublisher.publishEvent(AnalyticsEvent(eventName, params, ignoreSnowplow = ignoreSnowplow))
    }
}