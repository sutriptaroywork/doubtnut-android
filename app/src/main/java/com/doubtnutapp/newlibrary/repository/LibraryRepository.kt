package com.doubtnutapp.studygroup.service

import com.doubtnutapp.data.newlibrary.model.ApiLibraryExamBottomSheetData
import com.doubtnutapp.data.newlibrary.service.LibraryTabService
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import io.reactivex.Single
import okhttp3.RequestBody
import javax.inject.Inject

class LibraryRepository @Inject constructor(
    private val libraryTabService: LibraryTabService,
) {

    fun getLibraryWidgets(): Single<List<WidgetEntityModel<WidgetData, WidgetAction>>> =
        libraryTabService.getLibraryWidgets().map {
            it.data.list
        }

    fun getExamWidgets(widgetId: String, commaSeparatedTabIds: String?): Single<ApiLibraryExamBottomSheetData> =
        libraryTabService.getLibraryExamsWidgets(widgetId, commaSeparatedTabIds).map {
            it.data
        }

    fun changeExam(requestBody: RequestBody): Single<WidgetEntityModel<WidgetData, WidgetAction>> =
        libraryTabService.changeExam(requestBody).map {
            it.data
        }
}