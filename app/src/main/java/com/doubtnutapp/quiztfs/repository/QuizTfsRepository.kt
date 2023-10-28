package com.doubtnutapp.quiztfs.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.quiztfs.*
import com.doubtnutapp.data.toRequestBody
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class QuizTfsRepository @Inject constructor(
    private val networkService: NetworkService
) {

    fun getQuizQuestion(
        isFirst: Boolean,
        studentClass: String,
        language: String,
        subject: String
    ): Flow<ApiResponse<QuizQnaInfoApi>> =
        flow { emit(networkService.getQuizQuestion(isFirst, studentClass, language, subject)) }

    fun submitQuiz(map: HashMap<String, Any>)
            : Flow<ApiResponse<QuizTfsSubmitResponse>> =
        flow { emit(networkService.submitQuiz(map.toRequestBody())) }


    fun getQuizSolution(
        id: String,
        date: String
    ): Flow<ApiResponse<QuizTfsData>> =
        flow { emit(networkService.getQuizSolution(id, date)) }

    fun getQuizTfsAnalysisData(
        page: Int,
        date: String?,
        filter: String?
    ): Flow<ApiResponse<AnalysisData>> =
        flow { emit(networkService.getAnalysisData(page, date, filter)) }

    fun getQuizTfsStatus(
        classCode: String,
        language: String,
        subject: String
    ): Flow<ApiResponse<QuizStatusData>> =
        flow { emit(networkService.getQuizStatus(classCode, language, subject)) }
}