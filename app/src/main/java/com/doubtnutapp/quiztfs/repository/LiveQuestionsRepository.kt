package com.doubtnutapp.quiztfs.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.quiztfs.LiveQuestionsData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by Mehul Bisht on 25-08-2021
 */
class LiveQuestionsRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun fetchData(
        classCode: String,
        medium: String,
        subject: String
    ): Flow<ApiResponse<LiveQuestionsData>> =
        flow { emit(networkService.getLiveQuestionsPracticeData(classCode, medium, subject)) }

    fun fetchInitialData():
            Flow<ApiResponse<LiveQuestionsData>> =
        flow { emit(networkService.getLiveQuestionsPracticeInitialData()) }
}