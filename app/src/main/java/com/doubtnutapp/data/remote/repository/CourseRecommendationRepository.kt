package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.CourseRecommendationResponse
import com.doubtnutapp.data.toRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CourseRecommendationRepository @Inject constructor(private val networkService: NetworkService) {

    fun fetchCourseRecommendationData(
        initiate: Boolean,
        isBack: Boolean,
        sessionId: String,
        messageId: String,
        selectedOptionKey: String,
        page: String
    ): Flow<ApiResponse<CourseRecommendationResponse>> =
        flow {
            emit(
                networkService.fetchCourseRecommendation(
                    hashMapOf<String, Any>().apply {
                        put("initiate", initiate)
                        put("is_back", isBack)
                        put("session_id", sessionId)
                        put("message_id", messageId)
                        put("selected_option_key", selectedOptionKey)
                        put("page", page)
                    }.toRequestBody()
                )
            )
        }
}
