package com.doubtnutapp.data.remote.repository

import androidx.collection.SimpleArrayMap
import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.doubtfeed2.DailyGoalSubmitResponse
import com.doubtnutapp.data.remote.models.doubtfeed2.DoubtFeed
import com.doubtnutapp.widgetmanager.widgets.DoubtFeedWidget
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by devansh on 7/5/21.
 */

class DoubtFeedRepository2 @Inject constructor(private val networkService: NetworkService) {

    private val topicsDoubtFeedMap = SimpleArrayMap<String, ApiResponse<DoubtFeed>>()
    private val previousTopicsDoubtFeedMap =
        SimpleArrayMap<String, ApiResponse<DoubtFeedWidget.Data>>()

    fun getDoubtFeed(topicId: String?, cached: Boolean): Flow<ApiResponse<DoubtFeed>> {
        return flow {
            if (topicsDoubtFeedMap.containsKey(topicId) && cached) {
                emit(topicsDoubtFeedMap[topicId]!!)
            } else {
                emit(networkService.getDoubtFeed2(topicId))
            }
        }
    }

    fun cacheDoubtFeedApiResponse(topicId: String?, apiResponse: ApiResponse<DoubtFeed>) {
        if (topicId != null) {
            topicsDoubtFeedMap.put(topicId, apiResponse)
        }
    }

    fun submitDoubtCompletion(goalId: Int): Flow<ApiResponse<DailyGoalSubmitResponse>> {
        return flow {
            emit(networkService.submitDoubtCompletion2(goalId))
        }
    }

    fun submitDoubtCompletionForPrevious(goalId: Int): Flow<Unit> {
        return flow {
            emit(networkService.submitDoubtCompletionForPrevious(goalId))
        }
    }

    fun getPreviousDoubtFeed(topicId: String?): Flow<ApiResponse<DoubtFeedWidget.Data>> {
        return flow {
            if (previousTopicsDoubtFeedMap.containsKey(topicId)) {
                emit(previousTopicsDoubtFeedMap[topicId]!!)
            } else {
                emit(networkService.getPreviousDoubtFeed(topicId))
            }
        }
    }

    fun cachePreviousDoubtFeedApiResponse(
        topicId: String?,
        apiResponse: ApiResponse<DoubtFeedWidget.Data>
    ) {
        if (topicId != null) {
            previousTopicsDoubtFeedMap.put(topicId, apiResponse)
        }
    }

    fun markStreak(): Flow<ApiResponse<Unit>> {
        return flow {
            emit(networkService.markDfStreak())
        }
    }
}
