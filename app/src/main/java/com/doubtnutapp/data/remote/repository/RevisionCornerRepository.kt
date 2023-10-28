package com.doubtnutapp.data.remote.repository

import com.doubtnutapp.data.remote.api.services.NetworkService
import com.doubtnutapp.data.remote.models.ApiResponse
import com.doubtnutapp.data.remote.models.HomeWorkQuestionData
import com.doubtnutapp.data.remote.models.HomeWorkSolutionData
import com.doubtnutapp.data.remote.models.ShortTestSubmitData
import com.doubtnutapp.data.remote.models.revisioncorner.*
import com.doubtnutapp.data.toRequestBody
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Created by devansh on 10/08/21.
 */

class RevisionCornerRepository @Inject constructor(
    private val networkService: NetworkService,
    private val gson: Gson,
) {

    fun getRevisionCornerHomeData(): Flow<ApiResponse<RevisionCornerHomeData>> =
        flow { emit(networkService.getRevisionCornerHomeData()) }

    fun getChapters(subject: String): Flow<ApiResponse<ChapterSelectionData>> =
        flow {
            val requestBody = hashMapOf("subject" to subject).toRequestBody()
            emit(networkService.getRevisionCornerChapters(requestBody))
        }

    fun getPerformanceReport(): Flow<ApiResponse<PerformanceReport>> =
        flow { emit(networkService.getPerformanceReport()) }

    fun getResultInfoData(widgetId: String): Flow<ApiResponse<ResultInfo>> =
        flow {
            val requestBody = mapOf(
                "widget_id" to widgetId,
            ).toRequestBody()
            emit(networkService.getRevisionCornerResultData(requestBody))
        }

    fun getResultListData(
        widgetId: String,
        tabId: Int,
        page: Int
    ): Flow<ApiResponse<ResultInfo>> =
        flow {
            val requestBody = mapOf(
                "widget_id" to widgetId,
                "tab_id" to tabId,
                "page" to page,
            ).toRequestBody()
            emit(networkService.getRevisionCornerResultListData(requestBody))
        }

    fun getRuleInfoData(
        widgetId: Int,
        topic: String?,
        subject: String?
    ): Flow<ApiResponse<RulesInfo>> =
        flow {
            val requestBody = mapOf(
                "widget_id" to widgetId,
                "topic" to topic.orEmpty(),
                "subject" to subject.orEmpty(),
            ).toRequestBody()
            emit(networkService.getRevisionCornerRuleData(requestBody))
        }

    fun getShortTestQuestions(
        widgetId: Int,
        chapterAlias: String
    ): Flow<ApiResponse<HomeWorkQuestionData>> =
        flow {
            val requestBody = mapOf(
                "widget_id" to widgetId,
                "chapter_alias" to chapterAlias
            ).toRequestBody()
            emit(networkService.getShortTestQuestions(requestBody))
        }

    fun submitShortTestResult(data: ShortTestSubmitData): Flow<ApiResponse<HomeWorkSolutionData>> =
        flow {
            val requestBody = mapOf(
                "all_questions" to data.allQuestions,
                "correct_questions" to data.correctQuestions,
                "incorrect_questions" to data.incorrectQuestions,
                "chapter_alias" to data.chapterAlias,
                "widget_id" to data.widgetId,
                "subject" to data.subject,
                "submitted_options" to data.submittedOptions,
            ).toRequestBody(gson)
            emit(networkService.submitShortTestResult(requestBody))
        }

    fun getPreviousShortTestResult(
        resultId: String,
        widgetId: Int
    ): Flow<ApiResponse<HomeWorkSolutionData>> =
        flow {
            val requestBody = mapOf(
                "result_id" to resultId,
                "widget_id" to widgetId,
            ).toRequestBody()
            emit(networkService.getPreviousShortTestResult(requestBody))
        }

    fun getTestListData(
        examType: String,
        page: Int,
        tabId: String
    ): Flow<ApiResponse<TestList>> =
        flow {
            val requestBody = mapOf(
                "exam_type" to examType,
                "page" to page,
                "tabId" to tabId
            ).toRequestBody()
            emit(networkService.getRevisionCornerTestListData(requestBody))
        }
}
