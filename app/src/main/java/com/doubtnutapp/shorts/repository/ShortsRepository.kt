package com.doubtnutapp.shorts.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.shorts.model.ShortsCategoryData
import com.doubtnutapp.shorts.model.ShortsListData
import com.doubtnutapp.toRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ShortsRepository @Inject constructor(private val networkService: NetworkService) {

    fun fetchShortsList(lastId: String?, qid: String?, type: String?)
            : Flow<ApiResponse<ShortsListData>> =
        flow { emit(networkService.fetchShortsList(lastId, qid, type)) }

    fun bookmarkShortsVideo(questionId: String, isBookmarked: Boolean)
            : Flow<ApiResponse<Any>> =
        flow { emit(networkService.bookmarkShortsVideo(questionId, isBookmarked)) }

    fun updateShortsWatchFootprint(questionId: String, engageTime: Long)
            : Flow<ApiResponse<Any>> =
        flow { emit(networkService.updateShortsWatchFootprint(questionId, engageTime)) }

    fun getCategoryBottomSheet(): Flow<ApiResponse<ShortsCategoryData>> = flow {
        emit(networkService.getCategoryBottomSheet())
    }

    fun sendCategoriesData(selectedCategories: List<Int>): Flow<ApiResponse<Any>> {
        val requestBody = hashMapOf<String, Any>(
            "selected_categories" to selectedCategories
        ).toRequestBody()
        return flow { emit(networkService.sendCategoriesData(requestBody)) }
    }
}