package com.doubtnutapp.data.remote.repository

import androidx.collection.arrayMapOf
import com.doubtnutapp.data.common.UserPreference
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.doubtfeed.*
import com.doubtnutapp.data.toRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by devansh on 7/5/21.
 */

class DoubtFeedRepository @Inject constructor(
    private val networkService: NetworkService,
    private val userPreference: UserPreference,
) {

    private val topicsDoubtFeedMap = arrayMapOf<String, ApiResponse<DoubtFeed>>()

    fun getDoubtFeed(topicId: String?): Flow<ApiResponse<DoubtFeed>> {
        return flow {
            if (topicId in topicsDoubtFeedMap) {
                emit(topicsDoubtFeedMap[topicId]!!)
            } else {
                emit(networkService.getDoubtFeed(topicId))
            }
        }
    }

    fun cacheDoubtFeedApiResponse(topicId: String, apiResponse: ApiResponse<DoubtFeed>) {
        topicsDoubtFeedMap[topicId] = apiResponse
    }

    fun getDoubtFeedProgress(topicId: String): Flow<ApiResponse<DoubtFeedProgress>> {
        return flow {
            emit(networkService.getDoubtFeedProgress(topicId))
        }
    }

    fun submitDoubtCompletion(goalId: Int): Flow<ApiResponse<DoubtFeedDailyGoalTaskCompletedPopupData>> {
        return flow {
            emit(networkService.submitDoubtCompletion(goalId))
        }
    }

    fun getDoubtFeedStatus(): Flow<ApiResponse<DoubtFeedStatus>> {
        return flow {
            val apiResponse = networkService.getDoubtFeedStatus()
            userPreference.setDoubtFeedAvailable(apiResponse.data.isDoubtFeedAvailable)
            emit(apiResponse)
        }
    }

    fun getDoubtFeedVideoBanner(chapter: String): Flow<ApiResponse<DoubtFeedBanner>> {
        return flow {
            val requestBody = hashMapOf("chapter" to chapter).toRequestBody()
            emit(networkService.getDoubtFeedVideoBanner(requestBody))
        }
    }
}
