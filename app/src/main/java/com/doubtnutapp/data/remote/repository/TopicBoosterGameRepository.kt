package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.topicboostergame.*
import com.doubtnutapp.toInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by devansh on 26/2/21.
 */

class TopicBoosterGameRepository @Inject constructor(private val networkService: NetworkService) {

    fun getTopicBoosterGameBanner(questionId: String): Flow<ApiResponse<TopicBoosterGameBannerData>?> {
        return flow {
            emit(networkService.getTopicBoosterGameBanner(questionId))
        }
    }

    fun getTopicBoosterGameQuestionsList(
        testUuid: String,
        chapterAlias: String,
        totalQuestions: Int,
        expiry: Int
    ):
        Flow<ApiResponse<List<TopicBoosterGameQuestion>>?> {
        return flow {
            emit(networkService.getTopicBoosterGameQuestionsList(testUuid, chapterAlias, totalQuestions, expiry))
        }
    }

    fun saveUserResponse(userResponse: TopicBoosterGameUserResponse):
        Flow<ApiResponse<TopicBoosterGameSaveResponseResult>> {
        return flow {
            emit(networkService.saveUserResponse(userResponse))
        }
    }

    fun submitResult(gameResult: Int, isWalletReward: Boolean): Flow<ApiResponse<TopicBoosterGameResult>> {
        return flow {
            emit(networkService.submitResult(gameResult, isWalletReward.toInt()))
        }
    }
}
