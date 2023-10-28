package com.doubtnutapp.data.newlibrary.service

import com.doubtnutapp.data.common.model.ApiResponse
import com.doubtnutapp.data.newlibrary.model.ApiLibraryExamBottomSheetData
import com.doubtnutapp.data.newlibrary.model.ApiLibraryTabData
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import io.reactivex.Single
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LibraryTabService {

    @GET("v9/library/getall")
    fun getLibraryWidgets(): Single<ApiResponse<ApiLibraryTabData>>

    @GET("v9/library/get-all-exams")
    fun getLibraryExamsWidgets(
        @Query("widget_id") widgetId: String,
        @Query("tab_ids") commaSeparatedTabIds: String?
    ): Single<ApiResponse<ApiLibraryExamBottomSheetData>>

    @POST("v9/library/change-exam")
    fun changeExam(@Body requestBody: RequestBody): Single<ApiResponse<WidgetEntityModel<WidgetData, WidgetAction>>>
}
