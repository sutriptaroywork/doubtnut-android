package com.doubtnutapp.widgettest.repository

import android.content.Context
import com.doubtnutapp.data.base.di.qualifier.ApplicationContext
import com.doubtnutapp.data.fromJson
import com.doubtnut.core.widgets.entities.WidgetAction
import com.doubtnut.core.widgets.entities.WidgetData
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import com.doubtnutapp.widgettest.model.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ApiTestRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {

    fun fetchWidgetData(json: String)
            : Flow<Resource<List<WidgetEntityModel<WidgetData, WidgetAction>>>> =
        flow {
            try {
                val widgets = getWidgetJson(json)
                emit(Resource.Success(widgets))
            } catch (e: Exception) {
                emit(Resource.Error<List<WidgetEntityModel<WidgetData, WidgetAction>>>("Invalid Json"))
            }
        }

    private fun getWidgetJson(json: String): List<WidgetEntityModel<WidgetData, WidgetAction>> {
        return gson.fromJson(json)
    }
}