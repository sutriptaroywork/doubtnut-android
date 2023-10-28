package com.doubtnutapp.data.remote.repository

import androidx.core.content.edit
import com.doubtnutapp.Constants
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.reward.MarkAttendanceModel
import com.doubtnutapp.data.remote.models.reward.RewardDetails
import com.doubtnutapp.defaultPrefs
import com.doubtnutapp.toInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RewardRepository @Inject constructor(
    private val networkService: NetworkService,
    private val userPreference: UserPreference,
) {
    fun markAutoDailyAttendance(): Flow<ApiResponse<MarkAttendanceModel>> {
        return flow {
            val apiResponse = networkService.markAutoDailyAttendance()
            userPreference.putRewardSystemCurrentDay(apiResponse.data.day)
            defaultPrefs().edit {
                val unscratchedCardCount = defaultPrefs().getInt(Constants.UNSCRATCHED_CARD_COUNT, 0)
                putInt(Constants.UNSCRATCHED_CARD_COUNT, unscratchedCardCount + apiResponse.data.isRewardPresent.toInt())
            }
            emit(apiResponse)
        }
    }

    fun getManualDailyAttendancePopup(): Flow<ApiResponse<MarkAttendanceModel>> {
        return flow {
            emit(networkService.markManualDailyAttendance())
        }
    }

    fun getRewardDetails(): Flow<ApiResponse<RewardDetails>> =
        flow {
            val apiResponse = networkService.getRewardDetails()
            userPreference.putRewardSystemCurrentLevel(apiResponse.data.currentLevel)
            userPreference.putRewardSystemCurrentDay(apiResponse.data.lastMarkedAttendance)
            emit(apiResponse)
        }

    fun subscribeRewardNotification(isSubscribed: Boolean): Flow<ApiResponse<Unit>> =
        flow {
            userPreference.putShowRewardSystemScratchCardReminder(isSubscribed)
            emit(networkService.subscribeRewardNotification(isSubscribed.toInt()))
        }

    fun markRewardScratched(level: Int): Flow<ApiResponse<Unit>> =
        flow {
            emit(networkService.markRewardScratched(level))
        }
}
