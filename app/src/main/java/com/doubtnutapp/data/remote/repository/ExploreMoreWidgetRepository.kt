package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.ExploreMoreWidgetService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.home.model.ExploreMoreWidgetResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Created by Mehul Bisht on 05/01/22
 */

class ExploreMoreWidgetRepository(private val service: ExploreMoreWidgetService) {

    suspend fun getExploreMoreWidget(): Flow<ApiResponse<ExploreMoreWidgetResponse>> {
        return flow { emit(service.getExploreMoreWidget()) }
    }
}
