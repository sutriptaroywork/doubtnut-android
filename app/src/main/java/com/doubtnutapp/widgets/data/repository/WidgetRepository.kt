package com.doubtnutapp.widgets.data.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.widgets.data.entities.BaseWidgetData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.http.Query
import javax.inject.Inject

class WidgetRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun getBottomSheetWidgetData(
        widgetType: String,
        assortmentId: String?,
        userCategory: String?,
        openCount: String?,
        questionAskCount: String?,
    )
            : Flow<BaseWidgetData> =
        flow {
            emit(
                networkService.getBottomSheetWidgetData(
                    widgetType,
                    assortmentId,
                    userCategory,
                    openCount,
                    questionAskCount
                ).data
            )
        }

    fun getPaginatedBottomSheetWidgetData(
        id: String?,
        type: String,
        tabId: String?,
        page: Int,
    )
            : Flow<BaseWidgetData> =
        flow {
            emit(
                networkService.getPaginatedBottomSheetWidgetData(
                    id = id,
                    type = type,
                    tabId = tabId,
                    page = page
                ).data
            )
        }

    fun getDialogData(
        widgetType: String,
        studentId: String?,
        assortmentId: String?,
        testId: String?,
        tabNumber: String?,
    )
            : Flow<BaseWidgetData> =
        flow {
            emit(
                networkService.getDialogData(
                    widgetType = widgetType,
                    studentId = studentId,
                    assortmentId = assortmentId,
                    testId = testId,
                    tabNumber = tabNumber
                ).data
            )
        }
}
