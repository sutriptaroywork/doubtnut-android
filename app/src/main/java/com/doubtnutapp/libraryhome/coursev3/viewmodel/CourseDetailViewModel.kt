package com.doubtnutapp.libraryhome.coursev3.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.doubtnut.analytics.EventConstants
import com.doubtnutapp.base.BaseViewModel
import com.doubtnutapp.data.Outcome
import com.doubtnutapp.data.remote.DataHandler
import com.doubtnutapp.data.remote.RetrofitLiveData
import com.doubtnutapp.data.remote.models.ApiCourseDetailData
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.Comment
import com.doubtnutapp.domain.newglobalsearch.entities.SearchFilter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.net.HttpURLConnection
import javax.inject.Inject

class CourseDetailViewModel @Inject constructor(
    compositeDisposable: CompositeDisposable,
) : BaseViewModel(compositeDisposable) {

    var extraParams: HashMap<String, Any> = hashMapOf()
    var isFilterV2Enabled: Boolean = false
    var v2Filters: ArrayList<SearchFilter> = arrayListOf()
    var apiQueryParams: HashMap<String, String> = hashMapOf()
    var v2filterQueryParams: HashMap<String, String> = hashMapOf()
    var isCourseFilterApplied: Boolean = false
    var itemsAdded = 0

    private val _widgetsLiveData: MutableLiveData<Outcome<ApiCourseDetailData>> = MutableLiveData()

    val widgetsLiveData: LiveData<Outcome<ApiCourseDetailData>>
        get() = _widgetsLiveData

    private fun startLoading() {
        _widgetsLiveData.value = Outcome.loading(true)
    }

    private fun stopLoading() {
        _widgetsLiveData.value = Outcome.loading(false)
    }

    fun fetchCourseData(
        page: Int,
        assortmentId: String,
        tab: String,
        subTabId: String?,
        subject: String?,
        notesType: String?,
        queryParams: HashMap<String, String> = hashMapOf()
    ) {
        startLoading()
        viewModelScope.launch {
            queryParams.apply {
                put("page", "$page")
                put("assortment_id", assortmentId)
                put("tab", tab)
                put("sub_tab_id", subTabId.orEmpty())
                put("subject", subject.orEmpty())
                put("notes_type", notesType.orEmpty())
            }
            DataHandler.INSTANCE.courseRepository.getCourseDetail(isFilterV2Enabled, queryParams)
                .map {
                    it.data.widgets?.mapIndexed { index, widget ->
                        if (widget != null) {
                            if (widget.extraParams == null) {
                                widget.extraParams = hashMapOf()
                            }
                            widget.extraParams?.putAll(extraParams)
                            widget.extraParams?.put(
                                EventConstants.ITEM_PARENT_POSITION,
                                itemsAdded + index
                            )
                        }
                    }
                    it.data.filterWidgets?.mapIndexed { index, widget ->
                        if (widget != null) {
                            if (widget.extraParams == null) {
                                widget.extraParams = hashMapOf()
                            }
                            widget.extraParams?.putAll(extraParams)
                            widget.extraParams?.put(
                                EventConstants.ITEM_PARENT_POSITION,
                                itemsAdded + index
                            )
                        }
                    }
                    it.data
                }
                .catch {
                    onError(it)
                }
                .collect {
                    if (!it.widgets.isNullOrEmpty()) {
                        itemsAdded.plus(it.widgets.size)
                    }
                    _widgetsLiveData.value = Outcome.success(it)
                    stopLoading()
                }
        }
    }

    private fun onError(error: Throwable) {
        stopLoading()
        _widgetsLiveData.value = if (error is HttpException) {
            when (error.response()?.code()) {
                HttpURLConnection.HTTP_UNAUTHORIZED -> Outcome.BadRequest(
                    error.message
                        ?: ""
                )
                HttpURLConnection.HTTP_BAD_REQUEST -> Outcome.ApiError(error)
                else -> Outcome.Failure(error)
            }
        } else {
            Outcome.Failure(error)
        }
    }

    fun updateV2Filters(newV2Filters: java.util.ArrayList<SearchFilter>) {
        v2Filters.clear()
        v2Filters.addAll(newV2Filters.filter { !it.filters.isNullOrEmpty() })
    }

    fun getComments(
        authToken: String,
        entityType: String,
        entityId: String,
        page: String
    ): RetrofitLiveData<ApiResponse<ArrayList<Comment>>> {
        return DataHandler.INSTANCE.commentRepository.getComments(
            token = authToken,
            entityType = entityType,
            entityId = entityId,
            page = page,
            filter = null,
            batchId = null
        )
    }

}