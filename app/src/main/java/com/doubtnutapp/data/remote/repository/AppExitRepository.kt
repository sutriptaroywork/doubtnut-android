package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.AppExitDialogData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by devansh on 9/1/21.
 */

class AppExitRepository @Inject constructor(
    private val networkService: NetworkService,
    private val userPreference: UserPreference
) {

    suspend fun getAppExitDialogData(experiment: Int, listCount: Int): Flow<ApiResponse<AppExitDialogData>> {
        return flow {
            emit(networkService.getAppExitDialogData(1, experiment, listCount))
        }
    }

    suspend fun askIfCanShowAppExitDialog(experiment: Int, listCount: Int): Flow<ApiResponse<AppExitDialogData>> {
        return flow {
            emit(networkService.getAppExitDialogData(0, experiment, listCount))
        }
    }

    fun setAppExitDialogShownInCurrentSession(shown: Boolean) {
        userPreference.setAppExitDialogShownInCurrentSession(shown)
    }

    fun getAppExitDialogShownInCurrentSession(): Boolean =
        userPreference.getAppExitDialogShownInCurrentSession()
}
