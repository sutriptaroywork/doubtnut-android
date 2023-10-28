package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.bottomnavigation.model.BottomNavigationItemData
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.feed.TopOptionsWidgetData
import com.doubtnutapp.db.DoubtnutDatabase
import com.doubtnut.core.widgets.entities.WidgetEntityModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by devansh on 25/1/21.
 */

class TopOptionRepository @Inject constructor(
    private val networkService: NetworkService,
    private val database: DoubtnutDatabase
) {

    suspend fun getCameraNavigationTopIcons(iconCount: Int): Flow<ApiResponse<WidgetEntityModel<TopOptionsWidgetData, *>>> {
        return flow {
            val apiResponse = networkService.getCameraNavigationTopIcons(iconCount)
            database.topOptionWidgetItemDao().apply {
                deleteAllTopOptions()
                insertTopOptions(apiResponse.data.data.items)
            }
            emit(apiResponse)
        }
    }

    suspend fun getCameraBottomIcons(isDoubtFeedAvailable: Boolean): Flow<ApiResponse<List<BottomNavigationItemData>>> {
        return flow {
            emit(networkService.getCameraBottomIcons(isDoubtFeedAvailable))
        }
    }
}
