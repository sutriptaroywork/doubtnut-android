package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.videoPage.model.ApiVideoPagePlaylist
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by devansh on 16/4/21.
 */

/**
 * Contains Coroutines based methods and API calls for video page
 */
class VideoPageRepository2 @Inject constructor(private val networkService: NetworkService) {

    fun getVideoPlaylist(questionId: String): Flow<ApiResponse<ApiVideoPagePlaylist>> {
        return flow {
            emit(networkService.getVideoPlaylist(questionId))
        }
    }
}
