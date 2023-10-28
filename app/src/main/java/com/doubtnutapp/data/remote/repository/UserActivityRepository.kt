package com.doubtnutapp.data.remote.repository

import androidx.core.content.edit
import com.doubtnutapp.Constants
import com.doubtnutapp.CoreActions
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.StoreActivityResponse
import com.doubtnutapp.defaultPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by devansh on 11/1/21.
 */

class UserActivityRepository @Inject constructor(private val networkService: NetworkService) {

    suspend fun storeCoreActionDone(actionName: String): Flow<ApiResponse<StoreActivityResponse>> {
        if (defaultPrefs().getBoolean(actionName, false).not()) {
            setCoreActionStatusInPref(actionName, true)
            return flow {
                emit(networkService.storeUserActivity(Constants.ACTIVITY, actionName, null))
            }
        }
        return emptyFlow()
    }

    suspend fun storeAppOpenActivity(): Flow<ApiResponse<StoreActivityResponse>> {
        return flow {
            emit(networkService.storeUserActivity(Constants.APP_OPEN, null, null))
        }
    }

    suspend fun resetCoreActions(): Flow<ApiResponse<StoreActivityResponse>> {
        return flow {
            emit(networkService.storeUserActivity(Constants.RESET, null, Constants.ACTIVITY))
            CoreActions.appExitCoreActions.forEach {
                setCoreActionStatusInPref(it, false)
            }
        }
    }

    suspend fun resetAppOpenCountOnBackend(): Flow<ApiResponse<StoreActivityResponse>> {
        return flow {
            emit(networkService.storeUserActivity(Constants.RESET, null, Constants.APP_OPEN))
        }
    }

    fun setCoreActionStatusInPref(actionName: String, isDone: Boolean) {
        defaultPrefs().edit {
            putBoolean(actionName, isDone)
        }
    }
}
